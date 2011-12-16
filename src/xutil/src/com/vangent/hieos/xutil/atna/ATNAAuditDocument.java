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
package com.vangent.hieos.xutil.atna;

/**
 *
 * @author Bernie Thuman
 */
public class ATNAAuditDocument {

    private String documentUniqueId;
    private String repositoryUniqueId;
    private String homeCommunityId;

    /**
     *
     */
    public ATNAAuditDocument() {
    }

    /**
     *
     * @return
     */
    public String getDocumentUniqueId() {
        return documentUniqueId;
    }

    /**
     *
     * @param documentUniqueId
     */
    public void setDocumentUniqueId(String documentUniqueId) {
        this.documentUniqueId = documentUniqueId;
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
    public String getRepositoryUniqueId() {
        return repositoryUniqueId;
    }

    /**
     *
     * @param repositoryUniqueId
     */
    public void setRepositoryUniqueId(String repositoryUniqueId) {
        this.repositoryUniqueId = repositoryUniqueId;
    }
}
