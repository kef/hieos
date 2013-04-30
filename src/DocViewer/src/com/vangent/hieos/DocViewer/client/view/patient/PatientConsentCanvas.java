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
import com.smartgwt.client.widgets.Canvas;
import com.vangent.hieos.DocViewer.client.controller.DocViewerController;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientConsentCanvas extends Canvas {
	private final DocViewerController controller;
	

	/**
	 * 
	 * @param patientRecord
	 * @param controller
	 */
	public PatientConsentCanvas(final PatientRecord patientRecord,
			final DocViewerController controller) {
		this.controller = controller;
		
		
	}
	
	/**
	 * 
	 * @param patientRecord
	 */
	public void update(PatientRecord patientRecord) {
		// TBD
	}
}
