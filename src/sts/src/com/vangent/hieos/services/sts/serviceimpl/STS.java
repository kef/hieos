/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.sts.serviceimpl;

import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.services.sts.model.STSConstants;
import com.vangent.hieos.services.sts.transactions.STSRequestHandler;
import com.vangent.hieos.services.sts.util.STSUtil;
import com.vangent.hieos.xutil.services.framework.XAbstractService;

import org.apache.log4j.Logger;

// Axis2 LifeCycle support.
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

// XATNA
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

/**
 * STS WS-Trust web service implementation.
 *
 * @author Bernie Thuman
 */
public class STS extends XAbstractService {

    private final static Logger logger = Logger.getLogger(STS.class);
    private static XConfigActor config = null;  // Singleton.

    @Override
    protected XConfigActor getConfigActor() {
        return config;
    }

    /**
     *
     * @param request
     * @return
     * @throws AxisFault
     */
    public OMElement RequestSecurityToken(OMElement request) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction(this.getRequestType(request), request);
            validateWS();
            validateNoMTOM();
            STSRequestHandler handler = new STSRequestHandler(this.log_message);
            handler.setConfigActor(config);
            response = handler.run(request);
            endTransaction(handler.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;
    }

    /**
     * 
     * @param request
     * @return
     */
    private String getRequestType(OMElement request) {
        try {
            String requestType = STSUtil.getRequestType(request);
            if (requestType.equalsIgnoreCase(STSConstants.ISSUE_REQUEST_TYPE)) {
                return "STS:RST:Issue";
            } else if (requestType.equalsIgnoreCase(STSConstants.VALIDATE_REQUEST_TYPE)) {
                return "STS:RST:Validate";
            } else {
                return "STS:RST:Unknown";
            }
        } catch (STSException ex) {
            return "STS:RST:Unknown";
        }
    }

    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     * @param configctx
     * @param service
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("STS::startUp()");
        try {
            XConfig xconf;
            xconf = XConfig.getInstance();
            XConfigObject homeCommunity = xconf.getHomeCommunityConfig();
            config = (XConfigActor) homeCommunity.getXConfigObjectWithName("sts", XConfig.STS_TYPE);
        } catch (Exception ex) {
            logger.fatal("Unable to get configuration for service", ex);
        }
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     * @param configctx
     * @param service
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("STS::shutDown()");
    }
}
