/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.atna;

import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xua.client.XServiceProvider;

// Third-party.
import java.io.UnsupportedEncodingException;

import java.lang.management.ManagementFactory;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 *
 * @author Vincent Lewis (original)
 * @author Sastry Dhara  (clean up,, refactor)
 * @author Ravi Nistala  (clean up, refactor)
 * @author Bernie Thuman (rewrite).
 */
public class XATNALogger {

    private final static Logger logger = Logger.getLogger(XATNALogger.class);
    private static final String IHE_TX = "IHE Transactions";
    private static final String IHE_XDS_MDT = "IHE XDS Metadata";
    private static final String DCM = "DCM";
    private static final String APL_ACTV = "Application Activity";
    private static final String AUDIT_SRC_SUFFIX = "VANGENT_HIEOS";
    private boolean performAudit = false;
    private boolean logSAMLAssertion = false;
    private AuditMessageBuilder amb = null;
    private String hostAddress = "";
    private String pid = "";
    private String endpoint = "";
    private String fromAddress = "";
    private String replyTo = "";
    private String userName = "";
    private UserContext userContext = null;
    private MessageContext parentThreadMessageContext = null;

    /**
     *
     * @throws Exception
     */
    public XATNALogger() throws Exception {
        this(null);
    }

    /**
     * 
     * @param userContext
     * @throws Exception
     */
    public XATNALogger(UserContext userContext) throws Exception {
        this.userContext = userContext;
        XConfig xconfig = XConfig.getInstance();
        this.performAudit = xconfig.getHomeCommunityConfigPropertyAsBoolean("ATNAPerformAudit");
        if (this.performAudit) {
            this.logSAMLAssertion = xconfig.getHomeCommunityConfigPropertyAsBoolean("ATNALogSAMLAssertion", false);
        }
    }

    /**
     *
     * @param parentThreadMessageContext
     */
    public void setParentThreadMessageContext(MessageContext parentThreadMessageContext) {
        this.parentThreadMessageContext = parentThreadMessageContext;
    }

    /**
     *
     * @return
     */
    public boolean isPerformAudit() {
        return performAudit;
    }

    /**
     *
     * @param auditEvent
     */
    public void audit(ATNAAuditEventStart auditEvent) {
        this.setContextVariables();
        CodedValueType eventId = this.getCodedValueType("110100", DCM, APL_ACTV);
        CodedValueType eventType = this.getCodedValueType("110120", DCM, auditEvent.getTransactionDisplayName());
        amb = new AuditMessageBuilder(null, null, eventId, eventType, "E", "0");
        CodedValueType roleIdCode = this.getCodedValueType("110150", DCM, "Application");
        amb.setActiveParticipant(
                "root", /* userId */
                this.pid, /* altnerateuserId */
                null, /* userName */
                "false", /* userIsRequestor */
                roleIdCode, /* roleIdCode */
                "2", /* networkAccessPointTypeCode (1 = hostname, 2 = IP Address) */
                this.hostAddress);  /* networkAccessPointId  */
        this.persistMessage(auditEvent);
    }

    /**
     * 
     * @param auditEvent
     */
    public void audit(ATNAAuditEventStop auditEvent) {
        this.setContextVariables();
        CodedValueType eventId = this.getCodedValueType("110100", DCM, APL_ACTV);
        CodedValueType eventType = this.getCodedValueType("110121", DCM, auditEvent.getTransactionDisplayName());
        amb = new AuditMessageBuilder(null, null, eventId, eventType, "E", "0");
        CodedValueType roleIdCode = this.getCodedValueType("110150", DCM, "Application");
        amb.setActiveParticipant(
                "root", /* userId */
                this.pid, /* altnerateuserId */
                null, /* userName */
                "false", /* userIsRequestor */
                roleIdCode, /* roleIdCode */
                "2", /* networkAccessPointTypeCode (1 = hostname, 2 = IP Address) */
                this.hostAddress);  /* networkAccessPointId  */
        this.persistMessage(auditEvent);
    }

    /**
     *
     * @param auditEvent
     * @throws Exception
     */
    public void audit(ATNAAuditEventQuery auditEvent) throws Exception {
        this.setContextVariables();
        if (auditEvent.getAuditEventType().equals(ATNAAuditEvent.AuditEventType.QUERY_INITIATOR)) {
            this.auditQueryInitiator(auditEvent);
        } else if (auditEvent.getAuditEventType().equals(ATNAAuditEvent.AuditEventType.QUERY_PROVIDER)) {
            this.auditQueryProvider(auditEvent);
        }
        this.persistMessage(auditEvent);
    }

