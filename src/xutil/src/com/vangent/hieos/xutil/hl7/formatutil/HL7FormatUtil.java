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

/**
 *
 * @author Bernie Thuman
 */
public class HL7FormatUtil {

    /**
     *
     * @param value
     * @return
     */
    public static boolean isCXFormatted(String value) {
        // 5cfe5f4f31604fa^^^&1.3.6.1.4.1.21367.2005.3.7&ISO
        String parts[] = value.split("\\^");
        if (parts.length == 4) {

            // Assigning authority.
            String aa[] = parts[3].split("\\&");
            if (aa.length == 3) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param value
     * @return
     */
    public static String getXON_Identifier(String value) {
        // Only Example 1 & 2 are acceptable here.
        // Example 1: Some Hospital
        // Example 2: Some Hospital^^^^^^^^^1.2.3.4.5.6.7.8.9.1789.45
        // Example 3: Some Hospital^^^^^&1.2.3.4.5.6.7.8.9.1789&ISO^^^^45
        String[] parts = value.split("\\^");
        if (parts.length < 10) {
            return "^^^^^^^^^UNKNOWN_ID";
        }
        // Component 10 = id
        String id = parts[9].trim();
        if (id.length() == 0) {
            return "^^^^^^^^^UNKNOWN_ID";
        }
        // Component 6 = assigning authority
        String assigningAuthority = parts[5].trim();
        if (assigningAuthority.length() == 0) {
            // Just return id.
            return "^^^^^^^^^" + id;
        } else {
            // Return id with assigning authority.
            return "^^^^^" + assigningAuthority + "^^^^" + id;
        }
    }

    /**
     *
     * @param value
     * @return
     */
    public static boolean isXON_Identifier(String value) {
        // Only Example 1 & 2 are acceptable here.
        // Example 1: Some Hospital
        // Example 2: Some Hospital^^^^^^^^^1.2.3.4.5.6.7.8.9.1789.45
        // Example 3: Some Hospital^^^^^&1.2.3.4.5.6.7.8.9.1789&ISO^^^^45
        String[] parts = value.split("\\^");
        if (parts.length < 10) {
            return false;
        }
        // Must have an id (component 10).
        String id = parts[9].trim();
        if (id.length() == 0) {
            // No id.
            return false;
        }
        // Only allow data in component 6 (optional) and 10.
        for (int i = 1; i <= parts.length; i++) {
            if (!(i == 6 || i == 10)) {
                String componentText = parts[i - 1].trim();
                if (componentText.length() != 0) {
                    return false;  // Another component has some text.
                }
            }
        }
        return true;
    }

    /**
     *
     * @param value
     * @return
     */
    public static String getXCN_Identifier(String value) {
        String[] parts = value.split("\\^");
        if (parts.length == 0) {
            return "UNKNOWN_ID";
        }
        // Component 1 = id
        String id = parts[0].trim();
        if (id.length() == 0) {
            return "UNKNOWN_ID";
        }
        // Component 9 = assigning authority
        String assigningAuthority = null;
        if (parts.length > 8) {
            assigningAuthority = parts[8].trim();
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
     *
     * @param value
     * @return
     */
    public static boolean isXCN_Identifier(String value) {
        String[] parts = value.split("\\^");
        if (parts.length == 0) {
            return false;  // Nothing specified.
        }
        // Component 1 = id
        String id = parts[0].trim();
        if (id.length() == 0) {
            return false;  // No id.
        }

        // Component 9 = assigning authority (optional)
        // Only allow data in component 1 and 9 (optional).
        for (int i = 1; i <= parts.length; i++) {
            if (!(i == 1 || i == 9)) {
                String componentText = parts[i - 1].trim();
                if (componentText.length() != 0) {
                    return false;  // Another component has some text.
                }
            }
        }
        return true;  // Good here.
    }
}
