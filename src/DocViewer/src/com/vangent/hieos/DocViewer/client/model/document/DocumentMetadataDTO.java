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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Bernie Thuman
 *
 */
public class DocumentMetadata implements IsSerializable {
	private String source;
	private String mimeType;
	private Date creationTime;
	private String title;
	private List<DocumentAuthorMetadata> authors;
	private String repositoryID;
	private String documentID;
	private String homeCommunityID;
	private String euid;
	private String patientID;
	private String assigningAuthority;
	private int size;
	private String classCode;
	private String formatCode;
	private String typeCode;
	private String contentURL;  // Location to get content.

	public String getContentURL()
	{
		return contentURL;
	}
	
	public void setContentURL(String contentURL)
	{
		this.contentURL = contentURL;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<DocumentAuthorMetadata> getAuthors() {
		return authors;
	}

	public void setAuthors(List<DocumentAuthorMetadata> authors) {
		this.authors = authors;
	}

	public String getRepositoryID() {
		return repositoryID;
	}

	public void setRepositoryID(String repositoryID) {
		this.repositoryID = repositoryID;
	}

	public String getDocumentID() {
		return documentID;
	}

	public void setDocumentID(String documentID) {
		this.documentID = documentID;
	}

	public String getHomeCommunityID() {
		return homeCommunityID;
	}

	public void setHomeCommunityID(String homeCommunityID) {
		this.homeCommunityID = homeCommunityID;
	}

	public String getPatientID() {
		return patientID;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public String getClassCode() {
		return classCode;
	}

	public void setClassCode(String classCode) {
		this.classCode = classCode;
	}

	public String getFormatCode() {
		return formatCode;
	}

	public void setFormatCode(String formatCode) {
		this.formatCode = formatCode;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getEuid() {
		return euid;
	}

	public void setEuid(String euid) {
		this.euid = euid;
	}

	public String getAssigningAuthority() {
		return assigningAuthority;
	}

	public void setAssigningAuthority(String assigningAuthority) {
		this.assigningAuthority = assigningAuthority;
	}

}
