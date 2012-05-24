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
package com.vangent.hieos.services.xds.registry.backend;

import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import com.vangent.hieos.xutil.xml.XMLParser;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.registry.RegistryException;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMFactory;
import org.apache.log4j.Logger;

import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;

/**
 *
 * @author Bernie Thuman
 */
public class BackendRegistry {

    private final static Logger log = Logger.getLogger(BackendRegistry.class);
    private final static String REGISTRY_COMMIT_FAILURE = "ebXML EXCEPTION: Failed to commit transaction";
    private final static String REGISTRY_ROLLBACK_FAILURE = "ebXML EXCEPTION: Failed to rollback transaction";
    private static PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
    private final static QName idQName = new QName("id");
    private final static String adhocQueryRequestHeader =
            "<query:AdhocQueryRequest\n"
            + "xmlns=\"urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0\"\n"
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "xmlns:lcm=\"urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0\"\n"
            + "xmlns:rs=\"urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0\"\n"
            + "xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\"\n"
            + "xmlns:query=\"urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0\"\n"
            + "xsi:schemaLocation=\"urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0 http://oasis-open.org/committees/regrep/documents/3.0/schema/query.xsd\">\n";
    private XLogMessage logMessage;
    private Connection connection = null;
    private String reason = "";

    /**
     * 
     * @param logMessage
     */
    public BackendRegistry(XLogMessage logMessage) {
        this.logMessage = logMessage;
    }

    /**
     *
     * @param reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 
     * @param m
     * @return
     * @throws XdsInternalException
     */
    public OMElement submit(Metadata m) throws XdsInternalException {
        OMElement request = m.getV3SubmitObjectsRequest();
        return this.submit(request);
    }

    /**
     *
     * @param registryObjects
     * @return
     * @throws XdsInternalException
     */
    public OMElement submit(List<OMElement> registryObjects) throws XdsInternalException {
        Metadata m = new Metadata();
        OMElement request = m.getV3SubmitObjectsRequest(registryObjects);
        return this.submit(request);
    }

    /**
     *
     * @param objectIds
     * @return
     * @throws XdsInternalException
     */
    public OMElement submitApproveObjectsRequest(List<String> objectIds) throws XdsInternalException {
        OMElement approveObjectsRequest = this.getApproveObjectsRequest(objectIds);
        this.setReason("Approve");
        return this.submit(approveObjectsRequest);
    }

    /**
     *
     * @param objectIds
     * @return
     * @throws XdsInternalException
     */
    public OMElement submitDeprecateObjectsRequest(List<String> objectIds) throws XdsInternalException {
        OMElement deprecateObjectsRequest = this.getDeprecateObjectsRequest(objectIds);
        this.setReason("Deprecate");
        return this.submit(deprecateObjectsRequest);
    }

    /**
     *
     * @param objectIds
     * @return
     * @throws XdsInternalException
     */
    public OMElement submitRemoveObjectsRequest(List<String> objectIds) throws XdsInternalException {
        OMElement removeObjectsRequest = this.getRemoveObjectsRequest(objectIds);
        this.setReason("Remove Objects");
        return this.submit(removeObjectsRequest);
    }

    /**
     * 
     * @param objectId
     * @param status
     * @return
     * @throws XdsException
     */
    public OMElement submitSetStatusOnObjectsRequest(String objectId, String status) throws XdsException {
        List<String> objectIds = new ArrayList<String>();
        objectIds.add(objectId);
        return this.submitSetStatusOnObjectsRequest(objectIds, status);
    }

    /**
     *
     * @param objectIds
     * @param status
     * @return
     * @throws XdsInternalException
     */
    public OMElement submitSetStatusOnObjectsRequest(List<String> objectIds, String status) throws XdsInternalException {
        OMElement setStatusOnObjectsRequest = this.getSetStatusOnObjectsRequest(objectIds, status);
        this.setReason("Set Status");
        return this.submit(setStatusOnObjectsRequest);
    }