    /**
     * 
     * @param auditEvent
     * @throws Exception 
     */
    public void audit(ATNAAuditEventRegisterDocumentSet auditEvent) throws Exception {
        this.setContextVariables();
        if (auditEvent.getAuditEventType().equals(ATNAAuditEvent.AuditEventType.IMPORT)) {
            this.auditRegisterDocumentSetImport(auditEvent);
        } else if (auditEvent.getAuditEventType().equals(ATNAAuditEvent.AuditEventType.EXPORT)) {
            this.auditRegisterDocumentSetExport(auditEvent);
        }
        this.persistMessage(auditEvent);
    }

    /**
     *
     * @param auditEvent
     */
    public void audit(ATNAAuditEventPatientIdentityFeed auditEvent) {
        this.setContextVariables();
        this.auditPatientIdentityFeedToRegistry(auditEvent);
        this.persistMessage(auditEvent);
    }

    /**
     *
     * @param auditEvent
     * @throws Exception
     */
    public void audit(ATNAAuditEventRetrieveDocumentSet auditEvent) throws Exception {
        this.setContextVariables();
        if (auditEvent.getAuditEventType().equals(ATNAAuditEvent.AuditEventType.IMPORT)) {
            this.auditRetrieveDocumentSetImport(auditEvent);
        } else if (auditEvent.getAuditEventType().equals(ATNAAuditEvent.AuditEventType.EXPORT)) {
            this.auditRetrieveDocumentSetExport(auditEvent);
        }
        this.persistMessage(auditEvent);
    }

    /**
     *
     * @param auditEvent
     * @throws MalformedURLException
     */
    public void audit(ATNAAuditEventPatientRecord auditEvent) throws MalformedURLException {
        this.setContextVariables();
        this.auditPatientRecordUpdateNotification(auditEvent);
        this.persistMessage(auditEvent);
    }

    /**
     *
     * @param auditEvent
     * @throws MalformedURLException
     */
    private void auditPatientRecordUpdateNotification(ATNAAuditEventPatientRecord auditEvent) throws MalformedURLException {
        this.addEvent(auditEvent, "110110", "Patient Record", "R");

        // Source (PIX Manager):
        this.addSource(this.endpoint, this.pid, null, "false", this.hostAddress);

        // Destination (PIX Consumer):
        URL url = new URL(auditEvent.getTargetEndpoint());
        this.addDestination(auditEvent.getTargetEndpoint(), null, null, "false", url.getHost());

        // Patient IDs.
        this.addPatientIds(auditEvent.getPatientIds(), auditEvent.getMessageId());
    }

    /**
     *
     * @param auditEvent
     */
    private void auditRetrieveDocumentSetExport(ATNAAuditEventRetrieveDocumentSet auditEvent) {
        this.addEvent(auditEvent, "110106", "Export", "R");

        // Source (Document Repository):
        this.addSource(this.endpoint, this.pid, null, "false", this.hostAddress);

        // Destination (Document Consumer):
        this.addDestination(this.replyTo, null, this.userName, "true", this.fromAddress);

        // Document URIs:
        this.addDocuments(auditEvent);
    }

    /**
     *
     * @param auditEvent
     * @throws MalformedURLException
     */
    private void auditRetrieveDocumentSetImport(ATNAAuditEventRetrieveDocumentSet auditEvent) throws MalformedURLException {
        this.addEvent(auditEvent, "110107", "Import", "C");

        // Source (Document Repository)
        URL url = new URL(auditEvent.getTargetEndpoint());
        this.addSource(auditEvent.getTargetEndpoint(), null, null, "false", url.getHost());

        // Destination (Document Consumer):
        this.addDestination(this.endpoint, this.pid, this.userName, "true", this.hostAddress);

        this.addHumanRequestor();  // Add human requestor (if exists).

        // Document URIs:
        this.addDocuments(auditEvent);
    }

