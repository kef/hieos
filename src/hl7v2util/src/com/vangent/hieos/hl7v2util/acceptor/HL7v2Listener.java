/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v2util.acceptor;

import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import com.vangent.hieos.hl7v2util.config.AcceptorConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7v2Listener implements Runnable {

    private static final Logger log = Logger.getLogger(HL7v2Listener.class);
    private static final int SHUTDOWN_TIMEOUT_MSEC = 500;  // FIXME: Place in configuration file?
    private final LowerLayerProtocol llp;
    private final Parser parser;
    private ConnectionManager connectionManager;
    private final MessageRouter messageRouter;
    private final AcceptorConfig acceptorConfig;
    private ServerSocket serverSocket;
    private boolean shuttingDownSocket = false;
    private ExecutorService executorService;
    private ExecutorService workerPool;

    /**
     * 
     * @param acceptorConfig
     * @param serverSocket
     */
    public HL7v2Listener(AcceptorConfig acceptorConfig, ServerSocket serverSocket) {
        this.acceptorConfig = acceptorConfig;
        this.serverSocket = serverSocket;
        this.messageRouter = new MessageRouter(acceptorConfig);
        this.llp = LowerLayerProtocol.makeLLP(); // The transport protocol
        this.parser = new PipeParser(); // The message parser
        this.connectionManager = new ConnectionManager();

        // Create thread pool.
        this.workerPool = Executors.newFixedThreadPool(acceptorConfig.getAcceptorThreadPoolSize());
    }

    /**
     * 
     */
    public void startup() {
        // Start listener thread.
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this);
    }

    /**
     *
     */
    public void shutdownAndAwaitTermination() {
        try {
            log.info(getVitals() + "closing socket ...");
            shuttingDownSocket = true;
            serverSocket.close();
            log.info(getVitals() + "socket closed");
        } catch (IOException ex) {
            log.error(getVitals() + "exception when closing socket", ex);
        }

        // Try to shutdown main listener now.
        log.info(getVitals() + "shutting down socket listener thread ...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT_MSEC, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT_MSEC, TimeUnit.MILLISECONDS)) {
                    log.error(getVitals() + "socket listener thread did not terminate");
                } else {
                    log.warn(getVitals() + "socket listener thread terminated after forced shutdown!");
                }
            } else {
                log.info(getVitals() + "socket listener thread terminated gracefully!");
            }
        } catch (InterruptedException ex) {// (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            log.warn(getVitals() + "socket listener thread terminated after forced shutdown!", ex);
            // Preserve interrupt status
            // FIXME?
            //Thread.currentThread().interrupt();
        }


        log.info(getVitals() + "shutting down thread pool ...");
        if (workerPool == null) {
            return;  // Early exit!
        }
        workerPool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            // FIXME: Make wait time configurable.
            if (!workerPool.awaitTermination(SHUTDOWN_TIMEOUT_MSEC, TimeUnit.MILLISECONDS)) {
                workerPool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!workerPool.awaitTermination(SHUTDOWN_TIMEOUT_MSEC, TimeUnit.MILLISECONDS)) {
                    log.error(getVitals() + "thread pool did not terminate");
                } else {
                    log.warn(getVitals() + "thread pool terminated after forced shutdown!");
                }
            } else {
                log.info(getVitals() + "thread pool terminated gracefully!");
            }
        } catch (InterruptedException ex) {
            // (Re-)Cancel if current thread also interrupted
            workerPool.shutdownNow();
            log.warn(getVitals() + "thread pool terminated after forced shutdown!", ex);
            // Preserve interrupt status
            // FIXME?
            // Thread.currentThread().interrupt();
        }

        // Shutdown any open connections
        log.info(getVitals() + "shutting down open connections ...");
        if (connectionManager == null) {
            return;  // Early exit!
        }
        log.info(getVitals() + "connection count (after listener termination) = " + connectionManager.getConnectionCount());
        connectionManager.closeConnections();
    }

    /**
     * 
     * @return
     */
    private String getVitals() {
        return "HL7v2Listener (port = " + acceptorConfig.getAcceptorListenerPort() + "): ";
    }

    /**
     *
     */
    public void run() { // run the service
        while (!shuttingDownSocket) {
            log.info(getVitals() + "waiting for connection (thread = "
                    + Thread.currentThread().getName()
                    + ", thread pool size = " + acceptorConfig.getAcceptorThreadPoolSize()
                    + ")");
            Connection connection = null;
            Socket socket = null;
            try {
                // Wait for a connection on the socket.
                socket = serverSocket.accept();
            } catch (IOException ex) {
                if (!shuttingDownSocket) {
                    log.error(getVitals() + "exception on socket accept()", ex);
                }
            }
            try {
                connection = new Connection(acceptorConfig, parser, llp, messageRouter, socket);
                // Add connection to the ConnectionManager.
                connectionManager.addConnection(connection);
                log.info(getVitals() + "accepted connection (thread = "
                        + Thread.currentThread().getName()
                        + ", remote ip = " + connection.getRemoteAddress()
                        + ", remote port = " + connection.getRemotePort()
                        + ")");

                // Handle the connection (from the thread pool).
                workerPool.execute(new ConnectionHandler(connectionManager, connection));
            } catch (LLPException ex) {
                log.error(getVitals() + "exception accepting connection", ex);
            } catch (IOException ex) {
                log.error(getVitals() + "exception accepting connection", ex);
            }
        }
        log.info(getVitals() + "thread ended!");
    }
}
