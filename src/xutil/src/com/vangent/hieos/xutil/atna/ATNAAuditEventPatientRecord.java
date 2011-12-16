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
 *
 * @author Bernie Thuman
 */
public class ATNAAuditEventPatientRecord extends ATNAAuditEvent {

    private String messageId;
    private List<String> patientIds = new ArrayList<String>();  // List of Patient IDs in CX format.

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
    public void addPatientId(String patientId) {
        patientIds.add(patientId);
    }

    /**
     *
     * @return
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     *
     * @param messageId
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
