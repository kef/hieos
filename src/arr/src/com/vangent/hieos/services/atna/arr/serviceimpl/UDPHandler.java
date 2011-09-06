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

import com.vangent.hieos.xutil.jms.JMSHandler;
import com.vangent.hieos.xutil.socket.ServerProperties;
import java.net.DatagramPacket;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class UDPHandler implements Runnable {

    private static final Logger log = Logger.getLogger(UDPHandler.class);
    private DatagramPacket clientPacket;
    private ServerProperties props;
    private final static String JMS_FACTORY = "jms_queue_mgr";
    private final static String JMS_QUEUE = "jms_queue";
    private final static String JMS_FACTORY_CLASS = "jms_factory_class";
    private final static String JMS_FACTORY_URL = "jms_url";

    /**
     *
     * @param clientPacket
     * @param props
     */
    UDPHandler(DatagramPacket clientPacket, ServerProperties props) {
        this.clientPacket = clientPacket;
        this.props = props;
        log.info("UDPHandler Instantiated ");
    }

    /**
     * Do the actual work - Write message to the ATNA Queue
     * 
     */
    public void run() {
        // Instantiate the message handler
        String jmsFactory = props.getProperty(JMS_FACTORY);
        String jmsDestination = props.getProperty(JMS_QUEUE);
        String jmsFactoryClass = props.getProperty(JMS_FACTORY_CLASS);
        String jmsFactoryURL = props.getProperty(JMS_FACTORY_URL);
        JMSHandler jmsHandler = new JMSHandler(jmsFactory, jmsDestination);

        // Extract the data from the packet
        String clientIP = clientPacket.getAddress().getHostAddress();
        int clientPort = clientPacket.getPort();
        String message = new String(clientPacket.getData()).trim();

        try {
            // Populate the Audit Queue Object
            HashMap<String, Object> auditMap = new HashMap<String, Object>();
            auditMap.put("protocol", "UDP");
            auditMap.put("clientIP", clientIP);
            auditMap.put("clientPort", clientPort);
            auditMap.put("message", message);
            if (log.isDebugEnabled()) {
                log.debug("UDP MESSAGE RECEIVED FROM CLIENT: " + clientIP + ":" + clientPort + " Length: " + message.length());
                log.debug(message);
            }

            //Place Audit Message on AuditMessageQueue
            jmsHandler.createConnectionFactoryFromProperties(jmsFactoryClass, jmsFactoryURL);
            jmsHandler.createJMSSession();
            jmsHandler.sendMessage(auditMap);

        } catch (Exception ex) {
            log.error("Error Audit Message Not Written to Queue: ", ex);
            log.error("Audit Message Protocol: " + "UDP");
            log.error("Audit Message Client IP: " + clientIP);
            log.error("Audit Message Client Port: " + clientPort);
            log.error("Audit Message XML: " + message);
            ex.printStackTrace();
        } finally {
            jmsHandler.close();
        }

    }
}
