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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class MatchResults {

    private List<ScoredRecord> matches = new ArrayList<ScoredRecord>();
    private List<ScoredRecord> nonMatches = new ArrayList<ScoredRecord>();
    private List<ScoredRecord> possibleMatches = new ArrayList<ScoredRecord>();

    /**
     *
     * @param scoredRecord
     */
    public void addMatch(ScoredRecord scoredRecord) {
        this.matches.add(scoredRecord);
    }

    /**
     *
     * @param scoredRecord
     */
    public void addNonMatch(ScoredRecord scoredRecord) {
        this.nonMatches.add(scoredRecord);
    }

    /**
     *
     * @param scoredRecord
     */
    public void addPossibleMatch(ScoredRecord scoredRecord) {
        this.possibleMatches.add(scoredRecord);
    }

    /**
     *
     * @return
     */
    public List<ScoredRecord> getMatches() {
        return matches;
    }

    /**
     *
     * @return
     */
    public List<ScoredRecord> getNonMatches() {
        return nonMatches;
    }

    /**
     *
     * @return
     */
    public List<ScoredRecord> getPossibleMatches() {
        return possibleMatches;
    }
}
