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

import com.vangent.hieos.xutil.services.framework.ContentValidationService;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.adt.verify.Verify;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.exception.XdsDeprecatedException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsFormatException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.exception.XdsPatientIdDoesNotMatchException;
import com.vangent.hieos.xutil.exception.XdsUnknownPatientIdException;
import com.vangent.hieos.xutil.registry.BackendRegistry;
import com.vangent.hieos.xutil.metadata.structure.IdParser;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.query.RegistryObjectValidator;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.registry.RegistryUtility;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.metadata.structure.Structure;
import com.vangent.hieos.xutil.metadata.validation.Validator;
import com.vangent.hieos.services.xds.registry.storedquery.SQFactory;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.vangent.hieos.xutil.xconfig.XConfig;

import java.util.List;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

/**
 *
 * @author NIST, Bernie Thuman (overall cleanup).
 */
public class SubmitObjectsRequest extends XBaseTransaction {
    //Message context was added when trying to send audit message

    MessageContext messageContext;
    boolean submit_raw = false;
    ContentValidationService validater;
    private final static Logger logger = Logger.getLogger(SubmitObjectsRequest.class);

    /**
     *
     * @param logMessage
     * @param messageContext
     */
    public SubmitObjectsRequest(XLogMessage logMessage, MessageContext messageContext) {
        this.log_message = logMessage;
        this.messageContext = messageContext;
        try {
            init(new RegistryResponse(), messageContext);
        } catch (XdsInternalException e) {
            logger.fatal(logger_exception_details(e));
        }
    }

