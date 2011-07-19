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

import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class PIPResponseBuilder {
//<pip:GetConsentDirectivesResponse xsi:schemaLocation="urn:hieos:policy:pip PIP.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:pip="urn:hieos:policy:pip">
//	<pip:ConsentDirectives alwaysAuthorize="false">
//		<pip:AllowedOrganizations>
//			<pip:Organization>String</pip:Organization>
//			<pip:Organization>String</pip:Organization>
//		</pip:AllowedOrganizations>
//		<pip:BlockedOrganizations>
//			<pip:Organization>String</pip:Organization>
//			<pip:Organization>String</pip:Organization>
//		</pip:BlockedOrganizations>
//		<pip:BlockedIndividuals>
//			<pip:Individual>String</pip:Individual>
//			<pip:Individual>String</pip:Individual>
//		</pip:BlockedIndividuals>
//		<pip:AllowedRoles>
//			<pip:Role codeSystem="String" displayName="String" codeSystemName="String" code="String"/>
//			<pip:Role codeSystem="String" displayName="String" codeSystemName="String" code="String"/>
//		</pip:AllowedRoles>
//		<pip:AllowedPurposeOfUse>
//			<pip:PurposeOfUse codeSystem="String" displayName="String" codeSystemName="String" code="String"/>
//			<pip:PurposeOfUse codeSystem="String" displayName="String" codeSystemName="String" code="String"/>
//		</pip:AllowedPurposeOfUse>
//		<pip:SensitiveDocumentTypes>
//			<pip:DocumentType codeSystem="String" displayName="String" codeSystemName="String" code="String"/>
//			<pip:DocumentType codeSystem="String" displayName="String" codeSystemName="String" code="String"/>
//		</pip:SensitiveDocumentTypes>
//		<pip:SensitiveDocumentAccessList>
//			<pip:SensitiveDocumentAccess>
//				<pip:Organization>String</pip:Organization>
//				<pip:Individual>String</pip:Individual>
//			</pip:SensitiveDocumentAccess>
//			<pip:SensitiveDocumentAccess>
//				<pip:Organization>String</pip:Organization>
//				<pip:Individual>String</pip:Individual>
//			</pip:SensitiveDocumentAccess>
//		</pip:SensitiveDocumentAccessList>
//	</pip:ConsentDirectives>
//</pip:GetConsentDirectivesResponse>

    /**
     * 
     * @param pipResponseElement
     * @return
     */
    public PIPResponse buildPIPResponse(PIPResponseElement pipResponseElement) {
        // FIXME: Do not hard-wire
        String nsURI = "urn:hieos:policy:pip";
        PIPResponse pipResponse = new PIPResponse();
        PatientConsentDirectives patientConsentDirectives = new PatientConsentDirectives();
        pipResponse.setPatientConsentDirectives(patientConsentDirectives);
        try {
            OMElement pipResponseNode = pipResponseElement.getElement();

            // ConsentDirectives
            OMElement consentDirectivesNode = XPathHelper.selectSingleNode(pipResponseNode,
                    "./ns:ConsentDirectives[1]", nsURI);
            patientConsentDirectives.setContent(consentDirectivesNode);

            // TBD: Do more ... e.g. SensitiveDocumentTypes, etc.

        } catch (XPathHelperException ex) {
            // FIXME: ? Do something ?
        }
        return pipResponse;
    }
}
