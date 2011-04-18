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

import com.vangent.hieos.xutil.socket.ServerProperties;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class UDPListener implements Callable {

    private DatagramSocket serverSocket;
    private ServerProperties props;
    private ExecutorService pool;

    private static final String UDP_PORT = "udp_port";
    private static final String UDP_PACKET_SIZE = "udp_packet_size";
    private static final Logger log = Logger.getLogger(UDPListener.class);

    /**
     *
     * @param serverSocket
     * @param props
     * @param pool
     */
    public UDPListener(DatagramSocket serverSocket, ServerProperties props, ExecutorService pool) {
        this.serverSocket = serverSocket;
        this.props = props;
        this.pool = pool;
    }

    /**
     * 
     * @return
     */
    public String call(){
        int udpPort = props.getIntegerProperty(UDP_PORT);
        int packetSize = props.getIntegerProperty(UDP_PACKET_SIZE);
        try {
            log.info("Waiting For UDP Messages on Port: " + udpPort);
            while (true) {
                // Setup datagram packet to receive the client message
                // Figure out how to process if client sends packets in blocks of 1024 bytes
                byte[] clientData = new byte[packetSize]; // default minimum is 1024
                DatagramPacket clientPacket = new DatagramPacket(clientData, clientData.length);
                // Wait for a UDP client connection
                serverSocket.receive(clientPacket);
                // Process the packet received
                pool.execute(new UDPHandler(clientPacket, props));
            }
        } catch  (Exception ex) {
            log.error(ex.toString());
        } finally {
            log.info("Closing UDP ServerSocket ");
            if (serverSocket != null) {
                serverSocket.close();
            }
            pool.shutdown();
            return "DONE";
        }
    }
}
