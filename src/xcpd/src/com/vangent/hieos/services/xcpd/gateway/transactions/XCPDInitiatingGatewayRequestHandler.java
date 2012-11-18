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
package com.vangent.hieos.services.xcpd.gateway.transactions;

import com.vangent.hieos.services.xcpd.gateway.controller.XCPDGatewayRequestController;
import com.vangent.hieos.services.xcpd.gateway.exception.XCPDException;
import com.vangent.hieos.services.xcpd.gateway.framework.XCPDGatewayRequestHandler;

import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.message.HL7V3MessageBuilderHelper;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message_Builder;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201301UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201309UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201310UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201310UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.subject.SubjectBuilder;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteriaBuilder;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.message.HL7V3ErrorDetail;
import com.vangent.hieos.xutil.exception.SOAPFaultException;

import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 * Class to handle all requests to XCPD Initiating Gateway (IG).
 *
 * @author Bernie Thuman
 */
public class XCPDInitiatingGatewayRequestHandler extends XCPDGatewayRequestHandler {

    // Type type of message received.
    public enum MessageType {

        PatientRegistryGetIdentifiersQuery,
        PatientRegistryRecordAdded,
        PatientRegistryFindCandidatesQuery
    };
    private final static Logger logger = Logger.getLogger(XCPDInitiatingGatewayRequestHandler.class);
    private static XConfigActor _pdsConfig = null;
  
