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

//import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.CloseClickHandler;
import com.smartgwt.client.widgets.tab.events.TabCloseClickEvent;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.patient.Patient;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientTabSetMainCanvas extends Canvas implements
		CloseClickHandler {
	private final DocViewerController controller;
	private TabSet patientTabSet = null;

	/**
	 * 
	 * @param controller
	 */
	public PatientTabSetMainCanvas(final DocViewerController controller) {
		this.controller = controller;
		this.patientTabSet = this.getPatientTabSet();
		this.setWidth100();
		this.setHeight100();
		this.addChild(this.patientTabSet);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return patientTabSet == null
				|| this.patientTabSet.getTabs().length == 0;
	}

	/**
	 * 
	 * @param patientRecord
	 * @param patientContainerCanvas
	 */
	public void addPatientTab(PatientRecord patientRecord,
			final PatientContainerCanvas patientContainerCanvas) {
		if (patientTabSet == null) {
			patientTabSet = this.getPatientTabSet();
			this.addChild(patientTabSet);
		}

		// Get tab and add to the tab set.
		final Tab patientTab = this.getPatientTab(patientRecord);
		patientTab.setPane(patientContainerCanvas);

		// Add tab to the tab set.
		patientTabSet.addTab(patientTab);

		// Show the patient as the current tab.
		patientTabSet.selectTab(patientTab);

		// Tell controller to switch to this canvas (if not already current).
		controller.addPaneToMainCanvas(this);
	}

	/**
	 * 
	 * @param patientRecord
	 * @return
	 */
	private Tab getPatientTab(PatientRecord patientRecord) {

		// Create a new tab for the patient.
		final Tab patientTab = new Tab();
		String imageName = "person.png";
		Patient patient = patientRecord.getPatient();
		if (patient.getGender() != null) {
			if (patient.getGender().equals("M")) {
				imageName = "gender_male.png";
			} else if (patient.getGender().equals("F")) {
				imageName = "gender_female.png";
			}
		}
		patientTab.setTitle(Canvas.imgHTML(imageName) + " "
				+ patientRecord.getFormattedName());
		patientTab.setCanClose(true);

		return patientTab;
	}

	/**
	 * 
	 */
	private TabSet getPatientTabSet() {
		TabSet patientTabSet = new TabSet();
		patientTabSet.setWidth100();
		patientTabSet.setHeight100();
		patientTabSet.addCloseClickHandler(this);
		return patientTabSet;
	}

	@Override
	public void onCloseClick(TabCloseClickEvent event) {
		// Tab tab = event.getTab();
		int numTabs = patientTabSet.getTabs().length;
		if (numTabs == 1) {
			// Clear out the tabs.
			this.removeChild(patientTabSet);
			patientTabSet = null;
			// Show the find patients view.
			controller.showFindPatients();
		}
	}
}
