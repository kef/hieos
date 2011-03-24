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
package com.vangent.hieos.DocViewer.client.controller;

//import com.google.gwt.user.client.Window;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.vangent.hieos.DocViewer.client.config.Config;
import com.vangent.hieos.DocViewer.client.helper.TimeOutHelper;
import com.vangent.hieos.DocViewer.client.model.document.DocumentSearchCriteria;
import com.vangent.hieos.DocViewer.client.model.patient.Patient;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;
import com.vangent.hieos.DocViewer.client.model.patient.PatientSearchCriteria;
import com.vangent.hieos.DocViewer.client.services.proxy.DocumentQueryService;
import com.vangent.hieos.DocViewer.client.services.proxy.PatientQueryService;
import com.vangent.hieos.DocViewer.client.view.document.DocumentListObserver;
import com.vangent.hieos.DocViewer.client.view.document.DocumentViewContainer;
import com.vangent.hieos.DocViewer.client.view.patient.PatientListObserver;
import com.vangent.hieos.DocViewer.client.view.patient.PatientViewContainer;

//import com.vangent.hieos.DocViewer.client.services.PDSRemoteService.Util;

public class DocViewerController {
	private Canvas mainCanvas;
	private PatientViewContainer patientViewContainer;
	private TabSet patientTabs = null;
	private Config config = new Config();

	/**
	 * 
	 */
	public DocViewerController() {
		// Setup defaults ...
		// FIXME: Later should configure from the server-side.
		this.getConfig()
				.put(Config.KEY_SEARCH_MODE, Config.VAL_SEARCH_MODE_HIE);
	}

	/**
	 * 
	 * @return
	 */
	public Config getConfig() {
		return this.config;
	}

	/**
	 * 
	 * @param canvas
	 */
	public void setMainCanvas(Canvas canvas) {
		mainCanvas = canvas;
	}

	/**
	 * 
	 * @param patientViewContainer
	 */
	public void setPatientViewContainer(
			PatientViewContainer patientViewContainer) {
		this.patientViewContainer = patientViewContainer;
	}

	/**
	 * 
	 * @param criteria
	 */
	public void findPatients(PatientSearchCriteria criteria) {
		PatientListObserver observer = new PatientListObserver(patientViewContainer);
		this.findPatients(criteria, observer);
	}

	/**
	 * 
	 * @param criteria
	 * @param observer
	 */
	public void findPatients(PatientSearchCriteria criteria,
			final PatientListObserver observer) {
		TimeOutHelper progressHelper = new TimeOutHelper();
		PatientQueryService service = new PatientQueryService(criteria, observer, progressHelper);
		service.doWork();
	}

	/**
	 * 
	 * @param patientRecord
	 */
	public void findDocuments(PatientRecord patientRecord) {
		DocumentSearchCriteria criteria = new DocumentSearchCriteria();
		criteria.setPatient(patientRecord.getPatient());
		String searchMode = this.getConfig().get(Config.KEY_SEARCH_MODE);
		criteria.setSearchMode(searchMode);
		DocumentListObserver observer = new DocumentListObserver(patientRecord, this);
		this.findDocuments(criteria, observer);
	}

	/**
	 * 
	 * @param criteria
	 * @param observer
	 */
	private void findDocuments(final DocumentSearchCriteria criteria,
			final DocumentListObserver observer) {

		TimeOutHelper progressHelper = new TimeOutHelper();
		DocumentQueryService service = new DocumentQueryService(criteria, observer, progressHelper);
		service.doWork();
	}

	/**
	 * 
	 */
	public void showPatients() {
		if (patientTabs != null) {
			this.addPaneToMainCanvas(patientTabs);
		} else {
			SC.warn("You must choose \"Find Patients\" before being able to view documents");
		}
	}

	/**
	 * 
	 */
	public void showFindPatients() {
		if (patientViewContainer == null) {
			patientViewContainer = new PatientViewContainer(this);
		}
		this.addPaneToMainCanvas(patientViewContainer);
	}

	/**
	 * 
	 * @param patientRecord
	 * @param documentViewContainer
	 */
	public void addPatientTab(PatientRecord patientRecord,
			DocumentViewContainer documentViewContainer) {
		// Add the document view container to the patient tab.
		final Tab patientTab = this.getPatientTab(patientRecord);
		patientTab.setPane(documentViewContainer);
		this.addPaneToMainCanvas(patientTabs);

		// Show the patient as the current tab.
		patientTabs.selectTab(patientTab);
	}

	/**
	 * 
	 * @param patientRecord
	 * @return
	 */
	private Tab getPatientTab(PatientRecord patientRecord) {
		// Create patient tab set if it does not already exist.
		if (patientTabs == null) {
			patientTabs = new TabSet();
			patientTabs.setWidth100();
			patientTabs.setHeight100();
		}

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

		// Add tab to the tab set.
		patientTabs.addTab(patientTab);

		return patientTab;
	}

	/**
	 * 
	 * @param childPane
	 */
	private void addPaneToMainCanvas(Canvas childPane) {
		Canvas[] children = mainCanvas.getChildren();
		boolean foundPane = false;
		for (int i = 0; i < children.length; i++) {
			if (children[i] != childPane) {
				// A bit of a hack: should only have one item (so we can break
				// here).
				mainCanvas.removeChild(children[i]);
				mainCanvas.addChild(childPane);
				foundPane = true;
				break;
			}
		}
		if (foundPane == false) {
			mainCanvas.addChild(childPane);
		}
	}
}