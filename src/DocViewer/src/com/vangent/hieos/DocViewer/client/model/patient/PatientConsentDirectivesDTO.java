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
package com.vangent.hieos.DocViewer.client.model.patient;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientConsentDirectivesDTO implements IsSerializable {
	public enum StatusType {
		ACTIVE, INACTIVE, NOT_ESTABLISHED
	};

	private String patientID;
	private StatusType status;
	private List<PatientConsentRuleDTO> patientConsentRules = new ArrayList<PatientConsentRuleDTO>();

	/**
	 * 
	 * @return
	 */
	public String getPatientID() {
		return patientID;
	}

	/**
	 * 
	 * @param patientID
	 */
	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	/**
	 * 
	 * @return
	 */
	public StatusType getStatus() {
		return status;
	}

	/**
	 * 
	 * @param status
	 */
	public void setStatus(StatusType status) {
		this.status = status;
	}

	/**
	 * 
	 * @return
	 */
	public List<PatientConsentRuleDTO> getPatientConsentRules() {
		return this.patientConsentRules;
	}

	/**
	 * 
	 * @param patientConsentRule
	 */
	public void add(PatientConsentRuleDTO patientConsentRule) {
		this.patientConsentRules.add(patientConsentRule);
	}
}
