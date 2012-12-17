/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.model;

import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.SubjectAbstractEntity;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectReviewItem extends SubjectAbstractEntity implements Cloneable {

    /**
     *
     */
    public enum ReviewType {

        /**
         * 
         */
        POTENTIAL_DUPLICATE,
        /**
         *
         */
        POTENTIAL_MATCH
    };
    private InternalId otherSubjectId;
    private ReviewType reviewType = ReviewType.POTENTIAL_DUPLICATE;  // Default.

    /**
     * 
     * @return
     */
    public InternalId getOtherSubjectId() {
        return otherSubjectId;
    }

    /**
     *
     * @param otherSubjectId
     */
    public void setOtherSubjectId(InternalId otherSubjectId) {
        this.otherSubjectId = otherSubjectId;
    }

    /**
     *
     * @return
     */
    public ReviewType getReviewType() {
        return reviewType;
    }

    /**
     *
     * @param reviewType
     */
    public void setReviewType(ReviewType reviewType) {
        this.reviewType = reviewType;
    }

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
