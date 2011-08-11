/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xcpd.gateway.serviceimpl;

import com.vangent.hieos.services.xcpd.gateway.transactions.XCPDRespondingGatewayRequestHandler;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.log4j.Logger;

/**
 * Class to handle all web service requests to XCPD Responding Gateway (RG).
 *
 * @author Bernie Thuman
 */
public class XCPDRespondingGateway extends XCPDGateway {

    private final static Logger logger = Logger.getLogger(XCPDRespondingGateway.class);
    private static XConfigActor config = null;  // Singleton.

    @Override
    protected XConfigActor getConfigActor() {
        return config;
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return
     */
    public OMElement CrossGatewayPatientDiscovery(OMElement PRPA_IN201305UV02_Message) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction(this.getTransactionName("CGPD (RG)"), PRPA_IN201305UV02_Message);
            validateWS();
            validateNoMTOM();
            XCPDRespondingGatewayRequestHandler handler = new XCPDRespondingGatewayRequestHandler(this.log_message);
            handler.setConfigActor(config);
            response = handler.run(PRPA_IN201305UV02_Message,
                    XCPDRespondingGatewayRequestHandler.MessageType.CrossGatewayPatientDiscovery);
            endTransaction(handler.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;
    }

    /**
     *
     * @param plq
     * @return
     */
    public OMElement PatientLocationQuery(OMElement plq) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction(this.getTransactionName("PLQ (RG)"), plq);
            validateWS();
            validateNoMTOM();
            XCPDRespondingGatewayRequestHandler handler = new XCPDRespondingGatewayRequestHandler(this.log_message);
            handler.setConfigActor(config);
            response =
                    handler.run(plq,
                    XCPDRespondingGatewayRequestHandler.MessageType.PatientLocationQuery);
            endTransaction(handler.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;
    }

    // BHT (ADDED Axis2 LifeCycle methods):
    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("XCPDRespondingGateway::startUp()");
        try {
            XConfig xconf;
            xconf = XConfig.getInstance();
            XConfigObject homeCommunity = xconf.getHomeCommunityConfig();
            config = (XConfigActor) homeCommunity.getXConfigObjectWithName("rg", XConfig.XCA_RESPONDING_GATEWAY_TYPE);
        } catch (Exception ex) {
            logger.fatal("Unable to get configuration for service", ex);
        }
        this.ATNAlogStart(XATNALogger.ActorType.RESPONDING_GATEWAY);
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("XCPDRespondingGateway::shutDown()");
        this.ATNAlogStop(XATNALogger.ActorType.RESPONDING_GATEWAY);
    }
}
