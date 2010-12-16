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
package com.vangent.hieos.xutil.xua.utils;

import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XUAConfig {

    private final static Logger logger = Logger.getLogger(XUAConfig.class);
    static XUAConfig _instance = null;
    XConfigObject configObject = null;
    List<String> soapActionsList = new ArrayList<String>();
    List<String> constrainedIPList = new ArrayList<String>();

    /**
     * 
     * @return
     */
    static public synchronized XUAConfig getInstance() {
        if (_instance == null) {
            _instance = new XUAConfig();
        }
        return _instance;
    }

    /**
     *
     * @param config
     */
    private XUAConfig() {
        try {
            XConfig xconf = XConfig.getInstance();
            this.configObject = xconf.getXUAConfigProperties();
            init();
        } catch (XdsInternalException ex) {
            logger.fatal("UNABLE TO GET XConfig (for XUA): ", ex);
        }
    }

    /**
     * @param propKey
     * @return
     */
    public String getProperty(String propKey) {
        return configObject.getProperty(propKey);
    }

    /**
     *
     * @param propKey
     * @return
     */
    public boolean getPropertyAsBoolean(String propKey) {
        return configObject.getPropertyAsBoolean(propKey);
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
     */
    private void init() {
        // Load SOAP actions to control.
        this.parseList("SOAPActions", this.soapActionsList);
        // Load IP addresses to control.
        this.parseList("ConstrainedIPAddresses", this.constrainedIPList);
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
}