    /**
     *
     * @param request
     * @return
     * @throws XdsInternalException
     */
    public OMElement submit(OMElement request) throws XdsInternalException {
        if (logMessage != null) {
            logMessage.addOtherParam("ebXML Request (" + reason + ")", request);
        }
        OMElement result = null;
        try {
            Connection conn = this.getConnection();
            OmarRegistry or = new OmarRegistry(request, conn);
            result = or.process();
            if (logMessage != null) {
                logMessage.addOtherParam("ebXML Response", (result != null) ? result : "null");
            }
            return result;
        } catch (Exception ex) {
            try {
                this.rollback();
            } catch (Exception regEx) {
                log.fatal(REGISTRY_ROLLBACK_FAILURE, regEx);
            }
            throw new XdsInternalException("ebXML EXCEPTION: " + ex.getMessage());
        }
    }

    /**
     *
     * @throws XdsInternalException
     */
    public void commit() throws XdsInternalException {
        if (connection != null) {
            try {
                connection.commit();
                pm.releaseConnection(connection);
                connection = null;
            } catch (Exception ex) {
                log.fatal(REGISTRY_COMMIT_FAILURE, ex);
                this.rollback();
                throw new XdsInternalException(REGISTRY_COMMIT_FAILURE + ": " + ex.getMessage());
            }
        }
    }

    /**
     *
     * @throws XdsInternalException
     */
    public void rollback() throws XdsInternalException {
        try {
            if (connection != null) {
                connection.rollback();
                pm.releaseConnection(connection);
                connection = null;
            }
        } catch (Exception ex) {
            log.fatal(REGISTRY_ROLLBACK_FAILURE, ex);
            throw new XdsInternalException(REGISTRY_ROLLBACK_FAILURE + ": " + ex.getMessage());
        }
    }

    /**
     *
     * @param sql
     * @param leafClass
     * @return
     * @throws MetadataException
     * @throws MetadataValidationException
     * @throws XdsInternalException
     */
    public OMElement runQuery(String sql, boolean leafClass) throws MetadataException, MetadataValidationException, XdsInternalException {
        OMElement result = this.basicQuery(sql, leafClass);
        Metadata m = MetadataParser.parseNonSubmission(result);
        m.fixClassifications();
        return result;
    }

    /**
     * 
     * @param sql
     * @return
     * @throws XdsException
     */
    public ArrayList<String> runQueryForObjectRefs(String sql) throws XdsException {

        // Perform the query (only for references):
        OMElement result = this.runQuery(sql, false /* leafClass */);

        // Now create the array of object references:
        ArrayList<String> ors = new ArrayList<String>();
        if (result == null) // error occured
        {
            return ors;
        }

        // Parse result and gather all object reference ids:
        OMElement sqlQueryResult = MetadataSupport.firstChildWithLocalName(result, "RegistryObjectList");
        if (sqlQueryResult != null) {
            for (OMElement or : MetadataSupport.childrenWithLocalName(sqlQueryResult, "ObjectRef")) {
                String id = or.getAttributeValue(idQName);
                if (id != null && !id.equals("")) {
                    ors.add(id);
                }
            }
        }
        return ors;
    }

