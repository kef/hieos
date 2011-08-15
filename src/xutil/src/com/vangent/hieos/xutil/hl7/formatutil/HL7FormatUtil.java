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
package com.vangent.hieos.xutil.hl7.formatutil;

import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;

/**
 * Includes a set of utility methods to validate and parse HL7v2 formatted strings.
 *
 * @author Bernie Thuman
 */
public class HL7FormatUtil {

    private static final String COMPONENT_SEPARATOR = "\\^";
    private static final String SUB_COMPONENT_SEPARATOR = "\\&";

    /**
     * Verify that given value is in CX format where:
     *   Component 1 (ID): REQUIRED
     *   Component 4 (Assigning Authority): REQUIRED
     *
     * Example: 5cfe5f4f31604fa^^^&1.3.6.1.4.1.21367.2005.3.7&ISO
     *
     * @param value CX formatted value.
     * @return boolean
     */
    public static boolean isCX_Formatted(String value) {
        int ASSIGNING_AUTHORITY_COMPONENT = 4;
        int ASSIGNING_AUTHORITY_NUM_COMPONENTS = 3;

        // Split into component parts.
        String components[] = value.split(COMPONENT_SEPARATOR);
        if (components.length == ASSIGNING_AUTHORITY_COMPONENT) {

            // Assigning authority.
            String aa[] = components[ASSIGNING_AUTHORITY_COMPONENT - 1].split(SUB_COMPONENT_SEPARATOR);
            if (aa.length == ASSIGNING_AUTHORITY_NUM_COMPONENTS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verify that given value only includes an XCN identifier where:
     *   Component 1 (ID): REQUIRED
     *   Component 9 (Assigning Authority): OPTIONAL
     *   Other components: NOT ALLOWED
     *
     * @param value XCN formatted string.
     * @return boolean
     */
    public static boolean isXCN_Identifier(String value) {
        int ID_COMPONENT = 1;
        int ASSIGNING_AUTHORITY_COMPONENT = 9;

        // Split into component parts.
        String[] components = value.split(COMPONENT_SEPARATOR);
        if (components.length == 0) {
            return false;  // Nothing specified.
        }
        // Component 1 = id
        String id = components[ID_COMPONENT - 1].trim();
        if (id.length() == 0) {
            return false;  // No id.
        }

        // Component 9 = assigning authority (optional)
        // Only allow data in component 1 and 9 (optional).
        int[] excludeComponents = {ID_COMPONENT, ASSIGNING_AUTHORITY_COMPONENT};
        return validateComponentsAreEmpty(components, excludeComponents);
    }

    /**
     * Get XCN formatted identifier from given value where:
     *   Component 1 (ID): REQUIRED
     *   Component 9 (Assigning Authority): OPTIONAL
     *
     * @param value XCN formatted string.
     * @return XCN formatted string with ID portions only.
     */
    public static String getXCN_Identifier(String value) {
        int ID_COMPONENT = 1;
        int ASSIGNING_AUTHORITY_COMPONENT = 9;

        // Split into component parts.
        String[] components = value.split(COMPONENT_SEPARATOR);
        if (components.length == 0) {
            return "UNKNOWN_ID";
        }
        // Component 1 = id
        String id = components[ID_COMPONENT - 1].trim();
        if (id.length() == 0) {
            return "UNKNOWN_ID";
        }
        // Component 9 = assigning authority
        String assigningAuthority = null;
        if (components.length >= ASSIGNING_AUTHORITY_COMPONENT) {
            assigningAuthority = components[ASSIGNING_AUTHORITY_COMPONENT - 1].trim();
        }
        if (assigningAuthority == null || assigningAuthority.length() == 0) {
            // Just return id if no assigning authority is specified.
            return id;
        } else {
            // Return id with assigning authority.
            return id + "^^^^^^^^" + assigningAuthority;
        }
    }

    /**
     * Verify that given value only includes an XON identifier where:
     *   Component 6 (Assigning Authority): OPTIONAL
     *   Component 10 (ID): REQUIRED
     *   Other components: NOT ALLOWED
     *
     * Only Example 2 & 3 are acceptable here.
     *   Example 1: Some Hospital
     *   Example 2: ^^^^^^^^^1.2.3.4.5.6.7.8.9.1789.45
     *   Example 3: ^^^^^&1.2.3.4.5.6.7.8.9.1789&ISO^^^^45
     *
     * @param value XON formatted string.
     * @return boolean
     */
    public static boolean isXON_Identifier(String value) {
        int ASSIGNING_AUTHORITY_COMPONENT = 6;
        int ID_COMPONENT = 10;

        // Split into component parts.
        String[] components = value.split(COMPONENT_SEPARATOR);
        if (components.length < ID_COMPONENT) {
            return false;
        }
        // Must have an id (component 10).
        String id = components[ID_COMPONENT - 1].trim();
        if (id.length() == 0) {
            // No id.
            return false;
        }
        // Only allow data in component 6 (optional) and 10.
        int[] excludeComponents = {ASSIGNING_AUTHORITY_COMPONENT, ID_COMPONENT};
        return validateComponentsAreEmpty(components, excludeComponents);
    }

    /**
     * Get XON formatted identifier from given value where:
     *   Component 6 (Assigning Authority): OPTIONAL
     *   Component 10 (ID): REQUIRED
     *
     * Only Example 2 & 3 are acceptable here.
     *   Example 1: Some Hospital
     *   Example 2: ^^^^^^^^^1.2.3.4.5.6.7.8.9.1789.45
     *   Example 3: ^^^^^&1.2.3.4.5.6.7.8.9.1789&ISO^^^^45
     *
     * @param value XON formatted string.
     * @return XON formatted string with ID portions only.
     */
    public static String getXON_Identifier(String value) {
        int ASSIGNING_AUTHORITY_COMPONENT = 6;
        int ID_COMPONENT = 10;

        // Split into component parts.
        String[] components = value.split(COMPONENT_SEPARATOR);
        if (components.length < ID_COMPONENT) {
            return "^^^^^^^^^UNKNOWN_ID";
        }
        // Component 10 = id
        String id = components[ID_COMPONENT - 1].trim();
        if (id.length() == 0) {
            return "^^^^^^^^^UNKNOWN_ID";
        }
        // Component 6 = assigning authority (optional).
        String assigningAuthority = components[ASSIGNING_AUTHORITY_COMPONENT - 1].trim();
        if (assigningAuthority.length() == 0) {
            // Just return id.
            return "^^^^^^^^^" + id;
        } else {
            // Return id with assigning authority.
            return "^^^^^" + assigningAuthority + "^^^^" + id;
        }
    }

    /**
     * Verify that given value only includes an CNE coded value where:
     *   Component 1 (code): REQUIRED
     *   Component 3 (codingSystem): OPTIONAL
     *   Other components: NOT ALLOWED
     *
     * Only Example 1 & 2 are acceptable here.
     *   Example 1: TREATMENT
     *   Example 2: TREATMENT^^2.16.840.1.113883.3.18.7.1
     *
     * @param value CNE formatted string.
     * @return boolean
     */
    public static boolean isCNE_Code(String value) {
        int CODE_COMPONENT = 1;
        int CODING_SCHEME_COMPONENT = 3;

        // Split into component parts.
        String[] components = value.split(COMPONENT_SEPARATOR);
        if (components.length == 0) {
            return false;  // Nothing specified.
        }
        // Component 1 = code
        String code = components[CODE_COMPONENT - 1].trim();
        if (code.length() == 0) {
            return false;  // No code.
        }

        // Component 3 = coding system (optional)
        // Only allow data in component 1 and 3 (optional).
        int[] excludeComponents = {CODE_COMPONENT, CODING_SCHEME_COMPONENT};
        return validateComponentsAreEmpty(components, excludeComponents);
    }

    /**
     * Returns a CNE formatted code string given an OMElement formatted with a "code" and optional
     * "codeSystem" attribute.
     *
     * @param codedValueNode  A node with "code" and optional "codeSystem" attribute.
     * @return CNE formatted string with ID portions only.
     */
    public static String getCNE_Code(OMElement codedValueNode) {
        String code = codedValueNode.getAttributeValue(new QName("code"));
        String codeSystem = codedValueNode.getAttributeValue(new QName("codeSystem"));
        if (code == null) {
            return "UNKNOWN_CODE";
        }
        if (codeSystem == null || codeSystem.isEmpty()) {
            return code;
        }
        return code + "^^" + codeSystem;
    }

    /**
     * Get CNE formatted code from given value where:
     *   Component 1 (code): REQUIRED
     *   Component 3 (codingSystem): OPTIONAL
     *   Other components: NOT ALLOWED
     *
     * Only Example 1 & 2 are acceptable here.
     *   Example 1: TREATMENT
     *   Example 2: TREATMENT^^2.16.840.1.113883.3.18.7.1
     *
     * @param value CNE formatted string.
     * @return CNE formatted string with ID portions only.
     */
    public static String getCNE_Code(String value) {
        int CODE_COMPONENT = 1;
        int CODING_SCHEME_COMPONENT = 3;

        // Split into component parts.
        String[] components = value.split(COMPONENT_SEPARATOR);
        if (components.length == 0) {
            return "UNKNOWN_CODE";
        }
        // Component 1 = code
        String code = components[CODE_COMPONENT - 1].trim();
        if (code.length() == 0) {
            return "UNKNOWN_CODE";
        }
        // Component 3 = coding system
        String codingSystem = null;
        if (components.length >= CODING_SCHEME_COMPONENT) {
            codingSystem = components[CODING_SCHEME_COMPONENT - 1].trim();
        }
        if (codingSystem == null || codingSystem.length() == 0) {
            // Just return code if no coding system is specified.
            return code;
        } else {
            // Return code with coding system.
            return code + "^^" + codingSystem;
        }
    }

    /**
     * Validates that all components are empty except those in the "excludedComponents" list.
     *
     * @param components List of component parts.
     * @param excludeComponents List of component part indexes that should not be evaluated.
     * @return boolean
     */
    private static boolean validateComponentsAreEmpty(String[] components, int[] excludeComponents) {

        // Go through all components
        for (int i = 1; i <= components.length; i++) {
            boolean excludeComponent = false;
            for (int j = 0; j < excludeComponents.length; j++) {
                if (i == excludeComponents[j]) {
                    excludeComponent = true;
                    break;
                }
            }
            if (!excludeComponent) {
                String componentText = components[i - 1].trim();
                if (componentText.length() != 0) {
                    return false;  // Another component has some text.
                }
            }
        }
        return true;  // Good here.
    }
}
