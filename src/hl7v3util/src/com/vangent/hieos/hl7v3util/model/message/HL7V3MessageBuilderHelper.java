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
import com.vangent.hieos.hl7v3util.model.subject.Custodian;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectName;
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
     * @param rootNode
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
            String homeCommunityId = this.getNodeAttributeValue(deviceNode,
                    "./ns:asAgent/ns:representedOrganization/ns:id[1]", "root");
            deviceInfo.setId(id);
            deviceInfo.setName(name);
            deviceInfo.setTelecom(telecom);
            deviceInfo.setHomeCommunityId(homeCommunityId);
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
     * @param rootNode
     * @return
     */
    protected OMElement addSender(OMElement rootNode) {
        // rootNode/sender
        OMElement senderNode = this.addChildOMElement(rootNode, "sender");
        senderNode.addAttribute("typeCode", "SND", null);
        this.addDevice(senderNode, this.getSenderDeviceInfo());
        return senderNode;
    }

    /**
     * 
     * @param rootNode
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
     * @param rootNode
     * @param deviceInfo
     * @return
     */
    protected OMElement addDevice(OMElement rootNode, DeviceInfo deviceInfo) {
        // Add "device" for Sender:
        // rootNode/sender/device
        OMElement deviceNode = this.addChildOMElement(rootNode, "device");
        this.setAttribute(deviceNode, "classCode", "DEV");
        this.setAttribute(deviceNode, "determinerCode", "INSTANCE");

        // rootNode/sender/device/id
        if (deviceInfo.getId() != null) {
            OMElement idNode = this.addChildOMElement(deviceNode, "id");
            this.setAttribute(idNode, "root", deviceInfo.getId());
        }

        // rootNode/sender/device/name
        if (deviceInfo.getName() != null) {
            this.addChildOMElementWithValue(deviceNode, "name", deviceInfo.getName());
        }

        // rootNode/sender/device/telecom
        if (deviceInfo.getTelecom() != null) {
            OMElement telecomNode = this.addChildOMElement(deviceNode, "telecom");
            this.setAttribute(telecomNode, "value", deviceInfo.getTelecom());
        }

        // Now, see if there is a home community id.
        if (deviceInfo.getHomeCommunityId() != null) {
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
                    "root", this.getHomeCommunityIdWithStrippedPrefix(deviceInfo.getHomeCommunityId()));
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
     * @param rootNode
     */
    protected void addInteractionId(String messageName, OMElement rootNode) {
        OMElement childNode = this.createOMElement("interactionId");
        rootNode.addChild(childNode);
        childNode.addAttribute("extension", messageName, null);
        childNode.addAttribute("root", "2.16.840.1.113883.1.6", null);
    }

    /**
     *
     * @param rootNode
     */
    protected void addCreationTime(OMElement rootNode) {
        OMElement creationTimeNode = this.createOMElement("creationTime");
        rootNode.addChild(creationTimeNode);
        creationTimeNode.addAttribute("value", Hl7Date.now().toString(), null);
    }

    /**
     *
     * @param rootNode
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
     * @param errorText
     */
    protected void addAcknowledgementToRequest(OMElement requestNode, OMElement rootNode, String errorText) {
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

        if (errorText != null) {
            this.setAttribute(typeCodeNode, "code", "AE");
        } else {
            this.setAttribute(typeCodeNode, "code", "AA");
        }
        OMElement targetMessageNode = this.addChildOMElement(ackNode, "targetMessage");
        try {
            OMElement idNodeOnRequest = this.selectSingleNode(requestNode, "./ns:id[1]");
            targetMessageNode.addChild(idNodeOnRequest.cloneOMElement());
        } catch (XPathHelperException ex) {
            // FIXME: What to do?
        }
        if (errorText != null) {
            OMElement ackDetailNode = this.addChildOMElement(ackNode, "acknowledgementDetail");
            this.addChildOMElementWithValue(ackDetailNode, "text", errorText);
        }
    }

    /**
     *
     * @param rootNode
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
     * @param rootNode
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
     * @param rootNode
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
    private OMElement addCustodianId(OMElement assignedEntityNode, String id) {
        OMElement idNode = this.addChildOMElement(assignedEntityNode, "id");
        this.setAttribute(idNode, "root", id);
        return idNode;
    }

    /**
     *
     * @param requestNode
     * @param rootNode
     * @param subjects
     * @param errorText
     */
    protected void addQueryAckToRequest(
            OMElement requestNode,
            OMElement rootNode,
            List<Subject> subjects,
            String errorText) {
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
            if (subjects == null || subjects.size() == 0) {
                //queryResponseCode = "NF";
                quantity = "0";
            } else {
                //queryResponseCode = "OK";
                quantity = new Integer(subjects.size()).toString();
            }
            if (errorText != null) {
                // Overrides above code ... a bit ugly ...
                queryResponseCode = "QE";  // FIXME: Should this be AE?
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
     * @param rootNode
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
     * @param rootNode
     * @param subject
     */
    protected void addAddresses(OMElement rootNode, Subject subject) {
        for (Address address : subject.getAddresses()) {
            OMElement addressNode = this.addChildOMElement(rootNode, "addr");
            this.addChildOMElementWithValue(addressNode, "streetAddressLine", address.getStreetAddressLine());
            this.addChildOMElementWithValue(addressNode, "city", address.getCity());
            this.addChildOMElementWithValue(addressNode, "state", address.getState());
            this.addChildOMElementWithValue(addressNode, "postalCode", address.getPostalCode());
            this.addChildOMElementWithValue(addressNode, "country", address.getCountry());
        }
    }

    /**
     *
     * @param rootNode
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
     * @param rootNode
     * @param subjectIdentifier
     */
    protected void addSubjectIdentifier(OMElement rootNode, SubjectIdentifier subjectIdentifier) {
        this.setAttribute(rootNode, "root", subjectIdentifier.getIdentifierDomain().getUniversalId());
        this.setAttribute(rootNode, "extension", subjectIdentifier.getIdentifier());
    }

    /**
     *
     * @param rootNode
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
     * @param rootNode
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
     * @param rootNode
     * @param component
     * @param nameValue
     */
    private void addSubjectNameComponent(OMElement rootNode, String component, String nameValue) {
        if (nameValue != null && nameValue.length() > 0) {
            this.addChildOMElementWithValue(rootNode, component, nameValue);
        }
    }
}
