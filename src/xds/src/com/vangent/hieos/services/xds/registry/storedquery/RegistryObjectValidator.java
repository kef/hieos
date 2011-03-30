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

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.IdIndex;
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
     * @param log_message
     */
    public RegistryObjectValidator(ErrorLogger response, XLogMessage log_message) {
        super(response, log_message);
    }

    /**
     *
     * @param uuids
     * @return A list of uuids that do not exist in the registry.  null is returned if all found.
     * @throws XdsException
     */
    /* REMOVED (BHT) -- NOT USED
    public ArrayList validateExists(ArrayList uuids) throws XdsException {
    init();
    append("SELECT id FROM RegistryObject ro");
    newline();
    append("WHERE");
    newline();
    append("  ro.id IN ");
    append(uuids);
    newline();

    ArrayList<String> results = this.query_for_object_refs();

    ArrayList missing = null;
    for (int i = 0; i < uuids.size(); i++) {
    String uuid = (String) uuids.get(i);
    if (!results.contains(uuid)) {
    if (missing == null) {
    missing = new ArrayList();
    }
    missing.add(uuid);
    }
    }
    return missing;

    } */

    /**
     *
     * @param ids
     * @return
     */
    private List<String> uuidsOnly(List<String> ids) {
        List<String> uuids = new ArrayList<String>();
        for (String id : ids) {
            if (id.startsWith("urn:uuid:")) {
                uuids.add(id);
            }
        }
        return uuids;
    }

    // returns UUIDs that do exist in registry
    /**
     *
     * @param ids
     * @return
     * @throws XdsException
     */
    /* NO LONGER USED
    public List<String> validateNotExists(List<String> ids) throws XdsException {
        List<String> uuids = this.uuidsOnly(ids);
        System.out.println("-----> ids = " + ids);
        System.out.println("-----> uuids = " + uuids);
        init();
        append("SELECT id FROM RegistryObject ro");
        newline();
        append("WHERE");
        newline();
        append(" ro.id IN ");
        append(uuids);
        newline();
        return this.queryForObjectRefs();
    } */

    /**
     * 
     * @param metadata
     * @throws XdsException
     */
    public void validateProperUids(Metadata metadata) throws XdsException {
        IdIndex idIndex = new IdIndex(metadata);  // Parse metadata.

        // Pull unique ids from all major metadata types:
        ArrayList<String> documentUids = this.getDocument_uids(metadata);
        ArrayList<String> folderUids = this.getFolder_uids(metadata, idIndex);
        ArrayList<String> submissionSetUids = this.getSubmissionSet_uids(metadata, idIndex);

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
        if (uuids.size() == 0) {
            return;  // EARLY EXIT!
        }

        Metadata m = new Metadata();  // Houses registry results.

        // For all existing document UUIDs, do a full leaf query and add to metadata result:
        if (documentUUIDs != null && documentUUIDs.size() > 0) {
            this.return_leaf_class = true;
            OMElement objects = this.getDocumentByUUID(documentUUIDs);   // LeafClass for offending objects
            if (objects == null) {
                throw new XdsInternalException("RegistryObjectValidator.validateProperUids(): could not retrieve LeafClass for ObjectRef obtained from registry: UUIDs were " + uuids);
            }
            Metadata mLeafObjects = MetadataParser.parseNonSubmission(objects);
            m.addMetadata(mLeafObjects, true /* dedup */);
        }

        // For all existing folder UUIDs, do a full leaf query and add to metadata result:
        if (folderUUIDs != null && folderUUIDs.size() > 0) {
            this.return_leaf_class = true;
            OMElement objects = this.getFolderByUUID(folderUUIDs);   // LeafClass for offending objects
            if (objects == null) {
                throw new XdsInternalException("RegistryObjectValidator.validateProperUids(): could not retrieve LeafClass for ObjectRef obtained from registry: UUIDs were " + uuids);
            }
            Metadata mLeafObjects = MetadataParser.parseNonSubmission(objects);
            m.addMetadata(mLeafObjects, true /* dedup */);
        }

        // For all existing submission set UUIDs, do a full leaf query and add to metadata result:
         if (submissionSetUUIDs != null && submissionSetUUIDs.size() > 0) {
            this.return_leaf_class = true;
            OMElement objects = this.getSubmissionSetByUUID(submissionSetUUIDs);   // LeafClass for offending objects
            if (objects == null) {
                throw new XdsInternalException("RegistryObjectValidator.validateProperUids(): could not retrieve LeafClass for ObjectRef obtained from registry: UUIDs were " + uuids);
            }
            Metadata mLeafObjects = MetadataParser.parseNonSubmission(objects);
            m.addMetadata(mLeafObjects, true /* dedup */);
        }

        // Now, deal with potential duplicates:
        ArrayList<String> dup_uids = new ArrayList<String>();
        HashMap<String, OMElement> dup_objects = m.getUidMap();
        dup_uids.addAll(dup_objects.keySet());

        // Tell the logger:
        log_message.addOtherParam("dup uuids", uuids);
        log_message.addOtherParam("dup uids", dup_uids);

        // Look for duplicate submission set ids (ids that already exist in registry):
        for (String suuid : metadata.getSubmissionSetIds()) {
            String sid = metadata.getExternalIdentifierValue(suuid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
            log_message.addOtherParam("ssuid", sid);
            if (dup_uids.contains(sid)) {
                throw new MetadataValidationException("SubmissionSet uniqueId " +
                        sid +
                        " ( id = " + suuid + " ) " +
                        " already present in the registry");
            }
        }

        // Look for duplicate folder ids (ids that already exist in registry):
        for (String fuuid : metadata.getFolderIds()) {
            String fuid = metadata.getExternalIdentifierValue(fuuid, MetadataSupport.XDSFolder_uniqueid_uuid);
            log_message.addOtherParam("fuid", fuid);
            if (dup_uids.contains(fuid)) {
                throw new MetadataValidationException("Folder uniqueId " +
                        fuid +
                        " ( id = " + fuuid + " ) " +
                        " already present in the registry");
            }
        }

        // Look for duplicate document ids (ids that already exist in registry):
        HashMap<String, OMElement> docs_submit_uid_map = metadata.getUidMap(metadata.getExtrinsicObjects());
        for (String doc_uid : docs_submit_uid_map.keySet()) {
            if (dup_uids.contains(doc_uid)) {  // Found an entry that exists in registry.
                OMElement reg_obj = dup_objects.get(doc_uid);
                String type = reg_obj.getLocalName();
                if (!type.equals("ExtrinsicObject")) {
                    throw new MetadataValidationException("Document uniqueId " +
                            doc_uid +
                            " already present in the registry as a non-document object");
                }
                // This is an extrinisic object - make sure hash values match:
                OMElement sub_obj = docs_submit_uid_map.get(doc_uid);
                String sub_hash = m.getSlotValue(sub_obj, "hash", 0); // Hash for submitted object.
                String reg_hash = m.getSlotValue(reg_obj, "hash", 0); // Hash for object in registry.
                if (sub_hash != null && reg_hash != null && !sub_hash.equals(reg_hash)) {
                    response.add_error(MetadataSupport.XDSNonIdenticalHash,
                            "UniqueId " + doc_uid + " exists in both the submission and Registry and the hash value is not the same: " +
                            "Submission Hash Value = " + sub_hash + " and " +
                            "Registry Hash Value = " + reg_hash,
                            this.getClass().getName(), log_message);
                }
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
        init();
        append("SELECT id FROM ExtrinsicObject eo");
        newline();
        append("WHERE");
        newline();
        append(" eo.id IN ");
        append(uuids);
        newline();
        List<String> results = this.queryForObjectRefs();
        return this.findMissingIds(uuids, results);
    }

    // validate the ids are in registry and belong to folders
    // return any that aren't
    /**
     *
     * @param ids
     * @return
     * @throws XdsException
     */
    public List<String> validateAreFolders(ArrayList<String> ids) throws XdsException {
        init();
        append("SELECT rp.id FROM RegistryPackage rp, ExternalIdentifier ei");
        newline();
        append("WHERE");
        newline();
        append(" rp.status = '");
        append(RegistryCodedValueMapper.convertStatus_ValueToCode(MetadataSupport.status_type_approved));
        append("' AND");
        newline();
        append(" rp.id IN ");
        append(ids);
        append(" AND");
        newline();
        append(" ei.registryObject = rp.id AND");
        newline();
        append(" ei.identificationScheme = '" + 
                RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSFolder_patientid_uuid)
                + "'");
        newline();
        br.setReason("Verify are Folders");
        List<String> results = this.queryForObjectRefs();
        return this.findMissingIds(ids, results);
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    public List<String> validateApproved(List<String> uuids) throws XdsException {
        init();
        append("SELECT id FROM ExtrinsicObject eo");
        newline();
        append("WHERE");
        newline();
        append(" eo.status = '");
        append(RegistryCodedValueMapper.convertStatus_ValueToCode(MetadataSupport.status_type_approved));
        append("' AND");
        newline();
        append("  eo.id IN ");
        append(uuids);
        newline();
        List<String> results1 = this.queryForObjectRefs();
        init();
        append("SELECT id FROM RegistryPackage eo");
        newline();
        append("WHERE");
        newline();
        append(" eo.status = '");
        append(RegistryCodedValueMapper.convertStatus_ValueToCode(MetadataSupport.status_type_approved));
        append("' AND");
        newline();
        append(" eo.id IN ");
        append(uuids);
        newline();
        List<String> results = this.queryForObjectRefs();
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
    public List<String> validateSamePatientId(List<String> uuids, String patient_id)
            throws XdsException {
        if (uuids.size() == 0) {
            return null;
        }
        init();
        append("SELECT eo.id FROM ExtrinsicObject eo, ExternalIdentifier pid");
        newline();
        append("WHERE");
        newline();
        append(" eo.id IN ");
        append(uuids);
        append(" AND ");
        newline();
        append(" pid.registryobject = eo.id AND");
        newline();
        append(" pid.identificationScheme='" + 
                RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSDocumentEntry_patientid_uuid)
                + "' AND");
        newline();
        append(" pid.value = '");
        append(patient_id);
        append("'");
        newline();
        List<String> results1 = this.queryForObjectRefs();

        init();
        append("SELECT eo.id FROM RegistryPackage eo, ExternalIdentifier pid");
        newline();
        append("WHERE");
        newline();
        append(" eo.id IN ");
        append(uuids);
        append(" AND");
        newline();
        append(" pid.registryobject = eo.id AND");
        newline();
        append(" pid.identificationScheme IN ('" + 
                RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSSubmissionSet_patientid_uuid)
                + "','" +
                RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSFolder_patientid_uuid)
                + "') AND");
        newline();
        append(" pid.value = '");
        append(patient_id);
        append("'");
        newline();
        List<String> results = this.queryForObjectRefs();
        results.addAll(results1);
        return this.findMissingIds(uuids, results);
    }

    /**
     *
     * @param ids
     * @param results
     * @return
     */
    private List<String> findMissingIds(List<String> ids, List<String> results)
    {
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
        if (uuids.size() == 0) {
            return new ArrayList<String>();
        }
        init();
        append("SELECT eo.id FROM ExtrinsicObject eo, Association a");
        newline();
        append("WHERE");
        newline();
        append(" a.associationType IN ('");
        append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_ihe_assoc_type_xfrm));
        append("', '");
        append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_ihe_assoc_type_apnd));
        append("') AND");
        newline();
        append(" a.targetObject IN ");
        append(uuids);
        append(" AND");
        newline();
        append(" a.sourceObject = eo.id");
        newline();
        return this.queryForObjectRefs();
    }

    /**
     *
     * @param m
     * @return
     * @throws MetadataException
     */
    private ArrayList<String> getDocument_uids(Metadata m) throws MetadataException {
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
    private ArrayList<String> getSubmissionSet_uids(Metadata m, IdIndex idIndex) throws MetadataException {
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
    private ArrayList<String> getFolder_uids(Metadata m, IdIndex idIndex) throws MetadataException {
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
        if (uids.size() == 0) {
            return null;
        }
        init();
        append("SELECT eo.id from ExtrinsicObject eo, ExternalIdentifier ei");
        newline();
        append("WHERE");
        newline();
        append(" ei.registryobject = eo.id AND ");
        newline();
        append(" ei.identificationScheme = ");
        append("'" + 
                RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSDocumentEntry_uniqueid_uuid)
                + "'");
        append(" AND");
        newline();
        append(" ei.value IN ");
        append(uids);
        newline();
        return this.queryForObjectRefs();
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
     * @param identification_scheme
     * @return
     * @throws XdsException
     */
    private List<String> queryForObjectRefs_RegistryPackageByUID(List<String> uids, String identification_scheme) throws XdsException {
        if (uids.size() == 0) {
            return null;
        }
        init();
        append("SELECT ss.id");
        newline();
        append(" FROM RegistryPackage ss, ExternalIdentifier ei");
        newline();
        append("WHERE");
        newline();
        append(" ei.registryObject = ss.id AND");
        newline();
        append(" ei.identificationScheme = '" + 
                RegistryCodedValueMapper.convertIdScheme_ValueToCode(identification_scheme)
                + "' AND");
        newline();
        append(" ei.value IN ");
        append(uids);
        newline();
        return this.queryForObjectRefs();
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    @Override
    public Metadata run_internal() throws XdsException {
        // TODO Auto-generated method stub
        return null;
    }
}
