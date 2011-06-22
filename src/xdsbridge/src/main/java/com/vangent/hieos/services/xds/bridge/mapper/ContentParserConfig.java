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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Jim Horner
 */
public class ContentParserConfig {

    /** Field description */
    private final Map<String, String> expressionMap;

    /** Field description */
    private final String initializationErrors;

    /** Field description */
    private final Map<String, String> namespaces;

    /** Field description */
    private final Map<String, String> staticValues;

    /**
     * Constructs ...
     *
     *
     * @param ns
     * @param expressions
     * @param statVals
     */
    public ContentParserConfig(Map<String, String> ns,
                               Map<String, String> expressions,
                               Map<String, String> statVals) {

        this(ns, expressions, statVals, null);
    }

    /**
     * Constructs ...
     *
     *
     * @param ns
     * @param expressions
     * @param statVals
     * @param errors
     */
    public ContentParserConfig(Map<String, String> ns,
                               Map<String, String> expressions,
                               Map<String, String> statVals, String errors) {

        this.namespaces = new HashMap<String, String>();
        this.namespaces.putAll(ns);

        this.expressionMap = new HashMap<String, String>();
        this.expressionMap.putAll(expressions);

        this.staticValues = new HashMap<String, String>();
        this.staticValues.putAll(statVals);

        this.initializationErrors = errors;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Map<String, String> getExpressionMap() {
        return Collections.unmodifiableMap(expressionMap);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getInitializationErrors() {
        return initializationErrors;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Map<String, String> getNamespaces() {
        return Collections.unmodifiableMap(namespaces);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Map<String, String> getStaticValues() {
        return staticValues;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String[][] toPrefixURIArray() {

        Map<String, String> spaces = getNamespaces();
        String[][] result = new String[2][spaces.size()];

        int index = 0;

        for (Map.Entry<String, String> entry : spaces.entrySet()) {

            result[0][index] = entry.getKey();
            result[1][index] = entry.getValue();

            ++index;
        }

        return result;
    }
}
