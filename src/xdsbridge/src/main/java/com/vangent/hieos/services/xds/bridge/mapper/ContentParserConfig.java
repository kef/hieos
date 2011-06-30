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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Jim Horner
 */
public class ContentParserConfig {

    /** Field description */
    private final Map<String, List<String>> expressionMap;

    /** Field description */
    private String initializationErrors;

    /** Field description */
    private final ContentParserConfigName name;

    /** Field description */
    private final Map<String, String> namespaces;

    /** Field description */
    private final Map<String, String> staticValues;

    /** Field description */
    private final String templateFileName;

    /**
     * Enum description
     *
     */
    public enum ContentParserConfigName { CDAToXDSMapper }

    /**
     * Constructs ...
     *
     *
     *
     *
     * @param name
     * @param namespaces
     * @param expressions
     * @param staticValues
     * @param templFileName
     */
    public ContentParserConfig(ContentParserConfigName name,
                               Map<String, String> namespaces,
                               Map<String, List<String>> expressions,
                               Map<String, String> staticValues,
                               String templFileName) {

        super();

        this.name = name;

        this.namespaces = new LinkedHashMap<String, String>();
        this.namespaces.putAll(namespaces);

        this.expressionMap = new LinkedHashMap<String, List<String>>();
        this.expressionMap.putAll(expressions);

        this.staticValues = new LinkedHashMap<String, String>();
        this.staticValues.putAll(staticValues);

        this.templateFileName = templFileName;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Map<String, List<String>> getExpressionMap() {
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
    public ContentParserConfigName getName() {
        return name;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Map<String, String> getNamespaces() {
        return Collections.unmodifiableMap(this.namespaces);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Map<String, String> getStaticValues() {
        return Collections.unmodifiableMap(staticValues);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getTemplateFileName() {
        return templateFileName;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    public String retrieveTemplateFileAsString() throws IOException {

        String result = null;

        String filename = getTemplateFileName();

        result = FileUtils.readFileToString(new File(filename));

        return result;
    }

    /**
     * Method description
     *
     *
     * @param initializationErrors
     */
    public void setInitializationErrors(String initializationErrors) {
        this.initializationErrors = initializationErrors;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String[][] toPrefixURIArrays() {

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
