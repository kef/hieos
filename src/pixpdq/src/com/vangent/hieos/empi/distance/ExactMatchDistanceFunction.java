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

    /**
     *
     * @param s1
     * @param s2
     * @return
     */
    public double getDistance(String s1, String s2) {
        if (s1.equalsIgnoreCase(s2)) {
            return 1.0;
        }
        return 0.0;
    }
}
