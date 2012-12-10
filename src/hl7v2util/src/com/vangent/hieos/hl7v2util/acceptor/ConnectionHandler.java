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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.llp.HL7Reader;
import ca.uhn.hl7v2.llp.HL7Writer;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.MessageIDGenerator;
import ca.uhn.hl7v2.util.Terser;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class ConnectionHandler implements Runnable {

    private static final Logger log = Logger.getLogger(ConnectionHandler.class);
    private final ConnectionManager connectionManager;
    private final Connection connection;

    /**
     *
     * @param connectionManager
     * @param connection
     */
    public ConnectionHandler(ConnectionManager connectionManager, Connection connection) {
        this.connectionManager = connectionManager;
        this.connection = connection;
    }

    /**
     *
     */
    public void run() {
        log.info("ConnectionHandler Thread servicing connection (ConnectionHandler thread name = "
                + Thread.currentThread().getName()
                + ", remote ip = " + connection.getRemoteAddress()
                + ", remote port = " + connection.getRemotePort()
                + ")");
        try {
            // Get HL7Reader and get raw message.
            HL7Reader hl7Reader = connection.getHl7Reader();
            String incomingMessageString = hl7Reader.getMessage();

            // Process message and send out response.
            String outgoingMessageString = this.processMessage(incomingMessageString);
            if (outgoingMessageString != null) {

                // Get HL7Writer and send out raw message.
                HL7Writer hl7Writer = connection.getHl7Writer();
                hl7Writer.writeMessage(outgoingMessageString);
            }
            log.info("ConnectionHandler Thread closing connection (ConnectionHandler thread name = "
                    + Thread.currentThread().getName()
                    + ", remote ip = " + connection.getRemoteAddress()
                    + ", remote port = " + connection.getRemotePort()
                    + ")");
            connectionManager.removeConnection(connection);
        } catch (Exception ex) {
            log.error("Exception in ConnectionHandler", ex);
            log.info("ConnectionHandler Thread closing connection (ConnectionHandler thread name = "
                    + Thread.currentThread().getName()
                    + ", remote ip = " + connection.getRemoteAddress()
                    + ", remote port = " + connection.getRemotePort()
                    + ")");
            connectionManager.removeConnection(connection);
        }
    }

    /**
     * Processes an incoming message string and returns the response message
     * string. Message processing consists of parsing the message, finding an
     * appropriate Application and processing the message with it, and encoding
     * the response. Applications are chosen from among those registered using
     * <code>registerApplication</code>. The Parser is obtained from the
     * Connection associated with this Responder.
     * @param incomingMessageString 
     * @return
     * @throws HL7Exception
     */
    protected String processMessage(String incomingMessageString) throws HL7Exception {

        Message incomingMessageObject = null;
        String outgoingMessageString = null;
        Parser parser = connection.getParser();
        try {
            // DEBUG:
            log.info("[received] RAW HL7v2 Message:");
            log.info(incomingMessageString);

            log.info("... parsing");
            incomingMessageObject = parser.parse(incomingMessageString);
            log.info("... parsing complete");
            
            // DEBUG:
            log.info("... XML parsing");
            Parser xmlParser = new DefaultXMLParser();
            String xmlEncodedMessage = xmlParser.encode(incomingMessageObject);
            log.info("... XML parsing complete");
            log.info("[received] XML Encoded Message:");
            log.info(xmlEncodedMessage);
        } catch (HL7Exception e) {
            // TODO this may also throw an Exception, which hides the
            // previous one.
            log.error("Exception in ConnectionHandler", e);
            outgoingMessageString = logAndMakeErrorMessage(e,
                    parser.getCriticalResponseData(incomingMessageString),
                    parser, parser.getEncoding(incomingMessageString));
            //for (Object app : apps) {
            //	if (app instanceof ApplicationExceptionHandler) {
            //		ApplicationExceptionHandler aeh = (ApplicationExceptionHandler) app;
            //		outgoingMessageString = aeh.processException(
            //				incomingMessageString, outgoingMessageString, e);
            //	}
            //}
        }

        if (outgoingMessageString == null) {
            try {
                // optionally check integrity of parse
                //try {
                //	if (checkWriter != null)
                //		checkParse(incomingMessageString,
                //				incomingMessageObject, parser);
                //} catch (IOException e) {
                //	log.error("Unable to write parse check results to file", e);
                //}

                // message validation (in terms of optionality, cardinality)
                // would go here ***

                MessageRouter messageRouter = connection.getMessageRouter();
                if (messageRouter.getMessageHandler(incomingMessageObject) == null) {
                    throw new HL7Exception("Message Handler does not exist for inbound message");
                } else if (messageRouter.canProcess(connection, incomingMessageObject)) {
                    Message response = messageRouter.processMessage(connection, incomingMessageObject);

                    if (response == null) {
                        throw new HL7Exception("MessageRouter failed to return a response message from 'processMessage'");
                    }

                    // Here we explicitly use the same encoding as that of the
                    // inbound message - this is important with GenericParser, which
                    // might use a different encoding by default
                    outgoingMessageString = parser.encode(response, parser.getEncoding(incomingMessageString));
                }
            } catch (Exception e) {
                outgoingMessageString = logAndMakeErrorMessage(e,
                        (Segment) incomingMessageObject.get("MSH"), parser,
                        parser.getEncoding(incomingMessageString));
            }
        }

        // DEBUG:
        if (outgoingMessageString != null) {
            log.info("[response] RAW HL7v2 Message:");
            log.info(outgoingMessageString);
            Message outgoingMessageObject = parser.parse(outgoingMessageString);
            Parser xmlParser = new DefaultXMLParser();
            String xmlEncodedMessage = xmlParser.encode(outgoingMessageObject);
            log.info("[response] XML Encoded Message:");
            log.info(xmlEncodedMessage);
        }
        return outgoingMessageString;
    }

    /**
     * Logs the given exception and creates an error message to send to the
     * remote system.
     *
     * @param e
     * @param inHeader
     * @param encoding
     *            The encoding for the error message. If <code>null</code>, uses
     *            default encoding
     * @param p
     * @return
     * @throws HL7Exception
     */
    public static String logAndMakeErrorMessage(Exception e, Segment inHeader,
            Parser p, String encoding) throws HL7Exception {

        //log.error("Attempting to send error message to remote system.", e);

        // create error message ...
        String errorMessage = null;
        try {
            Message out = DefaultApplication.makeACK(inHeader);
            Terser t = new Terser(out);

            // copy required data from incoming message ...
            try {
                t.set("/MSH-10", MessageIDGenerator.getInstance().getNewID());
            } catch (IOException ioe) {
                throw new HL7Exception("Problem creating error message ID: "
                        + ioe.getMessage());
            }

            // populate MSA ...
            t.set("/MSA-1", "AE"); // should this come from HL7Exception
            // constructor?
            t.set("/MSA-2", Terser.get(inHeader, 10, 0, 1, 1));
            String excepMessage = e.getMessage();
            if (excepMessage != null) {
                t.set("/MSA-3",
                        excepMessage.substring(0,
                        Math.min(80, excepMessage.length())));
            }

            /*
             * Some earlier ACKs don't have ERRs, but I think we'll change this
             * within HAPI so that there is a single ACK for each version (with
             * an ERR).
             */
            // see if it's an HL7Exception (so we can get specific information)
            // ...
            if (e.getClass().equals(HL7Exception.class)) {
//				Segment err = (Segment) out.get("ERR");
                // ((HL7Exception) e).populate(err); // FIXME: this is broken,
                // it relies on the database in a place where it's not available
            } else {
                t.set("/ERR-1-4-1", "207");
                t.set("/ERR-1-4-2", "Application Internal Error");
                t.set("/ERR-1-4-3", "HL70357");
            }

            if (encoding != null) {
                errorMessage = p.encode(out, encoding);
            } else {
                errorMessage = p.encode(out);
            }

        } catch (IOException ioe) {
            throw new HL7Exception(
                    "IOException creating error response message: "
                    + ioe.getMessage(),
                    HL7Exception.APPLICATION_INTERNAL_ERROR);
        }
        return errorMessage;
    }
}
