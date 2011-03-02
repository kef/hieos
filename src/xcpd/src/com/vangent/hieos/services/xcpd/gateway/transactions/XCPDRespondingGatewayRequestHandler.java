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
package com.vangent.hieos.services.xcpd.gateway.transactions;

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
import com.vangent.hieos.services.xcpd.gateway.exception.XCPDException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCPDRespondingGatewayRequestHandler extends XCPDGatewayRequestHandler {

    private final static Logger logger = Logger.getLogger(XCPDRespondingGatewayRequestHandler.class);
    private static XConfigActor _pdsConfig = null;

    /**
     * 
     * @param log_message
     * @param gatewayType
     */
    public XCPDRespondingGatewayRequestHandler(XLogMessage log_message, XCPDGatewayRequestHandler.GatewayType gatewayType) {
        super(log_message, gatewayType);
    }

    /**
     *
     * @param request
     * @param messageType
     * @return
     */
    public OMElement run(OMElement request, MessageType messageType) throws AxisFault {
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
    private PRPA_IN201306UV02_Message processCrossGatewayPatientDiscovery(PRPA_IN201305UV02_Message request) throws AxisFault {
        String errorText = null;

        // Validate request against XML schema.
        this.validateHL7V3Message(request);

        PRPA_IN201306UV02_Message queryResponse = null;
        try {
            // First convert the request.
            SubjectSearchCriteriaBuilder criteriaBuilder = new SubjectSearchCriteriaBuilder();
            SubjectSearchCriteria subjectSearchCriteria =
                    criteriaBuilder.buildSubjectSearchCriteria(request);

            this.performATNAAudit(request, subjectSearchCriteria, null /* endpoint */);

            // Validate against XCPD rules.
            this.validateRequest(subjectSearchCriteria);

            // Now, make PDQ query to MPI.
            DeviceInfo senderDeviceInfo = this.getDeviceInfo();

            // To be safe, strip any identifiers that may be in the search request ...
            Subject searchSubject = subjectSearchCriteria.getSubject();
            if (searchSubject != null) {
                searchSubject.setSubjectIdentifiers(new ArrayList<SubjectIdentifier>());
            }

            // Make sure that only matches for the CommunityAssigningAuthority are returned.
            SubjectIdentifierDomain identifierDomain = this.getCommunityAssigningAuthority();
            subjectSearchCriteria.setCommunityAssigningAuthority(identifierDomain);
            subjectSearchCriteria.addScopingAssigningAuthority(identifierDomain);

            // Issue PDQ to PDS.
            SubjectSearchResponse pdqSubjectSearchResponse = this.findCandidatesQuery(senderDeviceInfo, subjectSearchCriteria);

            // Go through all subjects and add custodian
            List<Subject> subjects = pdqSubjectSearchResponse.getSubjects();
            for (Subject subject : subjects) {
                Custodian custodian = new Custodian();
                String homeCommunityId = this.getGatewayConfig().getUniqueId();
                homeCommunityId = homeCommunityId.replace("urn:oid:", "");
                custodian.setCustodianId(homeCommunityId);
                custodian.setSupportsHealthDataLocator(false);
                subject.setCustodian(custodian);
            }

            // Now prepare the XCPD response.
            queryResponse = this.getCrossGatewayPatientDiscoveryResponse(request, pdqSubjectSearchResponse, null);
        } catch (Exception ex) {
            errorText = ex.getMessage();
            queryResponse = this.getCrossGatewayPatientDiscoveryResponse(
                    request, null /* subjects */, ex.getMessage());
        }
        this.log(errorText);
        this.validateHL7V3Message(queryResponse);
        return queryResponse;

    }

    /**
     *
     * @param subjectSearchCriteria
     * @throws XCPDException
     */
    private void validateRequest(SubjectSearchCriteria subjectSearchCriteria) throws XCPDException {
        // Validate XCPD rules:
        Subject subject = subjectSearchCriteria.getSubject();

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
        DeviceInfo senderDeviceInfo = this.getDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        PRPA_IN201306UV02_Message_Builder builder = new PRPA_IN201306UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        return builder.buildPRPA_IN201306UV02_Message(request, subjectSearchResponse, errorText);
    }

    /**
     *
     * @return
     * @throws AxisFault
     */
    @Override
    protected synchronized XConfigActor getPDSConfig() throws AxisFault {
        if (_pdsConfig != null) {
            return _pdsConfig;
        }
        XConfigObject gatewayConfig = this.getGatewayConfig();

        // Now get the "PDS" object (and cache it away).
        _pdsConfig = (XConfigActor) gatewayConfig.getXConfigObjectWithName("pds", XConfig.PDS_TYPE);
        return _pdsConfig;
    }
}
