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

    /**
     * Constructs ...
     *
     *
     * @param ns
     * @param expressions
     */
    public ContentParserConfig(Map<String, String> ns,
                               Map<String, String> expressions) {

        this(ns, expressions, null);
    }

    /**
     * Constructs ...
     *
     *
     * @param ns
     * @param expressions
     * @param errors
     */
    public ContentParserConfig(Map<String, String> ns,
                               Map<String, String> expressions, String errors) {

        this.namespaces = new HashMap<String, String>();
        this.namespaces.putAll(ns);

        this.expressionMap = new HashMap<String, String>();
        this.expressionMap.putAll(expressions);

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
    public Map<String, String> getNamespaces() {
        return Collections.unmodifiableMap(namespaces);
    }
}
