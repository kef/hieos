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

import java.util.List;

import com.google.gwt.core.client.EntryPoint;
//import com.google.gwt.event.dom.client.ClickEvent;
//import com.google.gwt.event.dom.client.ClickHandler;
//import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.events.SubmitValuesEvent;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.HeaderItem;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VStack;
import com.vangent.hieos.DocViewer.client.controller.AuthenticationObserver;
import com.vangent.hieos.DocViewer.client.controller.ConfigObserver;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.controller.LogoutObserver;
import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContextDTO;
import com.vangent.hieos.DocViewer.client.model.config.Config;
import com.vangent.hieos.DocViewer.client.model.config.AuthenticationDomainConfig;
import com.vangent.hieos.DocViewer.client.model.patient.PatientDTO;
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
	private Canvas currentCanvas = null;

	/**
	 * 
	 */
	@Override
	public void onModuleLoad() {
		// Load client configuration ...
		this.loadConfig();

		// Load the Login page
		// this.loadLoginPage();
	}

	/**
	 * 
	 */
	private void loadConfig() {
		// Load client configuration ...
		ConfigObserver observer = new ConfigObserver(this, controller);
		controller.loadConfig(observer);
	}

	/**
	 * This is the entry point method.
	 */
	public void loadLoginPage() {
		// Get Title and Logo details from config file
		Config config = controller.getConfig();
		String title = config.get(Config.KEY_TITLE);
		String logoFileName = config.get(Config.KEY_LOGO_FILE_NAME);
		Integer logoWidth = config.getAsInteger(Config.KEY_LOGO_WIDTH);
		Integer logoHeight = config.getAsInteger(Config.KEY_LOGO_HEIGHT);

		// Set up Login Form
		final DynamicForm loginForm = new DynamicForm();
		loginForm.setWidth100();
		loginForm.setHeight100();

		HeaderItem header = new HeaderItem();
		header.setDefaultValue(title);
		header.setAlign(Alignment.CENTER);

		final Img logo = new Img(logoFileName, logoWidth, logoHeight);

		/*
		 * Label logonLabel = new Label(); logonLabel.setWidth(200);
		 * logonLabel.setHeight100(); logonLabel.setAlign(Alignment.CENTER);
		 * logonLabel.setContents("Enter your account details below");
		 */

		final TextItem userIdItem = new TextItem("userid", "User ID");
		final PasswordItem passwordItem = new PasswordItem("Password",
				"Password");

		String authDomainName = config.get(Config.KEY_LABEL_AUTHDOMAIN_NAME);
		String authDomainSelect = config
				.get(Config.KEY_LABEL_AUTHDOMAIN_SELECT);
		final SelectItem authDomainList = new SelectItem(authDomainSelect,
				authDomainName);

		userIdItem.setRequired(true);
		userIdItem.setRequiredMessage("Please specify User ID");
		passwordItem.setRequired(true);
		passwordItem.setRequiredMessage("Please specify Password");

		final ButtonItem loginButton = new ButtonItem("Login");
		loginButton.setIcon("login-blue.png");
		loginButton.setAlign(Alignment.CENTER);
		loginButton.setEndRow(true);
		loginButton.setColSpan(2);
		loginButton
				.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
					@Override
					public void onClick(
							com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {

						loginForm.submit();
					}
				});

		// Get the authentication domains from xconfig.xml.
		// Changed for the IHS requirement- work order: 7334- Provide means for
		// Tribal Sites (non-D1 users) to authenticate and log onto the HIE
		// DocViewer” .
		boolean showAuthDomainList = Boolean.parseBoolean(config
				.get(Config.KEY_SHOW_AUTHDOMAIN_LIST) + "");
		java.util.LinkedHashMap<String, String> map = getAuthDomainList();
		// Show the authentication domain selection box.
		if (showAuthDomainList) {
			// //this.getAuthDomainList();
			// Set up the authDomain drop-down box.
			authDomainList.setShowAllOptions(true); // true makes sure something
													// is selected. false makes
													// the first selection
													// blank.
			// authDomainList.setValueMap(authDomainDisplayName);
			authDomainList.setValueMap(map);
			authDomainList.setRequired(true);
			authDomainList.setRequiredMessage("Please specify "
					+ authDomainName.toLowerCase());
			loginForm.setFields(header, userIdItem, passwordItem,
					authDomainList, loginButton);
		} else {
			if (!map.keySet()
					.contains(Config.DEFAULT_ATHENTICATION_DOMAIN_TYPE)) {
				System.out.println("Check xconfig.xml. Domain: '"
						+ Config.DEFAULT_ATHENTICATION_DOMAIN_TYPE
						+ "' does not exist");
			}
			loginForm.setFields(header, userIdItem, passwordItem, loginButton);
		}
		final DocViewer entryPoint = this;

		loginForm.setAutoFocus(true);
		loginForm.setSaveOnEnter(true);
		loginForm
				.addSubmitValuesHandler(new com.smartgwt.client.widgets.form.events.SubmitValuesHandler() {

					@Override
					public void onSubmitValues(SubmitValuesEvent event) {
						boolean validatedOk = loginForm.validate();
						if (validatedOk == true) {
							AuthenticationObserver authObserver = new AuthenticationObserver(
									entryPoint);

							// Initialize the authDomain select to nothing.
							// Changed for the IHS requirement- work order:
							// 7334- Provide means for Tribal Sites (non-D1
							// users) to authenticate and log onto the HIE
							// DocViewer” .
							// String authDomainSelected = "default";
							String authDomainSelected = Config.DEFAULT_ATHENTICATION_DOMAIN_TYPE;

							// Check if the default authDomain is used.
							if (authDomainList.getValueAsString() != null) {
								authDomainSelected = authDomainList
										.getValueAsString();
							}
							controller.login(authObserver,
									userIdItem.getValueAsString(),
									passwordItem.getValueAsString(),
									authDomainSelected);
						}
					}
				});

		// Now, lay it out.
		final VStack mainLayout = new VStack();
		mainLayout.setWidth100();
		mainLayout.setHeight100();
		// vLayout.setAlign(Alignment.CENTER);
		mainLayout.setLayoutAlign(VerticalAlignment.CENTER);

		final VLayout formLayout = new VLayout();
		formLayout.setLayoutAlign(VerticalAlignment.CENTER);

		// layout.setAlign(Alignment.CENTER);
		formLayout.setWidth(300);
		formLayout.setHeight(220);
		formLayout.setShowEdges(true);
		formLayout.setEdgeSize(3);
		formLayout.setPadding(10);
		formLayout.addMember(logo);
		formLayout.addMember(loginForm);

		mainLayout.addMember(formLayout);
		this.addCanvasToRootPanel(mainLayout);
	}

	/**
	 * 
	 * @param authContext
	 */
	public void loadMainPageOnLoginSuccess(AuthenticationContextDTO authContext) {
		controller.setAuthContext(authContext);
		if (authContext.getSuccessStatus() == true) {
			// Check if the user has permission to use the application
			boolean permitted = authContext.hasPermissionToApplication();
			if (permitted) {
				loadMainPage();
			} else {
				SC.warn("You do not have access to this application");
			}
		} else {
			// Login failure.
			SC.warn("Invalid User ID and/or Password. Please check your credentials and try again.");
		}
	}

	/**
	 * 
	 */
	public void loadMainPage() {

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

		this.addCanvasToRootPanel(vLayout);

		// Show the find patients view.
		controller.showFindPatients();
	}

	/**
	 * 
	 * @param canvas
	 */
	private void addCanvasToRootPanel(Canvas canvas) {
		final RootPanel rootPanel = RootPanel.get();
		// Remove old canvas (if exists).
		if (currentCanvas != null) {
			rootPanel.remove(currentCanvas);
		}
		currentCanvas = canvas;
		rootPanel.add(canvas);

		// Remove loading wrapper (established in host HTML page).
		final RootPanel loadingWrapper = RootPanel.get("loadingWrapper");
		if (loadingWrapper != null) {
			RootPanel.getBodyElement().removeChild(loadingWrapper.getElement());
		}
	}

	/**
	 * 
	 * @return
	 */
	private ToolStrip createToolStrip() {

		Config config = controller.getConfig();

		// Create the "Find Patients" button.
		final ToolStripButton findPatientsButton = new ToolStripButton();
		findPatientsButton.setTitle("Find Patients");
		findPatientsButton.setTooltip("Search network for available patients");
		findPatientsButton.setIcon("person.png");
		findPatientsButton
				.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						controller.showFindPatients();
					}
				});

		findPatientsButton.setActionType(SelectionType.RADIO);
		findPatientsButton.setRadioGroup("maintoolbar");
		findPatientsButton.setSelected(true);

		// Create the "View Patients" button.
		ToolStripButton viewPatientsButton = this.getViewPatientsButton();
		controller.setViewPatientsButton(viewPatientsButton);
		controller.setFindPatientsButton(findPatientsButton);

		// Create the "Patient Consent" button.
		/*
		 * final ToolStripButton patientConsentButton = new ToolStripButton();
		 * patientConsentButton.setTitle("Patient Consent");
		 * patientConsentButton.setTooltip("Add/Update Patient Consent");
		 * patientConsentButton.setIcon("privacy.png"); patientConsentButton
		 * .addClickHandler(new
		 * com.smartgwt.client.widgets.events.ClickHandler() {
		 * 
		 * @Override public void onClick(ClickEvent event) {
		 * SC.warn("Under construction!"); } });
		 */

		// Create "Logout" button.
		final ToolStripButton logoutButton = new ToolStripButton();
		final DocViewer entryPoint = this;
		logoutButton.setTooltip("Logout");
		logoutButton.setTitle("Logout");
		logoutButton.setIcon("logout.png");
		logoutButton
				.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						LogoutObserver observer = new LogoutObserver(entryPoint);
						controller.logout(observer);
					}
				});

		// Now create the tool strip (that holds the buttons).
		final ToolStrip toolStrip = new ToolStrip();
		toolStrip.setHeight(30);
		toolStrip.setWidth100();

		// Layout the tool strip.
		toolStrip.addSpacer(5);

		boolean showBranding = config
				.getAsBoolean(Config.KEY_SHOW_TITLE_BRANDING);
		if (showBranding) {
			// Title:
			final Label title = new Label("HIEOS DocViewer");
			title.setWidth(120);
			title.setIcon("application.png");
			toolStrip.addMember(title);
			toolStrip.addSeparator();
		}

		toolStrip.addButton(findPatientsButton);
		if (viewPatientsButton != null) {
			toolStrip.addSeparator();
			toolStrip.addButton(viewPatientsButton);
		}
		// toolStrip.addButton(patientConsentButton);

		// Create "Find Documents" button (only if user has permissions -
		// usually for debug only).
		boolean showFindDocuments = config
				.getAsBoolean(Config.KEY_SHOW_FIND_DOCUMENTS_BUTTON);
		if (showFindDocuments) {
			final ToolStripButton findDocumentsButton = this
					.getFindDocumentsButton();
			if (findDocumentsButton != null) {
				toolStrip.addSeparator();
				toolStrip.addButton(findDocumentsButton);
			}
		}

		toolStrip.addFill();
		toolStrip.addButton(logoutButton);

		return toolStrip;
	}

	/**
	 * 
	 * @return
	 */
	private ToolStripButton getViewPatientsButton() {
		// Create the "Show Patients" button.
		ToolStripButton viewPatientsButton = null;
		viewPatientsButton = new ToolStripButton();
		viewPatientsButton.setTitle("View Patients");
		viewPatientsButton.setTooltip("View patients already selected");
		viewPatientsButton.setIcon("document.png");
		viewPatientsButton
				.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						controller.showViewPatients();
					}
				});
		viewPatientsButton.setActionType(SelectionType.RADIO);
		viewPatientsButton.setRadioGroup("maintoolbar");
		return viewPatientsButton;
	}

	/**
	 * 
	 * @return
	 */
	private ToolStripButton getFindDocumentsButton() {
		ToolStripButton findDocumentsButton = null;
		boolean hasPermission = controller.getAuthContext()
				.hasPermissionToFeature(
						AuthenticationContextDTO.PERMISSION_VIEWDOCS);
		if (hasPermission) {
			findDocumentsButton = new ToolStripButton();
			findDocumentsButton.setTooltip("Find documents given a patient id");
			findDocumentsButton.setTitle("Find Documents");
			findDocumentsButton.setIcon("document.png");
			findDocumentsButton
					.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							// SC.showConsole();
							final PatientIDValueCallback callback = new PatientIDValueCallback(
									controller);
							SC.askforValue("Find Documents", "Patient ID:",
									callback);
						}
					});
			//findDocumentsButton.setActionType(SelectionType.RADIO);
			//findDocumentsButton.setRadioGroup("maintoolbar");
		}
		return findDocumentsButton;
	}

	/**
	 * Get the list of authentication domains. Changed for the IHS requirement-
	 * work order: 7334- Provide means for Tribal Sites (non-D1 users) to
	 * authenticate and log onto the HIE DocViewer” .
	 */
	private java.util.LinkedHashMap<String, String> getAuthDomainList() {
		// FIXME: Why do like this? Rewrite. Weak means to pass around params.
		Config config = controller.getConfig();
		List<AuthenticationDomainConfig> authDomainConfigs = config
				.getAuthDomainListConfigs();
		/**
		 * authDomainDisplayName = new String[authDomainConfigs.size()];
		 * authDomainValueList = new String[authDomainConfigs.size()]; int i =
		 * 0;
		 * 
		 * // Loop through all the authentication domains. for
		 * (AuthenticationDomainConfig authDomainConfig : authDomainConfigs) {
		 * authDomainDisplayName[i] = authDomainConfig.getAuthDomainName();
		 * authDomainValueList[i] = authDomainConfig.getAuthDomainValue(); ++i;
		 * } // Set the authDomain value to the first authDomain. //
		 * this.authDomainValue = authDomainValueList[0];
		 */
		java.util.LinkedHashMap<String, String> domainListMap = new java.util.LinkedHashMap<String, String>();
		for (AuthenticationDomainConfig authDomainConfig : authDomainConfigs) {
			domainListMap.put(authDomainConfig.getAuthDomainValue(),
					authDomainConfig.getAuthDomainName());

		}
		return domainListMap;
	}

	/**
	 * 
	 * @author Bernie Thuman
	 * 
	 */
	public class PatientIDValueCallback implements ValueCallback {
		private DocViewerController controller;

		/**
		 * 
		 * @param controller
		 */
		PatientIDValueCallback(DocViewerController controller) {
			this.controller = controller;
		}

		@Override
		public void execute(String patientID) {
			// TODO Auto-generated method stub
			if (patientID == null || patientID.length() == 0) {
				// Nothing to do.
				// SC.say("Nothing to do ...");
				return;
			}
			// SC.say("Patient ID entered = " + patientID);
			boolean validFormat = PatientUtil
					.validatePIDStringFormat(patientID);
			if (validFormat != true) {
				SC.warn("Patient ID must be in <b>EUID</b>^^^&<b>UNIVERSAL_ID</b>&ISO format");
				return;
			}

			// Create patient instance -- just with patient id.
			final PatientDTO patient = new PatientDTO();
			patient.setEuid(PatientUtil.getIDFromPIDString(patientID));
			patient.setEuidUniversalID(PatientUtil
					.getUniversalIDFromPIDString(patientID));
			patient.setFamilyName(null);
			patient.setGivenName(null);
			patient.setGender(null);
			patient.setDateOfBirth(null);
			patient.setSSN("N/A");
			patient.setMatchConfidencePercentage(100);

			// Now do the document search.
			final PatientRecord patientRecord = new PatientRecord(patient);
			controller.viewPatient(patientRecord);
		}

	}
}
