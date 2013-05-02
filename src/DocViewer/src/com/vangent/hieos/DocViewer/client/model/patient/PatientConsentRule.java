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
public class PatientConsentRule implements IsSerializable {
	public final static String DENY = "DENY";
	public final static String PERMIT = "PERMIT";

	private String action; // PERMIT or DENY.
	private String purposeOfUse;
	private String role;
	private String organizationID;
	private String individualID;
	private String documentType;
	private String documentID;
	private String homeCommunityID;
	private String confidentialityCode;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getPurposeOfUse() {
		return purposeOfUse;
	}

	public void setPurposeOfUse(String purposeOfUse) {
		this.purposeOfUse = purposeOfUse;
	}

	public String getOrganizationID() {
		return organizationID;
	}

	public void setOrganizationID(String organizationID) {
		this.organizationID = organizationID;
	}

	public String getIndividualID() {
		return individualID;
	}

	public void setIndividualID(String individualID) {
		this.individualID = individualID;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getDocumentID() {
		return documentID;
	}

	public void setDocumentID(String documentID) {
		this.documentID = documentID;
	}

	public String getHomeCommunityID() {
		return homeCommunityID;
	}

	public void setHomeCommunityID(String homeCommunityID) {
		this.homeCommunityID = homeCommunityID;
	}

	public String getConfidentialityCode() {
		return confidentialityCode;
	}

	public void setConfidentialityCode(String confidentialityCode) {
		this.confidentialityCode = confidentialityCode;
	}
}
