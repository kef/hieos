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
package com.vangent.hieos.empi.match;

import java.util.Comparator;

/**
 *
 * @author Bernie Thuman
 */
public class ScoredRecordComparator implements Comparator<ScoredRecord> {

    /**
     * 
     * @param o1
     * @param o2
     * @return
     */
    public int compare(ScoredRecord o1, ScoredRecord o2) {
        // Will sort in descending order
        double o1score = o1.getScore();
        double o2score = o2.getScore();
        return Double.valueOf(o2score).compareTo(o1score);
    }
}
