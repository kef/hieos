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

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.exception.XDSRegistryOutOfResourcesException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsResultNotSinglePatientException;
import com.vangent.hieos.xutil.metadata.structure.SQCodeAnd;
import com.vangent.hieos.xutil.metadata.structure.SQCodedTerm;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.axiom.om.OMElement;
import org.freebxml.omar.server.persistence.rdb.RegistryCodedValueMapper;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public abstract class StoredQuery {

    //private boolean where = false;
    private boolean hasAlternateValidationErrors = false;
    private ErrorLogger response;
    private XLogMessage logMessage;
    private SqParams sqParams;
    //private StringBuffer query;
    private BackendRegistry backendRegistry;
    private boolean returnLeafClass;
    private long maxLeafObjectsAllowedFromQuery = 25;  // Default.
    /**
     *
     */
    private boolean hasValidationErrors = false;

    /**
     *
     * @return
     * @throws XdsException
     * @throws XDSRegistryOutOfResourcesException
     */
    abstract public Metadata runInternal() throws XdsException, XDSRegistryOutOfResourcesException;

    /**
     *
     * @param response
     * @param logMessage
     * @param backendRegistry
     */
    public StoredQuery(ErrorLogger response, XLogMessage logMessage, BackendRegistry backendRegistry) {
        this.response = response;
        this.logMessage = logMessage;
        //br = new BackendRegistry(response, log_message);
        this.backendRegistry = backendRegistry;
    }

    /**
     *
     * @param params
     * @param returnLeafClass
     * @param logMessage
     * @param response
     * @param backendRegistry
     */
    public StoredQuery(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry) {
        this.response = response;
        this.logMessage = logMessage;
        this.sqParams = params;
        //br = new BackendRegistry(response, log_message);
        this.backendRegistry = backendRegistry;
        this.returnLeafClass = returnLeafClass;
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
     * @return
     */
    public boolean isReturnLeafClass() {
        return returnLeafClass;
    }

    /**
     *
     * @param returnLeafClass
     */
    public void setReturnLeafClass(boolean returnLeafClass) {
        this.returnLeafClass = returnLeafClass;
    }

    /**
     *
     * @return
     */
    public XLogMessage getLogMessage() {
        return logMessage;
    }

    /**
     *
     * @param sqParams
     */
    public void setSqParams(SqParams sqParams) {
        this.sqParams = sqParams;
    }

    /**
     * 
     * @return
     */
    public SqParams getSqParams() {
        return this.sqParams;
    }

    /**
     * 
     * @return
     */
    public BackendRegistry getBackendRegistry() {
        return backendRegistry;
    }

    /**
     *
     * @return
     */
    public boolean hasValidationErrors() {
        return hasValidationErrors;
    }

    /**
     *
     * @param validateConsistentPatientId
     * @return
     * @throws XdsResultNotSinglePatientException
     * @throws XdsException
     * @throws XDSRegistryOutOfResourcesException
     */
    public List<OMElement> run(boolean validateConsistentPatientId)
            throws XdsResultNotSinglePatientException, XDSRegistryOutOfResourcesException, XdsException {
        Metadata metadata = runInternal();
        if (metadata == null) {
            return null;
        }
        // Validate consistent patient identifiers (only if set to true).
        if ((validateConsistentPatientId == true)
                && (metadata.isPatientIdConsistent() == false)) {
            throw new XdsResultNotSinglePatientException("More than one Patient ID in Stored Query result");
        }
        if (this.returnLeafClass) {
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
    public List<String> getIdsFromRegistryResponse(OMElement rr) {
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
     * @param sqb
     * @return
     * @throws XdsException
     */
    public List<String> runQueryForObjectRefs(StoredQueryBuilder sqb) throws XdsException {
        String query = sqb.getQuery();
        //String q = sb.toString();
        if (logMessage != null) {
            logMessage.addOtherParam("raw query", query);
        }
        return backendRegistry.runQueryForObjectRefs(query);
    }

    /**
     *
     * @param sqb 
     * @return
     * @throws XdsException
     */
    public OMElement runQuery(StoredQueryBuilder sqb) throws XdsException {
        String query = sqb.getQuery();
        //String q = sb.toString();
        if (logMessage != null) {
            logMessage.addOtherParam("raw query", query);
        }
        return backendRegistry.runQuery(query, this.returnLeafClass);
    }

    /**
     * 
     * @param name
     * @param required
     * @param multiple
     * @param isString
     * @param isCode 
     * @param andOrOk
     * @param alternatives
     */
    public void validateQueryParam(String name, boolean required, boolean multiple, boolean isString, boolean isCode, boolean andOrOk, String... alternatives) {
        Object value = sqParams.getParm(name);

        //System.out.println("validate_parm: name=" + name + " value=" + value + " required=" + required + " multiple=" + multiple + " is_string=" + is_string + " is_code=" + is_code + " alternatives=" + valuesAsString(null, alternatives));
        if (value == null && alternatives == null) {
            if (required) {
                response.add_error("XDSRegistryError", "Parameter " + name + " is required but not present in query", "StoredQuery.java", logMessage);
                this.hasValidationErrors = true;
                return;
            }
            return;
        }

        if (value == null && alternatives != null) {
            //System.out.println("looking for alternatives");
            if (!isAlternativePresent(alternatives)) {
                if (!hasAlternateValidationErrors) {
                    response.add_error("XDSRegistryError", "One of these parameters must be present in the query: " + valuesAsString(name, alternatives), "StoredQuery.java", logMessage);
                    hasAlternateValidationErrors = true;  // keeps from generating multiples of this message
                }
                hasValidationErrors = true;
                return;
            }
        }
        if (value == null) {
            return;
        }
        if (isCode) {
            if (!(value instanceof SQCodedTerm)) {
                response.add_error("XDSRegistryError", "Parameter, " + name
                        + ", must be a coded term", "StoredQuery.java", logMessage);
                this.hasValidationErrors = true;
                return;
            }
            if ((value instanceof SQCodeAnd) && !andOrOk) {
                response.add_error("XDSRegistryError", "Parameter, " + name
                        + ", is coded with AND/OR semantics which are not allowed on this parameter", "StoredQuery.java", logMessage);
                this.hasValidationErrors = true;
                return;
            }
        } else {
            if (multiple && !(value instanceof ArrayList)) {
                response.add_error("XDSRegistryError", "Parameter, " + name + ", accepts multiple values but (  ) syntax is missing", "StoredQuery.java", logMessage);
                this.hasValidationErrors = true;
                return;
            }
            if (!multiple && (value instanceof ArrayList)) {
                response.add_error("XDSRegistryError", "Parameter, " + name + ", accepts single value value only but (  )  syntax is present", "StoredQuery.java", logMessage);
                this.hasValidationErrors = true;
                return;
            }
            if (multiple && (value instanceof ArrayList) && ((ArrayList) value).isEmpty()) {
                response.add_error("XDSRegistryError", "Parameter, " + name + ", (  )  syntax is present but list is empty", "StoredQuery.java", logMessage);
                this.hasValidationErrors = true;
                return;
            }
            if (!(value instanceof ArrayList)) {
                return;
            }
            ArrayList values = (ArrayList) value;
            for (int i = 0; i < values.size(); i++) {
                Object a_o = values.get(i);
                if (isString
                        && !(a_o instanceof String)
                        && !((a_o instanceof ArrayList)
                        && ((ArrayList) a_o).size() > 0
                        && (((ArrayList) a_o).get(0) instanceof String))) {
                    response.add_error("XDSRegistryError", "Parameter, " + name + ", is not coded as a string (is type " + a_o.getClass().getName() + ") (single quotes missing?)", "StoredQuery.java", logMessage);
                    this.hasValidationErrors = true;
                }
                if (!isString && !(a_o instanceof Integer)) {
                    response.add_error("XDSRegistryError", "Parameter, " + name + " is not coded as a number (is type " + a_o.getClass().getName() + ") (single quotes present)", "StoredQuery.java", logMessage);
                    this.hasValidationErrors = true;
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
        StringBuilder buf = new StringBuilder();
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
            Object value = sqParams.getParm(alternative);
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
    public OMElement getDocumentByUUID(String uuid) throws XdsException {
        ArrayList<String> ids = new ArrayList<String>();
        ids.add(uuid);
        return this.getDocumentByUUID(ids);
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    public OMElement getDocumentByUUID(List<String> uuids) throws XdsException {
        StoredQueryBuilder sqb = StoredQuery.getSQL_DocumentByUUID(uuids, this.returnLeafClass);
        return this.runQuery(sqb);
    }

    /**
     *
     * @param lid
     * @param status
     * @param version
     * @return
     * @throws XdsException
     */
    public OMElement getDocumentsByLID(String lid, String status, String version) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.returnLeafClass);
        sqb.select("eo");
        sqb.append("FROM ExtrinsicObject eo");
        sqb.newline();
        sqb.append(" WHERE");
        sqb.newline();
        sqb.append(" eo.lid=");
        sqb.appendQuoted(lid);
        sqb.append(" AND eo.status=");
        sqb.appendQuoted(RegistryCodedValueMapper.convertStatus_ValueToCode(status));
        sqb.append(" AND eo.versionname=");
        sqb.appendQuoted(version);
        sqb.newline();
        return this.runQuery(sqb);
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    public OMElement getDocumentByUID(String uid) throws XdsException {
        ArrayList<String> uids = new ArrayList<String>();
        uids.add(uid);
        return this.getDocumentByUID(uids);
    }

    /**
     *
     * @param uids
     * @return
     * @throws XdsException
     */
    public OMElement getDocumentByUID(List<String> uids) throws XdsException {
        StoredQueryBuilder sqb = StoredQuery.getSQL_DocumentByUID(uids, this.returnLeafClass);
        return this.runQuery(sqb);
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    public String getDocumentUUIDFromUID(String uid) throws XdsException {
        // FIXME: Should be an alternative/efficient way to do this..
        boolean rlc = returnLeafClass;
        this.returnLeafClass = false;
        OMElement result = getDocumentByUID(uid);
        Metadata metadata = MetadataParser.parseNonSubmission(result);
        List<OMElement> objRefs = metadata.getObjectRefs();
        this.returnLeafClass = rlc;
        if (objRefs.isEmpty()) {
            return null;
        }
        return metadata.getId(objRefs.get(0));
    }

    /**
     *
     * @param uuids
     * @param assocTypes
     * @return
     * @throws XdsException
     */
    public OMElement getAssociationsByUUID(List<String> uuids, List<String> assocTypes) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.returnLeafClass);
        sqb.select("a");
        sqb.append("FROM Association a");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" (a.sourceObject IN ");
        sqb.append(uuids);
        sqb.append(" OR");
        sqb.newline();
        sqb.append(" a.targetObject IN ");
        sqb.append(uuids);
        sqb.append(" )");
        sqb.newline();
        if (assocTypes != null) {
            sqb.append(" AND a.associationType IN ");
            sqb.append(RegistryCodedValueMapper.convertAssocType_ValueToCode(assocTypes));
            sqb.newline();
        }
        return this.runQuery(sqb);
    }

    /**
     *
     * @param uuids
     * @param identificationScheme
     * @return
     * @throws XdsException
     */
    //public OMElement getRegistryPackageByID(List<String> uuids, String identificationScheme)
    //        throws XdsException {
    //    StoredQueryBuilder sqb = StoredQuery.getRegistryPackageByUUID(uuids, identificationScheme, this.returnLeafClass);
    //    return this.runQuery(sqb);
    //}

    /**
     *
     * @param uuid
     * @param identificationScheme
     * @return
     * @throws XdsException
     */
    public OMElement getRegistryPackageByUUID(String uuid, String identificationScheme)
            throws XdsException {
        ArrayList<String> uuids = new ArrayList<String>();
        uuids.add(uuid);
        return this.getRegistryPackageByUUID(uuids, identificationScheme);
    }

    /**
     *
     * @param uuids
     * @param identificationScheme
     * @return
     * @throws XdsException
     */
    public OMElement getRegistryPackageByUUID(List<String> uuids, String identificationScheme)
            throws XdsException {
        StoredQueryBuilder sqb = StoredQuery.getSQL_RegistryPackageByUUID(uuids, identificationScheme, this.returnLeafClass);
        return runQuery(sqb);
    }

    /**
     *
     * @param uid
     * @param identificationScheme
     * @return
     * @throws XdsException
     */
    public OMElement getRegistryPackageByUID(String uid, String identificationScheme) throws XdsException {
        ArrayList<String> uids = new ArrayList<String>();
        uids.add(uid);
        return this.getRegistryPackageByUID(uids, identificationScheme);
    }

    /**
     *
     * @param uids
     * @param identificationScheme
     * @return
     * @throws XdsException
     */
    public OMElement getRegistryPackageByUID(List<String> uids, String identificationScheme) throws XdsException {
        StoredQueryBuilder sqb = StoredQuery.getSQL_RegistryPackageByUID(uids, identificationScheme, this.returnLeafClass);
        return this.runQuery(sqb);
    }

   
    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    public OMElement getFolderByUUID(String uuid) throws XdsException {
        return getRegistryPackageByUUID(uuid, MetadataSupport.XDSFolder_uniqueid_uuid);
    }

    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    public OMElement getFolderByUUID(List<String> uuid) throws XdsException {
        return getRegistryPackageByUUID(uuid, MetadataSupport.XDSFolder_uniqueid_uuid);
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    public OMElement getFolderByUID(String uid) throws XdsException {
        return getRegistryPackageByUID(uid, MetadataSupport.XDSFolder_uniqueid_uuid);
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    public OMElement getFolderByUID(List<String> uid) throws XdsException {
        return getRegistryPackageByUID(uid, MetadataSupport.XDSFolder_uniqueid_uuid);
    }

    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    public OMElement getSubmissionSetByUUID(String uuid) throws XdsException {
        return getRegistryPackageByUUID(uuid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
    }

    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    public OMElement getSubmissionSetByUUID(List<String> uuid) throws XdsException {
        return getRegistryPackageByUUID(uuid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    public OMElement getSubmissionSetByUID(String uid) throws XdsException {
        return getRegistryPackageByUID(uid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
    }

    /**
     *
     * @param uid
     * @return
     * @throws XdsException
     */
    public OMElement getSubmissionSetByUID(List<String> uid) throws XdsException {
        return getRegistryPackageByUID(uid, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
    }

    /**
     *
     * @param uuid
     * @param formatCodes
     * @param confidentialityCodes
     * @return
     * @throws XdsException
     */
    public OMElement getSubmissionSetDocuments(String uuid, SQCodedTerm formatCodes, SQCodedTerm confidentialityCodes)
            throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.returnLeafClass);
        //sqb.initQuery();
        sqb.select("obj");
        sqb.append("FROM ExtrinsicObject obj, Association a");
        sqb.newline();
        sqb.appendClassificationDeclaration(confidentialityCodes);
        sqb.newline();
        sqb.appendClassificationDeclaration(formatCodes);
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" a.sourceObject='" + uuid + "' AND");
        sqb.newline();
        sqb.append(" a.associationType='");
        sqb.append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_eb_assoc_type_has_member));
        sqb.append("' AND");
        sqb.newline();
        sqb.append(" a.targetObject=obj.id ");
        sqb.newline();
        sqb.addCode(confidentialityCodes);
        sqb.addCode(formatCodes);
        return runQuery(sqb);
    }

    /**
     *
     * @param uuid
     * @param formatCodes
     * @param confidentialityCodes
     * @return
     * @throws XdsException
     */
    public OMElement getFolderDocuments(String uuid, SQCodedTerm formatCodes, SQCodedTerm confidentialityCodes)
            throws XdsException {
        return getSubmissionSetDocuments(uuid, formatCodes, confidentialityCodes);
    }

    /**
     *
     * @param sourceOrTargetIDs
     * @param assocTypes
     * @return
     * @throws XdsException
     */
    public OMElement getAssociations(List<String> sourceOrTargetIDs, List<String> assocTypes) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.returnLeafClass);
        //sqb.initQuery();
        sqb.select("a");
        sqb.append("FROM Association a");
        sqb.newline();
        sqb.append("WHERE ");
        sqb.newline();
        sqb.append(" (a.sourceObject IN ");
        sqb.append(sourceOrTargetIDs);
        sqb.append(" OR");
        sqb.newline();
        sqb.append(" a.targetObject IN ");
        sqb.append(sourceOrTargetIDs);
        sqb.append(" )");
        sqb.newline();
        if (assocTypes != null) {
            sqb.append(" AND a.associationType IN ");
            sqb.append(RegistryCodedValueMapper.convertAssocType_ValueToCode(assocTypes));
            sqb.newline();
        }
        return runQuery(sqb);
    }

    /**
     *
     * @param assocType
     * @param froms
     * @param tos
     * @return
     * @throws XdsException
     */
    public OMElement getAssocations(String assocType, List<String> froms, List<String> tos)
            throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.returnLeafClass);
        //sqb.initQuery();
        sqb.select("a");
        sqb.append("FROM Association a");
        sqb.newline();
        sqb.append("WHERE ");
        sqb.newline();
        sqb.append(" a.associationType='" + RegistryCodedValueMapper.convertAssocType_ValueToCode(assocType) + "' AND");
        sqb.newline();
        sqb.append(" a.sourceObject IN");
        sqb.append(froms);
        sqb.append(" AND");
        sqb.newline();
        sqb.append(" a.targetObject IN");
        sqb.append(tos);
        sqb.newline();
        return runQuery(sqb);
    }

    /**
     *
     * @param assocID
     * @return
     * @throws XdsException
     */
    public OMElement getAssociationByUUID(String assocUUID)
            throws XdsException {
        List<String> assocUUIDs = new ArrayList<String>();
        assocUUIDs.add(assocUUID);
        return this.getAssociationByUUID(assocUUIDs);
    }

    /**
     *
     * @param assocIDs
     * @return
     * @throws XdsException
     */
    public OMElement getAssociationByUUID(List<String> assocUUIDs)
            throws XdsException {
        StoredQueryBuilder sqb = StoredQuery.getSQL_AssociationByUUID(assocUUIDs, this.returnLeafClass);
        return this.runQuery(sqb);
    }

    /**
     *
     * @param packageUUIDs
     * @param contentUUIDs
     * @return
     * @throws XdsException
     */
    public OMElement getRegistryPackageAssociations(List<String> packageUUIDs, List<String> contentUUIDs) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.returnLeafClass);
        sqb.select("ass");
        sqb.append("FROM Association ass");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" ass.associationType='");
        sqb.append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_eb_assoc_type_has_member));
        sqb.append("' AND");
        sqb.newline();
        sqb.append(" ass.sourceObject IN (");
        for (int i = 0; i < packageUUIDs.size(); i++) {
            if (i > 0) {
                sqb.append(",");
            }
            sqb.appendQuoted((String) packageUUIDs.get(i));
        }
        sqb.append(") AND");
        sqb.newline();
        sqb.append("ass.targetObject IN (");
        for (int i = 0; i < contentUUIDs.size(); i++) {
            if (i > 0) {
                sqb.append(",");
            }
            sqb.appendQuoted((String) contentUUIDs.get(i));
        }
        sqb.append(")");
        sqb.newline();
        return runQuery(sqb);
    }

    /**
     *
     * @param packageUUIDs
     * @return
     * @throws XdsException
     */
    public OMElement getRegistryPackageAssociations(List<String> packageUUIDs) throws XdsException {
        if (packageUUIDs == null || packageUUIDs.isEmpty()) {
            return null;
        }
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.returnLeafClass);
        sqb.select("ass");
        sqb.append("FROM Association ass");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" ass.associationType='");
        sqb.append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_eb_assoc_type_has_member));
        sqb.append("' AND");
        sqb.newline();
        sqb.append(" ass.sourceObject IN (");
        for (int i = 0; i < packageUUIDs.size(); i++) {
            if (i > 0) {
                sqb.append(",");
            }
            sqb.appendQuoted((String) packageUUIDs.get(i));
        }
        sqb.append(")");
        sqb.newline();
        return runQuery(sqb);
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    public OMElement getSubmissionSetsOfContents(List<String> uuids) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.returnLeafClass);
        sqb.select("rp");
        sqb.append("FROM RegistryPackage rp, Association a, ExternalIdentifier ei");
        sqb.newline();
        sqb.append("WHERE ");
        sqb.newline();
        sqb.append(" a.sourceObject=rp.id AND");
        sqb.newline();
        sqb.append(" a.associationType='");
        sqb.append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_eb_assoc_type_has_member));
        sqb.append("' AND");
        sqb.newline();
        sqb.append(" a.targetObject IN ");
        sqb.append(uuids);
        sqb.append(" AND");
        sqb.newline();
        sqb.append(" ei.registryObject=rp.id AND");
        sqb.newline();
        sqb.append(" ei.identificationScheme='"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSSubmissionSet_patientid_uuid)
                + "'");
        sqb.newline();
        return runQuery(sqb);
    }

    /**
     *
     * @param submissionSetUUID
     * @return
     * @throws XdsException
     */
    public OMElement getSubmissionSetFolders(String submissionSetUUID) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.returnLeafClass);
        sqb.select("fol");
        sqb.append("FROM RegistryPackage fol, Association a");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" a.associationType='");
        sqb.append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_eb_assoc_type_has_member));
        sqb.append("' AND");
        sqb.newline();
        sqb.append(" a.sourceObject='" + submissionSetUUID + "' AND");
        sqb.newline();
        sqb.append(" a.targetObject=fol.id ");
        sqb.newline();
        return runQuery(sqb);
    }

    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    public OMElement getFoldersForDocument(String uuid) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.returnLeafClass);
        sqb.select("fol");
        sqb.append("FROM RegistryPackage fol, ExternalIdentifier uniq, Association a");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append("   a.associationType='");
        sqb.append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_eb_assoc_type_has_member));
        sqb.append("' AND");
        sqb.newline();
        sqb.append(" a.targetObject='" + uuid + "' AND");
        sqb.newline();
        sqb.append(" a.sourceObject=fol.id AND");
        sqb.newline();
        sqb.append(" uniq.registryObject=fol.id AND");
        sqb.newline();
        sqb.append(" uniq.identificationScheme='"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSFolder_uniqueid_uuid)
                + "' ");
        return runQuery(sqb);
    }

    /******************************* StoredQueryBuilder factories ****************************************/
    /**
     * 
     * @param uuids
     * @param returnLeafClass
     * @return
     * @throws XdsException
     */
    public static StoredQueryBuilder getSQL_DocumentByUUID(List<String> uuids, boolean returnLeafClass) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(returnLeafClass);
        sqb.select("eo");
        sqb.append("FROM ExtrinsicObject eo");
        sqb.newline();
        sqb.append(" WHERE");
        sqb.newline();
        sqb.append(" eo.id IN ");
        sqb.append(uuids);
        sqb.newline();
        return sqb;
    }

    /**
     *
     * @param uids
     * @param returnLeafClass
     * @return
     * @throws XdsException
     */
    public static StoredQueryBuilder getSQL_DocumentByUID(List<String> uids, boolean returnLeafClass) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(returnLeafClass);
        sqb.select("eo");
        sqb.append("FROM ExtrinsicObject eo, ExternalIdentifier ei");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" ei.registryObject=eo.id AND");
        sqb.newline();
        sqb.append(" ei.identificationScheme='"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSDocumentEntry_uniqueid_uuid)
                + "' AND");
        sqb.newline();
        sqb.append(" ei.value IN ");
        sqb.append(uids);
        sqb.newline();
        return sqb;
    }

    /**
     * 
     * @param uuids
     * @param identificationScheme
     * @param returnLeafClass
     * @return
     * @throws XdsException
     */
    public static StoredQueryBuilder getSQL_RegistryPackageByUUID(List<String> uuids, String identificationScheme, boolean returnLeafClass)
            throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(returnLeafClass);
        sqb.select("ss");
        sqb.append("FROM RegistryPackage ss, ExternalIdentifier uniq");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" ss.id IN ");
        sqb.append(uuids);
        sqb.append(" AND");
        sqb.newline();
        sqb.append(" uniq.registryObject=ss.id AND");
        sqb.newline();
        sqb.append(" uniq.identificationScheme='"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(identificationScheme)
                + "' ");
        sqb.newline();
        return sqb;
    }

     /**
     *
     * @param uids
     * @param identificationScheme
     * @param returnLeafClass
     * @return
     * @throws XdsException
     */
    public static StoredQueryBuilder getSQL_RegistryPackageByUID(List<String> uids, String identificationScheme, boolean returnLeafClass) throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(returnLeafClass);
        sqb.select("ss");
        sqb.append("FROM RegistryPackage ss, ExternalIdentifier uniq");
        sqb.newline();
        sqb.append("WHERE");
        sqb.newline();
        sqb.append(" uniq.registryObject=ss.id AND");
        sqb.newline();
        sqb.append(" uniq.identificationScheme='"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(identificationScheme)
                + "' AND");
        sqb.newline();
        sqb.append(" uniq.value IN ");
        sqb.append(uids);
        return sqb;
    }

    /**
     *
     * @param assocUUIDs
     * @param returnLeafClass
     * @return
     * @throws XdsException
     */
     public static StoredQueryBuilder getSQL_AssociationByUUID(List<String> assocUUIDs, boolean returnLeafClass)
            throws XdsException {
        StoredQueryBuilder sqb = new StoredQueryBuilder(returnLeafClass);
        sqb.select("ass");
        sqb.append("FROM Association ass");
        sqb.newline();
        sqb.append("WHERE ");
        sqb.newline();
        sqb.append(" ass.id IN ");
        sqb.append(assocUUIDs);
        sqb.newline();
        return sqb;
    }
}
