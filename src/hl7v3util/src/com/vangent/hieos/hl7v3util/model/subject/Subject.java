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
import java.util.Date;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class Subject {

    private List<SubjectIdentifier> subjectIdentifiers = new ArrayList<SubjectIdentifier>();
    private List<SubjectName> subjectNames = new ArrayList<SubjectName>();
    private List<Address> addresses = new ArrayList<Address>();
    private SubjectGender gender = null;
    private Custodian custodian = null;
    private Date birthTime = null;
    private Double matchWeight = 0.0;

    public Double getMatchWeight() {
        return matchWeight;
    }

    public void setMatchWeight(Double matchWeight) {
        this.matchWeight = matchWeight;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public void addAddress(Address address) {
        this.addresses.add(address);
    }

    public List<SubjectIdentifier> getSubjectIdentifiers() {
        return subjectIdentifiers;
    }

    public void setSubjectIdentifiers(List<SubjectIdentifier> subjectIdentifiers) {
        this.subjectIdentifiers = subjectIdentifiers;
    }

    public void addSubjectIdentifier(SubjectIdentifier subjectIdentifier) {
        this.subjectIdentifiers.add(subjectIdentifier);
    }

    public List<SubjectName> getSubjectNames() {
        return subjectNames;
    }

    public void addSubjectName(SubjectName subjectName) {
        this.subjectNames.add(subjectName);
    }

    public void setSubjectNames(List<SubjectName> subjectNames) {
        this.subjectNames = subjectNames;
    }

    public SubjectGender getGender() {
        return gender;
    }

    public void setGender(SubjectGender gender) {
        this.gender = gender;
    }

    public Date getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(Date birthTime) {
        this.birthTime = birthTime;
    }

    public Custodian getCustodian() {
        return custodian;
    }

    public void setCustodian(Custodian custodian) {
        this.custodian = custodian;
    }

    /**
     * Return SubjectIdentifier in the given SubjectIdentifierDomain.  Return null if not found.
     *
     * @param identifierDomain
     * @return
     */
    public SubjectIdentifier getSubjectIdentifier(SubjectIdentifierDomain identifierDomain) {
        for (SubjectIdentifier subjectIdentifier : this.subjectIdentifiers) {
            SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
            if (subjectIdentifierDomain.getUniversalId().equals(identifierDomain.getUniversalId())) {
                return subjectIdentifier;
            }
        }
        return null;
    }

    /**
     * 
     * @param identifier
     * @return
     */
    public boolean hasSubjectIdentifier(SubjectIdentifier identifier) {
        SubjectIdentifierDomain identifierDomain = identifier.getIdentifierDomain();
        // See if we find our subject identifier.
        for (SubjectIdentifier subjectIdentifier : this.getSubjectIdentifiers()) {
            SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
            if (subjectIdentifierDomain.getUniversalId().equals(identifierDomain.getUniversalId()) 
                    && subjectIdentifier.getIdentifier().equals(identifier.getIdentifier())) {
                return true;  // Match.
            }
        }
        return false;  // No match.
    }
}
