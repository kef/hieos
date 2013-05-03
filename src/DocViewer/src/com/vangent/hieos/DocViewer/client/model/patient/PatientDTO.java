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
package com.vangent.hieos.DocViewer.client.model.patient;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Bernie Thuman
 *
 */
public class PatientDTO implements IsSerializable {
	private String familyName;
	private String givenName;
	/* [03/14/13]  IHS Release 1.3 (Requirement # 7333 - Middle Name updates in DocViewer) */
	private String middleName;
	private String euid;
	private Date dateOfBirth;
	private String gender;
	private String euidUniversalID;
	private String ssn;
	private String hrn;
	private int matchConfidencePercentage;

	public String getPatientID() {
		return this.getEuid() + "^^^&amp;" + this.getEuidUniversalID()
				+ "&amp;ISO";
	}

	public String getEuidUniversalID() {
		return euidUniversalID;
	}

	public void setEuidUniversalID(String euidUniversalID) {
		this.euidUniversalID = euidUniversalID;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/* [03/14/13]  IHS Release 1.3 (Requirement # 7333 - Middle Name updates in DocViewer) */
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getEuid() {
		return euid;
	}

	public void setEuid(String euid) {
		this.euid = euid;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public void setSSN(String ssn) {
		this.ssn = ssn;
	}

	public String getSSN() {
		return ssn;
	}

	public void setHRN(String hrn) {
		this.hrn = hrn;
	}

	public String getHRN() {
		return hrn;
	}

	public void setMatchConfidencePercentage(int matchConfidencePercentage) {
		this.matchConfidencePercentage = matchConfidencePercentage;
	}

	public int getMatchConfidencePercentage() {
		return matchConfidencePercentage;
	}

}
