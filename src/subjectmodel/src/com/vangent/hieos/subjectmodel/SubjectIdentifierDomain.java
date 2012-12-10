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
package com.vangent.hieos.subjectmodel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectIdentifierDomain implements Cloneable {

    private int id = -1;
    private String namespaceId = null;
    private String universalId = null;
    private String universalIdType = null;

    /**
     *
     */
    public SubjectIdentifierDomain() {
    }

    /**
     *
     * @param domainCXFormatted
     */
    public SubjectIdentifierDomain(String domainCXFormatted) {
        this.buildFromDomainCXFormatted(domainCXFormatted);
    }

    /**
     *
     * @param domainCXFormatted
     */
    private void buildFromDomainCXFormatted(String domainCXFormatted) {
        // 5cfe5f4f31604fa^^^&1.3.6.1.4.1.21367.2005.3.7&ISO
        String parts[] = domainCXFormatted.split("\\^");
        if (parts.length == 4) {
            //this.identifier = parts[0];

            // Assigning authority.
            String aa[] = parts[3].split("\\&");
            if (aa.length > 0) {
                this.setNamespaceId(aa[0]);
            }
            if (aa.length > 1) {
                this.setUniversalId(aa[1]);
            }
            if (aa.length > 2) {
                this.setUniversalIdType(aa[2]);
            }
        }
    }

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
     * @return
     */
    public boolean isLoaded() {
        return id != -1;
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

    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Identifier Domain (HD formatted) = ").append(this.getHDFormatted());
        return result.toString();
    }

    /**
     *
     * @return
     */
    public String getHDFormatted() {
        StringBuilder result = new StringBuilder();
        if (namespaceId != null) {
            result.append(namespaceId);
        }
        result.append("^");
        if (universalId != null) {
            result.append(universalId);
        }
        result.append("^");
        if (universalIdType != null) {
            result.append(universalIdType);
        }
        return result.toString();
    }

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     *
     * @param listToClone
     * @return
     * @throws CloneNotSupportedException
     */
    public static List<SubjectIdentifierDomain> clone(List<SubjectIdentifierDomain> listToClone) throws CloneNotSupportedException {
        List<SubjectIdentifierDomain> copy = null;
        if (listToClone != null) {
            copy = new ArrayList<SubjectIdentifierDomain>();
            for (SubjectIdentifierDomain elementToClone : listToClone) {
                SubjectIdentifierDomain clonedElement = (SubjectIdentifierDomain) elementToClone.clone();
                copy.add(clonedElement);
            }
        }
        return copy;
    }
}
