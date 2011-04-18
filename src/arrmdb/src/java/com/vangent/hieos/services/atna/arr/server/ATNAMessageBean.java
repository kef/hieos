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
package com.vangent.hieos.services.atna.arr.server;

import com.vangent.hieos.services.atna.arr.support.AuditException;
import com.vangent.hieos.services.atna.arr.support.AuditMessageHandler;
import java.util.HashMap;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
@MessageDriven()
public class ATNAMessageBean implements MessageListener {

    private static final String beginMessage = "<AuditMessage";
    private final static Logger logger = Logger.getLogger(ATNAMessageBean.class);

    public ATNAMessageBean() {
    }

    /**
     *
     * @param message
     */
    public void onMessage(Message message) {
        String xmlMessage = null;
        String clientIP = null;
        Integer clientPort = null;
        String protocol = null;
        String errorMessage = null;
        // Initialize the AuditMessagehandler used to store both Audit Data & Audit Log
        AuditMessageHandler handler = new AuditMessageHandler();

        try {
            if (logger.isTraceEnabled()) {
                logger.trace("QUEUE Message ID = " + message.getJMSMessageID());
            }

            if (message instanceof ObjectMessage) {
                logger.info("ATNA ObjectMessage Retrieved From Queue");
                ObjectMessage m = (ObjectMessage) message;
                HashMap auditMap = (HashMap) m.getObject();
                protocol = (String) auditMap.get("protocol");
                clientIP = (String) auditMap.get("clientIP");
                clientPort = (Integer) auditMap.get("clientPort");

                Object messageObject = auditMap.get("message");
                if (messageObject instanceof String) {
                    xmlMessage = (String) messageObject;
                } else if (messageObject instanceof StringBuffer) {
                    StringBuffer stringBuffer = (StringBuffer) messageObject;
                    xmlMessage = stringBuffer.toString();
                } else {
                    logger.info("AUDIT MESSAGE is neither a String or StringBuffer: " + messageObject);
                    throw new AuditException("AUDIT MESSAGE is neither a String or StringBuffer: " + messageObject);
                }

                logger.info("AUDIT XML LOG: " + protocol + " : " + clientIP + " : " + clientPort + " : " + xmlMessage);
                // Remove the SYSLOG (RFC 5424) Header from the XML Message if present
                String auditMessage = parseMessage(xmlMessage);

                // Parse the XML Message and persist the Audit Data
                handler.createATNAMessage(auditMessage);
                handler.persistMessage();
            } else {
                logger.error("Received message of Invalid Class Type: " + message.getClass().getName());
            }
        } catch (Exception e) {
            logger.error(e.toString());
            errorMessage = e.toString();
            e.printStackTrace(System.err);
        } finally {
            try {
                if (errorMessage != null) {
                    logger.trace("SAVE AUDIT XML LOG WITH ERROR: " + clientIP + " : " + clientPort + " : " +
                            errorMessage + " : " + xmlMessage);
                }
                // Always save the raw XML that was transmitted and error message (if applicable)
                handler.createATNALog(clientIP, clientPort, protocol, xmlMessage, errorMessage);
                handler.persistLog();
            } catch (AuditException ex) {
                logger.error(ex);
                ex.printStackTrace(System.err);
            }
        }
    }

    /**
     * This method extracts the message from SYSLOG (RFC5424) header in the Audit Message
     *
     * @param inMessage
     * @return String - the audit data part of the Message
     */
    private String parseMessage(String inMessage) throws AuditException {
        // Check for minimum length before extracting message.
        if (inMessage.length() < 14) {
            throw new AuditException("Invalid Audit Message Received - Message too short");
        }
        // The actual audit data starts with <AuditMessage>
        int beginPosition = inMessage.indexOf(beginMessage);
        if (beginPosition < 0) {
            throw new AuditException("Invalid Audit Message Received - Does not begin with <AuditMessage");
        }
        String message = inMessage.substring(beginPosition, inMessage.length());
        String header = inMessage.substring(0, beginPosition - 1);
        logger.info("AUDIT HEADER FROM CLIENT: " + header);
        //logger.trace("AUDIT MESSAGE FROM CLIENT: " + message);
        return message;
    }
}
