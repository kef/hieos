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
package com.vangent.hieos.hl7v3util.client;

import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCPDGatewayClient extends Client {

    private final static Logger logger = Logger.getLogger(XCPDGatewayClient.class);
    protected final static String XCPD_GATEWAY_CGPD_ACTION = "urn:hl7-org:v3:PRPA_IN201305UV02:CrossGatewayPatientDiscovery";
    protected final static String XCPD_GATEWAY_CGPD_ACTION_RESPONSE = "urn:hl7-org:v3:PRPA_IN201306UV02:CrossGatewayPatientDiscovery";

    /**
     *
     * @param gatewayConfig
     */
    public XCPDGatewayClient(XConfigActor gatewayConfig) {
        super(gatewayConfig);
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return PRPA_IN201306UV02_Message
     * @throws AxisFault
     */
    public PRPA_IN201306UV02_Message findCandidatesQuery(PRPA_IN201305UV02_Message request) throws AxisFault {
        PRPA_IN201306UV02_Message response = null;
        // TBD: Validate against schema.
        Soap soap = new Soap();
        try {
            XConfigActor gatewayConfig = this.getConfig();
            XConfigTransaction txn = gatewayConfig.getTransaction("CrossGatewayPatientDiscovery");
            soap.setAsync(txn.isAsyncTransaction());
            OMElement soapResponse = soap.soapCall(
                    request.getMessageNode(),
                    txn.getEndpointURL(),
                    false /* mtom */,
                    true /* addressing */,
                    true /* soap12 */,
                    XCPDGatewayClient.XCPD_GATEWAY_CGPD_ACTION /* SOAP action */,
                    XCPDGatewayClient.XCPD_GATEWAY_CGPD_ACTION_RESPONSE /* SOAP action response */);
            response = new PRPA_IN201306UV02_Message(soapResponse);
        } catch (XdsException ex) {
            throw new AxisFault(ex.getMessage());
        }
        return response;
    }
}
