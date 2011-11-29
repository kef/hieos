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

import com.vangent.hieos.hl7v3util.model.builder.*;
import com.vangent.hieos.hl7v3util.model.subject.Address;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.hl7v3util.model.subject.Custodian;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectName;
import com.vangent.hieos.hl7v3util.model.subject.TelecomAddress;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import java.util.List;
import java.util.UUID;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

/**
 *
 * @author Bernie Thuman
 */
public class HL7V3MessageBuilderHelper extends BuilderHelper {

    private DeviceInfo senderDeviceInfo = null;
    private DeviceInfo receiverDeviceInfo = null;

    /**
     *
     * @param message
     * @return
     */
    static public DeviceInfo getSenderDeviceInfo(HL7V3Message message) {
        HL7V3MessageBuilderHelper builder = new HL7V3MessageBuilderHelper();
        return builder.getDeviceInfo(message.getMessageNode(), "./ns:sender/ns:device[1]");
    }

    /**
     * 
     * @param message
     * @return
     */
    static public DeviceInfo getReceiverDeviceInfo(HL7V3Message message) {
        HL7V3MessageBuilderHelper builder = new HL7V3MessageBuilderHelper();
        return builder.getDeviceInfo(message.getMessageNode(), "./ns:receiver/ns:device[1]");
    }

    /**
     * 
     */
    public HL7V3MessageBuilderHelper() {
        super();
    }

    /**
     *
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     */
    public HL7V3MessageBuilderHelper(DeviceInfo senderDeviceInfo, DeviceInfo receiverDeviceInfo) {
        super();
        this.senderDeviceInfo = senderDeviceInfo;
        this.receiverDeviceInfo = receiverDeviceInfo;
    }

    /**
     *
     * @return
     */
    public DeviceInfo getSenderDeviceInfo() {
        return this.senderDeviceInfo;
    }

    /**
     *
     * @return
     */
    public DeviceInfo getReceiverDeviceInfo() {
        return this.receiverDeviceInfo;
    }

    /**
     *
     * @param requestNode
     * @param xpathExpression
     * @return
     */
    private DeviceInfo getDeviceInfo(OMElement rootNode, String xpathExpression) {
        OMElement deviceNode = null;
        try {
            deviceNode = this.selectSingleNode(rootNode, xpathExpression);
        } catch (XPathHelperException ex) {
            // TBD: Do something?
        }

        // Now get component parts.
        DeviceInfo deviceInfo = new DeviceInfo();
        if (deviceNode != null) {
            String id = this.getNodeAttributeValue(deviceNode, "./ns:id[1]", "root");
            String name = this.getNodeText(deviceNode, "./ns:name[1]");
            String telecom = this.getNodeAttributeValue(deviceNode, "./ns:telecom[1]", "value");
            String representedOrganizationId = this.getNodeAttributeValue(deviceNode,
                    "./ns:asAgent/ns:representedOrganization/ns:id[1]", "root");
            deviceInfo.setId(id);
            deviceInfo.setName(name);
            deviceInfo.setTelecom(telecom);
            deviceInfo.setRepresentedOrganizationId(representedOrganizationId);
        }
        return deviceInfo;
    }

    /**
     * 
     * @param rootNode
     * @param elementName
     * @param value
     * @return
     */
    protected OMElement addCode(OMElement rootNode, String elementName, String value) {
        OMElement childNode = this.createOMElement(elementName);
        rootNode.addChild(childNode);
        childNode.addAttribute("code", value, null);
        return childNode;
    }

    /**
     * 
     * @param rootNode
     * @param elementName
     * @param codedValue
     * @return
     */
    protected OMElement addCode(OMElement rootNode, String elementName, CodedValue codedValue) {
        OMElement childNode = null;
        if (codedValue != null) {
            childNode = this.addCode(rootNode, elementName, codedValue.getCode());
        }
        return childNode;
    }

    /**
     *
     * @param requestNode
     * @param elementName
     * @param value
     * @return
     */
    protected OMElement addValue(OMElement rootNode, String elementName, String value) {
        OMElement childNode = this.createOMElement(elementName);
        rootNode.addChild(childNode);
        childNode.addAttribute("value", value, null);
        return childNode;
    }

