/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.template;

import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.xml.XMLParser;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.axiom.om.OMElement;

/**
 * Class that allows variables to be replaced in template strings in a relatively simple
 * programmatic manner.
 *
 * A sample template string is below (could have been read from a file):
 *
 * <RetrieveDocumentSetRequest xmlns="urn:ihe:iti:xds-b:2007">
 *   <DocumentRequest>
 *       <HomeCommunityId>{HOME_COMMUNITY_ID}</HomeCommunityId>
 *       <RepositoryUniqueId>{REPOSITORY_UNIQUE_ID}</RepositoryUniqueId>
 *       <DocumentUniqueId>{DOCUMENT_UNIQUE_ID}</DocumentUniqueId>
 *   </DocumentRequest>
 * </RetrieveDocumentSetRequest>
 *
 * Notice that template variables begin with "{" and end with "}".  Variable names are within
 * the brackets.
 *
 * @author Bernie Thuman
 */
public class TemplateUtil {

    /**
     * Return a string with template variables replaced.
     *
     * @param text The template with template variables to replace.
     * @param replacements Map of values by template key.  For example, "HOME_COMMUNITY_ID" would be
     *        the name of a template variable in the map.  In the template string, it would be
     *        represented as "{HOME_COMMUNITY_ID}".
     * @return The result (a String) of the template substitution.
     */
    public static String replaceTemplateVariables(
            String text,
            Map<String, String> replacements) {
        StringBuilder output = new StringBuilder();
        Pattern tokenPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher tokenMatcher = tokenPattern.matcher(text);
        int cursor = 0;
        while (tokenMatcher.find()) {
            // A token is defined as a sequence of the format "{...}".
            // A key is defined as the content between the brackets.
            int tokenStart = tokenMatcher.start();
            int tokenEnd = tokenMatcher.end();
            int keyStart = tokenMatcher.start(1);
            int keyEnd = tokenMatcher.end(1);
            output.append(text.substring(cursor, tokenStart));
            String token = text.substring(tokenStart, tokenEnd);
            String key = text.substring(keyStart, keyEnd);
            if (replacements.containsKey(key)) {
                String value = replacements.get(key);
                output.append(value);
            } else {
                output.append(token);
            }
            cursor = tokenEnd;
        }
        output.append(text.substring(cursor));
        return output.toString();
    }

    /**
     * Return an OMElement (in this case, from an XML string) with template variables replaced.
     *
     * @param text The template with template variables to replace.
     * @param replacements Map of values by template key.  For example, "HOME_COMMUNITY_ID" would be
     *        the name of a template variable in the map.  In the template string, it would be
     *        represented as "{HOME_COMMUNITY_ID}".
     * @return The result (an OMElement) of the template substitution.
     */
    public static OMElement getOMElementFromTemplate(String template,
            Map<String, String> replacements) {
        OMElement result = null;
        String expandedText = TemplateUtil.replaceTemplateVariables(template, replacements);
        try {
            result = XMLParser.stringToOM(expandedText);
        } catch (XMLParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
}
