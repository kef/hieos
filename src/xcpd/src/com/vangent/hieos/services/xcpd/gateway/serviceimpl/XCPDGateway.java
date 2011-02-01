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
package com.vangent.hieos.services.xcpd.gateway.serviceimpl;

import com.vangent.hieos.xutil.services.framework.XAbstractService;

import org.apache.log4j.Logger;

// Axis2 LifeCycle support.
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

// XATNA
import com.vangent.hieos.xutil.atna.XATNALogger;

/**
 *
 * @author Bernie Thuman
 */
public class XCPDGateway extends XAbstractService {

    private final static Logger logger = Logger.getLogger(XCPDGateway.class);

    // BHT (ADDED Axis2 LifeCycle methods):
    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("XCPDGateway::startUp(): " + service.getParameterValue("ActorName"));
        if (service.getParameterValue("ActorName").equals("InitiatingGateway")) {
            this.ATNAlogStart(XATNALogger.ActorType.INITIATING_GATEWAY);
        } else {
            this.ATNAlogStart(XATNALogger.ActorType.RESPONDING_GATEWAY);
        }
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("XCPDGateway::shutDown(): " + service.getParameterValue("ActorName"));
        if (service.getParameterValue("ActorName").equals("InitiatingGateway")) {
            this.ATNAlogStop(XATNALogger.ActorType.INITIATING_GATEWAY);
        } else {
            this.ATNAlogStop(XATNALogger.ActorType.RESPONDING_GATEWAY);
        }
    }

    /**
     * 
     * @param name
     * @return
     */
    protected String getTransactionName(String name)
    {
        String txnName = name;
        if (isAsync()) {
            txnName = name + " ASync";
        }
        return txnName;
    }
}
