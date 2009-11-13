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

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.MessageReceiver;

/**
 *
 * @author thumbe
 */
public class XDSRawXMLInOutMessageReceiver extends AbstractXDSRawXMLINoutMessageReceiver
        implements MessageReceiver {

    /**
     *
     * @param msgContext
     * @param newmsgContext
     */
    public void validate_action(MessageContext msgContext, MessageContext newmsgContext) {
        String in_action = msgContext.getWSAAction();

        String out_action = SoapActionFactory.getResponseAction(in_action);
        if (out_action == null) {
            newmsgContext.setFailureReason(new Exception("Unknown action <" + in_action + ">"));
            return;
        }
        newmsgContext.setWSAAction(out_action);
    }
}
