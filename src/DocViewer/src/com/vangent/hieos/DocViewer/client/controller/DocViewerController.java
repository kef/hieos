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
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContextDTO;
import com.vangent.hieos.DocViewer.client.model.authentication.CredentialsDTO;
import com.vangent.hieos.DocViewer.client.model.config.ConfigDTO;
import com.vangent.hieos.DocViewer.client.helper.Observer;
import com.vangent.hieos.DocViewer.client.helper.TimeOutHelper;
import com.vangent.hieos.DocViewer.client.model.document.DocumentSearchCriteriaDTO;
import com.vangent.hieos.DocViewer.client.model.patient.PatientConsentSearchCriteriaDTO;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;
import com.vangent.hieos.DocViewer.client.model.patient.PatientSearchCriteriaDTO;
import com.vangent.hieos.DocViewer.client.services.proxy.AuthenticationService;
import com.vangent.hieos.DocViewer.client.services.proxy.ConfigRetrieveService;
import com.vangent.hieos.DocViewer.client.services.proxy.DocumentQueryService;
import com.vangent.hieos.DocViewer.client.services.proxy.LogoutService;
import com.vangent.hieos.DocViewer.client.services.proxy.PatientConsentQueryService;
import com.vangent.hieos.DocViewer.client.services.proxy.PatientQueryService;
import com.vangent.hieos.DocViewer.client.view.document.DocumentListObserver;
import com.vangent.hieos.DocViewer.client.view.document.DocumentContainerCanvas;
import com.vangent.hieos.DocViewer.client.view.patient.FindPatientsMainCanvas;
import com.vangent.hieos.DocViewer.client.view.patient.PatientConsentObserver;
import com.vangent.hieos.DocViewer.client.view.patient.PatientListObserver;
import com.vangent.hieos.DocViewer.client.view.patient.PatientContainerCanvas;
import com.vangent.hieos.DocViewer.client.view.patient.PatientTabSetMainCanvas;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class DocViewerController {
	private Canvas mainCanvas;
	private AuthenticationContextDTO authContext;
	private FindPatientsMainCanvas findPatientsMainCanvas;
	private PatientTabSetMainCanvas patientTabSetMainCanvas;
	
	private ConfigDTO config = null;
	private ToolStripButton viewPatientsButton;
	private ToolStripButton findPatientsButton;

	/**
	 * 
	 */
	public DocViewerController() {
		this.patientTabSetMainCanvas = new PatientTabSetMainCanvas(this);
	}

	/**
	 * 
	 * @param authObserver
	 * @param userid
	 * @param password
	 */
	public void login(AuthenticationObserver authObserver, String userid,
			String password, String authDomainTypeKey) {
		CredentialsDTO creds = new CredentialsDTO();
		creds.setPassword(password);
		creds.setUserId(userid);
		creds.setAuthDomainTypeKey(authDomainTypeKey);
		TimeOutHelper timeOutHelper = new TimeOutHelper();
		timeOutHelper.setPrompt("Authenticating ...");
		AuthenticationService service = new AuthenticationService(creds,
				authObserver, timeOutHelper);
		service.doWork();
	}

	/**
	 * 
	 * @param observer
	 */
	public void logout(LogoutObserver observer) {
		// Reset view elements.
		this.removePaneFromMainCanvas(findPatientsMainCanvas);
		this.removePaneFromMainCanvas(patientTabSetMainCanvas);
		findPatientsMainCanvas = null;
		patientTabSetMainCanvas = new PatientTabSetMainCanvas(this);

		TimeOutHelper timeOutHelper = new TimeOutHelper();
		LogoutService service = new LogoutService(observer, timeOutHelper);
		service.doWork();
	}

	/**
	 * 
	 */
	public void loadConfig(Observer observer) {
		// ConfigObserver observer = new ConfigObserver(this);
		TimeOutHelper timeOutHelper = new TimeOutHelper();
		timeOutHelper.setPrompt("Loading configuration ...");
		ConfigRetrieveService service = new ConfigRetrieveService(observer,
				timeOutHelper);
		service.doWork();
	}

	/**
	 * 
	 * @param config
	 */
	public void setConfig(ConfigDTO config) {
		this.config = config;
	}

	/**
	 * 
	 * @return
	 */
	public ConfigDTO getConfig() {
		return this.config;
	}

	/**
	 * 
	 * @return
	 */
	public AuthenticationContextDTO getAuthContext() {
		return authContext;
	}

	/**
	 * 
	 * @param authContext
	 */
	public void setAuthContext(AuthenticationContextDTO authContext) {
		this.authContext = authContext;
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
	 * @return
	 */
	public ToolStripButton getViewPatientsButton() {
		return viewPatientsButton;
	}

	/**
	 * 
	 * @param viewPatientsButton
	 */
	public void setViewPatientsButton(ToolStripButton viewPatientsButton) {
		this.viewPatientsButton = viewPatientsButton;
	}

	/**
	 * 
	 * @return
	 */
	public ToolStripButton getFindPatientsButton() {
		return findPatientsButton;
	}

	/**
	 * 
	 * @param findPatientsButton
	 */
	public void setFindPatientsButton(ToolStripButton findPatientsButton) {
		this.findPatientsButton = findPatientsButton;
	}

	/**
	 * 
	 * @param findPatientsMainCanvas
	 */
	public void setFindPatientsMainCanvas(
			FindPatientsMainCanvas findPatientsMainCanvas) {
		this.findPatientsMainCanvas = findPatientsMainCanvas;
	}

	/**
	 * 
	 * @param criteria
	 */
	public void findPatients(PatientSearchCriteriaDTO criteria) {
		PatientListObserver observer = new PatientListObserver(
				findPatientsMainCanvas);
		this.findPatients(criteria, observer);
	}

	/**
	 * 
	 * @param criteria
	 * @param observer
	 */
	public void findPatients(PatientSearchCriteriaDTO criteria,
			final PatientListObserver observer) {
		TimeOutHelper timeOutHelper = new TimeOutHelper();
		
		PatientQueryService service = new PatientQueryService(criteria,
				observer, timeOutHelper);
		service.doWork();
	}

	/**
	 * 
	 * @param patientRecord
	 */
	public void viewPatient(PatientRecord patientRecord) {
		// Create a patient view container for the patient.
		viewPatientsButton.setSelected(true);
		final PatientContainerCanvas patientContainerCanvas = new PatientContainerCanvas(
				patientRecord, this);
		patientTabSetMainCanvas.addPatientTab(patientRecord, patientContainerCanvas);
	}

	/**
	 * 
	 * @param patientRecord
	 * @param documentContainerCanvas
	 */
	public void findDocuments(PatientRecord patientRecord,
			DocumentContainerCanvas documentContainerCanvas) {
		DocumentSearchCriteriaDTO criteria = new DocumentSearchCriteriaDTO();
		criteria.setPatient(patientRecord.getPatient());
		String searchMode = this.getConfig().get(ConfigDTO.KEY_SEARCH_MODE);
		criteria.setSearchMode(searchMode);
		DocumentListObserver observer = new DocumentListObserver(
				documentContainerCanvas);
		this.findDocuments(criteria, observer);
	}

	/**
	 * 
	 * @param criteria
	 * @param observer
	 */
	private void findDocuments(final DocumentSearchCriteriaDTO criteria,
			final DocumentListObserver observer) {
		TimeOutHelper timeOutHelper = new TimeOutHelper();
		DocumentQueryService service = new DocumentQueryService(criteria,
				observer, timeOutHelper);
		service.doWork();
	}

	/**
	 * 
	 * @param criteria
	 * @param observer
	 */
	public void getConsentDirectives(
			final PatientConsentSearchCriteriaDTO criteria,
			final PatientConsentObserver observer) {
		TimeOutHelper timeOutHelper = new TimeOutHelper();
		PatientConsentQueryService service = new PatientConsentQueryService(
				criteria, observer, timeOutHelper);
		service.doWork();
	}

	/**
	 * 
	 */
	public void showViewPatients() {
		if (!patientTabSetMainCanvas.isEmpty()) {
			this.addPaneToMainCanvas(patientTabSetMainCanvas);
		} else {
			findPatientsButton.setSelected(true); // Reset.
			SC.warn("You must choose a Patient using \"Find Patients\" before being able to view patient data");
		}
	}

	/**
	 * 
	 */
	public void showFindPatients() {
		if (findPatientsMainCanvas == null) {
			findPatientsMainCanvas = new FindPatientsMainCanvas(this);
		}
		this.addPaneToMainCanvas(findPatientsMainCanvas);
		findPatientsButton.setSelected(true);
	}

	

	/**
	 * 
	 * @param childPane
	 */
	public void addPaneToMainCanvas(Canvas childPane) {
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
	
	/**
	 * 
	 * @param childPane
	 */
	private void removePaneFromMainCanvas(Canvas childPane) {
		Canvas[] children = mainCanvas.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] == childPane) {
				mainCanvas.removeChild(children[i]);
				break;
			}
		}
	}
}