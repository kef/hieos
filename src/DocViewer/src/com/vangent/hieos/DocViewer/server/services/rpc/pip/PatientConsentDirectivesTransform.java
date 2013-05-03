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
import com.vangent.hieos.policyutil.pip.model.PatientConsentDirectives;

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
		/*switch (patientConsentDirectives.getStatus())
		{
		case com.vangent.hieos.policyutil.pip.model.PatientConsentDirectives.StatusType.
		}
		patientConsentDirectivesX.setStatus(patientConsentDirectives.getStatus());*/
		return patientConsentDirectivesDTO;

	}

	/**
	 * 
	 * @param patientConsentDirectivesDTO
	 * @return
	 */
	public static PatientConsentDirectives transform(
			PatientConsentDirectivesDTO patientConsentDirectivesDTO) {
		return null;

	}

}
