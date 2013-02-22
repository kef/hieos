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
package com.vangent.hieos.services.pixpdqv3.transactions;

import com.vangent.hieos.empi.adapter.EMPIAdapter;
import com.vangent.hieos.empi.adapter.EMPIAdapterFactory;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownSubjectIdentifier;
import com.vangent.hieos.hl7v3util.atna.ATNAAuditEventHelper;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message_Builder;
import com.vangent.hieos.hl7v3util.model.subject.SubjectBuilder;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.message.HL7V3MessageBuilderHelper;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201301UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201309UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201310UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201310UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteriaBuilder;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.message.HL7V3ErrorDetail;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201302UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201304UV02_Message;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectMergeRequest;
import com.vangent.hieos.hl7v3util.model.subject.SubjectMergeRequestBuilder;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;
import com.vangent.hieos.empi.adapter.EMPINotification;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEventPatientIdentityFeed;
import com.vangent.hieos.xutil.atna.ATNAAuditEventQuery;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIXRequestHandler extends RequestHandler {

    private final static Logger logger = Logger.getLogger(PIXRequestHandler.class);

    // Type type of message received.
    /**
     *
     */
    public enum MessageType {

        /**
         *
         */
        PatientRegistryGetIdentifiersQuery,
        /**
         *
         */
        PatientRegistryRecordAdded,
        /**
         *
         */
        PatientRegistryRecordRevised,
        /**
         *
         */
        PatientRegistryDuplicatesResolved
    };

    /**
     *
     * @param log_message
     */
    public PIXRequestHandler(XLogMessage log_message) {
        super(log_message);
    }

    /**
     *
     * @param request
     * @param messageType
     * @return
     * @throws SOAPFaultException
     */
    public OMElement run(OMElement request, MessageType messageType) throws SOAPFaultException {
        HL7V3Message result = null;
        log_message.setPass(true);  // Hope for the best.
        switch (messageType) {
            case PatientRegistryGetIdentifiersQuery:
                result = this.processPatientRegistryGetIdentifiersQuery(new PRPA_IN201309UV02_Message(request));
                break;
            case PatientRegistryRecordAdded:
                result = this.processPatientRegistryRecordAdded(new PRPA_IN201301UV02_Message(request));
                break;
            case PatientRegistryRecordRevised:
                result = this.processPatientRegistryRecordRevised(new PRPA_IN201302UV02_Message(request));
                break;
            case PatientRegistryDuplicatesResolved:
                result = this.processPatientRegistryDuplicatesResolved(new PRPA_IN201304UV02_Message(request));
                break;
        }
        if (result != null) {
            log_message.addOtherParam("Response", result.getMessageNode());
        }
        return (result != null) ? result.getMessageNode() : null;
    }

    /**
     *
     * @param PRPA_IN201301UV02_Message
     * @return
     * @throws SOAPFaultException
     */
    private MCCI_IN000002UV01_Message processPatientRegistryRecordAdded(PRPA_IN201301UV02_Message request) throws SOAPFaultException {
        this.validateHL7V3Message(request);
        DeviceInfo senderDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        HL7V3ErrorDetail errorDetail = null;
        try {
            SubjectBuilder builder = new SubjectBuilder();
            Subject subject = builder.buildSubject(request);
            subject.setIdentitySource(senderDeviceInfo.getId());
            // Clone identifiers (for audit later).
            List<SubjectIdentifier> subjectIdentifiers = SubjectIdentifier.clone(subject.getSubjectIdentifiers());

            // Add subject using EMPI.
            EMPIAdapter adapter = EMPIAdapterFactory.getInstance();
            adapter.setSenderDeviceInfo(senderDeviceInfo);
            EMPINotification updateNotificationContent = adapter.addSubject(subject);

            // Send out notifications.
            this.sendUpdateNotifications(updateNotificationContent);

            // Perform ATNA audit.
            this.performAuditPatientIdentityFeed(request, 
                    ATNAAuditEventPatientIdentityFeed.EventActionCode.CREATE,
                    subjectIdentifiers);
            
        } catch (EMPIExceptionUnknownIdentifierDomain ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage(), EMPIExceptionUnknownIdentifierDomain.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        } catch (EMPIExceptionUnknownSubjectIdentifier ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage(), EMPIExceptionUnknownSubjectIdentifier.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        } catch (CloneNotSupportedException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (ModelBuilderException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (EMPIException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (Exception ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        }
        if (log_message.isLogEnabled() && errorDetail != null) {
            log_message.addErrorParam("EXCEPTION", errorDetail.getText());
        }
        MCCI_IN000002UV01_Message ackResponse = this.getPatientIdentityFeedResponse(request, errorDetail);
        this.validateHL7V3Message(ackResponse);
        return ackResponse;
    }

    /**
     *
     * @param PRPA_IN201302UV02_Message
     * @return
     * @throws SOAPFaultException
     */
    private MCCI_IN000002UV01_Message processPatientRegistryRecordRevised(PRPA_IN201302UV02_Message request) throws SOAPFaultException {
        this.validateHL7V3Message(request);
        DeviceInfo senderDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        HL7V3ErrorDetail errorDetail = null;
        try {
            SubjectBuilder builder = new SubjectBuilder();
            Subject subject = builder.buildSubject(request);
            subject.setIdentitySource(senderDeviceInfo.getId());

            // Clone identifiers (for audit later).
            List<SubjectIdentifier> subjectIdentifiers = SubjectIdentifier.clone(subject.getSubjectIdentifiers());

            // Update subject through EMPI.
            EMPIAdapter adapter = EMPIAdapterFactory.getInstance();
            adapter.setSenderDeviceInfo(senderDeviceInfo);
            EMPINotification updateNotificationContent = adapter.updateSubject(subject);

            // Send out notifications.
            this.sendUpdateNotifications(updateNotificationContent);

            // Perform ATNA audit.
            this.performAuditPatientIdentityFeed(request, 
                    ATNAAuditEventPatientIdentityFeed.EventActionCode.UPDATE,
                    subjectIdentifiers);
            
        } catch (EMPIExceptionUnknownIdentifierDomain ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage(), EMPIExceptionUnknownIdentifierDomain.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        } catch (EMPIExceptionUnknownSubjectIdentifier ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage(), EMPIExceptionUnknownSubjectIdentifier.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        } catch (CloneNotSupportedException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (ModelBuilderException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (EMPIException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (Exception ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        }
        if (log_message.isLogEnabled() && errorDetail != null) {
            log_message.addErrorParam("EXCEPTION", errorDetail.getText());
        }
        MCCI_IN000002UV01_Message ackResponse = this.getPatientIdentityFeedResponse(request, errorDetail);
        this.validateHL7V3Message(ackResponse);
        return ackResponse;
    }

    /**
     *
     * @param PRPA_IN201304UV02_Message
     * @return
     * @throws SOAPFaultException
     */
    private MCCI_IN000002UV01_Message processPatientRegistryDuplicatesResolved(PRPA_IN201304UV02_Message request) throws SOAPFaultException {
        this.validateHL7V3Message(request);
        DeviceInfo senderDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        HL7V3ErrorDetail errorDetail = null;
        try {
            SubjectMergeRequestBuilder builder = new SubjectMergeRequestBuilder();
            SubjectMergeRequest subjectMergeRequest = builder.buildSubjectMergeRequest(request);

            // Clone identifiers (for audit later).
            List<SubjectIdentifier> survivingSubjectIdentifiers = SubjectIdentifier.clone(subjectMergeRequest.getSurvivingSubject().getSubjectIdentifiers());
            List<SubjectIdentifier> subsumedSubjectIdentifiers = SubjectIdentifier.clone(subjectMergeRequest.getSubsumedSubject().getSubjectIdentifiers());

            // Merge the subjects (clone first - above - since merge has side-effects).
            EMPIAdapter adapter = EMPIAdapterFactory.getInstance();
            adapter.setSenderDeviceInfo(senderDeviceInfo);
            EMPINotification updateNotificationContent = adapter.mergeSubjects(subjectMergeRequest);

            // Send update notifications.
            this.sendUpdateNotifications(updateNotificationContent);

            // Perform ATNA audits.
            this.performAuditPatientIdentityFeed(request, 
                    ATNAAuditEventPatientIdentityFeed.EventActionCode.UPDATE,
                    survivingSubjectIdentifiers);

            this.performAuditPatientIdentityFeed(request,
                    ATNAAuditEventPatientIdentityFeed.EventActionCode.DELETE,
                    subsumedSubjectIdentifiers);

        } catch (EMPIExceptionUnknownIdentifierDomain ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage(), EMPIExceptionUnknownIdentifierDomain.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        } catch (EMPIExceptionUnknownSubjectIdentifier ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage(), EMPIExceptionUnknownSubjectIdentifier.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        } catch (CloneNotSupportedException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (ModelBuilderException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (EMPIException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (Exception ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        }
        if (log_message.isLogEnabled() && errorDetail != null) {
            log_message.addErrorParam("EXCEPTION", errorDetail.getText());
        }
        MCCI_IN000002UV01_Message ackResponse = this.getPatientIdentityFeedResponse(request, errorDetail);
        this.validateHL7V3Message(ackResponse);
        return ackResponse;
    }

    /**
     *
     * @param request
     * @param errorDetail
     * @return
     */
    private MCCI_IN000002UV01_Message getPatientIdentityFeedResponse(HL7V3Message request, HL7V3ErrorDetail errorDetail) {
        DeviceInfo senderDeviceInfo = this.getDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        MCCI_IN000002UV01_Message_Builder ackBuilder =
                new MCCI_IN000002UV01_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        MCCI_IN000002UV01_Message ackResponse = ackBuilder.buildMCCI_IN000002UV01(request, errorDetail);
        return ackResponse;
    }

    /**
     *
     * @param PRPA_IN201309UV02_Message
     * @return
     * @throws SOAPFaultException
     */
    private PRPA_IN201310UV02_Message processPatientRegistryGetIdentifiersQuery(PRPA_IN201309UV02_Message request) throws SOAPFaultException {
        this.validateHL7V3Message(request);
        SubjectSearchResponse subjectSearchResponse = null;
        HL7V3ErrorDetail errorDetail = null;
        try {
            SubjectSearchCriteria subjectSearchCriteria = this.getSubjectSearchCriteria(request);
            subjectSearchResponse = this.getBySubjectIdentifiers(subjectSearchCriteria);
            this.performAuditPIXQueryProvider(request, subjectSearchResponse);
        } catch (ModelBuilderException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (EMPIExceptionUnknownIdentifierDomain ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage(), EMPIExceptionUnknownIdentifierDomain.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        } catch (EMPIExceptionUnknownSubjectIdentifier ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage(), EMPIExceptionUnknownSubjectIdentifier.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        } catch (EMPIException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        } catch (Exception ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        }
        if (log_message.isLogEnabled() && errorDetail != null) {
            log_message.addErrorParam("EXCEPTION", errorDetail.getText());
        }
        PRPA_IN201310UV02_Message pixResponse =
                this.getPatientRegistryGetIdentifiersQueryResponse(
                request, subjectSearchResponse, errorDetail);
        this.validateHL7V3Message(pixResponse);
        return pixResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    private SubjectSearchResponse getBySubjectIdentifiers(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier {
        EMPIAdapter adapter = EMPIAdapterFactory.getInstance();
        SubjectSearchResponse subjectSearchResponse = adapter.getBySubjectIdentifiers(subjectSearchCriteria);
        return subjectSearchResponse;
    }

    /**
     * 
     * @param request
     * @param subjectSearchResponse
     * @param errorDetail
     * @return
     */
    private PRPA_IN201310UV02_Message getPatientRegistryGetIdentifiersQueryResponse(PRPA_IN201309UV02_Message request, SubjectSearchResponse subjectSearchResponse, HL7V3ErrorDetail errorDetail) {
        DeviceInfo senderDeviceInfo = this.getDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        PRPA_IN201310UV02_Message_Builder builder =
                new PRPA_IN201310UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        return builder.buildPRPA_IN201310UV02_Message(request, subjectSearchResponse, errorDetail);
    }

    /**
     *
     * @param PRPA_IN201309UV02_Message
     * @return
     * @throws ModelBuilderException
     */
    private SubjectSearchCriteria getSubjectSearchCriteria(PRPA_IN201309UV02_Message request) throws ModelBuilderException {
        SubjectSearchCriteriaBuilder builder = new SubjectSearchCriteriaBuilder();
        SubjectSearchCriteria subjectSearchCriteria = builder.buildSubjectSearchCriteria(request);
        return subjectSearchCriteria;
    }

    /**
     * 
     * @param updateNotificationContent
     */
    private void sendUpdateNotifications(EMPINotification updateNotificationContent) {
        PIXUpdateNotificationHandler pixUpdateNotificationHandler = new PIXUpdateNotificationHandler();
        pixUpdateNotificationHandler.sendUpdateNotifications(updateNotificationContent);
    }

    /**
     *
     * @param request
     * @param subjectSearchResponse
     */
    private void performAuditPIXQueryProvider(PRPA_IN201309UV02_Message request, SubjectSearchResponse subjectSearchResponse) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                //String homeCommunityId = this.getGatewayConfig().getUniqueId();
                String homeCommunityId = null;  // FIXME: Should we set this?
                ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getATNAAuditEventPIXQueryProvider(
                        ATNAAuditEvent.ActorType.PIX_MANAGER, request, subjectSearchResponse, homeCommunityId);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            logger.error("PIXManager EXCEPTION: Could not perform ATNA audit", ex);
        }
    }

    /**
     *
     * @param request
     * @param eventActionCode
     * @param subjectIdentifiers
     */
    private void performAuditPatientIdentityFeed(
            HL7V3Message request, 
            ATNAAuditEventPatientIdentityFeed.EventActionCode eventActionCode,
            List<SubjectIdentifier> subjectIdentifiers) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                //String homeCommunityId = null;  // FIXME: Should we set this and use it?
                ATNAAuditEventPatientIdentityFeed auditEvent = ATNAAuditEventHelper.getATNAAuditEventPatientIdentityFeed(
                        ATNAAuditEvent.ActorType.PIX_MANAGER, request, eventActionCode, subjectIdentifiers);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            logger.error("PIXManager EXCEPTION: Could not perform ATNA audit", ex);
        }
    }
}
