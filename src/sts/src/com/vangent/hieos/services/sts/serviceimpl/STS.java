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

import com.vangent.hieos.services.sts.transactions.STSRequestHandler;
import com.vangent.hieos.xutil.services.framework.XAbstractService;

import org.apache.log4j.Logger;

// Axis2 LifeCycle support.
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

// XATNA
import com.vangent.hieos.xutil.atna.XATNALogger;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

/**
 * STS WS-Trust web service implementation.
 *
 * @author Bernie Thuman
 */
public class STS extends XAbstractService {

    private final static Logger logger = Logger.getLogger(STS.class);

    /**
     *
     * @param requestSecurityTokenRequest
     * @return
     * @throws AxisFault
     */
    public OMElement RequestSecurityToken(OMElement requestSecurityTokenRequest) throws AxisFault {
        try {
            this.setExcludedServiceFromXUA(true);
            OMElement startup_error = beginTransaction(
                    "STS:RST", requestSecurityTokenRequest, XAbstractService.ActorType.STS);
            if (startup_error != null) {
                // TBD: FIXUP (XUA should be returning a SOAP fault!)
                return startup_error;
            }
            validateWS();
            validateNoMTOM();
            STSRequestHandler handler =
                    new STSRequestHandler(this.log_message, MessageContext.getCurrentMessageContext());
            OMElement result = handler.run(requestSecurityTokenRequest);
            endTransaction(handler.getStatus());
            return result;
        } catch (Exception ex) {
            throw getAxisFault(ex);
        }
    }

    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("STS::startUp()");
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("STS::shutDown()");
    }
}
