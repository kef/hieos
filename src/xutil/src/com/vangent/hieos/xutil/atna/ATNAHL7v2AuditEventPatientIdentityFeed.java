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

/**
 * Covers ITI-8.
 *
 * @author Bernie Thuman
 */
public class ATNAHL7v2AuditEventPatientIdentityFeed extends ATNAHL7v2AuditEvent {

    public enum EventActionCode {

        CREATE, UPDATE, DELETE
    };
    private EventActionCode eventActionCode;
    private String patientId;

    /**
     *
     * @return
     */
    public EventActionCode getEventActionCode() {
        return eventActionCode;
    }

    /**
     *
     * @param eventActionCode
     */
    public void setEventActionCode(EventActionCode eventActionCode) {
        this.eventActionCode = eventActionCode;
    }

    /**
     * 
     * @return
     */
    public String getEventActionCodeAsString() {
        switch (eventActionCode) {
            case CREATE:
                return "C";
            case UPDATE:
                return "U";
            case DELETE:
            default:
                return "D";
        }
    }

    /**
     *
     * @return
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     *
     * @param patientId
     */
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
