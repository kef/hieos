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

import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201309UV02_Message;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectSearchCriteriaBuilder extends SubjectBuilder {

    private final static Logger logger = Logger.getLogger(SubjectSearchCriteriaBuilder.class);
    private final static String XPATH_PARAMETER_LIST =
            "./ns:controlActProcess/ns:queryByParameter/ns:parameterList[1]";
    private final static String XPATH_PARAMETER_ADDRESSES =
            "./ns:patientAddress";
    private final static String XPATH_PARAMETER_NAMES =
            "./ns:livingSubjectName";
    private final static String XPATH_PARAMETER_BIRTH_TIME =
            "./ns:livingSubjectBirthTime/ns:value[1]";
    private final static String XPATH_PARAMETER_GENDER =
            "./ns:livingSubjectAdministrativeGender/ns:value[1]";
    private final static String XPATH_OTHER_IDS_SCOPING_ORGANIZATIONS =
            "./ns:otherIDsScopingOrganization";
    private final static String XPATH_PARAMETER_IDS =
            "./ns:livingSubjectId";
    private final static String XPATH_PARAMETER_PATIENT_ID =
            "./ns:controlActProcess/ns:queryByParameter/ns:parameterList/ns:patientIdentifier[1]";
    private final static String XPATH_DATA_SOURCES =
            "./ns:controlActProcess/ns:queryByParameter/ns:parameterList/ns:dataSource";
    private final static String XPATH_COMMUNITY_PATIENT_ID_ASSIGNING_AUTHORITY =
            "./ns:controlActProcess/ns:authorOrPerformer/ns:assignedDevice/ns:id[1]";
    private final static String XPATH_PARAMETER_MIN_DEGREE_MATCH_VALUE =
            "./ns:controlActProcess/ns:queryByParameter/ns:matchCriterionList/ns:minimumDegreeMatch/ns:value[1]";

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return
     */
    public SubjectSearchCriteria buildSubjectSearchCriteria(PRPA_IN201305UV02_Message message) throws ModelBuilderException {
        OMElement parameterListNode;
        try {
            // Get ParameterList.
            parameterListNode = this.selectSingleNode(message.getMessageNode(), XPATH_PARAMETER_LIST);
        } catch (XPathHelperException ex) {
            throw new ModelBuilderException(ex.getMessage());
        }
        if (parameterListNode == null) {
            throw new ModelBuilderException("No parameterList found");
        }
        SubjectSearchCriteria crit = new SubjectSearchCriteria();
        Subject subject = new Subject();
        crit.setSubject(subject);

        // Get search items from request.
        this.setGender(subject, parameterListNode);
        this.setBirthTime(subject, parameterListNode);
        this.setNames(subject, parameterListNode);
        this.setAddresses(subject, parameterListNode);
        this.setIdentifiers(subject, parameterListNode);
        this.setScopingAssigningAuthorities(crit, parameterListNode);
        this.setMinimumDegreeMatchPercentage(crit, message.getMessageNode());

        // Get community patient id assigning authority.
        this.setCommunityAssigningAuthority(crit, message.getMessageNode());
        return crit;
    }

    /**
     *
     * @param PRPA_IN201309UV02_Message
     * @return
     */
    public SubjectSearchCriteria buildSubjectSearchCriteria(PRPA_IN201309UV02_Message message) throws ModelBuilderException {
        OMElement patientIdNode;
        try {
            // Get ParameterList.
            patientIdNode = this.selectSingleNode(message.getMessageNode(), XPATH_PARAMETER_PATIENT_ID);
        } catch (XPathHelperException ex) {
            throw new ModelBuilderException(ex.getMessage());
        }
        if (patientIdNode == null) {
            throw new ModelBuilderException("No PatientIdentifier found");
        }
        SubjectSearchCriteria crit = new SubjectSearchCriteria();
        Subject subject = new Subject();
        crit.setSubject(subject);
        this.setPatientIdentifier(subject, patientIdNode);
        this.setScopingAssigningAuthoritiesFromDataSources(crit, message.getMessageNode());
        return crit;
    }

    /**
     *
     * @param subject
     * @param parameterListNode
     */
    private void setGender(Subject subject, OMElement parameterListNode) {
        try {
            OMElement genderNode = this.selectSingleNode(parameterListNode, XPATH_PARAMETER_GENDER);
            if (genderNode != null) {
                CodedValue subjectGender = this.buildCodedValue(genderNode);
                subject.setGender(subjectGender);
            }
        } catch (XPathHelperException ex) {
            // Do nothing.
        }
    }

    /**
     *
     * @param subject
     * @param parameterListNode
     */
    private void setBirthTime(Subject subject, OMElement parameterListNode) {
        subject.setBirthTime(this.getHL7DateValue(parameterListNode, XPATH_PARAMETER_BIRTH_TIME));
    }

    /**
     *
     * @param subject
     * @param parameterListNode
     */
    private void setNames(Subject subject, OMElement parameterListNode) {
        try {
            List<SubjectName> subjectNames = subject.getSubjectNames();
            List<OMElement> subjectNameNodes = this.selectNodes(parameterListNode, XPATH_PARAMETER_NAMES);
            for (OMElement subjectNameNode : subjectNameNodes) {
                OMElement valueNode = this.getFirstChildNodeWithName(subjectNameNode, "value");
                SubjectName subjectName = this.buildSubjectName(valueNode);
                subjectNames.add(subjectName);
            }
        } catch (XPathHelperException ex) {
            // Just ignore here.
        }
    }

    /**
     *
     * @param subject
     * @param parameterListNode
     */
    private void setAddresses(Subject subject, OMElement parameterListNode) {
        try {
            List<Address> addresses = subject.getAddresses();
            List<OMElement> addressNodes = this.selectNodes(parameterListNode, XPATH_PARAMETER_ADDRESSES);
            for (OMElement addressNode : addressNodes) {
                OMElement valueNode = this.getFirstChildNodeWithName(addressNode, "value");
                Address address = this.buildAddress(valueNode);
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
    private void setPatientIdentifier(Subject subject, OMElement rootNode) {
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        OMElement valueNode = this.getFirstChildNodeWithName(rootNode, "value");
        SubjectIdentifier subjectIdentifier = this.buildSubjectIdentifier(valueNode);
        subjectIdentifiers.add(subjectIdentifier);
    }

    /**
     *
     * @param subject
     * @param parameterListNode
     */
    private void setIdentifiers(Subject subject, OMElement parameterListNode) {
        try {
            List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
            List<OMElement> idNodes = this.selectNodes(parameterListNode, XPATH_PARAMETER_IDS);
            for (OMElement idNode : idNodes) {
                OMElement valueNode = this.getFirstChildNodeWithName(idNode, "value");
                SubjectIdentifier subjectIdentifier = this.buildSubjectIdentifier(valueNode);
                subjectIdentifiers.add(subjectIdentifier);
            }
        } catch (XPathHelperException ex) {
            // TBD: ???
        }
    }

    /**
     *
     * @param subjectSearchCriteria
     * @param message
     */
    private void setScopingAssigningAuthoritiesFromDataSources(
            SubjectSearchCriteria subjectSearchCriteria,
            OMElement message) {
        try {
            List<OMElement> dataSourceNodes =
                    this.selectNodes(message, XPATH_DATA_SOURCES);
            for (OMElement dataSourceNode : dataSourceNodes) {
                OMElement valueNode = this.getFirstChildNodeWithName(dataSourceNode, "value");
                if (valueNode != null) {
                    // Pull out assigning authority.
                    String root = valueNode.getAttributeValue(new QName("root")); // Assigning Authority - required.
                    if (root != null) {
                        SubjectIdentifierDomain identifierDomain = new SubjectIdentifierDomain();
                        identifierDomain.setUniversalId(root);
                        //identifierDomain.setNamespaceId(assigningAuthorityName);
                        identifierDomain.setUniversalIdType("ISO"); // FIXME: FIXED VALUE?
                        subjectSearchCriteria.addScopingAssigningAuthority(identifierDomain);
                    }
                }
            }
        } catch (XPathHelperException ex) {
            // TBD: Do something.
        }
    }

    /**
     *
     * @param subjectSearchCriteria
     * @param message
     */
    private void setScopingAssigningAuthorities(
            SubjectSearchCriteria subjectSearchCriteria,
            OMElement message) {
        try {
            List<OMElement> otherIDsScopingOrganizationNodes =
                    this.selectNodes(message, XPATH_OTHER_IDS_SCOPING_ORGANIZATIONS);
            for (OMElement otherIDsScopingOrganizationNode : otherIDsScopingOrganizationNodes) {
                OMElement valueNode = this.getFirstChildNodeWithName(otherIDsScopingOrganizationNode, "value");
                if (valueNode != null) {
                    // Pull out assigning authority.
                    String root = valueNode.getAttributeValue(new QName("root")); // Assigning Authority - required.
                    if (root != null) {
                        SubjectIdentifierDomain identifierDomain = new SubjectIdentifierDomain();
                        identifierDomain.setUniversalId(root);
                        //identifierDomain.setNamespaceId(assigningAuthorityName);
                        identifierDomain.setUniversalIdType("ISO"); // FIXME: FIXED VALUE?
                        subjectSearchCriteria.addScopingAssigningAuthority(identifierDomain);
                    }
                }
            }
        } catch (XPathHelperException ex) {
            // TBD: Do something.
        }
    }

    /**
     *
     * @param subjectSearchCriteria
     * @param message
     */
    private void setCommunityAssigningAuthority(
            SubjectSearchCriteria subjectSearchCriteria,
            OMElement message) {
        try {
            // <authorOrPerformer typeCode="AUT">
            //   <assignedDevice classCode="ASSIGNED">
            //     <id root="1.2.840.114350.1.13.99997.2.3412"/>
            //   </assignedDevice>
            // </authorOrPerformer>
            OMElement idNode = this.selectSingleNode(message, XPATH_COMMUNITY_PATIENT_ID_ASSIGNING_AUTHORITY);
            if (idNode != null) {
                String assigningAuthority = idNode.getAttributeValue(new QName("root"));
                SubjectIdentifierDomain identifierDomain = new SubjectIdentifierDomain();
                identifierDomain.setUniversalId(assigningAuthority);
                identifierDomain.setUniversalIdType("ISO");
                subjectSearchCriteria.setCommunityAssigningAuthority(identifierDomain);
            }
        } catch (XPathHelperException ex) {
            // TBD: Do something.
        }
    }

    /**
     * 
     * @param subjectSearchCriteria
     * @param message
     */
    private void setMinimumDegreeMatchPercentage(SubjectSearchCriteria subjectSearchCriteria, OMElement message) {
        try {
            OMElement valueNode = this.selectSingleNode(message, XPATH_PARAMETER_MIN_DEGREE_MATCH_VALUE);
            if (valueNode != null) {
                String value = valueNode.getAttributeValue(new QName("value"));
                if (value != null) {
                    subjectSearchCriteria.setSpecifiedMinimumDegreeMatchPercentage(true);
                    subjectSearchCriteria.setMinimumDegreeMatchPercentage(new Integer(value));
                } else {
                    subjectSearchCriteria.setSpecifiedMinimumDegreeMatchPercentage(false);
                }
            }
        } catch (XPathHelperException ex) {
            subjectSearchCriteria.setSpecifiedMinimumDegreeMatchPercentage(false);
        }
    }
}
