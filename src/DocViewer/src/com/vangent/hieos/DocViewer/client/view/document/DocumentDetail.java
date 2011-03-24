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

//import com.google.gwt.user.client.Window;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.viewer.DetailFormatter;
import com.smartgwt.client.widgets.viewer.DetailViewer;
import com.smartgwt.client.widgets.viewer.DetailViewerField;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadata;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadataRecord;

/**
 * 
 * @author Bernie Thuman
 *
 */
public class DocumentDetail extends Canvas {
	final DetailViewer detailViewer = new DetailViewer();

	/**
	 * 
	 */
	public DocumentDetail() {
		detailViewer.setWidth100();
		detailViewer.setMargin(15);
		detailViewer.setEmptyMessage("Select a document to view its details");
		final DetailViewerField titleField = new DetailViewerField("title", "Title");
		final DetailViewerField creationDateField = new DetailViewerField("creation_date", "Creation Date");
		final DetailViewerField euidField = new DetailViewerField("euid", "EUID");
		final DetailViewerField euidUniversalIDField = new DetailViewerField("assigning_authority", "Assigning Authority");
		final DetailViewerField homeCommunityIDField = new DetailViewerField("home_community_id", "Home Community ID");
		final DetailViewerField repositoryIDField = new DetailViewerField("repository_id", "Repository ID");
		final DetailViewerField documentIDField = new DetailViewerField("document_id", "Document ID");
		final DetailViewerField authorNameField = new DetailViewerField("author_name", "Author Name");
		final DetailViewerField authorInstitutionField = new DetailViewerField("author_institution", "Author Institution");
		final DetailViewerField mimeTypeField = new DetailViewerField("mime_type", "Mime Type");
		final DetailViewerField classCodeField = new DetailViewerField("class_code", "Class Code");
		final DetailViewerField formatCodeField = new DetailViewerField("format_code", "Format Code");
		final DetailViewerField typeCodeField = new DetailViewerField("type_code", "Type Code");
		
		// Setup format for creation date ...
		creationDateField.setDetailFormatter(new DetailFormatter() {
			public String format(Object value, Record record,
					DetailViewerField field) {
				if (record == null)
					return null;
				DocumentMetadataRecord metadataRecord = (DocumentMetadataRecord) record;
				return metadataRecord.getFormattedCreationTime();
			}
		});

		detailViewer.setFields(new DetailViewerField[] { 
				titleField,
				creationDateField,
				mimeTypeField,
				euidField,
				euidUniversalIDField,
				homeCommunityIDField, 
				repositoryIDField, 
				documentIDField,
				authorNameField,
				authorInstitutionField,
				classCodeField,
				formatCodeField,
				typeCodeField});
		addChild(detailViewer);
	}
	
	/**
	 * 
	 * @param metadataRecord
	 */
	public void update(DocumentMetadata document) {
		//Window.alert("DocumentDetail ... document selected!");
		DocumentMetadataRecord metadataRecord = new DocumentMetadataRecord(document);
		detailViewer.setData(new Record[]{metadataRecord});		
	}
}
