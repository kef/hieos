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
public class ExactMatchDistanceFunction extends DistanceFunction {

    private static String PARAM_TRUNCATE_LENGTH = "truncate-length";

    /**
     *
     * @param s1
     * @param s2
     * @return
     */
    public double getDistance(String s1, String s2) {
        // See if the strings should be truncated before the compare.
        int compareLength = this.getFunctionConfig().getParameterAsInteger(PARAM_TRUNCATE_LENGTH, -1);
        if (compareLength != -1) {
            if (s1.length() > compareLength) {
                s1 = s1.substring(0, compareLength);
            }
            if (s2.length() > compareLength) {
                s2 = s2.substring(0, compareLength);
            }
        }
        return s1.equalsIgnoreCase(s2) ? 1.0 : 0;
    }
}
