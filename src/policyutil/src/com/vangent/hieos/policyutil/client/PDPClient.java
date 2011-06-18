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
import com.vangent.hieos.policyutil.model.pdp.PDPResponseBuilder;
import com.vangent.hieos.policyutil.model.pdp.XACMLRequestBuilder;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;

import java.util.List;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResultType;
import org.jboss.security.xacml.core.model.policy.AttributeAssignmentType;
import org.jboss.security.xacml.core.model.policy.ObligationType;
import org.jboss.security.xacml.core.model.policy.ObligationsType;

import org.picketlink.identity.federation.core.exceptions.ProcessingException;

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
            // Convert PDPRequest to XACML request.
            XACMLRequestBuilder xacmlRequestBuilder = new XACMLRequestBuilder();
            RequestType requestType = xacmlRequestBuilder.buildXACMLRequestType(pdpRequest);

            // Get configuration.
            XConfigActor config = this.getConfig();
            XConfigTransaction txn = config.getTransaction("Authorize");

            // Perform SOAP call to PDP.
            PDPSOAPClient soapSAMLXACML = new PDPSOAPClient();
            ResultType resultType = soapSAMLXACML.send(txn.getEndpointURL(),
                    txn.isSOAP12Endpoint(), pdpRequest.getIssuer(), requestType);
            // DEBUG:
            this.print(resultType);

            // Convert XACML response to PDPResponse.
            PDPResponseBuilder pdpResponseBuilder = new PDPResponseBuilder();
            PDPResponse pdpResponse = pdpResponseBuilder.buildPDPResponse(resultType);
            return pdpResponse;
        } catch (ProcessingException ex) {
            throw new PolicyException("Unable to contact Policy Decision Point: " + ex.getMessage());
        }
    }

    /**
     *
     * @param result
     */
    private void print(ResultType result) {
        System.out.println("Decision = " + result.getDecision());
        System.out.println("Obligations:");
        ObligationsType obligations = result.getObligations();
        if (obligations != null) {
            for (ObligationType obligationType : obligations.getObligation()) {
                System.out.println("... Id = " + obligationType.getObligationId());
                System.out.println("... fulfillOn.name = " + obligationType.getFulfillOn().name());
                System.out.println("... fulfillOn.value = " + obligationType.getFulfillOn().value());
                List<AttributeAssignmentType> attrAssignmentTypes = obligationType.getAttributeAssignment();
                for (AttributeAssignmentType attrAssignmentType : attrAssignmentTypes) {
                    System.out.println("..... attributeId = " + attrAssignmentType.getAttributeId());
                    System.out.println("..... dataType = " + attrAssignmentType.getDataType());
                }
            }
        }
        System.out.println("Status = " + result.getStatus().getStatusCode().getValue());
        System.out.println("Resource Id = " + result.getResourceId());
    }
}
