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

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.services.sts.util.STSUtil;
import javax.xml.namespace.QName;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;

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
    private CodedValue codedValue = new CodedValue();
   

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
    public CodedValue getCodedValue() {
        return codedValue;
    }

    /**
     *
     * @return
     */
    @Override
    public Attribute getAttribute() throws STSException {
        org.opensaml.xml.XMLObjectBuilderFactory bf = STSUtil.getXMLObjectBuilderFactory();

        XMLObjectBuilder<XSAny> xsAnyBuilder = bf.getBuilder(XSAny.TYPE_NAME);
        XSAny xsAny = xsAnyBuilder.buildObject("urn:hl7-org:v3", this.getNodeName(), "hl7");
        xsAny.getUnknownAttributes().put(new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi"), "CE");
        xsAny.getUnknownAttributes().put(new QName("code"), codedValue.getCode());
        xsAny.getUnknownAttributes().put(new QName("codeSystem"), codedValue.getCodeSystem());
        xsAny.getUnknownAttributes().put(new QName("codeSystemName"), codedValue.getCodeSystemName());
        xsAny.getUnknownAttributes().put(new QName("displayName"), codedValue.getDisplayName());

        XSAny attributeValue = xsAnyBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attributeValue.getUnknownXMLObjects().add(xsAny);

        Attribute attribute = (Attribute) bf.getBuilder(Attribute.DEFAULT_ELEMENT_NAME).buildObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(this.getName());
        //attribute.setNameFormat("http://www.hhs.gov/healthit/nhin");
        attribute.getAttributeValues().add(attributeValue);
        return attribute;
    }

    @Override
    public String getStringValue() {
        return codedValue.getCode() + "@" + codedValue.getCodeSystem();
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
