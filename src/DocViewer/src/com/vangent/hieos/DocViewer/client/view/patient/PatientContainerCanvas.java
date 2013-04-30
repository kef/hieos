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

//import com.google.gwt.core.client.GWT;
//import com.google.gwt.user.client.Window;
//import com.google.gwt.user.client.ui.FlowPanel;
//import com.google.gwt.user.client.Window;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContext;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;
import com.vangent.hieos.DocViewer.client.view.document.DocumentContainerCanvas;
import com.vangent.hieos.DocViewer.client.view.patient.PatientBannerCanvas;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientContainerCanvas extends Canvas {
	private final DocViewerController controller;
	private final DocumentContainerCanvas documentContainerCanvas;
	private final PatientDemographicsCanvas patientDemographicsCanvas;
	private Canvas currentPatientContainerView;
	private final PatientBannerCanvas patientBannerCanvas;
	private VLayout layout;

	/**
	 * 
	 * @param patientRecord
	 * @param controller
	 */
	public PatientContainerCanvas(final PatientRecord patientRecord,
			final DocViewerController controller) {
		this.controller = controller;

		// Create sub components.

		// Patient banner.

		this.patientBannerCanvas = new PatientBannerCanvas();
		patientBannerCanvas.update(patientRecord);

		// Build out containers.
		this.documentContainerCanvas = new DocumentContainerCanvas(patientRecord,
				controller);
		this.patientDemographicsCanvas = new PatientDemographicsCanvas(
				patientRecord, controller);

		// Default patient view.
		this.patientDemographicsCanvas.update(patientRecord); // Fill it in.
		this.currentPatientContainerView = this.patientDemographicsCanvas;

		// Add components to the layout.
		//final VLayout layout = new VLayout();
		layout = new VLayout();
		layout.setWidth100();
		layout.setHeight100();
		//layout.setOpacity(100);

		// Add secondary navigation.
		final ToolStrip navToolStrip = this.getSecondaryNavigation(patientRecord);
		final HLayout bannerLayout = new HLayout();
		bannerLayout.setWidth100();
		bannerLayout.setHeight(25);
		bannerLayout.addMember(this.patientBannerCanvas);
		final LayoutSpacer bannerSpacer = new LayoutSpacer();
		bannerSpacer.setWidth(10);
		bannerLayout.addMember(bannerSpacer);
		bannerLayout.addMember(navToolStrip);

		// layout.addMember(this.patientBanner);
		layout.addMember(bannerLayout);
		final LayoutSpacer spacer = new LayoutSpacer();
		spacer.setHeight(4);
		layout.addMember(spacer);
		layout.addMember(this.currentPatientContainerView);

		// Create patient content layout.
		// final HLayout patientContentLayout = new HLayout();
		//patientContentLayout.setWidth100();
		//patientContentLayout.setHeight100();

		// patientContentLayout.addMember(toolStrip);
		//patientContentLayout.addMember(this.currentPatientContainerView);
		//layout.addMember(patientContentLayout);

		this.addChild(layout);
		// this.markForRedraw();
	}

	/**
	 * 
	 * @param patientRecord
	 * @return
	 */
	private ToolStrip getSecondaryNavigation(final PatientRecord patientRecord) {
		final ToolStripButton documentsButton = new ToolStripButton();
		//documentsButton.setAlign(Alignment.LEFT);
		//documentsButton.setIconAlign("left");
		documentsButton.setTitle("Documents");
		documentsButton.setTooltip("View Patient Documents");
		documentsButton.setIcon("document.png");
		documentsButton.setActionType(SelectionType.RADIO);
		documentsButton.setRadioGroup(patientRecord.getPatient().getEuid());
		final DocumentContainerCanvas documentContainerCanvas = this.documentContainerCanvas;
		final PatientContainerCanvas patientContainerCanvas = this;
		documentsButton
				.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						patientContainerCanvas.setPatientContentCanvas(documentContainerCanvas);
						controller.findDocuments(patientRecord, documentContainerCanvas);
						// SC.warn("Documents");
					}
				});

		// Demographics ...
		final ToolStripButton demographicsButton = new ToolStripButton();
		//demographicsButton.setAlign(Alignment.LEFT);
		//demographicsButton.setIconAlign("left");
		demographicsButton.setTitle("Demographics");
		demographicsButton.setTooltip("View Patient Demographics");
		demographicsButton.setIcon("patient.png");
		demographicsButton.setActionType(SelectionType.RADIO);
		demographicsButton.setRadioGroup(patientRecord.getPatient().getEuid());
		final PatientDemographicsCanvas patientDemographicsCanvas = this.patientDemographicsCanvas;
		demographicsButton
				.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						// SC.warn("Demographics");
						patientContainerCanvas.setPatientContentCanvas(patientDemographicsCanvas);
					}
				});

		// Consent ...
		final ToolStripButton consentButton = new ToolStripButton();
		//consentButton.setAlign(Alignment.LEFT);
		//consentButton.setIconAlign("left");
		consentButton.setTitle("Consent");
		consentButton.setTooltip("View/Modify Patient Consent Options");
		consentButton.setIcon("privacy.png");
		consentButton.setActionType(SelectionType.RADIO);
		consentButton.setRadioGroup(patientRecord.getPatient().getEuid());
		consentButton
				.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						SC.warn("Consent - Under Construction");
					}
				});

		// Disable buttons if not permitted for user.
		AuthenticationContext authContext = controller.getAuthContext();
		boolean hasPermission = authContext
				.hasPermissionToFeature(AuthenticationContext.PERMISSION_VIEWDOCS);
		if (!hasPermission) {
			documentsButton.setDisabled(true);
		}

		// Now create the tool strip (that holds the buttons).
		final ToolStrip toolStrip = new ToolStrip();
		//toolStrip.addSpacer(10);
		toolStrip.setHeight(25);
		toolStrip.setWidth100();
		// toolStrip.setVertical(true);
		// toolStrip.setAlign(Alignment.LEFT);
		// toolStrip.setHeight100();
		// toolStrip.setWidth(50);

		// Layout the tool strip.
		// toolStrip.addSpacer(5);
		toolStrip.addMember(demographicsButton);
		toolStrip.addSeparator();
		toolStrip.addMember(documentsButton);
		toolStrip.addSeparator();
		demographicsButton.setSelected(true); // Default (FOR NOW).
		toolStrip.addMember(consentButton);
		toolStrip.addFill();
	

		return toolStrip;
	}

	/**
	 * 
	 * @param canvas
	 */
	private void setPatientContentCanvas(Canvas canvas) {
		this.layout.removeMember(this.currentPatientContainerView);
		this.layout.addMember(canvas);
		this.currentPatientContainerView = canvas;
	}

}
