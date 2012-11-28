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

import com.vangent.hieos.empi.config.MatchFieldConfig;
import com.vangent.hieos.empi.config.MatchConfig;
import com.vangent.hieos.empi.match.MatchAlgorithm.MatchType;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author Bernie Thuman
 */
public class ScoredRecord {

    private double score;
    private double goodnessOfFitScore;
    private Record record;
    private MatchConfig matchConfig;
    private double[] scores;
    private double[] distances;

    /**
     *
     * @param matchConfig
     */
    public ScoredRecord(MatchConfig matchConfig) {
        this.matchConfig = matchConfig;
        List<MatchFieldConfig> matchFieldConfigs = matchConfig.getMatchFieldConfigs();
        int numFields = matchFieldConfigs.size();
        this.scores = new double[numFields];
        this.distances = new double[numFields];
    }

    /**
     * 
     * @param fieldIndex
     * @param distance
     */
    public void setDistance(int fieldIndex, double distance) {
        this.distances[fieldIndex] = distance;
    }

    /**
     *
     * @param matchType
     */
    public void computeScores(MatchType matchType) {
        List<MatchFieldConfig> matchFieldConfigs = matchConfig.getMatchFieldConfigs();

        // Compute field-level scores.
        int fieldIndex = 0;
        double fieldWeightSum = 0.0;
        double fieldScoreSum = 0.0;
        for (MatchFieldConfig matchFieldConfig : matchFieldConfigs) {
            //if (matchType == MatchType.SUBJECT_FEED && !matchFieldConfig.isEnabledDuringSubjectAdd()) {
            // Divorce this field from any calculations.
            //    scores[fieldIndex] = -1.0;  // Not used (just an indicator for debugging).
            //} else {
            double fieldScore = this.computeFieldScore(fieldIndex, matchFieldConfig);
            scores[fieldIndex] = fieldScore;
            fieldScore *= matchFieldConfig.getWeight();
            fieldWeightSum += matchFieldConfig.getWeight();
            fieldScoreSum += fieldScore;
            //}
            ++fieldIndex;
        }

        // Compute record score.
        this.score = fieldScoreSum / fieldWeightSum;

        // Compute "goodness of fit" score (for the record).
        this.goodnessOfFitScore = this.computeGoodnessOfFitScore();
    }

    /**
     * 
     * @return
     */
    public double getScore() {
        return score;
    }

    /**
     * 
     * @return
     */
    public int getMatchScorePercentage() {
        // Round-up match score.
        BigDecimal bd = new BigDecimal(this.getScore() * 100.0);
        bd = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
        return bd.intValue();
    }

    /**
     *
     * @param score
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     *
     * @return
     */
    public Record getRecord() {
        return record;
    }

    /**
     *
     * @param record
     */
    public void setRecord(Record record) {
        this.record = record;
    }

    /**
     *
     * @return
     */
    public double[] getDistances() {
        return distances;
    }

    /**
     *
     * @return
     */
    public double getGoodnessOfFitScore() {
        return goodnessOfFitScore;
    }

    /**
     *
     * @return
     */
    public double[] getScores() {
        return scores;
    }

    /**
     *
     * @return
     */
    private double computeGoodnessOfFitScore() {
        double recordAcceptThreshold = matchConfig.getAcceptThreshold();
        double recordRejectThreshold = matchConfig.getRejectThreshold();
        double gofScore;
        if (this.score >= recordAcceptThreshold) {
            // Match.
            gofScore = 1.0;
        } else if (this.score < recordRejectThreshold) {
            // No match.
            gofScore = 0.0;
        } else {
            // Possible match.
            gofScore = (this.score - recordRejectThreshold) / (recordAcceptThreshold - recordRejectThreshold);
        }
        return gofScore;
    }

    /**
     *
     * @param fieldIndex
     * @param fieldConfig
     * @return
     */
    private double computeFieldScore(int fieldIndex, MatchFieldConfig fieldConfig) {
        double fieldAcceptThreshold = fieldConfig.getAcceptThreshold();
        double fieldRejectThreshold = fieldConfig.getRejectThreshold();
        double fieldScore;
        double fieldDistance = distances[fieldIndex];
        if (fieldDistance >= fieldAcceptThreshold) {
            // FIXME: THIS HAS BEEN CHANGED BACK TO THE ORIGINAL FRIL (inverted) implementation.
            // NOTE (BHT): The FRIL algorithm says to set the field score = 1.0 if the field
            // distances is >= the accept threshold for the field.  This FRIL implementation 
            // sets the field score equal to the field distance.
            fieldScore = 1.0;
            //fieldScore = fieldDistance;
        } else if (fieldDistance < fieldRejectThreshold) {
            // FIXME: ?
            // NOTE (BHT): Should we consider setting the field score to the field distance as above?
            fieldScore = 0.0;
        } else {
            fieldScore = (fieldDistance - fieldRejectThreshold) / (fieldAcceptThreshold - fieldRejectThreshold);
        }
        return fieldScore;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("record", record).append("scores", scores).append("distances", distances).append("score", score).append("gofScore", goodnessOfFitScore).toString();
    }
}
