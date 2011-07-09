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
package com.vangent.hieos.services.pip.serviceimpl;

import com.vangent.hieos.services.pip.transactions.PIPRequestHandler;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIP extends XAbstractService {

    private final static Logger logger = Logger.getLogger(PIP.class);

    /**
     *
     * @param request
     * @return
     * @throws AxisFault
     */
    public OMElement GetConsentDirectives(OMElement request) throws AxisFault {
        beginTransaction("PIP:GetConsentDirectives", request);
        validateWS();
        validateNoMTOM();
        PIPRequestHandler handler = new PIPRequestHandler(this.log_message, MessageContext.getCurrentMessageContext());
        OMElement result = handler.run(request);
        endTransaction(handler.getStatus());
        return result;
    }

    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("PIP::startUp()");
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("PIP::shutDown()");
    }
}
