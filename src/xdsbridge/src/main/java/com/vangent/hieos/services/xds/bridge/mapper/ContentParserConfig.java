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
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Vangent
 */
public class ContentParserConfig {

    /** Field description */
    private final Map<String, String> expressions;

    /** Field description */
    private String initializationErrors;

    /** Field description */
    private final ContentParserConfigName name;

    /** Field description */
    private final Map<String, String> namespaces;

    /** Field description */
    private final Map<String, Map<String, String>> staticValues;

    /** Field description */
    private final Map<String, String> contentConversions;

    /** Field description */
    private final String templateFileName;

    /**
     * Enum description
     *
     */
    public enum ContentParserConfigName { SharedHealthSummaryMapper,
            DischargeSummaryMapper }

    /**
     * Constructs ...
     *
     *
     *
     *
     *
     * @param inName
     * @param inNamespaces
     * @param inExpressions
     * @param inStaticValues
     * @param inConversions
     * @param inTemplateFileName
     */
    public ContentParserConfig(ContentParserConfigName inName,
                               Map<String, String> inNamespaces,
                               Map<String, String> inExpressions,
                               Map<String, Map<String, String>> inStaticValues,
                               Map<String, String> inContentConversions,
                               String inTemplateFileName) {

        super();

        this.name = inName;

        this.namespaces = new LinkedHashMap<String, String>();

        if (inNamespaces != null) {
            this.namespaces.putAll(inNamespaces);
        }

        this.expressions = new LinkedHashMap<String, String>();

        if (inExpressions != null) {
            this.expressions.putAll(inExpressions);
        }

        this.staticValues = new LinkedHashMap<String, Map<String, String>>();

        if (inStaticValues != null) {
            this.staticValues.putAll(inStaticValues);
        }

        this.contentConversions = new LinkedHashMap<String, String>();

        if (inContentConversions != null) {
            this.contentConversions.putAll(inContentConversions);
        }

        this.templateFileName = inTemplateFileName;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Map<String, String> getExpressions() {
        return Collections.unmodifiableMap(expressions);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Map<String, String> getContentConversions() {
        return Collections.unmodifiableMap(contentConversions);
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
    public Map<String, Map<String, String>> getStaticValues() {
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
     * Takes the other's map data members and does a putAll onto
     * this' map data members; overwriting at will.
     *
     *
     * @param other the other config to merge on top of this one
     */
    public void merge(ContentParserConfig other) {

        this.expressions.putAll(other.getExpressions());
        this.namespaces.putAll(other.getNamespaces());
        this.staticValues.putAll(other.getStaticValues());

        if (StringUtils.isNotBlank(other.getInitializationErrors())) {

            StringBuilder sb = new StringBuilder();

            sb.append(this.initializationErrors);
            sb.append("\n");
            sb.append(other.getInitializationErrors());

            this.initializationErrors = sb.toString();
        }
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
