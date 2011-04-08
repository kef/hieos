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
package com.vangent.hieos.services.pixpdq.empi.impl.openempi;

import com.vangent.hieos.services.pixpdq.empi.api.EMPIAdapter;
import com.vangent.hieos.services.pixpdq.empi.exception.EMPIException;

import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

import org.openhie.openempi.context.Context;
import org.openhie.openempi.matching.MatchingService;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Person;
import org.openhie.openempi.model.Record;
import org.openhie.openempi.model.RecordPair;

/**
 *
 * @author Bernie Thuman
 */
public class OpenEMPIAdapter implements EMPIAdapter {

    private final static Logger logger = Logger.getLogger(OpenEMPIAdapter.class);
    private final static String ENTERPRISE_RECORD_INDICATOR = "E";
    //private final static String SYSTEM_RECORD_INDICATOR = "S";

    // ** TBD: FIGURE OUT WHAT GOES IN OpenEMPI conf, lib directories (and axis2 AAR).
    /**
     *
     * @param classLoader
     */
    public void startup(ClassLoader classLoader) {
        //
        // The code below is required to properly initialize the Spring Framework
        // within axis2.
        //
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        logger.info("Starting OpenEMPI init");
        Context.startup();  // Startup OpenEMPI.
        Thread.currentThread().setContextClassLoader(currentClassLoader);  // Restore.
        logger.info("OpenEMPI loaded");
    }

    /**
     *
     */
    public OpenEMPIAdapter() {
        // Do nothing.
    }

