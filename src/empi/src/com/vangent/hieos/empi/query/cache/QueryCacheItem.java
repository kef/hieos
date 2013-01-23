/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.query.cache;

import com.vangent.hieos.subjectmodel.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class QueryCacheItem {

    private String key;
    int incrementQuantity;
    private List<Subject> subjects;

    /**
     * 
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     *
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 
     * @return
     */
    public int getIncrementQuantity() {
        return incrementQuantity;
    }

    /**
     *
     * @param incrementQuantity
     */
    public void setIncrementQuantity(int incrementQuantity) {
        this.incrementQuantity = incrementQuantity;
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
     * @param subjects
     */
    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    /**
     * 
     * @return
     */
    public boolean hasMoreSubjectsToReturn() {
        return subjects.size() > 0;
    }

    /**
     *
     * @return
     */
    public int getSubjectsRemainingQuantity() {
        return subjects.size();
    }

    /**
     *
     * @return
     */
    public List<Subject> getNextIncrement() {
        List<Subject> nextIncrementOfSubjects = new ArrayList<Subject>();
        for (int i = 0; i < incrementQuantity && subjects.size() > 0; i++) {
            nextIncrementOfSubjects.add(subjects.get(0));
            subjects.remove(0);
        }
        return nextIncrementOfSubjects;
    }
}
