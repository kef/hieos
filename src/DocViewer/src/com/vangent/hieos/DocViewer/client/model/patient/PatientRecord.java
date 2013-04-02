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

import com.google.gwt.i18n.client.DateTimeFormat;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientRecord extends ListGridRecord {
	static public final String DOB_FIELD = "dob";
	static public final String GENDER_FIELD = "gender";
	static public final String FAMILY_NAME_FIELD = "family_name";
	static public final String GIVEN_NAME_FIELD = "given_name";
	/* [03/14/13]  IHS Release 1.3 (Requirement # 7333 - Middle Name updates in DocViewer) */
	static public final String MIDDLE_NAME_FIELD = "middle_name";
	static public final String EUID_FIELD = "euid";
	static public final String SSN_FIELD = "ssn";
	static public final String MATCH_CONFIDENCE_FIELD = "match_confidence";

	
	private Patient patient;

	/**
	 * 
	 * @param patient
	 */
	public PatientRecord(Patient patient) {
		this.patient = patient;
		// To allow grouping/sorting:
		setAttribute(DOB_FIELD, patient.getDateOfBirth());
		setAttribute(GENDER_FIELD, this.getFormattedGender());
		setAttribute(FAMILY_NAME_FIELD, this.getFormattedFamilyName());
		setAttribute(GIVEN_NAME_FIELD, patient.getGivenName());
		/* [03/14/13] IHS Release 1.3 (Requirement # 7333 - Middle Name updates in DocViewer) */
		setAttribute(MIDDLE_NAME_FIELD, patient.getMiddleName());
		setAttribute(EUID_FIELD, patient.getEuid());
		setAttribute("ssn", patient.getSSN());
		setAttribute("match_confidence", patient.getMatchConfidencePercentage());
	}

	/**
	 * 
	 * @return
	 */
	public Patient getPatient() {
		return this.patient;
	}

	/**
	 * 
	 */
	public String getFormattedName() {
		if (patient.getFamilyName() != null && patient.getGivenName() != null) {
			return patient.getFamilyName().toUpperCase() + ", "
					+ patient.getGivenName();
		}
		// Use PID if no name is given ...
		return patient.getEuid();
	}

	/**
	 * 
	 * @return
	 */
	public String getFormattedGender() {
		String gender = patient.getGender();
		if (gender != null) {
			if (gender.equalsIgnoreCase("M"))
				return "Male";
			if (gender.equalsIgnoreCase("F"))
				return "Female";
		}
		return "UNKNOWN";
	}

	/**
	 * 
	 * @return
	 */
	public String getFormattedFamilyName() {
		String familyName = patient.getFamilyName();
		if (familyName != null) {
			return familyName.toUpperCase();
		} else {
			return "";
		}
	}

	/**
	 * 
	 * @return
	 */
	public String getFormattedDateOfBirth() {
		Date birthDate = patient.getDateOfBirth();
		if (birthDate != null) {
			DateTimeFormat dateFormatter = DateTimeFormat
					.getFormat("dd-MMM-yyyy");
			try {
				return dateFormatter.format((Date) birthDate);
			} catch (Exception e) {
				return birthDate.toString();
			}
		}
		return "UNKNOWN";
	}
}
