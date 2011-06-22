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
import java.util.List;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class SubmitDocumentRequest implements Serializable {

    /** Field description */
    private List<Document> documents;

    /** Field description */
    private Identifier organizationId;

    /** Field description */
    private Identifier patientId;

    /**
     * Constructs ...
     *
     */
    public SubmitDocumentRequest() {
        super();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<Document> getDocuments() {
        return documents;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Identifier getOrganizationId() {
        return organizationId;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Identifier getPatientId() {
        return patientId;
    }

    /**
     * Method description
     *
     *
     * @param documents
     */
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    /**
     * Method description
     *
     *
     * @param organizationId
     */
    public void setOrganizationId(Identifier organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Method description
     *
     *
     * @param patientId
     */
    public void setPatientId(Identifier patientId) {
        this.patientId = patientId;
    }
}
