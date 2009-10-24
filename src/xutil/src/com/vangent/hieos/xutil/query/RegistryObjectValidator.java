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
package com.vangent.hieos.xutil.query;

import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.axiom.om.OMElement;

/**
 * 
 * @author thumbe
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
    ArrayList<String> uuidsOnly(ArrayList<String> ids) {
        ArrayList<String> uuids = new ArrayList<String>();
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
    public ArrayList<String> validateNotExists(ArrayList<String> ids) throws XdsException {
        ArrayList<String> uuids = uuidsOnly(ids);
        init();
        append("SELECT id FROM RegistryObject ro");
        newline();
        append("WHERE");
        newline();
        append("  ro.id IN ");
        append(uuids);
        newline();

        ArrayList results = this.query_for_object_refs();

        return results;
    }

    // uid_hash is uid => hash (null for non documents)
    /**
     *
     * @param metadata
     * @throws XdsException
     */
    public void validateProperUids(Metadata metadata) throws XdsException {

        HashMap<String, ArrayList<String>> uid_hash = metadata.getUidHashMap();

        ArrayList<String> uids = new ArrayList<String>();
        uids.addAll(uid_hash.keySet());

        ArrayList<String> uid_id_schemes = new ArrayList<String>();
        uid_id_schemes.add(MetadataSupport.XDSFolder_uniqueid_uuid);
        uid_id_schemes.add(MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
        uid_id_schemes.add(MetadataSupport.XDSDocumentEntry_uniqueid_uuid);

        init();
        append("SELECT ro.id from RegistryObject ro, ExternalIdentifier ei");
        newline();
        append("WHERE");
        newline();
        append(" ei.registryobject = ro.id AND ");
        newline();
        append("  ei.identificationScheme IN ");
        append(uid_id_schemes);
        append(" AND");
        newline();
        append("  ei.value IN ");
        append(uids);
        newline();

        // these uuids identify objects that carry one of the uids passed in in the map
        ArrayList<String> uuids = this.query_for_object_refs();

        if (uuids.size() == 0) {
            return;
        }

        // at least one uniqueId is already present in the registry. If it is from append document
        // and the hashes are the same.  Otherwise it is an error.

        this.return_leaf_class = true;
        OMElement objects = get_objects_by_uuid(uuids);   // LeafClass for offending objects
        if (objects == null) {
            throw new XdsInternalException("RegistryObjectValidator.validateProperUids(): could not retrieve LeafClass for ObjectRef obtained from registry: UUIDs were " + uuids);
        }

        Metadata m = MetadataParser.parseNonSubmission(objects);
        ArrayList<String> dup_uids = new ArrayList<String>();
        HashMap<String, OMElement> dup_objects = m.getUidMap();
        dup_uids.addAll(dup_objects.keySet());


        log_message.addOtherParam("dup uuids", uuids);
        log_message.addOtherParam("dup uids", dup_uids);

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

        HashMap<String, OMElement> docs_submit_uid_map = metadata.getUidMap(metadata.getExtrinsicObjects());
        for (String doc_uid : docs_submit_uid_map.keySet()) {
            if (dup_uids.contains(doc_uid)) {
                OMElement reg_obj = dup_objects.get(doc_uid);
                String type = reg_obj.getLocalName();
                if (!type.equals("ExtrinsicObject")) {
                    throw new MetadataValidationException("Document uniqueId " +
                            doc_uid +
                            " already present in the registry on a non-document object");
                }
                OMElement sub_obj = docs_submit_uid_map.get(doc_uid);
                String sub_hash = m.getSlotValue(sub_obj, "hash", 0);
                String reg_hash = m.getSlotValue(reg_obj, "hash", 0);
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
    public ArrayList validateDocuments(ArrayList uuids) throws XdsException {
        init();
        append("SELECT id FROM ExtrinsicObject eo");
        newline();
        append("WHERE");
        newline();
        append("  eo.id IN ");
        append(uuids);
        newline();

        ArrayList results = this.query_for_object_refs();

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

    }

    // validate the ids are in registry and belong to folders
    // return any that aren't
    /**
     *
     * @param ids
     * @return
     * @throws XdsException
     */
    public ArrayList<String> validateAreFolders(ArrayList<String> ids) throws XdsException {
        init();
        append("SELECT rp.id FROM RegistryPackage rp, ExternalIdentifier ei");
        newline();
        append("WHERE");
        newline();
        append("  rp.status = '");
        append(MetadataSupport.status_type_approved);
        append("' AND");
        newline();
        append("  rp.id IN ");
        append(ids);
        append(" AND");
        newline();
        append("  ei.registryObject = rp.id AND");
        newline();
        append("  ei.identificationScheme = '" + MetadataSupport.XDSFolder_patientid_uuid + "'");
        newline();

        br.setReason("Verify are Folders");

        ArrayList results1 = this.query_for_object_refs();

        ArrayList<String> missing = null;
        for (String id : ids) {
            if (!results1.contains(id)) {
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
    public ArrayList validateApproved(ArrayList uuids) throws XdsException {
        init();
        append("SELECT id FROM ExtrinsicObject eo");
        newline();
        append("WHERE");
        newline();
        append("  eo.status = '");
        append(MetadataSupport.status_type_approved);
        append("' AND");
        newline();
        append("  eo.id IN ");
        append(uuids);
        newline();

        ArrayList results1 = this.query_for_object_refs();

        init();
        append("SELECT id FROM RegistryPackage eo");
        newline();
        append("WHERE");
        newline();
        append("  eo.status = '");
        append(MetadataSupport.status_type_approved);
        append("' AND");
        newline();
        append("  eo.id IN ");
        append(uuids);
        newline();

        ArrayList results = this.query_for_object_refs();

        results.addAll(results1);

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
    }

    /**
     *
     * @param uuids
     * @param patient_id
     * @return
     * @throws XdsException
     */
    public ArrayList validateSamePatientId(ArrayList uuids, String patient_id)
            throws XdsException {
        if (uuids.size() == 0) {
            return null;
        }
        init();
        append("SELECT eo.id FROM ExtrinsicObject eo, ExternalIdentifier pid");
        newline();
        append("WHERE");
        newline();
        append("  eo.id IN ");
        append(uuids);
        append(" AND ");
        newline();
        append("  pid.registryobject = eo.id AND");
        newline();
        append("  pid.identificationScheme='urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427' AND");
        newline();
        append("  pid.value = '");
        append(patient_id);
        append("'");
        newline();

        ArrayList results1 = this.query_for_object_refs();

        init();
        append("SELECT eo.id FROM RegistryPackage eo, ExternalIdentifier pid");
        newline();
        append("WHERE");
        newline();
        append("  eo.id IN ");
        append(uuids);
        append(" AND");
        newline();
        append("  pid.registryobject = eo.id AND");
        newline();
        append("  pid.identificationScheme IN ('urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446','urn:uuid:f64ffdf0-4b97-4e06-b79f-a52b38ec2f8a') AND");
        newline();
        append("  pid.value = '");
        append(patient_id);
        append("'");
        newline();

        ArrayList results = this.query_for_object_refs();

        results.addAll(results1);

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
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    public ArrayList getXFRMandAPNDDocuments(ArrayList uuids) throws XdsException {
        if (uuids.size() == 0) {
            return new ArrayList();
        }
        init();
        append("SELECT eo.id FROM ExtrinsicObject eo, Association a");
        newline();
        append("WHERE");
        newline();
        append("  a.associationType in ('");
        append(MetadataSupport.xdsB_ihe_assoc_type_xfrm);
        append("', '");
        append(MetadataSupport.xdsB_ihe_assoc_type_apnd);
        append("') AND");
        newline();
        append("  a.targetObject IN ");
        append(uuids);
        append(" AND");
        newline();
        append("  a.sourceObject = eo.id");
        newline();

        return this.query_for_object_refs();
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
