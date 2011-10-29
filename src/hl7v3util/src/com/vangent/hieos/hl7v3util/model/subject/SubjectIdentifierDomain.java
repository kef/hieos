/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v3util.model.subject;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectIdentifierDomain {

    private int id;
    private String namespaceId;
    private String universalId;
    private String universalIdType;

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public String getNamespaceId() {
        return namespaceId;
    }

    /**
     *
     * @param namespaceId
     */
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    /**
     *
     * @return
     */
    public String getUniversalId() {
        return universalId;
    }

    /**
     *
     * @param universalId
     */
    public void setUniversalId(String universalId) {
        this.universalId = universalId;
    }

    /**
     *
     * @return
     */
    public String getUniversalIdType() {
        return universalIdType;
    }

    /**
     * 
     * @param universalIdType
     */
    public void setUniversalIdType(String universalIdType) {
        this.universalIdType = universalIdType;
    }

    /**
     *
     * @param subjectIdentifierDomain
     * @return
     */
    public boolean equals(SubjectIdentifierDomain subjectIdentifierDomain) {
        // FIXME?: Only looks at ID & Type since namespaceId could be problematic ...
        return subjectIdentifierDomain.getUniversalId().equals(this.universalId)
                && subjectIdentifierDomain.getUniversalIdType().equals(this.universalIdType);
    }
}
