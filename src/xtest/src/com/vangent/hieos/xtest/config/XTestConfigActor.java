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

import com.vangent.hieos.xutil.xconfig.XConfigProperties;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.axiom.om.OMElement;

/**
 * XTestConfigActor specifies an abstract representation of various actors
 * such as Registry, Repository and Gateways.
 *
 * @author Anand Sastry
 */
abstract public class XTestConfigActor {

    // Constants
    private static final String TRANSACTION_ELEMENT = "Transaction";
    private static final String UNIQUE_ID_ELEMENT = "UniqueId";
    private static final String NAME_ATTRIBUTE = "name";

    // State
    private String name = "";
    private String uniqueId = "";
    private ArrayList<XTestConfigTransaction> transactions = new ArrayList<XTestConfigTransaction>();
    private XConfigProperties properties = new XConfigProperties();

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////
    /**
     * This method returns a property value given a property key.
     * @param propKey
     * @return a String representing a property value
     */
    public String getProperty(String propKey) {
        return (String) properties.getProperty(propKey);
    }

    /**
     * This method returns a property value, in boolean form, given a property key.
     * @param propKey
     * @return a boolean value
     */
    public boolean getPropertyAsBoolean(String propKey) {
        return properties.getPropertyAsBoolean(propKey);
    }

    /**
     * Get the value of transactions
     *
     * @return the value of transactions
     */
    public ArrayList<XTestConfigTransaction> getTransactions() {
        return transactions;
    }

    /**
     * This method returns a XTestConfigTransaction object, given a transaction name.
     * @param txnName
     * @returns XTestConfigTransaction
     */
    public XTestConfigTransaction getTransaction(String txnName) {
        XTestConfigTransaction txn = null;
        for (Iterator it = transactions.iterator(); (txn == null) && it.hasNext();) {

            XTestConfigTransaction current = (XTestConfigTransaction) it.next();

            // Did we find a transaction that matches?
            if (current.getName().equals(txnName)) {
                txn = current;
                break;
            }
        }
        return txn;
    }

    /**
     * Get the value of uniqueId
     *
     * @return the value of uniqueId
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }


    ///////////////////////////////////////////////////////////////////////////
    // PROTECTED METHODS
    /////////////////////////////////////////////////////////////////////////
    /**
     * This method is passed in an actor OMElement - Could be "Registry", "Repository", "InitiatingGateway"
     * or "RespondingGateway".
     * It extracts the actor's UniqueId element and name attribute and defers to the private parseTransactions method.
     * @param rootNode
     */
    protected void parse(OMElement rootNode) {
        // Do local parsing and then let the subclass do the rest.
        OMElement node;
        this.name = rootNode.getAttributeValue(new QName(NAME_ATTRIBUTE));
        node = rootNode.getFirstChildWithName(new QName(UNIQUE_ID_ELEMENT));
        this.uniqueId = node.getText();

        parseTransactions(rootNode);
        properties.parse(rootNode);
    }


    ///////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    //////////////////////////////////////////////////////////////////////////

    /**
     * This method is passed in an actor OMElement - Could be "Registry", "Repository", "InitiatingGateway"
     * or "RespondingGateway".
     * It determines the actor's transactions by evaluating the "Transaction" children elements.
     *
     * @param rootNode an OMElement.
     */
    private void parseTransactions(OMElement rootNode) {
        ArrayList<OMElement> list = XTestConfig.parseLevelOneNode(rootNode, TRANSACTION_ELEMENT);
        for (OMElement currentNode : list) {
            XTestConfigTransaction txn = new XTestConfigTransaction();
            txn.parse(currentNode);
            this.transactions.add(txn);
        }
    }

}
