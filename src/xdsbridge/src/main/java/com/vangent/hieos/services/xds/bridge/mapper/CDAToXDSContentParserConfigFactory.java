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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-10
 * @author         Jim Horner
 */
public class CDAToXDSContentParserConfigFactory {

    /** Field description */
    private final static String EXPRESSIONPROPS =
        "META-INF/templates/CDAToXDSExpressions.properties";

    /** Field description */
    private final static String NAMESPACEPROPS =
        "META-INF/templates/CDAToXDSNamespaces.properties";

    /** Field description */
    private final static String STATICPROPS =
        "META-INF/templates/CDAToXDSStaticValues.properties";

    /** Field description */
    private final static Logger logger =
        Logger.getLogger(CDAToXDSContentParserConfigFactory.class);

    /**
     * Method description
     *
     *
     * @return
     */
    public static ContentParserConfig createConfig() {

        StringBuilder errors = new StringBuilder();

        Map<String, String> expressions = readPropertyFile(EXPRESSIONPROPS,
                                              errors, true);
        Map<String, String> namespaces = readPropertyFile(NAMESPACEPROPS,
                                             errors, false);
        Map<String, String> staticVals = readPropertyFile(STATICPROPS, errors,
                                             true);

        return new ContentParserConfig(namespaces, expressions, staticVals,
                                       errors.toString());
    }

    /**
     * Method description
     *
     *
     * @param filename
     * @param errors
     * @param checkKey
     *
     * @return
     */
    private static Map<String, String> readPropertyFile(String filename,
            StringBuilder errors, boolean checkKey) {

        Map<String, String> result = new HashMap<String, String>();
        Properties props = new Properties();

        ClassLoader cl =
            CDAToXDSContentParserConfigFactory.class.getClassLoader();

        try {

            props.load(cl.getResourceAsStream(filename));

        } catch (IOException e) {

            String msg = String.format("Error reading properties [%s].",
                                       filename);

            errors.append(msg);
            errors.append("\n");

            logger.error(msg, e);
        }

        for (String key : props.stringPropertyNames()) {

            if (checkKey) {

                try {

                    ContentVariableName varName =
                        ContentVariableName.valueOf(key);

                    result.put(varName.toString(), props.getProperty(key));

                } catch (IllegalArgumentException e) {

                    String msg =
                        String.format("[%s] is an unknown field, skipping.",
                                      key);

                    errors.append(msg);
                    errors.append("\n");

                    logger.warn(msg);
                }

            } else {

                result.put(key, props.getProperty(key));
            }
        }

        return result;
    }
}