    /**
     *
     * @param requestNode
     * @return
     */
    protected OMElement addSender(OMElement rootNode) {
        // requestNode/sender
        OMElement senderNode = this.addChildOMElement(rootNode, "sender");
        senderNode.addAttribute("typeCode", "SND", null);
        this.addDevice(senderNode, this.getSenderDeviceInfo());
        return senderNode;
    }

    /**
     * 
     * @param requestNode
     * @return
     */
    protected OMElement addReceiver(OMElement rootNode) {
        OMElement receiverNode = this.addChildOMElement(rootNode, "receiver");
        receiverNode.addAttribute("typeCode", "RCV", null);
        this.addDevice(receiverNode, this.getReceiverDeviceInfo());
        return receiverNode;
    }

    /**
     *
     * @param requestNode
     * @param deviceInfo
     * @return
     */
    protected OMElement addDevice(OMElement rootNode, DeviceInfo deviceInfo) {
        // Add "device" for Sender:
        // requestNode/sender/device
        OMElement deviceNode = this.addChildOMElement(rootNode, "device");
        this.setAttribute(deviceNode, "classCode", "DEV");
        this.setAttribute(deviceNode, "determinerCode", "INSTANCE");

        // requestNode/sender/device/id
        if (deviceInfo.getId() != null) {
            OMElement idNode = this.addChildOMElement(deviceNode, "id");
            this.setAttribute(idNode, "root", deviceInfo.getId());
        }

        // requestNode/sender/device/name
        if (deviceInfo.getName() != null) {
            this.addChildOMElementWithValue(deviceNode, "name", deviceInfo.getName());
        }

        // requestNode/sender/device/telecom
        if (deviceInfo.getTelecom() != null) {
            OMElement telecomNode = this.addChildOMElement(deviceNode, "telecom");
            this.setAttribute(telecomNode, "value", deviceInfo.getTelecom());
        }

        // Now, see if there is a home community id.
        if (deviceInfo.getRepresentedOrganizationId() != null) {
            // TBD ....
            //<!--Used to carry the homeCommunityId-->
            //<asAgent classCode="AGNT">
            //  <representedOrganization classCode="ORG" determinerCode="INSTANCE">
            //    <!--homeCommunityId=urn:oid:1.2.3.928.955-->
            //    <id root="1.2.3.928.955"/>
            //  </representedOrganization>
            //</asAgent>
            OMElement asAgentNode = this.addChildOMElement(deviceNode, "asAgent");
            this.setAttribute(asAgentNode, "classCode", "AGNT");

            OMElement representedOrganizationNode = this.addChildOMElement(asAgentNode, "representedOrganization");
            this.setAttribute(representedOrganizationNode, "classCode", "ORG");
            this.setAttribute(representedOrganizationNode, "determinerCode", "INSTANCE");

            OMElement homeCommunityIdNode = this.addChildOMElement(representedOrganizationNode, "id");
            this.setAttribute(homeCommunityIdNode,
                    "root", this.getHomeCommunityIdWithStrippedPrefix(deviceInfo.getRepresentedOrganizationId()));
        }
        return deviceNode;
    }

    /**
     *
     * @param homeCommunityId
     * @return
     */
    protected String getHomeCommunityIdWithStrippedPrefix(String homeCommunityId) {
        return homeCommunityId.replace("urn:oid:", "");
    }

    /**
     *
     * @param messageName
     * @param requestNode
     */
    protected void addInteractionId(String messageName, OMElement rootNode) {
        OMElement childNode = this.createOMElement("interactionId");
        rootNode.addChild(childNode);
        childNode.addAttribute("extension", messageName, null);
        childNode.addAttribute("root", "2.16.840.1.113883.1.6", null);
    }

    /**
     *
     * @param requestNode
     */
    protected void addCreationTime(OMElement rootNode) {
        OMElement creationTimeNode = this.createOMElement("creationTime");
        rootNode.addChild(creationTimeNode);
        creationTimeNode.addAttribute("value", Hl7Date.now().toString(), null);
    }

