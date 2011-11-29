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
package com.vangent.hieos.hl7v3util.model.subject;

import com.vangent.hieos.hl7v3util.model.builder.BuilderHelper;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201301UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201310UV02_Message;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201302UV02_Message;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectBuilder extends BuilderHelper {

    private final static Logger logger = Logger.getLogger(SubjectBuilder.class);
    private final static String XPATH_PATIENT_ADD =
            "./ns:controlActProcess/ns:subject/ns:registrationEvent/ns:subject1/ns:patient[1]";
    private final static String XPATH_PATIENT_ADDRESSES =
            "./ns:patientPerson/ns:addr";
    private final static String XPATH_PATIENT_TELECOM_ADDRESSES =
            "./ns:patientPerson/ns:telecom";
    private final static String XPATH_PATIENT_NAMES =
            "./ns:patientPerson/ns:name";
    private final static String XPATH_PATIENT_GENDER =
            "./ns:patientPerson/ns:administrativeGenderCode[1]";
    private final static String XPATH_PATIENT_MARITAL_STATUS =
            "./ns:patientPerson/ns:maritalStatusCode[1]";
    private final static String XPATH_PATIENT_RELIGIOUS_AFFILIATION =
            "./ns:patientPerson/ns:religiousAffiliationCode[1]";
    private final static String XPATH_PATIENT_RACE =
            "./ns:patientPerson/ns:raceCode[1]";
    private final static String XPATH_PATIENT_ETHNIC_GROUP =
            "./ns:patientPerson/ns:ethnicGroupCode[1]";
    private final static String XPATH_PATIENT_BIRTH_TIME =
            "./ns:patientPerson/ns:birthTime[1]";
    private final static String XPATH_PATIENT_DECEASED_INDICATOR =
            "./ns:patientPerson/ns:deceasedInd[1]";
    private final static String XPATH_PATIENT_DECEASED_TIME =
            "./ns:patientPerson/ns:deceasedTime[1]";
    private final static String XPATH_PATIENT_MULTIPLE_BIRTH_INDICATOR =
            "./ns:patientPerson/ns:multipleBirthInd[1]";
    private final static String XPATH_PATIENT_MULTIPLE_BIRTH_ORDER_NUMBER =
            "./ns:patientPerson/ns:multipleBirthOrderNumber[1]";
    private final static String XPATH_PATIENT_AS_OTHER_IDS =
            "./ns:patientPerson/ns:asOtherIDs";
    private final static String XPATH_SUBJECTS =
            "./ns:controlActProcess/ns:subject";
    private final static String XPATH_SUBJECT_REGISTRATION_EVENT =
            "./ns:registrationEvent[1]";
    private final static String XPATH_PATIENT =
            "./ns:registrationEvent/ns:subject1/ns:patient[1]";

    /**
     *
     * @param message
     * @return
     * @throws ModelBuilderException
     */
    public Subject buildSubject(PRPA_IN201301UV02_Message message) throws ModelBuilderException {
        return this.buildSubjectFromMessage(message);
    }

    /**
     *
     * @param message
     * @return
     * @throws ModelBuilderException
     */
    public Subject buildSubject(PRPA_IN201302UV02_Message message) throws ModelBuilderException {
        return this.buildSubjectFromMessage(message);
    }

    /**
     *
     * @param message
     * @return
     * @throws ModelBuilderException
     */
    public SubjectSearchResponse buildSubjectSearchResponse(PRPA_IN201306UV02_Message message) throws ModelBuilderException {
        return this.buildSubjectSearchResponse(message, true);

    }

    /**
     *
     * @param message
     * @return
     * @throws ModelBuilderException
     */
    public SubjectSearchResponse buildSubjectSearchResponse(PRPA_IN201310UV02_Message message) throws ModelBuilderException {
        return this.buildSubjectSearchResponse(message, false);
    }

    /**
     * 
     * @param message
     * @return
     * @throws ModelBuilderException
     */
    protected Subject buildSubjectFromMessage(HL7V3Message message) throws ModelBuilderException {
        // Get the patient.
        OMElement patientNode = null;
        try {
            patientNode = this.selectSingleNode(message.getMessageNode(), XPATH_PATIENT_ADD);
        } catch (XPathHelperException e) {
            throw new ModelBuilderException("Patient not found on request: " + e.getMessage());
        }
        if (patientNode == null) {
            throw new ModelBuilderException("Patient not found on request");
        }

        // Set component parts.
        Subject subject = this.getSubject(patientNode, null);

        return subject;
    }

    /**
     * 
     * @param message
     * @param getCompleteSubjects
     * @return
     * @throws ModelBuilderException
     */
    private SubjectSearchResponse buildSubjectSearchResponse(HL7V3Message message, boolean getCompleteSubjects) throws ModelBuilderException {
        List<OMElement> subjectNodes = null;
        try {
            // Get the list of subjects.
            subjectNodes = this.selectNodes(message.getMessageNode(), XPATH_SUBJECTS);
        } catch (XPathHelperException e) {
            throw new ModelBuilderException("No subjects found on request: " + e.getMessage());
        }
        if (subjectNodes == null) {
            throw new ModelBuilderException("No subjects found on request");
        }

        // Set component parts.
        List<Subject> subjects = new ArrayList<Subject>();
        for (OMElement subjectNode : subjectNodes) {
            try {
                OMElement registrationEventNode = this.selectSingleNode(subjectNode, XPATH_SUBJECT_REGISTRATION_EVENT);
                OMElement patientNode = this.selectSingleNode(subjectNode, XPATH_PATIENT);
                Subject subject;
                if (getCompleteSubjects == true) {
                    subject = this.getSubject(patientNode, registrationEventNode);
                } else {
                    subject = this.getSubjectWithIdentifiersOnly(patientNode, registrationEventNode);
                }
                subjects.add(subject);
            } catch (XPathHelperException e) {
                throw new ModelBuilderException("./controlActProcess/subject(s) is malformed: " + e.getMessage());
            }
        }
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        subjectSearchResponse.setSubjects(subjects);
        return subjectSearchResponse;
    }

    /**
     *
     * @param patientNode
     * @param registrationEventNode
     * @return
     */
    private Subject getSubject(OMElement patientNode, OMElement registrationEventNode) {
        Subject subject = new Subject();
        this.setGender(subject, patientNode);
        this.setBirthTime(subject, patientNode);
        this.setNames(subject, patientNode);
        this.setTelecomAddresses(subject, patientNode);
        this.setAddresses(subject, patientNode);
        this.setMultipleBirthIndicator(subject, patientNode);
        this.setDeceasedIndicator(subject, patientNode);
        this.setMaritalStatus(subject, patientNode);
        this.setReligiousAffiliation(subject, patientNode);
        this.setRace(subject, patientNode);
        this.setEthnicGroup(subject, patientNode);
        this.setSubjectIdentifiers(subject, patientNode);
        this.setSubjectOtherIdentifiers(subject, patientNode);
        this.setCustodian(subject, registrationEventNode);
        this.setMatchConfidencePercentage(subject, patientNode);
        return subject;
    }

    /**
     *
     * @param patientNode
     * @param registrationEventNode
     * @return
     */
    private Subject getSubjectWithIdentifiersOnly(OMElement patientNode, OMElement registrationEventNode) {
        Subject subject = new Subject();
        this.setSubjectIdentifiers(subject, patientNode);
        this.setSubjectOtherIdentifiers(subject, patientNode);
        this.setCustodian(subject, registrationEventNode);
        return subject;
    }

    /**
     *
     * @param subject
     * @param patientNode
     */
    private void setGender(Subject subject, OMElement patientNode) {
        try {
            OMElement genderNode = this.selectSingleNode(patientNode, XPATH_PATIENT_GENDER);
            //if (genderNode != null) {
            CodedValue subjectGender = this.buildCodedValue(genderNode);
            subject.setGender(subjectGender);
            //}
        } catch (XPathHelperException ex) {
            // Do nothing.
        }
    }

    /**
     *
     * @param subject
     * @param patientNode
     */
    private void setMaritalStatus(Subject subject, OMElement patientNode) {
        try {
            OMElement maritalStatusNode = this.selectSingleNode(patientNode, XPATH_PATIENT_MARITAL_STATUS);
            CodedValue maritalStatus = this.buildCodedValue(maritalStatusNode);
            subject.setMaritalStatus(maritalStatus);
        } catch (XPathHelperException ex) {
            // Do nothing.
        }
    }

    /**
     *
     * @param subject
     * @param patientNode
     */
    private void setReligiousAffiliation(Subject subject, OMElement patientNode) {
        try {
            OMElement religiousAffiliationNode = this.selectSingleNode(patientNode, XPATH_PATIENT_RELIGIOUS_AFFILIATION);
            CodedValue religiousAffiliation = this.buildCodedValue(religiousAffiliationNode);
            subject.setReligiousAffiliation(religiousAffiliation);
        } catch (XPathHelperException ex) {
            // Do nothing.
        }
    }

    /**
     *
     * @param subject
     * @param patientNode
     */
    private void setRace(Subject subject, OMElement patientNode) {
        try {
            OMElement raceNode = this.selectSingleNode(patientNode, XPATH_PATIENT_RACE);
            CodedValue race = this.buildCodedValue(raceNode);
            subject.setRace(race);
        } catch (XPathHelperException ex) {
            // Do nothing.
        }
    }

    /**
     *
     * @param subject
     * @param patientNode
     */
    private void setEthnicGroup(Subject subject, OMElement patientNode) {
        try {
            OMElement ethnicGroupNode = this.selectSingleNode(patientNode, XPATH_PATIENT_ETHNIC_GROUP);
            CodedValue ethnicGroup = this.buildCodedValue(ethnicGroupNode);
            subject.setEthnicGroup(ethnicGroup);
        } catch (XPathHelperException ex) {
            // Do nothing.
        }
    }

    /**
     *
     * @param node
     * @return
     */
    public CodedValue buildCodedValue(OMElement node) {
        CodedValue codedValue = null;
        if (node != null) {
            String code = node.getAttributeValue(new QName("code"));
            codedValue = new CodedValue();
            codedValue.setCode(code);
        }
        return codedValue;
    }

    /**
     *
     * @param subject
     * @param patientNode
     */
    private void setBirthTime(Subject subject, OMElement patientNode) {
        subject.setBirthTime(this.getHL7DateValue(patientNode, XPATH_PATIENT_BIRTH_TIME));
    }

    /**
     *
     * @param subject
     * @param patientNode
     */
    private void setNames(Subject subject, OMElement patientNode) {
        try {
            List<SubjectName> subjectNames = subject.getSubjectNames();
            List<OMElement> subjectNameNodes = this.selectNodes(patientNode, XPATH_PATIENT_NAMES);
            for (OMElement subjectNameNode : subjectNameNodes) {
                SubjectName subjectName = this.buildSubjectName(subjectNameNode);
                subjectNames.add(subjectName);
            }
        } catch (XPathHelperException ex) {
            // Just ignore here.
        }
    }

    /**
     *
     * @param rootNode
     * @return
     */
    public SubjectName buildSubjectName(OMElement rootNode) {
        SubjectName subjectName = new SubjectName();
        subjectName.setFamilyName(this.getFirstChildNodeValue(rootNode, "family"));
        subjectName.setPrefix(this.getFirstChildNodeValue(rootNode, "prefix"));
        subjectName.setSuffix(this.getFirstChildNodeValue(rootNode, "suffix"));
        try {
            List<OMElement> givenNames = this.selectNodes(rootNode, "./ns:given");
            if (givenNames != null) {
                int numGivenNames = givenNames.size();
                if (numGivenNames > 0) {
                    subjectName.setGivenName(givenNames.get(0).getText());
                    if (numGivenNames > 1) {
                        subjectName.setMiddleName(givenNames.get(1).getText());
                    }
                }
            }
        } catch (XPathHelperException e) {
            // TBD: Do something.
        }
        subjectName.nullEmptyFields();  // FIXME: HACK ... bad workaround for OpenEMPI problem.
        return subjectName;
    }

    /**
     *
     * @param subject
     * @param rootNode
     */
    private void setAddresses(Subject subject, OMElement rootNode) {
        try {
            List<Address> addresses = subject.getAddresses();
            List<OMElement> addressNodes = this.selectNodes(rootNode, XPATH_PATIENT_ADDRESSES);
            for (OMElement addressNode : addressNodes) {
                Address address = this.buildAddress(addressNode);
                addresses.add(address);
            }
        } catch (XPathHelperException ex) {
            // Just ignore here.
        }
    }

    /**
     *
     * @param subject
     * @param rootNode
     */
    private void setDeceasedIndicator(Subject subject, OMElement rootNode) {
        // Deceased indicator.
        subject.setDeceasedIndicator(this.getBooleanValue(rootNode, XPATH_PATIENT_DECEASED_INDICATOR));

        // Deceased time.
        subject.setDeceasedTime(this.getHL7DateValue(rootNode, XPATH_PATIENT_DECEASED_TIME));
    }

    /**
     *
     * @param subject
     * @param rootNode
     */
    private void setMultipleBirthIndicator(Subject subject, OMElement rootNode) {
        // Multiple birth indicator.
        subject.setMultipleBirthIndicator(this.getBooleanValue(rootNode, XPATH_PATIENT_MULTIPLE_BIRTH_INDICATOR));

        // Multiple birth order number.
        subject.setMultipleBirthOrderNumber(this.getIntegerValue(rootNode, XPATH_PATIENT_MULTIPLE_BIRTH_ORDER_NUMBER));
    }

    /**
     *
     * @param subject
     * @param rootNode
     */
    private void setTelecomAddresses(Subject subject, OMElement rootNode) {
        try {
            List<TelecomAddress> telecomAddresses = subject.getTelecomAddresses();
            List<OMElement> telecomAddressNodes = this.selectNodes(rootNode, XPATH_PATIENT_TELECOM_ADDRESSES);
            for (OMElement telecomAddressNode : telecomAddressNodes) {
                TelecomAddress telecomAddress = this.buildTelecomAddress(telecomAddressNode);
                telecomAddresses.add(telecomAddress);
            }
        } catch (XPathHelperException ex) {
            // Just ignore here.
        }
    }

    /**
     *
     * @param rootNode
     * @return
     */
    public TelecomAddress buildTelecomAddress(OMElement rootNode) {
        TelecomAddress telecomAddress = new TelecomAddress();
        telecomAddress.setUse(rootNode.getAttributeValue(new QName("use")));
        telecomAddress.setValue(rootNode.getAttributeValue(new QName("value")));
        return telecomAddress;
    }

    /**
     *
     * @param rootNode
     * @return
     */
    public Address buildAddress(OMElement rootNode) {
        Address address = new Address();
        // FIXME? - deal with more than one address line.
        address.setStreetAddressLine1(this.getFirstChildNodeValue(rootNode, "streetAddressLine"));
        address.setCity(this.getFirstChildNodeValue(rootNode, "city"));
        address.setState(this.getFirstChildNodeValue(rootNode, "state"));
        address.setPostalCode(this.getFirstChildNodeValue(rootNode, "postalCode"));
        address.setCountry(this.getFirstChildNodeValue(rootNode, "country"));
        return address;
    }

    /**
     * 
     * @param subject
     * @param rootNode
     */
    protected void setSubjectIdentifiers(Subject subject, OMElement rootNode) {
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        Iterator<OMElement> iter = rootNode.getChildrenWithName(new QName("id"));
        while (iter.hasNext()) {
            OMElement idNode = iter.next();
            SubjectIdentifier subjectIdentifier = this.buildSubjectIdentifier(idNode);
            subjectIdentifiers.add(subjectIdentifier);
        }
    }

    /**
     *
     * @param subject
     * @param rootNode
     */
    private void setSubjectOtherIdentifiers(Subject subject, OMElement rootNode) {
        List<SubjectIdentifier> subjectOtherIdentifiers = subject.getSubjectOtherIdentifiers();
        try {
            List<OMElement> asOtherIDs = this.selectNodes(rootNode, XPATH_PATIENT_AS_OTHER_IDS);
            for (OMElement asOtherID : asOtherIDs) {
                OMElement idNode = this.getFirstChildNodeWithName(asOtherID, "id");
                SubjectIdentifier subjectIdentifier = this.buildSubjectIdentifier(idNode);
                subjectOtherIdentifiers.add(subjectIdentifier);
            }
        } catch (XPathHelperException ex) {
            // Just ignore here.
        }
    }

    /**
     * 
     * @param subject
     * @param rootNode
     */
    private void setCustodian(Subject subject, OMElement rootNode) {
        // <custodian typeCode="CST">
        //   <assignedEntity classCode="ASSIGNED">
        //     <id root="1.2.840.114350.1.13.99998.8734"/>
        //     <code code="SupportsHealthDataLocator"
        //             codeSystem="1.3.6.1.4.1.19376.1.2.27.2"/>
        //   </assignedEntity>
        // </custodian>
        if (rootNode != null) {
            try {
                Custodian custodian = null;
                OMElement idNode = this.selectSingleNode(rootNode,
                        "./ns:custodian/ns:assignedEntity/ns:id[1]");
                if (idNode != null) {
                    String custodianId = idNode.getAttributeValue(new QName("root"));
                    if (custodianId != null) {
                        custodian = new Custodian();
                        custodian.setCustodianId(custodianId);
                        subject.setCustodian(custodian);
                    }
                }
                if (custodian != null) {
                    OMElement codeNode = this.selectSingleNode(rootNode,
                            "./ns:custodian/ns:assignedEntity/ns:code[1]");
                    if (codeNode != null) {
                        String healthLocatorCodeValue = codeNode.getAttributeValue(new QName("code"));
                        if (healthLocatorCodeValue != null) {
                            if (healthLocatorCodeValue.equalsIgnoreCase("SupportsHealthDataLocator")) {
                                custodian.setSupportsHealthDataLocator(true);
                            } else {
                                custodian.setSupportsHealthDataLocator(false);
                            }
                        }
                    }
                }
            } catch (XPathHelperException ex) {
                // TBD: ? Just eat ?
            }
        }
    }

    /**
     * 
     * @param subject
     * @param rootNode
     */
    private void setMatchConfidencePercentage(Subject subject, OMElement rootNode) {
        // <subjectOf1>
        //   <queryMatchObservation classCode="COND" moodCode="EVN">
        //     <code code="IHE_PDQ"/>
        //     <value xsi:type="INT" value="92" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>
        //   </queryMatchObservation>
        // </subjectOf1>
        try {
            OMElement valueNode = this.selectSingleNode(rootNode,
                    "./ns:subjectOf1/ns:queryMatchObservation/ns:value[1]");
            if (valueNode != null) {
                String value = valueNode.getAttributeValue(new QName("value"));
                if (value != null) {
                    subject.setMatchConfidencePercentage(new Integer(value));
                }
            }
        } catch (XPathHelperException ex) {
            // TBD: ? Just eat ?
        }
    }

    /**
     *
     * @param rootNode
     * @return
     */
    public SubjectIdentifier buildSubjectIdentifier(OMElement rootNode) {
        SubjectIdentifier subjectIdentifier = new SubjectIdentifier();
        String root = rootNode.getAttributeValue(new QName("root")); // Assigning Authority - required.
        String extension = rootNode.getAttributeValue(new QName("extension")); // PID - required.
        // TBD: Validate root/extension exist!
        String assigningAuthorityName = rootNode.getAttributeValue(new QName("assigningAuthorityName")); // Optional.
        subjectIdentifier.setIdentifier(extension);
        SubjectIdentifierDomain identifierDomain = new SubjectIdentifierDomain();
        identifierDomain.setUniversalId(root);
        identifierDomain.setNamespaceId(assigningAuthorityName);
        identifierDomain.setUniversalIdType("ISO"); // FIXME: FIXED VALUE?
        subjectIdentifier.setIdentifierDomain(identifierDomain);
        return subjectIdentifier;
    }
}
