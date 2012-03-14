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
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.exception.XdsDeprecatedException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.exception.XdsPatientIdDoesNotMatchException;
import com.vangent.hieos.xutil.exception.XdsUnknownPatientIdException;
import com.vangent.hieos.xutil.metadata.structure.IdIndex;
import com.vangent.hieos.xutil.metadata.validation.Validator;
import com.vangent.hieos.xutil.response.RegistryErrorList;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import org.apache.axiom.om.OMElement;
import org.freebxml.omar.server.persistence.rdb.RegistryCodedValueMapper;

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
     * @param m
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
    public void validate(Metadata m, RegistryErrorList registryErrorList, XConfigActor registryConfig) throws XdsPatientIdDoesNotMatchException, MetadataValidationException, XDSNonIdenticalHashException, MetadataException, XdsDeprecatedException, XdsInternalException, XdsException {
        // Validate that the SOR is internally consistent:
        Validator val = new Validator(m, registryErrorList, true, this.getLogMessage());
        val.run();

        // Validate uuid's, etc. -- side effect, will update Metadata instance.
        this.validateProperUids(m);

        // Validate patient id.
        this.validatePatientId(m, registryConfig);

        // Check for references to registry contents
        this.validateApprovedStatus(m);
    }

    /**
     * 
     * @param metadata
     * @throws MetadataValidationException 
     * @throws XdsException
     * @throws MetadataException
     * @throws XdsInternalException
     * @throws XDSNonIdenticalHashException
     */
    public void validateProperUids(Metadata metadata) throws MetadataValidationException, MetadataException, XDSNonIdenticalHashException, XdsInternalException, XdsException {
        IdIndex idIndex = new IdIndex(metadata);  // Parse metadata.

        // Pull unique ids from all major metadata types:
        ArrayList<String> documentUids = this.getDocumentUIDs(metadata);
        ArrayList<String> folderUids = this.getFolderUIDs(metadata, idIndex);
        ArrayList<String> submissionSetUids = this.getSubmissionSetUIDs(metadata, idIndex);

        // Get UUIDs (makes registry queries) for all major metadata types:
        List<String> documentUUIDs = this.queryForObjectRefs_DocumentByUID(documentUids);
        List<String> folderUUIDs = this.queryForObjectRefs_FolderByUID(folderUids);
        List<String> submissionSetUUIDs = this.queryForObjectRefs_SubmissionSetByUID(submissionSetUids);

        // Consolidate UUIDs into a single list:
        List<String> uuids = new ArrayList<String>();
        if (documentUUIDs != null && documentUUIDs.size() > 0) {
            uuids.addAll(documentUUIDs);
        }
        if (folderUUIDs != null && folderUUIDs.size() > 0) {
            uuids.addAll(folderUUIDs);
        }
        if (submissionSetUUIDs != null && submissionSetUUIDs.size() > 0) {
            uuids.addAll(submissionSetUUIDs);
        }

        // If no UUIDs found, simply exit.
        if (uuids.isEmpty()) {
            return;  // EARLY EXIT!
        }

        Metadata m = new Metadata();  // Houses registry results.

        // For all existing document UUIDs, do a full leaf query and add to metadata result:
        if (documentUUIDs != null && documentUUIDs.size() > 0) {
            this.setReturnLeafClass(true);
            OMElement objects = this.getDocumentByUUID(documentUUIDs);   // LeafClass for offending objects
            if (objects == null) {
                throw new XdsInternalException("Could not retrieve LeafClass for ObjectRef obtained from registry: UUIDs were " + uuids);
            }
            Metadata mLeafObjects = MetadataParser.parseNonSubmission(objects);
            m.addMetadata(mLeafObjects, true /* dedup */);
        }

        // For all existing folder UUIDs, do a full leaf query and add to metadata result:
        if (folderUUIDs != null && folderUUIDs.size() > 0) {
            this.setReturnLeafClass(true);
            OMElement objects = this.getFolderByUUID(folderUUIDs);   // LeafClass for offending objects
            if (objects == null) {
                throw new XdsInternalException("Could not retrieve LeafClass for ObjectRef obtained from registry: UUIDs were " + uuids);
            }
            Metadata mLeafObjects = MetadataParser.parseNonSubmission(objects);
            m.addMetadata(mLeafObjects, true /* dedup */);
        }

        // For all existing submission set UUIDs, do a full leaf query and add to metadata result:
        if (submissionSetUUIDs != null && submissionSetUUIDs.size() > 0) {
            this.setReturnLeafClass(true);
            OMElement objects = this.getSubmissionSetByUUID(submissionSetUUIDs);   // LeafClass for offending objects
            if (objects == null) {
                throw new XdsInternalException("Could not retrieve LeafClass for ObjectRef obtained from registry: UUIDs were " + uuids);
            }
            Metadata mLeafObjects = MetadataParser.parseNonSubmission(objects);
            m.addMetadata(mLeafObjects, true /* dedup */);
        }

        // Now, deal with potential duplicates:
        ArrayList<String> duplicateUIDs = new ArrayList<String>();
        HashMap<String, OMElement> dup_objects = m.getUidMap();
        duplicateUIDs.addAll(dup_objects.keySet());

        // Tell the logger:
        XLogMessage logMessage = this.getLogMessage();
        logMessage.addOtherParam("dup uuids", uuids);
        logMessage.addOtherParam("dup uids", duplicateUIDs);

        // Look for duplicate submission set ids (ids that already exist in registry):
        for (String suuid : metadata.getSubmissionSetIds()) {
            String sid = metadata.getExternalIdentifierValue(suuid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
            logMessage.addOtherParam("ssuid", sid);
            if (duplicateUIDs.contains(sid)) {
                throw new MetadataValidationException("SubmissionSet uniqueId "
                        + sid
                        + " ( id = " + suuid + " ) "
                        + " already present in the registry");
            }
        }

        // Look for duplicate folder ids (ids that already exist in registry):
        for (String fuuid : metadata.getFolderIds()) {
            String fuid = metadata.getExternalIdentifierValue(fuuid, MetadataSupport.XDSFolder_uniqueid_uuid);
            logMessage.addOtherParam("fuid", fuid);
            if (duplicateUIDs.contains(fuid)) {
                throw new MetadataValidationException("Folder uniqueId "
                        + fuid
                        + " ( id = " + fuuid + " ) "
                        + " already present in the registry");
            }
        }

        // Look for duplicate document ids (ids that already exist in registry):
        HashMap<String, OMElement> docsSubmitUIDMap = metadata.getUidMap(metadata.getExtrinsicObjects());
        for (String docUID : docsSubmitUIDMap.keySet()) {
            if (duplicateUIDs.contains(docUID)) {  // Found an entry that exists in registry.
                OMElement regObj = dup_objects.get(docUID);
                String type = regObj.getLocalName();
                if (!type.equals("ExtrinsicObject")) {
                    throw new MetadataValidationException("Document uniqueId "
                            + docUID
                            + " already present in the registry as a non-document object");
                }
                // This is an extrinisic object - make sure hash values match:
                OMElement subObj = docsSubmitUIDMap.get(docUID);
                String subHash = m.getSlotValue(subObj, "hash", 0); // Hash for submitted object.
                String regHash = m.getSlotValue(regObj, "hash", 0); // Hash for object in registry.
                if (subHash != null && regHash != null && !subHash.equals(regHash)) {
                    throw new XDSNonIdenticalHashException(
                            "UniqueId " + docUID + " exists in both the submission and Registry and the hash value is not the same: "
                            + "Submission Hash Value = " + subHash + " and "
                            + "Registry Hash Value = " + regHash);
                }
            }
        }
    }

    /**
     * 
     * @param m
     * @param registryConfig
     * @throws XdsUnknownPatientIdException
     * @throws MetadataException
     * @throws XdsInternalException
     */
    public void validatePatientId(Metadata m, XConfigActor registryConfig) throws XdsUnknownPatientIdException, MetadataException, XdsInternalException {
        // Get the patient id associated with the request and validate that it is known
        // to the registry.
        String patientId = m.getSubmissionSetPatientId();
        this.getLogMessage().addOtherParam("Patient ID", patientId);
        this.validatePatientId(patientId, registryConfig);
    }

    /**
     * 
     * @param m
     * @throws XdsPatientIdDoesNotMatchException
     * @throws MetadataException
     * @throws MetadataValidationException
     * @throws XdsDeprecatedException
     * @throws XdsException
     */
    public void validateApprovedStatus(Metadata m) throws XdsPatientIdDoesNotMatchException, MetadataException, MetadataValidationException, XdsDeprecatedException, XdsException {
        String patientId = m.getSubmissionSetPatientId();
        // Check for references to registry contents
        List<String> referencedObjects = m.getReferencedObjects();
        if (referencedObjects.size() > 0) {
            // Make sure that referenced objects are "APPROVED":
            List<String> missing = this.validateApproved(referencedObjects);
            if (missing != null) {
                throw new XdsDeprecatedException("The following registry objects were referenced by this submission but are not present, as Approved documents, in the registry: " + missing);
            }
            missing = this.validateSamePatientId(m.getReferencedObjectsThatMustHaveSamePatientId(), patientId);
            if (missing != null) {
                throw new XdsPatientIdDoesNotMatchException("The following registry objects were referenced by this submission but do not reference the same patient ID: " + missing);
            }
        }
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
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    public List<String> validateDocuments(List<String> uuids) throws XdsException {
        StoredQueryBuilder sqb = StoredQuery.getSQL_DocumentByUUID(uuids, false /* LeafClass */);
        List<String> results = this.runQueryForObjectRefs(sqb);
        return this.findMissingIds(uuids, results);
    }

    /**
     *
     * @param ids
     * @return
     * @throws XdsException
     */
    public List<String> validateAreFolders(ArrayList<String> ids) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.isReturnLeafClass());
        sqb.append("SELECT rp.id FROM RegistryPackage rp, ExternalIdentifier ei");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" rp.status = '");
        sqb.append(RegistryCodedValueMapper.convertStatus_ValueToCode(MetadataSupport.status_type_approved));
        sqb.append("' AND");
        sqb.newline();
        sqb.append(" rp.id IN ");
        sqb.append(ids);
        sqb.append(" AND");
        sqb.newline();
        sqb.append(" ei.registryObject = rp.id AND");
        sqb.newline();
        sqb.append(" ei.identificationScheme = '"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSFolder_patientid_uuid)
                + "'");
        sqb.newline();
        BackendRegistry backendRegistry = this.getBackendRegistry();
        backendRegistry.setReason("Verify are Folders");
        List<String> results = this.runQueryForObjectRefs(sqb);
        return this.findMissingIds(ids, results);
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    private List<String> validateApproved(List<String> uuids) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.isReturnLeafClass());
        sqb.append("SELECT id FROM ExtrinsicObject eo");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" eo.status = '");
        sqb.append(RegistryCodedValueMapper.convertStatus_ValueToCode(MetadataSupport.status_type_approved));
        sqb.append("' AND");
        sqb.newline();
        sqb.append("  eo.id IN ");
        sqb.append(uuids);
        sqb.newline();
        List<String> results1 = this.runQueryForObjectRefs(sqb);
        sqb.initQuery();
        sqb.append("SELECT id FROM RegistryPackage eo");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" eo.status = '");
        sqb.append(RegistryCodedValueMapper.convertStatus_ValueToCode(MetadataSupport.status_type_approved));
        sqb.append("' AND");
        sqb.newline();
        sqb.append(" eo.id IN ");
        sqb.append(uuids);
        sqb.newline();
        List<String> results = this.runQueryForObjectRefs(sqb);
        results.addAll(results1);
        return this.findMissingIds(uuids, results);
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
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.isReturnLeafClass());
        sqb.append("SELECT eo.id FROM ExtrinsicObject eo, ExternalIdentifier pid");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" eo.id IN ");
        sqb.append(uuids);
        sqb.append(" AND ");
        sqb.newline();
        sqb.append(" pid.registryobject = eo.id AND");
        sqb.newline();
        sqb.append(" pid.identificationScheme='"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSDocumentEntry_patientid_uuid)
                + "' AND");
        sqb.newline();
        sqb.append(" pid.value = '");
        sqb.append(patient_id);
        sqb.append("'");
        sqb.newline();
        List<String> results1 = this.runQueryForObjectRefs(sqb);
        sqb.initQuery();
        sqb.append("SELECT eo.id FROM RegistryPackage eo, ExternalIdentifier pid");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" eo.id IN ");
        sqb.append(uuids);
        sqb.append(" AND");
        sqb.newline();
        sqb.append(" pid.registryobject = eo.id AND");
        sqb.newline();
        sqb.append(" pid.identificationScheme IN ('"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSSubmissionSet_patientid_uuid)
                + "','"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSFolder_patientid_uuid)
                + "') AND");
        sqb.newline();
        sqb.append(" pid.value = '");
        sqb.append(patient_id);
        sqb.append("'");
        sqb.newline();
        List<String> results = this.runQueryForObjectRefs(sqb);
        results.addAll(results1);
        return this.findMissingIds(uuids, results);
    }

    /**
     *
     * @param ids
     * @param results
     * @return
     */
    private List<String> findMissingIds(List<String> ids, List<String> results) {
        List<String> missing = null;
        for (String id : ids) {
            if (!results.contains(id)) {
                if (missing == null) {
                    missing = new ArrayList<String>();
                }
                missing.add(id);
            }
        }
        return missing;
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    public List<String> getXFRMandAPNDDocuments(List<String> uuids) throws XdsException {
        if (uuids.isEmpty()) {
            return new ArrayList<String>();
        }
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.isReturnLeafClass());
        sqb.append("SELECT eo.id FROM ExtrinsicObject eo, Association a");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" a.associationType IN ('");
        sqb.append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_ihe_assoc_type_xfrm));
        sqb.append("', '");
        sqb.append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_ihe_assoc_type_apnd));
        sqb.append("') AND");
        sqb.newline();
        sqb.append(" a.targetObject IN ");
        sqb.append(uuids);
        sqb.append(" AND");
        sqb.newline();
        sqb.append(" a.sourceObject = eo.id");
        sqb.newline();
        return this.runQueryForObjectRefs(sqb);
    }

    /**
     *
     * @param m
     * @return
     * @throws MetadataException
     */
    private ArrayList<String> getDocumentUIDs(Metadata m) throws MetadataException {
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<String> ids = m.getExtrinsicObjectIds();
        for (String id : ids) {
            OMElement registry_object = m.getObjectById(id);
            ArrayList<OMElement> eis = m.getExternalIdentifiers(id);
            ArrayList<OMElement> eid_eles = m.getExternalIdentifiers(registry_object, MetadataSupport.XDSDocumentEntry_uniqueid_uuid);
            String uid;
            if (eid_eles.size() > 0) {
                uid = eid_eles.get(0).getAttributeValue(MetadataSupport.value_qname);
            } else {
                throw new MetadataException("Document " + id + " has no uniqueId\nfound " + eis.size() + " external identifiers");
            }
            list.add(uid);
        }
        return list;
    }

    /**
     *
     * @param m
     * @return
     * @throws MetadataException
     */
    private ArrayList<String> getSubmissionSetUIDs(Metadata m, IdIndex idIndex) throws MetadataException {
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<String> ids = m.getSubmissionSetIds();
        for (String id : ids) {
            String uid = idIndex.getExternalIdentifierValue(id, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
            if (uid == null || uid.equals("")) {
                throw new MetadataException("Submission Set " + id + " has no uniqueId");
            }
            list.add(uid);
        }
        return list;
    }

    /**
     *
     * @param m
     * @return
     * @throws MetadataException
     */
    private ArrayList<String> getFolderUIDs(Metadata m, IdIndex idIndex) throws MetadataException {
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<String> ids = m.getFolderIds();
        for (String id : ids) {
            String uid = idIndex.getExternalIdentifierValue(id, MetadataSupport.XDSFolder_uniqueid_uuid);
            if (uid == null || uid.equals("")) {
                throw new MetadataException("Folder " + id + " has no uniqueId");
            }
            list.add(uid);
        }
        return list;
    }

    /**
     *
     * @param uids
     * @return
     * @throws MetadataException
     * @throws XMLParserException
     * @throws XdsException
     */
    private List<String> queryForObjectRefs_DocumentByUID(List<String> uids) throws MetadataException, XMLParserException, XdsException {
        if (uids.isEmpty()) {
            return null;
        }
        StoredQueryBuilder sqb = StoredQuery.getSQL_DocumentByUID(uids, false /* LeafClass */);
        return this.runQueryForObjectRefs(sqb);
    }

    /**
     *
     * @param uids
     * @return
     * @throws XdsException
     */
    private List<String> queryForObjectRefs_SubmissionSetByUID(List<String> uids) throws XdsException {
        return this.queryForObjectRefs_RegistryPackageByUID(uids, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
    }

    /**
     *
     * @param uids
     * @return
     * @throws XdsException
     */
    private List<String> queryForObjectRefs_FolderByUID(List<String> uids) throws XdsException {
        return this.queryForObjectRefs_RegistryPackageByUID(uids, MetadataSupport.XDSFolder_uniqueid_uuid);
    }

    /**
     *
     * @param uids
     * @param identificationScheme
     * @return
     * @throws XdsException
     */
    private List<String> queryForObjectRefs_RegistryPackageByUID(List<String> uids, String identificationScheme) throws XdsException {
        if (uids.isEmpty()) {
            return null;
        }
        StoredQueryBuilder sqb = StoredQuery.getSQL_RegistryPackageByUID(uids, identificationScheme, false /* LeafClass */);
        return this.runQueryForObjectRefs(sqb);
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