    /**
     *
     * @param subject
     * @return
     * @throws EMPIException
     */
    public Subject addSubject(Subject subject) throws EMPIException {
        Subject subjectAdded = null;
        try {
            OpenEMPIModelBuilder builder = new OpenEMPIModelBuilder();
            Person person = builder.buildPerson(subject);
            this.authenticate();
            Person personAdded = Context.getPersonManagerService().addPerson(person);
            subjectAdded = builder.buildSubject(personAdded, 100 /* matchConfidencePercentage */);
        } catch (Exception ex) {
            throw new EMPIException("EMPI EXCEPTION: when adding new Subject: " + ex.getMessage());
        }
        return subjectAdded;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    public SubjectSearchResponse findSubjects(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        // Prepare the response.
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        try {
            // Conduct the search ...
            if (subjectSearchCriteria.hasSubjectIdentifiers()) {
                logger.debug("Searching based on identifiers ...");
                subjectSearchResponse = this.findSubjectByIdentifier(subjectSearchCriteria, false);

            } else if (subjectSearchCriteria.hasSubjectDemographics()) {
                logger.debug("Searching based on demographics ...");
                subjectSearchResponse = this.findSubjectsByAttributes(subjectSearchCriteria);
            } else {
                // Do nothing ...
                logger.trace("Not searching at all!!");
            }
        } catch (Exception ex) {
            throw new EMPIException("EMPI EXCEPTION: when looking for Subjects: " + ex.getMessage());
        }
        return subjectSearchResponse;
    }

    /**
     * 
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    public SubjectSearchResponse findSubjectByIdentifier(
            SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {

        return this.findSubjectByIdentifier(subjectSearchCriteria, true);
    }

    /**
     *
     * @param subjectSearchCriteria
     * @param stripSearchSubjectIdentifier
     * @return
     * @throws EMPIException
     */
    private SubjectSearchResponse findSubjectByIdentifier(
            SubjectSearchCriteria subjectSearchCriteria, boolean shouldStripSearchSubjectIdentifier) throws EMPIException {

        // Prepare default response.
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();

        // Make sure we at least have one identifier to search from.
        Subject searchSubject = subjectSearchCriteria.getSubject();
        List<SubjectIdentifier> searchSubjectIdentifiers = searchSubject.getSubjectIdentifiers();
        if (searchSubjectIdentifiers.size() > 0) {
            Subject subject = null;

            // Pull the first identifier and convert.
            SubjectIdentifier subjectIdentifier = searchSubjectIdentifiers.get(0);
            OpenEMPIModelBuilder builder = new OpenEMPIModelBuilder();
            PersonIdentifier personIdentifier = builder.buildPersonIdentifier(subjectIdentifier);

            // Look for a match based on the subject identifier.
            Person matchedPerson = Context.getPersonQueryService().findPersonById(personIdentifier);
            if (matchedPerson != null) {
                Person enterprisePerson;
                if (this.isEnterprisePerson(matchedPerson)) {
                    // Just load.
                    enterprisePerson = Context.getPersonQueryService().loadPerson(matchedPerson.getPersonId());
                } else {
                    // Must have a System record, get the corresponding Enterprise Record.
                    enterprisePerson = this.getEnterprisePerson(matchedPerson);
                }
                this.print("MATCHED PERSON", enterprisePerson);

                // Convert Person into a HIEOS Subject.
                subject = builder.buildSubject(enterprisePerson, 100 /* matchConfidencePercentage */);

                // Add person links to the subject.
                this.getAndAddLinksToSubject(enterprisePerson, subject, builder);

                // Filter out those identifiers of no interest.
                SubjectIdentifier subjectIdentifierToStrip = subjectIdentifier;
                if (shouldStripSearchSubjectIdentifier == false) {
                    subjectIdentifierToStrip = null;
                }
                this.filterIdentifiers(subjectSearchCriteria, subject, subjectIdentifierToStrip);
            }
            if ((subject != null) && (subject.getSubjectIdentifiers().size() > 0)) {
                // Add Subject to SubjectSearchResponse.
                subjectSearchResponse.getSubjects().add(subject);
            }
        }

        return subjectSearchResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @param subject
     * @param subjectIdentifierToStrip
     */
    private void filterIdentifiers(SubjectSearchCriteria subjectSearchCriteria, Subject subject, SubjectIdentifier subjectIdentifierToStrip) {
        // Strip out the "subjectIdentifierToStrip" (if not null).
        if (subjectIdentifierToStrip != null) {
            subject.removeSubjectIdentifier(subjectIdentifierToStrip);
        }

        // Now should filter identifiers based upon scoping organization (assigning authority).
        if (subjectSearchCriteria.hasScopingAssigningAuthorities()) {

            List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
            List<SubjectIdentifier> copyOfSubjectIdentifiers = new ArrayList<SubjectIdentifier>();
            copyOfSubjectIdentifiers.addAll(subjectIdentifiers);

            // Go through each SubjectIdentifier.
            for (SubjectIdentifier subjectIdentifier : copyOfSubjectIdentifiers) {

                // Should we keep it?
                if (!this.shouldKeepIdentifier(subjectSearchCriteria, subjectIdentifier)) {
                    // Not a match ... disregard id (should not return id).
                    subjectIdentifiers.remove(subjectIdentifier);
                }
            }
        }
    }

    /**
     *
     * @param subjectSearchCriteria
     * @param subjectIdentifier
     * @return
     */
    private boolean shouldKeepIdentifier(SubjectSearchCriteria subjectSearchCriteria, SubjectIdentifier subjectIdentifier) {
        // Based on calling logic, already determined that scoping assigning authorities exist.

        // Get identifier domain for the given subject.
        SubjectIdentifierDomain identifierDomain = subjectIdentifier.getIdentifierDomain();

        // Now see if we should return the identifier or not.
        boolean keepIdentifier = false;
        for (SubjectIdentifierDomain aaDomain : subjectSearchCriteria.getScopingAssigningAuthorities()) {
            if (identifierDomain.equals(aaDomain)) {
                keepIdentifier = true;
                break;
            }
        }
        return keepIdentifier;
    }

    /**
     * 
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    private SubjectSearchResponse findSubjectsByAttributes(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        // Prepare the response.
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        List<Subject> subjects = subjectSearchResponse.getSubjects();

        // Create the builder.
        OpenEMPIModelBuilder builder = new OpenEMPIModelBuilder();

        // Convert the HIEOS search Subject into an OpenEMPI Person instance.
        Subject searchSubject = subjectSearchCriteria.getSubject();
        Person personSearchTemplate = builder.buildPerson(searchSubject);
        personSearchTemplate.setCustom1(OpenEMPIAdapter.ENTERPRISE_RECORD_INDICATOR);  // Look only for Enterprise records.

        // HACK ... Have to use custom field to house gender code for simple compare
        if (personSearchTemplate.getGender() != null) {
            personSearchTemplate.setCustom4(personSearchTemplate.getGender().getGenderCode());
        }

        Record record = new Record(personSearchTemplate);
        record.setRecordId(Long.MAX_VALUE);
        try {
            MatchingService matchingService = Context.getMatchingService();
            this.authenticate();  // Needed?

            // Conduct the matching ... do not count null search fields as a negative.
            Set<RecordPair> matches = matchingService.match(record, false);
            for (RecordPair matchedRecordPair : matches) {

                // RightRecord should be the matching record.
                Record rightRecord = matchedRecordPair.getRightRecord();
                Person rightPerson = (Person) rightRecord.getObject();

                // Need to expand the record.
                Person enterprisePerson = Context.getPersonQueryService().loadPerson(rightPerson.getPersonId());
                this.print("MATCHED PERSON (weight: " + matchedRecordPair.getWeight() + "):", enterprisePerson);

                Subject subject = builder.buildSubject(enterprisePerson, this.getMatchConfidencePercentage(matchedRecordPair));

                // Add identifiers for linked persons to the Subject.
                this.getAndAddLinksToSubject(enterprisePerson, subject, builder);

                // Filter unwanted results.
                this.filterIdentifiers(subjectSearchCriteria, subject, null);

                // If we kept at least one identifier ...
                if (subject.getSubjectIdentifiers().size() > 0) {
                    subjects.add(subject);
                }
            }
        } catch (Exception ex) {
            throw new EMPIException("EMPI EXCEPTION: when looking for Subjects: " + ex.getMessage());
        }
        return subjectSearchResponse;
    }

    /**
     * Add identifiers for linked Persons to the given Subject.
     *
     * @param enterprisePerson
     * @param subject
     * @param builder
     */
    private void getAndAddLinksToSubject(Person enterprisePerson, Subject subject, OpenEMPIModelBuilder builder) {
        // FIXME: Should we do this elsewhere???? in OpenEMPI????

        // Go through all identifiers for the given Enterprise Person.
        Set<PersonIdentifier> personIdentifiers = enterprisePerson.getPersonIdentifiers();
        for (PersonIdentifier personIdentifier : personIdentifiers) {  // Should only loop once.

            // Get linked Persons for the given PersonIdentifier.
            List<Person> linkedPersons = Context.getPersonQueryService().findLinkedPersons(personIdentifier);

            // Go through all linked Persons.
            for (Person linkedPerson : linkedPersons) {

                // Load the linked person.
                Person loadedLinkedPerson = Context.getPersonQueryService().loadPerson(linkedPerson.getPersonId());
                this.print("LINKED PERSON", loadedLinkedPerson);

                // Add identifiers to given Subject (will ensure ids are not added more than once).
                builder.addIdentifiersToSubject(subject, loadedLinkedPerson);
            }
        }
    }

    /**
     *
     * @param systemPerson
     * @return
     */
    private Person getEnterprisePerson(Person systemPerson) {
        Person enterprisePerson = null;

        // Loop through all identifiers for system person.
        Set<PersonIdentifier> personIdentifiers = systemPerson.getPersonIdentifiers();
        for (PersonIdentifier personIdentifier : personIdentifiers) {

            // Get linked Persons for the given PersonIdentifier.
            List<Person> linkedPersons = Context.getPersonQueryService().findLinkedPersons(personIdentifier);

            // Loop through linked persons.
            for (Person linkedPerson : linkedPersons) {

                // If found Enterprise record, break out.
                if (this.isEnterprisePerson(linkedPerson)) {

                    // Load the linked person (the Enterprise person) ....
                    enterprisePerson = Context.getPersonQueryService().loadPerson(linkedPerson.getPersonId());
                    break;
                }
            }
            if (enterprisePerson != null) {
                break;  // Get out of main loop.
            }
        }
        return enterprisePerson;
    }

    /**
     *
     * @param recordPair
     * @return
     */
    private int getMatchConfidencePercentage(RecordPair recordPair) {
        BigDecimal bd = new BigDecimal(recordPair.getWeight() * 100);
        bd = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
        return bd.intValue();
    }

    /**
     *
     * @param person
     * @return
     */
    private boolean isEnterprisePerson(Person person) {
        return person.getCustom1().equals(OpenEMPIAdapter.ENTERPRISE_RECORD_INDICATOR);
    }

    /**
     *
     * @param person
     */
    private void print(String text, Person person) {
        logger.trace(text);
        logger.trace("  custom1 (indicator) = " + person.getCustom1());
        logger.trace("  gender = " + person.getGender().getGenderCode());
        logger.trace("  givenName = " + person.getGivenName());
        logger.trace("  familyName = " + person.getFamilyName());
        logger.trace("  dateOfBirth = " + person.getDateOfBirth());

        // Person identifiers.
        Set<PersonIdentifier> personIdentifiers = person.getPersonIdentifiers();
        if (personIdentifiers != null) {
            for (PersonIdentifier personIdentifier : personIdentifiers) {
                IdentifierDomain identifierDomain = personIdentifier.getIdentifierDomain();
                logger.trace("  ... pid = " + personIdentifier.getIdentifier());
                logger.trace("      ... universalID = " + identifierDomain.getUniversalIdentifier());
                logger.trace("      ... universalIDNameSpace = " + identifierDomain.getNamespaceIdentifier());
                logger.trace("      ... universalIDTypeCode = " + identifierDomain.getUniversalIdentifierTypeCode());
            }

        }
    }

    /**
     *
     */
    private void authenticate() {
        String sessionKey;
        if (Context.getUserContext() != null && Context.getUserContext().getSessionKey() != null) {
            sessionKey = Context.getUserContext().getSessionKey();
        }

        sessionKey = Context.authenticate("admin", "admin"); // TBD: FIXME!!!
    }
}
