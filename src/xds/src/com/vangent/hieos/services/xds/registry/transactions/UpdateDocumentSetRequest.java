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
import com.vangent.hieos.services.xds.registry.mu.command.MetadataUpdateCommand;
import com.vangent.hieos.services.xds.registry.mu.command.SubmitAssociationCommand;
import com.vangent.hieos.services.xds.registry.mu.command.UpdateDocumentEntryMetadataCommand;
import com.vangent.hieos.services.xds.registry.mu.command.UpdateFolderMetadataCommand;
import com.vangent.hieos.services.xds.registry.mu.command.UpdateStatusCommand;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.services.xds.registry.storedquery.RegistryObjectValidator;
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
import java.util.ArrayList;
import java.util.List;

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

            // Validate metadata structure.
            RegistryObjectValidator rov = new RegistryObjectValidator(response, log_message, backendRegistry);
            rov.validateMetadataStructure(submittedMetadata, true /* isSubmit */, response.registryErrorList);
            if (!response.has_errors()) {
                // Make sure that submission set is unique.
                rov.validateSubmissionSetUniqueIds(submittedMetadata);

                // Make sure registry knows about the patient id.
                rov.validatePatientId(submittedMetadata, this.getConfigActor());
                rov.validateConsistentPatientId(true, submittedMetadata);

                // Build list of MU commands (Command Pattern) to execute.
                List<MetadataUpdateCommand> muCommands = this.buildMetadataUpdateCommands(submittedMetadata, metadataUpdateContext);

                // Execute each MU command.
                boolean runStatus = this.runMetadataUpdateCommands(muCommands);
                if (runStatus) {
                    // Commit on success.
                    backendRegistry.commit();
                    commitCompleted = true;
                }
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
     * @param submittedMetadata
     * @param metadataUpdateContext
     * @return
     * @throws MetadataException
     */
    private List<MetadataUpdateCommand> buildMetadataUpdateCommands(Metadata submittedMetadata, MetadataUpdateContext metadataUpdateContext) throws MetadataException, XdsException {
        // Technical operation cases:
        // - Update DocumentEntry Metadata
        // - Update DocumentEntry Status
        // - Update Folder Metadata
        // - Update Folder Status
        // - Update Association Status
        // - Submit new Assoication object(s)

        // Build list of commands (Command Pattern) to execute.
        List<MetadataUpdateCommand> muCommands = new ArrayList<MetadataUpdateCommand>();
        for (OMElement assoc : submittedMetadata.getAssociations()) {
            // See if we are dealing with a proper association.
            MetadataUpdateCommand muCommand = null;
            if (MetadataSupport.xdsB_ihe_assoc_type_update_availability_status.equals(submittedMetadata.getAssocType(assoc))) {
                muCommand = this.handleUpdateAvailabilityStatusAssociation(submittedMetadata, metadataUpdateContext, assoc);
            } else if (MetadataSupport.xdsB_eb_assoc_type_has_member.equals(submittedMetadata.getAssocType(assoc))) {
                muCommand = this.handleHasMemberAssociation(submittedMetadata, metadataUpdateContext, assoc);
            } else if (MetadataSupport.xdsB_ihe_assoc_type_submit_association.equals(submittedMetadata.getAssocType(assoc))) {
                muCommand = this.handleSubmitAssociation(submittedMetadata, metadataUpdateContext, assoc);
            } else {
                // Do nothing.
            }
            if (muCommand != null) {
                muCommands.add(muCommand);
            }
        }
        if (muCommands.isEmpty()) {
            // FIXME: Use proper exception.
            throw new XdsException("No trigger event detected - No updates made to registry");
        }
        return muCommands;
    }

    /**
     *
     * @param muCommands
     * @return
     * @throws XdsException
     */
    private boolean runMetadataUpdateCommands(List<MetadataUpdateCommand> muCommands) throws XdsException {
        // TBD: Do we need to order commands?
        // Execute each command.
        boolean runStatus = false;
        for (MetadataUpdateCommand muCommand : muCommands) {
            runStatus = muCommand.run();
            if (!runStatus) {
                break;  // Get out - do not run any more commands on first failure.
            }
        }
        return runStatus;
    }

    /**
     *
     * @param submittedMetadata
     * @param metadataUpdateContext
     * @param assoc
     * @return
     * @throws MetadataException
     */
    private MetadataUpdateCommand handleHasMemberAssociation(Metadata submittedMetadata, MetadataUpdateContext metadataUpdateContext, OMElement assoc) throws MetadataException {
        MetadataUpdateCommand muCommand = null;
        String submissionSetId = submittedMetadata.getSubmissionSetId();
        String sourceObjectId = submittedMetadata.getSourceObject(assoc);
        String targetObjectId = submittedMetadata.getTargetObject(assoc);

        // See what type of target object we are dealing with.
        String targetObjectType = submittedMetadata.getObjectTypeById(targetObjectId);
        OMElement submittedRegistryObject = submittedMetadata.getObjectById(targetObjectId);
        if (sourceObjectId.equals(submissionSetId)) {
            // See if we have a PreviousVersion slot name.
            String perviousVersion = submittedMetadata.getSlotValue(assoc, "PreviousVersion", 0);
            if (perviousVersion != null) {
                boolean associationPropagation = true;
                // See if "AssociationPropagation" slot is in request.
                String associationPropagationValueText = submittedMetadata.getSlotValue(assoc, "AssociationPropagation", 0);
                // If missing, default is "yes".
                if (associationPropagationValueText != null) {
                    if (associationPropagationValueText.equalsIgnoreCase("no")) {
                        associationPropagation = false;
                    }
                }
                if (targetObjectType.equals("Folder")) {
                    // Updating a folder.
                    UpdateFolderMetadataCommand updateFolderCommand =
                            new UpdateFolderMetadataCommand(submittedMetadata, metadataUpdateContext);
                    updateFolderCommand.setPreviousVersion(perviousVersion);
                    updateFolderCommand.setSubmittedRegistryObject(submittedRegistryObject);
                    updateFolderCommand.setAssociationPropagation(associationPropagation);
                    muCommand = updateFolderCommand;
                } else if (targetObjectType.equals("ExtrinsicObject")) {
                    // Updating a document.
                    UpdateDocumentEntryMetadataCommand updateDocumentEntryCommand =
                            new UpdateDocumentEntryMetadataCommand(submittedMetadata, metadataUpdateContext);
                    updateDocumentEntryCommand.setPreviousVersion(perviousVersion);
                    updateDocumentEntryCommand.setSubmittedRegistryObject(submittedRegistryObject);
                    updateDocumentEntryCommand.setAssociationPropagation(associationPropagation);
                    muCommand = updateDocumentEntryCommand;
                }
            }
        }
        return muCommand;
    }

    /**
     *
     * @param submittedMetadata
     * @param metadataUpdateContext
     * @param assoc
     * @return
     * @throws MetadataException
     */
    private MetadataUpdateCommand handleUpdateAvailabilityStatusAssociation(Metadata submittedMetadata, MetadataUpdateContext metadataUpdateContext, OMElement assoc) throws MetadataException {
        MetadataUpdateCommand muCommand = null;
        String targetObjectId = submittedMetadata.getTargetObject(assoc);
        // Get "NewStatus" and "OriginalStatus".
        String newStatus = submittedMetadata.getSlotValue(assoc, "NewStatus", 0);
        String originalStatus = submittedMetadata.getSlotValue(assoc, "OriginalStatus", 0);
        UpdateStatusCommand updateStatusCommand = new UpdateStatusCommand(submittedMetadata, metadataUpdateContext);
        updateStatusCommand.setNewStatus(newStatus);
        updateStatusCommand.setOriginalStatus(originalStatus);
        updateStatusCommand.setTargetObjectId(targetObjectId);
        muCommand = updateStatusCommand;
        return muCommand;
    }

    /**
     *
     * @param submittedMetadata
     * @param metadataUpdateContext
     * @param assoc
     * @return
     * @throws MetadataException
     */
    private MetadataUpdateCommand handleSubmitAssociation(Metadata submittedMetadata, MetadataUpdateContext metadataUpdateContext, OMElement assoc) throws MetadataException {
        MetadataUpdateCommand muCommand = null;
        String targetObjectId = submittedMetadata.getTargetObject(assoc);
        OMElement submittedRegistryObject = submittedMetadata.getObjectById(targetObjectId);
        SubmitAssociationCommand submitAssociationCommand = new SubmitAssociationCommand(submittedMetadata, metadataUpdateContext);
        submitAssociationCommand.setSubmittedRegistryObject(submittedRegistryObject);
        submitAssociationCommand.setSubmitAssociation(assoc);
        muCommand = submitAssociationCommand;
        return muCommand;
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
