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

package com.vangent.hieos.xutil.xconfig;

import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class XConfigTransaction {

    private XConfigTransactionEndpoint transactionEndpoint;
    private boolean secureTransaction;
    private boolean asyncTransaction;
    private String name = "";

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the value of secureTransaction
     *
     * @return the value of secureTransaction
     */
    public boolean isSecureTransaction() {
        return secureTransaction;
    }

    /**
     * Get the value of asyncTransaction
     *
     * @return the value of asyncTransaction
     */
    public boolean isAsyncTransaction() {
        return asyncTransaction;
    }

    /**
     * Get the Endpoint object
     *
     * @return the value of XConfigTransactionEndpoint
     */
    public XConfigTransactionEndpoint getEndpoint() {
        return this.transactionEndpoint;
    }

    /**
     * Get the value of endPointURL
     *
     * @return the value of endPointURL
     */
    public String getEndpointURL() {
        return ((this.transactionEndpoint != null) ? this.transactionEndpoint.getEndpointURL() : "");
    }

    /**
     * This method, populates the XConfigTransaction class by evaluating the input OMElement.
     *
     * @param rootNode, an OMElement, representing the transaction.
     */
    protected void parse(OMElement rootNode) {
        this.name = rootNode.getAttributeValue(new QName("name"));
        this.secureTransaction =
                ("true".equalsIgnoreCase(rootNode.getAttributeValue(new QName("secure")))) ? true : false;
        this.asyncTransaction =
                ("true".equalsIgnoreCase(rootNode.getAttributeValue(new QName("async")))) ? true : false;

        parseEndpoints(rootNode);
    }

    /**
     * 
     * @return a String representing the state of the XConfigTransaction object.
     */
    @Override
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("\n Transaction:");
        sbuf.append("\n   name: " + this.getName());
        sbuf.append("\n   endpointURL: " + this.getEndpointURL());
        sbuf.append("\n   secureTransaction: " + (this.isSecureTransaction() ? "true" : "false"));
        sbuf.append("\n   asyncTransaction: " + (this.isAsyncTransaction() ? "true" : "false"));
        return sbuf.toString();
    }

    /**
     * This method is passed in an OMElement with root corresponding to "Transaction".
     * It determines the transaction's endpoint by evaluating the child of the "Transaction" element.
     *
     * @param rootNode, an OMElement, representing the transaction.
     */
    private void parseEndpoints(OMElement rootNode) {
        List<OMElement> list = XConfig.parseLevelOneNode(rootNode, "Endpoint");
        for (OMElement currentNode : list) {
            XConfigTransactionEndpoint txnEP = new XConfigTransactionEndpoint();
            txnEP.parse(currentNode);
            if ((this.isSecureTransaction() == txnEP.isSecureEndpoint()) &&
                (this.isAsyncTransaction() == txnEP.isAsyncEndpoint()))
            {
                this.transactionEndpoint = txnEP;
                break;
            }
        }
    }
}
