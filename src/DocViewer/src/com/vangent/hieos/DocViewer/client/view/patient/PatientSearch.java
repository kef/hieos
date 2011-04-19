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

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.types.DateDisplayFormat;
import com.smartgwt.client.widgets.form.fields.DateTimeItem;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.patient.PatientSearchCriteria;

/**
 * 
 * @author Bernie Thuman
 *
 */
public class PatientSearch extends Canvas implements ClickHandler {
	private DocViewerController controller;
	private final DynamicForm searchForm;
	private final TextItem familyNameField = new TextItem("familyName",
			"Family name");
	private final TextItem givenNameField = new TextItem("givenName",
			"Given name");
	private final DateTimeItem dateOfBirthField = new DateTimeItem("DOB",
			"Date of birth");
	private final RadioGroupItem genderGroupItem = new RadioGroupItem();
	//private final TextItem ssnLast4Field = new TextItem("ssn", "SSN(last 4)");
	//private final TextItem hrnField = new TextItem("hrn", "HRN");

	/**
	 * 
	 */
	public PatientSearch(DocViewerController mainController) {
		this.controller = mainController;

		this.searchForm = new DynamicForm();
		searchForm.setWidth(300);

		// Family name:
		familyNameField.setRequired(true);

		// Given name:
		givenNameField.setRequired(true);

		// Date of birth:
		dateOfBirthField.setDisplayFormat(DateDisplayFormat.TOUSSHORTDATE);
		dateOfBirthField.setRequired(false);

		// Gender:
		genderGroupItem.setVertical(false);
		genderGroupItem.setStartRow(false);
		genderGroupItem.setTitle("Gender");
		genderGroupItem.setValueMap("male", "female", "unspecified");
		genderGroupItem.setDefaultValue("unspecified");
		genderGroupItem.setRequired(false);

		/*
		// HRN:
		hrnField.setRequired(false);

		// SSN:
		ssnLast4Field.setLength(4);
		ssnLast4Field.setWidth(40);
		ssnLast4Field.setRequired(false);
		*/

		// Search fields:
		searchForm.setFields(new FormItem[] { familyNameField, givenNameField,
				dateOfBirthField, genderGroupItem });
		
		//searchForm.setFields(new FormItem[] { familyNameField, givenNameField,
		//		dateOfBirthField, genderGroupItem, hrnField, ssnLast4Field });

		final IButton btnFind = new IButton("Find");
		btnFind.setIcon("find.png");
		btnFind.addClickHandler(this);
		btnFind.setLayoutAlign(Alignment.CENTER);
		
		// Now, lay it out.
		final VLayout layout = new VLayout();
		layout.setShowEdges(true);
		layout.setEdgeSize(3);
		layout.addMember(searchForm);
		layout.addMember(btnFind);

		final LayoutSpacer spacer = new LayoutSpacer();
		spacer.setHeight(3);
		layout.addMember(spacer);
		addChild(layout);
	}

	/**
	 * 
	 */
	@Override
	public void onClick(ClickEvent event) {
		boolean validatedOk = searchForm.validate();
		if (validatedOk == true) {
			// Pull values off of form.
			PatientSearchCriteria criteria = this.getPatientSearchCriteria();
			// Conduct the search.
			controller.findPatients(criteria);
		}
	}

	/**
	 * 
	 * @return
	 */
	private PatientSearchCriteria getPatientSearchCriteria() {
		PatientSearchCriteria criteria = new PatientSearchCriteria();
		// Name:
		criteria.setFamilyName(familyNameField.getValueAsString());
		criteria.setGivenName(givenNameField.getValueAsString());

		// DOB:
		criteria.setDateOfBirth(dateOfBirthField.getValueAsDate());

		// Gender Code:
		String genderCode = genderGroupItem.getValueAsString();
		if (genderCode.equals("male")) {
			genderCode = "M";
		} else if (genderCode.equals("female")) {
			genderCode = "F";
		} else {
			genderCode = "UN";
		}
		criteria.setGenderCode(genderCode);

		// HRN:
		//criteria.setHealthRecordNumber(hrnField.getValueAsString());

		// SSN(last4):
		//criteria.setSsnLast4(ssnLast4Field.getValueAsString());
		return criteria;
	}
	

}