    /**
     *
     * @param auditEvent
     * @throws UnsupportedEncodingException
     */
    private void auditQueryProvider(
            ATNAAuditEventQuery auditEvent) throws UnsupportedEncodingException {
        this.addEvent(auditEvent, "110112", "Query", "E");

        // Source (Initiating Gateway):
        this.addSource(this.replyTo, null, this.userName, "true", this.fromAddress);

        // Destination (Responding Gateway):
        this.addDestination(this.endpoint, this.pid, null, "false", this.hostAddress);

        // Patient IDs:
        this.addPatientIds(auditEvent.getPatientIds(), null /* message id */);

        // Query:
        this.addQueryDetails(auditEvent);
    }

    /**
     *
     * @param auditEvent
     * @throws UnsupportedEncodingException
     * @throws MalformedURLException
     */
    private void auditQueryInitiator(
            ATNAAuditEventQuery auditEvent) throws UnsupportedEncodingException, MalformedURLException {
        this.addEvent(auditEvent, "110112", "Query", "E");

        // Source (Document Consumer / Gateway):
        this.addSource(this.endpoint, this.pid, this.userName, "true", this.hostAddress);

        // Destination (Registry / Gateway):
        URL url = new URL(auditEvent.getTargetEndpoint());
        this.addDestination(auditEvent.getTargetEndpoint(), null, null, "false", url.getHost());

        // Patient IDs:
        this.addPatientIds(auditEvent.getPatientIds(), null /* message id */);

        // Query:
        this.addQueryDetails(auditEvent);
    }

    /**
     *
     * @param auditEvent
     * @throws MalformedURLException
     */
    private void auditRegisterDocumentSetExport(ATNAAuditEventRegisterDocumentSet auditEvent) throws MalformedURLException {
        this.addEvent(auditEvent, "110106", "Export", "R");

        // Source (Repository):
        this.addSource(this.endpoint, this.pid, null, "true", this.hostAddress);

        // Destination (Registry):
        URL url = new URL(auditEvent.getTargetEndpoint());
        this.addDestination(auditEvent.getTargetEndpoint(), null, null, "false", url.getHost());

        // Patient ID:
        this.addPatientId(auditEvent.getPatientId(), null /* message id */);

        // Submission Set:
        this.addSubmissionSet(auditEvent);
    }

    /**
     *
     * @param auditEvent
     */
    private void auditRegisterDocumentSetImport(ATNAAuditEventRegisterDocumentSet auditEvent) {
        this.addEvent(auditEvent, "110107", "Import", "C");

        // Source (Repository / Document Source):
        this.addSource(this.replyTo, null, this.userName, "true", this.fromAddress);

        // Destination (Registry):
        this.addDestination(this.endpoint, this.pid, null, "false", this.hostAddress);

        // Patient ID:
        this.addPatientId(auditEvent.getPatientId(), null /* message id */);

        // Submission Set:
        this.addSubmissionSet(auditEvent);
    }

    /**
     *
     * @param auditEvent
     */
    private void auditPatientIdentityFeedToRegistry(ATNAAuditEventPatientIdentityFeed auditEvent) {
        this.addEvent(auditEvent, "110110", "Patient Record", auditEvent.isUpdateMode() ? "U" : "C");

        // Source (Patient Identity Source):
        String sourceIdentity = auditEvent.getSourceIdentity();
        String sourceIP = auditEvent.getSourceIP();
        this.addSource(sourceIdentity != null ? sourceIdentity : this.replyTo,
                null, this.userName, "true", sourceIP != null ? sourceIP : this.fromAddress);

        // Destination (Registry):
        this.addDestination(this.endpoint, this.pid, null, "false", this.hostAddress);

        // Patient/Message ID:
        this.addPatientId(auditEvent.getPatientId(), auditEvent.getMessageId());
    }

    /**
     * 
     */
    private void addHumanRequestor() {
        // TBD: Add role usage from "userContext"
        if (this.userContext != null) {
            amb.setActiveParticipant(
                    userContext.getUserId(), /* userId  */
                    null, /* alternateUserId */
                    userContext.getUserName(), /* userName */
                    "true", /* userIsRequestor */
                    null, /* roleIdCode */
                    null, /* networkAccessPointTypeCode (1 = hostname, 2 = IP Address) */
                    null); /* networkAccessPointId */
        }
    }

