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

import com.vangent.hieos.xutil.xml.Util;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.iosupport.Io;

import com.vangent.hieos.xutil.xconfig.XConfigProperties;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import org.apache.axiom.om.OMElement;
import java.io.File;
import java.io.FileInputStream;
import org.apache.log4j.Logger;

/**
 * Maintains configuration information for the xtest driver. Acts as a Singleton. This class basically
 * encapsulates a collection of XTestConfigSites.
 *
 * @author Anand Sastry
 *
 */
public class XTestConfig {
    private final static Logger logger = Logger.getLogger(XTestConfig.class);

    // Constants
    private static final String DEFAULT_SITE = "DefaultSite";// Constants
    private static final String SITE_ELEMENT = "Site";
    private static final String CONFIG_FILE_NAME = "xtestconfig.xml";

    // state
    private static XTestConfig _instance = null;  // Singleton instance.
    private HashMap<String, XTestConfigSite> sites = new HashMap<String, XTestConfigSite>();// Key = name, Value = <XTestConfigSite>
    private XConfigProperties properties = new XConfigProperties();


    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////////////////////////////////////////////
    /**
     * Returns Singleton instance of XTestConfig.
     *
     * @return Singleton instance of XTestConfig
     * @throws XdsInternalException
     */
    public static synchronized XTestConfig getInstance() throws XdsInternalException {
        if (_instance == null) {
            _instance = new XTestConfig();
        }
        return _instance;
    }

    /**
     * Returns an instance of XTestConfigSite based on a siteName.
     *
     * @param siteName
     * @return An instance of XTestConfigSite or null (if not found).
     */
    public XTestConfigSite getSiteByName(String siteName) {
        return sites.get(siteName);
    }

    /**
     * Gets the value of the Default Site.
     *
     * @return a String value.
     */
    public String getDefaultSite() {
        return (String) properties.getProperty(DEFAULT_SITE);
    }

    /**
     * Gets the value of the PIDAllocateEndpoint. Accessing this url results in the generation of
     * a patient id for use by the registry.
     *
     * @return a String value.
     */
    public String getPIDAllocateEndpoint(String siteName) {
        String pidAllocateEndpoint = "";

        XTestConfigSite site = getSiteByName(siteName);
        if (site != null) {
            pidAllocateEndpoint = site.getPIDAllocateEndpoint();
        }
        return pidAllocateEndpoint;
    }

    /**
     * Gets the value of the Default Registry.
     *
     * @return a String value.
     */
    public String getDefaultRegistry(String siteName) {
        String defaultRegistry = "";

        XTestConfigSite site = getSiteByName(siteName);
        if (site != null) {
            defaultRegistry = site.getDefaultRegistry();
        }
        return defaultRegistry;
    }

    /**
     * Gets the value of the Default Repository.
     *
     * @return a String value.
     */
    public String getDefaultRepository(String siteName) {
        String defaultRepository = "";

        XTestConfigSite site = getSiteByName(siteName);
        if (site != null) {
            defaultRepository = site.getDefaultRepository();
        }
        return defaultRepository;
    }

    /**
     * Gets the value of the Default Initiating Gateway.
     *
     * @return a String value.
     */
    public String getDefaultInitiatingGateway(String siteName) {
        String gateway = "";

        XTestConfigSite site = getSiteByName(siteName);
        if (site != null) {
            gateway = site.getDefaultInitiatingGateway();
        }
        return gateway;
    }

    /**
     * Gets the value of a registry's endpoint based on the given inputs.
     *
     * @param siteName
     * @param registryName
     * @param transactionName
     * @param secureEndpoint
     * @param asyncEndpoint
     *
     * @return a String value.
     */
    public String getRegistryEndpoint(String siteName,
                                      String registryName,
                                      String transactionName,
                                      boolean secureEndpoint,
                                      boolean asyncEndpoint) {
        String endpointUrl = "";

        XTestConfigSite site = getSiteByName(siteName);
        if (site != null) {
            XTestConfigRegistry reg = site.getRegistryByName(registryName);
            if (reg != null) {
                endpointUrl = getActorEndpoint(reg, transactionName, secureEndpoint, asyncEndpoint);
            }
        }
        return endpointUrl;
    }

    /**
     * Gets the value of a repository's endpoint based on the given inputs.
     *
     * @param siteName
     * @param repositoryId
     * @param transactionName
     * @param secureEndpoint
     * @param asyncEndpoint
     *
     * @return a String value.
     */
    public String getRepositoryEndpoint(String siteName,
                                        String repositoryId,
                                        String transactionName,
                                        boolean secureEndpoint,
                                        boolean asyncEndpoint) {
        String endpointUrl = "";

        XTestConfigSite site = getSiteByName(siteName);
        if (site != null) {
            XTestConfigRepository rep = site.getRepository(repositoryId);
            if (rep != null) {
                endpointUrl = getActorEndpoint(rep, transactionName, secureEndpoint, asyncEndpoint);
            }
        }
        return endpointUrl;
    }

