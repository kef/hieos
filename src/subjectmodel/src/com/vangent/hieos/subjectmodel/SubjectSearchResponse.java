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
package com.vangent.hieos.subjectmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectSearchResponse implements Cloneable, Serializable {

    private List<Subject> subjects = new ArrayList<Subject>();
    private String continuationPointerId = null;

    /**
     * 
     * @return
     */
    public boolean hasMoreSubjectsToReturn()
    {
        return continuationPointerId != null;
    }

    /**
     *
     * @return
     */
    public String getContinuationPointerId() {
        return continuationPointerId;
    }

    /**
     *
     * @param continuationPointerId
     */
    public void setContinuationPointerId(String continuationPointerId) {
        this.continuationPointerId = continuationPointerId;
    }

    /**
     *
     * @return
     */
    public List<Subject> getSubjects() {
        return this.subjects;
    }

    /**
     *
     * @param subjects
     */
    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SubjectSearchResponse copy = (SubjectSearchResponse) super.clone();
        copy.subjects = Subject.clone(subjects);
        return copy;
    }
}