    /**
     *
     */
    private void addSAMLAssertion() {
        if (this.logSAMLAssertion) {
            OMElement assertionEle = null;
            try {
                // Get SAML assertion from the current message context.
                MessageContext mc = this.getCurrentMessageContext();
                if (mc != null) {
                    // if there isn't a MessageContext then there won't
                    // be any SAML assertions, most likely to happen
                    // during service start-up
                    assertionEle = XServiceProvider.getSAMLAssertionFromRequest(mc);
                }
            } catch (SOAPFaultException ex) {
                // Eat this.
                logger.warn("Could not get SAML Assertion", ex);
            }
            if (assertionEle != null) {
                String assertionId = assertionEle.getAttributeValue(new QName("ID"));
                String assertionStr = assertionEle.toString();
                CodedValueType participantObjectIdentifier = getCodedValueType("11", "RFC-3881", "User Identifier");
                amb.setParticipantObject(
                        "2", /* participantObjectTypeCode */
                        "14", /* participantObjectTypeCodeRole */
                        null, /* participantObjectDataLifeCycle */
                        participantObjectIdentifier, /* participantIDTypeCode */
                        null, /* participantObjectSensitivity */
                        assertionId, /* participantObjectId */
                        null, /* participantObjectName */
                        null, /* participantObjectQuery */
                        "SAML Assertion", /* participantObjectDetailName */
                        assertionStr.getBytes()); /* participantObjectDetailValue */
            }
        }
    }

    /**
     * 
     * @param auditEvent
     */
    private void addSubmissionSet(ATNAAuditEventRegisterDocumentSet auditEvent) {
        // Submission Set:
        CodedValueType participantObjectIdentifier = this.getCodedValueType(MetadataSupport.XDSSubmissionSet_classification_uuid, IHE_XDS_MDT, "submission set classificationNode");
        amb.setParticipantObject(
                "2", /* participantObjectTypeCode */
                "20", /* participantObjectTypeCodeRole */
                null, /* participantObjectDataLifeCycle */
                participantObjectIdentifier, /* participantIDTypeCode */
                null, /* participantObjectSensitivity */
                auditEvent.getSubmissionSetId(), /* participantObjectId */
                null, /* participantObjectName */
                null); /* participantObjectQuery */
    }

    /**
     * 
     * @param patientIds
     * @param messageId
     */
    private void addPatientIds(List<String> patientIds, String messageId) {
        for (String patientId : patientIds) {
            this.addPatientId(patientId, messageId);
        }
    }

    /**
     *
     * @param patientId
     * @param messageId
     */
    private void addPatientId(String patientId, String messageId) {
        CodedValueType participantObjectIdentifier = this.getCodedValueType("2", "RFC-3881", "Patient Number");
        byte[] messageIdValue = null;
        String messageIdType = null;
        if (messageId != null) {
            messageIdValue = messageId.getBytes();
            messageIdType = "II";
        }
        amb.setParticipantObject(
                "1", /* participantObjectTypeCode */
                "1", /* participantObjectTypeCodeRole */
                null, /* participantObjectDataLifeCycle */
                participantObjectIdentifier, /* participantIDTypeCode */
                null, /* participantObjectSensitivity */
                patientId, /* participantObjectId */
                null, /* participantObjectName */
                null, /* participantObjectQuery */
                messageIdType, /* participantObjectDetailName */
                messageIdValue); /* participantObjectDetailValue */
    }

    /**
     *
     * @param auditEvent
     */
    private void addQueryDetails(ATNAAuditEventQuery auditEvent) {

        byte[] queryBase64Bytes = Base64.encodeBase64(auditEvent.getQueryText().getBytes());

        CodedValueType participantObjectIdentifier = this.getCodedValueType(auditEvent.getTransaction().toString(),
                IHE_TX, auditEvent.getTransactionDisplayName());
        String[] participantObjectDetailNames = null;
        byte[][] participantObjectDetailValues = null;
        String homeCommunityId = auditEvent.getHomeCommunityId();
        if (homeCommunityId != null) {
            participantObjectDetailNames = new String[2];
            participantObjectDetailValues = new byte[2][];
        } else {
            participantObjectDetailNames = new String[1];
            participantObjectDetailValues = new byte[1][];
        }
        participantObjectDetailNames[0] = "QueryEncoding";
        participantObjectDetailValues[0] = "UTF-8".getBytes();
        if (homeCommunityId != null) {
            participantObjectDetailNames[1] = "urn:ihe:iti:xca:2010:homeCommunityId";
            participantObjectDetailValues[1] = homeCommunityId.getBytes();
        }

        amb.setParticipantObject(
                "2", /* participantObjectTypeCode */
                "24", /* participantObjectTypeCodeRole */
                null, /* participantObjectDataLifeCycle */
                participantObjectIdentifier, /* participantIDTypeCode */
                null, /* participantObjectSensitivity */
                auditEvent.getQueryId(), /* participantObjectId */
                null, /*homeCommunityId,*/ /* participantObjectName */
                queryBase64Bytes, /* participantObjectQuery */
                participantObjectDetailNames, /* participantObjectDetailNames */
                participantObjectDetailValues); /* participantObjectDetailValues */
    }

