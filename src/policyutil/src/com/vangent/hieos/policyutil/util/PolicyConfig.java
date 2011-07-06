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
    private List<ClaimConfig> requiredClaimConfigs = new ArrayList<ClaimConfig>();

    public enum IdType {

        SUBJECT_ID, RESOURCE_ID, ENVIRONMENT_ID
    };

    // FUTURE(???):
    //private final static String[] XACML_CODED_IDS = {
    //    "urn:oasis:names:tc:xacml:2.0:subject:role",
    //    "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse"
    //};
    // FUTURE (???):
    //private final static String[] CODED_XML_NAMES = {
    //    "Role",
    //    "PurposeForUse"
    //};
    // FUTURE (???):
    //private final static String[] NHIN_IDS = {
    //    "urn:oasis:names:tc:xspa:1.0:subject:subject-id", // Differs from XSPA.
    //    "urn:oasis:names:tc:xspa:1.0:subject:organization",
    //    "urn:oasis:names:tc:xspa:1.0:subject:organization-id",
    //    "urn:nhin:names:saml:homeCommunityId", // Not in XSPA.
    //    "urn:oasis:names:tc:xacml:2.0:subject:role", // Coded value.
    //    "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse", // Coded value (node is PurposeForUse).
    //    "urn:oasis:names:tc:xacml:2.0:resource:resource-id", // Differs from XSPA.
    //    "urn:oasis:names:tc:xspa:2.0:subject:npi"
    //};
    /**
     *
     */
    private PolicyConfig() {
        // Do not allow.
    }

    /**
     *
     * @return
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
    public List<ClaimConfig> getRequiredClaimConfigs() {
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
        this.parseSubjectAttributes(rootNode);
        this.parseResourceAttributes(rootNode);
        this.parseEnvironmentAttributes(rootNode);
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
    private void parseSubjectAttributes(OMElement rootNode) {
        this.subjectAttributeConfigs = this.parseAttributes(rootNode, "SubjectAttributes");
    }

    /**
     *
     * @param rootNode
     */
    private void parseResourceAttributes(OMElement rootNode) {
        this.resourceAttributeConfigs = this.parseAttributes(rootNode, "ResourceAttributes");
    }

    /**
     * 
     * @param rootNode
     */
    private void parseEnvironmentAttributes(OMElement rootNode) {
        this.environmentAttributeConfigs = this.parseAttributes(rootNode, "EnvironmentAttributes");
    }

    /**
     *
     * @param rootNode
     */
    private void parseRequiredClaims(OMElement rootNode) {
        this.requiredClaimConfigs = this.parseClaims(rootNode, "RequiredClaims");
    }

    /**
     *
     * @param rootNode
     * @param elementName
     * @return
     */
    private List<ClaimConfig> parseClaims(OMElement rootNode, String elementName) {
        OMElement claimsNode = rootNode.getFirstChildWithName(new QName(elementName));
        return this.parseClaims(claimsNode);
    }

    /**
     *
     * @param rootNode
     * @param elementName
     * @return
     */
    private List<AttributeConfig> parseAttributes(OMElement rootNode, String elementName) {
        OMElement attributesNode = rootNode.getFirstChildWithName(new QName(elementName));
        return this.parseAttributes(attributesNode);
    }

    /**
     * 
     * @param rootNode
     * @return
     */
    private List<AttributeConfig> parseAttributes(OMElement rootNode) {
        List<AttributeConfig> attributeConfigs = new ArrayList<AttributeConfig>();
        List<OMElement> attributeNodes = XConfig.parseLevelOneNode(rootNode, "Attribute");
        for (OMElement attributeNode : attributeNodes) {
            String id = attributeNode.getAttributeValue(new QName("id"));
            String type = attributeNode.getAttributeValue(new QName("type"));
            AttributeConfig attributeConfig = new AttributeConfig();
            attributeConfig.setId(id);
            attributeConfig.setType(type);
            attributeConfigs.add(attributeConfig);
        }
        return attributeConfigs;
    }

    /**
     *
     * @param rootNode
     * @return
     */
    private List<ClaimConfig> parseClaims(OMElement rootNode) {
        List<ClaimConfig> claimConfigs = new ArrayList<ClaimConfig>();
        List<OMElement> claimNodes = XConfig.parseLevelOneNode(rootNode, "Claim");
        for (OMElement claimNode : claimNodes) {
            String id = claimNode.getAttributeValue(new QName("id"));
            String type = claimNode.getAttributeValue(new QName("type"));
            ClaimConfig claimConfig = new ClaimConfig();
            claimConfig.setId(id);
            claimConfig.setType(type);
            claimConfigs.add(claimConfig);
        }
        return claimConfigs;
    }

    /**
     *
     * @param id
     * @return
     */
    public IdType getIdType(String id) {
        // FIXME: Rewrite (it is likely a very small list however - problably OK).
        boolean found = this.containsId(this.getSubjectAttributeConfigs(), id);
        if (found) {
            return IdType.SUBJECT_ID;
        }
        found = this.containsId(this.getResourceAttributeConfigs(), id);
        if (found) {
            return IdType.RESOURCE_ID;
        }
        // Default.
        return IdType.ENVIRONMENT_ID;
    }

    /**
     * 
     * @param attributeConfigs
     * @param id
     * @return
     */
    private boolean containsId(List<AttributeConfig> attributeConfigs, String id) {
        for (AttributeConfig attributeConfig : attributeConfigs) {
            if (attributeConfig.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }
}
