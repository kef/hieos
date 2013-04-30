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

import java.util.List;

import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.vangent.hieos.DocViewer.client.helper.Observer;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadata;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadataRecord;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class DocumentListObserver implements Observer {
	private DocumentViewContainer documentViewContainer;

	/**
	 * 
	 * @param documentViewContainer
	 */
	public DocumentListObserver(DocumentViewContainer documentViewContainer) {
		this.documentViewContainer = documentViewContainer;
	}

	/**
	 * 
	 * @param documents
	 */
	private void update(List<DocumentMetadata> documents) {
		// Will only get called if documents exist for the patient ...

		// Put documents in a list of grid records.
		ListGridRecord[] gridRecords = new ListGridRecord[documents.size()];
		int gridRecord = 0;
		for (DocumentMetadata document : documents) {
			DocumentMetadataRecord documentRecord = new DocumentMetadataRecord(
					document);
			gridRecords[gridRecord++] = documentRecord;
		}

		// Create a document view container for the patient's documents.
		//final DocumentViewContainer documentViewContainer = new DocumentViewContainer(
		//		patientRecord, controller);

		// documentViewContainer.setLoadingDataMessage("Loading data ...");

		// Update the document list.
		documentViewContainer.updateDocumentList(gridRecords);

		// Add a new patient tab.
		//controller.addPatientTab(patientRecord, documentViewContainer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Object object) {
		this.update((List<DocumentMetadata>) object);
	}
}