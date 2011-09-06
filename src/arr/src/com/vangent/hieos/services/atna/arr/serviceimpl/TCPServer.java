/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.atna.arr.serviceimpl;

import com.vangent.hieos.xutil.socket.BasicServer;
import com.vangent.hieos.xutil.socket.ServerProperties;
import com.vangent.hieos.xutil.socket.TLSSocketSupport;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class TCPServer extends BasicServer {

    private static final Logger log = Logger.getLogger(TCPServer.class);
    private ServerSocket serverSocket;
    private static final String SO_TIMEOUT = "socket_timeout"; 
    private static final String SO_BACKLOG = "socket_backlog";
    private static final String TLS_ENABLED = "tls_enabled";
    private static final String TCP_PORT = "tcp_port";


    /**
     *
     * @param poolSize
     * @param props
     * @throws IOException
     */
    public TCPServer(int poolSize, ServerProperties props) throws IOException {
        super(poolSize, props);
    }

    /**
     * Connect to Ports
     *
     */
    public void start() {
        // Establish connection on the port
        try {
            log.info("Start TCP Server ....");
            int soBacklog = props.getIntegerProperty(SO_BACKLOG);
            int tcpPort = props.getIntegerProperty(TCP_PORT);

            if (props.getProperty(TLS_ENABLED).equalsIgnoreCase("true")) {
                TLSSocketSupport socketSupport = new TLSSocketSupport();
                serverSocket = socketSupport.getSecureServerSocket(tcpPort, soBacklog);
                log.info("+++++ SOCKET [TLS] created (PORT = " + tcpPort + ") +++++");
            } else {
                serverSocket = new ServerSocket(tcpPort, soBacklog);
                log.info("+++++ SOCKET [non-TLS] created (PORT = " + tcpPort + ") +++++");
            }
        } catch (Exception ex) {
            log.error("Error Starting Server: " + ex);
        } finally {
        }
    }

    /**
     * Listen on Port
     *
     * @throws IOException
     */
    @Override
    public void listen() throws IOException {
        try {
            // Start new Thread and Listen on the TCP Server
            TCPListener listener = new TCPListener(serverSocket, props, pool);
            Future<String> future = pool.submit(listener);
        } catch (Exception ex) {
            log.error(ex.toString());
        } finally {
        }
    }

    /**
     *
     * @throws IOException
     */
    @Override
    public void serve() throws IOException {
        //ServerSocket serverSocket = null;
        try {
            int tcpPort = props.getIntegerProperty(TCP_PORT);
            //serverSocket = start();
            //serverSocket.setSoTimeout(props.getIntegerProperty(SO_TIMEOUT));
            log.info("Waiting For TCP Messages on Port: " + tcpPort);
            while (true) {
                Socket clientConnection = serverSocket.accept();
                pool.execute(new TCPHandler(clientConnection, props));
            }
        } catch (Exception ex) {
            log.error(ex.toString());
        } finally {
            close();
        }
    }

    /**
     *
     */
    @Override
    public void close() {
        log.info("Closing TCP ServerSocket");
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                log.error("Error Closing Socket: " + ex);
            }
        }
        super.close();
    }
}
