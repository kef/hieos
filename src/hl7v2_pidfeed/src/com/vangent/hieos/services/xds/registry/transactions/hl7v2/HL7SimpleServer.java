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
// Adapted from HAPI SimpleServer.java
package com.vangent.hieos.services.xds.registry.transactions.hl7v2;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.HL7Service;
import java.io.InterruptedIOException;
import com.vangent.hieos.xutil.socket.TLSSocketSupport;
import java.net.ServerSocket;
import java.net.Socket;

import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.log.HapiLog;
import ca.uhn.log.HapiLogFactory;

/**
 * <p>A simple TCP/IP-based HL7 server.  This server listens for connections
 * on a particular port, and creates a ConnectionManager for each incoming
 * connection.  </p>
 * <p>A single SimpleServer can only service requests that use a
 * single class of LowerLayerProtocol (specified at construction time).</p>
 * <p>The ConnectionManager uses a PipeParser of the version specified in
 * the constructor</p>
 * <p>ConnectionManagers currently only support original mode processing.</p>
 * <p>The ConnectionManager routes messages to various "Application"s based on
 * message type.  From the HL7 perspective, an Application is something that
 * does something with a message.</p>
 * @author  Bryan Tripp
 */
public class HL7SimpleServer extends HL7Service {
    private static final int SO_TIMEOUT = 3000;
    private static final int SO_BACKLOG = 20;
    private static final String TLS_ENABLED = "tls_enabled";
    private static final HapiLog log = HapiLogFactory.getHapiLog(HL7SimpleServer.class);
    private int port;
    private HL7ServerProperties props = null;

    /**
     * Creates a new instance of SimpleServer that listens
     * on the given port.  Exceptions are logged using ca.uhn.hl7v2.Log;
     */
    public HL7SimpleServer(HL7ServerProperties props, int port, LowerLayerProtocol llp, Parser parser) {
        super(parser, llp);
        this.port = port;
        this.props = props;
    }

    /**
     * Loop that waits for a connection and starts a ConnectionManager
     * when it gets one.
     */
    public void run() {
        try {
            ServerSocket ss;
            if (props.getProperty(TLS_ENABLED).equalsIgnoreCase("true")) {
                TLSSocketSupport socketSupport = new TLSSocketSupport();
                ss = socketSupport.getSecureServerSocket(port, SO_BACKLOG);
            } else {
                ss = new ServerSocket(port, SO_BACKLOG);
                log.info("+++++ SOCKET [non-TLS] CREATED (port = " + port + ") +++++");
            }
            ss.setSoTimeout(SO_TIMEOUT);
            log.info("HL7SimpleServer running on port " + ss.getLocalPort());
            while (isRunning()) {
                try {
                    Socket newSocket = ss.accept();
                    log.info("Accepted connection from " + newSocket.getInetAddress().getHostAddress());
                    Connection conn = new Connection(parser, this.llp, newSocket);
                    newConnection(conn);
                } catch (InterruptedIOException ie) {
                    //ignore - just timed out waiting for connection
                } catch (Exception e) {
                    log.error("Error while accepting connections: ", e);
                }
            }
            ss.close();
        } catch (Exception e) {
            log.error(e);
        } finally {
            this.stop();
        }
    }
}
