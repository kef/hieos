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
    private final static Logger logger =
        Logger.getLogger(CDAToXDSContentParserConfigFactory.class);

    /**
     * Method description
     *
     *
     * @return
     */
    public static ContentParserConfig createConfig() {

        Map<String, String> expressions = new HashMap<String, String>();
        StringBuilder errors = new StringBuilder();

        ClassLoader cl =
            CDAToXDSContentParserConfigFactory.class.getClassLoader();
        Properties exprprops = new Properties();

        try {

            exprprops.load(cl.getResourceAsStream(EXPRESSIONPROPS));

        } catch (IOException e) {

            String msg = String.format("Error reading properties [%s].",
                                       EXPRESSIONPROPS);

            errors.append(msg);
            errors.append("\n");

            logger.error(msg, e);
        }

        for (String key : exprprops.stringPropertyNames()) {

            ContentVariableName varName = null;

            try {

                varName = ContentVariableName.valueOf(key);
                expressions.put(varName.toString(), exprprops.getProperty(key));

            } catch (IllegalArgumentException e) {

                String msg =
                    String.format("[%s] is an unknown field, skipping.", key);

                errors.append(msg);
                errors.append("\n");

                logger.warn(msg);
            }
        }

        Map<String, String> namespaces = new HashMap<String, String>();
        Properties nsprops = new Properties();

        try {

            nsprops.load(cl.getResourceAsStream(NAMESPACEPROPS));

        } catch (IOException e) {

            String msg = String.format("Error reading properties [%s].",
                                       NAMESPACEPROPS);

            errors.append(msg);
            errors.append("\n");

            logger.error(msg, e);
        }

        for (String key : nsprops.stringPropertyNames()) {

            namespaces.put(key, nsprops.getProperty(key));
        }

        return new ContentParserConfig(namespaces, expressions,
                                       errors.toString());
    }
}
