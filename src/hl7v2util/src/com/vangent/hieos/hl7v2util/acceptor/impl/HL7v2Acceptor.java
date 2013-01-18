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
package com.vangent.hieos.hl7v2util.acceptor.impl;

import com.vangent.hieos.hl7v2util.acceptor.config.AcceptorConfig;
import com.vangent.hieos.hl7v2util.acceptor.config.ListenerConfig;
import com.vangent.hieos.hl7v2util.exception.HL7v2UtilException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7v2Acceptor {

    private static final Logger logger = Logger.getLogger(HL7v2Acceptor.class);
    private final AcceptorConfig acceptorConfig;
    private List<HL7v2Listener> listeners = new ArrayList<HL7v2Listener>();

    /**
     * 
     * @param acceptorConfig
     */
    public HL7v2Acceptor(AcceptorConfig acceptorConfig) {
        this.acceptorConfig = acceptorConfig;
    }

    /**
     *
     * @return
     */
    public AcceptorConfig getAcceptorConfig() {
        return acceptorConfig;
    }

    /**
     * 
     * @throws HL7v2UtilException
     */
    public void startup() {

        // Only need one of these.
        MessageRouter messageRouter = new MessageRouter(acceptorConfig);

        // Get list of listener configs.
        List<ListenerConfig> listenerConfigs = acceptorConfig.getListenerConfigs();

        // Startup all enabled listeners.
        for (ListenerConfig listenerConfig : listenerConfigs) {
            if (listenerConfig.isEnabled()) {
                try {
                    // Get listener and then start it up.
                    HL7v2Listener listener;
                    listener = new HL7v2Listener(listenerConfig, messageRouter);
                    listener.startup();
                    // Keep track of listeners (for later shutdown).
                    listeners.add(listener);
                } catch (HL7v2UtilException ex) {
                    logger.fatal("Could not startup listener", ex);
                }
            }
        }
    }

    /**
     * 
     */
    public void shutdown() {
        // Shutdown listeners (try gracefully).
        for (HL7v2Listener listener : listeners) {
            listener.shutdownAndAwaitTermination();
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String args[]) {
        if (args.length < 2) {
            HL7v2Acceptor.printUsage();
            System.exit(1);
        }
        if (!args[0].equalsIgnoreCase("-c")) {
            HL7v2Acceptor.printUsage();
            System.exit(1);
        }
        try {
            String configFileName = args[1];
            AcceptorConfig acceptorConfig = new AcceptorConfig(configFileName);
            HL7v2Acceptor acceptor = new HL7v2Acceptor(acceptorConfig);
            acceptor.startup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private static void printUsage() {
        System.out.println("Usage: HL7v2Acceptor -c <HL7v2AcceptorConfig.xml>");
    }
}
