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

import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class DocumentResponse {

    private OMElement documentResponseObject;
    private String documentId;
    private String repositoryId;
    private String homeCommunityId;  // Future.

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
    public OMElement getDocumentResponseObject() {
        return documentResponseObject;
    }

    /**
     *
     * @param documentResponseObject
     */
    public void setDocumentResponseObject(OMElement documentResponseObject) {
        this.documentResponseObject = documentResponseObject;
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
}
