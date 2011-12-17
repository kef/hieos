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
package com.vangent.hieos.hl7v3util.model.builder;

import com.vangent.hieos.xutil.xml.XPathHelper;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAbstractFactory;

/**
 *
 * @author Bernie Thuman
 */
public class BuilderHelper {

    public final static String HL7V3_NAMESPACE = "urn:hl7-org:v3";
    private OMFactory omfactory;
    private OMNamespace ns;

    /**
     * 
     */
    public BuilderHelper() {
        omfactory = OMAbstractFactory.getOMFactory();
        ns = omfactory.createOMNamespace(HL7V3_NAMESPACE, "");
    }

    /**
     *
     * @param uri
     * @param prefix
     * @return
     */
    protected OMNamespace createOMNamespace(String uri, String prefix) {
        return omfactory.createOMNamespace(uri, prefix);
    }

    /**
     *
     * @param parentNode
     * @param name
     * @return
     */
    protected OMElement getFirstChildNodeWithName(OMElement parentNode, String name) {
        return parentNode.getFirstChildWithName(new QName(HL7V3_NAMESPACE, name));
    }

    /**
     *
     * @param rootNode
     * @param xpathExpression
     * @param attributeName
     * @return
     */
    protected String getNodeAttributeValue(OMElement rootNode, String xpathExpression, String attributeName) {
        OMElement node = null;
        try {
            node = this.selectSingleNode(rootNode, xpathExpression);
        } catch (XPathHelperException ex) {
            // TBD: Do something???
        }
        String value = null;
        if (node != null) {
            value = node.getAttributeValue(new QName(attributeName));
        }
        return value;
    }

    /**
     *
     * @param rootNode
     * @param xpathExpression
     * @return
     */
    protected String getNodeText(OMElement rootNode, String xpathExpression) {
        OMElement node = null;
        try {
            node = this.selectSingleNode(rootNode, xpathExpression);
        } catch (XPathHelperException ex) {
            // TBD: Do something???
        }
        String value = null;
        if (node != null) {
            value = node.getText();
        }
        return value;
    }

    /**
     *
     * @param parentNode
     * @param name
     * @return
     */
    protected String getFirstChildNodeValue(OMElement parentNode, String name) {
        String result = null;
        OMElement childNode = this.getFirstChildNodeWithName(parentNode, name);
        if (childNode != null) {
            result = childNode.getText();
        }
        return result;
    }

    /**
     * 
     * @param rootNode
     * @param xpathExpression
     * @return
     * @throws XPathHelperException
     */
    protected OMElement selectSingleNode(OMElement rootNode, String xpathExpression) throws XPathHelperException {
        return XPathHelper.selectSingleNode(rootNode, xpathExpression, HL7V3_NAMESPACE);
    }

    /**
     *
     * @param rootNode
     * @param xpathExpression
     * @return
     * @throws XPathHelperException
     */
    protected List<OMElement> selectNodes(OMElement rootNode, String xpathExpression) throws XPathHelperException {
        return XPathHelper.selectNodes(rootNode, xpathExpression, HL7V3_NAMESPACE);
    }

    /**
     *
     * @param elementName
     * @return
     */
    protected OMElement createOMElement(String elementName) {
        return omfactory.createOMElement(elementName, ns);
    }

    /**
     *
     * @param rootNode
     * @param elementName
     * @return
     */
    protected OMElement addChildOMElement(OMElement rootNode, String elementName) {
        OMElement childNode = this.createOMElement(elementName);
        rootNode.addChild(childNode);
        return childNode;
    }

    /**
     *
     * @param rootNode
     * @param elementName
     * @param elementValue
     * @return
     */
    protected OMElement addChildOMElementWithValue(OMElement rootNode, String elementName, String elementValue) {
        OMElement childNode = this.createOMElement(elementName);
        rootNode.addChild(childNode);
        childNode.setText(elementValue);
        return childNode;
    }

