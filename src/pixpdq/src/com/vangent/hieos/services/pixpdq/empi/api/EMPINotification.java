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
package com.vangent.hieos.services.pixpdq.empi.api;

import com.vangent.hieos.hl7v3util.model.subject.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class EMPINotification {

    private List<Subject> subjects = new ArrayList<Subject>();

    /**
     *
     */
    public EMPINotification() {
    }

    /**
     *
     * @return
     */
    public List<Subject> getSubjects() {
        return subjects;
    }

    /**
     *
     * @param subject
     */
    public void addSubject(Subject subject) {
        subjects.add(subject);
    }

    /**
     *
     * @param toMergeNotification
     */
    public void addNotification(EMPINotification toMergeNotification) {
        // See if the notification already has the subject and if so, replace them.
        for (Subject subject : toMergeNotification.getSubjects()) {
            if (this.subjectExists(subject.getInternalId())) {
                this.replaceSubject(subject);
            } else {
                this.addSubject(subject);
            }
        }
    }

    /**
     *
     * @param subjectId
     * @return
     */
    private boolean subjectExists(String subjectId) {
        for (Subject subject : subjects) {
            if (subject.getInternalId().equals(subjectId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param replaceSubject
     */
    private void replaceSubject(Subject replaceSubject) {
        int subjectIndex = 0;
        for (Subject subject : subjects) {
            if (subject.getInternalId().equals(replaceSubject.getInternalId())) {
                subjects.remove(subjectIndex);
                subjects.add(replaceSubject);
                break;
            }
            ++subjectIndex;
        }
    }
}