    /**
     *
     * @param sql
     * @return
     * @throws MetadataValidationException
     * @throws XMLParserException
     * @throws XdsException
     */
    //public ArrayList<String> objectRefQuery(String sql)
    //        throws MetadataValidationException, XMLParserException, XdsException {
    //    OMElement response = this.basicQuery(sql, true /* leafClass */);
    //    Metadata m = MetadataParser.parseNonSubmission(response);
    //    return m.getObjectIds(m.getObjectRefs());
    //}
    /**
     *
     * @param sql
     * @param leafClass
     * @return
     * @throws XdsInternalException
     */
    public OMElement basicQuery(String sql, boolean leafClass)
            throws XdsInternalException {
        String queryString = BackendRegistry.adhocQueryRequestHeader
                + "<query:ResponseOption returnType=\""
                + ((leafClass) ? "LeafClass" : "ObjectRef")
                + "\" returnComposedObjects=\"true\">\n"
                + "</query:ResponseOption>\n "
                + "<rim:AdhocQuery id=\"tempId\">\n"
                + "<rim:QueryExpression queryLanguage=\"urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92\">\n"
                + //		sql +
                "</rim:QueryExpression>\n"
                + "</rim:AdhocQuery>\n"
                + "</query:AdhocQueryRequest>\n";

        OMElement query = XMLParser.stringToOM(queryString);
        //AMS 04/26/2009 - FIXME - Handle the condition that there might be no children with name AdhocQuery.
        // Or does validation already account for that?? ---- RESEARCH
        OMElement adhocQueryRequest = MetadataSupport.firstChildWithLocalName(query, "AdhocQuery");
        OMElement queryExpression = MetadataSupport.firstChildWithLocalName(adhocQueryRequest, "QueryExpression");
        queryExpression.setText(sql);

        OMElement result = this.submit(query);
        return result;
    }

    /**
     *
     * @param uuids
     * @return
     */
    private OMElement getApproveObjectsRequest(List<String> uuids) {
        OMNamespace lcm = MetadataSupport.ebLcm3;
        OMElement req = MetadataSupport.om_factory.createOMElement("ApproveObjectsRequest", lcm);
        req.addChild(this.getObjectRefList(uuids));
        return req;
    }

    /**
     *
     * @param uuids
     * @return
     */
    private OMElement getDeprecateObjectsRequest(List<String> uuids) {
        OMNamespace lcm = MetadataSupport.ebLcm3;
        OMElement req = MetadataSupport.om_factory.createOMElement("DeprecateObjectsRequest", lcm);
        req.addChild(this.getObjectRefList(uuids));
        return req;

    }

     /**
     *
     * @param uuids
     * @return
     */
    private OMElement getRemoveObjectsRequest(List<String> uuids) {
        OMNamespace lcm = MetadataSupport.ebLcm3;
        OMElement req = MetadataSupport.om_factory.createOMElement("RemoveObjectsRequest", lcm);
        req.addChild(this.getObjectRefList(uuids));
        return req;

    }

    /**
     * 
     * @param uuids
     * @param status
     * @return
     */
    private OMElement getSetStatusOnObjectsRequest(List<String> uuids, String status) {
        OMNamespace rsExt = MetadataSupport.om_factory.createOMNamespace("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0:ext", "rsext");
        OMElement req = MetadataSupport.om_factory.createOMElement("SetStatusOnObjectsRequest", rsExt);
        req.addAttribute("status", status, null);
        req.addChild(this.getObjectRefList(uuids));
        return req;

    }

    /**
     * Creates ebXML v3 <ObjectRefList> structure given a set of uuids.
     *
     * @param uuids Arraylist of uuids.
     * @return <ObjectRefList> OMElement node.
     */
    private OMElement getObjectRefList(List<String> uuids) {
        OMNamespace rimNs = MetadataSupport.ebRIMns3;
        OMFactory fact = MetadataSupport.om_factory;
        OMElement objectRefList = fact.createOMElement("ObjectRefList", rimNs);
        for (Iterator it = uuids.iterator(); it.hasNext();) {
            String uuid = (String) it.next();
            OMAttribute attribute = fact.createOMAttribute("id", null, uuid);
            OMElement objectReference = fact.createOMElement("ObjectRef", rimNs);
            objectReference.addAttribute(attribute);
            objectRefList.addChild(objectReference);
        }
        return objectRefList;
    }

    /**
     *
     * @return
     * @throws RegistryException
     */
    private Connection getConnection() throws RegistryException {
        if (connection == null) {
            connection = pm.getConnection();
        }
        return connection;
    }
}
