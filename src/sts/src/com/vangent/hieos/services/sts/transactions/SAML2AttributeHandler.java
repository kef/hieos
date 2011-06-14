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
package com.vangent.hieos.services.sts.transactions;

import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.services.sts.model.Claim;
import com.vangent.hieos.services.sts.model.STSConstants;
import com.vangent.hieos.services.sts.model.STSRequestData;
import com.vangent.hieos.services.sts.model.SimpleStringClaim;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.opensaml.saml2.core.Attribute;

/**
 *
 * @author Bernie Thuman
 */
public class SAML2AttributeHandler {

    // name, required(true/false)?, type(CodedValue/string), codeValueNodeName(optional)

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

    private final static String[] XSPA_NAMES_TO_VALIDATE = {
        "urn:oasis:names:tc:xacml:1.0:subject:subject-id",
        "urn:oasis:names:tc:xspa:1.0:subject:organization",
        "urn:oasis:names:tc:xspa:1.0:subject:organization-id",
        "urn:oasis:names:tc:xacml:2.0:subject:role",
        "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse",
        "urn:oasis:names:tc:xacml:1.0:resource:resource-id"
    };

    private final static String[] CODED_NAMES = {
        "urn:oasis:names:tc:xacml:2.0:subject:role",
        "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse"
    };

     private final static String[] CODED_NODE_NAMES = {
        "Role",
        "PurposeForUse"
    };

    // FIXME: Integrate
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
     */
    public List<Attribute> handle(STSRequestData requestData) throws STSException {
        OMElement claimsNode = requestData.getClaimsNode();
        List<Attribute> attributes = new ArrayList<Attribute>();
        if (claimsNode == null) {
            System.out.println("No Claims!");
            throw new STSException("No Claims specified");
        } else {
            System.out.println("Claims = " + claimsNode.toString());
            this.buildXSPAClaimTypeURIMap(claimsNode);
            List<Claim> claims = this.getClaims();
            attributes = this.getAttributes(claims);
            this.validate(attributes);
        }
        return attributes;
    }

    /**
     *
     * @param attributes
     * @throws STSException
     */
    private void validate(List<Attribute> attributes) throws STSException {
        // Validate proper attributes are available.

        for (int i = 0; i < XSPA_NAMES_TO_VALIDATE.length; i++) {
            String attributeNameToValidate = XSPA_NAMES_TO_VALIDATE[i];
            boolean foundAttributeName = false;
            for (Attribute attribute : attributes) {
                if (attribute.getName().equalsIgnoreCase(attributeNameToValidate)) {
                    foundAttributeName = true;
                    break;
                }
            }
            if (foundAttributeName == false) {
                System.out.println("Missing " + attributeNameToValidate + " attribute");
                throw new STSException("Missing " + attributeNameToValidate + " attribute");
            }
        }
    }

    /**
     *
     * @param claims
     */
    private void buildXSPAClaimTypeURIMap(OMElement claims) {
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

    /**
     *
     * @return
     */
    private List<Claim> getClaims() {
        List<Claim> claims = new ArrayList<Claim>();
        for (int i = 0; i < XSPA_NAMES.length; i++) {
            String xspaName = XSPA_NAMES[i];
            Claim claim = this.getClaim(xspaName);
            if (claim != null) {
                claims.add(claim);
            }
        }
        return claims;
    }

    /**
     * 
     * @return
     */
    private List<Attribute> getAttributes(List<Claim> claims) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (Claim claim : claims) {
            Attribute attribute = claim.getAttribute();
            attributes.add(attribute);
        }
        return attributes;
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
}
