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
package com.vangent.hieos.hl7v2util.client;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import com.vangent.hieos.hl7v2util.exception.HL7v2UtilException;
import com.vangent.hieos.xutil.socket.TLSSocketSupport;
import java.io.IOException;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7v2Client {

    private static final Logger log = Logger.getLogger(HL7v2Client.class);
    private HL7v2Endpoint endpoint;

    /**
     *
     * @param endpoint
     * @throws HL7v2UtilException
     */
    public HL7v2Client(String endpoint) throws HL7v2UtilException {
        this.endpoint = new HL7v2Endpoint(endpoint);
    }

    /**
     * 
     * @param outMessage
     * @return
     * @throws HL7v2UtilException
     */
    // FIXME: Create wrapper for "Message" result.
    public Message sendMessage(Message outMessage) throws HL7v2UtilException {
        Socket clientSocket = null;
        Connection connection = null;
        try {
            // Create a socket (using endpoint provided in constructor).
            clientSocket = this.getClientSocket();

            LowerLayerProtocol llp = LowerLayerProtocol.makeLLP(); // The transport protocol
            PipeParser parser = new PipeParser(); // The message parser
            connection = new Connection(parser, llp, clientSocket);

            // The initiator is used to transmit unsolicited messages
            Initiator initiator = connection.getInitiator();
            connection.activate();
            
            // FIXME: Change to DEBUG
            if (log.isInfoEnabled()) {
                Parser xmlParser = new DefaultXMLParser();
                log.info("[outbound] RAW HL7v2 Message:\n" + outMessage);
                log.info("[outbound] XML Encoded Message:\n" + xmlParser.encode(outMessage));
            }

            // Send and receive response.
            Message response = initiator.sendAndReceive(outMessage);

            // FIXME: Change to DEBUG
            if (log.isInfoEnabled()) {
                Parser xmlParser = new DefaultXMLParser();
                log.info("[outbound response] RAW HL7v2 Message:\n" + response);
                log.info("[outbound response] XML Encoded Message:\n" + xmlParser.encode(response));
            }

            // Close connection.
            connection.close();

            // Return response.
            return response;
        } catch (LLPException ex) {
            log.error("LLPException: ", ex);
            throw new HL7v2UtilException("Exception sending HL7v2 outbound message", ex);
        } catch (IOException ex) {
            log.error("IOException: ", ex);
            throw new HL7v2UtilException("Exception sending HL7v2 outbound message", ex);
        } catch (HL7Exception ex) {
            log.error("HL7Exception: ", ex);
            throw new HL7v2UtilException("Exception sending HL7v2 outbound message", ex);
        } finally {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        }
    }

    /**
     *
     * @return
     * @throws HL7v2UtilException
     */
    private Socket getClientSocket() throws HL7v2UtilException {
        Socket socket;

        // See if TLS is enabled.
        if (endpoint.isTlsEnabled()) {
            // Create listener socket (TLS).
            TLSSocketSupport socketSupport = new TLSSocketSupport();
            try {
                socket = socketSupport.getSecureClientSocket(
                        endpoint.getIpAddressOrHostName(), endpoint.getPort());
            } catch (Exception ex) {
                log.error("Could not open TLS socket for HL7v2 outbound connection", ex);
                throw new HL7v2UtilException("Could not open TLS socket for HL7v2 outbound connection", ex);
            }
        } else {
            try {
                // Create listener socket (no TLS).
                socket = new Socket(endpoint.getIpAddressOrHostName(), endpoint.getPort());
            } catch (IOException ex) {
                log.error("Could not open socket for HL7v2 outbound connection", ex);
                throw new HL7v2UtilException("Could not open socket for HL7v2 outbound connection", ex);
            }
        }
        return socket;
    }
}
