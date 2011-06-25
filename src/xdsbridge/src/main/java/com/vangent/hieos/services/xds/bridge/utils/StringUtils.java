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

package com.vangent.hieos.services.xds.bridge.utils;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class StringUtils {

    /**
     * Method description
     *
     *
     * @param str1
     * @param str2
     *
     * @return
     */
    public static boolean equals(String str1, String str2) {

        boolean result = false;

        if ((str1 == null) && (str2 == null)) {
            
            result = true;
            
        } else if ((str1 != null) && (str2 != null)) {

            result = str1.equals(str2);
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param str
     *
     * @return
     */
    public static boolean isBlank(String str) {

        boolean result = false;

        if ((str == null) || (str.trim().length() == 0)) {
            result = true;
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param str
     *
     * @return
     */
    public static boolean isNotBlank(String str) {

        return (isBlank(str) == false);
    }

    /**
     * Method description
     *
     *
     * @param str
     * @param token
     *
     * @return
     */
    public static String substringAfterLast(String str, String token) {

        String result = null;

        if (str != null) {

            result = "";

            if (token != null) {

                int index = str.lastIndexOf(token);

                if ((index > -1) && (index + 1) < str.length()) {

                    result = str.substring(index + 1);

                }
            }
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param str
     * @param token
     *
     * @return
     */
    public static String substringBeforeLast(String str, String token) {

        String result = null;

        if (str != null) {

            result = "";

            if (token != null) {

                int index = str.lastIndexOf(token);

                if (index - 1 > 0) {

                    result = str.substring(0, index);

                }
            }
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param str
     *
     * @return
     */
    public static String trimToEmpty(String str) {

        String result = "";

        if (str != null) {

            result = str.trim();
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param str
     *
     * @return
     */
    public static String trimToNull(String str) {

        String result = null;

        if (str != null) {

            result = str.trim();

            if (result.length() == 0) {
                result = null;
            }
        }

        return result;
    }
}
