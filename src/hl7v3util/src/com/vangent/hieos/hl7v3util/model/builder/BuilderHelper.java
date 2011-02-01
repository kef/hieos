/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
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
        node.addAttribute(attributeName, attributeValue, null);
    }
   
}
