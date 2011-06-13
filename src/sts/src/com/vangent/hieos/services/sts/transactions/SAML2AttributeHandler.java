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

import com.vangent.hieos.services.sts.model.STSConstants;
import com.vangent.hieos.services.sts.model.STSRequestData;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;

/**
 *
 * @author Bernie Thuman
 */
public class SAML2AttributeHandler {

    private final static String[] XSPA_NAMES = {
        "urn:oasis:names:tc:xacml:1.0:subject:subject-id",
        "urn:oasis:names:tc:xpsa:1.0:subject:organization",
        "urn:oasis:names:tc:xspa:1.0:subject:organization-id",
        "urn:oasis:names:tc:xspa:1.0:subject:hl7:permission",
        "urn:oasis:names:tc:xacml:2.0:subject:role",
        "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse",
        "urn:oasis:names:tc:xacml:1.0:resource:resource-id",
        "urn:oasis:names:tc:xspa:1.0:resource:hl7:type",
        "urn:oasis:names:tc:xspa:1.0:environment:locality",
        "urn:oasis:names:tc:xspa:2.0:subject:npi"
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
    public List<Attribute> handle(STSRequestData requestData) {
        OMElement claims = requestData.getClaims();
        List<Attribute> attributes = new ArrayList<Attribute>();
        if (claims == null) {
            System.out.println("No Claims!");
        } else {
            System.out.println("Claims = " + claims.toString());
            this.buildXSPAClaimTypeURIMap(claims);
            attributes = this.getAttributes();
        }
        return attributes;
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
     * @param attrCallback
     */
    private List<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (int i = 0; i < XSPA_NAMES.length; i++) {
            String xspaName = XSPA_NAMES[i];
            Attribute attribute = this.getSimpleStringAttribute(xspaName);
            if (attribute != null) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    /**
     *
     * @param name
     * @return
     */
    private Attribute getSimpleStringAttribute(String name) {

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

        // Now, create the SAML attribute.
        org.opensaml.xml.XMLObjectBuilderFactory bf = Configuration.getBuilderFactory();
        Attribute attribute = (Attribute) bf.getBuilder(Attribute.DEFAULT_ELEMENT_NAME).buildObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(name);
        XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
        XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        stringValue.setValue(xspaClaimValueText);
        attribute.getAttributeValues().add(stringValue);
        return attribute;
    }

    /**
     * FIXME: Uses NHIN-style coded values versus simply text as specified by XASP.
     *
     * @return
     */
    private Attribute getXSPAPurposeOfUseAttribute() {
        // <saml:Attribute Name="urn:oasis:names:tc:xspa:1.0:subject:purposeofuse">
        //    <saml:AttributeValue>
        //      <PurposeForUse xmlns="urn:hl7-org:v3" xsi:type="CE" code="OPERATIONS"
        //         codeSystem="2.16.840.1.113883.3.18.7.1" codeSystemName="nhin-purpose"
        //         displayName="Healthcare Operations"/>
        //    </saml:AttributeValue>
        // </saml:Attribute>

        org.opensaml.xml.XMLObjectBuilderFactory bf = Configuration.getBuilderFactory();
        XMLObjectBuilder<XSAny> xsAnyBuilder = bf.getBuilder(XSAny.TYPE_NAME);
        XSAny purposeOfUse = xsAnyBuilder.buildObject("urn:hl7-org:v3", "PurposeForUse", "hl7");
        purposeOfUse.getUnknownAttributes().put(new QName("xsi:type"), "CE");
        purposeOfUse.getUnknownAttributes().put(new QName("code"), "OPERATIONS");
        purposeOfUse.getUnknownAttributes().put(new QName("codeSystem"), "2.16.840.1.113883.3.18.7.1");
        purposeOfUse.getUnknownAttributes().put(new QName("codeSystemName"), "nhin-purpose");
        purposeOfUse.getUnknownAttributes().put(new QName("displayName"), "Healthcare Operations");

        XSAny purposeOfUseAttributeValue = xsAnyBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        purposeOfUseAttributeValue.getUnknownXMLObjects().add(purposeOfUse);

        Attribute attribute = (Attribute) bf.getBuilder(Attribute.DEFAULT_ELEMENT_NAME).buildObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName("urn:oasis:names:tc:xspa:1.0:subject:purposeofuse");
        //attribute.setNameFormat("http://www.hhs.gov/healthit/nhin");
        attribute.getAttributeValues().add(purposeOfUseAttributeValue);
        return attribute;
    }
}
