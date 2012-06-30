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
package com.vangent.hieos.empi.model;

// FIXME: Move class to a more reasonable package??

import com.vangent.hieos.hl7v3util.model.subject.InternalId;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectCrossReference {

    private InternalId enterpriseSubjectId;
    private InternalId systemSubjectId;
    private double matchScore;

    /**
     *
     * @return
     */
    public InternalId getEnterpriseSubjectId() {
        return enterpriseSubjectId;
    }

    /**
     *
     * @param enterpriseSubjectId
     */
    public void setEnterpriseSubjectId(InternalId enterpriseSubjectId) {
        this.enterpriseSubjectId = enterpriseSubjectId;
    }

    /**
     *
     * @return
     */
    public double getMatchScore() {
        return matchScore;
    }

    /**
     *
     * @param matchScore
     */
    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    /**
     *
     * @return
     */
    public InternalId getSystemSubjectId() {
        return systemSubjectId;
    }

    /**
     *
     * @param systemSubjectId
     */
    public void setSystemSubjectId(InternalId systemSubjectId) {
        this.systemSubjectId = systemSubjectId;
    }
}
