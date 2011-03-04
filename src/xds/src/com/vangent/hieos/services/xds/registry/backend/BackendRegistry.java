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
package com.vangent.hieos.xutil.registry;

import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import com.vangent.hieos.xutil.xml.XMLParser;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMFactory;

/**
 *
 * @author thumbe
 */
public class BackendRegistry {

    //private final static QName object_ref_qname = new QName("ObjectRef");
    private final static QName idQName = new QName("id");
    private final static String adhocQueryRequestHeader =
            "<query:AdhocQueryRequest\n" +
            "xmlns=\"urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0\"\n" +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "xmlns:lcm=\"urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0\"\n" +
            "xmlns:rs=\"urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0\"\n" +
            "xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\"\n" +
            "xmlns:query=\"urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0\"\n" +
            "xsi:schemaLocation=\"urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0 http://oasis-open.org/committees/regrep/documents/3.0/schema/query.xsd\">\n";
    ErrorLogger response;
    XLogMessage logMessage;
    String reason = "";

    /**
     *
     * @param response
     * @param logMessage
     */
    public BackendRegistry(ErrorLogger response, XLogMessage logMessage) {
        this.response = response;
        this.logMessage = logMessage;
    }

    /**
     *
     * @param response
     * @param logMessage
     * @param reason
     */
    public BackendRegistry(ErrorLogger response, XLogMessage logMessage, String reason) {
        this.response = response;
        this.logMessage = logMessage;
        this.reason = reason;
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
     * @param sql
     * @return
     * @throws XMLParserException
     * @throws XdsException
     */
    public ArrayList<String> queryForObjectRefs(String sql) throws XMLParserException, XdsException {

        // Perform the query (only for references):
        OMElement result = query(sql, false /* leafClass */);

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
     * @param leafClass
     * @return
     * @throws XMLParserException
     * @throws MetadataException
     * @throws MetadataValidationException
     * @throws XdsInternalException
     * @throws XdsException
     */
    public OMElement query(String sql, boolean leafClass) throws XMLParserException, MetadataException, MetadataValidationException, XdsInternalException, XdsException {
        OMElement result = this.basicQuery(sql, leafClass);
        Metadata m = MetadataParser.parseNonSubmission(result);
        m.fixClassifications();
        return result;
    }

    /**
     *
     * @param sql
     * @return
     * @throws MetadataValidationException
     * @throws XMLParserException
     * @throws XdsException
     */
    public ArrayList<String> objectRefQuery(String sql)
            throws MetadataValidationException, XMLParserException, XdsException {
        OMElement response = this.basicQuery(sql, true /* leafClass */);
        Metadata m = MetadataParser.parseNonSubmission(response);
        return m.getObjectIds(m.getObjectRefs());
    }

    /**
     *
     * @param sql
     * @param leafClass
     * @return
     * @throws XMLParserException
     * @throws XdsInternalException
     * @throws XdsException
     */
    public OMElement basicQuery(String sql, boolean leafClass)
            throws XMLParserException, XdsInternalException, XdsException {
        String queryString = this.adhocQueryRequestHeader +
                "<query:ResponseOption returnType=\"" +
                ((leafClass) ? "LeafClass" : "ObjectRef") +
                "\" returnComposedObjects=\"true\">\n" +
                "</query:ResponseOption>\n " +
                "<rim:AdhocQuery id=\"tempId\">\n" +
                "<rim:QueryExpression queryLanguage=\"urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92\">\n" +
                //		sql +
                "</rim:QueryExpression>\n" +
                "</rim:AdhocQuery>\n" +
                "</query:AdhocQueryRequest>\n";

        OMElement query = XMLParser.stringToOM(queryString);
        //AMS 04/26/2009 - FIXME - Handle the condition that there might be no children with name AdhocQuery.
        // Or does validation already account for that?? ---- RESEARCH
        OMElement query_request = MetadataSupport.firstChildWithLocalName(query, "AdhocQuery");//.get(0);
        OMElement sql_query = MetadataSupport.firstChildWithLocalName(query_request, "QueryExpression");
        sql_query.setText(sql);

        OMElement result = this.submit(query);
        return result;
    }

    /**
     *
     * @param uuids
     * @return
     */
    public OMElement getApproveObjectsRequest(ArrayList uuids) {
        OMNamespace lcm = MetadataSupport.ebLcm3;
        OMElement req = MetadataSupport.om_factory.createOMElement("ApproveObjectsRequest", lcm);
        req.addChild(makeObjectRefList(uuids));
        return req;
    }

    /**
     *
     * @param uuids
     * @return
     */
    public OMElement getDeprecateObjectsRequest(ArrayList uuids) {
        OMNamespace lcm = MetadataSupport.ebLcm3;
        OMElement req = MetadataSupport.om_factory.createOMElement("DeprecateObjectsRequest", lcm);
        req.addChild(makeObjectRefList(uuids));
        return req;

    }

    /**
     * Creates ebXML v3 <ObjectRefList> structure given a set of uuids.
     *
     * @param uuids Arraylist of uuids.
     * @return <ObjectRefList> OMElement node.
     */
    private OMElement makeObjectRefList(ArrayList uuids) {
        OMNamespace rim = MetadataSupport.ebRIMns3;
        OMFactory fact = MetadataSupport.om_factory;
        OMElement objectRefList = fact.createOMElement("ObjectRefList", rim);
        for (Iterator it = uuids.iterator(); it.hasNext();) {
            String uuid = (String) it.next();
            OMAttribute attribute = fact.createOMAttribute("id", null, uuid);
            OMElement objectReference = fact.createOMElement("ObjectRef", rim);
            objectReference.addAttribute(attribute);
            objectRefList.addChild(objectReference);
        }
        return objectRefList;
    }

    /**
     *
     * @param request
     * @return
     * @throws XdsException
     */
    public OMElement submit(OMElement request) throws XdsException {
        if (logMessage != null) {
            logMessage.addOtherParam("ebXML Request (" + reason + ")", request);
        }
        OMElement result = null;
        OmarRegistry or = new OmarRegistry(request);
        result = or.process();
        if (logMessage != null) {
            logMessage.addOtherParam("ebXML Response", (result != null) ? result : "null");
        }
        return result;
    }

    /*
    void insert_version_info(OMElement parent) {
    if (MetadataSupport.firstChildWithLocalName(parent, "VersionInfo") != null) {
    return;
    }
    OMElement vi = MetadataSupport.om_factory.createOMElement("VersionInfo", MetadataSupport.ebRIMns3);
    vi.addAttribute("versionName", "1.1", null);
    parent.addChild(vi);
    }*/
}
