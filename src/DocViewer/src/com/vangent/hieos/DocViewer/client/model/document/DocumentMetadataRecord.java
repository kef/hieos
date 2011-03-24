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

public class DocumentMetadataRecord extends ListGridRecord {
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
		setAttribute("creation_date", documentMetadata.getCreationTime());
		setAttribute("source", documentMetadata.getSource());
		setAttribute("mime_type", documentMetadata.getMimeType());
		setAttribute("class_code", documentMetadata.getClassCode());
		setAttribute("format_code", documentMetadata.getFormatCode());
		setAttribute("type_code", documentMetadata.getTypeCode());
		setAttribute("author_institution", this.getFormattedAuthorInstitution());
		setAttribute("author_name", this.getFormattedAuthorName());
		setAttribute("title", documentMetadata.getTitle());
		setAttribute("size", documentMetadata.getSize());
		setAttribute("document_id", documentMetadata.getDocumentID());
		setAttribute("repository_id", documentMetadata.getRepositoryID());
		setAttribute("home_community_id", documentMetadata.getHomeCommunityID());
		setAttribute("euid", documentMetadata.getEuid());
		setAttribute("assigning_authority", documentMetadata.getAssigningAuthority());
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