    /**
     *
     * @param sor
     * @param validater
     * @return
     */
    public OMElement submitObjectsRequest(OMElement sor, ContentValidationService validater) {
        this.validater = validater;

        try {
            sor.build();
            mustBeSimpleSoap();
            SubmitObjectsRequestInternal(sor);
            //AUDIT:POINT
            //call to audit message for document repository
            //for Transaction id = ITI-42. (Register Document set-b)
            //Here document consumer is treated as document repository
            performAudit(
                    XATNALogger.TXN_ITI42,
                    sor,
                    null,
                    XATNALogger.ActorType.REGISTRY,
                    XATNALogger.OutcomeIndicator.SUCCESS);
        } catch (XdsFormatException e) {
            response.add_error(MetadataSupport.XDSRegistryError, "SOAP Format Error: " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (XdsDeprecatedException e) {
            response.add_error("XDSRegistryDeprecatedDocumentError", "XDS Deprecated Document Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XdsUnknownPatientIdException e) {
            response.add_error(MetadataSupport.XDSUnknownPatientId, "XDS Unknown Patient Id:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XdsPatientIdDoesNotMatchException e) {
            response.add_error(MetadataSupport.XDSPatientIdDoesNotMatch, "Patient ID does not match:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XdsInternalException e) {
            response.add_error(MetadataSupport.XDSRegistryError, "XDS Internal Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (MetadataValidationException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError, "Metadata Validation Errors:\n " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (SchemaValidationException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError, "Schema Validation Errors:\n" + e.getMessage(), this.getClass().getName(), log_message);
        } catch (XdsException e) {
            response.add_error(MetadataSupport.XDSRegistryError, "Exception:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (TransformerConfigurationException e) {
            response.add_error(MetadataSupport.XDSRegistryError, "Internal Error: Transformer Configuration Error: " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (SQLException e) {
            response.add_error(MetadataSupport.XDSRegistryError, "Internal Logging error: SQLException: " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (Exception e) {
            response.add_error(MetadataSupport.XDSRegistryError, "XDS General Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        }
        //this.log_response();
        OMElement res = null;
        try {
            res = response.getResponse();
        } catch (XdsInternalException e) {
        }
        return res;
    }

    /**
     *
     * @param sor
     * @throws SQLException
     * @throws SchemaValidationException
     * @throws MetadataValidationException
     * @throws XdsInternalException
     * @throws TransformerConfigurationException
     * @throws MetadataValidationException
     * @throws XdsException
     */
    void SubmitObjectsRequestInternal(OMElement sor)
            throws SQLException, SchemaValidationException, MetadataValidationException, XdsInternalException, TransformerConfigurationException,
            MetadataValidationException, XdsException {
        boolean status;

        // First, make sure that the "SubmitObjectRequest" is valid against the XDS.b schema:
        RegistryUtility.schema_validate_local(sor, MetadataTypes.METADATA_TYPE_Rb);

        try {
            Metadata m = new Metadata(sor);  // Create meta-data instance for SOR.
            this.logMetadata(m);

            // Validate that the SOR is internally consistent:
            Validator val = new Validator(m, response.registryErrorList, true, log_message);
            val.run();

            RegistryObjectValidator rov = new RegistryObjectValidator(response, log_message);
            rov.validateProperUids(m);
            if (response.has_errors()) {
                logger.error("metadata validator failed");
            }

            // Get out early if the validation process failed:
            if (response.has_errors()) {
                return;
            }

            if (this.validater != null && !this.validater.runContentValidationService(m, response)) {
                return;
            }

            // VALIDATION STEP:
            // Get the patient id associated with the request and validate that it is known 
            // to the registry.
            String patientId = m.getSubmissionSetPatientId();
            log_message.addOtherParam("Patient ID", patientId);
            this.validatePatientId(patientId);

            // Check for references to registry contents
            ArrayList referencedObjects = m.getReferencedObjects();
            if (referencedObjects.size() > 0) {
                // Make sure that referenced objects are "approved":
                ArrayList missing = rov.validateApproved(referencedObjects);
                if (missing != null) {
                    throw new XdsDeprecatedException("The following registry objects were referenced by this submission but are not present, as Approved documents, in the registry: " +
                            missing);
                }

                // Make allowance for by reference inclusion
                missing = rov.validateSamePatientId(m.getReferencedObjectsThatMustHaveSamePatientId(), patientId);
                if (missing != null) {
                    throw new XdsPatientIdDoesNotMatchException("The following registry objects were referenced by this submission but do not reference the same patient ID: " +
                            missing);
                }
            }

            // Allocate uuids for symbolic ids
            IdParser idParser = new IdParser(m);
            idParser.compileSymbolicNamesIntoUuids();

            // Check that submission does not include any object ids that are already in registry
            List<String> idsInSubmission = m.getAllDefinedIds();
            //RegistryObjectValidator roval = new RegistryObjectValidator(response, log_message);
            List<String> idsAlreadyInRegistry = rov.validateNotExists(idsInSubmission);
            if (idsAlreadyInRegistry.size() != 0) {
                response.add_error(MetadataSupport.XDSRegistryMetadataError,
                        "The following UUIDs which are present in the submission are already present in registry: " + idsAlreadyInRegistry,
                        this.getClass().getName(),
                        log_message);
            }

            // Update any folders "lastUpdateTime" slot with the current time:
            m.updateFoldersLastUpdateTimeSlot();

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
                Metadata me = new SQFactory(this, false).findFoldersForDocumentByUuid(originalDocumentId);
                ArrayList<String> folderIds = me.getObjectIds(me.getObjectRefs());
                // for each folder, add an association placing replacment in that folder
                // This brings up interesting question, should the Assoc between SS and Assoc be generated also?  YES!
                for (String fid : folderIds) {

                    OMElement assoc = m.add_association(m.mkAssociation(MetadataSupport.xdsB_eb_assoc_type_has_member, fid, replacementDocumentId));
                    OMElement assoc2 = m.add_association(m.mkAssociation(MetadataSupport.xdsB_eb_assoc_type_has_member, m.getSubmissionSetId(), assoc.getAttributeValue(MetadataSupport.id_qname)));
                }
            }


            BackendRegistry backendRegistry = new BackendRegistry(response, log_message);
            // if this submission adds a document to a folder then update that folder's lastUpdateTime Slot
            for (OMElement assoc : m.getAssociations()) {
                if (MetadataSupport.xdsB_eb_assoc_type_has_member.equals(m.getAssocType(assoc))) {
                    String sourceId = m.getAssocSource(assoc);
                    if (!m.getSubmissionSetId().equals(sourceId) &&
                            !m.getFolderIds().contains(sourceId)) {
                        // Assoc src not part of the submission
                        logger.info("Adding to Folder (1)" + sourceId);
                        if (new Structure(new Metadata(), false).isFolder(sourceId)) {
                            logger.info("Adding to Folder (2)" + sourceId);

                            OMElement res = backendRegistry.basicQuery("SELECT * from RegistryPackage rp WHERE rp.id='" + sourceId + "'",
                                    true /* leafClass */);

                            // Update any folders "lastUpdateTime" slot:
                            Metadata fm = MetadataParser.parseNonSubmission(res);
                            fm.updateFoldersLastUpdateTimeSlot();

                            OMElement to_backend = fm.getV3SubmitObjectsRequest();
                            log_message.addOtherParam("From Registry Adaptor", to_backend);
                            status = submitToBackendRegistry(backendRegistry, to_backend);
                            if (!status) {
                                return;
                            }
                        }
                    }
                }
            }

            // Finally, make the actual submission:
            OMElement request = m.getV3SubmitObjectsRequest();
            log_message.addOtherParam("From Registry Adaptor", request);
            status = submitToBackendRegistry(backendRegistry, request);
            if (!status) {
                return;
            }

            // Approve
            ArrayList approvableObjectIds = m.getApprovableObjectIds();
            if (approvableObjectIds.size() > 0) {
                this.submitApproveObjectsRequest(backendRegistry, approvableObjectIds);
            }

            // Deprecate
            ArrayList deprecatableObjectIds = m.getDeprecatableObjectIds();
            // add to the list of things to deprecate, any XFRM or APND documents hanging off documents
            // in the deprecatable_object_ids list
            List<String> XFRMandAPNDDocuments = rov.getXFRMandAPNDDocuments(deprecatableObjectIds);
            deprecatableObjectIds.addAll(XFRMandAPNDDocuments);
            if (deprecatableObjectIds.size() > 0) {
                // validate that these are documents first
                List<String> missing = rov.validateDocuments(deprecatableObjectIds);
                if (missing != null) {
                    throw new XdsException("The following documents were referenced by this submission but are not present in the registry: " +
                            missing);
                }
                this.submitDeprecateObjectsRequest(backendRegistry, deprecatableObjectIds);
            }
            this.log_response();
        } catch (MetadataException e) {
            response.add_error(MetadataSupport.XDSRegistryError, e.getMessage(), this.getClass().getName(), log_message);
            return;
        }
    }

    /**
     * 
     * @param backendRegistry
     * @param objectIds
     * @throws XdsException
     */
    private void submitApproveObjectsRequest(BackendRegistry backendRegistry, ArrayList objectIds) throws XdsException {
        OMElement approveObjectsRequest = backendRegistry.getApproveObjectsRequest(objectIds);
        log_message.addOtherParam("Approve", approveObjectsRequest);
        submitToBackendRegistry(backendRegistry, approveObjectsRequest);
    }

    /**
     *
     * @param backendRegistry
     * @param objectIds
     * @throws XdsException
     */
    private void submitDeprecateObjectsRequest(BackendRegistry backendRegistry, ArrayList objectIds) throws XdsException {
        OMElement deprecateObjectsRequest = backendRegistry.getDeprecateObjectsRequest(objectIds);
        log_message.addOtherParam("Deprecate", deprecateObjectsRequest);
        submitToBackendRegistry(backendRegistry, deprecateObjectsRequest);
    }

    /**
     *
     * @param patientId
     * @throws java.sql.SQLException
     * @throws com.vangent.hieos.xutil.exception.XdsException
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void validatePatientId(String patientId) throws SQLException,
            XdsException, XdsInternalException {
        //if (Properties.loader().getBoolean("validate_patient_id")) {
        if (XConfig.getInstance().getHomeCommunityPropertyAsBoolean("validatePatientId")) {
            Verify v = new Verify();
            boolean isValidPatientId = v.isValid(patientId);
            if (!isValidPatientId) {
                throw new XdsUnknownPatientIdException("PatientId " + patientId + " is not known to the Registry");
            }
        }
    }

    //AMS 04/29/2009 - FIXME invoked by XdsRaw. REMOVE at some point.
    /**
     *
     * @param val
     */
    public void setSubmitRaw(boolean val) {
        submit_raw = val;
    }

    /* AMS 04/21/2009 - Added new method. */
    /**
     * 
     * @param br
     * @param omElement
     * @return
     * @throws XdsException
     */
    private boolean submitToBackendRegistry(BackendRegistry br, OMElement omElement) throws XdsException {
        OMElement result = br.submit(omElement);
        return getResult(result);// Method should be renamed to getRegistrySubmissionStatus ...
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
    }
}
