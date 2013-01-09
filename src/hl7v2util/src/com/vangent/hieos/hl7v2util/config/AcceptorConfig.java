/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v2util.config;

import com.vangent.hieos.hl7v2util.exception.HL7v2UtilException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class AcceptorConfig {

    private final static Logger log = Logger.getLogger(AcceptorConfig.class);
    private static String ACCEPTOR_ENABLE_TLS = "acceptor-enable-tls";
    private static String ACCEPTOR_LISTENER_PORT = "acceptor-listener-port";
    private static String ACCEPTOR_THREAD_POOL_SIZE = "acceptor-thread-pool-size";
    private static String ACCEPTOR_CIPHER_SUITES = "acceptor-cipher-suites";
    private static String MESSAGE_HANDLERS = "message-handlers.message-handler";
    private int acceptorListenerPort = -1;
    private int acceptorThreadPoolSize = -1;
    private boolean acceptorEnableTLS;
    private String[] cipherSuites = null;
    private List<MessageHandlerConfig> messageHandlerConfigs = new ArrayList<MessageHandlerConfig>();

    /**
     *
     * @param configFileName
     * @throws HL7v2UtilException
     */
    public AcceptorConfig(String configFileName) throws HL7v2UtilException {
        this.loadConfiguration(configFileName);
    }

    /**
     *
     * @return
     */
    public int getAcceptorListenerPort() {
        return acceptorListenerPort;
    }

    /**
     * 
     * @return
     */
    public boolean isAcceptorTLSEnabled() {
        return acceptorEnableTLS;
    }

    /**
     *
     * @return
     */
    public String[] getCipherSuites() {
        return cipherSuites;
    }

    /**
     *
     * @return
     */
    public int getAcceptorThreadPoolSize() {
        return acceptorThreadPoolSize;
    }

    /**
     * 
     * @return
     */
    public List<MessageHandlerConfig> getMessageHandlerConfigs() {
        return messageHandlerConfigs;
    }

    /**
     * 
     * @param configLocation
     * @throws HL7v2UtilException
     */
    private void loadConfiguration(String configLocation) throws HL7v2UtilException {
        try {
            log.info("Loading HL7v2Acceptor configuration from " + configLocation);
            XMLConfiguration xmlConfig = new XMLConfiguration(configLocation);
            acceptorEnableTLS = xmlConfig.getBoolean(ACCEPTOR_ENABLE_TLS, false);
            acceptorListenerPort = xmlConfig.getInt(ACCEPTOR_LISTENER_PORT, -1);
            acceptorThreadPoolSize = xmlConfig.getInt(ACCEPTOR_THREAD_POOL_SIZE, -1);
            if (acceptorEnableTLS) {
                this.loadCipherSuites(xmlConfig);
            }
            this.loadMessageHandlerConfigs(xmlConfig);
        } catch (ConfigurationException ex) {
            throw new HL7v2UtilException(
                    "HL7v2AcceptorConfig: Could not load configuration from "
                    + configLocation + " " + ex.getMessage());
        }
        if (acceptorListenerPort == -1) {
            throw new HL7v2UtilException("Must specify " + ACCEPTOR_LISTENER_PORT + " in configuration.");
        }
        if (acceptorThreadPoolSize == -1) {
            throw new HL7v2UtilException("Must specify " + ACCEPTOR_THREAD_POOL_SIZE + " in configuration.");
        }
    }

    /**
     *
     * @param xmlConfig
     * @throws HL7v2UtilException
     */
    private void loadCipherSuites(XMLConfiguration xmlConfig) throws HL7v2UtilException {
        cipherSuites = xmlConfig.getStringArray(ACCEPTOR_CIPHER_SUITES);
        if (cipherSuites == null || cipherSuites.length == 0) {
            throw new HL7v2UtilException("Must specify " + ACCEPTOR_CIPHER_SUITES + " in configuration.");
        }
    }

    /**
     * 
     * @param hc
     * @throws HL7v2UtilException
     */
    private void loadMessageHandlerConfigs(HierarchicalConfiguration hc) throws HL7v2UtilException {
        // Get message handler configurations.
        List messageHandlers = hc.configurationsAt(MESSAGE_HANDLERS);
        for (Iterator it = messageHandlers.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcMessageHandler = (HierarchicalConfiguration) it.next();
            MessageHandlerConfig messageHandlerConfig = new MessageHandlerConfig();
            messageHandlerConfig.load(hcMessageHandler, this);
            // Keep track of message handler configurations.
            messageHandlerConfigs.add(messageHandlerConfig);
        }
    }
}
