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

import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import com.vangent.hieos.hl7v2util.config.AcceptorConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7v2Acceptor implements Runnable {

    private static final Logger log = Logger.getLogger(HL7v2Acceptor.class);
    private final LowerLayerProtocol llp;
    private final Parser parser;
    private ExecutorService pool = null;
    private ConnectionManager connectionManager = null;
    private final MessageRouter messageRouter;
    private final AcceptorConfig acceptorConfig;
    private ExecutorService listener = null;
    private Future<?> acceptorThread = null;
    private ServerSocket serverSocket;

    /**
     * 
     * @param acceptorConfig
     */
    public HL7v2Acceptor(AcceptorConfig acceptorConfig) {
        this.acceptorConfig = acceptorConfig;
        this.messageRouter = new MessageRouter(acceptorConfig);
        this.llp = LowerLayerProtocol.makeLLP(); // The transport protocol
        this.parser = new PipeParser(); // The message parser

        // For the listener thread.
        this.listener = Executors.newSingleThreadExecutor();
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
     * @return
     */
    public LowerLayerProtocol getLowerLayerProtocol() {
        return llp;
    }

    /**
     *
     * @return
     */
    public MessageRouter getMessageRouter() {
        return messageRouter;
    }

    /**
     * 
     * @return
     */
    public Parser getParser() {
        return parser;
    }

    /**
     *
     * @return
     */
    public ExecutorService getPool() {
        return pool;
    }

    /**
     * 
     */
    public void start() throws IOException {
        log.info("HL7v2Acceptor getting ServerSocket (port = "
                + acceptorConfig.getAcceptorListenerPort()
                + ", TLS=" + acceptorConfig.isAcceptorTLSEnabled()
                + ", thread pool size = " + acceptorConfig.getAcceptorThreadPoolSize()
                + ")");
        // FIXME: Add TLSSocket support.

        // Create listener socket.
        this.serverSocket = new ServerSocket(acceptorConfig.getAcceptorListenerPort());

        // Create ConnectionManager instance (and tell about the server socket).
        this.connectionManager = new ConnectionManager();

        // Create acceptor thread pool.
        this.pool = Executors.newFixedThreadPool(acceptorConfig.getAcceptorThreadPoolSize());


        // Startup listener thread.
        this.acceptorThread = listener.submit(this);
    }

    /**
     *
     */
    public void run() { // run the service
        try {
            for (;;) {
                log.info("HL7v2Acceptor Thread waiting for connection (HL7v2Acceptor thread name = "
                        + Thread.currentThread().getName()
                        + ", port = " + acceptorConfig.getAcceptorListenerPort()
                        + ", TLS=" + acceptorConfig.isAcceptorTLSEnabled()
                        + ", thread pool size = " + acceptorConfig.getAcceptorThreadPoolSize()
                        + ")");
                Connection connection = null;
                // Wait on socket connection.
                Socket socket = serverSocket.accept();
                try {
                    // Create a new Connection instance.
                    connection = new Connection(acceptorConfig, parser, llp, messageRouter, socket);

                    // Add connection to the ConnectionManager.
                    connectionManager.addConnection(connection);
                    log.info("HL7v2Acceptor Thread accepted connection (HL7v2Acceptor thread name = "
                            + Thread.currentThread().getName()
                            + ", remote ip = " + connection.getRemoteAddress()
                            + ", remote port = " + connection.getRemotePort()
                            + ")");

                    // Handle the connection (from the thread pool).
                    pool.execute(new ConnectionHandler(connectionManager, connection));
                } catch (Exception ex) {
                    log.error("Exception in HL7v2Acceptor thread", ex);
                    // FIXME?
                    //if (connection != null) {
                    //    connection.close();
                    //}
                }
            }
        } catch (IOException ex) {
            log.error("Exception in HL7v2Acceptor thread", ex);
            //this.shutdownAndAwaitTermination();
        }
    }

    /**
     *
     */
    public void shutdownAndAwaitTermination() {
        try {
            log.info("Closing HL7v2Acceptor listener socket");
            serverSocket.close();
            log.info("HL7v2Acceptor listener socket closed!");
        } catch (IOException ex) {
            log.error("HL7v2Acceptor - problem closing socket", ex);
        }

        // Try to shutdown main listener now.
        log.info("Shutting down listener for HL7v2Acceptor ...");
        listener.shutdown();
        try {
            if (!listener.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                listener.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!listener.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                    log.error("HL7v2Acceptor - listener did not terminate");
                } else {
                    log.warn("HL7v2Acceptor listener terminated after forced shutdown!");
                }
            } else {
                log.info("HL7v2Acceptor listener terminated gracefully!");
            }
        } catch (InterruptedException ex) {// (Re-)Cancel if current thread also interrupted
            listener.shutdownNow();
            log.warn("HL7v2Acceptor listener terminated after forced shutdown!");
            // Preserve interrupt status
            // FIXME?
            //Thread.currentThread().interrupt();
        }


        log.info("Shutting down thread pool for HL7v2Acceptor ...");
        if (pool == null) {
            return;  // Early exit!
        }
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            // FIXME: Make wait time configurable.
            if (!pool.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                    log.error("HL7v2Acceptor - thread pool did not terminate");
                } else {
                    log.warn("HL7v2Acceptor thread pool terminated after forced shutdown!");
                }
            } else {
                log.info("HL7v2Acceptor thread pool terminated gracefully!");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            log.warn("HL7v2Acceptor thread pool terminated after forced shutdown!");
            // Preserve interrupt status
            // FIXME?
            // Thread.currentThread().interrupt();
        }

        // Shutdown any open connections
        log.info("Shutting down any open connections for HL7v2Acceptor ...");
        if (connectionManager == null) {
            return;  // Early exit!
        }
        connectionManager.closeConnections();
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
            acceptor.run();
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
