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

import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import org.apache.axis2.context.MessageContext;
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
     * @param gatewayConfig
     * @param log_message
     * @param messageContext
     */
    public XCARGAdhocQueryRequest(XConfigActor gatewayConfig, XLogMessage log_message, MessageContext messageContext) {
        super(gatewayConfig, log_message, messageContext);
    }

    /**
     *
     * @param request
     */
    @Override
    protected void validateRequest(OMElement request) {
        super.validateRequest(request);

        // Perform ATNA audit (FIXME - may not be best place).
        this.performAudit(
                XATNALogger.TXN_ITI38,
                request,
                null,
                XATNALogger.OutcomeIndicator.SUCCESS,
                XATNALogger.ActorType.REGISTRY);
    }

    /**
     *
     * @param queryRequest
     * @param responseOption
     */
    protected void processRequestWithPatientId(OMElement request, OMElement queryRequest, OMElement responseOption) throws XdsInternalException {
        XConfigActor registry = this.getLocalRegistry();
        this.addRequest(queryRequest, responseOption, registry.getName(), registry, true);
    }

    /**
     * 
     * @param queryRequest
     * @param responseOption
     * @param homeCommunityId
     * @throws XdsInternalException
     */
    protected void processTargetedHomeRequest(OMElement queryRequest, OMElement responseOption, String homeCommunityId) throws XdsInternalException {
        this.logInfo("HomeCommunityId", homeCommunityId);
        // See if this is for the local community.
        String localHomeCommunityId = this.getLocalHomeCommunityId();
        if (homeCommunityId.equals(localHomeCommunityId)) {  // Destined for the local home.
            this.logInfo("Note", "Going local for homeCommunityId: " + homeCommunityId);

            // XDSAffinityDomain option - get the local registry.
            XConfigActor localRegistry = this.getLocalRegistry();
            if (localRegistry != null) {
                // Add the local request.
                // Just use the registry name as the key (to avoid conflict with
                // local homeCommunityId testing).
                this.addRequest(queryRequest, responseOption, localRegistry.getName(), localRegistry, true);
            }

        } else {
            response.add_error(MetadataSupport.XDSUnknownCommunity,
                    "Do not understand homeCommunityId " + homeCommunityId,
                    this.getLocalHomeCommunityId(), log_message);
        }
    }

    /**
     *
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    protected XConfigActor getLocalRegistry() throws XdsInternalException {
        return this.getLocalRegistry("rg");
    }
}
