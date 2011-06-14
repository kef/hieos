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
package com.vangent.hieos.services.sts.model;

import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class ClaimBuilder {

    private final static String[] XSPA_NAMES = {
        "urn:oasis:names:tc:xacml:1.0:subject:subject-id",
        "urn:oasis:names:tc:xspa:1.0:subject:organization",
        "urn:oasis:names:tc:xspa:1.0:subject:organization-id",
        "urn:oasis:names:tc:xspa:1.0:subject:hl7:permission",
        "urn:oasis:names:tc:xacml:2.0:subject:role",
        "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse",
        "urn:oasis:names:tc:xacml:1.0:resource:resource-id",
        "urn:oasis:names:tc:xspa:1.0:resource:hl7:type",
        "urn:oasis:names:tc:xspa:1.0:environment:locality",
        "urn:oasis:names:tc:xspa:2.0:subject:npi"
    };
    private final static String[] XSPA_NAMES_REQUIRED = {
        "urn:oasis:names:tc:xacml:1.0:subject:subject-id",
        "urn:oasis:names:tc:xspa:1.0:subject:organization",
        "urn:oasis:names:tc:xspa:1.0:subject:organization-id",
        "urn:oasis:names:tc:xacml:2.0:subject:role",
        "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse",
        "urn:oasis:names:tc:xacml:1.0:resource:resource-id"
    };
    // FUTURE:
    private final static String[] CODED_NAMES = {
        "urn:oasis:names:tc:xacml:2.0:subject:role",
        "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse"
    };
    // FUTURE:
    private final static String[] CODED_NODE_NAMES = {
        "Role",
        "PurposeForUse"
    };
    // FUTURE:
    private final static String[] NHIN_NAMES = {
        "urn:oasis:names:tc:xspa:1.0:subject:subject-id", // Differs from XSPA.
        "urn:oasis:names:tc:xspa:1.0:subject:organization",
        "urn:oasis:names:tc:xspa:1.0:subject:organization-id",
        "urn:nhin:names:saml:homeCommunityId", // Not in XSPA.
        "urn:oasis:names:tc:xacml:2.0:subject:role", // Coded value.
        "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse", // Coded value (node is PurposeForUse).
        "urn:oasis:names:tc:xacml:2.0:resource:resource-id", // Differs from XSPA.
        "urn:oasis:names:tc:xspa:2.0:subject:npi"
    };
    // Maps URI to XSPA ClaimType node.
    private Map<String, OMElement> xspaClaimTypeURIMap = new HashMap<String, OMElement>();

    /**
     *
     * @param requestData
     * @return
     * @throws STSException
     */
    public List<Claim> parse(STSRequestData requestData) throws STSException {
        OMElement claimsNode = requestData.getClaimsNode();
        List<Claim> claims = new ArrayList<Claim>();
        this.buildXSPAClaimTypeURIMap(claimsNode);
        this.parse(claims);
        STSConfig stsConfig = requestData.getSTSConfig();
        if (stsConfig.getValidateRequiredClaims() == true) {
            this.validate(claims);
        }
        return claims;
    }

    /**
     *
     * @param claims
     * @throws STSException
     */
    private void validate(List<Claim> claims) throws STSException {
        // Validate proper attributes are available.

        for (int i = 0; i < XSPA_NAMES_REQUIRED.length; i++) {
            String nameToValidate = XSPA_NAMES_REQUIRED[i];
            boolean foundName = false;
            for (Claim claim : claims) {
                if (claim.getName().equalsIgnoreCase(nameToValidate)) {
                    foundName = true;
                    break;
                }
            }
            if (foundName == false) {
                System.out.println("Missing " + nameToValidate + " attribute");
                throw new STSException("Missing " + nameToValidate + " attribute");
            }
        }
    }

    /**
     *
     * @param claims
     */
    private void parse(List<Claim> claims) {
        for (int i = 0; i < XSPA_NAMES.length; i++) {
            String xspaName = XSPA_NAMES[i];
            Claim claim = this.getClaim(xspaName);
            if (claim != null) {
                claims.add(claim);
            }
        }
    }

    /**
     *
     * @param name
     * @return
     */
    private Claim getClaim(String name) {

        // Get the XSPA ClaimType node for the given XSPA name.
        OMElement xspaClaimTypeNode = this.xspaClaimTypeURIMap.get(name);
        if (xspaClaimTypeNode == null) {
            // FIXME: PUT DEBUG/DEFAULT?
            return null;  // Get out.
        }

        // Found ClaimType ... now, get its ClaimValue.
        OMElement xspaClaimValueNode = xspaClaimTypeNode.getFirstChildWithName(new QName(STSConstants.XSPA_CLAIMS_NS, "ClaimValue"));
        if (xspaClaimValueNode == null) {
            // FIXME: PUT DEBUG/DEFAULT?
            return null; // Get out.
        }

        // Get the String value.
        String xspaClaimValueText = xspaClaimValueNode.getText();

        SimpleStringClaim claim = new SimpleStringClaim();
        claim.setName(name);
        claim.setValue(xspaClaimValueText);
        return claim;
    }

    /**
     *
     * @param claims
     */
    private void buildXSPAClaimTypeURIMap(OMElement claims) {
        if (claims == null) {
            return;  // Nothing to do.
        }
        try {
            // Get all ClaimType nodes.
            List<OMElement> claimTypeNodes = XPathHelper.selectNodes(claims, "./ns:ClaimType",
                    STSConstants.XSPA_CLAIMS_NS);
            for (OMElement claimTypeNode : claimTypeNodes) {
                String claimTypeURI = claimTypeNode.getAttributeValue(new QName("Uri"));
                xspaClaimTypeURIMap.put(claimTypeURI, claimTypeNode);
            }
        } catch (XPathHelperException ex) {
            // FIXME.
        }
    }
}