    /**
     * 
     * @param auditEvent
     */
    private void addDocuments(ATNAAuditEventRetrieveDocumentSet auditEvent) {
        // Document URIs:
        for (ATNAAuditDocument document : auditEvent.getDocuments()) {
            this.addDocumentDetails(document);
        }
    }

    /**
     * 
     * @param document
     */
    private void addDocumentDetails(ATNAAuditDocument document) {
        String homeCommunityId = document.getHomeCommunityId();
        String documentUniqueId = document.getDocumentUniqueId();
        String repositoryUniqueId = document.getRepositoryUniqueId();
        CodedValueType participantObjectIdentifier = this.getCodedValueType("9", "RFC-3881", "Report Number");
        String[] participantObjectDetailNames = null;
        byte[][] participantObjectDetailValues = null;
        if (homeCommunityId != null) {
            participantObjectDetailNames = new String[2];
            participantObjectDetailValues = new byte[2][];
        } else {
            participantObjectDetailNames = new String[1];
            participantObjectDetailValues = new byte[1][];
        }
        participantObjectDetailNames[0] = "Repository Unique Id";
        participantObjectDetailValues[0] = repositoryUniqueId.getBytes();
        if (homeCommunityId != null) {
            participantObjectDetailNames[1] = "ihe:homeCommunityID";
            participantObjectDetailValues[1] = homeCommunityId.getBytes();
        }
        amb.setParticipantObject(
                "2", /* participantObjectTypeCode */
                "3", /* participantObjectTypeCodeRole */
                null, /* participantObjectDataLifeCycle */
                participantObjectIdentifier, /* participantIDTypeCode */
                null, /* participantObjectSensitivity */
                documentUniqueId, /* participantObjectId */
                null, /*homeCommunityId,*/ /* participantObjectName */
                null, /* participantObjectQuery */
                participantObjectDetailNames, /* participantObjectDetailNames */
                participantObjectDetailValues); /* participantObjectDetailValues */
    }

    /**
     *
     * @param auditEvent
     * @param eventCode
     * @param eventName
     * @param eventAction
     */
    private void addEvent(ATNAAuditEvent auditEvent, String eventCode, String eventName, String eventAction) {
        CodedValueType eventId = this.getCodedValueType(eventCode, DCM, eventName);
        CodedValueType eventType = this.getCodedValueType(
                auditEvent.getTransaction().toString(),
                IHE_TX,
                auditEvent.getTransactionDisplayName());
        amb = new AuditMessageBuilder(null, null, eventId, eventType, eventAction, auditEvent.getOutcomeIndicator().toString());
    }

    /**
     * 
     * @param userId
     * @param alternateUserId
     * @param userName
     * @param userIsRequestor
     * @param networkAccessPointId
     */
    private void addSource(String userId, String alternateUserId, String userName,
            String userIsRequestor, String networkAccessPointId) {
        CodedValueType roleIdCode = this.getCodedValueType("110153", DCM, "Source");
        amb.setActiveParticipant(
                userId, /* userId */
                alternateUserId, /* alternateUserId */
                userName, /* userName */
                userIsRequestor, /* userIsRequestor */
                roleIdCode, /* roleIdCode */
                "2", /* networkAccessPointTypeCode (1 = hostname, 2 = IP Address) */
                networkAccessPointId); /* networkAccessPointId */
    }

