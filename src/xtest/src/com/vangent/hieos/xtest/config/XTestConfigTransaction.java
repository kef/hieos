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

package com.vangent.hieos.xtest.config;

//import java.lang.StringBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;

/**
 * XTestConfigTransaction encapsulates a transaction as configured
 * in xtestconfig.xml.
 *
 * The following XML fragment is an example of a typical configuration, that shows a transaction
 * and its Endpoint.
 * <Transaction name="RegisterTransaction">
 *    <Endpoint secure="false"
 *           async="false">
 *           http://localhost:8080/axis2/services/xdsregistryb
 *    </Endpoint>
 * </Transaction>
 * 
 * @author Anand Sastry
 */
public class XTestConfigTransaction {

    // Constants
    private final static String ENDPOINT_ELEMENT = "Endpoint";
    private final static String NAME_ATTRIBUTE = "name";

    // State
    private ArrayList<XTestConfigTransactionEndpoint> transactionEndpoints = new ArrayList<XTestConfigTransactionEndpoint>();
    private String name = "";


    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////
    /**
     * Returns the transaction's name
     *
     * @return a String value.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a list of endpoints for this transaction.
     *
     * @return ArrayList<XConfigTransactionEndpoint>
     */
    public ArrayList<XTestConfigTransactionEndpoint> getEndpoints() {
        return this.transactionEndpoints;
    }

    /**
     * Returns an endpoint with specified secure transport and aysnchronous criteria.
     *
     * @param isSecure
     * @param isAsync
     * @return XTestConfigTransactionEndpoint that satisfies the specified criteria.
     */
    public XTestConfigTransactionEndpoint getEndpoint(boolean isSecure, boolean isAsync) {
        XTestConfigTransactionEndpoint txnEP = null;
        for (Iterator it = transactionEndpoints.iterator(); (txnEP == null) && it.hasNext();) {

            XTestConfigTransactionEndpoint current = (XTestConfigTransactionEndpoint) it.next();

            // Did we find a transaction endpoint that matches?
            if (current.isAsyncEndpoint() == isAsync &&
                current.isSecureEndpoint() == isSecure) {
                txnEP = current;
                break;
            }
        }
        return txnEP;
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
        sbuf.append("\n   endpointURLs: " + this.transactionEndpoints.toString());

        return sbuf.toString();
    }

    ///////////////////////////////////////////////////////////////////////////
    // PROTECTED METHODS
    //////////////////////////////////////////////////////////////////////////
    /**
     * This method is passed in a "Transaction" OMElement.
     * It extracts the transaction name and delegates to the parseEndpoints private method.
     *
     * @param rootNode, an OMElement, representing the transaction.
     */
    protected void parse(OMElement rootNode) {
        this.name = rootNode.getAttributeValue(new QName(NAME_ATTRIBUTE));
        parseEndpoints(rootNode);
    }

    ///////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    //////////////////////////////////////////////////////////////////////////
    /**
     * This method is passed in a "Transaction" OMElement.
     * It determines the transaction's endpoints by evaluating the "Endpoint" children elements.
     *
     * @param rootNode, an OMElement, representing the transaction.
     */
    private void parseEndpoints(OMElement rootNode) {
        ArrayList<OMElement> list = XTestConfig.parseLevelOneNode(rootNode, ENDPOINT_ELEMENT);
        for (OMElement currentNode : list) {
            XTestConfigTransactionEndpoint txnEP = new XTestConfigTransactionEndpoint();
            txnEP.parse(currentNode);
            this.transactionEndpoints.add(txnEP);
        }
    }
}
