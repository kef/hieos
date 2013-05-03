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
package com.vangent.hieos.policyutil.pip.model;

import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class PIPResponseBuilder {
 //           <pip:ConsentDirectives xmlns:pip="urn:hieos:policy:pip">
 //               <pip:PatientId>SC_1^^^&amp;1.3.6.1.4.1.21367.13.20.3000&amp;ISO</pip:PatientId>
 //               <pip:Status>Active</pip:Status>
 //               <pip:ConsentRules>
 //                       <!--Possible ConsentRule attributes:
 //                           purposeofuse (CNE_code_only)
 //                           organization (XON_id_only)
 //                           individual (XCN_id_only)
 //                           role (CNE_code_only)
 //                           documenttype (CNE_code_only)
 //                           documentid (OID)
 //                           homecommunityid (URN/OID)
 //                           confidentialitycode (CNE_code_only)-->
 //                   <pip:ConsentRule action="permit" documentid="129.6.58.92.193539"/>
 //                   <pip:ConsentRule action="permit" purposeofuse="TREATMENT"/>
 //               </pip:ConsentRules>
 //               <pip:ConsentOptions>
 //                   <pip:ConsentOption>ExplicitAuthorizationRequired</pip:ConsentOption>
 //               </pip:ConsentOptions>
 //           </pip:ConsentDirectives>

    /**
     *
     * @param pipResponseElement
     * @param buildDomainModel
     * @return
     */
    public PIPResponse buildPIPResponse(PIPResponseElement pipResponseElement, boolean buildDomainModel) {
        PIPResponse pipResponse = new PIPResponse();
        PatientConsentDirectives patientConsentDirectives = new PatientConsentDirectives();
        pipResponse.setPatientConsentDirectives(patientConsentDirectives);
        try {
            OMElement pipResponseNode = pipResponseElement.getElement();

            // ConsentDirectives
            OMElement consentDirectivesNode = XPathHelper.selectSingleNode(pipResponseNode,
                    "./ns:ConsentDirectives[1]", PolicyConstants.HIEOS_PIP_NS);
            patientConsentDirectives.setContent(consentDirectivesNode);
            if (buildDomainModel) {
                this.buildDomainModel(patientConsentDirectives, consentDirectivesNode);
            }
        } catch (XPathHelperException ex) {
            // FIXME: ? Do something ?
        }
        return pipResponse;
    }

    /**
     *
     * @param patientConsentDirectives
     * @param consentDirectivesNode
     */
    public void buildDomainModel(PatientConsentDirectives patientConsentDirectives, OMElement consentDirectivesNode) {

        try {
            // Get status for the patient.
            String status = XPathHelper.stringValueOf(consentDirectivesNode, "./ns:Status", PolicyConstants.HIEOS_PIP_NS);
            if (status.equalsIgnoreCase("active")) {
                patientConsentDirectives.setStatus(PatientConsentDirectives.StatusType.ACTIVE);
            } else if (status.equalsIgnoreCase("inactive")) {
                patientConsentDirectives.setStatus(PatientConsentDirectives.StatusType.INACTIVE);
            } else {
                patientConsentDirectives.setStatus(PatientConsentDirectives.StatusType.NOT_ESTABLISHED);
            }
            // Iterate through consent rule nodes and build PatientConsentRule instances.
            List<OMElement> consentRuleNodes = XPathHelper.selectNodes(consentDirectivesNode,
                    "./ns:ConsentRule", PolicyConstants.HIEOS_PIP_NS);
            for (OMElement consentRuleNode : consentRuleNodes) {
                // Get action associated with the consent rule.
                String action = consentRuleNode.getAttributeValue(new QName("action"));
                PatientConsentRule patientConsentRule = new PatientConsentRule();
                if (action.equalsIgnoreCase("permit")) {
                    patientConsentRule.setAction(PatientConsentRule.ActionType.PERMIT);
                } else {
                    patientConsentRule.setAction(PatientConsentRule.ActionType.DENY);
                }

                // Get purpose of use.
                String attributeValue = consentRuleNode.getAttributeValue(new QName("purposeofuse"));
                patientConsentRule.setPurposeOfUse(attributeValue);

                // Get organization.
                attributeValue = consentRuleNode.getAttributeValue(new QName("organization"));
                patientConsentRule.setOrganization(attributeValue);

                // Get individual.
                attributeValue = consentRuleNode.getAttributeValue(new QName("individual"));
                patientConsentRule.setIndividual(attributeValue);

                // Get role.
                attributeValue = consentRuleNode.getAttributeValue(new QName("role"));
                patientConsentRule.setRole(attributeValue);

                // Get documenttype.
                attributeValue = consentRuleNode.getAttributeValue(new QName("documenttype"));
                patientConsentRule.setDocumentType(attributeValue);

                // Get documentid.
                attributeValue = consentRuleNode.getAttributeValue(new QName("documentid"));
                patientConsentRule.setDocumentId(attributeValue);

                // Get homecommunityid.
                attributeValue = consentRuleNode.getAttributeValue(new QName("homecommunityid"));
                patientConsentRule.setHomeCommunityId(attributeValue);

                // Get confidentialitycode.
                attributeValue = consentRuleNode.getAttributeValue(new QName("confidentialitycode"));
                patientConsentRule.setConfidentialityCode(attributeValue);

                // Add the patient consent rule to patient consent directives.
                patientConsentDirectives.add(patientConsentRule);
            }
        } catch (XPathHelperException ex) {
            // FIXME: Do something.
        }
    }
}
