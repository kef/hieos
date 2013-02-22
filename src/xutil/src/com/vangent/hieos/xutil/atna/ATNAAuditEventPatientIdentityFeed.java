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
 * Covers ITI-8 and ITI-44.
 *
 * @author Bernie Thuman
 */
public class ATNAAuditEventPatientIdentityFeed extends ATNAAuditEvent {
     public enum EventActionCode {

        CREATE, UPDATE, DELETE
    };
    private EventActionCode eventActionCode;

    private String messageId;
    private String sourceIdentity = null;
    private String sourceIP = null;
    private String patientId;

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
    public String getSourceIP() {
        return sourceIP;
    }

    /**
     *
     * @param sourceIP
     */
    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    /**
     *
     * @return
     */
    public String getSourceIdentity() {
        return sourceIdentity;
    }

    /**
     *
     * @param sourceIdentity
     */
    public void setSourceIdentity(String sourceIdentity) {
        this.sourceIdentity = sourceIdentity;
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
