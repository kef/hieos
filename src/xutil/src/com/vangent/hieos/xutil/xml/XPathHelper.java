/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010-2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.xml;

import com.vangent.hieos.xutil.exception.XPathHelperException;
import java.util.List;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.log4j.Logger;
import org.jaxen.JaxenException;

/**
 * A utility to simplify XPath usage.
 *
 * @author Bernie Thuman
 */
public class XPathHelper {

    private final static Logger logger = Logger.getLogger(XPathHelper.class);

    /**
     * Return a single OMElement given the XPATH expression.
     *
     * @param rootNode The starting node (OMElement) for the search.
     * @param xpathExpression The XPATH expression.
     * @param namespaceURI  Value of namespace to use (e.g. "urn:hl7-org:v3"). May be null.
     * @return OMElement null if not found.
     * @throws XPathHelperException
     */
    static public OMElement selectSingleNode(
            OMElement rootNode, String xpathExpression, String namespaceURI) throws XPathHelperException {
        String[] namespacePrefixes = new String[1];
        String[] namespaceURIs = new String[1];
        namespacePrefixes[0] = "ns";
        namespaceURIs[0] = namespaceURI;
        return XPathHelper.selectSingleNode(rootNode, xpathExpression, namespacePrefixes, namespaceURIs);
    }

    /**
     * Return a single OMElement given the XPATH expression.
     *
     * @param rootNode The starting node (OMElement) for the search.
     * @param xpathExpression The XPATH expression.
     * @param namespacePrefixes List of namespace prefixes.
     * @param namespaceURIs List of namespace URIs (parallel array to namespacePrefixes).
     * @return OMElement null if not found.
     * @throws XPathHelperException
     */
    static public OMElement selectSingleNode(
            OMElement rootNode, String xpathExpression, String[] namespacePrefixes, String[] namespaceURIs) throws XPathHelperException {
        OMElement resultNode = null;
        try {
            AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
           for (int i = 0; i < namespacePrefixes.length; i++) {
                xpath.addNamespace(namespacePrefixes[i], namespaceURIs[i]);
            }
            resultNode = (OMElement) xpath.selectSingleNode(rootNode);
            if (resultNode != null) {
                logger.debug("*** Found node for XPATH: " + xpathExpression);
            } else {
                logger.error("*** Could not find node for XPATH: " + xpathExpression);
            }
        } catch (JaxenException e) {
            throw new XPathHelperException(e.getMessage());
        }
        return resultNode;
    }


    /**
     * Return a List of OMElements given the XPATH expression.
     *
     * @param rootNode The starting node (OMElement) for the search.
     * @param xpathExpression The XPATH expression.
     * @param namespaceURI  Value of namespace URI to use (e.g. "urn:hl7-org:v3").  May be null.
     * @return List<OMElement> null if not found.
     */
    static public List<OMElement> selectNodes(
            OMElement rootNode, String xpathExpression, String namespaceURI) throws XPathHelperException {
        String[] namespacePrefixes = new String[1];
        String[] namespaceURIs = new String[1];
        namespacePrefixes[0] = "ns";
        namespaceURIs[0] = namespaceURI;
        return XPathHelper.selectNodes(rootNode, xpathExpression, namespacePrefixes, namespaceURIs);
    }

    /**
     * Return a List of OMElements given the XPATH expression.
     *
     * @param rootNode The starting node (OMElement) for the search.
     * @param xpathExpression The XPATH expression.
     * @param namespacePrefixes List of namespace prefixes.
     * @param namespaceURIs List of namespace URIs (parallel array to namespacePrefixes).
     * @return List<OMElement> null if not found.
     * @throws XPathHelperException
     */
    static public List<OMElement> selectNodes(
            OMElement rootNode, String xpathExpression, String[] namespacePrefixes, String[] namespaceURIs) throws XPathHelperException {
        List resultNodes = null;
        try {
            AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
            for (int i = 0; i < namespacePrefixes.length; i++) {
                xpath.addNamespace(namespacePrefixes[i], namespaceURIs[i]);
            }
            resultNodes = xpath.selectNodes(rootNode);
            if (resultNodes != null) {
                logger.debug("*** Found nodes for XPATH: " + xpathExpression);
            } else {
                logger.error("*** Could not find nodes for XPATH: " + xpathExpression);
            }
        } catch (JaxenException e) {
            throw new XPathHelperException(e.getMessage());
        }
        return resultNodes;
    }

    /**
     * Return a string result given the XPATH expression.
     *
     * @param rootNode The starting node (OMElement) for the search.
     * @param xpathExpression The XPATH expression.
     * @param nameSpace  Value of name space to use (e.g. "urn:hl7-org:v3").  May be null.
     * @return String null if not found.
     */
    static public String stringValueOf(OMElement rootNode, String xpathExpression, String nameSpace) throws XPathHelperException {
        String result = null;
        try {
            AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
            if (nameSpace != null) {
                xpath.addNamespace("ns", nameSpace /* "urn:hl7-org:v3" */);
            }
            result = xpath.stringValueOf(rootNode);
            if (result != null) {
                logger.debug("*** Found nodes for XPATH: " + xpathExpression);
            } else {
                logger.error("*** Could not find nodes for XPATH: " + xpathExpression);
            }
        } catch (JaxenException e) {
            throw new XPathHelperException(e.getMessage());
        }
        return result;
    }
}