    /**
     *
     * @param requestNode
     */
    protected void addMessageId(OMElement rootNode) {
        OMElement childNode = this.createOMElement("id");
        rootNode.addChild(childNode);
        // TBD: ??????
        childNode.addAttribute("root", UUID.randomUUID().toString(), null);
    }

    /**
     * 
     * @param requestNode
     * @param rootNode
     * @param errorDetail
     */
    protected void addAcknowledgementToRequest(
            OMElement requestNode,
            OMElement rootNode,
            HL7V3ErrorDetail errorDetail, String successCode, String errorCode) {
        //<acknowledgement>
        //    <typeCode code="CE"/>
        //    <targetMessage>
        //       <id root="22a0f9e0-4454-11dc-a6be-3603d6866807"/>
        //    </targetMessage>
        //    <acknowledgementDetail>
        //       <text>EMPI EXCEPTION: when adding new Subject: Person record to be added already exists in the system.</text>
        //    </acknowledgementDetail>
        // </acknowledgement>
        OMElement ackNode = this.addChildOMElement(rootNode, "acknowledgement");
        OMElement typeCodeNode = this.addChildOMElement(ackNode, "typeCode");

        if (errorDetail != null) {
            this.setAttribute(typeCodeNode, "code", errorCode);
        } else {
            this.setAttribute(typeCodeNode, "code", successCode);
        }
        OMElement targetMessageNode = this.addChildOMElement(ackNode, "targetMessage");
        try {
            OMElement idNodeOnRequest = this.selectSingleNode(requestNode, "./ns:id[1]");
            targetMessageNode.addChild(idNodeOnRequest.cloneOMElement());
        } catch (XPathHelperException ex) {
            // FIXME: What to do?
        }
        if (errorDetail != null) {
            OMElement ackDetailNode = this.addChildOMElement(ackNode, "acknowledgementDetail");
            this.setAttribute(ackDetailNode, "typeCode", "E");
            if (errorDetail.getCode() != null) {
                OMElement codeNode = this.addChildOMElement(ackDetailNode, "code");
                this.setAttribute(codeNode, "code", errorDetail.getCode());
            }
            this.addChildOMElementWithValue(ackDetailNode, "text", errorDetail.getText());
            this.addChildOMElementWithValue(ackDetailNode, "location", this.getClass().getName());
        }
    }

    /**
     *
     * @param requestNode
     * @param subject
     */
    protected void addProviderOrganization(OMElement rootNode, Subject subject) {
        //
        // EXAMPLE:
        //
        // <providerOrganization classCode="ORG" determinerCode="INSTANCE">
        //   <id root="1.2.840.114350.1.13.99998.8734"/>
        //   <name>Good Health Clinic</name>
        //   <contactParty classCode="CON">
        //     <telecom value="tel:+1-342-555-8394"/>
        //   </contactParty>
        // </providerOrganization> */

        // controlActProcess/subject/registrationEvent/subject1/patient/providerOrganization
        OMElement providerOrganizationNode = this.addChildOMElement(rootNode, "providerOrganization");
        this.setAttribute(providerOrganizationNode, "classCode", "ORG");
        this.setAttribute(providerOrganizationNode, "determinerCode", "INSTANCE");

        // controlActProcess/subject/registrationEvent/subject1/patient/providerOrganization/id

        // TBD (FIX): Just pull from the first ID.
        if (subject.getSubjectIdentifiers().size() > 0) {
            SubjectIdentifier subjectIdentifier = subject.getSubjectIdentifiers().get(0);
            SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
            OMElement idNode = this.addChildOMElement(providerOrganizationNode, "id");
            this.setAttribute(idNode, "root", subjectIdentifierDomain.getUniversalId());

            // controlActProcess/subject/registrationEvent/subject1/patient/providerOrganization/name
            this.addChildOMElementWithValue(providerOrganizationNode, "name", subjectIdentifierDomain.getNamespaceId());
        }

        // controlActProcess/subject/registrationEvent/subject1/patient/providerOrganization/contactParty
        OMElement contactPartyNode = this.addChildOMElement(providerOrganizationNode, "contactParty");
        this.setAttribute(contactPartyNode, "classCode", "CON");
        // empty otherwise.
    }

