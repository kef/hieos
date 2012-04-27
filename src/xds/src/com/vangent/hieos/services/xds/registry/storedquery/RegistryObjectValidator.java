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
package com.vangent.hieos.services.xds.registry.storedquery;

import com.vangent.hieos.adt.verify.Verify;
import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XDSNonIdenticalHashException;
import com.vangent.hieos.xutil.exception.XdsDeprecatedException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.exception.XdsPatientIdDoesNotMatchException;
import com.vangent.hieos.xutil.exception.XdsUnknownPatientIdException;
import com.vangent.hieos.xutil.metadata.validation.Validator;
import com.vangent.hieos.xutil.response.RegistryErrorList;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import org.apache.axiom.om.OMElement;

/**
 * 
 * @author Bernie Thuman (many rewrites from NIST code base).
 */
public class RegistryObjectValidator extends StoredQuery {

    /**
     *
     * @param response
     * @param logMessage
     * @param backendRegistry
     */
    public RegistryObjectValidator(ErrorLogger response, XLogMessage logMessage, BackendRegistry backendRegistry) {
        super(response, logMessage, backendRegistry);
    }

    /**
     * 
     * @param metadata
     * @param isSubmit
     * @param registryErrorList
     * @param registryConfig
     * @throws XdsPatientIdDoesNotMatchException
     * @throws MetadataValidationException
     * @throws XDSNonIdenticalHashException
     * @throws MetadataException
     * @throws XdsDeprecatedException
     * @throws XdsInternalException
     * @throws XdsException
     */
    public void validate(Metadata metadata, boolean isSubmit, RegistryErrorList registryErrorList, XConfigActor registryConfig) throws XdsPatientIdDoesNotMatchException, MetadataValidationException, XDSNonIdenticalHashException, MetadataException, XdsDeprecatedException, XdsInternalException, XdsException {
        // Validate that the metadata is internally consistent:
        Validator val = new Validator(metadata, registryErrorList, isSubmit, this.getLogMessage());
        val.run();

        // Validate uuid's, etc. -- side effect, will update Metadata instance.
        this.validateUniqueIds(metadata);

        // Validate patient id.
        this.validatePatientId(metadata, registryConfig);

        // Check for references to registry contents
        this.validateApprovedStatus(metadata);
    }

    /**
     * 
     * @param metadata
     * @param isSubmit
     * @param registryErrorList
     * @throws XdsPatientIdDoesNotMatchException
     * @throws MetadataValidationException
     * @throws XDSNonIdenticalHashException
     * @throws MetadataException
     * @throws XdsDeprecatedException
     * @throws XdsInternalException
     * @throws XdsException
     */
    public void validateMetadataStructure(Metadata metadata, boolean isSubmit, RegistryErrorList registryErrorList) throws XdsPatientIdDoesNotMatchException, MetadataValidationException, XDSNonIdenticalHashException, MetadataException, XdsDeprecatedException, XdsInternalException, XdsException {
        // Validate that the metadata is internally consistent:
        Validator val = new Validator(metadata, registryErrorList, isSubmit, this.getLogMessage());
        val.run();
    }

    /**
     *
     * @param metadata
     * @throws MetadataValidationException
     * @throws MetadataException
     * @throws XDSNonIdenticalHashException
     * @throws XdsInternalException
     * @throws XdsException
     */
    private void validateUniqueIds(Metadata metadata) throws MetadataValidationException, MetadataException, XDSNonIdenticalHashException, XdsInternalException, XdsException {
        this.validateSubmissionSetUniqueIds(metadata);
        this.validateDocumentUniqueIds(metadata);
        this.validateFolderUniqueIds(metadata);
    }

