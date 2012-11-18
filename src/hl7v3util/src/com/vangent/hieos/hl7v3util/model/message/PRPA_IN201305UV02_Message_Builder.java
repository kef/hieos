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

import com.vangent.hieos.subjectmodel.Address;
import com.vangent.hieos.subjectmodel.CodedValue;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.subjectmodel.SubjectName;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import java.util.Date;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

/**
 *
 * @author Bernie Thuman
 */
public class PRPA_IN201305UV02_Message_Builder extends HL7V3MessageBuilderHelper {

    /**
     * 
     */
    private PRPA_IN201305UV02_Message_Builder() {
    }

    /**
     *
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     */
    public PRPA_IN201305UV02_Message_Builder(DeviceInfo senderDeviceInfo, DeviceInfo receiverDeviceInfo) {
        super(senderDeviceInfo, receiverDeviceInfo);
    }

    /**
     * 
     * @param subjectSearchCriteria
     * @param sendCommunityPatientId
     * @return
     */
    public PRPA_IN201305UV02_Message buildPRPA_IN201305UV02_Message(
            SubjectSearchCriteria subjectSearchCriteria) {

        String messageName = "PRPA_IN201305UV02";

        // PRPA_IN201305UV02
        OMElement requestNode = this.getRequestNode(messageName, "T", "T", "AL");

        // PRPA_IN201305UV02/controlActProcess
        OMElement controlActProcessNode = this.addControlActProcess(requestNode, "PRPA_TE201305UV02");

        // PRPA_IN201305UV02/controlActProcess/authorOrPerformer
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

        // PRPA_IN201305UV02/controlActProcess/queryAck
        //this.addQueryAck(requestNode, controlActProcessNode, subjects, errorText);

        // PRPA_IN201305UV02/controlActProcess/queryByParameter
        OMElement queryByParameterNode =
                this.addQueryByParameter(controlActProcessNode,
                subjectSearchCriteria);

        // TBD ... queryByParameter ... this was on the request .... not valid in this case.
        // PRPA_IN201305UV02/controlActProcess/queryByParameter
        //this.addQueryByParameter(requestNode, controlActProcessNode);

        return new PRPA_IN201305UV02_Message(requestNode);
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

        // PRPA_IN201305UV02/controlActProcess/queryByParameter
        OMElement queryByParameterNode = this.addChildOMElement(controlActProcessNode, "queryByParameter");

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/queryId
        OMElement queryIdNode = this.addChildOMElement(queryByParameterNode, "queryId");
        this.setAttribute(queryIdNode, "root", "1.2.840.114350.1.13.28.1.18.5.999");  // FIXME: ???
        this.setAttribute(queryIdNode, "extension", "18204");  // FIXME: ???

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/statusCode
        this.addCode(queryByParameterNode, "statusCode", "new");

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/responseModalityCode
        this.addCode(queryByParameterNode, "responseModalityCode", "R");

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/responsePriorityCode
        this.addCode(queryByParameterNode, "responsePriorityCode", "I");

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/matchCriterionList     
        this.addMatchCriterionList(queryByParameterNode, subjectSearchCriteria);

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList
        this.addParameterList(queryByParameterNode, subjectSearchCriteria);

        return queryByParameterNode;
    }

    /**
     *
     * @param requestNode
     * @param subjectSearchCriteria
     * @return
     */
    private OMElement addMatchCriterionList(
            OMElement rootNode,
            SubjectSearchCriteria subjectSearchCriteria) {
        OMElement matchCriterionListNode = null;
        // Only include "matchCriterionList" if flagged to do so.
        if (subjectSearchCriteria.hasSpecifiedMinimumDegreeMatchPercentage()) {
            // <matchCriterionList>
            //   <minimumDegreeMatch>
            //     <value xsi:type="INT" value="75"/>
            //     <semanticsText>Degree of match requested</semanticsText>
            //   </minimumDegreeMatch>
            // </matchCriterionList>

            // PRPA_IN201305UV02/controlActProcess/queryByParameter/matchCriterionList
            matchCriterionListNode = this.addChildOMElement(rootNode, "matchCriterionList");

            // PRPA_IN201305UV02/controlActProcess/queryByParameter/matchCriterionList/minimumDegreeMatch
            OMElement minimumDegreeMatchNode = this.addChildOMElement(matchCriterionListNode, "minimumDegreeMatch");

            // PRPA_IN201305UV02/controlActProcess/queryByParameter/matchCriterionList/minimumDegreeMatch/value
            OMElement valueNode = this.addChildOMElement(minimumDegreeMatchNode, "value");
            OMNamespace xsiNS = this.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
            valueNode.addAttribute("type", "INT", xsiNS);
            this.setAttribute(valueNode, "value", new Integer(subjectSearchCriteria.getMinimumDegreeMatchPercentage()).toString());

            // PRPA_IN201305UV02/controlActProcess/queryByParameter/matchCriterionList/minimumDegreeMatch/semanticsText
            OMElement semanticsTextNode = this.addChildOMElementWithValue(minimumDegreeMatchNode, "semanticsText", "Degree of match requested");
        }
        return matchCriterionListNode;
    }