    /**
     *
     * @param requestNode
     * @param subject
     */
    protected void addSubjectOf1(OMElement rootNode, Subject subject) {
        //
        // EXAMPLE:
        //
        // <subjectOf1>
        //   <queryMatchObservation classCode="COND" moodCode="EVN">
        //     <code code="IHE_PDQ"/>
        //       <value xsi:type="INT" value="85"/>
        //   </queryMatchObservation>
        // </subjectOf1>

        // controlActProcess/subject/registrationEvent/subject1/patient/subjectOf1
        OMElement subjectOf1Node = this.addChildOMElement(rootNode, "subjectOf1");

        // controlActProcess/subject/registrationEvent/subject1/patient/subjectOf1/queryMatchObservation
        OMElement queryMatchObservationNode = this.addChildOMElement(subjectOf1Node, "queryMatchObservation");
        this.setAttribute(queryMatchObservationNode, "classCode", "COND");
        this.setAttribute(queryMatchObservationNode, "moodCode", "EVN");
        this.addCode(queryMatchObservationNode, "code", "IHE_PDQ");

        OMElement valueNode = this.addChildOMElement(queryMatchObservationNode, "value");
        OMNamespace xsiNS = this.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        valueNode.addAttribute("type", "INT", xsiNS);
        this.setAttribute(valueNode, "value", new Integer(subject.getMatchConfidencePercentage()).toString());
    }

    /**
     *
     * @param requestNode
     * @param subject
     */
    protected void addCustodian(OMElement rootNode, Subject subject) {
        //
        // EXAMPLE:
        //
        // <custodian typeCode="CST">
        //   <assignedEntity classCode="ASSIGNED">
        //     <id root="1.2.840.114350.1.13.99998.8734"/>
        //   </assignedEntity>
        // </custodian>
        //

        // controlActProcess/subject/registrationEvent/custodian
        OMElement custodianNode = this.addChildOMElement(rootNode, "custodian");
        this.setAttribute(custodianNode, "typeCode", "CST");
        OMElement assignedEntityNode = this.addChildOMElement(custodianNode, "assignedEntity");
        this.setAttribute(assignedEntityNode, "classCode", "ASSIGNED");

        // FIXME: ??? Quick Connectathon code hack....
        Custodian custodian = subject.getCustodian();
        if (custodian != null) {
            if (custodian.getCustodianId() != null) {
                this.addCustodianId(assignedEntityNode, custodian.getCustodianId());

            } else {
                // Use default value...
                this.addCustodianId(assignedEntityNode, this.getSenderDeviceInfo().getId());
            }
            // Deal with health data locator option.
            String healthDataLocatorCodeValue = "NotHealthDataLocator";
            if (custodian.isSupportsHealthDataLocator()) {
                healthDataLocatorCodeValue = "SupportsHealthDataLocator";
            }
            OMElement codeNode = this.addCode(assignedEntityNode, "code", healthDataLocatorCodeValue);
            this.setAttribute(codeNode, "codeSystem", "1.3.6.1.4.1.19376.1.2.27.2");
        } else {
            // Use default value ...
            this.addCustodianId(assignedEntityNode, this.getSenderDeviceInfo().getId());
        }
    }

    /**
     *
     * @param assignedEntityNode
     * @param id
     * @return
     */
    protected OMElement addCustodianId(OMElement assignedEntityNode, String id) {
        OMElement idNode = this.addChildOMElement(assignedEntityNode, "id");
        this.setAttribute(idNode, "root", id);
        return idNode;
    }

