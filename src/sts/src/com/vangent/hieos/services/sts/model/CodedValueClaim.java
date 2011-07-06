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

import javax.xml.namespace.QName;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.schema.XSAny;

// Examples:
//
// <saml:Attribute Name="urn:oasis:names:tc:xspa:1.0:subject:purposeofuse">
//   <saml:AttributeValue>
//     <PurposeForUse xmlns="urn:hl7-org:v3" xsi:type="CE" code="TREATMENT"
//        codeSystem="2.16.840.1.113883.3.18.7.1"
//        codeSystemName="nhin-purpose" displayName="Treatment"/>
//   </saml:AttributeValue>
// </saml:Attribute>
//
// <saml:Attribute Name="urn:oasis:names:tc:xacml:2.0:subject:role">
//   <saml:AttributeValue>
//     <Role xmlns="urn:hl7-org:v3" xsi:type="CE" code="112247003"
//        codeSystem="2.16.840.1.113883.6.96"
//        codeSystemName="SNOMED CT" displayName="Medical doctor"/>
//   </saml:AttributeValue>
// </saml:Attribute>
//

/**
 *
 * @author Bernie Thuman
 */
public class CodedValueClaim extends Claim {

    private String nodeName;
    private String code;
    private String codeSystem;
    private String codeSystemName;
    private String displayName;

    /**
     *
     * @return
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     *
     * @param nodeName
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     *
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     *
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     *
     * @return
     */
    public String getCodeSystem() {
        return codeSystem;
    }

    /**
     *
     * @param codeSystem
     */
    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    /**
     *
     * @return
     */
    public String getCodeSystemName() {
        return codeSystemName;
    }

    /**
     * 
     * @param codeSystemName
     */
    public void setCodeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
    }

    /**
     *
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     *
     * @return
     */
    @Override
    // FIXME: Fully implement and implement based upon configuration.
    public Attribute getAttribute() {
        org.opensaml.xml.XMLObjectBuilderFactory bf = Configuration.getBuilderFactory();
        XMLObjectBuilder<XSAny> xsAnyBuilder = bf.getBuilder(XSAny.TYPE_NAME);
        XSAny purposeOfUse = xsAnyBuilder.buildObject("urn:hl7-org:v3", this.getNodeName(), "hl7");
        purposeOfUse.getUnknownAttributes().put(new QName("xsi:type"), "CE");
        purposeOfUse.getUnknownAttributes().put(new QName("code"), this.getCode());
        purposeOfUse.getUnknownAttributes().put(new QName("codeSystem"), this.getCodeSystem());
        purposeOfUse.getUnknownAttributes().put(new QName("codeSystemName"), this.getCodeSystemName());
        purposeOfUse.getUnknownAttributes().put(new QName("displayName"), this.getDisplayName());

        XSAny attributeValue = xsAnyBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attributeValue.getUnknownXMLObjects().add(purposeOfUse);

        Attribute attribute = (Attribute) bf.getBuilder(Attribute.DEFAULT_ELEMENT_NAME).buildObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(this.getName());
        //attribute.setNameFormat("http://www.hhs.gov/healthit/nhin");
        attribute.getAttributeValues().add(attributeValue);
        return attribute;
    }

    @Override
    public String getStringValue() {
        // TBD: Implement ...
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
