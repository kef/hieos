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
package com.vangent.hieos.hl7v2util.acceptor.config;

import com.vangent.hieos.hl7v2util.acceptor.impl.MessageHandler;
import com.vangent.hieos.hl7v2util.exception.HL7v2UtilException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class MessageHandlerConfig implements ConfigItem {

    private final static Logger log = Logger.getLogger(MessageHandlerConfig.class);
    private static String CLASS_NAME = "class";
    private static String MESSAGE_TYPE = "message-type";
    private static String TRIGGER_EVENT = "trigger-event";
    private String className;
    private String messageType;
    private String triggerEvent;
    private MessageHandler messageHandler;

    /**
     *
     * @return
     */
    public String getClassName() {
        return className;
    }

    /**
     *
     * @return
     */
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     *
     * @return
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * 
     * @return
     */
    public String getTriggerEvent() {
        return triggerEvent;
    }

    /**
     * 
     * @param hc
     * @param acceptorConfig
     * @throws HL7v2UtilException
     */
    public void load(HierarchicalConfiguration hc, AcceptorConfig acceptorConfig) throws HL7v2UtilException {
        this.messageType = hc.getString(MESSAGE_TYPE);
        this.triggerEvent = hc.getString(TRIGGER_EVENT);
        this.className = hc.getString(CLASS_NAME);
        log.info("... className = " + this.className);

        // Get an instance of the message handler.
        this.messageHandler = (MessageHandler) ConfigHelper.loadClassInstance(this.className);
    }
}
