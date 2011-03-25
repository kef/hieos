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

import java.util.Date;
import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * 
 * @author Bernie Thuman
 *
 */
public class DocumentMetadataRecord extends ListGridRecord {
	static public final String CREATION_DATE_FIELD = "creation_date";
	static public final String SOURCE_FIELD = "source";
	static public final String MIME_TYPE_FIELD = "mime_type";
	static public final String CLASS_CODE_FIELD = "class_code";
	static public final String FORMAT_CODE_FIELD = "format_code";
	static public final String TYPE_CODE_FIELD = "type_code";
	static public final String AUTHOR_INSTITUTION_FIELD = "author_institution";
	static public final String AUTHOR_NAME_FIELD = "author_name";
	static public final String TITLE_FIELD = "title";
	static public final String SIZE_FIELD = "size";
	static public final String DOCUMENT_ID_FIELD = "document_id";
	static public final String REPOSITORY_ID_FIELD = "repository_id";
	static public final String HOME_COMMUNITY_ID_FIELD = "home_community_id";
	static public final String EUID_FIELD = "euid";
	static public final String ASSIGNING_AUTHORITY_FIELD = "assigning_authority";
	
	private DocumentMetadata documentMetadata;

	/**
	 * 
	 * @return
	 */
	public DocumentMetadata getDocumentMetadata() {
		return this.documentMetadata;
	}

	/**
	 * 
	 * @param documentMetadata
	 */
	public DocumentMetadataRecord(DocumentMetadata documentMetadata) {
		this.documentMetadata = documentMetadata;
		// To allow grouping/sorting:
		setAttribute(CREATION_DATE_FIELD, documentMetadata.getCreationTime());
		setAttribute(SOURCE_FIELD, documentMetadata.getSource());
		setAttribute(MIME_TYPE_FIELD, documentMetadata.getMimeType());
		setAttribute(CLASS_CODE_FIELD, documentMetadata.getClassCode());
		setAttribute(FORMAT_CODE_FIELD, documentMetadata.getFormatCode());
		setAttribute(TYPE_CODE_FIELD, documentMetadata.getTypeCode());
		setAttribute(AUTHOR_INSTITUTION_FIELD, this.getFormattedAuthorInstitution());
		setAttribute(AUTHOR_NAME_FIELD, this.getFormattedAuthorName());
		setAttribute(TITLE_FIELD, documentMetadata.getTitle());
		setAttribute(SIZE_FIELD, documentMetadata.getSize());
		setAttribute(DOCUMENT_ID_FIELD, documentMetadata.getDocumentID());
		setAttribute(REPOSITORY_ID_FIELD, documentMetadata.getRepositoryID());
		setAttribute(HOME_COMMUNITY_ID_FIELD, documentMetadata.getHomeCommunityID());
		setAttribute(EUID_FIELD, documentMetadata.getEuid());
		setAttribute(ASSIGNING_AUTHORITY_FIELD, documentMetadata.getAssigningAuthority());
	}

	/**
	 * 
	 * @return
	 */
	public String getFormattedCreationTime() {
		// TBD: Should centralize standard date format
		Date date = documentMetadata.getCreationTime();
		DateTimeFormat dateFormatter = DateTimeFormat.getFormat("dd-MMM-yyyy");
		try {
			return dateFormatter.format((Date) date);
		} catch (Exception e) {
			return date.toString();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFormattedAuthorInstitution()
	{
		List<DocumentAuthorMetadata> authors = documentMetadata.getAuthors();
		String authorInstitution = "UNKNOWN";
		if (authors != null && authors.size() > 0)
		{
			// FIXME: ? Just take the first one.
			DocumentAuthorMetadata author = authors.get(0);
			authorInstitution = author.getInstitution();
		} 
		return authorInstitution;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFormattedAuthorName()
	{
		List<DocumentAuthorMetadata> authors = documentMetadata.getAuthors();
		String authorName = "UNKNOWN";
		if (authors != null && authors.size() > 0)
		{
			// FIXME: ? Just take the first one.
			DocumentAuthorMetadata author = authors.get(0);
			authorName = author.getName();
		} 
		return authorName;
	}
}
