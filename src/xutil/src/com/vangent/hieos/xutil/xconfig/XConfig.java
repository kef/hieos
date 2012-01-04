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

/*
 * XConfig.java
 *
 * Created on January 14, 2009
 *
 */
package com.vangent.hieos.xutil.xconfig;

import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.xml.XMLParser;
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.iosupport.Io;

// Third-party.
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import org.apache.axiom.om.OMElement;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Maintains system configuration parameters for IHE XDS.b and XCA profiles.  Acts as a Singleton.
 *
 * @author Bernie Thuman
 *
 */
public class XConfig {

    public enum ConfigItem {

        CONFIG_DIR, XCONFIG_FILE, SCHEMA_DIR, CODES_FILE, XDSBRIDGE_DIR, POLICY_DIR, EMPI_DIR
    };
    // HIEOS environment variables (would like to rename but remaining backward compatible).
    public final static String ENV_HIEOS_CONFIG_DIR = "HIEOSxConfigDir";
    public final static String ENV_HIEOS_XCONFIG_FILE = "HIEOSxConfigFile";
    public final static String ENV_HIEOS_SCHEMA_DIR = "HIEOSxSchemaDir";
    public final static String ENV_HIEOS_XDSBRIDGE_DIR = "HIEOSxXDSBridgeDir";
    public final static String ENV_HIEOS_POLICY_DIR = "HIEOSxPolicyDir";
    public final static String ENV_HIEOS_EMPI_DIR = "HIEOSxEMPIDir";
    public final static String ENV_HIEOS_CODES_FILE = "HIEOSxCodesFile";
    // HIEOS system properties (would like to rename but remaining backward compatible).
    public final static String SYSPROP_HIEOS_CONFIG_DIR = "com.vangent.hieos.configdir";
    public final static String SYSPROP_HIEOS_XCONFIG_FILE = "com.vangent.hieos.xconfig";
    public final static String SYSPROP_HIEOS_SCHEMA_DIR = "com.vangent.hieos.schemadir";
    public final static String SYSPROP_HIEOS_XDSBRIDGE_DIR = "com.vangent.hieos.xdsbridgedir";
    public final static String SYSPROP_HIEOS_POLICY_DIR = "com.vangent.hieos.policydir";
    public final static String SYSPROP_HIEOS_EMPI_DIR = "com.vangent.hieos.empidir";
    public final static String SYSPROP_HIEOS_CODES_FILE = "com.vangent.hieos.codesfile";
    private final static Logger logger = Logger.getLogger(XConfig.class);
    static private XConfig _instance = null;  // Singleton instance.
    static private String _configLocation = null;  // Location of xconfig.xml file
    // Internal data structure starts here.
    private List<XConfigObject> objects = new ArrayList<XConfigObject>();
    private HashMap<String, XConfigObject> objectByNameMap = new HashMap<String, XConfigObject>();
    private HashMap<String, XConfigObject> objectByIdMap = new HashMap<String, XConfigObject>();
    private List<XConfigObject> assigningAuthorities = new ArrayList<XConfigObject>();
    // Kept as strings versus enum to allow extension without XConfig code modification.
    static final public String HOME_COMMUNITY_TYPE = "HomeCommunityType";
    static final public String XDSB_DOCUMENT_REGISTRY_TYPE = "DocumentRegistryType";
    static final public String XDSB_DOCUMENT_REPOSITORY_TYPE = "DocumentRepositoryType";
    static final public String XCA_INITIATING_GATEWAY_TYPE = "InitiatingGatewayType";
    static final public String XCA_RESPONDING_GATEWAY_TYPE = "RespondingGatewayType";
    static final public String XUA_PROPERTIES_TYPE = "XUAPropertiesType";
    static final public String ASSIGNING_AUTHORITY_TYPE = "AssigningAuthorityType";
    static final public String PDS_TYPE = "PDSType";  // Patient Demographics Service
    static final public String PolicyDecisionPoint_TYPE = "PolicyDecisionPointType";
    static final public String PolicyInformationPoint_TYPE = "PolicyInformationPointType";
    static final public String STS_TYPE = "SecureTokenServiceType";
    static final public String XDSBRIDGE_TYPE = "XDSBridgeType";
    static final public String PIX_MANAGER_TYPE = "PIXManagerType";
    static final public String XDR_DOCUMENT_RECIPIENT_TYPE = "DocumentRecipientType";

