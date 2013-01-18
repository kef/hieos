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

import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.app.Responder;
import ca.uhn.hl7v2.llp.HL7Reader;
import ca.uhn.hl7v2.llp.HL7Writer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class Connection {

    private static final Logger log = Logger.getLogger(Connection.class);
    private Responder responder;
    private Socket socket;
    private Parser parser;
    private MessageRouter messageRouter;
    private HL7Writer hl7Writer;
    private HL7Reader hl7Reader;

    /**
     *
     * @param parser
     * @param llp
     * @param messageRouter
     * @param socket
     * @throws LLPException
     * @throws IOException
     */
    public Connection(Parser parser, LowerLayerProtocol llp, MessageRouter messageRouter, Socket socket)
            throws LLPException, IOException {
        this.parser = parser;
        this.messageRouter = messageRouter;
        this.hl7Writer = llp.getWriter(socket.getOutputStream());
        this.hl7Reader = llp.getReader(socket.getInputStream());
        this.socket = socket;
        this.responder = new Responder(parser);
    }

    /**
     * 
     * @return
     */
    public InetAddress getRemoteAddress() {
        return socket.getInetAddress();
    }

    /**
     *
     * @return
     */
    public int getRemotePort() {
        return socket.getPort();
    }

    /**
     *
     * @return
     */
    public Responder getResponder() {
        return this.responder;
    }

    /**
     *
     * @return
     */
    public boolean isSecure() {
        return (socket instanceof SSLSocket);
    }

    /**
     *
     * @return
     */
    public Parser getParser() {
        return this.parser;
    }

    /**
     *
     * @return
     */
    public HL7Reader getHl7Reader() {
        return hl7Reader;
    }

    /**
     *
     * @return
     */
    public HL7Writer getHl7Writer() {
        return hl7Writer;
    }

    /**
     *
     * @return
     */
    public Socket getSocket() {
        return socket;
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
     */
    public void close() {
        try {
            if (!socket.isClosed()) {
                hl7Reader.close();
                hl7Writer.close();
                socket.close();
            }
        } catch (Exception e) {
            log.error("Error while closing socket", e);
        }
    }
}
