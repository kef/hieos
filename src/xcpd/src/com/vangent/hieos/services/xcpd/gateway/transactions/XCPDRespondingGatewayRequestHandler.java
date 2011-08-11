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

import com.vangent.hieos.services.xcpd.gateway.framework.XCPDGatewayRequestHandler;
import com.vangent.hieos.services.xcpd.gateway.exception.XCPDException;

import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.message.HL7V3MessageBuilderHelper;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteriaBuilder;
import com.vangent.hieos.hl7v3util.model.subject.Custodian;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.xutil.exception.SOAPFaultException;

import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 * Class to handle all requests to XCPD Responding Gateway (RG).
 *
 * @author Bernie Thuman
 */
public class XCPDRespondingGatewayRequestHandler extends XCPDGatewayRequestHandler {

    // Type type of message received.
    public enum MessageType {
        CrossGatewayPatientDiscovery,
        PatientLocationQuery
     };
    private final static Logger logger = Logger.getLogger(XCPDRespondingGatewayRequestHandler.class);
    private static XConfigActor _pdsConfig = null;

    /**
     * Constructor for handling requests to XCPD Responding Gateway.
     *
     * @param log_message  Place to put internal log messages.
     */
    public XCPDRespondingGatewayRequestHandler(XLogMessage log_message) {
        super(log_message, XCPDInitiatingGatewayRequestHandler.GatewayType.RespondingGateway);
    }

    /**
     *
     * @param request
     * @param messageType
     * @return
     */
    public OMElement run(OMElement request, MessageType messageType) throws SOAPFaultException {
        HL7V3Message result = null;
        log_message.setPass(true);  // Hope for the best.
        switch (messageType) {
            case CrossGatewayPatientDiscovery:
                result = this.processCrossGatewayPatientDiscovery(new PRPA_IN201305UV02_Message(request));
                break;
            case PatientLocationQuery:
                //result = this.processPatientLocationQuery(request);
                break;
        }
        if (result != null) {
            log_message.addOtherParam("Response", result.getMessageNode());
        }
        return (result != null) ? result.getMessageNode() : null;
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return
     */
    private PRPA_IN201306UV02_Message processCrossGatewayPatientDiscovery(PRPA_IN201305UV02_Message request) throws SOAPFaultException {
        String errorText = null;
        SubjectSearchResponse patientDiscoverySearchResponse = null;

        // Validate request against XML schema.
        this.validateHL7V3Message(request);

        PRPA_IN201306UV02_Message result = null;
        try {
            // First convert the request.
            SubjectSearchCriteriaBuilder criteriaBuilder = new SubjectSearchCriteriaBuilder();
            SubjectSearchCriteria subjectSearchCriteria =
                    criteriaBuilder.buildSubjectSearchCriteria(request);

            this.performATNAAudit(request, subjectSearchCriteria, null /* endpoint */);

            // Validate against XCPD rules.
            this.validateRequest(subjectSearchCriteria);

            // Now, make PDQ query to MPI.
            DeviceInfo senderDeviceInfo = this.getSenderDeviceInfo();

            // To be safe, strip any identifiers that may be in the search request ...
            Subject searchSubject = subjectSearchCriteria.getSubject();
            if (searchSubject != null) {
                searchSubject.setSubjectIdentifiers(new ArrayList<SubjectIdentifier>());
            }

            // Again to be safe, remove any existing scoping assigning authorities.
            subjectSearchCriteria.setScopingAssigningAuthorities(new ArrayList<SubjectIdentifierDomain>());

            // Make sure that only matches for the CommunityAssigningAuthority are returned.
            SubjectIdentifierDomain identifierDomain = this.getCommunityAssigningAuthority();
            subjectSearchCriteria.setCommunityAssigningAuthority(identifierDomain);
            subjectSearchCriteria.addScopingAssigningAuthority(identifierDomain);

            // Issue PDQ to PDS.
            patientDiscoverySearchResponse = this.findCandidatesQuery(senderDeviceInfo, subjectSearchCriteria);

            // Go through all subjects and add custodian (the home community id for this gateway).
            List<Subject> subjects = patientDiscoverySearchResponse.getSubjects();
            for (Subject subject : subjects) {
                Custodian custodian = new Custodian();
                String homeCommunityId = this.getGatewayConfig().getUniqueId();
                homeCommunityId = homeCommunityId.replace("urn:oid:", "");
                custodian.setCustodianId(homeCommunityId);
                custodian.setSupportsHealthDataLocator(false);
                subject.setCustodian(custodian);
            }
        } catch (Exception ex) {
            errorText = ex.getMessage();
        }
        // Now prepare the XCPD response.
        result = this.getCrossGatewayPatientDiscoveryResponse(request, patientDiscoverySearchResponse, errorText);
        this.log(errorText);
        this.validateHL7V3Message(result);
        return result;

    }

    /**
     *
     * @param subjectSearchCriteria
     * @throws XCPDException
     */
    private void validateRequest(SubjectSearchCriteria subjectSearchCriteria) throws XCPDException {
        // Validate XCPD rules:
        Subject subject = subjectSearchCriteria.getSubject();

        // FIXME: ???
        // First see if any identifiers exist.
        if (subject.getSubjectIdentifiers().size() > 0) {
            return;  // All else are optional.
        }

        // Check required fields (Subject + BirthTime).
        if (subject.getSubjectNames().size() == 0) {
            throw new XCPDException("LivingSubjectName required");
        }

        if (subject.getBirthTime() == null) {
            throw new XCPDException("LivingSubjectBirthTime required");
        }
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @param subjectSearchResponse
     * @param errorText
     * @return
     */
    private PRPA_IN201306UV02_Message getCrossGatewayPatientDiscoveryResponse(PRPA_IN201305UV02_Message request,
            SubjectSearchResponse subjectSearchResponse, String errorText) {
        DeviceInfo senderDeviceInfo = this.getSenderDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        PRPA_IN201306UV02_Message_Builder builder = new PRPA_IN201306UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        return builder.buildPRPA_IN201306UV02_Message(request, subjectSearchResponse, errorText);
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
