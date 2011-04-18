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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class TCPListener implements Callable {

    private ServerSocket serverSocket;
    private ServerProperties props;
    private ExecutorService pool;

    private static final String TCP_PORT = "tcp_port";
    private static final Logger log = Logger.getLogger(TCPListener.class);

    /**
     *
     * @param serverSocket
     * @param props
     * @param pool
     */
    public TCPListener(ServerSocket serverSocket, ServerProperties props, ExecutorService pool) {
        this.serverSocket = serverSocket;
        this.props = props;
        this.pool = pool;
    }

    /**
     * 
     * @return
     */
    public String call(){
        try {
            int tcpPort = props.getIntegerProperty(TCP_PORT);
            log.info("Waiting For TCP Messages on Port: " + tcpPort);
            while (true) {
                Socket clientConnection = serverSocket.accept();
                pool.execute(new TCPHandler(clientConnection, props));
            }
        } catch (Exception ex) {
            log.error(ex.toString());
        } finally {
            log.info("Closing TCP ServerSocket ");
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    log.error("Error Closing Socket: " + ex);
                }
            }
            pool.shutdown();
            return "DONE";
        }
    }
}
