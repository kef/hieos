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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientConsentRuleDTO implements IsSerializable {
	/**
    *
    */
	public enum ActionType {

		/**
        * 
        */
		PERMIT,
		/**
        * 
        */
		DENY
	};

	private ActionType action;
	private String purposeOfUse;
	private String organization;
	private String individual;
	private String role;
	private String documentType;
	private String documentId;
	private String homeCommunityId;
	private String confidentialityCode;

	/**
	 * 
	 * @return
	 */
	public ActionType getAction() {
		return action;
	}

	/**
	 * 
	 * @param action
	 */
	public void setAction(ActionType action) {
		this.action = action;
	}

	/**
	 * 
	 * @return
	 */
	public String getPurposeOfUse() {
		return purposeOfUse;
	}

	/**
	 * 
	 * @param purposeOfUse
	 */
	public void setPurposeOfUse(String purposeOfUse) {
		this.purposeOfUse = purposeOfUse;
	}

	/**
	 * 
	 * @return
	 */
	public String getConfidentialityCode() {
		return confidentialityCode;
	}

	/**
	 * 
	 * @param confidentialityCode
	 */
	public void setConfidentialityCode(String confidentialityCode) {
		this.confidentialityCode = confidentialityCode;
	}

	/**
	 * 
	 * @return
	 */
	public String getDocumentId() {
		return documentId;
	}

	/**
	 * 
	 * @param documentId
	 */
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	/**
	 * 
	 * @return
	 */
	public String getDocumentType() {
		return documentType;
	}

	/**
	 * 
	 * @param documentType
	 */
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	/**
	 * 
	 * @return
	 */
	public String getHomeCommunityId() {
		return homeCommunityId;
	}

	/**
	 * 
	 * @param homeCommunityId
	 */
	public void setHomeCommunityId(String homeCommunityId) {
		this.homeCommunityId = homeCommunityId;
	}

	/**
	 * 
	 * @return
	 */
	public String getIndividual() {
		return individual;
	}

	/**
	 * 
	 * @param individual
	 */
	public void setIndividual(String individual) {
		this.individual = individual;
	}

	/**
	 * 
	 * @return
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * 
	 * @param organization
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * 
	 * @return
	 */
	public String getRole() {
		return role;
	}

	/**
	 * 
	 * @param role
	 */
	public void setRole(String role) {
		this.role = role;
	}
}
