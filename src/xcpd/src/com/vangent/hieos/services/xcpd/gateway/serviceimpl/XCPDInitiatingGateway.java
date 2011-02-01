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

import com.vangent.hieos.services.xcpd.gateway.transactions.XCPDGatewayRequestHandler;
import com.vangent.hieos.services.xcpd.gateway.transactions.XCPDInitiatingGatewayRequestHandler;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCPDInitiatingGateway extends XCPDGateway {

    private final static Logger logger = Logger.getLogger(XCPDInitiatingGateway.class);

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return
     */
    public OMElement CrossGatewayPatientDiscovery(OMElement PRPA_IN201305UV02_Message) throws AxisFault {
        try {
            OMElement startup_error = beginTransaction(
                    this.getTransactionName("CGPD (IG)"), PRPA_IN201305UV02_Message, XAbstractService.ActorType.REGISTRY);
            if (startup_error != null) {
                // TBD: FIXUP (XUA should be returning a SOAP fault!)
                return startup_error;
            }
            validateWS();
            validateNoMTOM();
            XCPDInitiatingGatewayRequestHandler handler =
                    new XCPDInitiatingGatewayRequestHandler(
                    this.log_message,
                    XCPDGatewayRequestHandler.GatewayType.InitiatingGateway);
            OMElement result = handler.run(
                    PRPA_IN201305UV02_Message,
                    XCPDGatewayRequestHandler.MessageType.CrossGatewayPatientDiscovery);
            endTransaction(true);
            return result;
        } catch (Exception ex) {
            throw getAxisFault(ex);
        }
    }

    /**
     *
     * @param PRPA_IN201301UV02_Message
     * @return
     */
    public OMElement PatientRegistryRecordAdded(OMElement PRPA_IN201301UV02_Message) throws AxisFault {
        try {
            OMElement startup_error = beginTransaction(
                    this.getTransactionName("PIDFEED.Add (IG)"), PRPA_IN201301UV02_Message, XAbstractService.ActorType.PIXMGR);
            if (startup_error != null) {
                return startup_error;
            }
            validateWS();
            validateNoMTOM();
            XCPDInitiatingGatewayRequestHandler handler =
                    new XCPDInitiatingGatewayRequestHandler(
                    this.log_message,
                    XCPDInitiatingGatewayRequestHandler.GatewayType.InitiatingGateway);
            OMElement result = handler.run(
                    PRPA_IN201301UV02_Message,
                    XCPDInitiatingGatewayRequestHandler.MessageType.PatientRegistryRecordAdded);
            endTransaction(true);
            return result;
        } catch (Exception ex) {
            throw getAxisFault(ex);
        }
    }
}
