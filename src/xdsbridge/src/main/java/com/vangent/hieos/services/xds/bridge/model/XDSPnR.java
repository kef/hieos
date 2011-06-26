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

package com.vangent.hieos.services.xds.bridge.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
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
public class XDSPnR {

    /** Field description */
    private static final String XDS_URI = "urn:ihe:iti:xds-b:2007";

    /** Field description */
    private final OMElement node;

    /**
     * Constructs ...
     *
     *
     * @param pnr
     *
     */
    public XDSPnR(OMElement pnr) {

        super();

        this.node = pnr;
    }

    /**
     * Method description
     *
     *
     *
     * @param document
     */
    public void attachDocument(Document document) {

        OMFactory fac = this.node.getOMFactory();

        // create document node <xdsb:Document id="eo_id_0"></xdsb:Document>
        QName docQName = new QName(XDS_URI, "Document");
        OMElement docelem = fac.createOMElement(docQName);

        docelem.addAttribute("id", document.getSymbolicId(), null);

        // TODO make sure the message goes to XDS as mtom
        // Set up the DataHandler.
        ByteArrayDataSource ds = new ByteArrayDataSource();

        ds.setBytes(document.getContent());
        ds.setName(document.getSymbolicId());
        ds.setContentType(document.getMimeType());

        DataHandler dataHandler = new DataHandler(ds);

        OMText text = fac.createOMText(dataHandler, true);

        text.setOptimize(true);

        docelem.addChild(text);

        // for some reason, this.node.addChild(docelem)
        // is adding the docelem as the first child, should be last
        // forcing it to be last child
        OMElement firstChild = this.node.getFirstElement();

        firstChild.insertSiblingAfter(docelem);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public OMElement getNode() {
        return node;
    }

    /**
     * Class description
     *
     *
     * @version        v1.0, 2011-06-15
     * @author         Jim Horner
     */
    public class ByteArrayDataSource implements javax.activation.DataSource {

        /** Field description */
        private byte[] bytes;

        /** Field description */
        private String contentType;

        /** Field description */
        private String name;

        /**
         * Method description
         *
         *
         * @return
         */
        public byte[] getBytes() {
            return bytes;
        }

        /**
         * Method description
         *
         *
         * @return
         */
        @Override
        public String getContentType() {
            return contentType;
        }

        /**
         * Method description
         *
         *
         * @return
         */
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        /**
         * Method description
         *
         *
         * @return
         */
        @Override
        public String getName() {
            return name;
        }

        /**
         * Method description
         *
         *
         * @return
         */
        @Override
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException();
        }

        /**
         * Method description
         *
         *
         * @param bytes
         */
        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        /**
         * Method description
         *
         *
         * @param contentType
         */
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        /**
         * Method description
         *
         *
         * @param name
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}
