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
package com.vangent.hieos.DocViewer.client.view.patient;

//import com.google.gwt.core.client.GWT;
//import com.google.gwt.user.client.Window;
//import com.google.gwt.user.client.ui.FlowPanel;
//import com.google.gwt.user.client.Window;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.viewer.DetailFormatter;
import com.smartgwt.client.widgets.viewer.DetailViewer;
import com.smartgwt.client.widgets.viewer.DetailViewerField;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class SinglePatientDemographicsView extends Canvas {
	private final DocViewerController controller;
	final DetailViewer detailViewer = new DetailViewer();
	

	/**
	 * 
	 * @param patientRecord
	 * @param controller
	 */
	public SinglePatientDemographicsView(final PatientRecord patientRecord,
			final DocViewerController controller) {
		this.controller = controller;
		detailViewer.setWidth(300);
		//detailViewer.setMargin(15);
		
		
		//detailViewer.setEmptyMessage("Select a document to view its details");
		final DetailViewerField dobField = new DetailViewerField(PatientRecord.DOB_FIELD, "Date of Birth");
		final DetailViewerField genderField = new DetailViewerField(PatientRecord.GENDER_FIELD, "Gender");
		final DetailViewerField familyNameField = new DetailViewerField(PatientRecord.FAMILY_NAME_FIELD, "Family Name");
		final DetailViewerField givenNameField = new DetailViewerField(PatientRecord.GIVEN_NAME_FIELD, "Given Name");
		final DetailViewerField middleNameField = new DetailViewerField(PatientRecord.MIDDLE_NAME_FIELD, "Middle Name");
		final DetailViewerField euidField = new DetailViewerField(PatientRecord.EUID_FIELD, "EUID");
		final DetailViewerField ssnField = new DetailViewerField(PatientRecord.SSN_FIELD, "SSN");
		final DetailViewerField matchConfidenceField = new DetailViewerField(PatientRecord.MATCH_CONFIDENCE_FIELD, "Match Confidence");
	
		
		// Setup format for date of birth field ...
		dobField.setDetailFormatter(new DetailFormatter() {
			public String format(Object value, Record record,
					DetailViewerField field) {
				if (record == null)
					return null;
				PatientRecord patientRecord = (PatientRecord) record;
				return patientRecord.getFormattedDateOfBirth();
			}
		});

		detailViewer.setFields(new DetailViewerField[] { 
				familyNameField,
				middleNameField,
				givenNameField,
				genderField,
				dobField,
				euidField,
				ssnField,
				matchConfidenceField});
		addChild(detailViewer);
		
	}
	
	/**
	 * 
	 * @param patientRecord
	 */
	public void update(PatientRecord patientRecord) {
		detailViewer.setData(new Record[]{patientRecord});		
	}
}
