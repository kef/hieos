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
import com.smartgwt.client.widgets.form.events.SubmitValuesEvent;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.types.DateDisplayFormat;
import com.smartgwt.client.widgets.form.fields.DateTimeItem;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.events.SubmitValuesHandler;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.config.ConfigDTO;
import com.vangent.hieos.DocViewer.client.model.patient.PatientSearchCriteriaDTO;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientSearchFormCanvas extends Canvas implements ClickHandler,
		SubmitValuesHandler {
	private DocViewerController controller;
	private final DynamicForm searchForm;
	private final TextItem familyNameField;
	private final TextItem givenNameField;
	private final DateTimeItem dateOfBirthField;
	private final RadioGroupItem genderGroupItem;
	private final TextItem ssnLast4Field;
	private final CheckboxItem fuzzyNameSearchCheckboxItem;
	private String showFuzzyNameSearch;

	// private final TextItem hrnField = new TextItem("hrn", "HRN");

	/**
	 * 
	 */
	public PatientSearchFormCanvas(DocViewerController mainController) {

		this.controller = mainController;

		ConfigDTO controllerConfig = this.controller.getConfig();

		this.searchForm = new DynamicForm();
		this.searchForm.setIsGroup(true);
		this.searchForm.setGroupTitle("<b>Patient Search Criteria</b>");
		this.searchForm.setWidth(300);
		this.searchForm.setSaveOnEnter(true);
		this.searchForm.addSubmitValuesHandler(this);

		// Family name:
		String familyNameLabel = controllerConfig
				.get(ConfigDTO.KEY_LABEL_FAMILY_NAME);
		this.familyNameField = new TextItem("familyName", familyNameLabel);
		this.familyNameField.setRequired(true);

		// Given name:
		String givenNameLabel = controllerConfig
				.get(ConfigDTO.KEY_LABEL_GIVEN_NAME);
		this.givenNameField = new TextItem("givenName", givenNameLabel);
		this.givenNameField.setRequired(true);

		// Date of birth:
		this.dateOfBirthField = new DateTimeItem("DOB", "Date of birth");
		dateOfBirthField.setDisplayFormat(DateDisplayFormat.TOUSSHORTDATE);
		this.dateOfBirthField.setRequired(false);

		// SSN:
		ssnLast4Field = new TextItem("ssn", "SSN");
		ssnLast4Field.setLength(9);
		ssnLast4Field.setMask("#########");
		ssnLast4Field.setRequired(false);

		// Gender:
		this.genderGroupItem = new RadioGroupItem();
		this.genderGroupItem.setVertical(false);
		this.genderGroupItem.setStartRow(false);
		this.genderGroupItem.setTitle("Gender");
		this.genderGroupItem.setValueMap("male", "female", "unspecified");
		this.genderGroupItem.setDefaultValue("unspecified");
		this.genderGroupItem.setRequired(false);

		/*
		 * // HRN: hrnField.setRequired(false);
		 */

		final ButtonItem btnFind = new ButtonItem("Find");
		btnFind.setIcon("find.png");
		btnFind.addClickHandler(this);
		btnFind.setEndRow(true);
		btnFind.setColSpan(2);
		btnFind.setAlign(Alignment.CENTER);

		// To enable "fuzzy searching" on name.
		this.fuzzyNameSearchCheckboxItem = new CheckboxItem();
		this.fuzzyNameSearchCheckboxItem.setName("fuzzyNameSearchCheckboxItem");
		this.fuzzyNameSearchCheckboxItem.setTitle("Fuzzy Name Search");
		this.fuzzyNameSearchCheckboxItem.setValue(true);

		this.showFuzzyNameSearch = controllerConfig.get(ConfigDTO.KEY_SHOW_FUZZY_NAME_SEARCH);
		
		// Check to display Fuzzy Name Search checkbox.
		if (this.showFuzzyNameSearch.equals("false"))
			// Search fields:
			this.searchForm.setFields(new FormItem[] { familyNameField,
					givenNameField, dateOfBirthField, ssnLast4Field,
					genderGroupItem, btnFind });
		else
			// Search fields:
			this.searchForm.setFields(new FormItem[] { fuzzyNameSearchCheckboxItem, familyNameField,
					givenNameField, dateOfBirthField, ssnLast4Field,
					genderGroupItem, btnFind });

		// Now, lay it out.
		final VLayout layout = new VLayout();

		// layout.setShowEdges(true);
		// layout.setEdgeSize(3);
		layout.addMember(searchForm);

		addChild(layout);
	}

	/**
	 * 
	 */
	@Override
	public void onClick(ClickEvent event) {
		this.searchForm.submit();
	}

	/**
	 * 
	 */
	@Override
	public void onSubmitValues(SubmitValuesEvent event) {
		boolean validatedOk = searchForm.validate();
		if (validatedOk == true) {
			// Pull values off of form.
			PatientSearchCriteriaDTO criteria = this.getPatientSearchCriteria();
			// Conduct the search.
			controller.findPatients(criteria);
		}
	}

	/**
	 * 
	 * @return
	 */
	private PatientSearchCriteriaDTO getPatientSearchCriteria() {
		PatientSearchCriteriaDTO criteria = new PatientSearchCriteriaDTO();
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
		// criteria.setHealthRecordNumber(hrnField.getValueAsString());

		// SSN(last4):
		criteria.setSsnLast4(ssnLast4Field.getValueAsString());

		// Check if there was a Fuzzy Name Search checkbox.
		if (showFuzzyNameSearch.equals("false"))
			criteria.setFuzzyNameSearch(false);
		else
			criteria.setFuzzyNameSearch(fuzzyNameSearchCheckboxItem.getValueAsBoolean());

		return criteria;
	}

}