    /**
     * Constructor for handling requests to XCPD Initiating Gateway.
     *
     * @param log_message  Place to put internal log messages.
     */
    public XCPDInitiatingGatewayRequestHandler(XLogMessage log_message) {
        super(log_message, XCPDInitiatingGatewayRequestHandler.GatewayType.InitiatingGateway);
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
            case PatientRegistryFindCandidatesQuery:
                result = this.processPatientRegistryFindCandidatesQuery(new PRPA_IN201305UV02_Message(request));
                break;
            case PatientRegistryGetIdentifiersQuery:
                result = this.processPatientRegistryGetIdentifiersQuery(new PRPA_IN201309UV02_Message(request));
                break;
            case PatientRegistryRecordAdded:
                result = this.processPatientRegistryRecordAdded(new PRPA_IN201301UV02_Message(request));
                break;
        }
        if (result != null) {
            if (log_message.isLogEnabled()) {
                log_message.addOtherParam("Response", result.getMessageNode());
            }
        }
        return (result != null) ? result.getMessageNode() : null;
    }

    /**
     * 
     * @param request
     * @return
     * @throws SOAPFaultException
     */
    private PRPA_IN201306UV02_Message processPatientRegistryFindCandidatesQuery(PRPA_IN201305UV02_Message request) throws SOAPFaultException {
        this.validateHL7V3Message(request);
        PRPA_IN201306UV02_Message result = null;
        SubjectSearchResponse patientDiscoverySearchResponse = null;
        HL7V3ErrorDetail errorDetail = null;
        try {
            SubjectSearchCriteria subjectSearchCriteria = this.getSubjectSearchCriteria(request);
            this.validateSearchCriteriaRequest(subjectSearchCriteria);
            // TBD: Should we see if we know about this patient first?
            // DeviceInfo senderDeviceInfo = this.getDeviceInfo();
            // SubjectSearchResponse pdqSearchResponse = this.findCandidatesQuery(senderDeviceInfo, subjectSearchCriteria);

            // Initialize the request controller
            XCPDGatewayRequestController requestController = new XCPDGatewayRequestController(XCPDGatewayRequestController.SearchResultMode.Demographics, this, log_message);
            requestController.init(subjectSearchCriteria);

            // Assume that PDQ request has valid patient id and demographics (skip PDQ validation).
            patientDiscoverySearchResponse = requestController.performCrossGatewayPatientDiscovery(subjectSearchCriteria);
        } catch (XCPDException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        }
        result = this.getPatientRegistryFindCandidatesQueryResponse(request, patientDiscoverySearchResponse, errorDetail);
        this.log(errorDetail);
        this.validateHL7V3Message(result);
        return result;
    }

    /**
     * 
     * @param request
     * @return
     * @throws SOAPFaultException
     */
    private PRPA_IN201310UV02_Message processPatientRegistryGetIdentifiersQuery(PRPA_IN201309UV02_Message request) throws SOAPFaultException {
        this.validateHL7V3Message(request);
        PRPA_IN201310UV02_Message result = null;
        SubjectSearchResponse patientDiscoverySearchResponse = null;
        HL7V3ErrorDetail errorDetail = null;  // Hope for the best.
        try {
            // Get SubjectSearchCriteria instance from PIX Query.
            SubjectSearchCriteria subjectSearchCriteria = this.getSubjectSearchCriteria(request);

            // Validate that PIX Query has what is required.
            this.validateSearchCriteriaPIXRequest(subjectSearchCriteria);

            // Initialize the request controller
            XCPDGatewayRequestController requestController = new XCPDGatewayRequestController(XCPDGatewayRequestController.SearchResultMode.Identifiers, this, log_message);
            requestController.init(subjectSearchCriteria);

            if (requestController.isExternalSearchRequired()) {

                // Now issue a PDQ to PDS for the supplied patient id (on PIX query).
                DeviceInfo senderDeviceInfo = this.getSenderDeviceInfo();
                SubjectSearchResponse pdqSearchResponse = this.findCandidatesQuery(senderDeviceInfo, subjectSearchCriteria);

                // See if we only have one match (from PDS) for the patient.
                List<Subject> subjects = pdqSearchResponse.getSubjects();
                if (subjects.isEmpty()) {
                    errorDetail = new HL7V3ErrorDetail("0 local subjects found for PIX query request");
                } else if (subjects.size() > 1) {
                    // Should not be feasible, but check anyway.
                    errorDetail = new HL7V3ErrorDetail("> 1 local subjects found for PIX query request");
                } else {
                    // Get the first subject from the list.
                    Subject subject = subjects.get(0);

                    // Get ready to send CGPD request.
                    SubjectSearchCriteria patientDiscoverySearchCriteria = new SubjectSearchCriteria();
                    patientDiscoverySearchCriteria.setSubject(subject);
                    // FIXME?
                    /* TEMPORARY DISABLE FOR CONNECTATHON (need to make configurable)
                     * some RGs do not support properly -- e.g. Intersystems
                     */
                    /*
                    patientDiscoverySearchCriteria.setSpecifiedMinimumDegreeMatchPercentage(true);
                    patientDiscoverySearchCriteria.setMinimumDegreeMatchPercentage(subjectSearchCriteria.getMinimumDegreeMatchPercentage());
                    */
                    // FIXME: Should strip out all ids except for the one that matches the
                    // communityAssigningAuthority
                    SubjectIdentifierDomain communityAssigningAuthority = subjectSearchCriteria.getCommunityAssigningAuthority();
                    patientDiscoverySearchCriteria.setCommunityAssigningAuthority(communityAssigningAuthority);

                    // Fan out CGPD requests and get collective response.
                    patientDiscoverySearchResponse = requestController.performCrossGatewayPatientDiscovery(patientDiscoverySearchCriteria);
                }
            } else {
                patientDiscoverySearchResponse = requestController.getPatientDiscoverySearchResponse();
            }
        } catch (XCPDException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        }
        result = this.getCrossGatewayPatientDiscoveryResponse(request, patientDiscoverySearchResponse, errorDetail);
        this.log(errorDetail);
        this.validateHL7V3Message(result);
        return result;
    }

    /**
     *
     * @param request
     * @return
     * @throws SOAPFaultException
     */
    private MCCI_IN000002UV01_Message processPatientRegistryRecordAdded(PRPA_IN201301UV02_Message request) throws SOAPFaultException {
        this.validateHL7V3Message(request);
        HL7V3ErrorDetail errorDetail = null;  // Hope for the best.
        try {
            // Get SubjectSearchCriteria instance from PID Feed.
            SubjectSearchCriteria subjectSearchCriteria = this.getSubjectSearchCriteria(request);
            this.validateSearchCriteriaRequest(subjectSearchCriteria);

            // TBD: Should we see if we know about this patient first?
            // DeviceInfo senderDeviceInfo = this.getDeviceInfo();
            // SubjectSearchResponse pdqSearchResponse = this.findCandidatesQuery(senderDeviceInfo, subjectSearchCriteria);

            // Initialize the request controller
            XCPDGatewayRequestController requestController = new XCPDGatewayRequestController(XCPDGatewayRequestController.SearchResultMode.Identifiers, this, log_message);
            requestController.init(subjectSearchCriteria);

            // Assume that PID Feed request has valid patient id and demographics (skip PDQ validation).
            SubjectSearchResponse patientDiscoverySearchResponse = requestController.performCrossGatewayPatientDiscovery(subjectSearchCriteria);

            // Nothing more to do here ...

        } catch (XCPDException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        }
        MCCI_IN000002UV01_Message ackResponse = this.getPatientIdentityFeedResponse(request, errorDetail);
        this.log(errorDetail);
        this.validateHL7V3Message(ackResponse);
        return ackResponse;
    }

    /**
     * Convert PDQ query to SubjectSearchCriteria.
     *
     * @param request PDQ query.
     * @return SubjectSearchCriteria
     * @throws XCPDException
     */
    private SubjectSearchCriteria getSubjectSearchCriteria(PRPA_IN201305UV02_Message request) throws XCPDException {
        try {
            SubjectSearchCriteriaBuilder subjectSearchCriteriaBuilder = new SubjectSearchCriteriaBuilder();
            SubjectSearchCriteria subjectSearchCriteria = subjectSearchCriteriaBuilder.buildSubjectSearchCriteria(request);
            SubjectIdentifierDomain communityAssigningAuthority = this.getCommunityAssigningAuthority();
            subjectSearchCriteria.setCommunityAssigningAuthority(communityAssigningAuthority);
            this.setMinimumDegreeMatchPercentage(subjectSearchCriteria);
            return subjectSearchCriteria;
        } catch (ModelBuilderException ex) {
            throw new XCPDException(ex.getMessage());
        }
    }

    /**
     * Convert PIX query to SubjectSearchCriteria.
     *
     * @param request PIX query.
     * @return SubjectSearchCriteria
     * @throws XCPDException
     */
    private SubjectSearchCriteria getSubjectSearchCriteria(PRPA_IN201309UV02_Message request) throws XCPDException {
        try {
            SubjectSearchCriteriaBuilder subjectSearchCriteriaBuilder = new SubjectSearchCriteriaBuilder();
            SubjectSearchCriteria subjectSearchCriteria = subjectSearchCriteriaBuilder.buildSubjectSearchCriteria(request);
            SubjectIdentifierDomain communityAssigningAuthority = this.getCommunityAssigningAuthority();
            subjectSearchCriteria.setCommunityAssigningAuthority(communityAssigningAuthority);
            subjectSearchCriteria.addScopingAssigningAuthority(communityAssigningAuthority);
            this.setMinimumDegreeMatchPercentage(subjectSearchCriteria);
            return subjectSearchCriteria;
        } catch (ModelBuilderException ex) {
            throw new XCPDException(ex.getMessage());
        }
    }

    /**
     * Convert PID Feed (Add) to SubjectSearchCriteria.
     *
     * @param request PID Feed (Add).
     * @return SubjectSearchCriteria
     * @throws XCPDException
     */
    private SubjectSearchCriteria getSubjectSearchCriteria(PRPA_IN201301UV02_Message request) throws XCPDException {
        try {
            SubjectBuilder builder = new SubjectBuilder();
            Subject searchSubject = builder.buildSubject(request);
            SubjectSearchCriteria subjectSearchCriteria = new SubjectSearchCriteria();
            subjectSearchCriteria.setSubject(searchSubject);
            SubjectIdentifierDomain communityAssigningAuthority = this.getCommunityAssigningAuthority();
            subjectSearchCriteria.setCommunityAssigningAuthority(communityAssigningAuthority);
            //subjectSearchCriteria.addScopingAssigningAuthority(communityAssigningAuthority);
            this.setMinimumDegreeMatchPercentage(subjectSearchCriteria);
            return subjectSearchCriteria;
        } catch (ModelBuilderException ex) {
            throw new XCPDException(ex.getMessage());
        }
    }

    /**
     *
     * @param subjectSearchCriteria
     * @throws XCPDException
     */
    private void validateSearchCriteriaRequest(SubjectSearchCriteria subjectSearchCriteria) throws XCPDException {
        SubjectIdentifierDomain communityAssigningAuthority = subjectSearchCriteria.getCommunityAssigningAuthority();

        // Validate request:
        Subject subject = subjectSearchCriteria.getSubject();

        // Make sure that at least one subject identifier is for the
        // designated "CommunityAssigningAuthority"
        boolean foundCommunityAssigningAuthority = false;
        for (SubjectIdentifier subjectIdentifier : subject.getSubjectIdentifiers()) {
            SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
            if (subjectIdentifierDomain.getUniversalId().equals(communityAssigningAuthority.getUniversalId())) {
                foundCommunityAssigningAuthority = true;
                break;
            }
        }

        if (foundCommunityAssigningAuthority == false) {
            // Did not find an appropriate patient on the request.
            throw new XCPDException(
                    "You must specify at least one LivingSubjectId for the " +
                    communityAssigningAuthority.getUniversalId() + " assigning authority");
        }

        // Check required fields (Subject + BirthTime).
        if (subject.getSubjectNames().isEmpty()) {
            throw new XCPDException("LivingSubjectName required");
        }

        if (subject.getBirthTime() == null) {
            throw new XCPDException("LivingSubjectBirthTime required");
        }
    }

    /**
     *
     * @param subjectSearchCriteria
     * @throws XCPDException
     */
    private void validateSearchCriteriaPIXRequest(SubjectSearchCriteria subjectSearchCriteria) throws XCPDException {
        SubjectIdentifierDomain communityAssigningAuthority = subjectSearchCriteria.getCommunityAssigningAuthority();

        // Validate request:
        Subject subject = subjectSearchCriteria.getSubject();

        // Make sure we have an identifier.
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        if (subjectIdentifiers == null) {
            throw new XCPDException(
                    "You must specify one LivingSubjectId for the " +
                    communityAssigningAuthority.getUniversalId() + " assigning authority");
        }

        // Make sure we only have one identifier.
        if (subjectIdentifiers.size() > 1) {
            throw new XCPDException(
                    "You must specify only one LivingSubjectId for the " +
                    communityAssigningAuthority.getUniversalId() + " assigning authority");
        }

        // Make sure that the specified subject identifier is for the
        // designated "CommunityAssigningAuthority"
        SubjectIdentifier subjectIdentifier = subject.getSubjectIdentifiers().get(0);
        SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
        if (!subjectIdentifierDomain.getUniversalId().equals(communityAssigningAuthority.getUniversalId())) {
            // Did not find an appropriate identifier on the request.
            throw new XCPDException(
                    "You must specify one LivingSubjectId for the " +
                    communityAssigningAuthority.getUniversalId() + " assigning authority");
        }
    }

    /**
     * 
     * @param request
     * @param errorDetail
     * @return
     */
    private MCCI_IN000002UV01_Message getPatientIdentityFeedResponse(HL7V3Message request, HL7V3ErrorDetail errorDetail) {
        DeviceInfo senderDeviceInfo = this.getSenderDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        MCCI_IN000002UV01_Message_Builder ackBuilder = new MCCI_IN000002UV01_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        MCCI_IN000002UV01_Message ackResponse = ackBuilder.buildMCCI_IN000002UV01(request, errorDetail);
        return ackResponse;
    }

    /**
     * 
     * @param request
     * @param subjectSearchResponse
     * @param errorDetail
     * @return
     */
    private PRPA_IN201306UV02_Message getPatientRegistryFindCandidatesQueryResponse(PRPA_IN201305UV02_Message request,
            SubjectSearchResponse subjectSearchResponse, HL7V3ErrorDetail errorDetail) {
        DeviceInfo senderDeviceInfo = this.getSenderDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        PRPA_IN201306UV02_Message_Builder builder =
                new PRPA_IN201306UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        return builder.buildPRPA_IN201306UV02_Message(request, subjectSearchResponse, errorDetail);
    }

    /**
     * 
     * @param request
     * @param subjectSearchResponse
     * @param errorDetail
     * @return
     */
    private PRPA_IN201310UV02_Message getCrossGatewayPatientDiscoveryResponse(PRPA_IN201309UV02_Message request,
            SubjectSearchResponse subjectSearchResponse, HL7V3ErrorDetail errorDetail) {
        DeviceInfo senderDeviceInfo = this.getSenderDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        PRPA_IN201310UV02_Message_Builder builder = new PRPA_IN201310UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        return builder.buildPRPA_IN201310UV02_Message(request, subjectSearchResponse, errorDetail);
    }

    /**
     *
     * @return
     * @throws SOAPFaultException
     */
    @Override
    protected synchronized XConfigActor getPDSConfig() throws SOAPFaultException {
        if (_pdsConfig != null) {
            return _pdsConfig;
        }
        XConfigObject gatewayConfig = this.getGatewayConfig();

        // Now get the "PDS" object (and cache it away).
        _pdsConfig = (XConfigActor) gatewayConfig.getXConfigObjectWithName("pds", XConfig.PDS_TYPE);
        return _pdsConfig;
    }
}