    /**
     *
     * @param metadata
     * @throws MetadataValidationException
     * @throws MetadataException
     * @throws XDSNonIdenticalHashException
     * @throws XdsInternalException
     * @throws XdsException
     */
    public void validateSubmissionSetUniqueIds(Metadata metadata) throws MetadataValidationException, MetadataException, XDSNonIdenticalHashException, XdsInternalException, XdsException {
        // Pull unique ids from all major metadata types:
        List<String> submittedSubmissionSetUIDs = metadata.getSubmissionSetUniqueIds();
        if (submittedSubmissionSetUIDs.isEmpty()) {
            return;  // EARLY EXIT!
        }

        // For all existing submission set UIDs, do a full leaf query.
        this.setReturnLeafClass(true);
        this.getBackendRegistry().setReason("Validate Submission Set Unique IDs");
        OMElement queryResults = this.getSubmissionSetByUID(submittedSubmissionSetUIDs);   // LeafClass for offending objects
        this.getBackendRegistry().setReason("");
        if (queryResults == null) {
            throw new XdsInternalException("Could not get submission sets from registry where UIDs = " + submittedSubmissionSetUIDs);
        }
        Metadata currentMetadata = MetadataParser.parseNonSubmission(queryResults);

        // Tell the logger:
        XLogMessage logMessage = this.getLogMessage();
        logMessage.addOtherParam("submitted submission set uids", submittedSubmissionSetUIDs);

        // Throw exception for submission sets that already exist in registry.
        for (String suuid : currentMetadata.getSubmissionSetIds()) {
            String sid = metadata.getExternalIdentifierValue(suuid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
            logMessage.addOtherParam("duplicate ssuid", sid);
            throw new MetadataValidationException("SubmissionSet uniqueId "
                    + sid
                    + " ( id = " + suuid + " ) "
                    + " already present in the registry");
        }
    }

    /**
     *
     * @param metadata
     * @throws MetadataValidationException
     * @throws MetadataException
     * @throws XDSNonIdenticalHashException
     * @throws XdsInternalException
     * @throws XdsException
     */
    public void validateDocumentUniqueIds(Metadata metadata) throws MetadataValidationException, MetadataException, XDSNonIdenticalHashException, XdsInternalException, XdsException {

        // Pull unique ids from all major metadata types:
        List<String> submittedDocumentUIDs = metadata.getExtrinsicObjectUniqueIds();
        if (submittedDocumentUIDs.isEmpty()) {
            return;  // EARLY EXIT!
        }

        // For all existing document UIDs, do a full leaf query and add to metadata result:
        this.setReturnLeafClass(true);
        this.getBackendRegistry().setReason("Validate Document Unique IDs");
        OMElement queryResults = this.getDocumentByUID(submittedDocumentUIDs);
        this.getBackendRegistry().setReason("");
        if (queryResults == null) {
            throw new XdsInternalException("Could not get documents from registry where UIDs = "
                    + submittedDocumentUIDs);
        }
        Metadata currentMetadata = MetadataParser.parseNonSubmission(queryResults);

        // Now, deal with potential duplicates:
        ArrayList<String> currentDocumentUIDs = new ArrayList<String>();
        Map<String, OMElement> currentDocumentsUIDMap = currentMetadata.getUidMap();
        currentDocumentUIDs.addAll(currentDocumentsUIDMap.keySet());

        // Tell the logger:
        XLogMessage logMessage = this.getLogMessage();
        logMessage.addOtherParam("submitted document uids", submittedDocumentUIDs);
        logMessage.addOtherParam("current document uids", currentDocumentUIDs);

        // Throw exception for documents that already exist in registry (with a different hash).
        Map<String, OMElement> docsSubmittedUIDMap = metadata.getUidMap(metadata.getExtrinsicObjects());
        for (String submittedDocumentUID : docsSubmittedUIDMap.keySet()) {
            if (currentDocumentUIDs.contains(submittedDocumentUID)) {  // Found an entry that exists in registry.
                OMElement currentDocument = currentDocumentsUIDMap.get(submittedDocumentUID);
                OMElement submittedDocument = docsSubmittedUIDMap.get(submittedDocumentUID);

                // Make sure hash values match.
                String submittedHash = metadata.getSlotValue(submittedDocument, "hash", 0); // Hash for submitted object.
                String currentHash = currentMetadata.getSlotValue(currentDocument, "hash", 0); // Hash for object in registry.
                if (submittedHash != null && currentHash != null && !submittedHash.equals(currentHash)) {
                    throw new XDSNonIdenticalHashException(
                            "UniqueId " + submittedDocumentUID + " exists in both the submission and Registry and the hash value is not the same: "
                            + "Submission Hash Value = " + submittedHash + " and "
                            + "Registry Hash Value = " + currentHash);
                }
            }
        }
    }

    /**
     * 
     * @param metadata
     * @throws MetadataValidationException
     * @throws MetadataException
     * @throws XDSNonIdenticalHashException
     * @throws XdsInternalException
     * @throws XdsException
     */
    public void validateFolderUniqueIds(Metadata metadata) throws MetadataValidationException, MetadataException, XDSNonIdenticalHashException, XdsInternalException, XdsException {
        // Pull unique ids from all major metadata types:
        List<String> submittedFolderUIDs = metadata.getFolderUniqueIds();
        if (submittedFolderUIDs.isEmpty()) {
            return;  // Early exit!
        }

        // For all existing folder UIDs, do a full leaf query.
        this.setReturnLeafClass(true);
        this.getBackendRegistry().setReason("Validate Folder Unique IDs");
        OMElement queryResults = this.getFolderByUID(submittedFolderUIDs);
        this.getBackendRegistry().setReason("");
        if (queryResults == null) {
            throw new XdsInternalException("Could not get folders from registry where UIDs = " + submittedFolderUIDs);
        }
        Metadata currentMetadata = MetadataParser.parseNonSubmission(queryResults);

        // Tell the logger:
        XLogMessage logMessage = this.getLogMessage();
        logMessage.addOtherParam("submitted folder uids", submittedFolderUIDs);

        // Throw exception for folders that already exist in registry.
        for (String fuuid : currentMetadata.getFolderIds()) {
            String fuid = metadata.getExternalIdentifierValue(fuuid, MetadataSupport.XDSFolder_uniqueid_uuid);
            logMessage.addOtherParam("duplicate fuid", fuid);
            throw new MetadataValidationException("Folder uniqueId "
                    + fuid
                    + " ( id = " + fuuid + " ) "
                    + " already present in the registry");
        }
    }

    /**
     * 
     * @param metadata
     * @param registryConfig
     * @throws XdsUnknownPatientIdException
     * @throws MetadataException
     * @throws XdsInternalException
     */
    public void validatePatientId(Metadata metadata, XConfigActor registryConfig) throws XdsUnknownPatientIdException, MetadataException, XdsInternalException {
        // Get the patient id associated with the request and validate that it is known
        // to the registry.
        //this.getBackendRegistry().setReason("Validate Patient IDs");
        String patientId = metadata.getSubmissionSetPatientId();
        this.getLogMessage().addOtherParam("Patient ID", patientId);
        this.validatePatientId(patientId, registryConfig);
        //this.getBackendRegistry().setReason("");
    }

    /**
     * 
     * @param metadata
     * @throws XdsPatientIdDoesNotMatchException
     * @throws MetadataException
     * @throws MetadataValidationException
     * @throws XdsDeprecatedException
     * @throws XdsException
     */
    private void validateApprovedStatus(Metadata metadata) throws XdsPatientIdDoesNotMatchException, MetadataException, MetadataValidationException, XdsDeprecatedException, XdsException {
        String patientId = metadata.getSubmissionSetPatientId();
        // Check for references to registry contents
        List<String> referencedObjects = metadata.getReferencedObjects();
        if (referencedObjects.size() > 0) {
            // Make sure that referenced objects are "APPROVED":
            List<String> missing = this.getMissingApprovedRegistryObjects(referencedObjects);
            if (missing != null) {
                throw new XdsDeprecatedException("The following registry objects were referenced by this submission but are not present, as Approved documents, in the registry: " + missing);
            }
            missing = this.validateSamePatientId(metadata.getReferencedObjectsThatMustHaveSamePatientId(), patientId);
            if (missing != null) {
                throw new XdsPatientIdDoesNotMatchException("The following registry objects were referenced by this submission but do not reference the same patient ID: " + missing);
            }
        }
        this.getBackendRegistry().setReason("");
    }

    /**
     * 
     * @param patientId
     * @param registryConfig
     * @throws XdsUnknownPatientIdException
     * @throws XdsInternalException
     */
    private void validatePatientId(String patientId, XConfigActor registryConfig) throws XdsUnknownPatientIdException, XdsInternalException {
        String validatePatientIdAsString = registryConfig.getProperty("validatePatientId");
        boolean validatePatientId = true;
        if (validatePatientIdAsString != null) {
            validatePatientId = registryConfig.getPropertyAsBoolean("validatePatientId");
        }
        if (validatePatientId) {
            Verify v = new Verify();
            boolean isValidPatientId = v.isValid(patientId);
            if (!isValidPatientId) {
                throw new XdsUnknownPatientIdException("PatientId " + patientId + " is not known to the Registry");
            }
        }
        this.getBackendRegistry().setReason("");
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    private List<String> getMissingApprovedRegistryObjects(List<String> uuids) throws XdsException {
        // Get "Approved" documents.
        this.getBackendRegistry().setReason("Get Approved Documents");
        StoredQueryBuilder sqb = StoredQuery.getSQL_DocumentByUUID(uuids, false /* LeafClass */, MetadataSupport.status_type_approved);
        List<String> queryResults = this.runQueryForObjectRefs(sqb);

        // Get "Approved" registry packages.
        this.getBackendRegistry().setReason("Get Approved Registry Packages");
        sqb = StoredQuery.getSQL_RegistryPackageByUUID(uuids, false /* LeafClass */, MetadataSupport.status_type_approved);
        List<String> registryPackageQueryResults = this.runQueryForObjectRefs(sqb);

        // Consolidate results.
        queryResults.addAll(registryPackageQueryResults);

        return this.findMissingIds(uuids, queryResults);
    }

    /**
     *
     * @param uuids
     * @param patient_id
     * @return
     * @throws XdsException
     */
    private List<String> validateSamePatientId(List<String> uuids, String patient_id)
            throws XdsException {
        if (uuids.isEmpty()) {
            return null;
        }
        // Get documents that match supplied UUIDs and patient id.
        this.getBackendRegistry().setReason("Validate Same PID - Get Documents By PID");
        StoredQueryBuilder sqb = StoredQuery.getSQL_DocumentsByUUID_PID(uuids, patient_id, false /* LeafClass */);
        List<String> queryResults = this.runQueryForObjectRefs(sqb);

        // Get registry packages that match supplied UUIDs and patient id.
        this.getBackendRegistry().setReason("Validate Same PID - Get Registry Packages By PID");
        sqb = StoredQuery.getSQL_RegistryPackagesByUUID_PID(uuids, patient_id, false /* LeafClass */);
        List<String> registryPackageQueryResults = this.runQueryForObjectRefs(sqb);

        // Consolidate results.
        queryResults.addAll(registryPackageQueryResults);

        return this.findMissingIds(uuids, queryResults);
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    @Override
    public Metadata runInternal() throws XdsException {
        // TODO Auto-generated method stub
        return null;
    }
}
