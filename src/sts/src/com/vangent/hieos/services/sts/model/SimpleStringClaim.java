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

import org.opensaml.Configuration;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;

/**
 *
 * @author Bernie Thuman
 */
public class SimpleStringClaim extends Claim {

    private String value;

    /**
     *
     * @return
     */
    @Override
    public Attribute getAttribute() {

        // Create SAML attribute from Claim.
        org.opensaml.xml.XMLObjectBuilderFactory bf = Configuration.getBuilderFactory();
        Attribute attribute = (Attribute) bf.getBuilder(Attribute.DEFAULT_ELEMENT_NAME).buildObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(this.getName());
        XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
        XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        stringValue.setValue(this.getValue());
        attribute.getAttributeValues().add(stringValue);
        return attribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getStringValue() {
        return this.getValue();
    }
}
