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
package com.vangent.hieos.xutil.services.framework;

import com.vangent.hieos.xutil.soap.SoapActionFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.log4j.Logger;

/**
 * This class is the main processor of SOAP messages.  Most processing is handled
 * by the axis2 framework ... we do a little setup and book keeping here.
 *
 * @author Bernie Thuman
 */
public class XMLInOutMessageReceiver extends RawXMLINOutMessageReceiver {

    private final static Logger logger = Logger.getLogger(XMLInOutMessageReceiver.class);

    @Override
    public void receive(final MessageContext messageCtx) throws AxisFault {
        // FIXME: enabling the next line should work, but causes havoc!!
        //messageCtx.setProperty(DO_ASYNC, Boolean.TRUE);
        super.receive(messageCtx);
    }

    /**
     *
     * @param messageContext
     * @param responseMessageContext
     * @throws AxisFault
     */
    @Override
    public void invokeBusinessLogic(MessageContext messageContext, MessageContext responseMessageContext)
            throws AxisFault {
        this.setResponseAction(messageContext, responseMessageContext);
        super.invokeBusinessLogic(messageContext, responseMessageContext);
    }

    /**
     *
     * @param messageContext
     * @param responseMessageContext
     */
    private void setResponseAction(MessageContext messageContext, MessageContext responseMessageContext) {
        String inAction = messageContext.getWSAAction();
        String outAction = SoapActionFactory.getResponseAction(inAction);
        if (outAction == null) {
            responseMessageContext.setFailureReason(new Exception("Unknown action <" + inAction + ">"));
            return;
        }
        responseMessageContext.setWSAAction(outAction);
    }
}
