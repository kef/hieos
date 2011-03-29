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

import java.util.Map;

//import com.google.gwt.user.client.Window;
//import com.google.gwt.user.client.ui.FlowPanel;
import com.smartgwt.client.widgets.Window;  
//import com.google.gwt.user.client.Window;
import com.smartgwt.client.types.ContentsType;
import com.smartgwt.client.types.Side;
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
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.vangent.hieos.DocViewer.client.model.config.Config;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadata;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadataRecord;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;
import com.vangent.hieos.DocViewer.client.view.patient.PatientBanner;

/**
 * 
 * @author Bernie Thuman
 *
 */
public class DocumentViewContainer extends Canvas {
	private final DocViewerController controller;
	private final DocumentList documentList;
	private final DocumentDetail documentDetail;
	private final TabSet tabSet;
	private final PatientBanner patientBanner;
	private final SelectItem c32TemplateSelectItem;
	private String[] c32TemplateDisplayNames;
	private String[] c32TemplateFileNames;
	private String c32TemplateFileName;	

	/**
	 * 
	 * @param mainController
	 */
	public DocumentViewContainer(PatientRecord patientRecord, DocViewerController controller) {

		// Create sub components.		
		this.patientBanner = new PatientBanner();
		this.controller = controller;
		patientBanner.update(patientRecord);
		this.documentList = new DocumentList(this);
		this.documentDetail = new DocumentDetail();
		
		// Options form.
		this.initializeC32Templates();
		this.c32TemplateSelectItem = new SelectItem();   
		c32TemplateSelectItem.setDefaultToFirstOption(true);    
		c32TemplateSelectItem.setTitle("C32 Template");  
		c32TemplateSelectItem.setWidth(105);
		c32TemplateSelectItem.setValueMap(this.c32TemplateDisplayNames);   

		c32TemplateSelectItem.addChangeHandler(new ChangeHandler() {   
            public void onChange(ChangeEvent event) {   
            	setC32TemplateFileName(event.getValue().toString());
            }   
        });   

		DynamicForm optionsForm = new DynamicForm();
		optionsForm.setTitle("Options");
		optionsForm.setGroupTitle("Options");  
		optionsForm.setIsGroup(true);   
		optionsForm.setWidth(190);
		optionsForm.setHeight(30);
		optionsForm.setItems(c32TemplateSelectItem);
		
		
		// Create the tab set.
		this.tabSet = new TabSet();
		tabSet.setWidth100();
		tabSet.setHeight100();
		tabSet.setTabBarPosition(Side.TOP);  
		
		// Tabs.
		Tab documentsTab = new Tab("Documents", "folder.png");		       
		tabSet.addTab(documentsTab);
		
		HLayout documentTabLayout = new HLayout();
		documentTabLayout.setWidth(500);
		documentTabLayout.addMember(documentList);
		final LayoutSpacer spacer = new LayoutSpacer();
		spacer.setWidth(10);
		documentTabLayout.addMember(spacer);
		documentTabLayout.addMember(this.documentDetail);
		documentTabLayout.addMember(optionsForm);
		documentsTab.setPane(documentTabLayout);
		
		// Add components to the layout.
		final VLayout layout = new VLayout();
		layout.setWidth100();
		layout.setHeight100();
		layout.addMember(patientBanner);
		final LayoutSpacer bannerSpacer = new LayoutSpacer();
		spacer.setHeight(4);
		layout.addMember(bannerSpacer);
		layout.addMember(tabSet);

		this.addChild(layout);
		//this.markForRedraw();
	}
	
