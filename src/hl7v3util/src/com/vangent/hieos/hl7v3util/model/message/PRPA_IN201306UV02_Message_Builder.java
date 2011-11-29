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
package com.vangent.hieos.hl7v3util.model.message;

import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class PRPA_IN201306UV02_Message_Builder extends HL7V3MessageBuilderHelper {

    /**
     *
     */
    private PRPA_IN201306UV02_Message_Builder() {
    }

    /**
     *
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     */
    public PRPA_IN201306UV02_Message_Builder(DeviceInfo senderDeviceInfo, DeviceInfo receiverDeviceInfo) {
        super(senderDeviceInfo, receiverDeviceInfo);
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message The request.
     * @param subjectSearchResponse [may be null]
     * @param errorDetail [may be null]
     * @return PRPA_IN201306UV02_Message
     */
    public PRPA_IN201306UV02_Message buildPRPA_IN201306UV02_Message(
            PRPA_IN201305UV02_Message request,
            SubjectSearchResponse subjectSearchResponse,
            HL7V3ErrorDetail errorDetail) {
        OMElement requestNode = request.getMessageNode();
        String messageName = "PRPA_IN201306UV02";

        // PRPA_IN201306UV02
        OMElement responseNode = this.getResponseNode(messageName, "P", "T", "NE");

        // PRPA_IN201306UV02/acknowledgement
        this.addAcknowledgementToRequest(requestNode, responseNode, errorDetail, "AA", "AE");

        // PRPA_IN201306UV02/controlActProcess
        OMElement controlActProcessNode = this.addControlActProcess(responseNode, "PRPA_TE201306UV02");

        // PRPA_IN201306UV02/controlActProcess/subject
        List<Subject> subjects = subjectSearchResponse != null ? subjectSearchResponse.getSubjects() : null;
        this.addSubjects(controlActProcessNode, subjects);

        // PRPA_IN201306UV02/controlActProcess/queryAck
        this.addQueryAckToRequest(requestNode, controlActProcessNode, subjects, errorDetail);

        // PRPA_IN201306UV02/controlActProcess/queryByParameter
        this.addQueryByParameterFromRequest(requestNode, controlActProcessNode);

        return new PRPA_IN201306UV02_Message(responseNode);
    }

    /**
     *
     * @param controlActProcessNode
     * @param subjects
     */
    private void addSubjects(OMElement controlActProcessNode, List<Subject> subjects) {
        if (subjects != null) {
            for (Subject subject : subjects) {
                this.addSubject(controlActProcessNode, subject);
            }
        }
    }

    /**
     *
     * @param controlActProcessNode
     * @param subject
     */
    private void addSubject(OMElement controlActProcessNode, Subject subject) {
        // controlActProcess/subject/registrationEvent/subject1/patient
        OMElement patientNode = this.addPatientNode(controlActProcessNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/id[*]
        this.addSubjectIdentifiers(patientNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/statusCode
        OMElement statusCodeNode = this.addChildOMElement(patientNode, "statusCode");
        this.setAttribute(statusCodeNode, "code", "active");

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson
        OMElement patientPersonNode = this.addPatientPersonNode(patientNode);

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/name[*]
        this.addSubjectNames(patientPersonNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/telecom[*]
        this.addTelecomAddresses(patientPersonNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/administrativeGenderCode
        this.addCode(patientPersonNode, "administrativeGenderCode", subject.getGender());

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/birthTime
        this.addChildNodeWithDateValueAttribute(patientPersonNode, "birthTime", subject.getBirthTime());
        //OMElement birthTimeNode = this.addChildOMElement(patientPersonNode, "birthTime");
        //this.setAttribute(birthTimeNode, "value", Hl7Date.toHL7format(subject.getBirthTime()));

        // Deceased indicator.
        this.addChildNodeWithBooleanValueAttribute(patientPersonNode, "deceasedInd", subject.getDeceasedIndicator());

        // Deceased time.
        this.addChildNodeWithDateValueAttribute(patientPersonNode, "deceasedTime", subject.getDeceasedTime());

        // Multi-birth indicator.
        this.addChildNodeWithBooleanValueAttribute(patientPersonNode, "multipleBirthInd", subject.getMultipleBirthIndicator());

        // Multi-birth order number.
        this.addChildNodeWithIntegerValueAttribute(patientPersonNode, "multipleBirthOrderNumber", subject.getMultipleBirthOrderNumber());

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/addr[*]
        this.addAddresses(patientPersonNode, subject);

        // Add other coded values ...
        this.addCode(patientPersonNode, "maritalStatusCode", subject.getMaritalStatus());
        this.addCode(patientPersonNode, "religiousAffiliationCode", subject.getReligiousAffiliation());
        this.addCode(patientPersonNode, "raceCode", subject.getRace());
        this.addCode(patientPersonNode, "ethnicGroupCode", subject.getEthnicGroup());

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/asOtherIds[*]
        this.addSubjectOtherIdentifiers(patientPersonNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/providerOrganization
        this.addProviderOrganization(patientNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/subjectOf1
        this.addSubjectOf1(patientNode, subject);
    }
}
