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
     */
    public void computeScores() {
        List<MatchFieldConfig> matchFieldConfigs = matchConfig.getMatchFieldConfigs();

        // Compute field-level scores.
        int fieldIndex = 0;
        double fieldWeightSum = 0.0;
        double fieldScoreSum = 0.0;
        for (MatchFieldConfig matchFieldConfig : matchFieldConfigs) {
            double fieldScore = this.computeFieldScore(fieldIndex, matchFieldConfig);
            scores[fieldIndex] = fieldScore;
            fieldScore *= matchFieldConfig.getWeight();
            fieldWeightSum += matchFieldConfig.getWeight();
            fieldScoreSum += fieldScore;
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
            fieldScore = 1.0;
        } else if (fieldDistance < fieldRejectThreshold) {
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
        return new ToStringBuilder(this)
                .append("record", record)
                .append("scores", scores)
                .append("distances", distances)
                .append("score", score)
                .append("gofScore", goodnessOfFitScore)
                .toString();
    }
}
