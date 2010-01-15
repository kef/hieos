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
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class XConfigXUAProperties {

    private XConfigProperties properties = new XConfigProperties();
    ArrayList soapActionsList = null;
    ArrayList constrainedIPList = null;

    /**
     *
     * @param propKey
     * @return
     */
    public String getProperty(String propKey) {
        return (String) properties.getProperty(propKey);
    }

    /**
     *
     * @param propKey
     * @return
     */
    public boolean getPropertyAsBoolean(String propKey) {
        return properties.getPropertyAsBoolean(propKey);
    }

    /**
     *
     * @param soapAction
     * @return
     */
    public boolean containsSOAPAction(String soapAction) {
        return this.soapActionsList.contains(soapAction.toLowerCase());
    }

    /**
     *
     * @param IPAddress
     * @return
     */
    public boolean IPAddressIsConstrained(String IPAddress) {
        // Check to see if all IP addresses should be constrained.
        if (this.constrainedIPList.size() == 0) {
            // Always constrain if the IP list is empty.
            return true;
        }
        if (this.constrainedIPList.contains("all")) {
            // Constrain all IP addresses.
            return true;
        }
        // Only constrain if the IP address is on our list.
        return this.constrainedIPList.contains(IPAddress.toLowerCase());
    }

    /**
     *
     * @param rootNode
     */
    protected void parse(OMElement rootNode) {
        properties.parse(rootNode);
        // Load SOAP actions to control.
        this.soapActionsList = new ArrayList();
        this.parseList("SOAPActions", this.soapActionsList);
        // Load IP addresses to control.
        this.constrainedIPList = new ArrayList();
        this.parseList("ConstrainedIPAddresses", this.constrainedIPList);
    }

    /**
     *
     * @param propKey
     * @param targetList
     */
    private void parseList(String propKey, ArrayList targetList) {
        // Load IP addresses to control.
        String listUnParsed = this.getProperty(propKey);
        String[] listParsed = listUnParsed.split(";");
        for (int i = 0; i < listParsed.length; i++) {
            if (listParsed[i].length() != 0) {
                targetList.add(listParsed[i].toLowerCase());
            }
        }
    }
}
