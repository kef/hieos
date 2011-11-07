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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectSearchCriteria {

    private Subject subject = null;
    private int minimumDegreeMatchPercentage = 100;  // Default.
    private boolean specifiedMinimumDegreeMatchPercentage = false;
    private SubjectIdentifierDomain communityAssigningAuthority = null;
    private List<SubjectIdentifierDomain> scopingAssigningAuthorities = new ArrayList<SubjectIdentifierDomain>();

    /**
     *
     * @return
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     *
     * @param subject
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    /**
     * Return SubjectIdentifier in the given SubjectIdentifierDomain.  Return null if not found.
     *
     * @param identifierDomain
     * @return
     */
    public SubjectIdentifier getSubjectIdentifier(SubjectIdentifierDomain identifierDomain) {
        return subject.getSubjectIdentifier(identifierDomain);
    }

    /**
     * 
     * @return
     */
    public boolean hasSubjectDemographics() {
        return (subject != null) && subject.hasSubjectDemographics();
    }

    /**
     *
     * @return
     */
    public boolean hasSubjectIdentifiers() {
        return (subject != null) && subject.hasSubjectIdentifiers();
    }

    /**
     *
     * @return
     */
    public boolean hasScopingAssigningAuthorities() {
        return !scopingAssigningAuthorities.isEmpty();
    }

    /**
     *
     * @return
     */
    public SubjectIdentifierDomain getCommunityAssigningAuthority() {
        return communityAssigningAuthority;
    }

    /**
     * 
     * @param communityAssigningAuthority
     */
    public void setCommunityAssigningAuthority(SubjectIdentifierDomain communityAssigningAuthority) {
        this.communityAssigningAuthority = communityAssigningAuthority;
    }

    /**
     *
     * @return
     */
    public List<SubjectIdentifierDomain> getScopingAssigningAuthorities() {
        return scopingAssigningAuthorities;
    }

    /**
     * 
     * @param assigningAuthority
     */
    public void addScopingAssigningAuthority(SubjectIdentifierDomain assigningAuthority) {
        scopingAssigningAuthorities.add(assigningAuthority);
    }

    /**
     *
     * @param scopingAssigningAuthorities
     */
    public void setScopingAssigningAuthorities(List<SubjectIdentifierDomain> scopingAssigningAuthorities) {
        this.scopingAssigningAuthorities = scopingAssigningAuthorities;
    }

    /**
     *
     * @return
     */
    public int getMinimumDegreeMatchPercentage() {
        return minimumDegreeMatchPercentage;
    }

    /**
     * 
     * @param minimumDegreeMatchPercentage
     */
    public void setMinimumDegreeMatchPercentage(int minimumDegreeMatchPercentage) {
        this.minimumDegreeMatchPercentage = minimumDegreeMatchPercentage;
    }

    /**
     *
     * @return
     */
    public boolean hasSpecifiedMinimumDegreeMatchPercentage() {
        return specifiedMinimumDegreeMatchPercentage;
    }

    /**
     *
     * @param specifiedMinimumDegreeMatchPercentage
     */
    public void setSpecifiedMinimumDegreeMatchPercentage(boolean specifiedMinimumDegreeMatchPercentage) {
        this.specifiedMinimumDegreeMatchPercentage = specifiedMinimumDegreeMatchPercentage;
    }
}
