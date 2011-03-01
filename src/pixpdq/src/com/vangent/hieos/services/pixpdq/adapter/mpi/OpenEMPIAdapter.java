/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.pixpdq.adapter.mpi;

import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.services.pixpdq.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openhie.openempi.context.Context;
import org.openhie.openempi.model.PersonIdentifier;
import org.openhie.openempi.model.IdentifierDomain;
import org.openhie.openempi.model.Person;

/**
 *
 * @author Bernie Thuman
 */
public class OpenEMPIAdapter implements EMPIAdapter {

    private final static Logger logger = Logger.getLogger(OpenEMPIAdapter.class);

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
        System.out.println("Starting OpenEMPI init");
        Context.startup();  // Startup OpenEMPI.
        // HACK -- startup does not properly initialize the matching service.
        Context.getMatchingService().init();
        //Context.getMatchingService().linkRecords();

        // HACK (END)
        Thread.currentThread().setContextClassLoader(currentClassLoader);  // Restore.
        System.out.println("OpenEMPI loaded");
    }

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
            Person person = builder.buildPersonFromSubject(subject);
            this.authenticate();
            Person personAdded = Context.getPersonManagerService().addPerson(person);
            subjectAdded = builder.buildSubjectFromPerson(personAdded, 1.0);
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
        List<Subject> responseSubjects = subjectSearchResponse.getSubjects();
        try {
            // First convert the HIEOS  search Subject into an OpenEMPI Person instance.
            Subject searchSubject = subjectSearchCriteria.getSubject();
            OpenEMPIModelBuilder builder = new OpenEMPIModelBuilder();
            Person personSearchTemplate = builder.buildPersonFromSubject(searchSubject);
            // HACK ... Have to use custom field to house gender code for simple compare
            if (personSearchTemplate.getGender() != null) {
                personSearchTemplate.setCustom3(personSearchTemplate.getGender().getGenderCode());
            }

            // Conduct the search ...
            List<Subject> matchedSubjects = new ArrayList<Subject>();
            if (subjectSearchCriteria.hasSubjectIdentifiers()) {
                System.out.println("Searching based on identifiers ...");
                // Have to do this differently, otherwise OpenEMPI returns the entire DB matching
                // the assigning authority specified.
                Subject matchedSubject = this.findSubjectByIdentifier(personSearchTemplate);
                if (matchedSubject != null) {
                    matchedSubjects.add(matchedSubject);
                }
            } else if (subjectSearchCriteria.hasSubjectDemographics()) {
                System.out.println("Searching based on demographics ...");
                matchedSubjects = this.findSubjectsByAttributes(personSearchTemplate);
            } else {
                System.out.println("Not searching at all!!");
                // Do nothing ...
            }
            if (subjectSearchCriteria.hasScopingAssigningAuthorities()) {
                // Now should filter based upon scoping organization (assigning authority).

                // Go through each matched subject.
                for (Subject subject : matchedSubjects) {
                    List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
                    List<SubjectIdentifier> copyOfSubjectIdentifiers = new ArrayList<SubjectIdentifier>();
                    copyOfSubjectIdentifiers.addAll(subjectIdentifiers);

                    // Go through each subject identifier.
                    boolean retainedAtLeastOneSubjectId = false;
                    for (SubjectIdentifier subjectIdentifier : copyOfSubjectIdentifiers) {
                        SubjectIdentifierDomain identifierDomain = subjectIdentifier.getIdentifierDomain();

                        // Now see if we should return the identifier or not.
                        for (SubjectIdentifierDomain aaDomain : subjectSearchCriteria.getScopingAssigningAuthorities()) {
                            if (identifierDomain.getUniversalId().equals(aaDomain.getUniversalId()) && identifierDomain.getUniversalIdType().equals(aaDomain.getUniversalIdType())) {
                                retainedAtLeastOneSubjectId = true;  // Kept at least one.
                            } else {
                                // Not a match ... disregard id (should not return id).
                                subjectIdentifiers.remove(subjectIdentifier);
                            }
                        }
                    }
                    if (retainedAtLeastOneSubjectId == true) {
                        // Add to the response if we retained at least one id for the subject.
                        responseSubjects.add(subject);
                    }
                }
            } else {
                // Just return the result with all known identifiers ...
                responseSubjects.addAll(matchedSubjects);
            }
        } catch (Exception ex) {
            throw new EMPIException("EMPI EXCEPTION: when looking for Subjects: " + ex.getMessage());
        }


        return subjectSearchResponse;
    }

    /**
     *
     * @param person
     */
    private List<Subject> findSubjectsByAttributes(Person personSearchTemplate) {
        List<Subject> subjects = new ArrayList<Subject>();
        OpenEMPIModelBuilder builder = new OpenEMPIModelBuilder();
        List<Person> persons = Context.getPersonQueryService().findPersonsByAttributes(personSearchTemplate);
        for (Person matchedPerson : persons) {
            Person loadedPerson = Context.getPersonQueryService().loadPerson(matchedPerson.getPersonId());
            this.print(loadedPerson);
            // TBD?: NEED TO DEAL WITH LINKS!!
            Subject subject = builder.buildSubjectFromPerson(loadedPerson, 100.0);
            subjects.add(subject);
        }
        return subjects;
        /*
        try {
        OpenEMPIModelBuilder builder = new OpenEMPIModelBuilder();
        Subject searchSubject = subjectSearchCriteria.getSubject();
        Person person = builder.buildPersonFromSubject(searchSubject);
        Record record = new Record(person);
        record.setRecordId(Long.MAX_VALUE);  // FIXME: HACK

        MatchingService matchingService = Context.getMatchingService();
        this.authenticate();
        Set<RecordPair> links = matchingService.match(record);
        // FIXME: Deal with links from PERSON (also, need to avoid dups).
        for (RecordPair recordPair : links) {
        // RightRecord should be the matching record.
        Double matchWeight = recordPair.getWeight();
        Record rightRecord = recordPair.getRightRecord();
        Person rightPerson = (Person) rightRecord.getObject();
        Person matchedPerson = Context.getPersonQueryService().loadPerson(rightPerson.getPersonId());
        System.out.println("Matched Person: ");
        System.out.println("  weight = " + matchWeight);
        System.out.println("  gender = " + matchedPerson.getGender().getGenderCode());
        System.out.println("  givenName = " + matchedPerson.getGivenName());
        System.out.println("  familyName = " + matchedPerson.getFamilyName());
        System.out.println("  dateOfBirth = " + matchedPerson.getDateOfBirth());
        // Person identifiers.
        Set<PersonIdentifier> personIdentifiers = matchedPerson.getPersonIdentifiers();
        if (personIdentifiers != null) {
        for (PersonIdentifier personIdentifier : personIdentifiers) {
        IdentifierDomain identifierDomain = personIdentifier.getIdentifierDomain();
        System.out.println("  ... pid = " + personIdentifier.getIdentifier());
        System.out.println("      ... universalID = " + identifierDomain.getUniversalIdentifier());
        System.out.println("      ... universalIDNameSpace = " + identifierDomain.getNamespaceIdentifier());
        System.out.println("      ... universalIDTypeCode = " + identifierDomain.getUniversalIdentifierTypeCode());
        }
        }
        Subject subject = builder.buildSubjectFromPerson(matchedPerson, matchWeight);
        subjects.add(subject);
        }
        } catch (Exception ex) {
        // TBD: Do something.
        System.out.println("EMPI EXCEPTION: when looking for Subjects: " + ex.getMessage());
        }*/
    }

    /**
     *
     * @param person
     * @return
     */
    private Subject findSubjectByIdentifier(Person personSearchTemplate) {
        OpenEMPIModelBuilder builder = new OpenEMPIModelBuilder();
        List<PersonIdentifier> personIdentifiers = new ArrayList<PersonIdentifier>();
        personIdentifiers.addAll(personSearchTemplate.getPersonIdentifiers());

        // FIXME: This may not be accurate ... links ... are all search ids in the result???
        Person matchedPerson = Context.getPersonManagerService().getPerson(personIdentifiers);
        if (matchedPerson != null) {
            Person loadedPerson = Context.getPersonQueryService().loadPerson(matchedPerson.getPersonId());
            this.print(loadedPerson);

            // TBD?: NEED TO DEAL WITH LINKS!!
            Subject subject = builder.buildSubjectFromPerson(loadedPerson, 100.0);
            return subject;
        } else {
            return null;
        }
    }

    /**
     *
     * @param subjectIdentifier
     * @return
     * @throws EMPIException
     */
    public SubjectSearchResponse findSubjectByIdentifier(SubjectIdentifier subjectIdentifier) throws EMPIException {
        Subject subject = null;
        OpenEMPIModelBuilder builder = new OpenEMPIModelBuilder();
        PersonIdentifier personIdentifier =
                builder.buildPersonIdentifierFromSubjectIdentifier(subjectIdentifier);
        Person matchedPerson = Context.getPersonQueryService().findPersonById(personIdentifier);
        if (matchedPerson != null) {
            Person loadedPerson = Context.getPersonQueryService().loadPerson(matchedPerson.getPersonId());
            this.print(loadedPerson);
            // FIXME: NEED TO DEAL WITH LINKS!!
            subject = builder.buildSubjectFromPerson(loadedPerson, 100.0 /* FIXME */);
        }
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        if (subject != null) {
            List<Subject> subjects = subjectSearchResponse.getSubjects();
            subjects.add(subject);
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param person
     */
    private void print(Person person) {
        System.out.println("Matched Person: ");
        System.out.println("  gender = " + person.getGender().getGenderCode());
        System.out.println("  givenName = " + person.getGivenName());
        System.out.println("  familyName = " + person.getFamilyName());
        System.out.println("  dateOfBirth = " + person.getDateOfBirth());

        // Person identifiers.
        Set<PersonIdentifier> personIdentifiers = person.getPersonIdentifiers();
        if (personIdentifiers != null) {
            for (PersonIdentifier personIdentifier : personIdentifiers) {
                IdentifierDomain identifierDomain = personIdentifier.getIdentifierDomain();
                System.out.println("  ... pid = " + personIdentifier.getIdentifier());
                System.out.println("      ... universalID = " + identifierDomain.getUniversalIdentifier());
                System.out.println("      ... universalIDNameSpace = " + identifierDomain.getNamespaceIdentifier());
                System.out.println("      ... universalIDTypeCode = " + identifierDomain.getUniversalIdentifierTypeCode());
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
