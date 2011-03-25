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
package com.vangent.hieos.DocViewer.client.entrypoint;

import com.google.gwt.core.client.EntryPoint;
//import com.google.gwt.event.dom.client.ClickEvent;
//import com.google.gwt.event.dom.client.ClickHandler;
//import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.patient.Patient;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;
import com.vangent.hieos.DocViewer.client.model.patient.PatientUtil;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 
 * @author Bernie Thuman
 */
public class DocViewer implements EntryPoint {
	private final DocViewerController controller = new DocViewerController();
	private final Canvas mainCanvas = new Canvas();

	public void onModuleLoad() {
		
		// Create the ToolStrip.
		final ToolStrip toolStrip = this.createToolStrip();
		
		// Configure the content pane.
		mainCanvas.setWidth100();
		mainCanvas.setHeight100();
		controller.setMainCanvas(mainCanvas);
		
		// Create the layout.
		final VLayout vLayout = new VLayout();
		vLayout.setWidth("98%");
		vLayout.setHeight("98%");
		vLayout.setAlign(Alignment.CENTER);
		
		// Add members to the layout.
		vLayout.addMember(toolStrip);
		final LayoutSpacer spacer = new LayoutSpacer();
		spacer.setHeight(4);
		vLayout.addMember(spacer);
		vLayout.addMember(mainCanvas);		

		final RootPanel rootPanel = RootPanel.get();
		rootPanel.add(vLayout);
		
		// Remove loading wrapper (established in host HTML page).
		final RootPanel loadingWrapper = RootPanel.get("loadingWrapper");
        if (loadingWrapper != null) {
        	RootPanel.getBodyElement().removeChild(loadingWrapper.getElement());
        }
        
        // Show the find patients view.
        controller.showFindPatients();
	}
	
	/**
	 * 
	 * @return
	 */
	private ToolStrip createToolStrip()
	{
		// Create the "Find Patient" button.
		final ToolStripButton findPatientButton = new ToolStripButton();
		findPatientButton.setTitle("Find Patients");
		findPatientButton.setTooltip("Search network for available patients");
		findPatientButton.setIcon("person.png");
		findPatientButton
				.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					public void onClick(ClickEvent event) {
						controller.showFindPatients();
					}
				});
		
		// Create the "Show Documents" button.
		final ToolStripButton showDocumentsButton = new ToolStripButton();
		showDocumentsButton.setTitle("Show Documents");
		showDocumentsButton.setTooltip("Show documents for patients already selected");
		showDocumentsButton.setIcon("document.png");
		showDocumentsButton
				.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					public void onClick(ClickEvent event) {
						controller.showPatients();
					}
				});
		
		// Create the "Patient Consent" button.
		final ToolStripButton patientConsentButton = new ToolStripButton();
		patientConsentButton.setTitle("Patient Consent");
		patientConsentButton.setTooltip("Add/Update Patient Consent");
		patientConsentButton.setIcon("privacy.png");
		patientConsentButton
					.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
						public void onClick(ClickEvent event) {
							SC.warn("Under construction!");
						}
					});
		
		// Create "Find Documents" button.
		final ToolStripButton findDocumentsButton = new ToolStripButton();
		findDocumentsButton.setTooltip("Find documents given a patient id");
		findDocumentsButton.setTitle("Find Documents");
		findDocumentsButton.setIcon("document.png");
		findDocumentsButton
				.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					public void onClick(ClickEvent event) {
						// SC.showConsole();
						final PatientIDValueCallback callback = new PatientIDValueCallback(controller);
						SC.askforValue("Find Documents", "Patient ID:", callback);
					}
				});

		// Create "Logout" button.
		final ToolStripButton logoutButton = new ToolStripButton();
		logoutButton.setTooltip("Logout");
		logoutButton.setTitle("Logout");
		logoutButton.setIcon("logout.png");
		logoutButton
				.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					public void onClick(ClickEvent event) {
						// SC.showConsole();
						// FIXME: do something
					}
				});

		// Title:
		final Label title = new Label("HIEOS DocViewer");
		title.setWidth(120);
		title.setIcon("application.png");
		
		// Now create the tool strip (that holds the buttons).
		final ToolStrip toolStrip = new ToolStrip();
		toolStrip.setHeight(30);
		toolStrip.setWidth100();
		
		// Layout the tool strip.
		toolStrip.addSpacer(5);
		toolStrip.addMember(title);
		toolStrip.addSeparator();
		toolStrip.addButton(findPatientButton);
		toolStrip.addButton(showDocumentsButton);
		toolStrip.addButton(patientConsentButton);
		toolStrip.addButton(findDocumentsButton);
		toolStrip.addFill();
		toolStrip.addButton(logoutButton);
		
		return toolStrip;
	}
	
	/**
	 * 
	 * @author Bernie Thuman
	 *
	 */
	public class PatientIDValueCallback implements ValueCallback
	{
		private DocViewerController controller;
		
		/**
		 * 
		 * @param controller
		 */
		PatientIDValueCallback(DocViewerController controller)
		{
			this.controller = controller;
		}

		@Override
		public void execute(String patientID) {
			// TODO Auto-generated method stub
			if (patientID == null || patientID.length() == 0) {
				// Nothing to do.
				//SC.say("Nothing to do ...");
				return;
			}
			//SC.say("Patient ID entered = " + patientID);
			boolean validFormat = PatientUtil.validatePIDStringFormat(patientID);
			if (validFormat != true)
			{
				SC.warn("Patient ID must be in <b>EUID</b>^^^&<b>UNIVERSAL_ID</b>&ISO format");
				return;
			}
			
			// Create patient instance -- just with patient id.
			final Patient patient = new Patient();
			patient.setEuid(PatientUtil.getIDFromPIDString(patientID));
			patient.setEuidUniversalID(PatientUtil.getUniversalIDFromPIDString(patientID));
			patient.setFamilyName(null);
			patient.setGivenName(null);
			patient.setGender(null);
			patient.setDateOfBirth(null);
			patient.setSSN("N/A");
			patient.setMatchConfidencePercentage(100);
			
			// Now do the document search.
			final PatientRecord patientRecord = new PatientRecord(patient);
			controller.findDocuments(patientRecord);
		}
		
	}
}
