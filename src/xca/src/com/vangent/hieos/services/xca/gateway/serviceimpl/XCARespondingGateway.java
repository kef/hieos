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
import com.vangent.hieos.services.xca.gateway.transactions.XCARGAdhocQueryRequest;
import com.vangent.hieos.services.xca.gateway.transactions.XCARGRetrieveDocumentSet;

// Axis2 LifeCycle support.
import com.vangent.hieos.services.xca.gateway.transactions.XCARetrieveDocumentSet;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

// XATNA
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCARespondingGateway extends XCAGateway {

    private final static Logger logger = Logger.getLogger(XCARespondingGateway.class);

    /**
     * 
     * @return
     * @throws XdsInternalException
     */
    protected XCAAdhocQueryRequest getAdHocQueryTransaction() throws XdsInternalException {
       return new XCARGAdhocQueryRequest(this.getGatewayConfig(), log_message, getMessageContext());
    }

    /**
     * 
     * @return
     * @throws XdsInternalException
     */
    protected XCARetrieveDocumentSet getRetrieveDocumentSet() throws XdsInternalException {
        return new XCARGRetrieveDocumentSet(this.getGatewayConfig(), log_message, getMessageContext());
    }

    /**
     *
     * @return
     * @throws AxisFault
     */
    protected XConfigActor getGatewayConfig() throws XdsInternalException {
        try {
            XConfig xconf = XConfig.getInstance();
            // Get the home community config.
            XConfigObject homeCommunityConfig = xconf.getHomeCommunityConfig();
            XConfigObject gatewayConfig = homeCommunityConfig.getXConfigObjectWithName(
                    "rg", XConfig.XCA_RESPONDING_GATEWAY_TYPE);
            return (XConfigActor) gatewayConfig;
        } catch (XdsInternalException ex) {
            logger.fatal("Unable to load XConfig for XCA Responding Gateway", ex);
            throw ex;  // Rethrow.
        }
    }

    /**
     *
     * @return
     */
    protected String getQueryTransactionName() {
        return "XGQ (XCA)";
    }

    /**
     *
     * @return
     */
    protected String getRetTransactionName() {
        return "XGR (XCA)";
    }

// BHT (ADDED Axis2 LifeCycle methods):
    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("XCARespondingGateway::startUp()");
        this.ATNAlogStart(XATNALogger.ActorType.RESPONDING_GATEWAY);
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("XCARespondingGateway::shutDown()");
        this.ATNAlogStop(XATNALogger.ActorType.RESPONDING_GATEWAY);
    }
}
