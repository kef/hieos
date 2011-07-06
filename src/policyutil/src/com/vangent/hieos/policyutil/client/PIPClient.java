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
import com.vangent.hieos.policyutil.model.pip.PIPRequest;
import com.vangent.hieos.policyutil.model.pip.PIPRequestBuilder;
import com.vangent.hieos.policyutil.model.pip.PIPRequestElement;
import com.vangent.hieos.policyutil.model.pip.PIPResponse;
import com.vangent.hieos.policyutil.model.pip.PIPResponseBuilder;
import com.vangent.hieos.policyutil.model.pip.PIPResponseElement;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

/**
 * Client interface (proxy) to Policy Information Point (PIP).
 *
 * @author Bernie Thuman
 */
public class PIPClient extends Client {

    /**
     * PIPClient constructor.
     *
     * @param config
     */
    public PIPClient(XConfigActor config) {
        super(config);
    }

    /**
     * Simple client interface for issuing PIP requests and receiving PIP responses.
     *
     * @param pipRequest
     * @return PIPResponse
     * @throws PolicyException
     */
    public PIPResponse getPatientConsentDirectives(
            PIPRequest pipRequest) throws PolicyException {
        try {
            // Get configuration.
            XConfigActor config = this.getConfig();
            XConfigTransaction txn = config.getTransaction("GetConsentDirectives");
            // FIXME: Do not hard-wire.
            String soapAction = "urn:hieos:policy:pip:GetConsentDirectivesRequest";

            // Perform SOAP call to PIP.
            PIPResponse pipResponse = this.send(pipRequest, soapAction, txn.getEndpointURL(), txn.isSOAP12Endpoint());
            return pipResponse;
        } catch (Exception ex) {
            throw new PolicyException("Unable to contact Policy Information Point: " + ex.getMessage());
        }
       
    }

    /**
     * Issues SOAP request to PIP and returns PIP response.
     *
     * @param pipRequest
     * @param soapAction
     * @param endpointURL
     * @param soap12
     * @return PIPResponse
     * @throws PolicyException
     */
    private PIPResponse send(PIPRequest pipRequest, String soapAction, String endpointURL, boolean soap12) throws PolicyException {
        try {
            // Builder the request (in XML).
            PIPRequestBuilder requestBuilder = new PIPRequestBuilder();
            PIPRequestElement pipRequestElement = requestBuilder.buildPIPRequestElement(pipRequest);

            // Make SOAP call to PIP.
            Soap soap = new Soap();
            OMElement pipResponseNode;
            try {
                pipResponseNode = soap.soapCall(
                        pipRequestElement.getElement(),
                        endpointURL,
                        false /* MTOM */,
                        soap12 /* Addressing - Only if SOAP 1.2 */,
                        soap12 /* SOAP 1.2 */,
                        soapAction, null);
            } catch (Exception ex) {
                throw new PolicyException(ex.getMessage());
            }
            if (pipResponseNode == null) {
                throw new PolicyException("No SOAP Response!");
            }

            // Build the PIP Response.
            PIPResponseBuilder responseBuilder = new PIPResponseBuilder();
            PIPResponse pipResponse = responseBuilder.buildPIPResponse(new PIPResponseElement(pipResponseNode));
            return pipResponse;

        } catch (Exception ex) {
            throw new PolicyException(ex.getMessage());
        }
    }
}
