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
package com.vangent.hieos.DocViewer.client.model.document;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.vangent.hieos.DocViewer.client.model.patient.Patient;

/**
 * 
 * @author Bernie Thuman
 *
 */
public class DocumentSearchCriteria implements IsSerializable {
	private Patient patient;
	private String searchMode;
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Patient getPatient() {
		return this.patient;
	}

	public void setSearchMode(String searchMode) {
		this.searchMode = searchMode;
	}

	public String getSearchMode() {
		return searchMode;
	}
	

	
}
