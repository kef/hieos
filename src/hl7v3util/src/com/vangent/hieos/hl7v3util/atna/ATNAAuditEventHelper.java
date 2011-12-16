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
package com.vangent.hieos.hl7v3util.atna;

import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.message.HL7V3MessageBuilderHelper;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201309UV02_Message;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.IHETransaction;
import com.vangent.hieos.xutil.atna.ATNAAuditEventPatientIdentityFeed;
import com.vangent.hieos.xutil.atna.ATNAAuditEventPatientRecord;
import com.vangent.hieos.xutil.atna.ATNAAuditEventQuery;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class ATNAAuditEventHelper {

    private final static Logger logger = Logger.getLogger(ATNAAuditEventHelper.class);

    /**
     * 
     * @param actorType
     * @param message
     * @param updateMode
     * @param subjectIdentifiers 
     * @return
     */
    public static ATNAAuditEventPatientIdentityFeed getATNAAuditEventPatientIdentityFeed(
            ATNAAuditEvent.ActorType actorType,
            HL7V3Message message,
            boolean updateMode,
            List<SubjectIdentifier> subjectIdentifiers) {
        ATNAAuditEventPatientIdentityFeed auditEvent = new ATNAAuditEventPatientIdentityFeed();
        auditEvent.setTransaction(IHETransaction.ITI44);
        auditEvent.setActorType(actorType);
        auditEvent.setUpdateMode(updateMode);
        String messageId = HL7V3MessageBuilderHelper.getMessageId(message);
        auditEvent.setMessageId(messageId);
        String patientId = ATNAAuditEventHelper.getPatientId(subjectIdentifiers);
        auditEvent.setPatientId(patientId);
        return auditEvent;
    }

    /**
     *
     * @param actorType
     * @param message
     * @param subject
     * @param targetEndpoint
     * @return
     */
    public static ATNAAuditEventPatientRecord getATNAAuditEventPatientRecord(
            ATNAAuditEvent.ActorType actorType,
            Subject subject, String targetEndpoint, String messageId) {
        ATNAAuditEventPatientRecord auditEvent = new ATNAAuditEventPatientRecord();
        auditEvent.setTransaction(IHETransaction.ITI46);
        auditEvent.setActorType(actorType);
        auditEvent.setTargetEndpoint(targetEndpoint);
        auditEvent.setMessageId(messageId);
        for (SubjectIdentifier subjectIdentifier : subject.getSubjectIdentifiers()) {
            auditEvent.addPatientId(subjectIdentifier.getCXFormatted());
        }
        // FIXME: Should we add other ids????
        return auditEvent;
    }

    /**
     * 
     * @param actorType
     * @param request
     * @param subjectSearchResponse
     * @param homeCommunityId
     * @return
     */
    public static ATNAAuditEventQuery getATNAAuditEventPDQQueryProvider(
            ATNAAuditEvent.ActorType actorType,
            PRPA_IN201305UV02_Message request,
            SubjectSearchResponse subjectSearchResponse,
            String homeCommunityId) {
        ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getAuditEventQueryProvider(
                actorType, request, subjectSearchResponse, homeCommunityId);
        auditEvent.setTransaction(IHETransaction.ITI47);
        return auditEvent;
    }

    /**
     * 
     * @param actorType
     * @param request
     * @param homeCommunityId
     * @param targetEndpoint
     * @return
     */
    public static ATNAAuditEventQuery getATNAAuditEventPDQQueryInitiator(
            ATNAAuditEvent.ActorType actorType,
            PRPA_IN201305UV02_Message request,
            String homeCommunityId, String targetEndpoint) {
        ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getAuditEventQueryInitiator(
                actorType, request, homeCommunityId, targetEndpoint);
        auditEvent.setTransaction(IHETransaction.ITI47);
        return auditEvent;
    }

    /**
     * 
     * @param actorType
     * @param request
     * @param subjectSearchResponse
     * @param homeCommunityId
     * @return
     */
    public static ATNAAuditEventQuery getATNAAuditEventPIXQueryProvider(
            ATNAAuditEvent.ActorType actorType,
            PRPA_IN201309UV02_Message request,
            SubjectSearchResponse subjectSearchResponse,
            String homeCommunityId) {
        ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getAuditEventQueryProvider(
                actorType, request, subjectSearchResponse, homeCommunityId);
        auditEvent.setTransaction(IHETransaction.ITI45);
        return auditEvent;
    }

    /**
     *
     * @param actorType
     * @param request
     * @param homeCommunityId
     * @param targetEndpoint
     * @return
     */
    public static ATNAAuditEventQuery getATNAAuditEventPIXQueryInitiator(
            ATNAAuditEvent.ActorType actorType,
            PRPA_IN201309UV02_Message request,
            String homeCommunityId, String targetEndpoint) {
        ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getAuditEventQueryInitiator(
                actorType, request, homeCommunityId, targetEndpoint);
        auditEvent.setTransaction(IHETransaction.ITI45);
        return auditEvent;
    }

    /**
     * 
     * @param actorType
     * @param request
     * @param subjectSearchResponse
     * @param homeCommunityId
     * @return
     */
    private static ATNAAuditEventQuery getAuditEventQueryProvider(
            ATNAAuditEvent.ActorType actorType,
            HL7V3Message request,
            SubjectSearchResponse subjectSearchResponse,
            String homeCommunityId) {
        ATNAAuditEventQuery auditEvent = new ATNAAuditEventQuery();
        auditEvent.setAuditEventType(ATNAAuditEvent.AuditEventType.QUERY_PROVIDER);
        auditEvent.setActorType(actorType);

        // Add patient ids for all subjects returned.
        List<Subject> subjects = subjectSearchResponse.getSubjects();
        for (Subject subject : subjects) {
            for (SubjectIdentifier subjectIdentifer : subject.getSubjectIdentifiers()) {
                auditEvent.addPatientId(subjectIdentifer.getCXFormatted());
            }
        }
        auditEvent.setHomeCommunityId(homeCommunityId);
        auditEvent.setQueryId(HL7V3MessageBuilderHelper.getQueryId(request));
        auditEvent.setQueryText(HL7V3MessageBuilderHelper.getQueryByParameter(request));
        return auditEvent;
    }

    /**
     *
     * @param actorType
     * @param request
     * @param homeCommunityId
     * @param targetEndpoint
     * @return
     */
    private static ATNAAuditEventQuery getAuditEventQueryInitiator(
            ATNAAuditEvent.ActorType actorType,
            HL7V3Message request,
            String homeCommunityId, String targetEndpoint) {
        ATNAAuditEventQuery auditEvent = new ATNAAuditEventQuery();
        auditEvent.setAuditEventType(ATNAAuditEvent.AuditEventType.QUERY_INITIATOR);
        auditEvent.setActorType(actorType);
        auditEvent.setHomeCommunityId(homeCommunityId);
        auditEvent.setQueryId(HL7V3MessageBuilderHelper.getQueryId(request));
        auditEvent.setQueryText(HL7V3MessageBuilderHelper.getQueryByParameter(request));
        auditEvent.setTargetEndpoint(targetEndpoint);
        return auditEvent;
    }

    /**
     *
     * @param subject
     * @return
     */
    static private String getPatientId(List<SubjectIdentifier> subjectIdentifiers) {
        String patientId = null;
        if (subjectIdentifiers != null && !subjectIdentifiers.isEmpty()) {
            // Return first one.
            SubjectIdentifier subjectIdentifier = subjectIdentifiers.get(0);
            patientId = subjectIdentifier.getCXFormatted();
        }
        return patientId;
    }
}
