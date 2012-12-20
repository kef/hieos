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
package com.vangent.hieos.services.xca.gateway.serviceimpl;

import com.vangent.hieos.services.xca.gateway.transactions.XCAAdhocQueryRequest;
import com.vangent.hieos.services.xca.gateway.transactions.XCAIGRetrieveDocumentSet;
import com.vangent.hieos.services.xca.gateway.transactions.XCAIGAdhocQueryRequest;

import com.vangent.hieos.services.xca.gateway.transactions.XCARetrieveDocumentSet;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;

// XATNA
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCAInitiatingGateway extends XCAGateway {

    private final static Logger logger = Logger.getLogger(XCAInitiatingGateway.class);
    private static XConfigActor config = null;  // Singleton.

    @Override
    protected XConfigActor getConfigActor() {
        return config;
    }

    /**
     * 
     * @return
     * @throws XdsInternalException
     */
    protected XCAAdhocQueryRequest getAdHocQueryTransaction() throws XdsInternalException {
        XCAAdhocQueryRequest request = new XCAIGAdhocQueryRequest(log_message);
        request.setConfigActor(config);
        request.setGatewayActorType(ATNAAuditEvent.ActorType.INITIATING_GATEWAY);
        return request;
    }

    /**
     * 
     * @return
     * @throws XdsInternalException
     */
    protected XCARetrieveDocumentSet getRetrieveDocumentSet() throws XdsInternalException {
        XCARetrieveDocumentSet request = new XCAIGRetrieveDocumentSet(log_message);
        request.setConfigActor(config);
        request.setGatewayActorType(ATNAAuditEvent.ActorType.INITIATING_GATEWAY);
        return request;
    }

    /**
     *
     * @return
     */
    protected String getQueryTransactionName() {
        return "SQ (XCA)";
    }

    /**
     *
     * @return
     */
    protected String getRetTransactionName() {
        return "RET.b (XCA)";
    }

    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startup() {
        logger.info("XCAInitiatingGateway::startup()");
        try {
            XConfig xconf = XConfig.getInstance();
            XConfigObject homeCommunity = xconf.getHomeCommunityConfig();
            config = (XConfigActor) homeCommunity.getXConfigObjectWithName("ig", XConfig.XCA_INITIATING_GATEWAY_TYPE);
        } catch (Exception ex) {
            logger.fatal("Unable to get configuration for service", ex);
        }
        this.ATNAlogStart(ATNAAuditEvent.ActorType.INITIATING_GATEWAY);
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutdown() {
        logger.info("XCAInitiatingGateway::shutdown()");
        this.ATNAlogStop(ATNAAuditEvent.ActorType.INITIATING_GATEWAY);
    }
}