    /**
     *
     * @param configItem
     * @return
     */
    static public String getConfigLocation(ConfigItem configItem) {
        String configLocation = null;
        switch (configItem) {
            case CONFIG_DIR:
                configLocation = getConfigDir();
                break;
            case XCONFIG_FILE:
                configLocation = getConfigLocation(configItem, XConfig.SYSPROP_HIEOS_XCONFIG_FILE, XConfig.ENV_HIEOS_XCONFIG_FILE);
                break;
            case SCHEMA_DIR:
                configLocation = getConfigLocation(configItem, XConfig.SYSPROP_HIEOS_SCHEMA_DIR, XConfig.ENV_HIEOS_SCHEMA_DIR);
                break;
            case CODES_FILE:
                configLocation = getConfigLocation(configItem, XConfig.SYSPROP_HIEOS_CODES_FILE, XConfig.ENV_HIEOS_CODES_FILE);
                break;
            case XDSBRIDGE_DIR:
                configLocation = getConfigLocation(configItem, XConfig.SYSPROP_HIEOS_XDSBRIDGE_DIR, XConfig.ENV_HIEOS_XDSBRIDGE_DIR);
                break;
            case POLICY_DIR:
                configLocation = getConfigLocation(configItem, XConfig.SYSPROP_HIEOS_POLICY_DIR, XConfig.ENV_HIEOS_POLICY_DIR);
                break;
            case EMPI_DIR:
                configLocation = getConfigLocation(configItem, XConfig.SYSPROP_HIEOS_EMPI_DIR, XConfig.ENV_HIEOS_EMPI_DIR);
                break;
        }
        return configLocation;
    }

    /**
     *
     * @return
     */
    static public String getConfigDir() {
        // First look at system property.
        String configDir = System.getProperty(XConfig.SYSPROP_HIEOS_CONFIG_DIR);
        if (configDir == null) {
            // Look in environment variable next.
            configDir = System.getenv(XConfig.ENV_HIEOS_CONFIG_DIR);
        }
        return configDir;
    }

    /**
     * 
     * @param sysPropName
     * @param envName
     * @return
     */
    static private String getConfigLocation(ConfigItem configItem, String sysPropName, String envName) {
        // First look at system property.
        String configLocation = System.getProperty(sysPropName);
        if (configLocation == null) {
            // Look in environment variable next.
            configLocation = System.getenv(envName);
        }
        if (configLocation == null) {
            String configDir = XConfig.getConfigDir();
            switch (configItem) {
                case XCONFIG_FILE:
                    configLocation = configDir + "/xconfig.xml";
                    break;
                case SCHEMA_DIR:
                    configLocation = configDir + "/schema";
                    break;
                case CODES_FILE:
                    configLocation = configDir + "/codes/codes.xml";
                    break;
                case XDSBRIDGE_DIR:
                    configLocation = configDir + "/xdsbridge";
                    break;
                case POLICY_DIR:
                    configLocation = configDir + "/policy";
                    break;
                case EMPI_DIR:
                    configLocation = configDir + "/empi";
                    break;
            }
        }
        return configLocation;
    }

    /**
     * 
     * @param configLocation
     */
    static public synchronized void setConfigLocation(String configLocation) {
        _configLocation = configLocation;
    }

    /**
     * Returns Singleton instance of XConfig.
     *
     * @return Singleton instance of XConfig
     * @throws XConfigException
     */
    static public synchronized XConfig getInstance() throws XConfigException {
        if (_instance == null) {
            _instance = new XConfig();
        }
        return _instance;
    }

    /**
     * Private constructor responsible for loading configuration file into memory.
     */
    private XConfig() throws XConfigException {
        loadConfiguration();
    }

    /**
     * Returns the global configuration for the Home Community.
     *
     * @return XConfigObject
     */
    public XConfigObject getHomeCommunityConfig() {
        String key = this.getKey("home", XConfig.HOME_COMMUNITY_TYPE);
        return this.objectByNameMap.get(key);
    }

    /**
     * Returns an XConfigActor (RespondingGatewayType) based on a homeCommunityId.
     *
     * @param homeCommunityId
     * @return An instance of XConfigActor or null (if not found).
     */
    public XConfigActor getRespondingGatewayConfigForHomeCommunityId(String homeCommunityId) {
        return this.getXConfigActorById(homeCommunityId, XConfig.XCA_RESPONDING_GATEWAY_TYPE);
    }

