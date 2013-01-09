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
package com.vangent.hieos.hl7v2util.acceptor;

import com.vangent.hieos.hl7v2util.config.AcceptorConfig;
import com.vangent.hieos.hl7v2util.exception.HL7v2UtilException;
import com.vangent.hieos.xutil.socket.TLSSocketSupport;
import java.io.IOException;
import java.net.ServerSocket;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7v2Acceptor {

    private static final Logger log = Logger.getLogger(HL7v2Acceptor.class);
    private static final int SO_BACKLOG = 20;
    private final AcceptorConfig acceptorConfig;
    private ServerSocket serverSocket;
    private HL7v2Listener listener;

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
    public void startup() throws HL7v2UtilException {
        log.info("HL7v2Acceptor: getting ServerSocket (port = "
                + acceptorConfig.getAcceptorListenerPort()
                + ", TLS=" + acceptorConfig.isAcceptorTLSEnabled()
                + ", thread pool size = " + acceptorConfig.getAcceptorThreadPoolSize()
                + ")");

        // See if TLS is enabled.
        if (acceptorConfig.isAcceptorTLSEnabled()) {
            // Create listener socket (TLS).
            TLSSocketSupport socketSupport = new TLSSocketSupport();
            try {
                this.serverSocket = socketSupport.getSecureServerSocket(
                        acceptorConfig.getAcceptorListenerPort(), SO_BACKLOG, acceptorConfig.getCipherSuites());
            } catch (Exception ex) {
                log.fatal("HL7v2Acceptor: could not open TLS socket", ex);
                throw new HL7v2UtilException("HL7v2Acceptor: could not open TLS socket", ex);
            }
        } else {
            try {
                // Create listener socket (no TLS).
                this.serverSocket = new ServerSocket(acceptorConfig.getAcceptorListenerPort(), SO_BACKLOG);
            } catch (IOException ex) {
                log.fatal("HL7v2Acceptor: could not open socket", ex);
                throw new HL7v2UtilException("HL7v2Acceptor: could not open socket", ex);
            }
        }

        // Get and start listener.
        listener = new HL7v2Listener(acceptorConfig, this.serverSocket);
        listener.startup();
    }

    /**
     * 
     */
    public void shutdown() {
        // Shutdown listener (try gracefully).
        listener.shutdownAndAwaitTermination();
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
