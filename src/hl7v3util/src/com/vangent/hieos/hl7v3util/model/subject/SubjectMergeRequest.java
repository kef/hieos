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
package com.vangent.hieos.hl7v3util.model.subject;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectMergeRequest implements Cloneable {
    // Even though, we really only care about subject identifiers, there may be other
    // information we need (in the future).

    private Subject survivingSubject;
    private Subject subsumedSubject;

    /**
     *
     * @return
     */
    public Subject getSubsumedSubject() {
        return subsumedSubject;
    }

    /**
     *
     * @param subsumedSubject
     */
    public void setSubsumedSubject(Subject subsumedSubject) {
        this.subsumedSubject = subsumedSubject;
    }

    /**
     *
     * @return
     */
    public Subject getSurvivingSubject() {
        return survivingSubject;
    }

    /**
     *
     * @param survivingSubject
     */
    public void setSurvivingSubject(Subject survivingSubject) {
        this.survivingSubject = survivingSubject;
    }

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SubjectMergeRequest copy = (SubjectMergeRequest) super.clone();
        if (survivingSubject != null) {
            copy.survivingSubject = (Subject) survivingSubject.clone();
        }
        if (subsumedSubject != null) {
            copy.subsumedSubject = (Subject) subsumedSubject.clone();
        }
        return copy;
    }
}
