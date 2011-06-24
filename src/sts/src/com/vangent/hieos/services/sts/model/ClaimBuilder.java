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

import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.util.PolicyConfig;
import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class ClaimBuilder {

    /**
     *
     * @param requestData
     * @return
     * @throws STSException
     */
    public List<Claim> parse(STSRequestData requestData) throws STSException {
        OMElement claimsNode = requestData.getClaimsNode();
        List<Claim> claims = new ArrayList<Claim>();
        this.parse(claimsNode, claims);
        STSConfig stsConfig = requestData.getSTSConfig();
        if (stsConfig.getValidateRequiredClaims() == true) {
            this.validate(claims);
        }
        return claims;
    }

    /**
     *
     * @param claimsNode
     * @param claims
     */
    private void parse(OMElement claimsNode, List<Claim> claims) {
        try {
            // Get all ClaimType nodes.
            List<OMElement> claimTypeNodes = XPathHelper.selectNodes(claimsNode, "./ns:ClaimType", PolicyConstants.XSPA_CLAIMS_NS);
            for (OMElement claimTypeNode : claimTypeNodes) {
                Claim claim = this.getClaim(claimTypeNode);
                if (claim != null) {
                    claims.add(claim);
                }
            }
        } catch (XPathHelperException ex) {
            // FIXME: Do something here.
        }
    }

    /**
     *
     * @param claims
     * @throws STSException
     */
    private void validate(List<Claim> claims) throws STSException {
        // Validate proper attributes are available.
        PolicyConfig pConfig;
        try {
            pConfig = PolicyConfig.getInstance();
        } catch (PolicyException ex) {
            // rethrow
            throw new STSException(ex.getMessage());
        }
        String[] requiredIds = pConfig.getRequiredClaimIds();
        for (int i = 0; i < requiredIds.length; i++) {
            String nameToValidate = requiredIds[i];
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
     * @param claimTypeNode
     * @return
     */
    private Claim getClaim(OMElement claimTypeNode) {
        String claimTypeURI = claimTypeNode.getAttributeValue(new QName("Uri"));

        // Found ClaimType ... now, get its ClaimValue.
        OMElement xspaClaimValueNode = claimTypeNode.getFirstChildWithName(new QName(PolicyConstants.XSPA_CLAIMS_NS, "ClaimValue"));
        if (xspaClaimValueNode == null) {
            // FIXME: PUT DEBUG/DEFAULT?
            return null; // Get out.
        }

        // Get the String value.
        String valueText = xspaClaimValueNode.getText();

        // FIXME: Only dealing with simple single value string claims.
        SimpleStringClaim claim = new SimpleStringClaim();
        claim.setName(claimTypeURI);
        claim.setValue(valueText);
        return claim;
    }
}
