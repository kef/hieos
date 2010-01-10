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
package com.vangent.hieos.services.xds.registry.transactions.hl7v2;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import java.net.InetAddress;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public abstract class HL7Application implements Application {
    private static final Logger log = Logger.getLogger(HL7Application.class);

    private HL7ServerProperties props;

    /**
     *
     * @param props
     */
    public HL7Application(HL7ServerProperties props) {
        this.props = props;
    }

    /**
     *
     * @param inMessage
     * @return
     * @throws ApplicationException
     * @throws HL7Exception
     */
    public Message processMessage(Message inMessage) throws ApplicationException, HL7Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param inMessage
     * @return
     */
    public boolean canProcess(Message inMessage) {
        return true;
    }

    /**
     *
     * @param inMessage
     * @param socket
     * @return
     * @throws ApplicationException
     * @throws HL7Exception
     */
    public Message processMessage(Message inMessage, Socket socket) throws ApplicationException, HL7Exception {
        log.info("++++++++++++++++++++++ Inbound HL7 Message ++++++++++++++++++++++");
        log.info("Sender IP = " + this.getRemoteIPAddress(socket));
        log.info("HL7 Version = " + inMessage.getVersion());
        DefaultXMLParser xmlParser = new DefaultXMLParser();
        String xmlEncodedMessage = xmlParser.encode(inMessage);
        String encodedMessage = new PipeParser().encode(inMessage);
        log.info("Raw HL7 Message:\n" + encodedMessage);
        log.info("XML Encoded Message:\n" + xmlEncodedMessage);
        return this.handleMessage(inMessage, socket);
    }

    /**
     * Must be implemented by subclass.
     *
     * @param inMessage
     * @param socket
     * @return
     */
    protected abstract Message handleMessage(Message inMessage, Socket socket) throws ApplicationException, HL7Exception;

    /**
     * Return the IP address for the sender.
     *
     * @param socket The socket that accepted the request.
     * @return IP address as a string.
     */
    protected String getRemoteIPAddress(Socket socket) {
        InetAddress inet = socket.getInetAddress();
        return inet.getHostAddress();
    }

    /**
     *
     * @return
     */
    protected HL7ServerProperties getProperties() {
        return this.props;
    }
}
