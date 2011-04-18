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
package com.vangent.hieos.services.atna.arr.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 
 *  @author Adeola Odunlami
 */
public class ATNAMessage {
    // System generated unique identifier
    private String uniqueID;
    // Indicates if the complete message was saved - errors in audit xml data may have prevented
    // the complete audit message from being saved.
    private String status;

    // Event Identification Details
    private ATNACodedValue eventID;
    private String eventActionCode;
    private Date eventDateTime;
    private Integer eventOutcomeIndicator;
    private List<ATNACodedValue> eventTypeCodes;

    // Audit Source Details
    private List<ATNAAuditSource> auditSources;

    // Active Participants e.g. Document Source & Human Requestor (if known)
    private List<ATNAActiveParticipant> activeParticipants;

    // Participant Objects e.g. Patient & Submission Set
    private List<ATNAParticipantObject> participantObjects;


    /**
     * @return the uniqueID
     */
    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * @param uniqueID the uniqueID to set
     */
    public void setUniqueID(String id) {
        this.uniqueID = id;
    }

    /**
     * @return the eventID
     */
    public ATNACodedValue getEventID() {
        return eventID;
    }

    /**
     * @param eventID the eventID to set
     */
    public void setEventID(ATNACodedValue eventID) {
        this.eventID = eventID;
    }

    /**
     * @return the eventActionCode
     */
    public String getEventActionCode() {
        return eventActionCode;
    }

    /**
     * @param eventActionCode the eventActionCode to set
     */
    public void setEventActionCode(String eventActionCode) {
        this.eventActionCode = eventActionCode;
    }

    /**
     * @return the eventDateTime
     */
    public Date getEventDateTime() {
        return eventDateTime;
    }

    /**
     * @param eventDateTime the eventDateTime to set
     */
    public void setEventDateTime(Date eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    /**
     * @return the eventOutcomeIndicator
     */
    public Integer getEventOutcomeIndicator() {
        return eventOutcomeIndicator;
    }

    /**
     * @param eventOutcomeIndicator the eventOutcomeIndicator to set
     */
    public void setEventOutcomeIndicator(Integer eventOutcomeIndicator) {
        this.eventOutcomeIndicator = eventOutcomeIndicator;
    }

    /**
     * @return the activeParticipants
     */
    public List<ATNAActiveParticipant> getActiveParticipants() {
        return activeParticipants;
    }

    /**
     * @param activeParticipants the activeParticipants to set
     */
    public void setActiveParticipants(List<ATNAActiveParticipant> activeParticipants) {
        this.activeParticipants = activeParticipants;
    }

    /**
     * Method used to retrive the list of Active Participants. It is also used to
     * update the list of objects using getActiveParticipantsList().add(ATNAActiveParticipant)
     *
     * @return the ActiveParticipants
     */
    public List<ATNAActiveParticipant> getActiveParticipantsList() {
        if (getActiveParticipants() == null) {
            setActiveParticipants(new ArrayList<ATNAActiveParticipant>());
        }
        return this.getActiveParticipants();
    }

    /**
     * @return the participantObjects
     */
    public List<ATNAParticipantObject> getParticipantObjects() {
        return participantObjects;
    }

    /**
     * @param participantObjects the participantObjects to set
     */
    public void setParticipantObjects(List<ATNAParticipantObject> participantObjects) {
        this.participantObjects = participantObjects;
    }

    /**
     * Method used to retrive the list of ATNA Participant Objects. It is also used to
     * update the list of objects using getParticipantObjectsList().add(ATNAParticipantObject)
     * @return the participantObjects
     */
    public List<ATNAParticipantObject> getParticipantObjectsList() {
        if (getParticipantObjects() == null) {
            setParticipantObjects(new ArrayList<ATNAParticipantObject>());
        }
        return this.getParticipantObjects();
    }

    /**
     * @return the eventTypeCodes
     */
    public List<ATNACodedValue> getEventTypeCodes() {
        return eventTypeCodes;
    }

    /**
     * @param eventTypeCodes the eventTypeCodes to set
     */
    public void setEventTypeCodes(List<ATNACodedValue> eventTypeCodes) {
        this.eventTypeCodes = eventTypeCodes;
    }

    /**
     * @return the auditSources
     */
    public List<ATNAAuditSource> getAuditSources() {
        return auditSources;
    }

    /**
     * @param auditSources the auditSources to set
     */
    public void setAuditSources(List<ATNAAuditSource> auditSources) {
        this.auditSources = auditSources;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
