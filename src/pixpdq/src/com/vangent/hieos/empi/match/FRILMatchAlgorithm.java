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

import com.vangent.hieos.empi.distance.DistanceFunction;
import com.vangent.hieos.empi.config.DistanceFunctionConfig;
import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.config.MatchFieldConfig;
import com.vangent.hieos.empi.config.MatchConfig;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.exception.EMPIException;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class FRILMatchAlgorithm extends MatchAlgorithm {

    /**
     *
     */
    public FRILMatchAlgorithm() {
    }

    /**
     *
     * @param searchRecord
     * @param matchType
     * @return
     * @throws EMPIException
     */
    @Override
    public List<Record> findCandidates(Record searchRecord, MatchType matchType) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        return pm.findCandidates(searchRecord, matchType);
    }

    /**
     * 
     * @param searchRecord
     * @param matchType
     * @return
     * @throws EMPIException
     */
    @Override
    public MatchResults findMatches(Record searchRecord, MatchType matchType) throws EMPIException {
        System.out.println("Search Record: " + searchRecord);

        // First, get list of candidate records.
        List<Record> candidateRecords = this.findCandidates(searchRecord, matchType);

        // DEBUG:
        System.out.println("... number of candidate records = " + candidateRecords.size());
        // Now, run the findMatches algorithm.
        return this.findMatches(searchRecord, candidateRecords, matchType);
    }

    /**
     * 
     * @param searchRecord
     * @param records
     * @param matchType
     * @return
     * @throws EMPIException
     */
    @Override
    public MatchResults findMatches(Record searchRecord, List<Record> records, MatchType matchType) throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        MatchConfig matchConfig = empiConfig.getMatchConfig();
        double recordAcceptThreshold = matchConfig.getAcceptThreshold();
        double recordRejectThreshold = matchConfig.getRejectThreshold();
        MatchResults matchResults = new MatchResults();
        System.out.println("... Search Record: " + searchRecord.toString());
        for (Record record : records) {
            ScoredRecord scoredRecord = this.score(searchRecord, record, matchConfig, matchType);
            double recordScore = scoredRecord.getScore();
            // FIXME: Shouldn't we return a sorted list as the result?
            if (recordScore >= recordAcceptThreshold) {
                // Match.
                System.out.println("... Matched Record: " + scoredRecord.toString());
                matchResults.addMatch(scoredRecord);
            } else if (recordScore < recordRejectThreshold) {
                // Non-match.
                System.out.println("... Non-Matched Record: " + scoredRecord.toString());
                matchResults.addNonMatch(scoredRecord);
            } else {
                // Possible match.
                System.out.println("... Possible Matched Record: " + scoredRecord.toString());
                matchResults.addPossibleMatch(scoredRecord);
            }
        }
        this.sortMatchResults(matchResults);
        return matchResults;
    }

    /**
     * 
     * @param matchResults
     */
    private void sortMatchResults(MatchResults matchResults) {
        // Only sort matches (in descending order by score).
        Collections.sort(matchResults.getMatches(), new ScoredRecordComparator());
    }

    /**
     *
     * @param searchRecord
     * @param record
     * @param matchConfig
     * @param matchType
     * @return
     * @throws EMPIException
     */
    private ScoredRecord score(Record searchRecord, Record record, MatchConfig matchConfig, MatchType matchType) throws EMPIException {
        ScoredRecord scoredRecord = new ScoredRecord(matchConfig);
        scoredRecord.setRecord(record);

        // Go through list of fields to compare (based on configuration).
        List<MatchFieldConfig> matchFieldConfigs = matchConfig.getMatchFieldConfigs();
        int fieldIndex = 0;
        for (MatchFieldConfig matchFieldConfig : matchFieldConfigs) {
            if (matchType == MatchType.SUBJECT_ADD && !matchFieldConfig.isEnabledDuringSubjectAdd()) {
                scoredRecord.setDistance(fieldIndex, -1.0);  // -1.0 really means nothing (just for debug).
                ++fieldIndex;
                // FIXME: This is a temporary FIX (in a rush).
                continue;
            }
            String matchFieldName = matchFieldConfig.getName();

            // Get the current field's "distance function" configuration.
            DistanceFunctionConfig distanceFunctionConfig = matchFieldConfig.getDistanceFunctionConfig();

            // Get the "distance function".
            DistanceFunction distanceFunction = distanceFunctionConfig.getDistanceFunction();

            // Compute the field distance (a.k.a similarity).
            Field searchRecordField = searchRecord.getField(matchFieldName);
            String searchRecordFieldValue = searchRecordField != null ? searchRecordField.getValue() : null;
            double fieldDistance;
            if ((searchRecordFieldValue != null) && !searchRecordFieldValue.isEmpty()) {
                String candidateRecordFieldValue = record.getField(matchFieldName).getValue();
                fieldDistance = distanceFunction.getDistance(searchRecordFieldValue, candidateRecordFieldValue);
            } else {
                fieldDistance = this.getEmptyFieldDistance(matchType);
            }
            scoredRecord.setDistance(fieldIndex, fieldDistance);
            ++fieldIndex;
            // TBD: Most of the algorithm appears generic ... would be good to abstract at higher level.
            // TBD: Also, assumes any blocking rounds have occurred.
        }
        // Now, compute field-level and record scores.
        scoredRecord.computeScores(matchType);
        //System.out.println("ScoredRecord: " + scoredRecord.toString());
        //System.out.println("... recordScore = " + scoredRecord.getScore());
        return scoredRecord;
    }

    /**
     *
     * @param matchType
     * @return
     */
    private double getEmptyFieldDistance(MatchType matchType) {
        if (matchType.equals(MatchType.SUBJECT_FIND)) {
            return 1.0;
        } else {
            // MatchType.NOMATCH_EMTPY_FIELDS
            return 0.0;
        }
    }
}
