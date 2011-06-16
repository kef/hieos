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

package com.vangent.hieos.services.xds.bridge.mapper;

import java.util.HashMap;
import java.util.Map;
import com.vangent.hieos.services.xds.bridge.utils.StringUtils;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XMLParser;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.log4j.Logger;
import org.jaxen.JaxenException;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-10
 * @author         Jim Horner
 */
public class ContentParser {

    /** Field description */
    public final static Logger logger = Logger.getLogger(ContentParser.class);

    /**
     * Constructs ...
     *
     *
     *
     */
    public ContentParser() {

        super();
    }

    /**
     * Method description
     *
     *
     *
     * @param config
     * @param input
     *
     * @return
     *
     * @throws XMLParserException
     * @throws XPathHelperException
     */
    public Map<String, String> parse(ContentParserConfig config, byte[] input)
            throws XMLParserException, XPathHelperException {

        Map<String, String> result = new HashMap<String, String>();

        OMElement node = XMLParser.bytesToOM(input);
        Map<String, String> expressions = config.getExpressionMap();

        for (Map.Entry<String, String> entry : expressions.entrySet()) {

            String expr = entry.getValue();

            result.put(entry.getKey(), parseText(config, node, expr));
        }

        return result;
    }

    /**
     * Method description
     *
     *
     *
     * @param config
     * @param elem
     * @param expr
     *
     * @return
     *
     * @throws XPathHelperException
     */
    private String parseText(ContentParserConfig config, OMElement elem,
                             String expr)
            throws XPathHelperException {

        String result = null;

        // TODO add this XPathHelper, similar to selectNodes()

        try {

            AXIOMXPath xpath = new AXIOMXPath(expr);

            Map<String, String> namespaces = config.getNamespaces();

            for (String prefix : namespaces.keySet()) {
                xpath.addNamespace(prefix, namespaces.get(prefix));
            }

            result = StringUtils.trimToEmpty(xpath.stringValueOf(elem));

            if (StringUtils.isNotBlank(result)) {

                logger.debug(String.format("*** Found nodes for XPATH: %s",
                                           expr));

            } else {

                logger.warn(
                    String.format(
                        "*** Could not find nodes for XPATH: %s", expr));
            }

        } catch (JaxenException e) {

            logger.error(e.getMessage(), e);

            throw new XPathHelperException(e.getMessage());
        }

        return result;
    }
}
