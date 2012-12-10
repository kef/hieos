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
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.hl7v2util.config.AcceptorConfig;
import com.vangent.hieos.hl7v2util.config.MessageHandlerConfig;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class MessageRouter {
    // FIXME: Possibly remove synchronization.

    private static final Logger log = Logger.getLogger(MessageRouter.class);
    private Map<String, MessageHandler> messageHandlers;
    private AcceptorConfig acceptorConfig = null;

    /**
     *
     */
    public MessageRouter() {
        this.messageHandlers = new HashMap<String, MessageHandler>();
    }

    /**
     *
     * @param acceptorConfig
     */
    public MessageRouter(AcceptorConfig acceptorConfig) {
        this.acceptorConfig = acceptorConfig;
        this.messageHandlers = new HashMap<String, MessageHandler>();
        for (MessageHandlerConfig messageHandlerConfig : acceptorConfig.getMessageHandlerConfigs()) {
            String messageType = messageHandlerConfig.getMessageType();
            String triggerEvent = messageHandlerConfig.getTriggerEvent();
            MessageHandler messageHandler = messageHandlerConfig.getMessageHandler();
            this.messageHandlers.put(getKey(messageType, triggerEvent), messageHandler);
        }
    }

    /**
     *
     * @return
     */
    public AcceptorConfig getAcceptorConfig() {
        return acceptorConfig;
    }

    /**
     *
     * @param connection
     * @param in
     * @return
     */
    public boolean canProcess(Connection connection, Message in) throws ApplicationException {
        try {
            MessageHandler messageHandler = this.getMessageHandler(in);
            return messageHandler != null ? messageHandler.canProcess(connection, in) : false;
        } catch (HL7Exception e) {
            throw new ApplicationException("Error internally routing message: "
                    + e.toString(), e);
        }
    }

    /**
     *
     * @param connection
     * @param in
     * @return
     * @throws ApplicationException
     */
    public Message processMessage(Connection connection, Message in) throws ApplicationException {
        Message out;
        try {
            MessageHandler messageHandler = this.getMessageHandler(in);
            out = messageHandler.processMessage(connection, in);
        } catch (HL7Exception e) {
            throw new ApplicationException("Error internally routing message: "
                    + e.toString(), e);
        }
        return out;
    }

    /**
     *
     * @param messageType
     * @param triggerEvent
     * @param handler
     */
    public synchronized void registerMessageHandler(String messageType,
            String triggerEvent, MessageHandler handler) {
        this.messageHandlers.put(getKey(messageType, triggerEvent), handler);
    }

    /**
     *
     * @param message
     * @return
     * @throws HL7Exception
     */
    public MessageHandler getMessageHandler(Message message)
            throws HL7Exception {
        Terser t = new Terser(message);
        String messageType = t.get("/MSH-9-1");
        String triggerEvent = t.get("/MSH-9-2");
        return this.getMessageHandler(messageType, triggerEvent);
    }

    /**
     *
     * @param messageType
     * @param triggerEvent
     * @return
     */
    private synchronized MessageHandler getMessageHandler(String messageType, String triggerEvent) {
        MessageHandler messageHandler = null;
        MessageHandler o = this.messageHandlers.get(getKey(messageType, triggerEvent));
        if (o == null) {
            o = this.messageHandlers.get(getKey(messageType, "*"));
        }
        if (o == null) {
            o = this.messageHandlers.get(getKey("*", triggerEvent));
        }
        if (o == null) {
            o = this.messageHandlers.get(getKey("*", "*"));
        }
        if (o != null) {
            messageHandler = o;
        }
        return messageHandler;
    }

    /**
     * Creates reproducible hash key.
     */
    private String getKey(String messageType, String triggerEvent) {
        // create hash key string by concatenating type and trigger event
        return messageType + "|" + triggerEvent;
    }
}
