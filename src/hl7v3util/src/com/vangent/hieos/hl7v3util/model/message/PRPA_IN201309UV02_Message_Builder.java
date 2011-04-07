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
package com.vangent.hieos.hl7v3util.model.message;

import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class PRPA_IN201309UV02_Message_Builder extends HL7V3MessageBuilderHelper {

    /**
     * 
     */
    private PRPA_IN201309UV02_Message_Builder() {
    }

    /**
     *
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     */
    public PRPA_IN201309UV02_Message_Builder(DeviceInfo senderDeviceInfo, DeviceInfo receiverDeviceInfo) {
        super(senderDeviceInfo, receiverDeviceInfo);
    }

    /**
     * 
     * @param subjectSearchCriteria
     * @param sendCommunityPatientId
     * @return
     */
    public PRPA_IN201309UV02_Message buildPRPA_IN201309UV02_Message(
            SubjectSearchCriteria subjectSearchCriteria) {

        String messageName = "PRPA_IN201309UV02";

        // PRPA_IN201309UV02
        OMElement rootNode = this.createOMElement(messageName);
        this.setAttribute(rootNode, "ITSVersion", "XML_1.0");
        this.addMessageId(rootNode);

        // <id root="1.2.840.114350.1.13.0.1.7.1.1" extension="35423"/>
        // <creationTime value="20090417150301"/>
        // <interactionId root="2.16.840.1.113883.1.6" extension="PRPA_IN201309UV02"/>
        // <processingCode code="T"/>
        // <processingModeCode code="I"/>
        // <acceptAckCode code="NE"/>
        this.addCreationTime(rootNode);
        this.addInteractionId(messageName, rootNode);
        this.addCode(rootNode, "processingCode", "T");
        this.addCode(rootNode, "processingModeCode", "I");
        this.addCode(rootNode, "acceptAckCode", "NE");

        // PRPA_IN201309UV02/receiver
        // PRPA_IN201309UV02/sender
        this.addReceiver(rootNode);
        this.addSender(rootNode);

        // PRPA_IN201309UV02/controlActProcess
        OMElement controlActProcessNode = this.addChildOMElement(rootNode, "controlActProcess");
        this.setAttribute(controlActProcessNode, "moodCode", "EVN");
        this.setAttribute(controlActProcessNode, "classCode", "CACT");

        // PRPA_IN201309UV02/controlActProcess/code
        OMElement codeNode = this.addCode(controlActProcessNode, "code", "PRPA_TE201309UV02");
        this.setAttribute(codeNode, "codeSystem", "2.16.840.1.113883.1.6");

        // PRPA_IN201309UV02/controlActProcess/authorOrPerformer
        // <authorOrPerformer typeCode="AUT">
        //       <assignedDevice classCode="ASSIGNED">
        //          <id root="1.2.840.114350.1.13.99997.2.3412"/>
        //       </assignedDevice>
        // </authorOrPerformer>
        if (subjectSearchCriteria.getCommunityAssigningAuthority() != null) {
            OMElement authorOrPerformerNode = this.addChildOMElement(controlActProcessNode, "authorOrPerformer");
            this.setAttribute(authorOrPerformerNode, "typeCode", "AUT");
            OMElement assignedDeviceNode = this.addChildOMElement(authorOrPerformerNode, "assignedDevice");
            this.setAttribute(assignedDeviceNode, "classCode", "ASSIGNED");
            OMElement idNode = this.addChildOMElement(assignedDeviceNode, "id");
            SubjectIdentifierDomain identifierDomain = subjectSearchCriteria.getCommunityAssigningAuthority();
            this.setAttribute(idNode, "root", identifierDomain.getUniversalId());
        }

        // PRPA_IN201309UV02/controlActProcess/queryAck
        //this.addQueryAck(requestNode, controlActProcessNode, subjects, errorText);

        // PRPA_IN201309UV02/controlActProcess/queryByParameter
        OMElement queryByParameterNode =
                this.addQueryByParameter(controlActProcessNode,
                subjectSearchCriteria);

        return new PRPA_IN201309UV02_Message(rootNode);
    }

    /**
     * 
     * @param controlActProcessNode
     * @param subjectSearchCriteria
     * @return
     */
    private OMElement addQueryByParameter(
            OMElement controlActProcessNode,
            SubjectSearchCriteria subjectSearchCriteria) {

        // PRPA_IN201309UV02/controlActProcess/queryByParameter
        OMElement queryByParameterNode = this.addChildOMElement(controlActProcessNode, "queryByParameter");

        // PRPA_IN201309UV02/controlActProcess/queryByParameter/queryId
        OMElement queryIdNode = this.addChildOMElement(queryByParameterNode, "queryId");
        this.setAttribute(queryIdNode, "root", "1.2.840.114350.1.13.99999.4567.34");  // FIXME: ???
        this.setAttribute(queryIdNode, "extension", "33452");  // FIXME: ???

        // PRPA_IN201309UV02/controlActProcess/queryByParameter/statusCode
        this.addCode(queryByParameterNode, "statusCode", "new");

        // PRPA_IN201309UV02/controlActProcess/queryByParameter/responseModalityCode
        //this.addCode(queryByParameterNode, "responseModalityCode", "R");

        // PRPA_IN201309UV02/controlActProcess/queryByParameter/responsePriorityCode
        this.addCode(queryByParameterNode, "responsePriorityCode", "I");

        // PRPA_IN201309UV02/controlActProcess/queryByParameter/matchCriterionList
        //this.addMatchCriterionList(queryByParameterNode, subjectSearchCriteria);

        // PRPA_IN201309UV02/controlActProcess/queryByParameter/parameterList
        this.addParameterList(queryByParameterNode, subjectSearchCriteria);

        return queryByParameterNode;
    }

    /**
     *
     * @param rootNode
     * @param subjectSearchCriteria
     * @return
     */
    private OMElement addParameterList(
            OMElement rootNode,
            SubjectSearchCriteria subjectSearchCriteria) {
        Subject searchSubject = subjectSearchCriteria.getSubject();

        // PRPA_IN201309UV02/controlActProcess/queryByParameter/parameterList
        OMElement parameterListNode = this.addChildOMElement(rootNode, "parameterList");

        // Now add parameters.

        // PRPA_IN201309UV02/controlActProcess/queryByParameter/parameterList/dataSource
        // <dataSource>
        //    <value root="1.2.840.114350.1.13.99997.2.3412"/>
        //    <semanticsText>DataSource.id</semanticsText>
        // </dataSource>
        for (SubjectIdentifierDomain subjectIdentifierDomain : subjectSearchCriteria.getScopingAssigningAuthorities()) {
            String assigningAuthority = subjectIdentifierDomain.getUniversalId();
            OMElement dataSourceNode = this.addChildOMElement(parameterListNode, "dataSource");
            OMElement valueNode = this.addChildOMElement(dataSourceNode, "value");
            this.setAttribute(valueNode, "root", assigningAuthority);
            this.addChildOMElementWithValue(dataSourceNode, "semanticsText", "DataSource.id");
        }

        // PRPA_IN201309UV02/controlActProcess/queryByParameter/parameterList/patientIdentifier
        // <patientIdentifier>
        //    <value root="1.3.6.1.4.1.21367.13.20.1000" extension="TESTME1000_9"/>
        //    <semanticsText>Patient.Id</semanticsText>
        // </patientIdentifier>
        for (SubjectIdentifier subjectIdentifier : searchSubject.getSubjectIdentifiers()) {
            OMElement patientIdentifierNode = this.addChildOMElement(parameterListNode, "patientIdentifier");
            OMElement valueNode = this.addChildOMElement(patientIdentifierNode, "value");
            this.addSubjectIdentifier(valueNode, subjectIdentifier);
            this.addChildOMElementWithValue(patientIdentifierNode, "semanticsText", "Patient.id");
        }

        return parameterListNode;
    }
}
