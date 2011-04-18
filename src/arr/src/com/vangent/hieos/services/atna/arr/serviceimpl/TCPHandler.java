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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class TCPHandler implements Runnable {

    private static final Logger log = Logger.getLogger(TCPHandler.class);
    private Socket clientConnection;
    private ServerProperties props;

    private final static String JMS_FACTORY = "jms_queue_mgr";
    private final static String JMS_QUEUE = "jms_queue";
    private final static String JMS_FACTORY_CLASS = "jms_factory_class";
    private final static String JMS_FACTORY_URL = "jms_url";

    /**
     *
     * @param clientConnection
     * @param props
     */
    TCPHandler(Socket clientConnection, ServerProperties props) {
        this.clientConnection = clientConnection;
        this.props = props;
        log.info("TCPHandler Instantiated ");
    }

    /**
     *  Performs the work - Writes message to Audit Queue
     * 
     */
    public void run() {
        InputStreamReader isr = null;
        BufferedReader br = null;

        // Instantiate the message handler
        String jmsFactory = props.getProperty(JMS_FACTORY);
        String jmsDestination = props.getProperty(JMS_QUEUE);
        String jmsFactoryClass = props.getProperty(JMS_FACTORY_CLASS);
        String jmsFactoryURL = props.getProperty(JMS_FACTORY_URL);
        JMSHandler jmsHandler = new JMSHandler(jmsFactory, jmsDestination);

        HashMap<String, Object> auditMap = new HashMap<String, Object>();
        auditMap.put("protocol", "TCP");
        StringBuffer message = new StringBuffer();

        try {
            // read and service request
            log.info("Processing TCP Message from " + clientConnection.getInetAddress().getHostAddress());
            isr = new InputStreamReader(clientConnection.getInputStream());
            br = new BufferedReader(isr);
            String inputLine = null;

            // Read message from client
            while ((inputLine = br.readLine()) != null) {
                //log.info("TCPMessage Line: " + inputLine);
                message.append(inputLine);
            }
            log.info("TCP Message From Client: ");
            log.info(message);

            auditMap.put("clientIP", clientConnection.getInetAddress().getHostAddress());
            auditMap.put("clientPort",clientConnection.getLocalPort());
            auditMap.put("message", message);

            //Parse, Process and Save the Audit Message
            jmsHandler.createConnectionFactoryFromProperties(jmsFactoryClass, jmsFactoryURL);
            jmsHandler.createJMSSession();
            jmsHandler.sendMessage(auditMap);

        } catch (Exception ex) {
            log.error("Error Audit Message Not Written to Queue: ", ex);
            log.error("Audit Message Protocol: " + "TCP/TLS");
            log.error("Audit Message Client IP: " + clientConnection.getInetAddress().getHostAddress());
            log.error("Audit Message Client Port: " + clientConnection.getLocalPort());
            log.error("Audit Message XML: " + message);
            ex.printStackTrace();
        }finally {
            // Close Streams & Connection
            jmsHandler.close();
            try {
                isr.close();
                br.close();
                clientConnection.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }

    }
}
