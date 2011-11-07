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

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Bernie Thuman
 */
public class LevenshteinDistanceFunction extends DistanceFunction {

    /**
     * 
     * @param s1
     * @param s2
     * @return
     */
    public double getDistance(String s1, String s2) {
        if (s1 == null && s2 == null) {
            // If both are null, return 1.0 (exact match).
            return 1.0;
        }
        if (s1 == null || s2 == null) {
            // If one of the strings is null, return 0.0 (no match).
            return 0.0;
        }
        int lens1 = s1.length();
        int lens2 = s2.length();
        if (lens1 == 0 && lens2 == 0) {
            // Both are the empty string, return 1.0 (exact match).
            return 1.0;
        }

        // Compute Levenshtein Distance.
        int distance = StringUtils.getLevenshteinDistance(s1, s2);

        // Now normalize between 0.0 and 1.0
        int maxlen = lens1 >= lens2 ? lens1 : lens2;

        // maxlen = 0 should never happen.
        return 1.0 - ((double)distance / (double)maxlen);
    }
}