    /**
     *
     * @param requestNode
     * @param subjectSearchCriteria
     * @return
     */
    private OMElement addParameterList(
            OMElement rootNode,
            SubjectSearchCriteria subjectSearchCriteria) {
        Subject searchSubject = subjectSearchCriteria.getSubject();

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList
        OMElement parameterListNode = this.addChildOMElement(rootNode, "parameterList");

        // Now add parameters.

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList/livingSubjectAdministrativeGender
        //  <livingSubjectAdministrativeGender>
        //    <value code="M"/>
        //    <semanticsText>LivingSubject.administrativeGender</semanticsText>
        CodedValue subjectGender = searchSubject.getGender();
        if (subjectGender != null) {
            OMElement genderNode = this.addChildOMElement(parameterListNode, "livingSubjectAdministrativeGender");
            this.addCode(genderNode, "value", subjectGender.getCode());
            this.addChildOMElementWithValue(genderNode, "semanticsText", "LivingSubject.administrativeGender");
        }

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList/livingSubjectBirthTime
        // <livingSubjectBirthTime>
        //   <value value="19640610"/>
        //   <semanticsText>LivingSubject.birthTime</semanticsText>
        // </livingSubjectBirthTime>-->
        Date birthTime = searchSubject.getBirthTime();
        if (birthTime != null) {
            OMElement birthTimeNode = this.addChildOMElement(parameterListNode, "livingSubjectBirthTime");
            this.addValue(birthTimeNode, "value", Hl7Date.toHL7format(birthTime));
            this.addChildOMElementWithValue(birthTimeNode, "semanticsText", "LivingSubject.birthTime");
        }

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList/livingSubjectId
        // <livingSubjectId>
        //   <value root="1.3.6.1.4.1.21367.13.20.2000" extension="GREEN_GLOBAL_05"/>
        //   <semanticsText>LivingSubject.id</semanticsText>
        // </livingSubjectId>
        //String communityAssigningAuthority = subjectSearchCriteria.getCommunityAssigningAuthority();
        for (SubjectIdentifier subjectIdentifier : searchSubject.getSubjectIdentifiers()) {
            //String assigningAuthority = subjectIdentifier.getIdentifierDomain().getUniversalId();
            //if (sendCommunityPatientId == true ||
            //        !assigningAuthority.equals(communityAssigningAuthority)) {
            OMElement subjectIdNode = this.addChildOMElement(parameterListNode, "livingSubjectId");
            OMElement valueNode = this.addChildOMElement(subjectIdNode, "value");
            this.addSubjectIdentifier(valueNode, subjectIdentifier);
            this.addChildOMElementWithValue(subjectIdNode, "semanticsText", "LivingSubject.id");
            //}
        }

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList/livingSubjectName
        // <livingSubjectName>
        //   <value>
        //     <given>John</given>
        //     <family>Jones</family>
        //   </value>
        //   <semanticsText>LivingSubject.name</semanticsText>
        // </livingSubjectName>-->
        for (SubjectName subjectName : searchSubject.getSubjectNames()) {
            OMElement subjectNameNode = this.addChildOMElement(parameterListNode, "livingSubjectName");
            OMElement valueNode = this.addChildOMElement(subjectNameNode, "value");
            if (subjectName.isFuzzySearchMode()) {
                this.setAttribute(valueNode, "use", "SRCH");
            }
            this.addSubjectName(valueNode, subjectName);
            this.addChildOMElementWithValue(subjectNameNode, "semanticsText", "LivingSubject.name");
        }



        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList/otherIDsScopingOrganization
        // <otherIDsScopingOrganization>
        //    <value root="1.2.840.114350.1.13.99997.2.3412"/>
        //    <semanticsText>OtherIDs.scopingOrganization.id</semanticsText>
        // </otherIDsScopingOrganization>
        for (SubjectIdentifierDomain subjectIdentifierDomain : subjectSearchCriteria.getScopingAssigningAuthorities()) {
            String assigningAuthority = subjectIdentifierDomain.getUniversalId();
            OMElement otherIDsScopingOrganizationNode = this.addChildOMElement(parameterListNode, "otherIDsScopingOrganization");
            OMElement valueNode = this.addChildOMElement(otherIDsScopingOrganizationNode, "value");
            this.setAttribute(valueNode, "root", assigningAuthority);
            this.addChildOMElementWithValue(otherIDsScopingOrganizationNode, "semanticsText", "OtherIDs.scopingOrganization.id");
        }

        // Patient Address.
        // <patientAddress>
        //   <value>
        //      <streetAddressLine>20 Point Street</streetAddressLine>
        //      <city>CHICAGO</city>
        //      <state>ILL</state>
        //      <postalCode>60606</postalCode>
        //   </value>
        //   <semanticsText>Patient.addr</semanticsText>
        // </patientAddress>
        for (Address address : searchSubject.getAddresses()) {
            OMElement subjectAddressNode = this.addChildOMElement(parameterListNode, "patientAddress");
            OMElement valueNode = this.addChildOMElement(subjectAddressNode, "value");
            this.addAddress(valueNode, address);
            this.addChildOMElementWithValue(subjectAddressNode, "semanticsText", "Patient.addr");
        }

        // FIXME: How about other parameters????
        return parameterListNode;
    }
}