    /**
     * Return an XConfigActor (RespondingGatewayType) given a gateway name.
     *
     * @param gatewayName Name of responding gateway.
     * @return XConfigActor
     */
    protected XConfigActor getRespondingGatewayConfigByName(String gatewayName) {
        return this.getXConfigActorByName(gatewayName, XConfig.XCA_RESPONDING_GATEWAY_TYPE);
    }

    /**
     * Return an XConfigActor (RepositoryType) given a repository id.
     *
     * @param repositoryId
     * @return XConfigActor
     */
    public XConfigActor getRepositoryConfigById(String repositoryId) {
        return this.getXConfigActorById(repositoryId, XConfig.XDSB_DOCUMENT_REPOSITORY_TYPE);
    }

    /**
     * Returns an XConfigObject for a given "name" and "type".
     *
     * @param name Name of the object.
     * @param type Type of the object.
     * @return XConfigObject
     */
    public XConfigObject getXConfigObjectByName(String name, String type) {
        XConfigObject configObject = null;
        String key = this.getKey(name, type);
        if (this.objectByNameMap.containsKey(key)) {
            configObject = this.objectByNameMap.get(key);
        }
        return configObject;
    }

    /**
     * Returns an XConfigObject for a given "id" and "type".
     * 
     * @param id Identifier for the object.
     * @param type Type of the object.
     * @return XConfigObject
     */
    public XConfigObject getXConfigObjectById(String id, String type) {
        XConfigObject configObject = null;
        String key = this.getKey(id, type);
        if (this.objectByIdMap.containsKey(key)) {
            configObject = this.objectByIdMap.get(key);
        }
        return configObject;
    }

    /**
     * Returns all XConfigObject instances of the specific type.
     *
     * @param type The type of XConfigObject to locate.
     * @return List<XConfigObject>
     */
    public List<XConfigObject> getXConfigObjectsOfType(String type) {
        List<XConfigObject> configObjects = new ArrayList<XConfigObject>();
        for (XConfigObject object : objects) {
            if (object.getType().equalsIgnoreCase(type)) {
                configObjects.add(object);
            }
        }
        return configObjects;
    }

    /**
     * Returns an XConfigActor for a give "name" and "type".
     *
     * @param name Name of the Actor.
     * @param type Type of the Actor.
     * @return XConfigActor.
     */
    public XConfigActor getXConfigActorByName(String name, String type) {
        return (XConfigActor) this.getXConfigObjectByName(name, type);
    }

    /**
     * Returns an XConfigActor for a give "id" and "type".
     *
     * @param id Identifier of the Actor.
     * @param type Type of the Actor.
     * @return XConfigActor.
     */
    public XConfigActor getXConfigActorById(String id, String type) {
        return (XConfigActor) this.getXConfigObjectById(id, type);
    }

    /**
     * Returns an XConfigActor (RepositoryType) for a given repository "name".
     *
     * @param repositoryName
     * @return XConfigActor
     */
    public XConfigActor getRepositoryConfigByName(String repositoryName) {
        return this.getXConfigActorByName(repositoryName, XConfig.XDSB_DOCUMENT_REPOSITORY_TYPE);
    }

    /**
     * Returns an XConfigActor (RegistryType) for a given registry "name".
     *
     * @param registryName
     * @return XConfigActor
     */
    public XConfigActor getRegistryConfigByName(String registryName) {
        return this.getXConfigActorByName(registryName, XConfig.XDSB_DOCUMENT_REGISTRY_TYPE);
    }

    /**
     * Returns list of available assigning authority configurations.
     *
     * @return List<XConfigObject> List of assigning authority configurations.
     */
    public List<XConfigObject> getAssigningAuthorityConfigs() {
        return this.assigningAuthorities;
    }

    /**
     * Returns an XConfigObject (AssigningAuthorityType) for a given "unique id".
     *
     * @param uniqueId
     * @return XConfigObject
     */
    public XConfigObject getAssigningAuthorityConfigById(String uniqueId) {
        return this.getXConfigObjectById(uniqueId, XConfig.ASSIGNING_AUTHORITY_TYPE);
    }

    /**
     * Returns list of XConfigActors (of RespondingGatewayType) for a given assigning authority.
     *
     * @param uniqueId
     * @return List<XConfigActor>
     */
    public List<XConfigActor> getRespondingGatewayConfigsForAssigningAuthorityId(String uniqueId) {
        XConfigObject aa = this.getAssigningAuthorityConfigById(uniqueId);
        List<XConfigActor> gateways = new ArrayList<XConfigActor>();
        if (aa != null) {
            List<XConfigObject> configObjects = aa.getXConfigObjectsWithType(XConfig.XCA_RESPONDING_GATEWAY_TYPE);
            for (XConfigObject configObject : configObjects) {
                gateways.add((XConfigActor) configObject);
            }
        }
        return gateways;
    }

