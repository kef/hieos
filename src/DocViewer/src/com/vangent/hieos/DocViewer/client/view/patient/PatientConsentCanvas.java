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
import java.util.List;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.SubmitValuesEvent;
import com.smartgwt.client.widgets.form.events.SubmitValuesHandler;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.patient.PatientConsentDirectivesDTO;
import com.vangent.hieos.DocViewer.client.model.patient.PatientConsentRuleDTO;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;
import com.vangent.hieos.DocViewer.client.model.patient.PatientSearchCriteriaDTO;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientConsentCanvas extends Canvas implements
		SubmitValuesHandler, ClickHandler {
	private final DocViewerController controller;
	private DynamicForm patientConsentForm;
	private CheckboxItem patientActiveCheckBoxItem;

	/**
	 * 
	 * @param patientRecord
	 * @param controller
	 */
	public PatientConsentCanvas(final PatientRecord patientRecord,
			final DocViewerController controller) {
		this.controller = controller;

		patientConsentForm = new DynamicForm();
		patientConsentForm.setIsGroup(true);
		patientConsentForm.setGroupTitle("<b>Patient Consent Directives</b>");
		patientConsentForm.setWidth(300);
		patientConsentForm.setSaveOnEnter(true);
		patientConsentForm.addSubmitValuesHandler(this);

		// Patient active/inactive flag.
		patientActiveCheckBoxItem = new CheckboxItem();
		patientActiveCheckBoxItem.setName("patientActiveCheckboxItem");
		patientActiveCheckBoxItem.setTitle("Active");
		patientActiveCheckBoxItem.setValue(false); // Default?

		final ButtonItem saveButtonItem = new ButtonItem("Save");
		// btnFind.setIcon("find.png");
		saveButtonItem.addClickHandler(this);
		saveButtonItem.setEndRow(true);
		saveButtonItem.setColSpan(2);
		saveButtonItem.setAlign(Alignment.CENTER);

		patientConsentForm.setFields(new FormItem[] {
				patientActiveCheckBoxItem, saveButtonItem });

		// Now, lay it out.
		final VLayout layout = new VLayout();

		// layout.setShowEdges(true);
		// layout.setEdgeSize(3);
		layout.addMember(patientConsentForm);

		addChild(layout);
	}

	/**
	 * 
	 * @param patientConsentDirectives
	 */
	public void update(PatientConsentDirectivesDTO patientConsentDirectives) {
		// TODO: IMPLEMENT
		// SC.warn("RETURNED -> " + patientConsentDirectives.getPatientID() +
		// ", ACTIVE = " +
		// (patientConsentDirectives.isActive() ? "TRUE" : "FALSE"));
		patientActiveCheckBoxItem.setValue(patientConsentDirectives.getStatus() == PatientConsentDirectivesDTO.StatusType.ACTIVE);
		
		// TBD: Implement more complex UI.
		
		// Only deal with Opt-In / Opt-Out for now.
		List<PatientConsentRuleDTO> patientConsentRules = patientConsentDirectives.getPatientConsentRules();
		
		// TBD: Implement.
	}

	@Override
	public void onSubmitValues(SubmitValuesEvent event) {
		boolean validatedOk = patientConsentForm.validate();
		if (validatedOk == true) {
			// Pull values off of form.
			// PatientSearchCriteria criteria = this.getPatientSearchCriteria();
			// Conduct the save.
			// controller.findPatients(criteria);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		this.patientConsentForm.submit();
	}
}