    /**
     *
     * @param requestNode
     * @param requestNode
     * @param subjects
     * @param errorDetail
     */
    protected void addQueryAckToRequest(
            OMElement requestNode,
            OMElement rootNode,
            List<Subject> subjects,
            HL7V3ErrorDetail errorDetail) {
        //
        // EXAMPLE:
        //
        // <queryAck>
        //   <queryId root="1.2.840.114350.1.13.28.1.18.5.999" extension="18204"/>
        //   <queryResponseCode code="OK"/>
        //   <resultTotalQuantity value="5"/>
        //   <resultCurrentQuantity value="2"/>
        //   <resultRemainingQuantity value="3"/>
        // </queryAck>
        //
        try {
            OMElement queryAckNode = this.addChildOMElement(rootNode, "queryAck");
            OMElement queryIdNode = this.selectSingleNode(requestNode,
                    "./ns:controlActProcess/ns:queryByParameter/ns:queryId[1]");
            queryAckNode.addChild(queryIdNode.cloneOMElement());
            String queryResponseCode = "OK";
            String quantity;
            if (subjects == null || subjects.isEmpty()) {
                queryResponseCode = "NF";
                quantity = "0";
            } else {
                //queryResponseCode = "OK";
                quantity = new Integer(subjects.size()).toString();
            }
            if (errorDetail != null) {
                // Overrides above code ... a bit ugly ...
                queryResponseCode = "AE";
            }
            // We only deal with full return of all data now (no continuation support).
            this.addCode(queryAckNode, "queryResponseCode", queryResponseCode);
            this.addValue(queryAckNode, "resultTotalQuantity", quantity);
            this.addValue(queryAckNode, "resultCurrentQuantity", quantity);
            this.addValue(queryAckNode, "resultRemainingQuantity", "0");
        } catch (XPathHelperException e) {
            // TBD: Do something.
        }
    }

    /**
     *
     * @param requestNode
     * @param requestNode
     */
    protected void addQueryByParameterFromRequest(OMElement requestNode, OMElement rootNode) {
        try {
            OMElement queryByParameterNode = this.selectSingleNode(requestNode,
                    "./ns:controlActProcess/ns:queryByParameter[1]");
            rootNode.addChild(queryByParameterNode.cloneOMElement());
        } catch (XPathHelperException e) {
            // TBD: Do something.
        }
    }

    /**
     *
     * @param requestNode
     * @param subject
     */
    protected void addAddresses(OMElement rootNode, Subject subject) {
        for (Address address : subject.getAddresses()) {
            OMElement addressNode = this.addChildOMElement(rootNode, "addr");
            // FIXME: Deal with more than one address line.
            this.addChildOMElementWithValue(addressNode, "streetAddressLine", address.getStreetAddressLine1());
            this.addChildOMElementWithValue(addressNode, "city", address.getCity());
            this.addChildOMElementWithValue(addressNode, "state", address.getState());
            this.addChildOMElementWithValue(addressNode, "postalCode", address.getPostalCode());
            this.addChildOMElementWithValue(addressNode, "country", address.getCountry());
        }
    }

    /**
     *
     * @param requestNode
     * @param subject
     */
    protected void addTelecomAddresses(OMElement rootNode, Subject subject) {
        for (TelecomAddress telecomAddress : subject.getTelecomAddresses()) {
            OMElement telecomAddressNode = this.addChildOMElement(rootNode, "telecom");
            this.setAttribute(telecomAddressNode, "use", telecomAddress.getUse());
            this.setAttribute(telecomAddressNode, "value", telecomAddress.getValue());
        }
    }

    /**
     *
     * @param requestNode
     * @param subject
     */
    protected void addSubjectIdentifiers(OMElement rootNode, Subject subject) {
        // controlActProcess/subject/registrationEvent/subject1/patient/id[*]
        for (SubjectIdentifier subjectIdentifier : subject.getSubjectIdentifiers()) {
            OMElement idNode = this.addChildOMElement(rootNode, "id");
            this.addSubjectIdentifier(idNode, subjectIdentifier);
        }
    }

    /**
     *
     * @param requestNode
     * @param subjectIdentifier
     */
    protected void addSubjectIdentifier(OMElement rootNode, SubjectIdentifier subjectIdentifier) {
        this.setAttribute(rootNode, "root", subjectIdentifier.getIdentifierDomain().getUniversalId());
        this.setAttribute(rootNode, "extension", subjectIdentifier.getIdentifier());
    }

