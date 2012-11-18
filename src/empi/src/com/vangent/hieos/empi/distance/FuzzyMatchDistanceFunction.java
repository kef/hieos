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
package com.vangent.hieos.empi.distance;

/**
 *
 * @author Bernie Thuman
 */
public class FuzzyMatchDistanceFunction extends DistanceFunction {

    /**
     *
     * @param s1
     * @param s2
     * @return
     */
    public double getDistance(String s1, String s2) {
        // s1 = search field value
        // s2 = field to compare against
        // Find '%' in search field (s1)
        // Determine mode.
        boolean isFuzzySearchMode = s1.endsWith("%");
        if (isFuzzySearchMode) {
            // Get search field value (without %).
            String searchFieldValue = s1.substring(0, s1.length() - 1);

            // Truncate comparison field (to length of search field).
            if (s2.length() < searchFieldValue.length()) {
                return 0.0;
            }
            String compareFieldValue = s2.substring(0, searchFieldValue.length());

            // If they are equal, return 1.0, else return 0.0
            if (searchFieldValue.equalsIgnoreCase(compareFieldValue)) {
                return 1.0;
            } else {
                return 0.0;
            }
        } else {
            // Do an exact match comparison (case insensitive).
            return s1.equalsIgnoreCase(s2) ? 1.0 : 0;
        }
    }
}
