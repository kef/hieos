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
package com.vangent.hieos.services.pixpdq.mpi.adapter.impl.openempi;

import com.vangent.hieos.hl7v3util.model.subject.Address;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectGender;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectName;
import org.openhie.openempi.model.Gender;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.PersonIdentifier;

/**
 *
 * @author Bernie Thuman
 */
public class OpenEMPIModelBuilder {

    /**
     *
     * @param subject
     * @return
     */
    public Person buildPerson(Subject subject) {
        Person person = new Person();

        // BirthTime:
        person.setDateOfBirth(subject.getBirthTime());

        // Gender:
        if (subject.getGender() != null) {
            Gender gender = new Gender();
            gender.setGenderCode(subject.getGender().getCode());
            person.setGender(gender);
        }

        // Person Name.
        if (subject.getSubjectNames().size() > 0) {
            // TBD: Can only use one in OpenEMPI it appears.
            SubjectName subjectName = subject.getSubjectNames().get(0);
            person.setFamilyName(subjectName.getFamilyName());
            person.setGivenName(subjectName.getGivenName());
            person.setMiddleName(subjectName.getMiddleName());
            person.setPrefix(subjectName.getPrefix());
            person.setSuffix(subjectName.getSuffix());
        }

        // Address.
        if (subject.getAddresses().size() > 0) {
            // TBD: Can only use one in OpenEMPI it appears.
            Address address = subject.getAddresses().get(0);
            person.setAddress1(address.getStreetAddressLine());
            person.setCity(address.getCity());
            person.setState(address.getState());
            person.setPostalCode(address.getPostalCode());
            person.setCountry(address.getCountry());
        }

        // Identifiers.
        for (SubjectIdentifier subjectIdentifier : subject.getSubjectIdentifiers()) {
            PersonIdentifier personIdentifier = buildPersonIdentifier(subjectIdentifier);
            person.addPersonIdentifier(personIdentifier);
        }
        return person;
    }

    /**
     *
     * @param subjectIdentifier
     * @return
     */
    public PersonIdentifier buildPersonIdentifier(SubjectIdentifier subjectIdentifier) {
        SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setIdentifier(subjectIdentifier.getIdentifier());
        IdentifierDomain identifierDomain = new IdentifierDomain();
        identifierDomain.setUniversalIdentifier(subjectIdentifierDomain.getUniversalId());
        // FIXME?
        // seems to cause a bit of an issue with OpenEMPI so just set these manually in DB.
        // identifierDomain.setNamespaceIdentifier(subjectIdentifierDomain.getNamespaceId());
        identifierDomain.setUniversalIdentifierTypeCode(subjectIdentifierDomain.getUniversalIdType());
        personIdentifier.setIdentifierDomain(identifierDomain);
        return personIdentifier;
    }

    /**
     *
     * @param person
     * @param matchConfidencePercentage
     * @return
     */
    public Subject buildSubject(Person person, int matchConfidencePercentage) {
        Subject subject = new Subject();
        subject.setMatchConfidencePercentage(matchConfidencePercentage);

        // BirthTime:
        subject.setBirthTime(person.getDateOfBirth());

        // Gender:
        if (person.getGender() != null) {
            SubjectGender subjectGender = new SubjectGender();
            subjectGender.setCode(person.getGender().getGenderCode());
            subject.setGender(subjectGender);
        }

        // Person Name (only 1 apparently in OpenEMPI).
        SubjectName subjectName = new SubjectName();
        subjectName.setFamilyName(person.getFamilyName());
        subjectName.setGivenName(person.getGivenName());
        subjectName.setMiddleName(person.getMiddleName());
        subjectName.setPrefix(person.getPrefix());
        subjectName.setSuffix(person.getSuffix());
        subject.addSubjectName(subjectName);

        // TBD: Addresses
        Address address = new Address();
        address.setStreetAddressLine(person.getAddress1());
        address.setCity(person.getCity());
        address.setState(person.getState());
        address.setPostalCode(person.getPostalCode());
        address.setCountry(person.getCountry());
        subject.addAddress(address);

        // Identifiers.
        this.addIdentifiersToSubject(subject, person);
       
        return subject;
    }

    /**
     *
     * @param subject
     * @param person
     */
    public void addIdentifiersToSubject(Subject subject, Person person) {
        for (PersonIdentifier personIdentifier : person.getPersonIdentifiers()) {
            SubjectIdentifier subjectIdentifier = this.buildSubjectIdentifier(personIdentifier);
            subject.addSubjectIdentifier(subjectIdentifier);
        }
    }

    /**
     * 
     * @param personIdentifier
     * @return
     */
    public SubjectIdentifier buildSubjectIdentifier(PersonIdentifier personIdentifier) {
        IdentifierDomain identifierDomain = personIdentifier.getIdentifierDomain();
        SubjectIdentifier subjectIdentifier = new SubjectIdentifier();
        subjectIdentifier.setIdentifier(personIdentifier.getIdentifier());
        SubjectIdentifierDomain subjectIdentifierDomain = new SubjectIdentifierDomain();
        subjectIdentifierDomain.setUniversalId(identifierDomain.getUniversalIdentifier());
        subjectIdentifierDomain.setNamespaceId(identifierDomain.getNamespaceIdentifier());
        subjectIdentifierDomain.setUniversalIdType(identifierDomain.getUniversalIdentifierTypeCode());
        subjectIdentifier.setIdentifierDomain(subjectIdentifierDomain);
        return subjectIdentifier;
    }
}
