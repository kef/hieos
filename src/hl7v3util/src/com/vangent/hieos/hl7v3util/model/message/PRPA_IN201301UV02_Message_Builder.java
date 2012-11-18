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

import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import org.apache.axiom.om.OMElement;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-29
 * @author         Jim Horner
 */
public class PRPA_IN201301UV02_Message_Builder
        extends HL7V3MessageBuilderHelper {

    /**
     *
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     */
    public PRPA_IN201301UV02_Message_Builder(DeviceInfo senderDeviceInfo,
            DeviceInfo receiverDeviceInfo) {
        super(senderDeviceInfo, receiverDeviceInfo);
    }

    /**
     * Method description
     *
     *
     * @param rootNode
     *
     * @return
     */
    protected OMElement addControlActProcessNode(OMElement rootNode) {

        // PRPA_IN201301UV02/controlActProcess
        OMElement result = addChildOMElement(rootNode, "controlActProcess");

        setAttribute(result, "moodCode", "EVN");
        setAttribute(result, "classCode", "CACT");

        return result;
    }

    /**
     * Method description
     *
     *
     * @param rootNode
     *
     * @return
     */
    protected OMElement addPatient(OMElement rootNode) {

        // PRPA_IN201301UV02/controlActProcess/subject/registrationEvent/patient
        OMElement result = addChildOMElement(rootNode, "patient");

        setAttribute(result, "classCode", "PAT");

        return result;
    }

    /**
     * Method description
     *
     *
     * @param rootNode
     *
     * @return
     */
    protected OMElement addRegistrationEventNode(OMElement rootNode) {

        // PRPA_IN201301UV02/controlActProcess/subject/registrationEvent
        OMElement result = addChildOMElement(rootNode, "registrationEvent");

        setAttribute(result, "classCode", "REG");
        setAttribute(result, "moodCode", "EVN");

        return result;
    }

    /**
     * Method description
     *
     *
     * @param rootNode
     *
     * @return
     */
    protected OMElement addSubject(OMElement rootNode) {

        // PRPA_IN201301UV02/controlActProcess/subject
        OMElement result = addChildOMElement(rootNode, "subject");

        setAttribute(result, "typeCode", "SUBJ");

        return result;
    }

    /**
     * Method description
     *
     *
     * @param rootNode
     *
     * @return
     */
    protected OMElement addSubject1(OMElement rootNode) {

        // PRPA_IN201301UV02/controlActProcess/subject/registrationEvent/subject1
        OMElement result = addChildOMElement(rootNode, "subject1");

        setAttribute(result, "typeCode", "SBJ");

        return result;
    }

    /**
     *
     *
     * @param subjectIdentifier
     * @return
     */
    public PRPA_IN201301UV02_Message buildPRPA_IN201301UV02_Message(
            SubjectIdentifier subjectIdentifier) {

        String messageName = "PRPA_IN201301UV02";

        // PRPA_IN201301UV02
        OMElement rootNode = createOMElement(messageName);

        setAttribute(rootNode, "ITSVersion", "XML_1.0");
        addMessageId(rootNode);

        // <id root="1.2.840.114350.1.13.0.1.7.1.1" extension="35423"/>
        // <creationTime value="20090417150301"/>
        // <interactionId root="2.16.840.1.113883.1.6" extension="PRPA_IN201301UV02"/>
        // <processingCode code="T"/>
        // <processingModeCode code="I"/>
        // <acceptAckCode code="NE"/>
        addCreationTime(rootNode);
        addInteractionId(messageName, rootNode);
        addCode(rootNode, "processingCode", "P");
        addCode(rootNode, "processingModeCode", "R");
        addCode(rootNode, "acceptAckCode", "AL");

        // PRPA_IN201301UV02/receiver
        // PRPA_IN201301UV02/sender
        addReceiver(rootNode);
        addSender(rootNode);

        // PRPA_IN201301UV02/controlActProcess
        OMElement controlActProcessNode = addControlActProcessNode(rootNode);

        // PRPA_IN201301UV02/controlActProcess/subject
        OMElement subjectNode = addSubject(controlActProcessNode);

        // PRPA_IN201301UV02/controlActProcess/subject/registrationEvent
        OMElement regEventNode = addRegistrationEventNode(subjectNode);

        // PRPA_IN201301UV02/controlActProcess/subject/registrationEvent/subject1
        OMElement subject1Node = addSubject1(regEventNode);

        // PRPA_IN201301UV02/controlActProcess/subject/registrationEvent/patient
        OMElement patientNode = addPatient(subject1Node);

        Subject subject = new Subject();

        subject.addSubjectIdentifier(subjectIdentifier);
        addSubjectIdentifiers(patientNode, subject);

        return new PRPA_IN201301UV02_Message(rootNode);
    }
}
