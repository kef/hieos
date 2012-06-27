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
package com.vangent.hieos.services.pixpdq.transactions;

import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.xml.HL7V3SchemaValidator;
import com.vangent.hieos.xutil.exception.SOAPFaultException;

import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.exception.XMLSchemaValidatorException;

import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public abstract class RequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(RequestHandler.class);

    /**
     *
     */
    protected RequestHandler() {
        // Do nothing.
    }

    /**
     *
     * @param log_message
     */
    public RequestHandler(XLogMessage log_message) {
        this.log_message = log_message;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean getStatus() {
        return log_message.isPass();
    }

    /**
     *
     * @return
     */
    protected DeviceInfo getDeviceInfo() {
        return new DeviceInfo(this.getConfigActor());
    }

    /**
     * 
     * @param message
     * @throws SOAPFaultException
     */
    protected void validateHL7V3Message(HL7V3Message message) throws SOAPFaultException {
        try {
            HL7V3SchemaValidator.validate(message.getMessageNode(), message.getType());
        } catch (XMLSchemaValidatorException ex) {
            //log_message.setPass(false);
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            throw new SOAPFaultException(ex.getMessage());
        }
    }
}