    /**
     * Returns XConfigActor (RegistryType) for a given assigning authority.
     *
     * @param uniqueId
     * @return XConfigActor
     */
    public XConfigActor getRegistryConfigForAssigningAuthorityId(String uniqueId) {
        XConfigObject aa = this.getAssigningAuthorityConfigById(uniqueId);
        XConfigActor registry = null;
        if (aa != null) {
            List<XConfigObject> configObjects = aa.getXConfigObjectsWithType(XConfig.XDSB_DOCUMENT_REGISTRY_TYPE);
            if (configObjects.size() > 0) {
                registry = (XConfigActor) configObjects.get(0);  // Should only have one.
            }
        }
        return registry;
    }

    /**
     * Return property value given a property key (scoped to home community).
     *
     * @param propKey Property key.
     * @return String Property value.
     */
    public String getHomeCommunityConfigProperty(String propKey) {
        XConfigObject homeCommunityConfig = this.getHomeCommunityConfig();
        return homeCommunityConfig.getProperty(propKey);
    }

    /**
     * Return boolean property value given a property key (scoped to home community).
     *
     * @param propKey Property key.
     * @return String Property value.
     */
    public boolean getHomeCommunityConfigPropertyAsBoolean(String propKey) {
        XConfigObject homeCommunityConfig = this.getHomeCommunityConfig();
        return homeCommunityConfig.getPropertyAsBoolean(propKey);
    }

    /**
     * Return boolean property value given a property key (scoped to home community).
     *
     * @param propKey Property key.
     * @param defaultValue Returned as value if "propKey" does not exist.
     * @return String Property value.
     */
    public boolean getHomeCommunityConfigPropertyAsBoolean(String propKey, boolean defaultValue) {
        XConfigObject homeCommunityConfig = this.getHomeCommunityConfig();
        return homeCommunityConfig.getPropertyAsBoolean(propKey, defaultValue);
    }

    /**
     * Return "long" property value given a property key (scoped to home community).
     *
     * @param propKey Property key.
     * @return long Property value.
     */
    public long getHomeCommunityConfigPropertyAsLong(String propKey) {
        String longVal = this.getHomeCommunityConfigProperty(propKey);
        return (new Long(longVal)).longValue();
    }

    /**
     * Return true if property "key" exists in home community configuration.
     *
     * @param propKey Property key.
     * @return boolean True if exists.  False, otherwise.
     */
    public boolean containsHomeCommunityConfigProperty(String propKey) {
        XConfigObject homeCommunityConfig = this.getHomeCommunityConfig();
        return homeCommunityConfig.containsProperty(propKey);
    }

    /**
     * Retrieves XML configuration file, parses and places into an easily accessible data structure.
     */
    private void loadConfiguration() throws XConfigException {
        String configLocation = _configLocation;  // May be set.
        if (configLocation == null) {
            configLocation = XConfig.getConfigLocation(ConfigItem.XCONFIG_FILE);
        }
        String configXML = null;
        if (configLocation != null) {
            try {
                logger.info("Loading XConfig from: " + configLocation);
                // Get the configuration file from the file system.
                configXML = Io.getStringFromInputStream(new FileInputStream(new File(configLocation)));
            } catch (Exception e) {
                throw new XConfigException(
                        "XConfig: Could not load configuration from " + configLocation + " " + e.getMessage());
            }
        } else {
            throw new XConfigException(
                    "XConfig: Unable to get location of xconfig file");
        }

        // Parse the XML file.
        OMElement configXMLRoot;
        try {
            configXMLRoot = XMLParser.stringToOM(configXML);
        } catch (XMLParserException ex) {
            throw new XConfigException(ex.getMessage());
        }
        if (configXMLRoot == null) {
            throw new XConfigException(
                    "XConfig: Could not parse configuration from " + configLocation);
        }
        buildInternalStructure(configXMLRoot);
    }

    /**
     * Builds internal data structures for quick access.
     *
     * @param rootNode Holds root node of parsed configuration file.
     */
    private void buildInternalStructure(OMElement rootNode) {
        parseObjects(rootNode);
        parseActors(rootNode);
        resolveObjectReferences();
    }

