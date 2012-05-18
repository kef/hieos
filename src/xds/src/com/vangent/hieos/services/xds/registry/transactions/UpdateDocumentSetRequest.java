/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xds.registry.transactions;

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateHelper;
import com.vangent.hieos.services.xds.registry.mu.command.UpdateDocumentSetController;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.ActorType;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.IHETransaction;
import com.vangent.hieos.xutil.atna.ATNAAuditEventHelper;
import com.vangent.hieos.xutil.atna.ATNAAuditEventRegisterDocumentSet;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.exception.XDSMetadataVersionException;
import com.vangent.hieos.xutil.exception.XDSNonIdenticalHashException;
import com.vangent.hieos.xutil.exception.XDSPatientIDReconciliationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.xutil.registry.RegistryUtility;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateDocumentSetRequest extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(UpdateDocumentSetRequest.class);

    /**
     *
     * @param logMessage
     */
    public UpdateDocumentSetRequest(XLogMessage logMessage) {
        this.log_message = logMessage;
        try {
            init(new RegistryResponse());
        } catch (XdsInternalException e) {
            logger.fatal(logger_exception_details(e));
        }
    }

    /**
     *
     * @param submitObjectsRequest
     * @return
     */
    public OMElement run(OMElement submitObjectsRequest) {
        try {
            submitObjectsRequest.build();
            this.auditUpdateDocumentSetRequest(submitObjectsRequest);
            this.handleUpdateDocumentSetRequest(submitObjectsRequest);
        } catch (XdsInternalException e) {
            response.add_error(MetadataSupport.XDSRegistryError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XDSPatientIDReconciliationException e) {
            response.add_error(MetadataSupport.XDSPatientIDReconciliationError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XDSMetadataVersionException e) {
            response.add_error(MetadataSupport.XDSMetadataVersionError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XDSNonIdenticalHashException e) {
            response.add_error(MetadataSupport.XDSNonIdenticalHash, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (SchemaValidationException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (MetadataException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (MetadataValidationException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XdsException e) {
            response.add_error(MetadataSupport.XDSRegistryError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (Exception e) {
            response.add_error(MetadataSupport.XDSRegistryError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        }
        OMElement res = null;
        try {
            res = response.getResponse();
            this.log_response();
        } catch (XdsInternalException e) {
        }
        return res;
    }

    /**
     * 
     * @param submitObjectsRequest
     * @throws XdsInternalException
     * @throws SchemaValidationException
     */
    private void handleUpdateDocumentSetRequest(OMElement submitObjectsRequest) throws XdsInternalException, SchemaValidationException, MetadataException, MetadataValidationException, XdsException {
        // Validate input message against XML schema.

        // FIXME: MetadataTypes.METADATA_TYPE_Rb?
        RegistryUtility.schema_validate_local(submitObjectsRequest, MetadataTypes.METADATA_TYPE_Rb);
        boolean commitCompleted = false;

        // Get backend registry instance.
        BackendRegistry backendRegistry = new BackendRegistry(log_message);
        try {
            // Build MetadataUpdateContext.
            MetadataUpdateContext metadataUpdateContext = this.buildMetadataUpdateContext(backendRegistry);

            // Create Metadata instance for SOR.
            Metadata submittedMetadata = new Metadata(submitObjectsRequest);  // Create meta-data instance for SOR.
            MetadataUpdateHelper.logMetadata(log_message, submittedMetadata);

            // Run commands and register submission set.
            UpdateDocumentSetController controller = new UpdateDocumentSetController(metadataUpdateContext, submittedMetadata);
            boolean runStatus = controller.execute();
            if (runStatus) {
                // Commit on success.
                backendRegistry.commit();
                commitCompleted = true;
            }
        } finally {
            // Rollback if commit not completed above.
            if (!commitCompleted) {
                backendRegistry.rollback();
            }
        }
    }

    /**
     *
     * @param backendRegistry
     * @return
     */
    private MetadataUpdateContext buildMetadataUpdateContext(BackendRegistry backendRegistry) {
        // Prepare for queries.
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                response, log_message, backendRegistry);
        muSQ.setReturnLeafClass(true);

        // Create the context.
        MetadataUpdateContext metadataUpdateContext = new MetadataUpdateContext();
        metadataUpdateContext.setBackendRegistry(backendRegistry);
        metadataUpdateContext.setLogMessage(log_message);
        metadataUpdateContext.setRegistryResponse((RegistryResponse) response);
        metadataUpdateContext.setConfigActor(this.getConfigActor());
        metadataUpdateContext.setStoredQuerySupport(muSQ);
        return metadataUpdateContext;
    }

    /**
     *
     * @param rootNode
     */
    private void auditUpdateDocumentSetRequest(OMElement rootNode) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                // Create and log audit event.
                ATNAAuditEventRegisterDocumentSet auditEvent = ATNAAuditEventHelper.getATNAAuditEventRegisterDocumentSet(rootNode);
                auditEvent.setActorType(ActorType.REGISTRY);
                auditEvent.setTransaction(IHETransaction.ITI57);
                auditEvent.setAuditEventType(ATNAAuditEvent.AuditEventType.IMPORT);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            // FIXME?:
        }
    }
}