    /**
     *
     * @param requestNode
     * @param subject
     */
    protected void addSubjectOtherIdentifiers(OMElement rootNode, Subject subject) {
        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/asOtherIDs[*]
        for (SubjectIdentifier subjectOtherIdentifier : subject.getSubjectOtherIdentifiers()) {
            OMElement asOtherIdsNode = this.addChildOMElement(rootNode, "asOtherIDs");
            this.setAttribute(asOtherIdsNode, "classCode", "CIT");
            this.addSubjectOtherIdentifier(asOtherIdsNode, subjectOtherIdentifier);
        }
    }

    /**
     *
     * @param requestNode
     * @param subjectOtherIdentifier
     */
    protected void addSubjectOtherIdentifier(OMElement rootNode, SubjectIdentifier subjectOtherIdentifier) {
        OMElement idNode = this.addChildOMElement(rootNode, "id");
        this.setAttribute(idNode, "root", subjectOtherIdentifier.getIdentifierDomain().getUniversalId());
        this.setAttribute(idNode, "extension", subjectOtherIdentifier.getIdentifier());
        // <scopingOrganization classCode="ORG" determinerCode="INSTANCE">
        //   <id root="2.16.840.1.113883.4.1" />
        // </scopingOrganization>
        OMElement scopingOrganizationNode = this.addChildOMElement(rootNode, "scopingOrganization");
        this.setAttribute(scopingOrganizationNode, "classCode", "ORG");
        this.setAttribute(scopingOrganizationNode, "determinerCode", "INSTANCE");

        OMElement scopingOrganizationIdNode = this.addChildOMElement(scopingOrganizationNode, "id");
        this.setAttribute(scopingOrganizationIdNode, "root", subjectOtherIdentifier.getIdentifierDomain().getUniversalId());
    }

    /**
     *
     * @param requestNode
     * @param subject
     */
    protected void addSubjectNames(OMElement rootNode, Subject subject) {
        for (SubjectName subjectName : subject.getSubjectNames()) {
            OMElement nameNode = this.addChildOMElement(rootNode, "name");
            this.addSubjectName(nameNode, subjectName);
        }
    }

    /**
     * 
     * @param requestNode
     * @param subjectName
     */
    protected void addSubjectName(OMElement rootNode, SubjectName subjectName) {
        this.addSubjectNameComponent(rootNode, "prefix", subjectName.getPrefix());
        this.addSubjectNameComponent(rootNode, "given", subjectName.getGivenName());
        this.addSubjectNameComponent(rootNode, "given", subjectName.getMiddleName());
        this.addSubjectNameComponent(rootNode, "family", subjectName.getFamilyName());
        this.addSubjectNameComponent(rootNode, "suffix", subjectName.getSuffix());
    }

    /**
     *
     * @param requestNode
     * @param component
     * @param nameValue
     */
    protected void addSubjectNameComponent(OMElement rootNode, String component, String nameValue) {
        if (nameValue != null && nameValue.length() > 0) {
            this.addChildOMElementWithValue(rootNode, component, nameValue);
        }
    }

    /**
     *
     * @param requestNode
     * @param code
     * @return
     */
    protected OMElement addControlActProcess(OMElement rootNode, String code) {
        OMElement controlActProcessNode = this.addChildOMElement(rootNode, "controlActProcess");
        this.setAttribute(controlActProcessNode, "moodCode", "EVN");
        this.setAttribute(controlActProcessNode, "classCode", "CACT");

        // PRPA_IN201309UV02/controlActProcess/code
        OMElement codeNode = this.addCode(controlActProcessNode, "code", code);
        this.setAttribute(codeNode, "codeSystem", "2.16.840.1.113883.1.6");
        return controlActProcessNode;
    }

    /**
     * 
     * @param messageName
     * @param processingCode
     * @param processingModeCode
     * @param acceptAckCode
     * @return
     */
    protected OMElement getResponseNode(String messageName, String processingCode, String processingModeCode, String acceptAckCode) {
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
        this.addCode(responseNode, "processingCode", processingCode);
        this.addCode(responseNode, "processingModeCode", processingModeCode);
        this.addCode(responseNode, "acceptAckCode", acceptAckCode);

        // PRPA_IN201306UV02/receiver
        // PRPA_IN201306UV02/sender
        this.addReceiver(responseNode);
        this.addSender(responseNode);
        return responseNode;
    }

