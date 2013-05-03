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

import com.vangent.hieos.DocViewer.client.helper.Observer;
import com.vangent.hieos.DocViewer.client.model.patient.PatientConsentDirectivesDTO;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientConsentObserver implements Observer {
	private PatientConsentCanvas patientConsentCanvas;

	/**
	 * 
	 * @param patientConsentCanvas
	 */
	public PatientConsentObserver(PatientConsentCanvas patientConsentCanvas) {
		this.patientConsentCanvas = patientConsentCanvas;
	}

	/**
	 * 
	 * @param patientConsentDirectives
	 */
	private void update(PatientConsentDirectivesDTO patientConsentDirectives) {
		patientConsentCanvas.update(patientConsentDirectives);
	}

	@Override
	public void update(Object object) {
		this.update((PatientConsentDirectivesDTO) object);
	}
}
