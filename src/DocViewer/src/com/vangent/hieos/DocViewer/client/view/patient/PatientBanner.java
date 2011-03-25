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
package com.vangent.hieos.DocViewer.client.view.patient;

import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.vangent.hieos.DocViewer.client.model.patient.Patient;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;

/**
 * 
 * @author Bernie Thuman
 *
 */
public class PatientBanner extends Canvas {
	private final Label name;
	private final Label dob;
	private final Label gender;
	private final Label euid;
	private final Label ssn;

	/**
	 * 
	 */
	public PatientBanner() {
		final ToolStrip topBar = new ToolStrip();
		topBar.setHeight(20);
		topBar.setWidth100();
		topBar.addSpacer(5);
		topBar.setAlign(VerticalAlignment.CENTER);

		name = new Label("Patient Name");
		dob = new Label("DOB");
		dob.setWidth(150);

		gender = new Label("Gender");

		euid = new Label("EUID");
		ssn = new Label("SSN");
		ssn.setWidth(60);

		topBar.addMember(name);
		topBar.addSeparator();
		topBar.addMember(dob);
		topBar.addSeparator();
		topBar.addMember(gender);
		topBar.addSeparator();
		topBar.addMember(ssn);
		topBar.addSeparator();
		topBar.addMember(euid);

		topBar.addFill();
		addChild(topBar);
	}

	/**
	 * 
	 * @param patientRecord
	 */
	public void update(PatientRecord patientRecord) {
		Patient patient = patientRecord.getPatient();
		if (patient.getFamilyName() != null && patient.getGivenName() != null) {
			name.setContents("<b>" + patientRecord.getFormattedName() + "</b>");
		} else {
			// Just show EUID instead of name.
			name.setContents("<b>" + patient.getEuid() + "</b>");
		}
		dob.setContents("<i>Born:</i>&nbsp;" + "<b>"
				+ patientRecord.getFormattedDateOfBirth() + "&nbsp;(ddy)</b>");
		gender.setContents("<i>Gender:</i>&nbsp;" + "<b>"
				+ patientRecord.getFormattedGender() + "</b>");
		ssn.setContents("<i>SSN:</i>&nbsp;" + "<b>"
				+ patientRecord.getPatient().getSSN() + "</b>");
		euid.setContents("<i>EUID:</i>&nbsp;" + "<b>"
				+ patientRecord.getPatient().getEuid() + "</b>");
	}
}
