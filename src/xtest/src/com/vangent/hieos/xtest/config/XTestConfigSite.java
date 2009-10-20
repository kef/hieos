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

// Third-party.
import com.vangent.hieos.xutil.xconfig.XConfigProperties;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 * This class, XTestConfigSite, encapsulates all actor definitions such as Repository, registry,
 * and gateways.
 *
 * @author Anand Sastry
 *
 */
public class XTestConfigSite {
    private static final Logger logger = Logger.getLogger(XTestConfigSite.class);

    // Constants
    private static final String PID_ALLOCATE_ENDPOINT = "PidAllocateEndpoint";
    private static final String DEFAULT_REGISTRY = "DefaultRegistry";
    private static final String DEFAULT_REPOSITORY = "DefaultRepository";
    private static final String DEFAULT_INITIATING_GATEWAY = "DefaultInitiatingGateway";
    private static final String REGISTRY_ELEMENT = "Registry";
    private static final String REPOSITORY_ELEMENT = "Repository";
    private static final String INITIATING_GATEWAY_ELEMENT = "InitiatingGateway";
    private static final String RESPONDING_GATEWAY_ELEMENT = "RespondingGateway";
    private static final String NAME_ATTRIBUTE = "name";


    // State
    private String name = "";

    private XConfigProperties properties = new XConfigProperties();
    private HashMap<String, XTestConfigRegistry> registries = new HashMap<String, XTestConfigRegistry>(); // Key = registry name, Value = XTestConfigRegistry
    private HashMap<String, XTestConfigRepository> repositories = new HashMap<String, XTestConfigRepository>();// Key = repository unique id, XTestConfigRepository
    protected HashMap<String, XTestConfigGateway> initiatingGateways = new HashMap<String, XTestConfigGateway>();// Key = homeCommunityId, Value = XTestConfigGateway
    protected HashMap<String, XTestConfigGateway> respondingGateways = new HashMap<String, XTestConfigGateway>();     // Key = AA unique ID, XTestConfigGateway

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////
    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the value of the PIDAllocateEndpoint. Accessing this url results in the generation of
     * a patient id for use by the registry.
     *
     * @return a String value.
     */
    public String getPIDAllocateEndpoint() {
        return (String) properties.getProperty(PID_ALLOCATE_ENDPOINT);
    }

    /**
     * Get the value of the Default Registry.
     *
     * @return a String value.
     */
    public String getDefaultRegistry() {
        return (String) properties.getProperty(DEFAULT_REGISTRY);
    }

    /**
     * Get the value of the Default Repository.
     *
     * @return a String value.
     */
    public String getDefaultRepository() {
        return (String) properties.getProperty(DEFAULT_REPOSITORY);
    }

     /**
     * Get the value of the Default Initiating Gateway.
     *
     * @return a String value.
     */
    public String getDefaultInitiatingGateway() {
        return (String) properties.getProperty(DEFAULT_INITIATING_GATEWAY);
    }

    /**
     * This returns a XTestConfigRegistry, a registry definition, given a registry name.
     * @param registryName
     * @return XTestConfigRegistry
     */
    public XTestConfigRegistry getRegistryByName(String registryName) {
        XTestConfigRegistry registry = null;
        if (this.registries.containsKey(registryName)) {
            registry = (XTestConfigRegistry) this.registries.get(registryName);
        }
        return registry;
    }
    
    /**
     * This returns a XTestConfigRepository, a repository definition,  given a repository uid.
     * @param repositoryId
     * @return XTestConfigRepository
     */
    public XTestConfigRepository getRepository(String repositoryId) {
        XTestConfigRepository repository = null;
        if (this.repositories.containsKey(repositoryId)) {
            repository = (XTestConfigRepository) this.repositories.get(repositoryId);
        }
        return repository;
    }

