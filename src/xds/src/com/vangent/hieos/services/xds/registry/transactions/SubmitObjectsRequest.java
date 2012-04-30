/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xds.registry.transactions;

import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.XDSNonIdenticalHashException;
import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.services.xds.registry.storedquery.RegistryObjectValidator;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsDeprecatedException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.exception.XdsPatientIdDoesNotMatchException;
import com.vangent.hieos.xutil.exception.XdsUnknownPatientIdException;
import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.services.xds.registry.storedquery.GetFoldersForDocument;
import com.vangent.hieos.services.xds.registry.storedquery.SubmitObjectsRequestStoredQuerySupport;
import com.vangent.hieos.xutil.metadata.structure.IdParser;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.registry.RegistryUtility;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.ActorType;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.IHETransaction;
import com.vangent.hieos.xutil.atna.ATNAAuditEventHelper;
import com.vangent.hieos.xutil.atna.ATNAAuditEventRegisterDocumentSet;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

/**
 *
 * @author NIST, Bernie Thuman (overall cleanup).
 */
public class SubmitObjectsRequest extends XBaseTransaction {

    //private MessageContext messageContext;
    private final static Logger logger = Logger.getLogger(SubmitObjectsRequest.class);

    /**
     *
     * @param logMessage
     * @param messageContext
     */
    public SubmitObjectsRequest(XLogMessage logMessage, MessageContext messageContext) {
        this.log_message = logMessage;
        //this.messageContext = messageContext;
        try {
            init(new RegistryResponse(), messageContext);
        } catch (XdsInternalException e) {
            logger.fatal(logger_exception_details(e));
        }
    }

