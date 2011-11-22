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
     * @return
     * @throws EMPIException
     */
    @Override
    public List<Record> findCandidates(Record searchRecord) throws EMPIException {
        PersistenceManager ps = this.getPersistenceService();
        return ps.lookup(searchRecord);
    }

    /**
     * 
     * @param searchRecord
     * @return
     * @throws EMPIException
     */
    @Override
    public MatchResults findMatches(Record searchRecord) throws EMPIException {
        // First, get list of candidate records.
        List<Record> candidateRecords = this.findCandidates(searchRecord);
        // Now, run the findMatches algorithm.
        return this.findMatches(searchRecord, candidateRecords);
    }

    /**
     * 
     * @param searchRecord
     * @param records
     * @return
     * @throws EMPIException
     */
    @Override
    public MatchResults findMatches(Record searchRecord, List<Record> records) throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        MatchConfig matchConfig = empiConfig.getMatchConfig();
        double recordAcceptThreshold = matchConfig.getAcceptThreshold();
        double recordRejectThreshold = matchConfig.getRejectThreshold();
        MatchResults matchResults = new MatchResults();
        for (Record record : records) {
            ScoredRecord scoredRecord = this.score(searchRecord, record, matchConfig);
            double recordScore = scoredRecord.getScore();
            // FIXME: Shouldn't we return a sorted list as the result?
            if (recordScore >= recordAcceptThreshold) {
                // Match.
                matchResults.addMatch(scoredRecord);
            } else if (recordScore < recordRejectThreshold) {
                // No findMatches.
                matchResults.addNonMatch(scoredRecord);
            } else {
                // Possible findMatches.
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
     * @return
     * @throws EMPIException
     */
    private ScoredRecord score(Record searchRecord, Record record, MatchConfig matchConfig) throws EMPIException {
        ScoredRecord scoredRecord = new ScoredRecord(matchConfig);
        scoredRecord.setRecord(record);

        // Go through list of fields to compare (based on configuration).
        List<MatchFieldConfig> matchFieldConfigs = matchConfig.getMatchFieldConfigs();
        int fieldIndex = 0;
        for (MatchFieldConfig matchFieldConfig : matchFieldConfigs) {
            String matchFieldName = matchFieldConfig.getName();

            // Get the current field's "distance function" configuration.
            DistanceFunctionConfig distanceFunctionConfig = matchFieldConfig.getDistanceFunctionConfig();

            // Get the "distance function".
            DistanceFunction distanceFunction = distanceFunctionConfig.getDistanceFunction();

            // Compute the field distance (a.k.a similarity).
            Field searchRecordField = searchRecord.getField(matchFieldName);
            String searchRecordFieldValue = searchRecordField != null ? searchRecordField.getValue() : null;
            double fieldDistance = 1.0;  // Empty search field is considered a match.
            if (searchRecordFieldValue != null) {
                String candidateRecordFieldValue = record.getField(matchFieldName).getValue();
                fieldDistance = distanceFunction.getDistance(searchRecordFieldValue, candidateRecordFieldValue);
            }
            scoredRecord.setDistance(fieldIndex, fieldDistance);
            ++fieldIndex;
            // TBD: Most of the algorithm appears generic ... would be good to abstract at higher level.
            // TBD: Also, assumes any blocking rounds have occurred.
        }
        // Now, compute field-level and record scores.
        scoredRecord.computeScores();
        System.out.println("ScoredRecord: " + scoredRecord.toString());
        System.out.println("... recordScore = " + scoredRecord.getScore());
        return scoredRecord;
    }
}
