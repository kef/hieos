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
    private static String MESSAGE_HANDLERS = "message-handlers.message-handler";
    private static String LISTENERS = "listeners.listener";
    private List<MessageHandlerConfig> messageHandlerConfigs = new ArrayList<MessageHandlerConfig>();
    private List<ListenerConfig> listenerConfigs = new ArrayList<ListenerConfig>();

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
    public List<MessageHandlerConfig> getMessageHandlerConfigs() {
        return messageHandlerConfigs;
    }

    /**
     *
     * @return
     */
    public List<ListenerConfig> getListenerConfigs() {
        return listenerConfigs;
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

            this.loadMessageHandlerConfigs(xmlConfig);
            this.loadListenerConfigs(xmlConfig);
        } catch (ConfigurationException ex) {
            throw new HL7v2UtilException(
                    "HL7v2AcceptorConfig: Could not load configuration from "
                    + configLocation + " " + ex.getMessage());
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

    /**
     *
     * @param hc
     * @throws HL7v2UtilException
     */
    private void loadListenerConfigs(HierarchicalConfiguration hc) throws HL7v2UtilException {
        // Get message handler configurations.
        List listeners = hc.configurationsAt(LISTENERS);
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcListenerConfig = (HierarchicalConfiguration) it.next();
            ListenerConfig listenerConfig = new ListenerConfig();
            listenerConfig.load(hcListenerConfig, this);
            // Keep track of listener configurations.
            listenerConfigs.add(listenerConfig);
        }
    }
}
