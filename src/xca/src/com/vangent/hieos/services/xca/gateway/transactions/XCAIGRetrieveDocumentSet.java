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
package com.vangent.hieos.services.xca.gateway.transactions;

import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEventHelper;
import com.vangent.hieos.xutil.atna.ATNAAuditEventRetrieveDocumentSet;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.atna.XATNALogger;

import org.apache.axis2.context.MessageContext;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCAIGRetrieveDocumentSet extends XCARetrieveDocumentSet {

    private final static Logger logger = Logger.getLogger(XCAIGRetrieveDocumentSet.class);

    /**
     * 
     * @param log_message
     * @param messageContext
     */
    public XCAIGRetrieveDocumentSet(XLogMessage log_message, MessageContext messageContext) {
        super(log_message, messageContext);
    }

    /**
     * Make sure that the xdsb namespace is in order.
     *
     * @param request  The root of the XML request.
     */
    @Override
    protected void validateRequest(OMElement request) {
        super.validateRequest(request);

        // Perform ATNA audit (FIXME - may not be best place).
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                ATNAAuditEventRetrieveDocumentSet auditEvent = ATNAAuditEventHelper.getATNAAuditEventRetrieveDocumentSet(request);
                auditEvent.setActorType(ATNAAuditEvent.ActorType.INITIATING_GATEWAY);
                auditEvent.setTransaction(ATNAAuditEvent.IHETransaction.ITI43);
                auditEvent.setAuditEventType(ATNAAuditEvent.AuditEventType.EXPORT);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            // FIXME?:
        }
    }

    /**
     *
     * @param docRequest
     * @param homeCommunityId
     * @param gatewayConfig
     * @throws XdsInternalException
     */
    @Override
    protected void processRemoteCommunityRequest(OMElement docRequest, String homeCommunityId, XConfigActor gatewayConfig) throws XdsInternalException {
        this.addRequest(docRequest, homeCommunityId, gatewayConfig, false);
    }
}
