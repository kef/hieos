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
    // Attribute class types.
    private static final String ATTRIBUTE_CLASS_TYPE_SUBJECT = "subject";
    private static final String ATTRIBUTE_CLASS_TYPE_RESOURCE = "resource";
    private static final String ATTRIBUTE_CLASS_TYPE_ENVIRONMENT = "environment";
    // Attribute types.
    private static final String ATTRIBUTE_TYPE_STRING = "string";
    private static final String ATTRIBUTE_TYPE_ANY = "any";
    private static final String ATTRIBUTE_TYPE_HL7V3_CODED_VALUE = "hl7v3_coded_value";
    // Attribute formats.
    private static final String ATTRIBUTE_FORMAT_XON_ID_ONLY = "XON_id_only";
    private static final String ATTRIBUTE_FORMAT_XCN_ID_ONLY = "XCN_id_only";
    private static final String ATTRIBUTE_FORMAT_CNE_CODE_ONLY = "CNE_code_only";
    private static final String ATTRIBUTE_FORMAT_CX = "CX";
    private static final String ATTRIBUTE_FORMAT_STRING = "string";

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
     * @throws PolicyException
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
     * @throws PolicyException
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
     * @throws PolicyException
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
            String format = attributeNode.getAttributeValue(new QName("format"));

            AttributeConfig attributeConfig = new AttributeConfig();
            attributeConfig.setId(id);
            attributeConfig.setName(name);

            // Set the type.
            this.setAttributeConfigType(attributeConfig, type);

            // Set format.
            if (format == null) {
                // If not specified (set based upon type).
                this.setDefaultAttributeConfigFormat(attributeConfig);
            } else {
                this.setAttributeConfigFormat(attributeConfig, format);
            }

            // Set classType.
            this.setAttributeConfigClassType(attributeConfig, classType);

            // Store the attribute configuration for later use.
            attributeConfigs.put(id, attributeConfig);
        }
    }

    /**
     * 
     * @param attributeConfig
     * @param type
     * @throws PolicyException
     */
    private void setAttributeConfigType(AttributeConfig attributeConfig, String type) throws PolicyException {
        if (type.equalsIgnoreCase(ATTRIBUTE_TYPE_STRING)) {
            attributeConfig.setType(AttributeConfig.AttributeType.STRING);
        } else if (type.equalsIgnoreCase(ATTRIBUTE_TYPE_HL7V3_CODED_VALUE)) {
            attributeConfig.setType(AttributeConfig.AttributeType.HL7V3_CODED_VALUE);
        } else if (type.equalsIgnoreCase(ATTRIBUTE_TYPE_ANY)) {
            attributeConfig.setType(AttributeConfig.AttributeType.ANY);
        } else {
            throw new PolicyException(
                    "Policy configuration type '"
                    + type + "' is unknown for attribute '"
                    + attributeConfig.getId() + "'");
        }
    }

    /**
     * 
     * @param attributeConfig
     */
    private void setDefaultAttributeConfigFormat(AttributeConfig attributeConfig) {
        switch (attributeConfig.getType()) {
            case ANY:
                attributeConfig.setFormat(AttributeConfig.AttributeFormat.ANY);
                break;
            case HL7V3_CODED_VALUE:
                attributeConfig.setFormat(AttributeConfig.AttributeFormat.CNE_CODE_ONLY);
                break;
            case STRING:
            // Fall through.
            default:
                attributeConfig.setFormat(AttributeConfig.AttributeFormat.STRING);
                break;
        }
    }

    /**
     *
     * @param attributeConfig
     */
    private void setAttributeConfigFormat(AttributeConfig attributeConfig, String format) throws PolicyException {
        if (format.equalsIgnoreCase(ATTRIBUTE_FORMAT_XON_ID_ONLY)) {
            attributeConfig.setFormat(AttributeConfig.AttributeFormat.XON_ID_ONLY);
        } else if (format.equalsIgnoreCase(ATTRIBUTE_FORMAT_XCN_ID_ONLY)) {
            attributeConfig.setFormat(AttributeConfig.AttributeFormat.XCN_ID_ONLY);
        } else if (format.equalsIgnoreCase(ATTRIBUTE_FORMAT_CNE_CODE_ONLY)) {
            attributeConfig.setFormat(AttributeConfig.AttributeFormat.CNE_CODE_ONLY);
        } else if (format.equalsIgnoreCase(ATTRIBUTE_FORMAT_CX)) {
            attributeConfig.setFormat(AttributeConfig.AttributeFormat.CX);
        } else if (format.equalsIgnoreCase(ATTRIBUTE_FORMAT_STRING)) {
            // Just in case.
            attributeConfig.setFormat(AttributeConfig.AttributeFormat.STRING);
        } else {
            throw new PolicyException(
                    "Policy configuration format '" + format
                    + "' is unknown for attribute '" + attributeConfig.getId() + "'");
        }
    }

    /**
     *
     * @param attributeConfig
     * @param classType
     */
    private void setAttributeConfigClassType(AttributeConfig attributeConfig, String classType) throws PolicyException {
        if (classType.equalsIgnoreCase(ATTRIBUTE_CLASS_TYPE_SUBJECT)) {
            attributeConfig.setClassType(AttributeClassType.SUBJECT_ID);
        } else if (classType.equalsIgnoreCase(ATTRIBUTE_CLASS_TYPE_RESOURCE)) {
            attributeConfig.setClassType(AttributeClassType.RESOURCE_ID);
        } else if (classType.equalsIgnoreCase(ATTRIBUTE_CLASS_TYPE_ENVIRONMENT)) {
            attributeConfig.setClassType(AttributeClassType.ENVIRONMENT_ID);
        } else {
            throw new PolicyException("Policy configuration classtype '" + classType
                    + "' is unknown for attribute '" + attributeConfig.getId() + "'");
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
