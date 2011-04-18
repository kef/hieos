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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class UDPServer extends BasicServer{

    private static final Logger log = Logger.getLogger(UDPServer.class);
    private DatagramSocket serverSocket;
    private static final String UDP_PORT = "udp_port";
    private static final String UDP_PACKET_SIZE = "udp_packet_size";

    /**
     *
     * @param poolSize
     * @param props
     * @throws IOException
     */
    public UDPServer(int poolSize, ServerProperties props) throws IOException {
        super(poolSize, props);
    }

    /**
     *
     */
    public void start() {
        // Establish connection on the port
        try {
            log.info("Start UDP Server ....");
            // Start the UDP Server
            int udpPort = props.getIntegerProperty(UDP_PORT);
            serverSocket = new DatagramSocket(udpPort);
            log.info("+++++ SOCKET [UDP] created (PORT = " + udpPort + ") +++++");
        } catch (Exception ex) {
            log.error("Error Starting UDP Server: " + ex);
        } finally {
        }
    }

    /**
     *
     * @throws IOException
     */
    public void listen() throws IOException {
        try {
            // Start new Thread and Listen on the UDP Server
           UDPListener listener = new UDPListener(serverSocket, props, pool);
           Future<String> future = pool.submit(listener);
        } catch  (Exception ex) {
            log.error(ex.toString());
        } finally {
            /*if (serverSocket != null) {
                serverSocket.close();
            }
            pool.shutdown(); */
        }
    }

    /**
     *
     * @throws IOException
     */
    public void serve() throws IOException {
        //DatagramSocket serverSocket = null;
        //log.info("About to Start UDP Server ");
        try {
            // Start the UDP Server
            int udpPort = props.getIntegerProperty(UDP_PORT);
            int packetSize = props.getIntegerProperty(UDP_PACKET_SIZE);
           // serverSocket = new DatagramSocket(udpPort);
            log.info("Waiting For UDP Messages on Port: " + udpPort);
            while (true) {
                // Setup datagram packet to receive the client message
                byte[] clientData = new byte[packetSize]; //1024 default minimum
                DatagramPacket clientPacket = new DatagramPacket(clientData, clientData.length);
                // Wait for a UDP client connection
                serverSocket.receive(clientPacket);
                // Process the packet received
                pool.execute(new UDPHandler(clientPacket, props));
            }
        } catch  (Exception ex) {
            log.error(ex.toString());
        } finally {
            close();
        }
    }

    /**
     * 
     */
    public void close() {
        log.info("Closing UDP ServerSocket");
        if (serverSocket != null) {
            serverSocket.close();
        }
        super.close();
    }
}
