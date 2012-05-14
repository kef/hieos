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
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.ActorType;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.IHETransaction;
import com.vangent.hieos.xutil.atna.ATNAAuditEventHelper;
import com.vangent.hieos.xutil.atna.ATNAAuditEventQuery;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCARGAdhocQueryRequest extends XCAAdhocQueryRequest {

    private final static Logger logger = Logger.getLogger(XCARGAdhocQueryRequest.class);

    /**
     * 
     * @param log_message
     */
    public XCARGAdhocQueryRequest(XLogMessage log_message) {
        super(log_message);
    }

    /**
     *
     * @param request
     */
    @Override
    protected void validateRequest(OMElement request) {
        super.validateRequest(request);

        // Perform ATNA audit (FIXME - may not be best place).
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getATNAAuditEventRegistryStoredQuery(request);
                auditEvent.setActorType(ActorType.RESPONDING_GATEWAY);
                auditEvent.setTransaction(IHETransaction.ITI38);
                auditEvent.setAuditEventType(ATNAAuditEvent.AuditEventType.QUERY_PROVIDER);
                auditEvent.setHomeCommunityId(this.getLocalHomeCommunityId());
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            // FIXME?:
        }
    }

    /**
     *
     * @param queryRequest
     * @return
     */
    @Override
    protected boolean requiresHomeCommunityId(OMElement queryRequest) {
        // The responding gateway does not require a home community id on
        // query requests.
        return false;
    }

    /**
     * 
     * @param request
     * @param queryRequest
     * @param responseOption
     * @throws XdsInternalException
     */
    protected void processRequestWithPatientId(OMElement request, OMElement queryRequest, OMElement responseOption) throws XdsInternalException {
        // Just simply look at local registry.
        XConfigActor registry = this.getLocalRegistry();
        this.addRequest(queryRequest, responseOption, registry.getName(), registry, true);
    }

    /**
     *
     * @param queryRequest
     * @param responseOption
     * @param homeCommunityId
     * @param gatewayConfig
     * @throws XdsInternalException
     */
    protected void processRemoteCommunityRequest(OMElement queryRequest, OMElement responseOption, String homeCommunityId, XConfigActor gatewayConfig) throws XdsInternalException {
        // NOOP: for now ... could do chaining of gateways here.
    }
}
