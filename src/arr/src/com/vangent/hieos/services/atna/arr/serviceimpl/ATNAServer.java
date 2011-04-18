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
import java.io.IOException;


/**
 *
 * @author Adeola Odunlami
 */
public class ATNAServer {

    private static final String POOL_SIZE = "pool_size";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException, Exception {
        if (args.length < 2) {
            ATNAServer.printUsage();
            System.exit(1);
        }
        if (!args[0].equalsIgnoreCase("-p")) {
            ATNAServer.printUsage();
            System.exit(1);
        }

        /*if (!args[2].equalsIgnoreCase("-protocol")) {
            ATNAServer.printUsage();
            System.exit(1);
        }*/

        String propertyFilename = args[1];
        ServerProperties props = new ServerProperties(propertyFilename);
        // String protocol = args[3];

        // Create server to process inbound messages.
        int poolSize = props.getIntegerProperty(POOL_SIZE);
        System.out.println("Initializing Server Environment");
        TCPServer tcpServer = new TCPServer(poolSize, props);
        UDPServer udpServer = new UDPServer(poolSize, props);

        // Start the servers
        udpServer.start();
        System.out.println("Started UDP Server");
        tcpServer.start();
        System.out.println("Started TCP Server");

        // Listen on both ports
        udpServer.listen();
        System.out.println("UDP Server  Waiting");
        tcpServer.listen();
        System.out.println("TCP Server  Waiting");

        // Close the sockets
        //udpServer.close();
        //tcpServer.close();

        // Check which port to listen on
        /*if (protocol.equals("TCP")){
            tcpServer.start();
            tcpServer.serve();
            // Close the socket
            tcpServer.close();
        } else {
            udpServer.start();
            udpServer.serve();
            // Close the socket
            udpServer.close();
        }*/
    }

    /**
     *
     */
    private static void printUsage() {
        System.out.println("Usage: ATNAServer -p <properties_file>");
    }
}
