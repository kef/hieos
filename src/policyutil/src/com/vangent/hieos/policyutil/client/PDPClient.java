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
package com.vangent.hieos.policyutil.client;

import com.vangent.hieos.hl7v3util.client.Client;
import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.model.pdp.PDPRequest;
import com.vangent.hieos.policyutil.model.pdp.PDPResponse;
import com.vangent.hieos.policyutil.model.pdp.SAMLResponseElement;
import com.vangent.hieos.policyutil.model.pdp.XACMLAuthzDecisionQueryElement;
import com.vangent.hieos.policyutil.model.pdp.XACMLRequestBuilder;
import com.vangent.hieos.policyutil.model.pdp.XACMLResponseBuilder;
import com.vangent.hieos.policyutil.model.saml.SAML2Assertion;
import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;

import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class PDPClient extends Client {

    /**
     * 
     * @param config
     */
    public PDPClient(XConfigActor config) {
        super(config);
    }

    /**
     *
     * @param pdpRequest
     * @return
     * @throws PolicyException
     */
    public PDPResponse authorize(PDPRequest pdpRequest) throws PolicyException {
        try {
            // Get configuration.
            XConfigActor config = this.getConfig();
            XConfigTransaction txn = config.getTransaction("Authorize");

            // Perform SOAP call to PDP.
            PDPResponse pdpResponse = this.send(pdpRequest, txn.getEndpointURL(), txn.isSOAP12Endpoint());
            return pdpResponse;
        } catch (Exception ex) {
            throw new PolicyException("Unable to contact Policy Decision Point: " + ex.getMessage());
        }
    }

    /**
     * 
     * @param pdpRequest
     * @param endpointURL
     * @param soap12
     * @return
     * @throws PolicyException
     */
    private PDPResponse send(PDPRequest pdpRequest, String endpointURL, boolean soap12) throws PolicyException {
        try {
            XACMLRequestBuilder requestBuilder = new XACMLRequestBuilder();
            XACMLAuthzDecisionQueryElement authzDecisionQuery = requestBuilder.buildXACMLAuthzDecisionQuery(pdpRequest);
            OMElement authzDecisionQueryNode = authzDecisionQuery.getElement();

            // Make SOAP call to PDP.
            Soap soap = new Soap();
            OMElement samlResponseNode;
            try {
                samlResponseNode = soap.soapCall(
                        authzDecisionQueryNode,
                        endpointURL,
                        false /* MTOM */,
                        soap12 /* Addressing - Only if SOAP 1.2 */,
                        soap12 /* SOAP 1.2 */,
                        PolicyConstants.PDP_SOAP_ACTION, null);
            } catch (Exception ex) {
                throw new PolicyException(ex.getMessage());
            }
            if (samlResponseNode == null) {
                throw new PolicyException("No SOAP Response!");
            }

            XACMLResponseBuilder responseBuilder = new XACMLResponseBuilder();
            PDPResponse pdpResponse = responseBuilder.buildPDPResponse(new SAMLResponseElement(samlResponseNode));
            return pdpResponse;

        } catch (Exception ex) {
            throw new PolicyException(ex.getMessage());
        }
    }
}
