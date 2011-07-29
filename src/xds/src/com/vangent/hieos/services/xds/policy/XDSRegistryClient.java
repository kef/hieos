/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xds.policy;

import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.soap.WebServiceClient;
import com.vangent.hieos.xutil.template.TemplateUtil;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

// FIXME: MOVE
/**
 *
 * @author Bernie Thuman
 */
public class XDSRegistryClient extends WebServiceClient {
    // Example "GetDocuments" query (with multiple entries):
    //<query:AdhocQueryRequest xmlns:query="urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0" xmlns="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" xmlns:rs="urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    //  <query:ResponseOption returnComposedObjects="true" returnType="LeafClass"></query:ResponseOption> <!-- GetDocuments Stored Query -->
    //  <AdhocQuery id="urn:uuid:5c4f972b-d56b-40ac-a5fc-c8ca9b40b9d4">
    //      <Slot name="$XDSDocumentEntryUniqueId">
    //          <ValueList>
    //              <Value>('129.6.58.92.2142454', '129.6.58.92.2142455')</Value>
    //          </ValueList>
    //      </Slot>
    //  </AdhocQuery>
    //</query:AdhocQueryRequest>

    private final static String adhocQueryRequestTemplate =
            "<query:AdhocQueryRequest xmlns:query=\"urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0\" xmlns=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\" xmlns:rs=\"urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            + "<query:ResponseOption returnComposedObjects=\"true\" returnType=\"LeafClass\"></query:ResponseOption>"
            + "<AdhocQuery id=\"urn:uuid:5c4f972b-d56b-40ac-a5fc-c8ca9b40b9d4\">"
            + "    <Slot name=\"$XDSDocumentEntryUniqueId\">"
            + "       <ValueList><Value>({DOCUMENT_UNIQUE_IDS})</Value></ValueList>"
            + "    </Slot>"
            + "</AdhocQuery>"
            + "</query:AdhocQueryRequest>";
    private final static String STORED_QUERY_SOAP_ACTION = "urn:ihe:iti:2007:RegistryStoredQuery";

    /**
     *
     * @param config
     */
    public XDSRegistryClient(XConfigActor config) {
        super(config);
    }

    /**
     *
     * @param documentResponseList
     * @param returnIdsOnly
     * @return
     * @throws AxisFault
     */
    public List<DocumentMetadata> getRegistryObjects(List<DocumentResponse> documentResponseList, boolean returnIdsOnly) throws AxisFault {
        // Build list of document ids suitable for query.

        // FIXME: Cleanup/refactor code.

        StringBuilder sb = new StringBuilder();
        Iterator<DocumentResponse> it = documentResponseList.iterator();
        while (it.hasNext()) {
            DocumentResponse documentResponse = it.next();
            sb.append("'");
            sb.append(documentResponse.getDocumentId());
            sb.append("'");
            if (it.hasNext()) {
                sb.append(",");
            }
        }
        // Place these document ids into the template and build request.
        Map<String, String> templateMap = new HashMap<String, String>();
        templateMap.put("DOCUMENT_UNIQUE_IDS", sb.toString());
        OMElement requestNode = TemplateUtil.getOMElementFromTemplate(adhocQueryRequestTemplate, templateMap);

        // Get transaction configuration.
        XConfigTransaction txn = this.getConfig().getTransaction("RegistryStoredQuery");

        // Make soap request.
        OMElement responseNode = this.send(requestNode,
                STORED_QUERY_SOAP_ACTION,
                txn.getEndpointURL(), txn.isSOAP12Endpoint());

        // Return list of extrinsic objects (ids only).
        try {
            // FIXME: Error handling ...
            List<OMElement> extrinsicObjects = XPathHelper.selectNodes(responseNode, "./ns:RegistryObjectList/ns:ExtrinsicObject", DocumentMetadataBuilder.EBXML_RIM_NS);

            // Only return identifiers in this case.
            DocumentMetadataBuilder documentMetadataBuilder = new DocumentMetadataBuilder();
            List<DocumentMetadata> documentMetadataIdentifierList;
            if (returnIdsOnly) {
                // Just get identifiers from the list.
                documentMetadataIdentifierList =
                        documentMetadataBuilder.buildDocumentIdentifiersList(
                        new RegistryObjectElementList(extrinsicObjects));
            } else {
                // Get full meta-data set.
                documentMetadataIdentifierList =
                        documentMetadataBuilder.buildDocumentMetadataList(
                        new RegistryObjectElementList(extrinsicObjects));
            }
            return documentMetadataIdentifierList;
        } catch (XPathHelperException ex) {
            throw new AxisFault("Can not parse registry response", ex);
        }
    }

    /**
     * 
     * @param requestNode
     * @param soapAction
     * @param endpointURL
     * @param soap12
     * @return
     * @throws AxisFault
     */
    private OMElement send(OMElement requestNode, String soapAction, String endpointURL, boolean soap12) throws AxisFault {
        try {
            // Make SOAP call.
            Soap soap = new Soap();
            OMElement responseNode;
            try {
                responseNode = soap.soapCall(
                        requestNode,
                        endpointURL,
                        false /* MTOM */,
                        soap12 /* Addressing - Only if SOAP 1.2 */,
                        soap12 /* SOAP 1.2 */,
                        soapAction, null);
            } catch (Exception ex) {
                throw new AxisFault(ex.getMessage());
            }
            if (responseNode == null) {
                throw new AxisFault("No SOAP Response!");
            }

            return responseNode;

        } catch (Exception ex) {
            throw new AxisFault(ex.getMessage());
        }
    }
}
