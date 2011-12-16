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

import com.vangent.hieos.services.xds.bridge.utils.ContentConverterUtils;
import java.util.HashMap;
import java.util.Map;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XMLParser;
import com.vangent.hieos.xutil.xml.XPathHelper;
import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-10
 * @author         Jim Horner
 */
public class ContentParser {

    /** Field description */
    public static final Logger logger = Logger.getLogger(ContentParser.class);

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

        String[][] prefixUris = config.toPrefixURIArrays();
        String[] prefixes = prefixUris[0];
        String[] uris = prefixUris[1];

        Map<String, String> expressions = config.getExpressions();
        Map<String, String> contentConversions = config.getContentConversions();
        for (Map.Entry<String, String> entry : expressions.entrySet()) {
            String expression = entry.getValue();
            String value = parseText(node, expression, prefixes, uris);
            String variableKey = entry.getKey();
            // See if we need to run a converter.
            if (contentConversions != null) {
                String contentConversion = contentConversions.get(variableKey);
                if (contentConversion != null) {
                    value = ContentConverterUtils.convert(variableKey, value, contentConversion);
                }
            }
            result.put(variableKey, value);
        }

        return result;
    }

    /**
     * Method description
     *
     *
     *
     * @param elem
     * @param expr
     * @param prefixes
     * @param uris
     *
     * @return
     *
     * @throws XPathHelperException
     */
    private String parseText(OMElement elem, String expr, String[] prefixes,
            String[] uris)
            throws XPathHelperException {

        String result = XPathHelper.stringValueOf(elem, expr, prefixes, uris);

        return StringUtils.trimToEmpty(result);
    }
}
