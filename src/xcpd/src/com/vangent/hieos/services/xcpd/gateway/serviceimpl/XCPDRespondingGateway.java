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

import com.vangent.hieos.services.xcpd.gateway.transactions.XCPDGatewayRequestHandler;
import com.vangent.hieos.services.xcpd.gateway.transactions.XCPDRespondingGatewayRequestHandler;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 * Class to handle all web service requests to XCPD Responding Gateway (RG).
 *
 * @author Bernie Thuman
 */
public class XCPDRespondingGateway extends XCPDGateway {

    private final static Logger logger = Logger.getLogger(XCPDRespondingGateway.class);

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return
     */
    public OMElement CrossGatewayPatientDiscovery(OMElement PRPA_IN201305UV02_Message) throws AxisFault {
        try {
            OMElement startup_error = beginTransaction(
                    this.getTransactionName("CGPD (RG)"), PRPA_IN201305UV02_Message, XAbstractService.ActorType.REGISTRY);
            if (startup_error != null) {
                // TBD: FIXUP (XUA should be returning a SOAP fault!)
                return startup_error;
            }
            validateWS();
            validateNoMTOM();
            XCPDRespondingGatewayRequestHandler handler = new XCPDRespondingGatewayRequestHandler(this.log_message);
            OMElement result = handler.run(PRPA_IN201305UV02_Message, 
                    XCPDRespondingGatewayRequestHandler.MessageType.CrossGatewayPatientDiscovery);
            endTransaction(this.log_message.isPass());
            return result;
        } catch (Exception ex) {
            throw getAxisFault(ex);
        }
    }

    /**
     *
     * @param plq
     * @return
     */
    public OMElement PatientLocationQuery(OMElement plq) throws AxisFault {
        try {
            OMElement startup_error = beginTransaction(
                    this.getTransactionName("PLQ (RG)"), plq, XAbstractService.ActorType.REGISTRY);
            if (startup_error != null) {
                // TBD: FIXUP (XUA should be returning a SOAP fault!)
                return startup_error;
            }
            validateWS();
            validateNoMTOM();
            XCPDRespondingGatewayRequestHandler handler = new XCPDRespondingGatewayRequestHandler(this.log_message);
            OMElement result =
                    handler.run(plq,
                    XCPDRespondingGatewayRequestHandler.MessageType.PatientLocationQuery);
            endTransaction(this.log_message.isPass());
            return result;
        } catch (Exception ex) {
            throw getAxisFault(ex);
        }
    }
}