    /**
     * Gets the value of an initiating gateway's endpoint based on the given inputs.
     *
     * @param siteName
     * @param homeCommunityId
     * @param transactionName
     * @param secureEndpoint
     * @param asyncEndpoint
     *
     * @return a String value.
     */
    public String getInitiatingGatewayEndpoint(String siteName,
                                               String homeCommunityId,
                                               String transactionName,
                                               boolean secureEndpoint,
                                               boolean asyncEndpoint) {
        String endpointUrl = "";

        XTestConfigSite site = getSiteByName(siteName);
        if (site != null) {
            XTestConfigGateway gateway = site.getInitiatingGatewayByHomeCommunityId(homeCommunityId);
            if (gateway != null) {
                endpointUrl = getActorEndpoint(gateway, transactionName, secureEndpoint, asyncEndpoint);
            }
        }
        return endpointUrl;
    }

    /**
     * Gets the value of a responding gateway's endpoint based on the given inputs.
     *
     * @param siteName
     * @param homeCommunityId
     * @param transactionName
     * @param secureEndpoint
     * @param asyncEndpoint
     *
     * @return a String value.
     */
    public String getRespondingGatewayEndpoint(String siteName,
                                               String homeCommunityId,
                                               String transactionName,
                                               boolean secureEndpoint,
                                               boolean asyncEndpoint) {
        String endpointUrl = "";

        XTestConfigSite site = getSiteByName(siteName);
        if (site != null) {
            XTestConfigGateway gateway = site.getRespondingGatewayByHomeCommunityId(homeCommunityId);
            if (gateway != null) {
                endpointUrl = getActorEndpoint(gateway, transactionName, secureEndpoint, asyncEndpoint);
            }
        }
        return endpointUrl;
    }


    ///////////////////////////////////////////////////////////////////////////
    // PROTECTED METHODS
    /////////////////////////////////////////////////////////////////////////
    /**
     * This method is an utility method to inspect a rootNode and get all its child elements with
     * the specified name
     * @param rootNode
     * @param localName
     */
    protected static ArrayList<OMElement> parseLevelOneNode(OMElement rootNode, String localName) {
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
     * Private constructor responsible for loading configuration file into memory.
     */
    private XTestConfig() throws XdsInternalException {
        loadConfiguration();
    }

    /**
     * Retrieves XML configuration file, parses and places into an easily accessible data structure.
     */
    private void loadConfiguration() throws XdsInternalException {
        String configLocation = System.getenv("HIEOSxTestDir");
        String configXML = null;
        if (configLocation != null) {
            try {
                logger.info("Loading XTestConfig from: " + configLocation);
                // Get the configuration file from the file system.
                String configFileAbsolutePath = configLocation + File.separatorChar + CONFIG_FILE_NAME;
                configXML = Io.getStringFromInputStream(new FileInputStream(new File(configFileAbsolutePath)));
            } catch (Exception e) {
                throw new XdsInternalException(
                        "XTestConfig: Could not load configuration from " + configLocation + " " + e.getMessage());
            }
        } else {
             throw new XdsInternalException("XTestConfig: Environment variable HIEOSxTestDir not set");
        }

        // Parse the XML file.
        OMElement configXMLRoot = Util.parse_xml(configXML);
        if (configXMLRoot == null) {
            throw new XdsInternalException(
                    "XTestConfig: Could not parse configuration from " + configLocation);
        }
        buildInternalStructure(configXMLRoot);
    }

    /**
     * This is passed in an OMElement, that represents the entire configuration file, xtestconfig.xml
     * @param rootNode, an OMElement.
     */
    private void buildInternalStructure(OMElement rootNode) throws XdsInternalException {
        parseSites(rootNode);
        properties.parse(rootNode);
    }

    /**
     * This is passed in an OMElement, that represents the root of the configuration file, xtestconfig.xml, namely "Config".
     * @param rootNode, an OMElement.
     */
    private void parseSites(OMElement rootNode) throws XdsInternalException {
        ArrayList<OMElement> list = parseLevelOneNode(rootNode, SITE_ELEMENT);
        for (OMElement currentNode : list) {
            XTestConfigSite site = new XTestConfigSite();
            site.parse(currentNode);
            sites.put(site.getName(), site);
        }
    }

    /**
     * This private utility method determines the endpoint url for an XTestConfigActor, based on other input criteria.
     * @param actor
     * @param transactionName
     * @param secureEndpoint
     * @param asyncEndpoint
     */
    private String getActorEndpoint(XTestConfigActor actor,
                                   String transactionName,
                                   boolean secureEndpoint,
                                   boolean asyncEndpoint) {
        String endpointUrl = "";

        XTestConfigTransaction txn = actor.getTransaction(transactionName);
        if (txn != null) {
            XTestConfigTransactionEndpoint txnEP = txn.getEndpoint(secureEndpoint, asyncEndpoint);
            if (txnEP != null) {
                endpointUrl = txnEP.getEndpointURL();
             }
        }
        return endpointUrl;
    }
}
