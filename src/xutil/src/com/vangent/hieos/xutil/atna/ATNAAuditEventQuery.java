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
package com.vangent.hieos.xutil.atna;

import java.util.ArrayList;
import java.util.List;

/**
 * Covers ITI-55, ITI-18 and ITI-38.
 *
 * @author Bernie Thuman
 */
public class ATNAAuditEventQuery extends ATNAAuditEvent {

    // Nothing special to add.
    private String queryId = null;
    private String queryText = null;
    private List<String> patientIds = new ArrayList<String>();  // List of Patient IDs in CX format.

    /**
     *
     * @return
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     *
     * @param queryId
     */
    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    /**
     *
     * @return
     */
    public String getQueryText() {
        return queryText;
    }

    /**
     *
     * @param queryText
     */
    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    /**
     *
     * @return
     */
    public List<String> getPatientIds() {
        return patientIds;
    }

    /**
     * 
     * @param patientId
     */
    public void addPatientId(String patientId)
    {
        patientIds.add(patientId);
    }
}
