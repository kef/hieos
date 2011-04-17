/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.jms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * This class provides methods to write messages to a JMS Queue or Topic
 * It supports JMS Connections using a JMS connection pool or native JMS driver
 *
 * @author Adeola Odunlami
 */
public class JMSHandler {

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private Context ctx;
    private String jmsFactory;
    private String jmsDestination;

    private final static Logger logger = Logger.getLogger(JMSHandler.class);

    public JMSHandler() {
    }

    /**
     * Initializes the JMS Handler with the Connection Factory and Destination JNDI lookup values
     *
     * @param jmsFactory
     * @param jmsDestination
     */
    public JMSHandler(String jmsFactory, String jmsDestination) {
        this.jmsFactory = jmsFactory;
        this.jmsDestination = jmsDestination;
    }

    /**
     * Wrapper method to send a Hash Map to a JMS destination
     *
     * @param message
     * @throws Exception
     */
    public void sendMessageToQueue(HashMap message) throws Exception {
        try {
            createConnectionFactoryFromPool();
            createJMSSession();
            sendMessage(message);
        } catch (JMSException jex) {
            logger.error(jex);
            throw new Exception(jex);
        } finally {
            close();
        }
    }

    /**
     * Creates a JMS connection factory using a JNDI lookup
     * @throws NamingException
     */
    public void createConnectionFactoryFromPool() throws NamingException {
        if (logger.isTraceEnabled())
            logger.trace("Finding Connection Factory With JMS Pool");
        ctx = new InitialContext();
        connectionFactory = (ConnectionFactory) ctx.lookup(jmsFactory);
        if (logger.isTraceEnabled())
            logger.trace("Connection Factory found");
    }

    /**
     * Creates a JMS Connection when there is no application server or JMS
     * connection pool. The Connection class and URL are retrieved from a properties file.
     *
     */
    public void createConnectionFactoryFromProperties(String jmsFactoryClass, String jmsFactoryURL)
            throws NamingException {
        if (logger.isTraceEnabled())
            logger.trace("Finding Connection Factory  - Using JMS Properties");
        Properties contextProps = new Properties();
        contextProps.setProperty(Context.INITIAL_CONTEXT_FACTORY, jmsFactoryClass);
        contextProps.setProperty(Context.PROVIDER_URL, jmsFactoryURL);
        ctx = new InitialContext(contextProps);
        connectionFactory = (ConnectionFactory) ctx.lookup(jmsFactory);
         if (logger.isTraceEnabled())
            logger.trace("Connection Factory found - Using JMS Properties");
    }

    /**
     * Creates a JMS session for putting messages on a JMS destination
     * @throws JMSException
     */
    public void createJMSSession() throws JMSException {
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Creates a JMS destination (queue or topic) using a JNDI lookup
     * @return
     * @throws JMSException
     * @throws NamingException
     */
    private MessageProducer createDestination() throws JMSException, NamingException {
        destination = (Destination) ctx.lookup(jmsDestination);
        if (logger.isTraceEnabled())
            logger.trace("JMS Destination found");

        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        return producer;
    }

    /**
     * Sends a Hash Map object to the JMS destination
     *
     * @param message
     * @throws JMSException
     * @throws NamingException
     */
    public void sendMessage(HashMap message) throws JMSException, NamingException {
        MessageProducer producer = createDestination();
        ObjectMessage messageObj = session.createObjectMessage((Serializable) message);
        if (logger.isTraceEnabled())
            logger.trace("Sending Message: ");
        producer.send(messageObj);
        if (logger.isTraceEnabled())
            logger.trace("Message sent");
    }

    /**
     * Sends a Serializable Java Object to a JMS destination
     *
     * @param message
     * @throws JMSException
     * @throws NamingException
     */
    public void sendMessage(Object message) throws JMSException, NamingException {
        MessageProducer producer = createDestination();
        ObjectMessage messageObj = session.createObjectMessage((Serializable) message);
        if (logger.isTraceEnabled())
            logger.trace("Sending Message: ");
        producer.send(messageObj);
        if (logger.isTraceEnabled())
            logger.trace("Message sent");
    }

    /**
     * Sends a Btyte string to a JMS destination
     *
     * @param message
     * @throws JMSException
     * @throws NamingException
     */
    public void sendMessage(Byte message) throws JMSException, NamingException {
        MessageProducer producer = createDestination();
        BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeByte(message);
        if (logger.isTraceEnabled())
            logger.trace("Sending Message: ");
        producer.send(bytesMessage);
        if (logger.isTraceEnabled())
            logger.trace("Message sent");
    }

    /**
     * Sends a String to a JMS destination
     *
     * @param message
     * @throws JMSException
     * @throws NamingException
     */
    public void sendMessage(String message) throws JMSException, NamingException {
        MessageProducer producer = createDestination();
        TextMessage textMessage = session.createTextMessage(message);
        if (logger.isTraceEnabled())
            logger.trace("Sending Message: " + textMessage.getText());
        producer.send(textMessage);
        if (logger.isTraceEnabled())
            logger.trace("Message sent");
    }

    /**
     * Closes the JMS Connection and Session. The client is responsible for
     * calling the close method to close the JMS Objects
     */
    public void close() {

        if (session != null) {
            try {
                session.close();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }
}