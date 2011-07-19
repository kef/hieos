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
package com.vangent.hieos.policyutil.util;

import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.util.AttributeConfig.AttributeClassType;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.iosupport.Io;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfig.ConfigItem;
import com.vangent.hieos.xutil.xml.XMLParser;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PolicyConfig {
    private final static Logger logger = Logger.getLogger(PolicyConfig.class);
    static private PolicyConfig _instance = null;

    // Configuration.
    private List<String> policyFiles = new ArrayList<String>();
    private Map<String, AttributeConfig> attributeConfigs = new HashMap<String, AttributeConfig>();

    /**
     *
     */
    private PolicyConfig() {
        // Do not allow.
    }

    /**
     *
     * @return
     * @throws PolicyException
     */
    static public synchronized PolicyConfig getInstance() throws PolicyException {
        if (_instance == null) {
            _instance = new PolicyConfig();
            _instance.loadConfiguration();
        }
        return _instance;
    }

    /**
     *
     * @return
     */
    public List<String> getPolicyFiles() {
        return policyFiles;
    }

    /**
     *
     */
    private void loadConfiguration() throws PolicyException {
        String policyDir = PolicyConfig.getConfigDir();
        String configLocation = policyDir + "/policyConfig.xml";
        // Load the "policyConfig.xml" file from the above directory.

        String configXML = null;
        if (configLocation != null) {
            try {
                logger.info("Loading \"policyConfig.xml\" from: " + configLocation);
                // Get the configuration file from the file system.
                configXML = Io.getStringFromInputStream(new FileInputStream(new File(configLocation)));
            } catch (Exception e) {
                throw new PolicyException(
                        "PolicyConfig: Could not load configuration from " + configLocation + " " + e.getMessage());
            }
        } else {
            throw new PolicyException(
                    "PolicyConfig: Unable to get location of \"policyConfig.xml\" file");
        }

        // Parse the XML file.
        OMElement configXMLRoot;
        try {
            configXMLRoot = XMLParser.stringToOM(configXML);
        } catch (XMLParserException ex) {
            throw new PolicyException(ex.getMessage());
        }
        if (configXMLRoot == null) {
            throw new PolicyException(
                    "PolicyConfig: Could not parse configuration from " + configLocation);
        }
        buildInternalStructure(configXMLRoot);
    }

    /**
     * 
     * @return
     */
    private static String getConfigDir() {
        return XConfig.getConfigLocation(ConfigItem.POLICY_DIR);
    }

    /**
     *
     * @param rootNode
     */
    private void buildInternalStructure(OMElement rootNode) throws PolicyException {
        this.parsePolicyFiles(rootNode);
        this.parseAttributeConfigs(rootNode);
    }

    /**
     *
     * @param rootNode
     */
    private void parsePolicyFiles(OMElement rootNode) {
        OMElement policyFilesNode = rootNode.getFirstChildWithName(new QName("PolicyFiles"));
        List<OMElement> policyFileNodes = XConfig.parseLevelOneNode(policyFilesNode, "PolicyFile");
        for (OMElement policyFileNode : policyFileNodes) {
            String policyFileName = policyFileNode.getAttributeValue(new QName("name"));
            String policyFullPathName = PolicyConfig.getConfigDir() + "/" + policyFileName;
            this.policyFiles.add(policyFullPathName);
        }
    }

    /**
     * 
     * @param rootNode
     * @return
     */
    private void parseAttributeConfigs(OMElement rootNode) throws PolicyException {
        OMElement attributesNode = rootNode.getFirstChildWithName(new QName("Attributes"));
        List<OMElement> attributeNodes = XConfig.parseLevelOneNode(attributesNode, "Attribute");
        attributeConfigs = new HashMap<String, AttributeConfig>();

        for (OMElement attributeNode : attributeNodes) {
            // Pull out attributes.
            String classType = attributeNode.getAttributeValue(new QName("classtype"));
            String name = attributeNode.getAttributeValue(new QName("name"));
            String id = attributeNode.getAttributeValue(new QName("id"));
            String type = attributeNode.getAttributeValue(new QName("type"));

            AttributeConfig attributeConfig = new AttributeConfig();
            attributeConfig.setId(id);
            attributeConfig.setName(name);

            // Set type.
            if (type.equalsIgnoreCase("string")) {
                attributeConfig.setType(AttributeConfig.AttributeType.STRING);
            } else if (type.equalsIgnoreCase("any")) {
                attributeConfig.setType(AttributeConfig.AttributeType.ANY);
            } else {
                throw new PolicyException("Policy configuration type '" + type + "' is unknown for attribute '" + id + "'");
            }

            // Set classType.
            if (classType.equalsIgnoreCase("subject")) {
                attributeConfig.setClassType(AttributeClassType.SUBJECT_ID);
            } else if (classType.equalsIgnoreCase("resource")) {
                attributeConfig.setClassType(AttributeClassType.RESOURCE_ID);
            } else if (classType.equalsIgnoreCase("environment")) {
                attributeConfig.setClassType(AttributeClassType.ENVIRONMENT_ID);
            } else {
                throw new PolicyException("Policy configuration classtype '" + classType + "' is unknown for attribute '" + id + "'");
            }
            attributeConfigs.put(id, attributeConfig);
        }
    }

    /**
     * 
     * @param id
     * @return
     * @throws PolicyException
     */
    public AttributeConfig getAttributeConfig(String id) throws PolicyException {
        AttributeConfig attributeConfig = attributeConfigs.get(id);
        if (attributeConfig == null) {
            throw new PolicyException("Policy configuration id '" + id + "' is unknown");
        } else {
            return attributeConfig;
        }
    }
}