    /**
     * 
     * @param sor
     * @return
     */
    public OMElement run(OMElement sor) {
        try {
            sor.build();
            //AUDIT:POINT
            //call to audit message for document registry
            //for Transaction id = ITI-42. (Register Document set-b)
            // NOTE!!: Moved above "SubjectObjectsRequestInternal()" method call since the "sor" instance
            // is changed during the execution of "SubjectObjectsRequestInternal() method.  Otherwise,
            // we would need to pay the penalty for a deep copy of the "sor" instance.
            this.auditSubmitObjectsRequest(sor);
            //performAudit(
            //        XATNALogger.TXN_ITI42,
            //        sor,
            //        null,
            //        XATNALogger.ActorType.REGISTRY,
            //        XATNALogger.OutcomeIndicator.SUCCESS);
            this.handleSubmitObjectsRequest(sor);
        } catch (XdsDeprecatedException e) {
            response.add_error("XDSRegistryDeprecatedDocumentError", e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XdsUnknownPatientIdException e) {
            response.add_error(MetadataSupport.XDSUnknownPatientId, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XdsPatientIdDoesNotMatchException e) {
            response.add_error(MetadataSupport.XDSPatientIdDoesNotMatch, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XDSNonIdenticalHashException e) {
            response.add_error(MetadataSupport.XDSNonIdenticalHash, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (MetadataException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (MetadataValidationException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XdsInternalException e) {
            response.add_error(MetadataSupport.XDSRegistryError, e.getMessage(), this.getClass().getName(), log_message);
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
     * @param sor
     * @throws XdsPatientIdDoesNotMatchException
     * @throws XdsDeprecatedException
     * @throws XdsUnknownPatientIdException
     * @throws MetadataException
     * @throws MetadataValidationException
     * @throws XDSNonIdenticalHashException
     * @throws XdsInternalException
     * @throws XdsException
     */
    void handleSubmitObjectsRequest(OMElement sor) throws XdsPatientIdDoesNotMatchException, XDSNonIdenticalHashException, XdsDeprecatedException, MetadataValidationException, MetadataException, XdsInternalException, XdsException {
        // Run XML schema validation.
        RegistryUtility.schema_validate_local(sor, MetadataTypes.METADATA_TYPE_Rb);
        boolean commitCompleted = false;

        // Get backend registry instance.
        BackendRegistry backendRegistry = new BackendRegistry(log_message);
        try {
            // Create metadata instance from SOR.
            Metadata m = new Metadata(sor);
            this.logMetadata(m);

            SubmitObjectsRequestStoredQuerySupport sqSupport = new SubmitObjectsRequestStoredQuerySupport(response, log_message, backendRegistry);

            // Run validations.
            RegistryObjectValidator rov = new RegistryObjectValidator(response, log_message, backendRegistry);
            rov.validate(m, true /* isSubmit */, response.registryErrorList, this.getConfigActor());
            if (!response.has_errors()) {
                // Only continue if response does not have any errors (a bit ugly).

                // Change symbolic names to UUIDs.
                IdParser idParser = new IdParser(m);
                idParser.compileSymbolicNamesIntoUuids();

                // If this submission includes a DocumentEntry replace and the original DocumentEntry is in a folder
                // then the replacement document must be put into the folder as well.  This must happen here
                // so the following logic to update folder lastUpdateTime can be triggered.
                this.updateFolderContentsOnDocumentReplace(m, backendRegistry);

                // if this submission adds a document to a folder then update that folder's lastUpdateTime Slot
                this.updateFoldersLastUpdateTimeSlot(m, sqSupport, backendRegistry);

                m.setStatusOnApprovableObjects();

                // Finally, make the actual submission:
                this.submitRegistryRequest(m, backendRegistry, "SubmitObjectsRequest");

                // Approve
                //this.approveObjects(m, backendRegistry);

                // Deprecate
                this.deprecateObjects(m, sqSupport, backendRegistry);
            }

            backendRegistry.commit();
            commitCompleted = true;
        } finally {
            if (!commitCompleted) {
                backendRegistry.rollback();
            }
        }
    }

    /**
     *
     * @param m
     * @param backendRegistry
     * @throws XdsInternalException
     */
    private void submitRegistryRequest(Metadata m, BackendRegistry backendRegistry, String reason) throws XdsInternalException {
        backendRegistry.setReason(reason);
        backendRegistry.submit(m);
        backendRegistry.setReason("");
    }

    /**
     *
     * @param m
     * @param backendRegistry
     * @throws XdsException
     */
    //private void approveObjects(Metadata m, BackendRegistry backendRegistry) throws XdsException {
    //    List<String> approvableObjectIds = m.getApprovableObjectIds();
    //    if (approvableObjectIds.size() > 0) {
    //        this.submitApproveObjectsRequest(backendRegistry, approvableObjectIds);
    //    }
    // }

    /**
     *
     * @param m
     * @param rov
     * @param backendRegistry
     * @throws MetadataValidationException
     * @throws MetadataException
     * @throws XdsException
     */
    private void deprecateObjects(Metadata m, SubmitObjectsRequestStoredQuerySupport sqSupport, BackendRegistry backendRegistry) throws MetadataValidationException, MetadataException, XdsException {
        List<String> deprecatableObjectIds = m.getDeprecatableObjectIds();
        // add to the list of things to deprecate, any XFRM or APND documents hanging off documents
        // in the deprecatable_object_ids list
        List<String> XFRMandAPNDDocuments = sqSupport.getXFRMandAPNDDocuments(deprecatableObjectIds);
        deprecatableObjectIds.addAll(XFRMandAPNDDocuments);
        if (deprecatableObjectIds.size() > 0) {
            // validate that these are documents first
            List<String> missing = sqSupport.getMissingDocuments(deprecatableObjectIds);
            if (missing != null) {
                throw new XdsException("The following documents were referenced by this submission but are not present in the registry: " + missing);
            }
            this.submitDeprecateObjectsRequest(backendRegistry, deprecatableObjectIds);
        }
    }

    /**
     *
     * @param m
     * @param backendRegistry
     * @throws XdsException
     */
    private void updateFolderContentsOnDocumentReplace(Metadata m, BackendRegistry backendRegistry) throws XdsException {
        // If this submission includes a DocumentEntry replace and the original DocumentEntry is in a folder
        // then the replacement document must be put into the folder as well.  This must happen here
        // so the following logic to update folder lastUpdateTime can be triggered.

        HashMap<String, String> rplcToOrigIds = new HashMap<String, String>();
        for (OMElement assoc : m.getAssociations()) {
            if (MetadataSupport.xdsB_ihe_assoc_type_rplc.equals(m.getAssocType(assoc))) {
                rplcToOrigIds.put(m.getAssocSource(assoc), m.getAssocTarget(assoc));
            }
        }
        for (String replacementDocumentId : rplcToOrigIds.keySet()) {
            String originalDocumentId = rplcToOrigIds.get(replacementDocumentId);
            // for each original document, find the collection of folders it belongs to
            Metadata me = this.findFoldersForDocumentByUuid(originalDocumentId, backendRegistry);
            List<String> folderIds = me.getObjectIds(me.getObjectRefs());
            // for each folder, add an association placing replacment in that folder
            // This brings up interesting question, should the Assoc between SS and Assoc be generated also?  YES!
            for (String fid : folderIds) {
                OMElement assoc = m.addAssociation(m.makeAssociation(MetadataSupport.xdsB_eb_assoc_type_has_member, fid, replacementDocumentId));
                OMElement assoc2 = m.addAssociation(m.makeAssociation(MetadataSupport.xdsB_eb_assoc_type_has_member, m.getSubmissionSetId(), assoc.getAttributeValue(MetadataSupport.id_qname)));
            }
        }
    }

    /**
     * 
     * @param uuid
     * @param backendRegistry
     * @return
     * @throws XdsException
     */
    private Metadata findFoldersForDocumentByUuid(String uuid, BackendRegistry backendRegistry)
            throws XdsException {
        SqParams parms = new SqParams();
        parms.addStringParm("$XDSDocumentEntryEntryUUID", uuid);
        //Response response, Message log_message
        GetFoldersForDocument sffd = new GetFoldersForDocument(parms, false /* LeafClass */, this.response, this.log_message, backendRegistry);
        return sffd.runInternal();
    }

    /**
     *
     * @param m
     * @param sqSupport
     * @param backendRegistry
     * @throws XdsException
     */
    private void updateFoldersLastUpdateTimeSlot(Metadata m, SubmitObjectsRequestStoredQuerySupport sqSupport, BackendRegistry backendRegistry) throws XdsException {
        // Update any folders "lastUpdateTime" slot with the current time:
        m.updateFoldersLastUpdateTimeSlot();

        // if this submission adds a document to a folder then update that folder's lastUpdateTime Slot
        for (OMElement assoc : m.getAssociations()) {
            if (MetadataSupport.xdsB_eb_assoc_type_has_member.equals(m.getAssocType(assoc))) {
                String sourceId = m.getAssocSource(assoc);
                if (!m.getSubmissionSetId().equals(sourceId) && !m.getFolderIds().contains(sourceId)) {
                    // Assoc src not part of the submission
                    logger.info("Adding to Folder (1)" + sourceId);
                    if (this.isFolder(sourceId, sqSupport)) {
                        logger.info("Adding to Folder (2)" + sourceId);
                        OMElement res = backendRegistry.basicQuery("SELECT * from RegistryPackage rp WHERE rp.id='" + sourceId + "'", true);
                        // Update any folders "lastUpdateTime" slot:
                        Metadata fm = MetadataParser.parseNonSubmission(res);
                        fm.updateFoldersLastUpdateTimeSlot();
                        //OMElement to_backend = fm.getV3SubmitObjectsRequest();
                        //log_message.addOtherParam("From Registry Adaptor", to_backend);
                        this.submitRegistryRequest(fm, backendRegistry, "Update Folder LastUpdateTime Slot");
                    }
                }
            }
        }
    }

    /**
     *
     * @param backendRegistry
     * @param objectIds
     * @throws XdsInternalException
     */
    private void submitApproveObjectsRequest(BackendRegistry backendRegistry, List<String> objectIds) throws XdsInternalException {
        backendRegistry.submitApproveObjectsRequest(objectIds);
    }

    /**
     *
     * @param backendRegistry
     * @param objectIds
     * @throws XdsInternalException
     */
    private void submitDeprecateObjectsRequest(BackendRegistry backendRegistry, List<String> objectIds) throws XdsInternalException {
        backendRegistry.submitDeprecateObjectsRequest(objectIds);
    }

    /**
     *
     * @param id
     * @param sqSupport
     * @return
     * @throws XdsException
     */
    public boolean isFolder(String id, SubmitObjectsRequestStoredQuerySupport sqSupport) throws XdsException {
        if (!id.startsWith("urn:uuid:")) {
            return false;
        }
        ArrayList<String> ids = new ArrayList<String>();
        ids.add(id);
        List<String> missing = sqSupport.getMissingFolders(ids);
        if (missing != null && missing.contains(id)) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param m
     * @throws MetadataException
     */
    private void logMetadata(Metadata m) throws MetadataException {
        // Log relevant data (if logger is turned on of course).
        if (log_message.isLogEnabled() == true) {
            log_message.addOtherParam("SSuid", m.getSubmissionSetUniqueId());
            ArrayList<String> doc_uids = new ArrayList<String>();
            for (String id : m.getExtrinsicObjectIds()) {
                String uid = m.getUniqueIdValue(id);
                if (uid != null && !uid.equals("")) {
                    doc_uids.add(uid);
                }
            }
            log_message.addOtherParam("DOCuids", doc_uids);
            ArrayList<String> fol_uids = new ArrayList<String>();
            for (String id : m.getFolderIds()) {
                String uid = m.getUniqueIdValue(id);
                if (uid != null && !uid.equals("")) {
                    fol_uids.add(uid);
                }
            }
            log_message.addOtherParam("FOLuids", fol_uids);
            log_message.addOtherParam("Structure", m.structure());
        }
    }

    /**
     *
     * @param result
     * @return
     */
    /*
    private boolean getResult(OMElement result) {
    if (result == null) {
    return false;
    }

    String value = result.getAttributeValue(MetadataSupport.status_qname);
    if (value == null) {
    return false;
    }
    if (value.indexOf(":") == -1) {
    return false;
    }
    String[] parts = value.split(":");
    if ("Success".equals(parts[parts.length - 1])) {
    return true;
    } else {
    return false;
    }
    }*/
    /**
     * 
     * @param rootNode
     */
    private void auditSubmitObjectsRequest(OMElement rootNode) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                // Create and log audit event.
                ATNAAuditEventRegisterDocumentSet auditEvent = ATNAAuditEventHelper.getATNAAuditEventRegisterDocumentSet(rootNode);
                auditEvent.setActorType(ActorType.REGISTRY);
                auditEvent.setTransaction(IHETransaction.ITI42);
                auditEvent.setAuditEventType(ATNAAuditEvent.AuditEventType.IMPORT);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            // FIXME?:
        }
    }
}
