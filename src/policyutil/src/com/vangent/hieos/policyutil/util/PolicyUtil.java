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
package com.vangent.hieos.policyutil.util;

/**
 *
 * @author Bernie Thuman
 */
public class PolicyUtil {

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
        String id = parts[0].trim();
        if (id.length() == 0) {
            return "UNKNOWN_ID";
        }
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
    public static String getXON_Identifier(String value) {
        // Only Example 1 & 2 are acceptable here.
        // Example 1: Some Hospital
        // Example 2: Some Hospital^^^^^^^^^1.2.3.4.5.6.7.8.9.1789.45
        // Example 3: Some Hospital^^^^^&1.2.3.4.5.6.7.8.9.1789&ISO^^^^45
        String[] parts = value.split("\\^");
        if (parts.length < 10) {
            return "^^^^^^^^^UNKNOWN_ID";
        }
        String id = parts[9].trim();
        String assigningAuthority = parts[5].trim();
        if (id.length() == 0) {
            return "^^^^^^^^^UNKNOWN_ID";
        }
        if (assigningAuthority.length() == 0) {
            // Just return id.
            return "^^^^^^^^^" + id;
        } else {
            // Return id with assigning authority.
            return "^^^^^" + assigningAuthority + "^^^^" + id;
        }
    }
}
