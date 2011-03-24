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

import java.util.List;

import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.vangent.hieos.DocViewer.client.helper.Observer;
import com.vangent.hieos.DocViewer.client.model.patient.Patient;
import com.vangent.hieos.DocViewer.client.model.patient.PatientRecord;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientListObserver implements Observer {
	private PatientViewContainer patientViewContainer;

	/**
	 * 
	 * @param patientViewContainer
	 */
	public PatientListObserver(PatientViewContainer patientViewContainer) {
		this.patientViewContainer = patientViewContainer;
	}

	/**
	 * 
	 * @param patients
	 */
	private void update(List<Patient> patients) {
		ListGridRecord[] gridRecords = new ListGridRecord[patients.size()];
		int gridRecord = 0;
		for (Patient patient : patients) {
			PatientRecord patientRecord = new PatientRecord(patient);
			gridRecords[gridRecord++] = patientRecord;
		}
		// Update the patient list.
		patientViewContainer.updatePatientList(gridRecords);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Object object) {
		this.update((List<Patient>) object);
	}
}
