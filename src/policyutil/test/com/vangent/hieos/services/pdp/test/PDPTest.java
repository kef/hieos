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
package com.vangent.hieos.services.pdp.test;

import com.vangent.hieos.policyutil.client.PDPClient;
import com.vangent.hieos.policyutil.model.pdp.PDPRequest;
import com.vangent.hieos.policyutil.model.pdp.PDPResponse;
import com.vangent.hieos.policyutil.util.AttributeConfig.AttributeIdType;
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import java.util.List;
import oasis.names.tc.xacml._2_0.context.schema.os.ResultType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeAssignmentType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ObligationType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ObligationsType;

import org.junit.Test;

/**
 *
 * @author Bernie Thuman
 */
public class PDPTest {
    //private String endpoint = "http://localhost:8127/axis2/services/PDP";

    private String issuer = "HIEOS TEST";

    @Test
    public void testPDPClient() {
        XConfig xconf = null;
        try {
            xconf = XConfig.getInstance();
        } catch (XConfigException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }
        XConfigObject config = xconf.getHomeCommunityConfig().getXConfigObjectWithName("pdp", "PolicyDecisionPointType");
        PDPClient pdpClient = new PDPClient((XConfigActor) config);
        try {

            PDPRequest request = this.getDummyPDPRequest();
            request.setIssuer(issuer);
            PDPResponse response = pdpClient.authorize(request);

            //pdpClient.testXACML();
            //pdpClient.testXACML2();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    /**
     *
     * @return
     */
    private PDPRequest getDummyPDPRequest() {
        PDPRequest request = new PDPRequest();

        // Issuer
        request.setIssuer("HIEOS TEST ISSUER");

        // Action requested
        request.setAction("urn:ihe:iti:2007:CrossGatewayQuery");

        // Load up Subject attributes

        // subject-id
        request.addAttribute(AttributeIdType.SUBJECT_ID, "urn:oasis:names:tc:xacml:1.0:subject:subject-id", "SUBJECT_ID");

        // organization
        request.addAttribute(AttributeIdType.SUBJECT_ID, "urn:oasis:names:tc:xspa:1.0:subject:organization", "ORG");

        // organization-id
        request.addAttribute(AttributeIdType.SUBJECT_ID, "urn:oasis:names:tc:xspa:1.0:subject:organization-id", "1.1");

        // role
        request.addAttribute(AttributeIdType.SUBJECT_ID, "urn:oasis:names:tc:xacml:2.0:subject:role", "DOCTOR");
        request.addAttribute(AttributeIdType.SUBJECT_ID, "urn:oasis:names:tc:xacml:2.0:subject:role", "FOO");

        // purposeofuse
        request.addAttribute(AttributeIdType.SUBJECT_ID, "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse", "TREATMENT");

        // npi
        request.addAttribute(AttributeIdType.SUBJECT_ID, "urn:oasis:names:tc:xspa:2.0:subject:npi", "NPI");

        // Resource attributes:

        // resource-id
        request.addAttribute(AttributeIdType.RESOURCE_ID, "urn:oasis:names:tc:xacml:1.0:resource:resource-id",
                "123456^^^&1.3.6.1.4.1.21367.2010.1.2.300&ISO");

        // FIXME: !!!! if resource-id is empty, always PERMITS????

        return request;
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