    /**
     * Parses all "Object" XML nodes.
     *
     * @param rootNode Starting point.
     */
    private void parseObjects(OMElement rootNode) {
        List<OMElement> nodes = XConfig.parseLevelOneNode(rootNode, "Object");
        for (OMElement currentNode : nodes) {
            XConfigObject configObject = new XConfigObject();
            configObject.parse(currentNode, this);
            this.addConfigObjectToMaps(configObject);
        }
    }

    /**
     * Parses all "Actor" XML nodes.
     *
     * @param rootNode Starting point.
     */
    private void parseActors(OMElement rootNode) {
        List<OMElement> nodes = XConfig.parseLevelOneNode(rootNode, "Actor");
        for (OMElement currentNode : nodes) {
            XConfigActor configObject = new XConfigActor();
            configObject.parse(currentNode, this);
            this.addConfigObjectToMaps(configObject);
        }
    }

    /**
     * Add XConfigObject to internal data structures.
     *
     * @param configObject XConfigObject
     */
    private void addConfigObjectToMaps(XConfigObject configObject) {
        if (configObject.getType().equals(XConfig.ASSIGNING_AUTHORITY_TYPE)) {
            this.assigningAuthorities.add(configObject);
        }
        this.objectByNameMap.put(this.getNameKey(configObject), configObject);
        if (configObject.getUniqueId() != null) {
            this.objectByIdMap.put(this.getIdKey(configObject), configObject);
        }
        this.objects.add(configObject);
    }

    /**
     * Formulate a "name based" key to use for lookups.
     *
     * @param configObject
     * @return String Formatted key.
     */
    private String getNameKey(XConfigObject configObject) {
        return configObject.getName() + ":" + configObject.getType();
    }

    /**
     * Formulate a key to use for lookups.
     *
     * @param name
     * @param key
     * @return String Formatted key.
     */
    private String getKey(String name, String type) {
        return name + ":" + type;
    }

    /**
     * Formulate a "name based" key to use for lookups.
     *
     * @param configObjectRef
     * @return String Formatted key.
     */
    private String getNameKey(XConfigObjectRef configObjectRef) {
        return configObjectRef.getRefName() + ":" + configObjectRef.getRefType();
    }

    /**
     * Formulate an "id based" key to use for lookups.
     *
     * @param configObject
     * @return String Formatted key.
     */
    private String getIdKey(XConfigObject configObject) {
        return configObject.getUniqueId() + ":" + configObject.getType();
    }

    /**
     * Go through list of objects and resolve references to each other.
     */
    private void resolveObjectReferences() {
        for (XConfigObject configObject : this.objects) {
            for (XConfigObjectRef objRef : configObject.getObjectRefs()) {
                // See if already resolved.
                if (objRef.getXConfigObject() == null) {

                    // Find reference (by refname).
                    String refKey = this.getNameKey(objRef);
                    if (this.objectByNameMap.containsKey(refKey) == true) {
                        XConfigObject referencedObject = objectByNameMap.get(refKey);
                        objRef.setXConfigObject(referencedObject);
                    }
                }
            }
        }
    }

    /**
     * Helper method to find all AXIOM nodes given a "root node" and "local name".
     *
     * @param rootNode  Starting point.
     * @param localName Local name to find.
     * @return List<OMElement>
     */
    public static List<OMElement> parseLevelOneNode(OMElement rootNode, String localName) {
        ArrayList<OMElement> al = new ArrayList<OMElement>();
        for (Iterator it = rootNode.getChildElements(); it.hasNext();) {
            OMElement child = (OMElement) it.next();
            if (child.getLocalName().equals(localName)) {
                al.add(child);
            }
        }
        return al;
    }
    /**
     * Just a test driver to exercise XConfig operations.
     *
     * @param args the command line arguments
     */
    /*
    public static void main(String[] args) {
    try {
    XConfig xconf = XConfig.getInstance();
    System.out.println("------------------------");
    System.out.println("Local Initiating Gateway = " + xconf.getHomeCommunity().getInitiatingGateway());
    System.out.println("------------------------");
    System.out.println("Local Responding Gateway = " + xconf.getHomeCommunity().getRespondingGateway());
    System.out.println("------------------------");
    System.out.println("Gateway = " + xconf.getGateway("urn:oid:1.19.6.24.109.42.1.3"));
    System.out.println("TXN = " + xconf.getGateway("urn:oid:1.19.6.24.109.42.1.3").getTransaction("CrossGatewayQuery"));
    System.out.println("------------------------");
    System.out.println("Success!");
    } catch (Exception e) {
    System.out.println(e.toString());
    }
    }*/
}
