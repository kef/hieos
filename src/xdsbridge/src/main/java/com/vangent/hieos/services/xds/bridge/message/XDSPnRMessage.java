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

package com.vangent.hieos.services.xds.bridge.message;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.support.URIConstants;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Vangent
 */
public class XDSPnRMessage extends AbstractXdsBridgeMessage {

    /**
     * Constructs ...
     *
     *
     * @param pnr
     *
     */
    public XDSPnRMessage(OMElement pnr) {

        super(pnr, "ProvideAndRegisterDocumentSetRequest");
    }

    /**
     * Method description
     *
     *
     * @param document
     */
    public void addReplaceAssociation(Document document) {

        /*
         *   <rim:Association associationType="urn:ihe:iti:2007:AssociationType:RPLC"
         *       sourceObject="eo_id_0"
         *       targetObject="__REPLACEEXTRINSICOBJECTID__"
         *       id="__REPLACEASSOCIATIONID__"
         *       objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association">
         *   </rim:Association>
         */

        OMFactory fac = OMAbstractFactory.getOMFactory();
        QName assocQName = new QName(URIConstants.RIM_URI, "Association");
        OMElement result = fac.createOMElement(assocQName);

        result.addAttribute("associationType",
                            "urn:ihe:iti:2007:AssociationType:RPLC", null);
        result.addAttribute("sourceObject", document.getSymbolicId(), null);
        result.addAttribute("targetObject",
                            document.getReplaceExtrinsicObjectId(), null);
        result.addAttribute("id",
                            String.format("assoc_id_%s",
                                          document.getSymbolicId()), null);
        result.addAttribute(
            "objectType",
            "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association",
            null);

        OMElement registryObjectList =
            getMessageNode().getFirstElement().getFirstElement();
        QName classQName = new QName(URIConstants.RIM_URI, "Classification");
        OMElement insertPoint =
            registryObjectList.getFirstChildWithName(classQName);

        insertPoint.insertSiblingBefore(result);
    }

    /**
     * Method description
     *
     *
     *
     * @param document
     */
    public void attachDocument(Document document) {

        OMElement rootNode = getMessageNode();

        OMFactory fac = rootNode.getOMFactory();

        // create document messageNode <xdsb:Document id="eo_id_0"></xdsb:Document>
        QName docQName = new QName(URIConstants.XDS_URI, "Document");
        OMElement docelem = fac.createOMElement(docQName);

        docelem.addAttribute("id", document.getSymbolicId(), null);

        // Set up the DataHandler.
        ByteArrayDataSource ds = new ByteArrayDataSource(document.getContent(),
                                     document.getMimeType());

        DataHandler dataHandler = new DataHandler(ds);

        OMText text = fac.createOMText(dataHandler, true);

        text.setOptimize(true);

        docelem.addChild(text);

        // for some reason, this.messageNode.addChild(docelem)
        // is adding the docelem as the first child, should be last
        // forcing it to be last child
        OMElement firstChild = rootNode.getFirstElement();

        firstChild.insertSiblingAfter(docelem);
    }
}
