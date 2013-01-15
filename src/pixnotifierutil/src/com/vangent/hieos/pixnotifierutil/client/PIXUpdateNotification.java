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
package com.vangent.hieos.pixnotifierutil.client;

import com.vangent.hieos.subjectmodel.Subject;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class PIXUpdateNotification implements Serializable {

    private String text;
    private List<Subject> subjects;

    /**
     * 
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
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
}
