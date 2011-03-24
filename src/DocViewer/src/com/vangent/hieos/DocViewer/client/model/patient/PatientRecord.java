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
	private Patient patient;

	/**
	 * 
	 * @param patient
	 */
	public PatientRecord(Patient patient) {
		this.patient = patient;
		// To allow grouping/sorting:
		setAttribute("dob", patient.getDateOfBirth());
		setAttribute("gender", this.getFormattedGender());
		setAttribute("family_name", this.getFormattedFamilyName());
		setAttribute("given_name", patient.getGivenName());
		setAttribute("euid", patient.getEuid());
		setAttribute("ssn", patient.getSSN());
		setAttribute("match_confidence_percentage", patient.getMatchConfidencePercentage());
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
