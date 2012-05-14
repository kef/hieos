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
package com.vangent.hieos.services.xcpd.gateway.controller;

import com.vangent.hieos.hl7v3util.client.XCPDGatewayClient;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;

import com.vangent.hieos.services.xcpd.gateway.framework.XCPDGatewayRequestHandler;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;

import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.concurrent.Callable;
import org.apache.axis2.context.MessageContext;

import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
/**
 *
 */
public class GatewayCallable implements Callable<GatewayResponse> {

    private final static Logger logger = Logger.getLogger(GatewayCallable.class);
    private XCPDGatewayRequestHandler requestHandler;
    private GatewayRequest request;
    private XLogMessage logMessage;

    /**
     *
     * @param request
     */
    public GatewayCallable(XCPDGatewayRequestHandler requestHandler, GatewayRequest request, XLogMessage logMessage) {
        this.requestHandler = requestHandler;
        this.request = request;
        this.logMessage = logMessage;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public GatewayResponse call() throws Exception {
        PRPA_IN201305UV02_Message message = this.request.getRequest();
        if (logMessage.isLogEnabled()) {
            logMessage.addOtherParam("CGPD REQUEST " + this.request.getVitals(),
                    message.getMessageNode().toString());
        }

        // Get parent thread's message context.
        MessageContext parentThreadMessageContext = this.request.getParentThreadMessageContext();

        // ATNA Audit:
        requestHandler.performAuditPDQQueryInitiator(ATNAAuditEvent.ActorType.INITIATING_GATEWAY,
                this.request.getRequest(),
                this.request.getEndpoint(),
                parentThreadMessageContext);

        // Make the call.
        XCPDGatewayClient client = new XCPDGatewayClient(request.getRGConfig());
        client.setParentThreadMessageContext(parentThreadMessageContext);
        GatewayResponse gatewayResponse = null;
        PRPA_IN201306UV02_Message queryResponse;
        try {
            queryResponse = client.findCandidatesQuery(message);
            if (logMessage.isLogEnabled()) {
                if (queryResponse.getMessageNode() != null) {
                    logMessage.addOtherParam("CGPD RESPONSE " + request.getVitals(),
                            queryResponse.getMessageNode().toString());
                } else {
                    logMessage.addErrorParam("CGPD RESPONSE " + request.getVitals(),
                            "NO RESPONSE FROM COMMUNITY");
                }
            }
            gatewayResponse = new GatewayResponse();
            gatewayResponse.setRequest(this.request);
            gatewayResponse.setResponse(queryResponse);
        } catch (Exception ex) {
            logger.error("XCPD EXCEPTION ... continuing " + request.getVitals(), ex);
            logMessage.addErrorParam("EXCEPTION " + request.getVitals(), ex.getMessage());
            // ***** Rethrow is needed otherwise Axis2 gets confused with Async.
            throw ex;  // Rethrow.
        }
        return gatewayResponse;
    }
}