    /**
     *
     * @param userId
     * @param alternateUserId
     * @param userName
     * @param userIsRequestor
     * @param networkAccessPointId
     */
    private void addDestination(String userId, String alternateUserId, String userName,
            String userIsRequestor, String networkAccessPointId) {
        CodedValueType roleIdCode = this.getCodedValueType("110152", DCM, "Destination");
        amb.setActiveParticipant(
                userId, /* userId */
                alternateUserId, /* alternateUserId */
                userName, /* userName */
                userIsRequestor, /* userIsRequestor */
                roleIdCode, /* roleIdCode */
                "2", /* networkAccessPointTypeCode (1 = hostname, 2 = IP Address) */
                networkAccessPointId); /* networkAccessPointId */
    }

    /**
     *
     * @param code
     * @param codeSystem
     * @param displayName
     * @return
     */
    private CodedValueType getCodedValueType(String code, String codeSystem, String displayName) {
        CodedValueType codeValueType = new CodedValueType();
        codeValueType.setCode(code);
        codeValueType.setCodeSystem(codeSystem);
        codeValueType.setCodeSystemName(codeSystem);
        codeValueType.setDisplayName(displayName);
        return codeValueType;
    }

    /**
     * 
     * @return
     */
    private MessageContext getCurrentMessageContext() {
        MessageContext currentMessageContext = MessageContext.getCurrentMessageContext();
        if (currentMessageContext == null) {
            // May be sending outbound SOAP requests in multi-threaded mode (i.e. XCA, XCPD).
            currentMessageContext = this.parentThreadMessageContext;
        }
        return currentMessageContext;
    }

    /**
     *
     */
    private void setContextVariables() {
        // Set host address:
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            this.hostAddress = addr.getHostAddress();
        } catch (UnknownHostException e) {
            this.hostAddress = null;
            logger.error("Exception in XATNALogger", e);
        }
        // Set target endpoint and process id:
        //this.targetEndpoint = targetEndpoint;
        this.pid = ManagementFactory.getRuntimeMXBean().getName();
        MessageContext messageContext = this.getCurrentMessageContext();
        if (messageContext != null) {

            // Set current endpoint:
            try {
                this.endpoint = messageContext.getTo().toString();
            } catch (Exception e) {
                this.endpoint = null;
                logger.error("Exception in XATNALogger", e);
            }

            // Set from address:
            try {
                this.fromAddress = (String) messageContext.getProperty(MessageContext.REMOTE_ADDR);
            } catch (Exception e) {
                this.fromAddress = null;
                logger.error("Exception in XATNALogger", e);
            }

            // Set replyTo address:
            try {
                org.apache.axis2.addressing.EndpointReference replyToEndpointRef = messageContext.getReplyTo();
                this.replyTo = replyToEndpointRef != null ? replyToEndpointRef.toString() : null;
            } catch (Exception e) {
                this.replyTo = null;
                logger.error("Exception in XATNALogger", e);
            }

            // Set userName on request (if available):
            this.userName = this.getUserNameFromRequest();

        } else {
            this.endpoint = hostAddress;
            this.fromAddress = hostAddress;
            this.replyTo = hostAddress;
        }
    }

    /**
     *
     */
    private void persistMessage(ATNAAuditEvent auditEvent) {
        // Persist the message.
        if (amb != null) {
            addSAMLAssertion();
            amb.setAuditSource(this.getAuditSourceId(auditEvent.getActorType()), null, null);
            if (logger.isTraceEnabled()) {
                logger.trace("--- ATNA Audit Event ---");
                logger.trace("+++ ATNA AuditEvent +++ ");
                logger.trace("... transaction: " + auditEvent.getTransaction().toString());
                logger.trace("... display name: " + auditEvent.getTransactionDisplayName());
                logger.trace("... actor: " + auditEvent.getActorType().toString());
                logger.trace("... event type: " + auditEvent.getAuditEventType().toString());
                logger.trace("------------------------");
            }
            amb.persistMessage();
        }
    }

    /**
     * 
     * @return
     */
    private String getAuditSourceId(ATNAAuditEvent.ActorType actorType) {
        return this.hostAddress + "@" + actorType + "_" + AUDIT_SRC_SUFFIX;
    }

    /**
     *
     * @return
     */
    private String getUserNameFromRequest() {
        XServiceProvider xServiceProvider = new XServiceProvider(null);
        return xServiceProvider.getUserNameFromRequest(this.getCurrentMessageContext());
    }
}
