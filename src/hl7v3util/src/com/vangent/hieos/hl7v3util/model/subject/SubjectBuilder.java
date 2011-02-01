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
package com.vangent.hieos.hl7v3util.model.subject;

import com.vangent.hieos.hl7v3util.model.builder.*;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201301UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private final static String XPATH_PATIENT =
            "./ns:controlActProcess/ns:subject/ns:registrationEvent/ns:subject1/ns:patient[1]";
    private final static String XPATH_PATIENTS =
            "./ns:controlActProcess/ns:subject/ns:registrationEvent/ns:subject1/ns:patient";
    private final static String XPATH_PATIENT_ADDRESSES =
            "./ns:patientPerson/ns:addr";
    private final static String XPATH_PATIENT_NAMES =
            "./ns:patientPerson/ns:name";
    private final static String XPATH_PATIENT_GENDER =
            "./ns:patientPerson/ns:administrativeGenderCode[1]";
    private final static String XPATH_PATIENT_BIRTH_TIME =
            "./ns:patientPerson/ns:birthTime[1]";
    private final static String XPATH_PATIENT_AS_OTHER_IDS =
            "./ns:patientPerson/ns:asOtherIDs";
    private final static String XPATH_PATIENT_REGISTRATION_EVENTS =
            "./ns:controlActProcess/ns:subject/ns:registrationEvent";
    private final static String XPATH_PATIENT_REGISTRATION_PATIENT =
            "./ns:subject1/ns:patient";

    /**
     *
     * @param PRPA_IN201301UV02_Message
     * @return
     */
    public Subject buildSubjectFromPRPA_IN201301UV02_Message(PRPA_IN201301UV02_Message message) throws ModelBuilderException {

        // Get the patient.
        OMElement patientNode = null;
        try {
            patientNode = this.selectSingleNode(message.getMessageNode(), XPATH_PATIENT);
        } catch (XPathHelperException e) {
            throw new ModelBuilderException("Patient not found on request: " + e.getMessage());
        }
        if (patientNode == null) {
            throw new ModelBuilderException("Patient not found on request");
        }

        // Set component parts.
        Subject subject = this.getSubject(patientNode, null);

        //SubjectIdentifier subjectIdentifier = new SubjectIdentifier();
        return subject;
    }

    /**
     *
     * @param PRPA_IN201306UV02_Message
     * @return
     * @throws ModelBuilderException
     */
    public List<Subject> buildSubjectsFromPRPA_IN201306UV02_Message(PRPA_IN201306UV02_Message message) throws ModelBuilderException {

        // Get the patient.
        List<OMElement> patientNodes = null;
        try {
            patientNodes = this.selectNodes(message.getMessageNode(), XPATH_PATIENTS);
        } catch (XPathHelperException e) {
            throw new ModelBuilderException("Patient not found on request: " + e.getMessage());
        }
        if (patientNodes == null) {
            throw new ModelBuilderException("Patient not found on request");
        }

        // Set component parts.
        List<Subject> subjects = new ArrayList<Subject>();
        for (OMElement patientNode : patientNodes) {
            Subject subject = this.getSubject(patientNode, null);
            subjects.add(subject);
        }
        //SubjectIdentifier subjectIdentifier = new SubjectIdentifier();
        return subjects;
    }

    /**
     *
     * @param PRPA_IN201306UV02_Message
     * @return
     * @throws ModelBuilderException
     */
    public List<Subject> buildSubjectsFromPRPA_IN201306UV02_MessageNEW(PRPA_IN201306UV02_Message message) throws ModelBuilderException {

        // Get the patient.
        List<OMElement> registrationEventNodes = null;
        try {
            registrationEventNodes = this.selectNodes(message.getMessageNode(), XPATH_PATIENT_REGISTRATION_EVENTS);
        } catch (XPathHelperException e) {
            throw new ModelBuilderException("No RegistrationEvents found on request: " + e.getMessage());
        }
        if (registrationEventNodes == null) {
            throw new ModelBuilderException("No RegistrationEvents found on request");
        }

        // Set component parts.
        List<Subject> subjects = new ArrayList<Subject>();
        for (OMElement registrationEventNode : registrationEventNodes) {
            try {
                OMElement patientNode = this.selectSingleNode(registrationEventNode,
                        XPATH_PATIENT_REGISTRATION_PATIENT);
                Subject subject = this.getSubject(patientNode, registrationEventNode);
                subjects.add(subject);
            } catch (XPathHelperException ex) {
                // FIXME: Do something.
                logger.error("XPATH ERROR: ", ex);
            }

        }
        //SubjectIdentifier subjectIdentifier = new SubjectIdentifier();
        return subjects;
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
        this.setAddresses(subject, patientNode);
        this.setIdentifiers(subject, patientNode);
        this.setOtherIdentifiers(subject, patientNode);
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
            SubjectGender subjectGender = this.buildGender(genderNode);
            subject.setGender(subjectGender);
            //}
        } catch (XPathHelperException ex) {
            // Do nothing.
        }
    }

    /**
     *
     * @param node
     * @return
     */
    public SubjectGender buildGender(OMElement node) {
        String genderCode = "UN";  // HL7v3: UN=Undifferentiated
        if (node != null) {
            genderCode = node.getAttributeValue(new QName("code"));
        }
        //String genderCode = node.getAttributeValue(new QName("code"));
        SubjectGender subjectGender = new SubjectGender();
        subjectGender.setCode(genderCode);
        return subjectGender;
    }

    /**
     *
     * @param subject
     * @param patientNode
     */
    private void setBirthTime(Subject subject, OMElement patientNode) {
        try {
            OMElement birthTimeNode = this.selectSingleNode(patientNode, XPATH_PATIENT_BIRTH_TIME);
            String hl7BirthTime = null;
            if (birthTimeNode != null) {
                hl7BirthTime = birthTimeNode.getAttributeValue(new QName("value"));
            }
            this.setBirthTime(subject, hl7BirthTime);
        } catch (XPathHelperException ex) {
            // TBD: Do something.
        }
    }

    /**
     *
     * @param subject
     * @param hl7BirthTime
     */
    public void setBirthTime(Subject subject, String hl7BirthTime) {
        Date birthTime = null;
        if (hl7BirthTime != null && hl7BirthTime.length() >= 8) {
            hl7BirthTime = hl7BirthTime.substring(0, 8);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            try {
                birthTime = sdf.parse(hl7BirthTime);
            } catch (ParseException ex) {
                // Do nothing.
            }
        } else {
            // TBD: EMIT WARNING OF SOME SORT.
        }
        subject.setBirthTime(birthTime);
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
     * @param node
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
     * @param patientNode
     */
    private void setAddresses(Subject subject, OMElement patientNode) {
        try {
            List<Address> addresses = subject.getAddresses();
            List<OMElement> addressNodes = this.selectNodes(patientNode, XPATH_PATIENT_ADDRESSES);
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
     * @param node
     * @return
     */
    public Address buildAddress(OMElement node) {
        Address address = new Address();
        address.setStreetAddressLine(this.getFirstChildNodeValue(node, "streetAddressLine"));
        address.setCity(this.getFirstChildNodeValue(node, "city"));
        address.setState(this.getFirstChildNodeValue(node, "state"));
        address.setPostalCode(this.getFirstChildNodeValue(node, "postalCode"));
        address.setCountry(this.getFirstChildNodeValue(node, "country"));
        return address;
    }

    /**
     * 
     * @param subject
     * @param patientNode
     */
    private void setIdentifiers(Subject subject, OMElement patientNode) {
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        Iterator<OMElement> iter = patientNode.getChildrenWithName(new QName("id"));
        while (iter.hasNext()) {
            OMElement idNode = iter.next();
            SubjectIdentifier subjectIdentifier = this.buildSubjectIdentifier(idNode);
            subjectIdentifiers.add(subjectIdentifier);
        }
    }

    /**
     *
     * @param subject
     * @param patientNode
     */
    private void setOtherIdentifiers(Subject subject, OMElement patientNode) {
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        try {
            List<OMElement> asOtherIDs = this.selectNodes(patientNode, XPATH_PATIENT_AS_OTHER_IDS);
            for (OMElement asOtherID : asOtherIDs) {
                OMElement idNode = this.getFirstChildNodeWithName(asOtherID, "id");
                SubjectIdentifier subjectIdentifier = this.buildSubjectIdentifier(idNode);
                subjectIdentifiers.add(subjectIdentifier);
            }
        } catch (XPathHelperException ex) {
            // Just ignore here.
        }
    }

    /**
     * 
     * @param subject
     * @param registrationEventNode
     */
    private void setCustodian(Subject subject, OMElement registrationEventNode) {
        // <custodian typeCode="CST">
        //   <assignedEntity classCode="ASSIGNED">
        //     <id root="1.2.840.114350.1.13.99998.8734"/>
        //     <code code="SupportsHealthDataLocator"
        //             codeSystem="1.3.6.1.4.1.19376.1.2.27.2"/>
        //   </assignedEntity>
        // </custodian>
        if (registrationEventNode != null) {
            try {
                OMElement codeNode = this.selectSingleNode(registrationEventNode,
                        "./ns:custodian/ns:assignedEntity/ns:code[1]");
                Custodian custodian = new Custodian();
                boolean foundCustodianInfo = false;
                if (codeNode != null) {
                    String healthLocatorCodeValue = codeNode.getAttributeValue(new QName("code"));
                    if (healthLocatorCodeValue != null) {
                        foundCustodianInfo = true;
                        if (healthLocatorCodeValue.equalsIgnoreCase("SupportsHealthDataLocator")) {
                            custodian.setSupportsHealthDataLocator(true);
                        } else {
                            custodian.setSupportsHealthDataLocator(false);
                        }
                    }
                }
                OMElement idNode = this.selectSingleNode(registrationEventNode,
                        "./ns:custodian/ns:assignedEntity/ns:id[1]");
                if (idNode != null) {
                    String custodianId = idNode.getAttributeValue(new QName("root"));
                    if (custodianId != null) {
                        foundCustodianInfo = true;
                        custodian.setCustodianId(custodianId);
                    }
                }
                if (foundCustodianInfo == true) {
                    subject.setCustodian(custodian);
                }
            } catch (XPathHelperException ex) {
                // TBD: ? Just eat ?
            }
        }
    }

    /**
     *
     * @param node
     * @return
     */
    public SubjectIdentifier buildSubjectIdentifier(OMElement node) {
        SubjectIdentifier subjectIdentifier = new SubjectIdentifier();
        String root = node.getAttributeValue(new QName("root")); // PID - required.
        String extension = node.getAttributeValue(new QName("extension")); // Assigning Authority - required.
        // TBD: Validate root/extension exist!
        String assigningAuthorityName = node.getAttributeValue(new QName("assigningAuthorityName")); // Optional.
        subjectIdentifier.setIdentifier(extension);
        SubjectIdentifierDomain identifierDomain = new SubjectIdentifierDomain();
        identifierDomain.setUniversalId(root);
        identifierDomain.setNamespaceId(assigningAuthorityName);
        identifierDomain.setUniversalIdType("ISO"); // FIXME: FIXED VALUE?
        subjectIdentifier.setIdentifierDomain(identifierDomain);
        return subjectIdentifier;
    }
}
