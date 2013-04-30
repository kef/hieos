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
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.SortDirection;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.config.Config;
import com.vangent.hieos.DocViewer.client.model.patient.Patient;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientListCanvas extends Canvas implements DoubleClickHandler {
	private final ListGrid patientListGrid;
	private final DocViewerController controller;

	/**
	 * @wbp.parser.constructor
	 * @param mainController
	 */
	public PatientListCanvas(DocViewerController mainController) {

		this.controller = mainController;
		Config controllerConfig = this.controller.getConfig();

		this.patientListGrid = new ListGrid();
		patientListGrid.addDoubleClickHandler(this);
		patientListGrid.setShowEmptyMessage(true);
		patientListGrid
				.setEmptyMessage("<br>Click the <b>Find</b> button to populate this grid.");
		patientListGrid.setSelectionType(SelectionStyle.SINGLE);
		patientListGrid.setShowAllRecords(true);
		patientListGrid.setSortField(PatientRecord.MATCH_CONFIDENCE_FIELD);
		patientListGrid.setSortDirection(SortDirection.DESCENDING);
		patientListGrid.setWidth(700);
		patientListGrid.setHeight(408);
		patientListGrid.setWrapCells(true);
		patientListGrid.setFixedRecordHeights(false);
		patientListGrid.setTooltip("Double-click to find patient records");

		// Given Name:
		String givenNameLabel = controllerConfig
				.get(Config.KEY_LABEL_GIVEN_NAME);
		final ListGridField givenNameField = new ListGridField(
				PatientRecord.GIVEN_NAME_FIELD, givenNameLabel, 90);
		givenNameField.setType(ListGridFieldType.TEXT);
		givenNameField.setShowHover(true);
		givenNameField.setHoverCustomizer(new PatientListHoverCustomizer(
				controllerConfig.get(Config.KEY_TOOLTIP_GIVEN_NAME)));

		// Family Name:
		String familyNameLabel = controllerConfig
				.get(Config.KEY_LABEL_FAMILY_NAME);
		final ListGridField familyNameField = new ListGridField(
				PatientRecord.FAMILY_NAME_FIELD, familyNameLabel, 90);
		familyNameField.setType(ListGridFieldType.TEXT);
		familyNameField.setShowHover(true);
		familyNameField.setHoverCustomizer(new PatientListHoverCustomizer(
				controllerConfig.get(Config.KEY_TOOLTIP_FAMILY_NAME)));

		/*
		 * [03/14/13] IHS Release 1.3 (Requirement # 7333 - Middle Name updates
		 * in DocViewer)
		 */
		// Middle Name:
		String middleNameLabel = controllerConfig
				.get(Config.KEY_LABEL_MIDDLE_NAME);
		final ListGridField middleNameField = new ListGridField(
				PatientRecord.MIDDLE_NAME_FIELD, middleNameLabel, 90);
		middleNameField.setType(ListGridFieldType.TEXT);
		middleNameField.setShowHover(true);
		middleNameField.setHoverCustomizer(new PatientListHoverCustomizer(
				controllerConfig.get(Config.KEY_TOOLTIP_MIDDLE_NAME)));

		// Gender:
		final ListGridField genderField = new ListGridField(
				PatientRecord.GENDER_FIELD, "Gender", 65);
		genderField.setAlign(Alignment.LEFT);
		genderField.setType(ListGridFieldType.TEXT);
		genderField.setCellFormatter(new CellFormatter() {
			@Override
			public String format(Object value, ListGridRecord record,
					int rowNum, int colNum) {
				if (record == null)
					return null;
				PatientRecord patientRecord = (PatientRecord) record;
				String imageName = "person.png";
				Patient patient = patientRecord.getPatient();
				if (patient.getGender().equals("M")) {
					imageName = "gender_male.png";
				} else {
					imageName = "gender_female.png";
				}
				return Canvas.imgHTML(imageName) + " "
						+ patientRecord.getFormattedGender();

				// return patientRecord.getFormattedGender();
			}
		});
		genderField.setShowHover(true);
		genderField.setHoverCustomizer(new PatientListHoverCustomizer(
				controllerConfig.get(Config.KEY_TOOLTIP_GENDER)));

		// Date of Birth:
		final ListGridField dateOfBirthField = new ListGridField(
				PatientRecord.DOB_FIELD, "Date of Birth", 75);
		dateOfBirthField.setType(ListGridFieldType.DATE);
		dateOfBirthField.setAlign(Alignment.LEFT);
		dateOfBirthField.setCellFormatter(new CellFormatter() {
			@Override
			public String format(Object value, ListGridRecord record,
					int rowNum, int colNum) {
				if (record == null)
					return null;
				PatientRecord patientRecord = (PatientRecord) record;
				return patientRecord.getFormattedDateOfBirth();
			}
		});
		dateOfBirthField.setShowHover(true);
		dateOfBirthField.setHoverCustomizer(new PatientListHoverCustomizer(
				controllerConfig.get(Config.KEY_TOOLTIP_DATE_OF_BIRTH)));

		// EUID:
		String euidLabel = controllerConfig.get(Config.KEY_LABEL_EUID);
		final ListGridField euidField = new ListGridField(
				PatientRecord.EUID_FIELD, euidLabel, 95);
		euidField.setType(ListGridFieldType.TEXT);
		euidField.setShowHover(true);
		euidField.setHoverCustomizer(new PatientListHoverCustomizer(
				controllerConfig.get(Config.KEY_TOOLTIP_EUID)));

		// SSN:
		final ListGridField ssnField = new ListGridField(
				PatientRecord.SSN_FIELD, "SSN", 85);
		ssnField.setType(ListGridFieldType.TEXT);
		ssnField.setShowHover(true);
		ssnField.setHoverCustomizer(new PatientListHoverCustomizer(
				controllerConfig.get(Config.KEY_TOOLTIP_SSN)));

		// Match Weight:
		final ListGridField matchConfidencePercentageField = new ListGridField(
				PatientRecord.MATCH_CONFIDENCE_FIELD, "Confidence", 65);
		matchConfidencePercentageField.setType(ListGridFieldType.INTEGER);
		matchConfidencePercentageField.setAlign(Alignment.CENTER);
		matchConfidencePercentageField.setCellFormatter(new CellFormatter() {
			@Override
			public String format(Object value, ListGridRecord record,
					int rowNum, int colNum) {
				if (record == null)
					return null;
				PatientRecord patientRecord = (PatientRecord) record;
				return new Integer(patientRecord.getPatient()
						.getMatchConfidencePercentage()).toString();
			}
		});
		matchConfidencePercentageField.setShowHover(true);
		matchConfidencePercentageField
				.setHoverCustomizer(new PatientListHoverCustomizer(
						controllerConfig.get(Config.KEY_TOOLTIP_CONFIDENCE)));

		/*
		 * [03/14/13] IHS Release 1.3 (Requirement # 7333 - Middle Name updates
		 * in DocViewer)
		 */
		patientListGrid.setFields(new ListGridField[] { familyNameField,
				givenNameField, middleNameField, dateOfBirthField, genderField,
				ssnField, euidField, matchConfidencePercentageField });
		addChild(patientListGrid);
	}

	/**
	 * 
	 * @param gridRecords
	 */
	public void update(ListGridRecord[] gridRecords) {
		patientListGrid.setData(gridRecords);
	}

	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		PatientRecord patientRecord = (PatientRecord) patientListGrid
				.getSelectedRecord();
		if (patientRecord != null) {
			controller.viewPatient(patientRecord);
		}
	}
}
