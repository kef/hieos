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
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
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
     * @param errorText [may be null]
     * @return PRPA_IN201306UV02_Message
     */
    public PRPA_IN201306UV02_Message buildPRPA_IN201306UV02_Message(
            PRPA_IN201305UV02_Message request,
            SubjectSearchResponse subjectSearchResponse,
            String errorText) {
        OMElement requestNode = request.getMessageNode();
        String messageName = "PRPA_IN201306UV02";

        // PRPA_IN201306UV02
        OMElement responseNode = this.createOMElement(messageName);
        this.setAttribute(responseNode, "ITSVersion", "XML_1.0");
        this.addMessageId(responseNode);

        //
        // EXAMPLE:
        //
        // <creationTime value="20101213081923"/>
        // <interactionId extension="PRPA_IN201306UV02" root="2.16.840.1.113883.1.6"/>
        // <processingCode code="T"/>
        // <processingModeCode code="I"/>
        // <acceptAckCode code="NE"/>
        //
        this.addCreationTime(responseNode);
        this.addInteractionId(messageName, responseNode);
        this.addCode(responseNode, "processingCode", "T");
        this.addCode(responseNode, "processingModeCode", "I");
        this.addCode(responseNode, "acceptAckCode", "NE");

        // PRPA_IN201306UV02/receiver
        // PRPA_IN201306UV02/sender
        this.addReceiver(responseNode);
        this.addSender(responseNode);

        // PRPA_IN201306UV02/acknowledgement
        this.addAcknowledgementToRequest(requestNode, responseNode, errorText);

        // PRPA_IN201306UV02/controlActProcess
        OMElement controlActProcessNode = this.addChildOMElement(responseNode, "controlActProcess");
        this.setAttribute(controlActProcessNode, "moodCode", "EVN");
        this.setAttribute(controlActProcessNode, "classCode", "CACT");

        // PRPA_IN201306UV02/controlActProcess/code
        OMElement codeNode = this.addCode(controlActProcessNode, "code", "PRPA_TE201306UV02");
        this.setAttribute(codeNode, "codeSystem", "2.16.840.1.113883.1.6");

        // PRPA_IN201306UV02/controlActProcess/subject
        List<Subject> subjects =
                subjectSearchResponse != null ? subjectSearchResponse.getSubjects() : null;
        this.addSubjects(controlActProcessNode, subjects);

        // PRPA_IN201306UV02/controlActProcess/queryAck
        this.addQueryAckToRequest(requestNode, controlActProcessNode, subjects, errorText);

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
        // controlActProcess/subject
        OMElement subjectNode = this.addChildOMElement(controlActProcessNode, "subject");
        this.setAttribute(subjectNode, "typeCode", "SUBJ");

        // controlActProcess/subject/registrationEvent
        OMElement registrationEventNode = this.addChildOMElement(subjectNode, "registrationEvent");
        this.setAttribute(registrationEventNode, "moodCode", "EVN");
        this.setAttribute(registrationEventNode, "classCode", "REG");

        // controlActProcess/subject/registrationEvent/id
        OMElement registrationEventIdNode = this.addChildOMElement(registrationEventNode, "id");
        this.setAttribute(registrationEventIdNode, "nullFlavor", "NA");

        // controlActProcess/subject/registrationEvent/statusCode
        OMElement statusCodeNode = this.addChildOMElement(registrationEventNode, "statusCode");
        this.setAttribute(statusCodeNode, "code", "active");

        // controlActProcess/subject/registrationEvent/subject1
        OMElement subject1Node = this.addChildOMElement(registrationEventNode, "subject1");
        this.setAttribute(subject1Node, "typeCode", "SBJ");

        // controlActProcess/subject/registrationEvent/subject1/patient
        OMElement patientNode = this.addChildOMElement(subject1Node, "patient");
        this.setAttribute(patientNode, "classCode", "PAT");

        // controlActProcess/subject/registrationEvent/subject1/patient/id[*]
        this.addSubjectIdentifiers(patientNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/statusCode
        statusCodeNode = this.addChildOMElement(patientNode, "statusCode");
        this.setAttribute(statusCodeNode, "code", "active");

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson
        OMElement patientPersonNode = this.addChildOMElement(patientNode, "patientPerson");
        this.setAttribute(patientPersonNode, "classCode", "PSN");
        this.setAttribute(patientPersonNode, "determinerCode", "INSTANCE");

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/name[*]
        this.addSubjectNames(patientPersonNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/administrativeGenderCode
        this.addCode(patientPersonNode, "administrativeGenderCode", subject.getGender().getCode());

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/birthTime
        OMElement birthTimeNode = this.addChildOMElement(patientPersonNode, "birthTime");
        this.setAttribute(birthTimeNode, "value", Hl7Date.toHL7format(subject.getBirthTime()));

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/addr[*]
        this.addAddresses(patientPersonNode, subject);

         // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/asOtherIds[*]
        this.addSubjectOtherIdentifiers(patientPersonNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/providerOrganization
        this.addProviderOrganization(patientNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/subjectOf1
        this.addSubjectOf1(patientNode, subject);

        // controlActProcess/subject/registrationEvent/custodian
        this.addCustodian(registrationEventNode, subject);
    }
}
