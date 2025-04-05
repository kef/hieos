/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.DocViewer.server.services.rpc.pip;

import com.vangent.hieos.DocViewer.client.model.patient.PatientConsentDirectivesDTO;
import com.vangent.hieos.DocViewer.client.model.patient.PatientConsentRuleDTO;
import com.vangent.hieos.policyutil.pip.model.PatientConsentDirectives;
import com.vangent.hieos.policyutil.pip.model.PatientConsentRule;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientConsentDirectivesTransform {

	/**
	 * 
	 * @param patientConsentDirectives
	 * @return
	 */
	public static PatientConsentDirectivesDTO transform(
			PatientConsentDirectives patientConsentDirectives) {
		PatientConsentDirectivesDTO patientConsentDirectivesDTO = new PatientConsentDirectivesDTO();

		// Set status.
		switch (patientConsentDirectives.getStatus()) {
		case ACTIVE:
			patientConsentDirectivesDTO
					.setStatus(PatientConsentDirectivesDTO.StatusType.ACTIVE);
			break;
		case INACTIVE:
			patientConsentDirectivesDTO
					.setStatus(PatientConsentDirectivesDTO.StatusType.INACTIVE);
			break;
		case NOT_ESTABLISHED:
			patientConsentDirectivesDTO
					.setStatus(PatientConsentDirectivesDTO.StatusType.NOT_ESTABLISHED);
		default:
			break;
		}

		// Now, the consent rules.
		for (PatientConsentRule patientConsentRule : patientConsentDirectives
				.getPatientConsentRules()) {
			PatientConsentRuleDTO patientConsentRuleDTO = new PatientConsentRuleDTO();
			// action.
			if (patientConsentRule.getAction() == PatientConsentRule.ActionType.PERMIT) {
				patientConsentRuleDTO
						.setAction(PatientConsentRuleDTO.ActionType.PERMIT);
			} else {
				patientConsentRuleDTO
						.setAction(PatientConsentRuleDTO.ActionType.DENY);
			}

			// TODO: Implement other types ... POU, role, etc.
			/** Similar to as shown below
			patientConsentRuleDTO.setOrganization(patientConsentRule.getOrganization());
			patientConsentRuleDTO.setPurposeOfUse(patientConsentRule.getPurposeOfUse());
			patientConsentRuleDTO.setConfidentialityCode(patientConsentRule.getConfidentialityCode());
			patientConsentRuleDTO.setDocumentId(patientConsentRule.getDocumentId());
			patientConsentRuleDTO.setDocumentType(patientConsentRule.getDocumentType());
			patientConsentRuleDTO.setHomeCommunityId(patientConsentRule.getHomeCommunityId());
			patientConsentRuleDTO.setIndividual(patientConsentRule.getIndividual());
			patientConsentRuleDTO.setRole(patientConsentRule.getRole());
			*/
			
			patientConsentDirectivesDTO.add(patientConsentRuleDTO);
		}
		return patientConsentDirectivesDTO;

	}

	/**
	 * 
	 * @param patientConsentDirectivesDTO
	 * @return
	 */
	public static PatientConsentDirectives transform(
			PatientConsentDirectivesDTO patientConsentDirectivesDTO) {
		// TODO: Implement (on save).
		return null;

	}

}
