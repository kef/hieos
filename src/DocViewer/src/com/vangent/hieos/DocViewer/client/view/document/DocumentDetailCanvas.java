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
		//detailViewer.setMargin(15);
		detailViewer.setEmptyMessage("Select a document to view its details");
		final DetailViewerField titleField = new DetailViewerField(DocumentMetadataRecord.TITLE_FIELD, "Title");
		final DetailViewerField creationDateField = new DetailViewerField(DocumentMetadataRecord.CREATION_DATE_FIELD, "Creation Date");
		final DetailViewerField euidField = new DetailViewerField(DocumentMetadataRecord.EUID_FIELD, "EUID");
		final DetailViewerField euidUniversalIDField = new DetailViewerField(DocumentMetadataRecord.ASSIGNING_AUTHORITY_FIELD, "Assigning Authority");
		final DetailViewerField homeCommunityIDField = new DetailViewerField(DocumentMetadataRecord.HOME_COMMUNITY_ID_FIELD, "Home Community ID");
		final DetailViewerField repositoryIDField = new DetailViewerField(DocumentMetadataRecord.REPOSITORY_ID_FIELD, "Repository ID");
		final DetailViewerField documentIDField = new DetailViewerField(DocumentMetadataRecord.DOCUMENT_ID_FIELD, "Document ID");
		final DetailViewerField authorNameField = new DetailViewerField(DocumentMetadataRecord.AUTHOR_NAME_FIELD, "Author Name");
		final DetailViewerField authorInstitutionField = new DetailViewerField(DocumentMetadataRecord.AUTHOR_INSTITUTION_FIELD, "Author Institution");
		final DetailViewerField mimeTypeField = new DetailViewerField(DocumentMetadataRecord.MIME_TYPE_FIELD, "Mime Type");
		final DetailViewerField classCodeField = new DetailViewerField(DocumentMetadataRecord.CLASS_CODE_FIELD, "Class Code");
		final DetailViewerField formatCodeField = new DetailViewerField(DocumentMetadataRecord.FORMAT_CODE_FIELD, "Format Code");
		final DetailViewerField typeCodeField = new DetailViewerField(DocumentMetadataRecord.TYPE_CODE_FIELD, "Type Code");
		
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
