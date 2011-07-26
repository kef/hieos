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
package com.vangent.hieos.services.xds.policy;

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class DocumentMetadata {

    private OMElement registryObject;
    private boolean isObjectRef;
    private CodedValue typeCode;
    private CodedValue classCode;
    private CodedValue formatCode;
    private List<CodedValue> confidentialityCodes;
    private List<DocumentAuthorMetadata> documentAuthorMetadataList;
    private SubjectIdentifier patientId;
    private String documentId;
    private String repositoryId;
    private String homeCommunityId;  // May not be used.
    private String title;
    private String mimeType;
    private int size;

    /**
     *
     * @return
     */
    public OMElement getRegistryObject() {
        return registryObject;
    }

    /**
     * 
     * @param registryObject
     */
    public void setRegistryObject(OMElement registryObject) {
        this.registryObject = registryObject;
    }

    /**
     *
     * @return
     */
    public boolean isObjectRef() {
        return isObjectRef;
    }

    /**
     *
     * @param isObjectRef
     */
    public void setIsObjectRef(boolean isObjectRef) {
        this.isObjectRef = isObjectRef;
    }

    /**
     * 
     * @return
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     *
     * @param documentId
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     *
     * @return
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * 
     * @param repositoryId
     */
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * 
     * @return
     */
    public String getHomeCommunityId() {
        return homeCommunityId;
    }

    /**
     *
     * @param homeCommunityId
     */
    public void setHomeCommunityId(String homeCommunityId) {
        this.homeCommunityId = homeCommunityId;
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 
     * @param mimeType
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     *
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     *
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     *
     * @return
     */
    public CodedValue getTypeCode() {
        return typeCode;
    }

    /**
     *
     * @param typeCode
     */
    public void setTypeCode(CodedValue typeCode) {
        this.typeCode = typeCode;
    }

    /**
     *
     * @return
     */
    public CodedValue getClassCode() {
        return classCode;
    }

    /**
     * 
     * @param classCode
     */
    public void setClassCode(CodedValue classCode) {
        this.classCode = classCode;
    }

    /**
     *
     * @return
     */
    public CodedValue getFormatCode() {
        return formatCode;
    }

    /**
     *
     * @param formatCode
     */
    public void setFormatCode(CodedValue formatCode) {
        this.formatCode = formatCode;
    }

    /**
     *
     * @return
     */
    public List<CodedValue> getConfidentialityCodes() {
        return confidentialityCodes;
    }

    /**
     *
     * @param confidentialityCodes
     */
    public void setConfidentialityCodes(List<CodedValue> confidentialityCodes) {
        this.confidentialityCodes = confidentialityCodes;
    }

    /**
     *
     * @return
     */
    public SubjectIdentifier getPatientId() {
        return patientId;
    }

    /**
     * 
     * @param patientId
     */
    public void setPatientId(SubjectIdentifier patientId) {
        this.patientId = patientId;
    }

    /**
     *
     * @return
     */
    public List<DocumentAuthorMetadata> getDocumentAuthorMetadataList() {
        return documentAuthorMetadataList;
    }

    /**
     *
     * @param documentAuthorMetadataList
     */
    public void setDocumentAuthorMetadataList(List<DocumentAuthorMetadata> documentAuthorMetadataList) {
        this.documentAuthorMetadataList = documentAuthorMetadataList;
    }
}
