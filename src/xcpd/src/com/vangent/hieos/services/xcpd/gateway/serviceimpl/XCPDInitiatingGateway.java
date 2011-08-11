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

import com.vangent.hieos.services.xcpd.gateway.transactions.XCPDInitiatingGatewayRequestHandler;
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
 * Class to handle all web service requests to XCPD Initiating Gateway (IG).
 *
 * @author Bernie Thuman
 */
public class XCPDInitiatingGateway extends XCPDGateway {

    private final static Logger logger = Logger.getLogger(XCPDInitiatingGateway.class);
    private static XConfigActor config = null;  // Singleton.

    @Override
    protected XConfigActor getConfigActor() {
        return config;
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return OMElement PRPA_IN201306UV02_Message
     * @throws AxisFault
     */
    public OMElement PatientRegistryFindCandidatesQuery(OMElement PRPA_IN201305UV02_Message) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction(this.getTransactionName("FindCandidatesQuery (IG)"), PRPA_IN201305UV02_Message);
            validateWS();
            validateNoMTOM();
            XCPDInitiatingGatewayRequestHandler handler = new XCPDInitiatingGatewayRequestHandler(this.log_message);
            handler.setConfigActor(config);
            response = handler.run(
                    PRPA_IN201305UV02_Message,
                    XCPDInitiatingGatewayRequestHandler.MessageType.PatientRegistryFindCandidatesQuery);
            endTransaction(handler.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;
    }

    /**
     *
     * @param PRPA_IN201309UV02_Message
     * @return OMElement PRPA_IN201310UV02_Message
     * @throws AxisFault
     */
    public OMElement PatientRegistryGetIdentifiersQuery(OMElement PRPA_IN201309UV02_Message) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction(this.getTransactionName("GetIdentifiersQuery (IG)"), PRPA_IN201309UV02_Message);
            validateWS();
            validateNoMTOM();
            XCPDInitiatingGatewayRequestHandler handler = new XCPDInitiatingGatewayRequestHandler(this.log_message);
            handler.setConfigActor(config);
            response = handler.run(
                    PRPA_IN201309UV02_Message,
                    XCPDInitiatingGatewayRequestHandler.MessageType.PatientRegistryGetIdentifiersQuery);
            endTransaction(handler.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;
    }

    /**
     *
     * @param PRPA_IN201301UV02_Message
     * @return OMElement MCCI_IN000002UV01_Message
     * @throws AxisFault
     */
    public OMElement PatientRegistryRecordAdded(OMElement PRPA_IN201301UV02_Message) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction(this.getTransactionName("PIDFEED.Add (IG)"), PRPA_IN201301UV02_Message);
            validateWS();
            validateNoMTOM();
            XCPDInitiatingGatewayRequestHandler handler = new XCPDInitiatingGatewayRequestHandler(this.log_message);
            handler.setConfigActor(config);
            response = handler.run(
                    PRPA_IN201301UV02_Message,
                    XCPDInitiatingGatewayRequestHandler.MessageType.PatientRegistryRecordAdded);
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
        logger.info("XCPDInitiatingGateway::startUp()");
        try {
            XConfig xconf;
            xconf = XConfig.getInstance();
            XConfigObject homeCommunity = xconf.getHomeCommunityConfig();
            config = (XConfigActor) homeCommunity.getXConfigObjectWithName("ig", XConfig.XCA_INITIATING_GATEWAY_TYPE);
        } catch (Exception ex) {
            logger.fatal("Unable to get configuration for service", ex);
        }
        this.ATNAlogStart(XATNALogger.ActorType.INITIATING_GATEWAY);
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("XCPDInitiatingGateway::shutDown()");
        this.ATNAlogStop(XATNALogger.ActorType.INITIATING_GATEWAY);
    }
}
