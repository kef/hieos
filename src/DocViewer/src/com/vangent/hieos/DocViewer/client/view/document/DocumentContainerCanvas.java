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
package com.vangent.hieos.DocViewer.client.view.document;

import java.util.HashMap;
import java.util.List;

import java.util.Map;

//import com.google.gwt.core.client.GWT;
//import com.google.gwt.user.client.Window;
//import com.google.gwt.user.client.ui.FlowPanel;
import com.smartgwt.client.widgets.Window;
//import com.google.gwt.user.client.Window;
import com.smartgwt.client.types.ContentsType;
import com.smartgwt.client.types.Side;
//import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.TabDeselectedEvent;
import com.smartgwt.client.widgets.tab.events.TabDeselectedHandler;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;
import com.vangent.hieos.DocViewer.client.model.config.Config;
import com.vangent.hieos.DocViewer.client.model.config.DocumentTemplateConfig;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadata;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadataRecord;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class DocumentContainerCanvas extends Canvas {
	private final DocViewerController controller;
	private final DocumentListCanvas documentListCanvas;
	private final DocumentDetailCanvas documentDetailCanvas;
	private final TabSet documentTabSet;
	private SelectItem documentTemplateSelectItem;
	private String[] documentTemplateDisplayNames;
	private String[] documentTemplateFileNames;
	private String documentTemplateFileName;

	/**
	 * Gets the name of the used browser.
	 */
	public static native String getBrowserName() /*-{
		return navigator.userAgent.toLowerCase();
	}-*/;

	/**
	 * Returns true if the current browser is IE (Internet Explorer).
	 */
	public static boolean isIEBrowser() {
		return getBrowserName().contains("msie");
	}

	/**
	 * 
	 * @param patientRecord
	 * @param controller
	 */
	public DocumentContainerCanvas(final PatientRecord patientRecord,
			final DocViewerController controller) {
		this.controller = controller;
		this.documentListCanvas = new DocumentListCanvas(this);
		this.documentDetailCanvas = new DocumentDetailCanvas();
		this.documentTabSet = this.getDocumentTabSet();
		this.addChild(documentTabSet);
	}

	/**
	 * 
	 * @return
	 */
	private TabSet getDocumentTabSet() {
		

		// Create the tab set (to holder list of documents and individually selected documents).
		final TabSet tabSet = new TabSet();
		tabSet.setWidth100();
		tabSet.setHeight100();
		tabSet.setTabBarPosition(Side.TOP);

		// Add the document tab.
		Tab documentsTab = new Tab("Documents", "folder.png");
		tabSet.addTab(documentsTab);

		final DynamicForm documentTemplateOptionsForm = this.getDocumentTemplateOptionsForm();
		
		// Now layout it out.
		VStack verticalLayout = new VStack();
		verticalLayout.addMember(this.documentListCanvas);
		LayoutSpacer layoutSpacer = new LayoutSpacer();
		layoutSpacer.setHeight(5);
		verticalLayout.addMember(layoutSpacer);
		verticalLayout.addMember(documentTemplateOptionsForm);

		HLayout mainLayout = new HLayout();
		mainLayout.setWidth(500);
		mainLayout.addMember(verticalLayout);
		layoutSpacer = new LayoutSpacer();
		layoutSpacer.setWidth(10);
		mainLayout.addMember(layoutSpacer);
		// layout.addMember(optionsForm);
		mainLayout.addMember(this.documentDetailCanvas);
		documentsTab.setPane(mainLayout);

		return tabSet;
	}

	/**
	 * 
	 * @return
	 */
	private DynamicForm getDocumentTemplateOptionsForm() {
		this.initializeDocumentTemplates();
		this.documentTemplateSelectItem = new SelectItem();
		documentTemplateSelectItem.setDefaultToFirstOption(true);
		documentTemplateSelectItem.setTitle("Document Template");
		documentTemplateSelectItem.setWidth(105);
		documentTemplateSelectItem
				.setValueMap(this.documentTemplateDisplayNames);
		documentTemplateSelectItem.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				setDocumentTemplateFileName(event.getValue().toString());
			}
		});

		DynamicForm optionsForm = new DynamicForm();
		// optionsForm.setTitle("Options");
		optionsForm.setGroupTitle("<b>Display Options</b>");
		optionsForm.setAutoWidth();
		optionsForm.setIsGroup(true);
		// optionsForm.setWidth(300);
		optionsForm.setHeight(30);
		optionsForm.setNumCols(1);
		optionsForm.setItems(documentTemplateSelectItem);

		return optionsForm;
	}

	/**
	 * 
	 */
	private void initializeDocumentTemplates() {
		// Probably not best way to integrate, but will do for now..
		Config config = controller.getConfig();
		List<DocumentTemplateConfig> documentTemplateConfigs = config
				.getDocumentTemplateConfigs();
		documentTemplateDisplayNames = new String[documentTemplateConfigs
				.size()];
		documentTemplateFileNames = new String[documentTemplateConfigs.size()];
		int i = 0;
		for (DocumentTemplateConfig documentTemplateConfig : documentTemplateConfigs) {
			documentTemplateDisplayNames[i] = documentTemplateConfig
					.getDisplayName();
			documentTemplateFileNames[i] = documentTemplateConfig.getFileName();
			++i;
		}
		documentTemplateFileName = documentTemplateFileNames[0];
	}

	/**
	 * 
	 * @param documentTemplateDisplayName
	 */
	private void setDocumentTemplateFileName(String documentTemplateDisplayName) {
		for (int i = 0; i < documentTemplateDisplayNames.length; i++) {
			if (documentTemplateDisplayNames[i]
					.equals(documentTemplateDisplayName)) {
				documentTemplateFileName = documentTemplateFileNames[i];
				break;
			}
		}
	}

	/**
	 * 
	 * @param metadata
	 */
	public void showDocument(DocumentMetadata metadata) {

		// Get HTMLPane to hold document.
		final HTMLPane htmlPane = this.getHTMLPaneForDocument(metadata);

		// Create tab to hold document.
		final Tab documentTab = new Tab();
		Config config = controller.getConfig();
		String title = metadata.getTitle();

		// Trim the document tab title if configured to do so.
		boolean trimDocumentTabTitles = config
				.getAsBoolean(Config.KEY_TRIM_DOCUMENT_TAB_TITLES);
		if (trimDocumentTabTitles == true) {
			Integer trimDocumentTabTitlesLength = config
					.getAsInteger(Config.KEY_TRIM_DOCUMENT_TAB_TITLES_LENGTH);
			if (title.length() > trimDocumentTabTitlesLength) {
				int endIndex = trimDocumentTabTitlesLength - 1;
				title = title.substring(0, endIndex) + "...";
			}
		}
		documentTab.setTitle(Canvas.imgHTML("document.png") + " " + title);
		documentTab.setCanClose(true);
		documentTab.setPrompt(metadata.getTitle());
		documentTabSet.addTab(documentTab);

		// Put htmlPane into an HLayout (to avoid Firefox problem).
		final HLayout layout = new HLayout();
		layout.setWidth100();
		layout.setHeight100();
		layout.addMember(htmlPane);
		documentTab.setPane(layout);

		// Begin HACK.
		if (DocumentContainerCanvas.isIEBrowser()) {

			// HACK to avoid opacity issues with IE8.
			layout.removeMember(htmlPane);
			documentTab.addTabDeselectedHandler(new TabDeselectedHandler() {
				@Override
				public void onTabDeselected(TabDeselectedEvent event) {
					// Clear the contents of the tab.
					// SC.warn("Tab Deselected!");
					layout.removeMember(htmlPane);
				}
			});
			// Continue HACK.
			documentTab.addTabSelectedHandler(new TabSelectedHandler() {
				@Override
				public void onTabSelected(TabSelectedEvent event) {
					// Restore the contents of the tab.
					// SC.warn("Tab Selected!");
					layout.addMember(htmlPane);
				}
			});
			// End HACK.
		}

		// Retrieve the document and show it.
		this.loadDocument(metadata, htmlPane);

		// Now make sure the new tab is selected.
		documentTabSet.selectTab(documentTab);
	}

	/**
	 * 
	 * @param metadata
	 * @return
	 */
	private HTMLPane getHTMLPaneForDocument(DocumentMetadata metadata) {
		// Create HTMLPane to hold document.
		final HTMLPane htmlPane = new HTMLPane();
		htmlPane.setContentsType(ContentsType.PAGE);
		htmlPane.setContents("No document selected");
		htmlPane.setWidth100();
		htmlPane.setHeight100();
		htmlPane.setOpacity(100); // Fixes refresh problem (on IE).
		htmlPane.setScrollbarSize(0); // Fixes vertical scroll bar problem.
		// htmlFlow.setAutoHeight();
		htmlPane.setLoadingMessage("Loading...");

		// Setup the URL parameters for the request.
		final Map<String, String> urlParams = new HashMap<String, String>();
		urlParams.put("hc_id", metadata.getHomeCommunityID());
		urlParams.put("doc_id", metadata.getDocumentID());
		urlParams.put("repo_id", metadata.getRepositoryID());
		urlParams.put("patient_id", metadata.getPatientID());
		urlParams.put("template_filename", this.documentTemplateFileName);
		String searchMode = controller.getConfig().get(Config.KEY_SEARCH_MODE);
		urlParams.put("search_mode", searchMode);
		htmlPane.setContentsURLParams(urlParams);

		return htmlPane;
	}

	/**
	 * 
	 * @param metadata
	 * @param htmlPane
	 */
	private void loadDocument(DocumentMetadata metadata, HTMLPane htmlPane) {
		// String baseURL = GWT.getModuleBaseURL();
		// com.google.gwt.user.client.Window.alert("baseURL = " + baseURL);
		htmlPane.setContentsURL(metadata.getContentURL());
		// htmlPane.setContentsURL("http://www.google.com");
	}

	/**
	 * 
	 * @param message
	 */
	public void setLoadingDataMessage(String message) {
		// FIXME: not necessarily working .... ?
		this.documentListCanvas.setLoadingDataMessage(message);
	}

	/**
	 * 
	 * @param gridRecords
	 */
	public void updateDocumentList(ListGridRecord[] gridRecords) {
		this.documentListCanvas.update(gridRecords);
		if (gridRecords.length > 0) {
			// Select the first record ...
			ListGridRecord firstRecord = gridRecords[0];
			DocumentMetadataRecord metadataRecord = (DocumentMetadataRecord) firstRecord;
			this.documentListCanvas.selectRecord(metadataRecord);
			// Should not have to do this next line also, so there must be a
			// timing issue ...
			this.showDocumentDetails(metadataRecord.getDocumentMetadata());
		}
	}

	/**
	 * 
	 * @param metadata
	 */
	public void showDocumentWindow(DocumentMetadata metadata) {
		// Get HTMLPane to hold document.
		final HTMLPane htmlPane = this.getHTMLPaneForDocument(metadata);

		// Create window to hold HTML pane.
		final Window window = new Window();
		window.setWidth100();
		window.setHeight100();
		window.setTitle(metadata.getTitle());
		window.setShowMinimizeButton(false);
		window.setCanDragResize(true);
		window.setIsModal(true);
		window.setShowModalMask(true);
		window.centerInPage();

		// Create layout (not sure if needed).
		final HLayout layout = new HLayout();
		layout.setWidth100();
		layout.setHeight100();
		layout.addMember(htmlPane);
		window.addItem(layout);
		window.addCloseClickHandler(new CloseClickHandler() {
			@Override
			public void onCloseClick(CloseClientEvent event) {
				window.destroy();
			}
		});

		// Now, get the content.
		this.loadDocument(metadata, htmlPane);

		window.show();
	}

	/**
	 * 
	 * @param metadata
	 */
	public void showDocumentDetails(DocumentMetadata metadata) {
		documentDetailCanvas.update(metadata);
	}
}
