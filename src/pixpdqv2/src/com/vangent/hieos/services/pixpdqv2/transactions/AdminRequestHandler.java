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
package com.vangent.hieos.services.pixpdqv2.transactions;

import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class AdminRequestHandler extends RequestHandler {

    private final static Logger logger = Logger.getLogger(AdminRequestHandler.class);

    // Type type of message received.
    /**
     *
     */
    public enum MessageType {

        /**
         *
         */
        GetConfig
    };

    /**
     *
     * @param log_message
     */
    public AdminRequestHandler(XLogMessage log_message) {
        super(log_message);
    }

    /**
     *
     * @param request
     * @param messageType
     * @return
     * @throws SOAPFaultException
     */
    public OMElement run(OMElement request, MessageType messageType) throws SOAPFaultException {
        log_message.setPass(true);  // Hope for the best.
        switch (messageType) {
            case GetConfig:
                // FIXME: Implement...
                throw new SOAPFaultException("Not yet implemented!");
        }
        return null;
    }
}