    /**
     * 
     * @param node
     * @param attributeName
     * @param attributeValue
     */
    protected void setAttribute(OMElement node, String attributeName, String attributeValue) {
        if (attributeValue != null) {
            node.addAttribute(attributeName, attributeValue, null);
        }
    }

    /**
     * 
     * @param rootNode
     * @param xPath
     * @return
     */
    protected Boolean getBooleanValue(OMElement rootNode, String xPath) {
        Boolean booleanVal = null;
        try {
            OMElement node = this.selectSingleNode(rootNode, xPath);
            if (node != null) {
                String textValue = node.getAttributeValue(new QName("value"));
                booleanVal = Boolean.valueOf(textValue);
            }
        } catch (XPathHelperException ex) {
            // TBD: Do something.
        }
        return booleanVal;
    }

    /**
     *
     * @param rootNode
     * @param xPath
     * @return
     */
    protected Integer getIntegerValue(OMElement rootNode, String xPath) {
        Integer integerVal = null;
        try {
            OMElement node = this.selectSingleNode(rootNode, xPath);
            if (node != null) {
                String textValue = node.getAttributeValue(new QName("value"));
                integerVal = Integer.valueOf(textValue);
            }
        } catch (XPathHelperException ex) {
            // TBD: Do something.
        }
        return integerVal;
    }

    /**
     *
     * @param rootNode
     * @param xPath
     * @return
     */
    protected Date getHL7DateValue(OMElement rootNode, String xPath) {
        Date dateVal = null;
        try {
            OMElement node = this.selectSingleNode(rootNode, xPath);
            if (node != null) {
                String textValue = node.getAttributeValue(new QName("value"));
                dateVal = this.getHL7Date(textValue);
            }
        } catch (XPathHelperException ex) {
            // TBD: Do something.
        }
        return dateVal;
    }

    /**
     *
     * @param hl7Date
     * @return
     */
    public Date getHL7Date(String hl7DateText) {
        Date hl7Date = null;
        if (hl7DateText != null && hl7DateText.length() >= 8) {
            hl7DateText = hl7DateText.substring(0, 8);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            try {
                hl7Date = sdf.parse(hl7DateText);
            } catch (ParseException ex) {
                // Do nothing.
            }
        } else {
            // TBD: EMIT WARNING OF SOME SORT.
        }
        return hl7Date;
    }

    /**
     *
     * @param rootNode
     * @param childNodeName
     * @param value
     * @return
     */
    public OMElement addChildNodeWithBooleanValueAttribute(OMElement rootNode, String childNodeName, Boolean value) {
        OMElement childNode = null;
        if (value != null) {
            childNode = this.addChildOMElement(rootNode, childNodeName);
            this.setAttribute(childNode, "value", value.toString());
        }
        return childNode;
    }

    /**
     *
     * @param rootNode
     * @param childNodeName
     * @param value
     * @return
     */
    public OMElement addChildNodeWithIntegerValueAttribute(OMElement rootNode, String childNodeName, Integer value) {
        OMElement childNode = null;
        if (value != null) {
            childNode = this.addChildOMElement(rootNode, childNodeName);
            this.setAttribute(childNode, "value", value.toString());
        }
        return childNode;
    }

    /**
     *
     * @param rootNode
     * @param childNodeName
     * @param value
     * @return
     */
    public OMElement addChildNodeWithDateValueAttribute(OMElement rootNode, String childNodeName, Date value) {
        OMElement childNode = null;
        if (value != null) {
            childNode = this.addChildOMElement(rootNode, childNodeName);
            this.setAttribute(childNode, "value", Hl7Date.toHL7format(value));
        }
        return childNode;
    }

    /**
     * 
     * @param rootNode
     * @param childNodeName
     * @param value
     * @return
     */
    public OMElement addChildNodeWithValueAttribute(OMElement rootNode, String childNodeName, String value) {
        OMElement childNode = this.addChildOMElement(rootNode, childNodeName);
        this.setAttribute(childNode, "value", value);
        return childNode;
    }
}
