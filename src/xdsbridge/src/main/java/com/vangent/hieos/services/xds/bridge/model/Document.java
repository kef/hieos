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

package com.vangent.hieos.services.xds.bridge.model;

import java.io.Serializable;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Vangent
 */
public class Document implements Serializable {

    /** Field description */
    private byte[] content;

    /** Field description */
    private String documentIdAsOID;

    /** Field description */
    private CodedValue format;

    /** Field description */
    private boolean generatedDocumentId;

    /** Field description */
    private String id;

    /** Field description */
    private String mimeType;

    /** Field description */
    private SubjectIdentifier patientId;

    /** Field description */
    private String replaceExtrinsicObjectId;

    /** Field description */
    private String replaceId;

    /** Field description */
    private String replaceIdAsOID;

    /** Field description */
    private String symbolicId;

    /** Field description */
    private CodedValue type;

    /**
     * Constructs ...
     *
     */
    public Document() {
        super();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getDocumentIdAsOID() {
        return documentIdAsOID;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public CodedValue getFormat() {
        return format;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public SubjectIdentifier getPatientId() {
        return patientId;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getReplaceExtrinsicObjectId() {
        return replaceExtrinsicObjectId;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getReplaceId() {
        return replaceId;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getReplaceIdAsOID() {
        return replaceIdAsOID;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getSymbolicId() {
        return symbolicId;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public CodedValue getType() {
        return type;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public boolean isGeneratedDocumentId() {
        return generatedDocumentId;
    }

    /**
     * Method description
     *
     *
     * @param content
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Method description
     *
     *
     * @param repositoryId
     */
    public void setDocumentIdAsOID(String repositoryId) {
        this.documentIdAsOID = repositoryId;
    }

    /**
     * Method description
     *
     *
     * @param format
     */
    public void setFormat(CodedValue format) {
        this.format = format;
    }

    /**
     * Method description
     *
     *
     * @param generatedDocumentId
     */
    public void setGeneratedDocumentId(boolean generatedDocumentId) {
        this.generatedDocumentId = generatedDocumentId;
    }

    /**
     * Method description
     *
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Method description
     *
     *
     * @param mimeType
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Method description
     *
     *
     * @param patientId
     */
    public void setPatientId(SubjectIdentifier patientId) {
        this.patientId = patientId;
    }

    /**
     * Method description
     *
     *
     * @param replaceExtrinsicObjectId
     */
    public void setReplaceExtrinsicObjectId(String replaceExtrinsicObjectId) {
        this.replaceExtrinsicObjectId = replaceExtrinsicObjectId;
    }

    /**
     * Method description
     *
     *
     * @param replaceId
     */
    public void setReplaceId(String replaceId) {
        this.replaceId = replaceId;
    }

    /**
     * Method description
     *
     *
     * @param replaceIdAsOID
     */
    public void setReplaceIdAsOID(String replaceIdAsOID) {
        this.replaceIdAsOID = replaceIdAsOID;
    }

    /**
     * Method description
     *
     *
     * @param symbolicId
     */
    public void setSymbolicId(String symbolicId) {
        this.symbolicId = symbolicId;
    }

    /**
     * Method description
     *
     *
     * @param type
     */
    public void setType(CodedValue type) {
        this.type = type;
    }
}
