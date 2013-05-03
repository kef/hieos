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
public class PatientSearchCriteria implements IsSerializable {

	private String givenName;
	private String familyName;
	private Date dateOfBirth;
	private String genderCode;
	private String healthRecordNumber;
	private String ssnLast4;
	private boolean fuzzyNameSearch;

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getGenderCode() {
		return genderCode;
	}

	public void setGenderCode(String genderCode) {
		this.genderCode = genderCode;
	}

	public String getHealthRecordNumber() {
		return healthRecordNumber;
	}

	public void setHealthRecordNumber(String healthRecordNumber) {
		this.healthRecordNumber = healthRecordNumber;
	}

	public String getSsnLast4() {
		return ssnLast4;
	}

	public void setSsnLast4(String ssnLast4) {
		this.ssnLast4 = ssnLast4;
	}

	public boolean isFuzzyNameSearch() {
		return fuzzyNameSearch;
	}

	public void setFuzzyNameSearch(boolean fuzzyNameSearch) {
		this.fuzzyNameSearch = fuzzyNameSearch;
	}

}