	/**
	 * 
	 */
	private void initializeC32Templates()
	{
		// FIXME: do not hardwire here.
		c32TemplateDisplayNames = new String[5];
		c32TemplateFileNames = new String[5];
		c32TemplateDisplayNames[0] = new String("IHS Template");
		c32TemplateFileNames[0] = "raa/CCD.xsl";
		c32TemplateDisplayNames[1] = new String("Basic Template");
		c32TemplateFileNames[1] = "basic/CCD.xsl";
		c32TemplateDisplayNames[2] = "DOD Template";
		c32TemplateFileNames[2] = "dod/CCD.xsl";
		c32TemplateDisplayNames[3] = "VA Template";
		c32TemplateFileNames[3] = "va/CCD.xsl";
		c32TemplateDisplayNames[4] = "MEDVA Template";
		c32TemplateFileNames[4] = "medva/CCD_MAIN.xsl";
		c32TemplateFileName = c32TemplateFileNames[0];
	}
	
	/**
	 * 
	 * @param c32TemplateDisplayName
	 */
	private void setC32TemplateFileName(String c32TemplateDisplayName)
	{
		for (int i = 0; i < c32TemplateDisplayNames.length; i++) {
			if (c32TemplateDisplayNames[i].equals(c32TemplateDisplayName)) {
				c32TemplateFileName = c32TemplateFileNames[i];
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
		documentTab.setTitle(Canvas.imgHTML("document.png") + " " + metadata.getTitle());
		documentTab.setCanClose(true);
		tabSet.addTab(documentTab);
		
		// Put htmlPane into an HLayout (to avoid Firefox problem).
		final HLayout layout = new HLayout();
		layout.setWidth100();
		layout.setHeight100();
		layout.addMember(htmlPane);
		documentTab.setPane(layout);
				
		// Retrieve the document and show it.
		this.loadDocument(metadata, htmlPane);
	
		// Now make sure the new tab is selected.
		tabSet.selectTab(documentTab);		
	}
	
	/**
	 * 
	 * @param metadata
	 * @return
	 */
	private HTMLPane getHTMLPaneForDocument(DocumentMetadata metadata)
	{
		// Create HTMLPane to hold document.
		final HTMLPane htmlPane = new HTMLPane();
		htmlPane.setContentsType(ContentsType.PAGE);
		htmlPane.setContents("No document selected");
		htmlPane.setWidth100();
		htmlPane.setHeight100();
		htmlPane.setOpacity(100);  // Fixes refresh problem (on IE).
		htmlPane.setScrollbarSize(0);  // Fixes vertical scroll bar problem.
		//htmlFlow.setAutoHeight();
		htmlPane.setLoadingMessage("Loading...");

		// Setup the URL parameters for the request.
		final Map<String, String> urlParams = new HashMap<String, String>();
		urlParams.put("hc_id", metadata.getHomeCommunityID());
		urlParams.put("doc_id", metadata.getDocumentID());
		urlParams.put("repo_id", metadata.getRepositoryID());
		urlParams.put("template_filename", this.c32TemplateFileName);
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
	private void loadDocument(DocumentMetadata metadata, HTMLPane htmlPane)
	{
		htmlPane.setContentsURL(metadata.getContentURL());
		//htmlPane.setContentsURL("http://www.google.com");
	}

	
	/**
	 * 
	 * @param message
	 */
	public void setLoadingDataMessage(String message) {
		// FIXME: not necessarily working .... ?
		this.documentList.setLoadingDataMessage(message);
	}

	/**
	 * 
	 * @param gridRecords
	 */
	public void updateDocumentList(ListGridRecord[] gridRecords) {
		this.documentList.update(gridRecords);
		if (gridRecords.length > 0)
		{
			// Select the first record ...
			ListGridRecord firstRecord = gridRecords[0];
			DocumentMetadataRecord metadataRecord = (DocumentMetadataRecord)firstRecord;
			this.documentList.selectRecord(metadataRecord);
			// Should not have to do this next line also, so there must be a timing issue ...
			this.showDocumentDetails(metadataRecord.getDocumentMetadata());
		}
	}
	
	/**
	 * 
	 * @param metadata
	 */
	public void showDocumentWindow(DocumentMetadata metadata)
	{
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
		documentDetail.update(metadata);
	}

}
