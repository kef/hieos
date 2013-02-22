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
package com.vangent.hieos.hl7v2util.atna;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.segment.QPD;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.IHETransaction;
import com.vangent.hieos.xutil.atna.ATNAHL7v2AuditEventPatientIdentityFeed;
import com.vangent.hieos.xutil.atna.ATNAHL7v2AuditEventQuery;
import java.util.List;
import java.util.logging.Level;
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
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     * @param inMessageTerser
     * @param sourceIP
     * @param eventActionCode
     * @param subjectIdentifiers
     * @return
     */
    public static ATNAHL7v2AuditEventPatientIdentityFeed getATNAAuditEventPatientIdentityFeed(
            ATNAAuditEvent.ActorType actorType,
            DeviceInfo senderDeviceInfo,
            DeviceInfo receiverDeviceInfo,
            Terser inMessageTerser,
            String sourceIP,
            ATNAHL7v2AuditEventPatientIdentityFeed.EventActionCode eventActionCode,
            List<SubjectIdentifier> subjectIdentifiers) {
        ATNAHL7v2AuditEventPatientIdentityFeed auditEvent = new ATNAHL7v2AuditEventPatientIdentityFeed();
        auditEvent.setTransaction(IHETransaction.ITI8);
        auditEvent.setActorType(actorType);

        // Sending application/facility.
        auditEvent.setSendingApplication(senderDeviceInfo.getId());
        auditEvent.setSendingFacility(senderDeviceInfo.getName());
        auditEvent.setSourceIP(sourceIP);
        auditEvent.setMessageControlId(getMessageControlId(inMessageTerser));

        // Recieving application/facility.
        auditEvent.setReceivingApplication(receiverDeviceInfo.getId());
        auditEvent.setReceivingFacility(receiverDeviceInfo.getName());

        // Event action code - C/U/D
        auditEvent.setEventActionCode(eventActionCode);

        // Set patient id.
        String patientId = ATNAAuditEventHelper.getPatientId(subjectIdentifiers);
        auditEvent.setPatientId(patientId);
        return auditEvent;
    }

    /**
     * 
     * @param actorType
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     * @param inMessageTerser
     * @param sourceIP
     * @param subjectSearchResponse
     * @return
     */
    public static ATNAHL7v2AuditEventQuery getATNAAuditEventPIXQueryProvider(
            ATNAAuditEvent.ActorType actorType,
            DeviceInfo senderDeviceInfo,
            DeviceInfo receiverDeviceInfo,
            Terser inMessageTerser,
            String sourceIP,
            SubjectSearchResponse subjectSearchResponse) {
        ATNAHL7v2AuditEventQuery auditEvent = new ATNAHL7v2AuditEventQuery();
        auditEvent.setTransaction(IHETransaction.ITI9);
        auditEvent.setActorType(actorType);
        auditEvent.setAuditEventType(ATNAAuditEvent.AuditEventType.QUERY_PROVIDER);

        // Sending application/facility.
        auditEvent.setSendingApplication(senderDeviceInfo.getId());
        auditEvent.setSendingFacility(senderDeviceInfo.getName());
        auditEvent.setSourceIP(sourceIP);
        auditEvent.setMessageControlId(getMessageControlId(inMessageTerser));

        // Recieving application/facility.
        auditEvent.setReceivingApplication(receiverDeviceInfo.getId());
        auditEvent.setReceivingFacility(receiverDeviceInfo.getName());

        // Add patient ids for all subjects returned.
        List<Subject> subjects = subjectSearchResponse.getSubjects();
        for (Subject subject : subjects) {
            for (SubjectIdentifier subjectIdentifer : subject.getSubjectIdentifiers()) {
                auditEvent.addPatientId(subjectIdentifer.getCXFormatted());
            }
        }

        //auditEvent.setHomeCommunityId(homeCommunityId);
        //auditEvent.setQueryId("TODO");
        // Pull fields from QPD segment.
        try {
            QPD qpd;
            qpd = (QPD) inMessageTerser.getSegment("/QPD");
            auditEvent.setQueryText(qpd.encode());
        } catch (HL7Exception ex) {
            // Should never happen.
            auditEvent.setQueryText("Unable to get QPD segment for ATNA audit - " + ex.getMessage());
            logger.error(auditEvent.getQueryText(), ex);
        }
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

    /**
     * 
     * @param terser
     * @return
     */
    static private String getMessageControlId(Terser terser) {
        String messageControlId;
        try {
            messageControlId = terser.get("/.MSH-10");
        } catch (HL7Exception ex) {
            logger.error("Exception getting message control id for ATNA logging - " + ex.getMessage(), ex);
            messageControlId = "UNKNOWN";
        }
        return messageControlId;
    }
}
