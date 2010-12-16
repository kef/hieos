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
import com.vangent.hieos.xutil.http.HttpClient;
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

    private final static Logger logger = Logger.getLogger(XConfig.class);
    // Location of XDS.b / XCA configuration file (looks in environment variable first.
    static private String _configURL = "http://localhost:8080/xref/config/xconfig.xml";
    static private XConfig _instance = null;  // Singleton instance.
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
    static final public String PDS_TYPE = "PDSType";
    static final public String PIX_MANAGER_TYPE = "PIXManagerType";
    static final public String XDR_DOCUMENT_RECIPIENT_TYPE = "DocumentRecipientType";

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

        // First see if a system property is set.
        String configLocation = System.getProperty("com.vangent.hieos.xconfig");
        if (configLocation == null) {
            // Look in environment variable next.
            configLocation = System.getenv("HIEOSxConfigFile");
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
            configLocation = _configURL;
            logger.info("Loading XConfig from: " + configLocation);
            // Get the configuration file from web server.
            try {
                configXML = HttpClient.httpGet(configLocation);
            } catch (Exception e) {
                throw new XConfigException(
                        "XConfig: Could not load configuration from " + configLocation + " " + e.getMessage());
            }
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
     * Return property value for XUA configuration.
     *
     * @param propKey Property key.
     * @return String Property value.
     */
    public String getXUAConfigProperty(String propKey) {
        XConfigObject configObject = this.getXUAConfigProperties();
        return configObject.getProperty(propKey);
    }

    /**
     * Return boolean property value for XUA configuration.
     *
     * @param propKey Property key.
     * @return String Property value.
     */
    public boolean getXUAConfigPropertyAsBoolean(String propKey) {
        XConfigObject configObject = this.getXUAConfigProperties();
        return configObject.getPropertyAsBoolean(propKey);
    }

    /**
     * Return "XUAProperties" object.
     *
     * @return XConfigObject
     */
    public XConfigObject getXUAConfigProperties() {
        return this.getXConfigObjectByName("XUAProperties", XConfig.XUA_PROPERTIES_TYPE);
    }

    /**
     * Helper method to find all AXIOM nodes given a "root node" and "local name".
     *
     * @param rootNode  Starting point.
     * @param localName Local name to find.
     * @return List<OMElement>
     */
    protected static List<OMElement> parseLevelOneNode(OMElement rootNode, String localName) {
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
