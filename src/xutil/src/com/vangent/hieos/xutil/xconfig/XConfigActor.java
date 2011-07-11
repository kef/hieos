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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 * Holds configuration for a given IHE actor (e.g. Registry, Repository).
 *
 * @author Bernie Thuman
 */
public class XConfigActor extends XConfigObject {

    private List<XConfigTransaction> transactions = new ArrayList<XConfigTransaction>();
    private boolean xuaEnabled = false;
    private List<String> xuaEnabledSOAPActions = new ArrayList<String>();

    /**
     * Get the value of transactions
     *
     * @return the value of transactions
     */
    public List<XConfigTransaction> getTransactions() {
        return transactions;
    }

    /**
     * Returns an XConfigTransaction with a given name.
     *
     * @param txnName Transaction name.
     * @return XConfigTransaction.
     */
    public XConfigTransaction getTransaction(String txnName) {
        XConfigTransaction txn = null;
        for (Iterator it = transactions.iterator(); (txn == null) && it.hasNext();) {

            XConfigTransaction current = (XConfigTransaction) it.next();

            // Did we find a transaction that matches?
            if (current.getName().equals(txnName)) {
                txn = current;
                break;
            }
        }
        return txn;
    }

    /**
     *
     * @return
     */
    public boolean isXUAEnabled() {
        return this.xuaEnabled;
    }

    /**
     *
     * @param soapAction
     * @return
     */
    public boolean isSOAPActionXUAEnabled(String soapAction) {
        if (xuaEnabledSOAPActions.isEmpty()) {
            // Always constrain if the SOAP action list is empty.
            return true;
        }
        if (xuaEnabledSOAPActions.contains("all")) {
            // Constrain all SOAP actions.
            return true;
        }
        return xuaEnabledSOAPActions.contains(soapAction.toLowerCase());
    }

    /**
     * Fills in the instance from a given AXIOM node.
     *
     * @param rootNode Starting point.
     */
    @Override
    protected void parse(OMElement rootNode, XConfig xconf) {
        super.parse(rootNode, xconf);

        // Now deal with specialized parts.
        parseTransactions(rootNode);

        // Parse XUA properties.
        setXUAProperties();
    }

    /**
     *
     */
    private void setXUAProperties() {
        // Not really having to parse as XUA are simply properties (already parsed).

        // Set XUA Enabled flag.
        String xuaEnabledText = this.getProperty("XUAEnabled");
        if (xuaEnabledText == null) {
            xuaEnabled = false;  // Default.
        } else {
            xuaEnabled = this.getPropertyAsBoolean("XUAEnabled");
        }

        // Parse XUAEnabledSOAPActions
        if (xuaEnabled == true) {
            this.parseList("XUAEnabledSOAPActions", this.xuaEnabledSOAPActions);
        }
    }

    /**
     *
     * @param propKey
     * @param targetList
     */
    private void parseList(String propKey, List targetList) {
        // Load IP addresses to control.
        String listUnParsed = this.getProperty(propKey);
        String[] listParsed = listUnParsed.split(";");
        for (int i = 0; i < listParsed.length; i++) {
            if (listParsed[i].length() != 0) {
                targetList.add(listParsed[i].toLowerCase());
            }
        }
    }

    /**
     *
     * @param rootNode
     */
    private void parseTransactions(OMElement rootNode) {
        List<OMElement> list = XConfig.parseLevelOneNode(rootNode, "Transaction");
        for (OMElement currentNode : list) {
            XConfigTransaction txn = new XConfigTransaction();
            txn.parse(currentNode);
            this.transactions.add(txn);
        }
    }
}