    /**
     *
     * @param messageName
     * @param processingCode
     * @param processingModeCode
     * @param acceptAckCode
     * @return
     */
    protected OMElement getRequestNode(String messageName, String processingCode, String processingModeCode, String acceptAckCode) {
        OMElement requestNode = this.createOMElement(messageName);
        this.setAttribute(requestNode, "ITSVersion", "XML_1.0");
        this.addMessageId(requestNode);

        // <id root="1.2.840.114350.1.13.0.1.7.1.1" extension="35423"/>
        // <creationTime value="20090417150301"/>
        // <interactionId root="2.16.840.1.113883.1.6" extension="PRPA_IN201305UV02"/>
        // <processingCode code="T"/>
        // <processingModeCode code="I"/>
        // <acceptAckCode code="NE"/>
        this.addCreationTime(requestNode);
        this.addInteractionId(messageName, requestNode);
        this.addCode(requestNode, "processingCode", processingCode);
        this.addCode(requestNode, "processingModeCode", processingModeCode);
        this.addCode(requestNode, "acceptAckCode", acceptAckCode);

        // PRPA_IN201305UV02/receiver
        // PRPA_IN201305UV02/sender
        this.addReceiver(requestNode);
        this.addSender(requestNode);
        return requestNode;
    }

    /**
     * 
     * @param rootNode
     * @param subject
     * @return
     */
    protected OMElement addPatientNode(OMElement rootNode, Subject subject) {
        // controlActProcess/subject
        OMElement subjectNode = this.addChildOMElement(rootNode, "subject");
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

        // controlActProcess/subject/registrationEvent/custodian
        this.addCustodian(registrationEventNode, subject);
        return patientNode;
    }

    /**
     *
     * @param rootNode
     * @return
     */
    protected OMElement addPatientPersonNode(OMElement rootNode) {
        OMElement patientPersonNode = this.addChildOMElement(rootNode, "patientPerson");
        this.setAttribute(patientPersonNode, "classCode", "PSN");
        this.setAttribute(patientPersonNode, "determinerCode", "INSTANCE");
        return patientPersonNode;
    }

    /**
     *
     * @param controlActProcessNode
     * @param subjects
     * @param includeDemographics
     */
    protected void addSubjectsWithOnlyIds(OMElement controlActProcessNode, List<Subject> subjects) {
        if (subjects != null) {
            for (Subject subject : subjects) {
                this.addSubjectWithIdsOnly(controlActProcessNode, subject);
            }
        }
    }

    /**
     *
     * @param controlActProcessNode
     * @param subject
     */
    protected void addSubjectWithIdsOnly(OMElement controlActProcessNode, Subject subject) {
        this.addSubjectWithIdsAndNamesOnly(controlActProcessNode, subject, false);
    }

    /**
     *
     * @param controlActProcessNode
     * @param subject
     */
    protected void addSubjectWithIdsAndNamesOnly(OMElement controlActProcessNode, Subject subject) {
        this.addSubjectWithIdsAndNamesOnly(controlActProcessNode, subject, true);
    }

    /**
     * 
     * @param controlActProcessNode
     * @param subject
     * @param emitNames
     */
    private void addSubjectWithIdsAndNamesOnly(OMElement controlActProcessNode, Subject subject, boolean emitNames) {
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
        if (emitNames) {
            this.addSubjectNames(patientPersonNode, subject);
        } else {
            OMElement nameNode = this.addChildOMElement(patientPersonNode, "name");
            this.setAttribute(nameNode, "nullFlavor", "NA");
        }

        // controlActProcess/subject/registrationEvent/subject1/patient/patientPerson/asOtherIds[*]
        this.addSubjectOtherIdentifiers(patientPersonNode, subject);

        // controlActProcess/subject/registrationEvent/subject1/patient/providerOrganization
        this.addProviderOrganization(patientNode, subject);
    }
}
