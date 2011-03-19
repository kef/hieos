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

import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

// Exceptions.
import com.vangent.hieos.xutil.exception.XdsInternalException;

// XConfig.
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;

// XATNA.
import com.vangent.hieos.xutil.atna.XATNALogger;

// Third party.
import org.apache.axis2.context.MessageContext;
import java.util.ArrayList;
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
     * @param gatewayConfig
     * @param log_message
     * @param messageContext
     */
    public XCAIGRetrieveDocumentSet(XConfigActor gatewayConfig, XLogMessage log_message, MessageContext messageContext) {
        super(gatewayConfig, log_message, messageContext);
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
        this.performAudit(
                XATNALogger.TXN_ITI43,
                request,
                null,
                XATNALogger.OutcomeIndicator.SUCCESS,
                XATNALogger.ActorType.REPOSITORY);
    }

    /**
     *
     * @param request
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    protected void prepareValidRequests(OMElement request) throws XdsInternalException {

        // Loop through each DocumentRequest
        ArrayList<OMElement> docRequests = MetadataSupport.decendentsWithLocalName(request, "DocumentRequest");
        for (OMElement docRequest : docRequests) {

            // Get the home community in the request.
            OMElement homeCommunityNode = MetadataSupport.firstChildWithLocalName(docRequest, "HomeCommunityId");
            if (homeCommunityNode == null) {

                // home community id is missing in this doc request.
                response.add_error(MetadataSupport.XDSMissingHomeCommunityId,
                        "homeCommunityId missing or empty",
                        this.getLocalHomeCommunityId(), log_message);
            } else {
                // Now retrieve the home community id from the node.
                String homeCommunityId = homeCommunityNode.getText();
                if (homeCommunityId == null || homeCommunityId.equals("")) {

                    // No home community id found.
                    response.add_error(MetadataSupport.XDSMissingHomeCommunityId,
                            "homeCommunityId missing or empty",
                            this.getLocalHomeCommunityId(), log_message);
                } else {
                    // Now determine if we know about this home community

                    // Is this request targeted for the local community?
                    XConfigObject homeCommunityConfig = XConfig.getInstance().getHomeCommunityConfig();

                    if (homeCommunityConfig.getUniqueId().equals(homeCommunityId)) {
                        // This is destined for the local community.
                        XConfigActor repositoryConfig = this.getRepositoryConfigBasedOnDocRequest(docRequest);
                        if (repositoryConfig != null) {
                            // This request is good (targeted for local community repository).
                            this.addRequest(docRequest, repositoryConfig.getUniqueId(), repositoryConfig, true);
                        }

                    } else {
                        // See if we know about a remote gateway that can respond.
                        XConfigActor gatewayConfig = XConfig.getInstance().getRespondingGatewayConfigForHomeCommunityId(homeCommunityId);
                        if (gatewayConfig == null) {
                            response.add_error(MetadataSupport.XDSUnknownCommunity,
                                    "Do not understand homeCommunityId " + homeCommunityId,
                                    this.getLocalHomeCommunityId(), log_message);
                        } else {
                            // This request is good (targeted for a remote community.
                            this.addRequest(docRequest, homeCommunityId, gatewayConfig, false);
                        }
                    }
                }
            }
        }
    }
}
