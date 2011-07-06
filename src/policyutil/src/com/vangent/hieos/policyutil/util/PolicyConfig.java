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
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.iosupport.Io;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfig.ConfigItem;
import com.vangent.hieos.xutil.xml.XMLParser;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PolicyConfig {
    // TBD: Deal with coded value types in AttributeConfig (and throughout code).

    private final static Logger logger = Logger.getLogger(PolicyConfig.class);
    static private PolicyConfig _instance = null;
    // Configuration.
    private List<String> policyFiles = new ArrayList<String>();
    private List<AttributeConfig> subjectAttributeConfigs = new ArrayList<AttributeConfig>();
    private List<AttributeConfig> resourceAttributeConfigs = new ArrayList<AttributeConfig>();
    private List<AttributeConfig> environmentAttributeConfigs = new ArrayList<AttributeConfig>();
    private List<AttributeConfig> requiredClaimConfigs = new ArrayList<AttributeConfig>();

    /**
     *
     */
    public enum AttributeIdType {

        /**
         *
         */
        SUBJECT_ID,
        /**
         * 
         */
        RESOURCE_ID,
        /**
         *
         */
        ENVIRONMENT_ID
    };

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
     * @return
     */
    public List<AttributeConfig> getSubjectAttributeConfigs() {
        return subjectAttributeConfigs;
    }

    /**
     *
     * @return
     */
    public List<AttributeConfig> getResourceAttributeConfigs() {
        return resourceAttributeConfigs;
    }

    /**
     *
     * @return
     */
    public List<AttributeConfig> getEnvironmentAttributeConfigs() {
        return environmentAttributeConfigs;
    }

    /**
     *
     * @return
     */
    public List<AttributeConfig> getRequiredClaimConfigs() {
        return requiredClaimConfigs;
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
    public static String getConfigDir() {
        return XConfig.getConfigLocation(ConfigItem.POLICY_DIR);
    }

    /**
     *
     * @param rootNode
     */
    private void buildInternalStructure(OMElement rootNode) {
        this.parsePolicyFiles(rootNode);
        this.parseSubjectAttributeConfigs(rootNode);
        this.parseResourceAttributeConfigs(rootNode);
        this.parseEnvironmentAttributeConfigs(rootNode);
        this.parseRequiredClaims(rootNode);
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
     */
    private void parseSubjectAttributeConfigs(OMElement rootNode) {
        this.subjectAttributeConfigs = this.parseAttributeConfigs(rootNode, "SubjectAttributes");
    }

    /**
     *
     * @param rootNode
     */
    private void parseResourceAttributeConfigs(OMElement rootNode) {
        this.resourceAttributeConfigs = this.parseAttributeConfigs(rootNode, "ResourceAttributes");
    }

    /**
     * 
     * @param rootNode
     */
    private void parseEnvironmentAttributeConfigs(OMElement rootNode) {
        this.environmentAttributeConfigs = this.parseAttributeConfigs(rootNode, "EnvironmentAttributes");
    }

    /**
     *
     * @param rootNode
     */
    private void parseRequiredClaims(OMElement rootNode) {
        this.requiredClaimConfigs = this.parseClaimConfigs(rootNode, "RequiredClaims");
    }

    /**
     *
     * @param rootNode
     * @param elementName
     * @return
     */
    private List<AttributeConfig> parseClaimConfigs(OMElement rootNode, String elementName) {
        OMElement claimsNode = rootNode.getFirstChildWithName(new QName(elementName));
        return this.parseClaimConfigs(claimsNode);
    }

    /**
     *
     * @param rootNode
     * @param elementName
     * @return
     */
    private List<AttributeConfig> parseAttributeConfigs(OMElement rootNode, String elementName) {
        OMElement attributesNode = rootNode.getFirstChildWithName(new QName(elementName));
        return this.parseAttributeConfigs(attributesNode);
    }

    /**
     * 
     * @param rootNode
     * @return
     */
    private List<AttributeConfig> parseAttributeConfigs(OMElement rootNode) {
        List<OMElement> attributeNodes = XConfig.parseLevelOneNode(rootNode, "Attribute");
        return this.parseAttributeConfigs(attributeNodes);
    }

    /**
     *
     * @param rootNode
     * @return
     */
    private List<AttributeConfig> parseClaimConfigs(OMElement rootNode) {
        List<OMElement> claimNodes = XConfig.parseLevelOneNode(rootNode, "Claim");
        return this.parseAttributeConfigs(claimNodes);
    }

    /**
     * 
     * @param attributeNodes
     * @return
     */
    private List<AttributeConfig> parseAttributeConfigs(List<OMElement> attributeNodes) {
        List<AttributeConfig> configs = new ArrayList<AttributeConfig>();
        for (OMElement attributeNode : attributeNodes) {
            String id = attributeNode.getAttributeValue(new QName("id"));
            String type = attributeNode.getAttributeValue(new QName("type"));
            String name = attributeNode.getAttributeValue(new QName("name"));
            AttributeConfig config = new AttributeConfig();
            config.setId(id);
            config.setType(type);
            config.setName(name);
            configs.add(config);
        }
        return configs;
    }

    /**
     *
     * @param id
     * @return
     */
    public AttributeIdType getAttributeIdType(String id) {
        // FIXME: Rewrite (it is likely a very small list however - problably OK).
        boolean found = this.containsId(this.subjectAttributeConfigs, id);
        if (found) {
            return AttributeIdType.SUBJECT_ID;
        }
        found = this.containsId(this.resourceAttributeConfigs, id);
        if (found) {
            return AttributeIdType.RESOURCE_ID;
        }
        // Default.
        return AttributeIdType.ENVIRONMENT_ID;
    }

    /**
     *
     * @param uri
     * @return
     */
    public AttributeConfig getClaimAttributeConfig(String uri) {
        return this.getAttributeConfig(this.requiredClaimConfigs, uri);
    }

    /**
     * 
     * @param id
     * @return
     */
    public AttributeConfig getSubjectAttributeConfig(String id) {
        return this.getAttributeConfig(this.subjectAttributeConfigs, id);
    }

    /**
     *
     * @param id
     * @return
     */
    public AttributeConfig getResourceAttributeConfig(String id) {
        return this.getAttributeConfig(this.resourceAttributeConfigs, id);
    }

    /**
     *
     * @param id
     * @return
     */
    public AttributeConfig getEnvironmentAttributeConfig(String id) {
        return this.getAttributeConfig(this.environmentAttributeConfigs, id);
    }

    /**
     *
     * @param configs
     * @param id
     * @return
     */
    private AttributeConfig getAttributeConfig(List<AttributeConfig> configs, String id) {
        for (AttributeConfig config : configs) {
            if (config.getId().equalsIgnoreCase(id)) {
                return config;
            }
        }
        return null;
    }

    /**
     * 
     * @param configs
     * @param id
     * @return
     */
    private boolean containsId(List<AttributeConfig> configs, String id) {
        for (AttributeConfig config : configs) {
            if (config.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }
}
