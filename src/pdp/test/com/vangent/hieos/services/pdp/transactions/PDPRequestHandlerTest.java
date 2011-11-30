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

package com.vangent.hieos.services.pdp.transactions;

import com.vangent.hieos.services.pdp.utils.DebugUtils;
import com.vangent.hieos.services.pdp.utils.JUnitHelper;
import com.vangent.hieos.xutil.xml.XPathHelper;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unable to test this the right way
 * @author Vangent
 */
public class PDPRequestHandlerTest {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(PDPRequestHandlerTest.class);

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDefaultTest() throws Exception {

        runTest("resources/requests/request-default.xml", "Permit",
                "evaluate-document-policy");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyBlockedIndividualTest() throws Exception {

        runTest("resources/requests/initial/request-denyBlockedIndividual.xml", "Deny",
                "blocked-individuals-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyBlockedOrganizationTest() throws Exception {

        runTest("resources/requests/initial/request-denyBlockedOrganization.xml", "Deny",
                "blocked-organizations-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyConfidentialityCodeTest() throws Exception {

        runTest("resources/requests/document/request-denyConfidentialityCode.xml",
                "Deny", "patient-consent-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyEmptyPermitTest() throws Exception {

        runTest("resources/requests/initial/request-denyEmptyPermit.xml", "Deny",
                "empty-permit-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyExplicitAuthorizationNONETest() throws Exception {

        runTest("resources/requests/document/request-denyExplicitAuthorization-NONE.xml",
                "Deny", "always-authorise-normal-document-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyExplicitAuthorizationNORMALTest() throws Exception {

        runTest(
            "resources/requests/document/request-denyExplicitAuthorization-NORMAL.xml",
            "Deny", "always-authorise-sensitive-document-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyHomeCommunityTest() throws Exception {

        runTest("resources/requests/document/request-denyHomeCommunity.xml", "Deny",
                "patient-consent-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyInactivePatientTest() throws Exception {

        runTest("resources/requests/initial/request-denyInactivePatient.xml", "Deny",
                "patient-inactive-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyNoDirectivesDocumentTest() throws Exception {

        runTest("resources/requests/document/request-denyNoDirectives.xml", "Deny",
                "final-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyUnallowedIndividualTest() throws Exception {

        runTest("resources/requests/initial/request-denyUnallowedIndividual.xml", "Deny",
                "allowed-individuals-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runDenyUnallowedOrganizationTest() throws Exception {

        runTest("resources/requests/initial/request-denyUnallowedOrganization.xml",
                "Deny", "allowed-organizations-policy-deny");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runEmergencyTest() throws Exception {

        runTest("resources/requests/initial/request-permitEmergency.xml", "Permit",
                "evaluate-document-policy");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runPermitEmergencyDocumentTest() throws Exception {

        runTest("resources/requests/document/request-permitEmergency.xml", "Permit",
                "emergency-access-policy-permit");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runPermitExplicitAuthorizationNORMALTest() throws Exception {

        runTest(
            "resources/requests/document/request-permitExplicitAuthorization-NORMAL.xml",
            "Permit", "always-authorise-normal-document-policy-permit");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runPermitExplicitAuthorizationRESTRICTED2Test()
            throws Exception {

        runTest(
            "resources/requests/document/request-permitExplicitAuthorization-RESTRICTED2.xml",
            "Permit", "always-authorise-sensitive-document-policy-permit");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runPermitExplicitAuthorizationRESTRICTEDTest()
            throws Exception {

        runTest(
            "resources/requests/document/request-permitExplicitAuthorization-RESTRICTED.xml",
            "Permit", "always-authorise-normal-document-policy-permit");
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void runPermitNoDirectivesTest() throws Exception {

        runTest("resources/requests/initial/request-permitNoDirectives.xml", "Permit",
                "evaluate-document-policy");
    }

    /**
     * Method description
     *
     *
     * @param file
     * @param outcome
     * @param obligation
     *
     * @throws Exception
     */
    private void runTest(String file, String outcome, String obligation)
            throws Exception {

        OMElement request = JUnitHelper.fileToOMElement(file);
        JUnitPDPRequestHandler pdp = new JUnitPDPRequestHandler();
        OMElement response = pdp.run(request);

        assertNotNull(response);
        logger.debug(DebugUtils.toPrettyString(response));

        String uri = "urn:oasis:names:tc:xacml:2.0:context:schema:os";
        String expr = "//ns:Response/ns:Result/ns:Decision";
        String status = XPathHelper.stringValueOf(response, expr, uri);

        String[] uris = { "urn:oasis:names:tc:xacml:2.0:context:schema:os",
                          "urn:oasis:names:tc:xacml:2.0:policy:schema:os" };
        String[] prefixes = { "ns1", "ns2" };

        expr = "//ns1:Response/ns1:Result/ns2:Obligations/ns2:Obligation[1]/@ObligationId";

        String oblid = XPathHelper.stringValueOf(response, expr, prefixes,
                           uris);

        String msg = String.format("Decision: [%s] Obligation: [%s]", status,
                                   oblid);

        assertEquals(msg, outcome, status);

        if (null == obligation) {
            obligation = "";
        }

        assertEquals(msg, obligation, oblid);
    }
}
