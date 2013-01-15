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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectSearchCriteria implements Cloneable, Serializable {

    private Subject subject = null;
    private int minimumDegreeMatchPercentage = 100;  // Default.
    private boolean specifiedMinimumDegreeMatchPercentage = false;
    private SubjectIdentifierDomain communitySubjectIdentifierDomain = null;
    private List<SubjectIdentifierDomain> scopingSubjectIdentifierDomains = new ArrayList<SubjectIdentifierDomain>();

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
     * @param subjectIdentifierDomain
     * @return
     */
    public SubjectIdentifier getSubjectIdentifier(SubjectIdentifierDomain subjectIdentifierDomain) {
        return subject.getSubjectIdentifier(subjectIdentifierDomain);
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
    public boolean hasScopingSubjectIdentifierDomains() {
        return !scopingSubjectIdentifierDomains.isEmpty();
    }

    /**
     *
     * @return
     */
    public SubjectIdentifierDomain getCommunitySubjectIdentifierDomain() {
        return communitySubjectIdentifierDomain;
    }

    /**
     * 
     * @param subjectIdentifierDomain
     */
    public void setCommunitySubjectIdentifierDomain(SubjectIdentifierDomain subjectIdentifierDomain) {
        this.communitySubjectIdentifierDomain = subjectIdentifierDomain;
    }

    /**
     *
     * @return
     */
    public List<SubjectIdentifierDomain> getScopingSubjectIdentifierDomains() {
        return scopingSubjectIdentifierDomains;
    }

    /**
     * 
     * @param subjectIdentifierDomain
     */
    public void addScopingSubjectIdentifierDomain(SubjectIdentifierDomain subjectIdentifierDomain) {
        scopingSubjectIdentifierDomains.add(subjectIdentifierDomain);
    }

    /**
     *
     * @param subjectIdentifierDomains
     */
    public void setScopingSubjectIdentifierDomains(List<SubjectIdentifierDomain> subjectIdentifierDomains) {
        this.scopingSubjectIdentifierDomains = subjectIdentifierDomains;
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

    /**
     * 
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SubjectSearchCriteria copy = (SubjectSearchCriteria) super.clone();
        if (subject != null) {
            copy.subject = (Subject) subject.clone();
        }
        if (communitySubjectIdentifierDomain != null) {
            copy.communitySubjectIdentifierDomain = (SubjectIdentifierDomain) communitySubjectIdentifierDomain.clone();
        }
        copy.scopingSubjectIdentifierDomains = SubjectIdentifierDomain.clone(scopingSubjectIdentifierDomains);
        return copy;
    }
}
