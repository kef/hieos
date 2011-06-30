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
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class XDSPnRMessage extends AbstractXdsBridgeMessage {

    /** Field description */
    private static final String XDS_URI = "urn:ihe:iti:xds-b:2007";

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
     *
     * @param document
     */
    public void attachDocument(Document document) {

        OMElement rootNode = getMessageNode();

        OMFactory fac = rootNode.getOMFactory();

        // create document messageNode <xdsb:Document id="eo_id_0"></xdsb:Document>
        QName docQName = new QName(XDS_URI, "Document");
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
