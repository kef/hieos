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

import com.vangent.hieos.xutil.registry.BackendRegistry;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.XDSRegistryOutOfResourcesException;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsResultNotSinglePatientException;
import com.vangent.hieos.xutil.metadata.structure.SQCodeAnd;
import com.vangent.hieos.xutil.metadata.structure.SQCodeOr;
import com.vangent.hieos.xutil.metadata.structure.SQCodedTerm;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public abstract class StoredQuery {

    private boolean where = false;
    private boolean has_alternate_validation_errors = false;
    protected ErrorLogger response;
    protected XLogMessage log_message;
    protected SqParams params;
    protected StringBuffer query;
    protected BackendRegistry br;
    protected boolean return_leaf_class;
    private long maxLeafObjectsAllowedFromQuery = 25;  // Default.
    public boolean has_validation_errors = false;

    /**
     *
     * @return
     * @throws XdsException
     * @throws XDSRegistryOutOfResourcesException
     */
    abstract public Metadata run_internal() throws XdsException, XDSRegistryOutOfResourcesException;

    /**
     *
     * @param response
     * @param log_message
     */
    public StoredQuery(ErrorLogger response, XLogMessage log_message) {
        this.response = response;
        this.log_message = log_message;
        br = new BackendRegistry(response, log_message);

    }

    /**
     * Set the maximum value for constraining Leaf Class query results.
     *
     * @param maxLeafObjectsAllowedFromQuery maximum to allow.
     */
    public void setMaxLeafObjectsAllowedFromQuery(long maxLeafObjectsAllowedFromQuery) {
        this.maxLeafObjectsAllowedFromQuery = maxLeafObjectsAllowedFromQuery;
    }

    /**
     * Get the maximum value for constraining Leaf Class query results.
     * 
     * @return maximum to allow.
     */
    public long getMaxLeafObjectsAllowedFromQuery() {
        return this.maxLeafObjectsAllowedFromQuery;
    }

    /**
     *
     * @param params
     * @param return_objects
     * @param response
     * @param log_message
     * @param is_secure
     */
    public StoredQuery(SqParams params, boolean return_leaf_class, Response response, XLogMessage log_message) {
        this.response = response;
        this.log_message = log_message;
        this.params = params;
        br = new BackendRegistry(response, log_message);
        this.return_leaf_class = return_leaf_class;
    }

    /**
     *
     * @param params
     */
    protected void setParams(SqParams params) {
        this.params = params;
    }

    /**
     *
     * @return
     * @throws XdsException
     * @throws XDSRegistryOutOfResourcesException
     */
    public List<OMElement> run(boolean validateConsistentPatientId)
            throws XdsResultNotSinglePatientException, XDSRegistryOutOfResourcesException, XdsException {
        Metadata metadata = run_internal();
        if (metadata == null) {
            return null;
        }
        // Validate consistent patient identifiers (only if set to true).
        if ((validateConsistentPatientId == true) &&
                (metadata.isPatientIdConsistent() == false)) {
            throw new XdsResultNotSinglePatientException("More than one Patient ID in Stored Query result");
        }
        if (this.return_leaf_class) {
            return metadata.getAllObjects();//getV3();
        } else {
            return metadata.getObjectRefs(metadata.getMajorObjects(), false);
        }
    }

    /**
     *
     * @param rr
     * @return
     */
    protected List<String> getIdsFromRegistryResponse(OMElement rr) {
        List<String> ids = new ArrayList<String>();
        OMElement sqr = MetadataSupport.firstChildWithLocalName(rr, "RegistryObjectList");
        if (sqr == null) {
            return ids;
        }
        for (Iterator<OMElement> it = sqr.getChildElements(); it.hasNext();) {
            OMElement ele = (OMElement) it.next();
            if (ele.getLocalName().equals("ObjectRef")) {
                continue;
            }
            ids.add(ele.getAttributeValue(MetadataSupport.id_qname));
        }
        return ids;
    }

    /**
     * 
     * @return
     * @throws XMLParserException
     * @throws XdsException
     */
    List<String> queryForObjectRefs() throws XMLParserException, XdsException {
        return br.queryForObjectRefs(query.toString());
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    protected OMElement query() throws XdsException {
        String q = query.toString();
        if (log_message != null) {
            log_message.addOtherParam("raw query", q);
        }
        return br.query(q, this.return_leaf_class);
    }

    /**
     *
     * @param leaf_class
     * @return
     * @throws XdsException
     */
    /*
    protected OMElement query(boolean leaf_class) throws XdsException {
    String q = query.toString();
    if (log_message != null) {
    log_message.addOtherParam("raw query", q);
    }
    return br.query(q, leaf_class);
    }*/
    /**
     * Reinitialize the query buffer.
     */
    protected void init() {
        where = false;
        query = new StringBuffer();
    }

    /**
     * 
     * @param name
     * @param required
     * @param multiple
     * @param is_string
     * @param is_code
     * @param alternatives
     */
    protected void validateQueryParam(String name, boolean required, boolean multiple, boolean is_string, boolean is_code, boolean support_and_or, String... alternatives) {
        Object value = params.getParm(name);

        //System.out.println("validate_parm: name=" + name + " value=" + value + " required=" + required + " multiple=" + multiple + " is_string=" + is_string + " is_code=" + is_code + " alternatives=" + valuesAsString(null, alternatives));
        if (value == null && alternatives == null) {
            if (required) {
                response.add_error("XDSRegistryError", "Parameter " + name + " is required but not present in query", "StoredQuery.java", log_message);
                this.has_validation_errors = true;
                return;
            }
            return;
        }

        if (value == null && alternatives != null) {
            //System.out.println("looking for alternatives");
            if (!isAlternativePresent(alternatives)) {
                if (!has_alternate_validation_errors) {
                    response.add_error("XDSRegistryError", "One of these parameters must be present in the query: " + valuesAsString(name, alternatives), "StoredQuery.java", log_message);
                    has_alternate_validation_errors = true;  // keeps from generating multiples of this message
                }
                has_validation_errors = true;
                return;
            }
        }
        if (value == null) {
            return;
        }
        if (is_code) {
            if (!(value instanceof SQCodedTerm)) {
                response.add_error("XDSRegistryError", "Parameter, " + name +
                        ", must be a coded term", "StoredQuery.java", log_message);
                this.has_validation_errors = true;
                return;
            }

        } else {
            if (multiple && !(value instanceof ArrayList)) {
                response.add_error("XDSRegistryError", "Parameter, " + name + ", accepts multiple values but (  ) syntax is missing", "StoredQuery.java", log_message);
                this.has_validation_errors = true;
                return;
            }
            if (!multiple && (value instanceof ArrayList)) {
                response.add_error("XDSRegistryError", "Parameter, " + name + ", accepts single value value only but (  )  syntax is present", "StoredQuery.java", log_message);
                this.has_validation_errors = true;
                return;
            }
            if (multiple && (value instanceof ArrayList) && ((ArrayList) value).size() == 0) {
                response.add_error("XDSRegistryError", "Parameter, " + name + ", (  )  syntax is present but list is empty", "StoredQuery.java", log_message);
                this.has_validation_errors = true;
                return;
            }
            if (!(value instanceof ArrayList)) {
                return;
            }
            ArrayList values = (ArrayList) value;
            for (int i = 0; i < values.size(); i++) {
                Object a_o = values.get(i);
                if (is_string &&
                        !(a_o instanceof String) &&
                        !((a_o instanceof ArrayList) &&
                        ((ArrayList) a_o).size() > 0 &&
                        (((ArrayList) a_o).get(0) instanceof String))) {
                    response.add_error("XDSRegistryError", "Parameter, " + name + ", is not coded as a string (is type " + a_o.getClass().getName() + ") (single quotes missing?)", "StoredQuery.java", log_message);
                    this.has_validation_errors = true;
                }
                if (!is_string && !(a_o instanceof Integer)) {
                    response.add_error("XDSRegistryError", "Parameter, " + name + " is not coded as a number (is type " + a_o.getClass().getName() + ") (single quotes present)", "StoredQuery.java", log_message);
                    this.has_validation_errors = true;
                }
            }
        }
    }

    /**
     * 
     * @param mainName
     * @param alternatives
     * @return
     */
    private String valuesAsString(String mainName, String... alternatives) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        if (mainName != null) {
            buf.append(mainName);
        }
        if (alternatives != null) {
            for (int i = 0; i < alternatives.length; i++) {
                buf.append(" ").append(alternatives[i]);
            }
        }
        buf.append("]");
        return buf.toString();
    }

    /**
     *
     * @param alternatives
     * @return
     */
    private boolean isAlternativePresent(String[] alternatives) {
        if (alternatives == null) {
            return false;
        }
        for (String alternative : alternatives) {
            Object value = params.getParm(alternative);
            if (value != null) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    protected OMElement getDocumentByUUID(String uuid) throws XdsException {
        ArrayList<String> ids = new ArrayList<String>();
        ids.add(uuid);
        return getDocumentByUUID(ids);
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    protected OMElement getDocumentByUUID(List<String> uuids) throws XdsException {
        init();
        select("eo");
        append("FROM ExtrinsicObject eo");
        newline();
        append(" WHERE");
        newline();
        append(" eo.id IN ");
        append(uuids);
        newline();
        return query();
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    protected OMElement getDocumentByUID(String uid) throws XdsException {
        ArrayList<String> uids = new ArrayList<String>();
        uids.add(uid);
        return getDocumentByUID(uids);
    }

    /**
     *
     * @param uids
     * @return
     * @throws XdsException
     */
    protected OMElement getDocumentByUID(List<String> uids) throws XdsException {
        init();
        select("eo");
        append("FROM ExtrinsicObject eo, ExternalIdentifier ei");
        newline();
        append("WHERE");
        newline();
        append(" ei.registryObject=eo.id AND");
        newline();
        append(" ei.identificationScheme='urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab' AND");
        newline();
        append(" ei.value IN ");
        append(uids);
        newline();
        return query();
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    protected String getDocumentIDFromUID(String uid) throws XdsException {
        boolean rlc = return_leaf_class;
        this.return_leaf_class = false;
        OMElement result = getDocumentByUID(uid);
        Metadata metadata = MetadataParser.parseNonSubmission(result);
        List<OMElement> obj_refs = metadata.getObjectRefs();
        this.return_leaf_class = rlc;
        if (obj_refs.size() == 0) {
            return null;
        }
        return metadata.getId(obj_refs.get(0));
    }

    /**
     *
     * @param uuids
     * @param assoc_types
     * @return
     * @throws XdsException
     */
    protected OMElement getAssociationsByUUID(List<String> uuids, List<String> assoc_types) throws XdsException {
        init();
        select("a");
        append("FROM Association a");
        newline();
        append("WHERE");
        newline();
        append(" (a.sourceObject IN ");
        append(uuids);
        append(" OR");
        newline();
        append(" a.targetObject IN ");
        append(uuids);
        append(" )");
        newline();
        if (assoc_types != null) {
            append(" AND a.associationType IN ");
            append(assoc_types);
            newline();
        }
        return query();
    }

    /**
     *
     * @param rp_id
     * @param identification_scheme
     * @return
     * @throws XdsException
     * @throws LoggerException
     */
    public OMElement getRegistryPackageByID(List<String> rp_id, String identification_scheme)
            throws XdsException {
        init();
        select("ss");
        append("FROM RegistryPackage ss, ExternalIdentifier uniq");
        newline();
        append("WHERE ");
        newline();
        append(" ss.id IN ");
        append(rp_id);
        append(" AND");
        newline();
        append(" uniq.registryObject = ss.id");
        newline();
        if (identification_scheme != null) {
            append(" AND uniq.identificationScheme = '" + identification_scheme + "' ");
            newline();
        }
        return query();
    }

    /**
     *
     * @param uid
     * @param identification_scheme
     * @return
     * @throws XdsException
     */
    public OMElement getRegistryPackageByUID(String uid, String identification_scheme) throws XdsException {
        init();
        select("ss");
        append("FROM RegistryPackage ss, ExternalIdentifier uniq");
        newline();
        append("WHERE");
        newline();
        append("  uniq.registryObject = ss.id AND");
        newline();
        append("  uniq.identificationScheme = '" + identification_scheme + "' AND");
        newline();
        append("  uniq.value = '" + uid + "'");
        return query();
    }

    /**
     *
     * @param uids
     * @param identification_scheme
     * @return
     * @throws XdsException
     */
    public OMElement getRegistryPackageByUID(List<String> uids, String identification_scheme) throws XdsException {
        init();
        select("ss");
        append("FROM RegistryPackage ss, ExternalIdentifier uniq");
        newline();
        append("WHERE");
        newline();
        append(" uniq.registryObject = ss.id AND");
        newline();
        append(" uniq.identificationScheme = '" + identification_scheme + "' AND");
        newline();
        append(" uniq.value IN ");
        append(uids);
        return query();
    }

    /**
     *
     * @param ss_uuid
     * @param identification_scheme
     * @return
     * @throws XdsException
     */
    protected OMElement getRegistryPackageByUUID(String ss_uuid, String identification_scheme)
            throws XdsException {
        init();
        select("ss");
        append("FROM RegistryPackage ss, ExternalIdentifier uniq");
        newline();
        append("WHERE");
        newline();
        append(" ss.id = '" + ss_uuid + "' AND");
        newline();
        append(" uniq.registryObject = ss.id AND");
        newline();
        append(" uniq.identificationScheme = '" + identification_scheme + "' ");
        newline();
        return query();
    }

    /**
     *
     * @param ss_uuid
     * @param identification_scheme
     * @return
     * @throws XdsException
     */
    protected OMElement getRegistryPackageByUUID(List<String> ss_uuid, String identification_scheme)
            throws XdsException {
        init();
        select("ss");
        append("FROM RegistryPackage ss, ExternalIdentifier uniq");
        newline();
        append("WHERE");
        newline();
        append(" ss.id IN ");
        append(ss_uuid);
        append(" AND");
        newline();
        append(" uniq.registryObject = ss.id AND");
        newline();
        append(" uniq.identificationScheme = '" + identification_scheme + "' ");
        newline();
        return query();
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    /*
    protected OMElement getObjectsByUUID(List<String> uuids) throws XdsException {
    if (uuids.size() == 0) {
    return null;
    }
    init();
    select("ro");
    append("FROM RegistryObject ro");
    newline();
    append("WHERE");
    newline();
    append(" ro.id IN ");
    append(uuids);
    newline();
    return query();
    } */
    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    protected OMElement getFolderByUUID(String uuid) throws XdsException {
        return getRegistryPackageByUUID(uuid, MetadataSupport.XDSFolder_uniqueid_uuid);
    }

    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    protected OMElement getFolderByUUID(List<String> uuid) throws XdsException {
        return getRegistryPackageByUUID(uuid, MetadataSupport.XDSFolder_uniqueid_uuid);
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    protected OMElement getFolderByUID(String uid) throws XdsException {
        return getRegistryPackageByUID(uid, MetadataSupport.XDSFolder_uniqueid_uuid);
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    protected OMElement getFolderByUID(List<String> uid) throws XdsException {
        return getRegistryPackageByUID(uid, MetadataSupport.XDSFolder_uniqueid_uuid);
    }

    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    protected OMElement getSubmissionSetByUUID(String uuid) throws XdsException {
        return getRegistryPackageByUUID(uuid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
    }

    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    protected OMElement getSubmissionSetByUUID(List<String> uuid) throws XdsException {
        return getRegistryPackageByUUID(uuid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    protected OMElement getSubmissionSetByUID(String uid) throws XdsException {
        return getRegistryPackageByUID(uid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
    }

    /**
     *
     * @param uids
     * @return
     * @throws XdsException
     */
    protected OMElement getSubmissionSetByUID(List<String> uid) throws XdsException {
        return getRegistryPackageByUID(uid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
    }

    /**
     * 
     * @param ss_uuid
     * @param format_codes
     * @param conf_codes
     * @return
     * @throws XdsException
     */
    protected OMElement getSubmissionSetDocuments(String ss_uuid, SQCodedTerm format_codes, SQCodedTerm conf_codes)
            throws XdsException {
        init();
        select("obj");
        append("FROM ExtrinsicObject obj, Association a");
        newline();
        if (conf_codes != null) {
            append(declareClassifications(conf_codes));
        }
        newline();
        if (format_codes != null) {
            append(declareClassifications(format_codes));
        }
        append("WHERE");
        newline();
        append(" a.sourceObject = '" + ss_uuid + "' AND");
        newline();
        append(" a.associationType = '");
        append(MetadataSupport.xdsB_eb_assoc_type_has_member);
        append("' AND");
        newline();
        append(" a.targetObject = obj.id ");
        newline();
        addCode(conf_codes);
        addCode(format_codes);
        return query();
    }

    /**
     *
     * @param fol_uuid
     * @param format_codes
     * @param conf_codes
     * @return
     * @throws XdsException
     */
    protected OMElement getFolderDocuments(String fol_uuid, SQCodedTerm format_codes, SQCodedTerm conf_codes)
            throws XdsException {
        return getSubmissionSetDocuments(fol_uuid, format_codes, conf_codes);
    }

    /**
     *
     * @param package_uuid
     * @param content_uuids
     * @return
     * @throws XdsException
     */
    /*
    protected OMElement getAssociations(String package_uuid, List<String> content_uuids) throws XdsException {
    init();
    select("ass");
    append("FROM Association ass");
    newline();
    append("WHERE");
    newline();
    append(" ass.associationType = '");
    append(MetadataSupport.xdsB_eb_assoc_type_has_member);
    append("' AND");
    newline();
    append(" ass.sourceObject = '" + package_uuid + "' AND");
    newline();
    append(" ass.targetObject IN (");
    for (int i = 0; i < content_uuids.size(); i++) {
    if (i > 0) {
    append(",");
    }
    append("'" + (String) content_uuids.get(i) + "'");
    }
    append(")");
    newline();
    return query();
    } */
    /**
     *
     * @param source_or_target_ids
     * @param assoc_types
     * @return
     * @throws XdsException
     */
    public OMElement getAssociations(List<String> source_or_target_ids, List<String> assoc_types) throws XdsException {
        init();
        select("a");
        append("FROM Association a");
        newline();
        append("WHERE ");
        newline();
        append(" (a.sourceObject IN ");
        append(source_or_target_ids);
        append(" OR");
        newline();
        append(" a.targetObject IN ");
        append(source_or_target_ids);
        append(" )");
        newline();
        if (assoc_types != null) {
            append(" AND a.associationType IN ");
            append(assoc_types);
            newline();
        }
        return query();
    }

    /**
     *
     * @param type
     * @param froms
     * @param tos
     * @return
     * @throws XdsException
     */
    public OMElement getAssocations(String type, List<String> froms, List<String> tos)
            throws XdsException {
        init();
        select("a");
        append("FROM Association a");
        newline();
        append("WHERE ");
        newline();
        append(" a.associationType = '" + type + "' AND");
        newline();
        append(" a.sourceObject IN");
        append(froms);
        append(" AND");
        newline();
        append(" a.targetObject IN");
        append(tos);
        newline();
        return query();
    }

    /**
     * 
     * @param assoc_id
     * @return
     * @throws XdsException
     */
    public OMElement getAssociationByID(String assoc_id)
            throws XdsException {
        init();
        select("as");
        append("FROM Association as");
        newline();
        append("WHERE ");
        newline();
        append(" as.id = '" + assoc_id + "'");
        newline();
        return query();
    }

    /**
     *
     * @param assoc_ids
     * @return
     * @throws XdsException
     */
    public OMElement getAssociationByID(List<String> assoc_ids)
            throws XdsException {
        init();
        select("ass");
        append("FROM Association ass");
        newline();
        append("WHERE ");
        newline();
        append(" ass.id IN ");
        append(assoc_ids);
        newline();
        return query();
    }

    /**
     *
     * @param package_uuids
     * @param content_uuids
     * @return
     * @throws XdsException
     */
    protected OMElement getRegistryPackageAssociations(List<String> package_uuids, List<String> content_uuids) throws XdsException {
        init();
        select("ass");
        append("FROM Association ass");
        newline();
        append("WHERE");
        newline();
        append(" ass.associationType = '");
        append(MetadataSupport.xdsB_eb_assoc_type_has_member);
        append("' AND");
        newline();
        append(" ass.sourceObject IN (");
        for (int i = 0; i < package_uuids.size(); i++) {
            if (i > 0) {
                append(",");
            }
            append("'" + (String) package_uuids.get(i) + "'");
        }
        append(") AND");
        newline();
        append("ass.targetObject IN (");
        for (int i = 0; i < content_uuids.size(); i++) {
            if (i > 0) {
                append(",");
            }
            append("'" + (String) content_uuids.get(i) + "'");
        }
        append(")");
        newline();
        return query();
    }

    /**
     * 
     * @param package_uuids
     * @return
     * @throws XdsException
     */
    protected OMElement getRegistryPackageAssociations(List<String> package_uuids) throws XdsException {
        if (package_uuids == null || package_uuids.size() == 0) {
            return null;
        }
        init();
        select("ass");
        append("FROM Association ass");
        newline();
        append("WHERE");
        newline();
        append(" ass.associationType = '");
        append(MetadataSupport.xdsB_eb_assoc_type_has_member);
        append("' AND");
        newline();
        append(" ass.sourceObject IN (");
        for (int i = 0; i < package_uuids.size(); i++) {
            if (i > 0) {
                append(",");
            }
            append("'" + (String) package_uuids.get(i) + "'");
        }
        append(")");
        newline();
        return query();
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    protected OMElement getSubmissionSetsOfContents(List<String> uuids) throws XdsException {
        init();
        select("rp");
        append("FROM RegistryPackage rp, Association a, ExternalIdentifier ei");
        newline();
        append("WHERE ");
        newline();
        append(" a.sourceObject = rp.id AND");
        newline();
        append(" a.associationType = '");
        append(MetadataSupport.xdsB_eb_assoc_type_has_member);
        append("' AND");
        newline();
        append(" a.targetObject IN ");
        append(uuids);
        append(" AND");
        newline();
        append(" ei.registryObject = rp.id AND");
        newline();
        append(" ei.identificationScheme = 'urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446'");
        newline();
        return query();
    }

    /**
     *
     * @param ss_uuid
     * @return
     * @throws XdsException
     */
    protected OMElement getSubmissionSetFolders(String ss_uuid) throws XdsException {
        init();
        select("fol");
        append("FROM RegistryPackage fol, Association a");
        newline();
        append("WHERE");
        newline();
        append(" a.associationType = '");
        append(MetadataSupport.xdsB_eb_assoc_type_has_member);
        append("' AND");
        newline();
        append(" a.sourceObject = '" + ss_uuid + "' AND");
        newline();
        append(" a.targetObject = fol.id ");
        newline();
        return query();
    }

    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    protected OMElement getFoldersForDocument(String uuid) throws XdsException {
        init();
        select("fol");
        append("FROM RegistryPackage fol, ExternalIdentifier uniq, Association a");
        newline();
        append("WHERE");
        newline();
        append("   a.associationType = '");
        append(MetadataSupport.xdsB_eb_assoc_type_has_member);
        append("' AND");
        newline();
        append(" a.targetObject = '" + uuid + "' AND");
        newline();
        append(" a.sourceObject = fol.id AND");
        newline();
        append(" uniq.registryObject = fol.id AND");
        newline();
        append(" uniq.identificationScheme = '" + MetadataSupport.XDSFolder_uniqueid_uuid + "' ");
        return query();
    }

    /**
     *
     * @param term
     * @return
     */
    public String declareClassifications(SQCodedTerm term) {
        if (term instanceof SQCodeOr) {
            return declareClassifications((SQCodeOr) term);
        }
        if (term instanceof SQCodeAnd) {
            return declareClassifications((SQCodeAnd) term);
        }
        return null;
    }

    /**
     *
     * @param or
     * @return
     */
    public String declareClassifications(SQCodeOr or) {
        StringBuffer buf = new StringBuffer();
        buf.append(", Classification " + or.getCodeVarName() + "\n");
        buf.append(", Slot " + or.getSchemeVarName() + "\n");
        return buf.toString();
    }

    /**
     *
     * @param and
     * @return
     */
    public String declareClassifications(SQCodeAnd and) {
        StringBuffer buf = new StringBuffer();
        for (String name : and.getCodeVarNames()) {
            buf.append(", Classification " + name + "\n");
        }
        for (String name : and.getSchemeVarNames()) {
            buf.append(", Slot " + name + "\n");
        }
        return buf.toString();
    }

    /**
     * 
     * @param names
     * @return
     */
    public String declareClassifications(List<String> names) {
        StringBuffer buf = new StringBuffer();
        for (String name : names) {
            buf.append(", Classification " + name + "\n");
        }
        return buf.toString();
    }

    /**
     *
     * @param term
     * @throws MetadataException
     */
    public void addCode(SQCodedTerm term) throws MetadataException {
        if (term instanceof SQCodeOr) {
            addCode((SQCodeOr) term);
        }
        if (term instanceof SQCodeAnd) {
            addCode((SQCodeAnd) term);
        }
    }

    /**
     *
     * @param term
     * @throws MetadataException
     */
    private void addCode(SQCodeOr term) throws MetadataException {
        and();
        append(" (");
        append(term.getCodeVarName());
        append(".classifiedobject = obj.id AND ");
        newline();
        append("  ");
        append(term.getCodeVarName());
        append(".classificationScheme = '");
        append(term.classification);
        append("' AND ");
        newline();
        append("  ");
        append(term.getCodeVarName());
        append(".nodeRepresentation IN ");
        append(term.getCodes());
        append(" )");
        newline();

        and();
        append(" (");
        append(term.getSchemeVarName());
        append(".parent = ");
        append(term.getCodeVarName());
        append(".id AND   ");
        newline();
        append("  ");
        append(term.getSchemeVarName());
        append(".name_ = 'codingScheme' AND   ");
        newline();
        append("  ");
        append(term.getSchemeVarName());
        append(".value IN ");
        append(term.getSchemes());
        append(" )");
        newline();
    }

    /**
     *
     * @param term
     * @throws MetadataException
     */
    private void addCode(SQCodeAnd term) throws MetadataException {
        for (SQCodeOr or : term.getCodeOrs()) {
            addCode(or);
        }
    }

    // times come in as numeric values but convert them to string values to avoid numeric overflow
    /**
     * 
     * @param att_name
     * @param from_var
     * @param to_var
     * @param from_limit
     * @param to_limit
     * @param var_name
     */
    public void addTimes(String att_name, String from_var, String to_var,
            String from_limit, String to_limit, String var_name) {
        if (from_limit != null) {
            // Parent:
            and();
            append(" (");
            append(from_var);
            append(".parent = " + var_name + ".id AND ");
            newline();
            // Name:
            append("  ");
            append(from_var);
            append(".name_ = '");
            append(att_name);
            append("' AND     ");
            newline();
            // Value:
            append("  ");
            append(from_var);
            append(".value >= ");
            appendQuoted(from_limit);
            append(" ) ");
            newline();
        }

        if (to_limit != null) {
            // Parent:
            and();
            append(" (");
            append(to_var);
            append(".parent = " + var_name + ".id AND ");
            newline();
            // Name:
            append("  ");
            append(to_var);
            append(".name_ = '");
            append(att_name);
            append("' AND     ");
            newline();
            // Value:
            append("  ");
            append(to_var);
            append(".value < ");
            appendQuoted(to_limit);
            append(" ) ");
            newline();
        }
    }

    /**
     * 
     */
    public void and() {
        if (!where) {
            append("AND");
        }
        where = false;
    }

    /**/
    /**
     *
     * @param varName
     */
    protected void select(String varName) {
        if (this.return_leaf_class) {
            append("SELECT * ");
        } else {
            append("SELECT ");
            append(varName);
            append(".id ");
        }
    }

    /**
     *
     */
    public void where() {
        append("WHERE");
        where = true;
    }

    /**
     *
     * @param x
     */
    protected void append(String x) {
        where = false;
        query.append(x);
    }

    /**
     *
     * @param x
     */
    protected void appendQuoted(String x) {
        where = false;
        query.append("'");
        query.append(x);
        query.append("'");
    }

    /**
     *
     */
    protected void newline() {
        query.append("\n");
    }

    /**
     *
     * @param list
     * @throws MetadataException
     */
    protected void append(List list) throws MetadataException {
        where = false;
        query.append("(");
        boolean first_time = true;
        for (Object o : list) {
            //for (int i = 0; i < list.size(); i++) {
            if (!first_time) {
                query.append(",");
            }
            //Object o = list.get(i);
            if (o instanceof String) {
                query.append("'" + (String) o + "'");
            } else if (o instanceof Integer) {
                query.append(((Integer) o).toString());
            } else {
                throw new MetadataException("Parameter value " + o + " cannot be decoded");
            }
            first_time = false;
        }
        query.append(")");
    }
}

