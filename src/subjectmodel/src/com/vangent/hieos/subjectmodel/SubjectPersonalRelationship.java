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
package com.vangent.hieos.subjectmodel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectPersonalRelationship extends SubjectAbstractEntity implements Cloneable {

    private CodedValue relationshipType = null;
    private Subject subject = null;

    /**
     *
     * @return
     */
    public CodedValue getRelationshipType() {
        return relationshipType;
    }

    /**
     *
     * @param relationshipType
     */
    public void setRelationshipType(CodedValue relationshipType) {
        this.relationshipType = relationshipType;
    }

    /**
     * 
     * @return
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     *
     * @param subject
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SubjectPersonalRelationship copy = (SubjectPersonalRelationship) super.clone();
        if (subject != null) {
            copy.subject = (Subject) subject.clone();
        }
        return copy;
    }

    /**
     *
     * @param listToClone
     * @return
     * @throws CloneNotSupportedException
     */
    public static List<SubjectPersonalRelationship> clone(List<SubjectPersonalRelationship> listToClone) throws CloneNotSupportedException {
        List<SubjectPersonalRelationship> copy = null;
        if (listToClone != null) {
            copy = new ArrayList<SubjectPersonalRelationship>();
            for (SubjectPersonalRelationship elementToClone : listToClone) {
                SubjectPersonalRelationship clonedElement = (SubjectPersonalRelationship) elementToClone.clone();
                copy.add(clonedElement);
            }
        }
        return copy;
    }
}