    ///////////////////////////////////////////////////////////////////////////
    // PROTECTED METHODS
    //////////////////////////////////////////////////////////////////////////
    /**
     * This returns a XTestConfigGateway, a initiating gateway definition,  given a home community id.
     * @param homeCommunityId
     * @return XTestConfigGateway
     */
    protected XTestConfigGateway getInitiatingGatewayByHomeCommunityId(String homeCommunityId) {
        XTestConfigGateway initiatingGateway = null;
        if (this.initiatingGateways.containsKey(homeCommunityId)) {
            initiatingGateway = initiatingGateways.get(homeCommunityId);
        }
        return initiatingGateway;
    }

    /**
     * This returns a XTestConfigGateway, a responding gateway definition,  given a home community id.
     * @param homeCommunityId
     * @return XTestConfigGateway
     */
    protected XTestConfigGateway getRespondingGatewayByHomeCommunityId(String homeCommunityId) {
        XTestConfigGateway respondingGateway = null;
        if (this.respondingGateways.containsKey(homeCommunityId)) {
            respondingGateway = respondingGateways.get(homeCommunityId);
        }
        return respondingGateway;
    }

    /**
     * This method is passed in a Site OMElement.
     * It establishes the internal structure of the site.
     *
     * @param rootNode an OMElement.
     */
    protected void parse(OMElement rootNode) {
        this.name = rootNode.getAttributeValue(new QName(NAME_ATTRIBUTE));
        parseProperties(rootNode);
        parseRegistries(rootNode);
        parseRepositories(rootNode);
        parseInitiatingGateways(rootNode);
        parseRespondingGateways(rootNode);
    }

    ///////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    //////////////////////////////////////////////////////////////////////////

    /**
     * This method is passed in a Site OMElement.
     * It establishes the sites properties.
     *
     * @param rootNode an OMElement.
     */
    private void parseProperties(OMElement rootNode) {
        properties.parse(rootNode);
    }

    /**
     * This method is passed in a Site OMElement.
     * It establishes the site's registries.
     *
     * @param rootNode an OMElement.
     */
    private void parseRegistries(OMElement rootNode) {
        ArrayList<OMElement> list = XTestConfig.parseLevelOneNode(rootNode, REGISTRY_ELEMENT);
        for (OMElement currentNode : list) {
            XTestConfigRegistry registry = new XTestConfigRegistry();
            registry.parse(currentNode);
            this.registries.put(registry.getName(), registry);
        }
    }

    /**
     * This method is passed in a Site OMElement.
     * It establishes the site's repositories.
     *
     * @param rootNode an OMElement.
     */
    private void parseRepositories(OMElement rootNode) {
        ArrayList<OMElement> list = XTestConfig.parseLevelOneNode(rootNode, REPOSITORY_ELEMENT);
        for (OMElement currentNode : list) {
            XTestConfigRepository repository = new XTestConfigRepository();
            repository.parse(currentNode);
            //System.out.println("*** Adding Repository ID = " + repository.getUniqueId());
            this.repositories.put(repository.getUniqueId(), repository);
        }
    }

    /**
     * This method is passed in a Site OMElement.
     * It establishes the site's initiating gateways.
     *
     * @param rootNode an OMElement.
     */
    private void parseInitiatingGateways(OMElement rootNode) {
        ArrayList<OMElement> list = XTestConfig.parseLevelOneNode(rootNode, INITIATING_GATEWAY_ELEMENT);
        for (OMElement currentNode : list) {
            XTestConfigGateway gateway = new XTestConfigGateway();
            gateway.parse(currentNode);
            this.initiatingGateways.put(gateway.getHomeCommunityId(), gateway);
        }
    }

    /**
     * This method is passed in a Site OMElement.
     * It establishes the site's responding gateways.
     *
     * @param rootNode an OMElement.
     */
    private void parseRespondingGateways(OMElement rootNode) {
        ArrayList<OMElement> list = XTestConfig.parseLevelOneNode(rootNode, RESPONDING_GATEWAY_ELEMENT);
        for (OMElement currentNode : list) {
            XTestConfigGateway gateway = new XTestConfigGateway();
            gateway.parse(currentNode);
            this.respondingGateways.put(gateway.getHomeCommunityId(), gateway);
        }
    }
}
