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
package com.vangent.hieos.empi.impl.base;

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.match.MatchAlgorithm;
import com.vangent.hieos.empi.match.MatchAlgorithm.MatchType;
import com.vangent.hieos.empi.match.MatchResults;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.match.RecordBuilder;
import com.vangent.hieos.empi.match.ScoredRecord;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.validator.FindSubjectsValidator;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.InternalId;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class FindSubjectsHandler extends BaseHandler {

    private static final Logger logger = Logger.getLogger(FindSubjectsHandler.class);

    /**
     *
     * @param configActor
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    public FindSubjectsHandler(XConfigActor configActor, PersistenceManager persistenceManager, DeviceInfo senderDeviceInfo) {
        super(configActor, persistenceManager, senderDeviceInfo);
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    public SubjectSearchResponse findSubjects(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();

        // First, run validations on input.
        FindSubjectsValidator validator = new FindSubjectsValidator(pm, this.getSenderDeviceInfo());
        validator.validate(subjectSearchCriteria);

        // Create default response.
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();

        // FIXME: This is not entirely accurate, how about "other ids"?
        // Determine which path to take.
        if (subjectSearchCriteria.hasSubjectIdentifiers()) {
            logger.trace("Searching based on identifiers ...");
            subjectSearchResponse = this.loadSubjectMatchesByIdentifiers(subjectSearchCriteria);
        } else if (subjectSearchCriteria.hasSubjectDemographics()) {
            logger.trace("Searching based on demographics ...");
            subjectSearchResponse = this.loadSubjectMatchesByDemographics(subjectSearchCriteria);
        } else {
            // Do nothing ...
            logger.trace("Not searching at all!!");
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    public SubjectSearchResponse findSubjectIdentifiers(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();

        // First, run validations on input.
        FindSubjectsValidator validator = new FindSubjectsValidator(this.getPersistenceManager(),
                this.getSenderDeviceInfo());
        validator.validate(subjectSearchCriteria);

        // Now, find subjects using the identifier in the search criteria.
        subjectSearchResponse = this.loadSubjectIdentifiers(subjectSearchCriteria);
        return subjectSearchResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    private SubjectSearchResponse loadSubjectMatchesByDemographics(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        Subject searchSubject = subjectSearchCriteria.getSubject();

        // Run the matching algorithm (in "find" mode).
        RecordBuilder rb = new RecordBuilder();
        Record searchRecord = rb.build(searchSubject);
        MatchAlgorithm matchAlgo = MatchAlgorithm.getMatchAlgorithm(pm);
        MatchResults matchResults = matchAlgo.findMatches(searchRecord, MatchType.SUBJECT_FIND);

        long startTime = System.currentTimeMillis();

        // Get EnterpriseSubjectManager to load unique enterprise subjects.
        EnterpriseSubjectLoader enterpriseSubjectLoader = new EnterpriseSubjectLoader(pm, true /* loadFullSubjects */);

        // Now load subjects from the match results.
        this.loadEnterpriseSubjectsFromMatchResults(subjectSearchCriteria, matchResults, enterpriseSubjectLoader);

        // Get subject search response.
        subjectSearchResponse = this.getSubjectSearchResponse(subjectSearchCriteria, enterpriseSubjectLoader, null /* subjectIdentifierToRemove */);

        long endTime = System.currentTimeMillis();
        if (logger.isTraceEnabled()) {
            logger.trace("FindSubjectsHandler.loadSubjectMatchesByDemographics.loadSubjects: elapedTimeMillis=" + (endTime - startTime));
        }
        //subjectSearchResponse.getSubjects().addAll(subjectMatches);
        return subjectSearchResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    private SubjectSearchResponse loadSubjectMatchesByIdentifiers(
            SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();

        // Make sure we at least have one identifier to search from.
        Subject searchSubject = subjectSearchCriteria.getSubject();
        List<SubjectIdentifier> searchSubjectIdentifiers = searchSubject.getSubjectIdentifiers();
        if (!searchSubjectIdentifiers.isEmpty()) {
            // Get EnterpriseSubjectManager to load unique enterprise subjects.
            EnterpriseSubjectLoader enterpriseSubjectLoader = new EnterpriseSubjectLoader(pm, true /* loadFullSubjects */);

            // Get the base subjects (only base-level information) to determine type and internal ids.
            List<Subject> baseSubjects = pm.loadBaseSubjectsByIdentifier(searchSubjectIdentifiers);

            // Now, filter on demographics if required.
            if (!baseSubjects.isEmpty() && subjectSearchCriteria.hasSubjectDemographics()) {
                // Get a search record.
                RecordBuilder rb = new RecordBuilder();
                Record searchRecord = rb.build(searchSubject);
                List<Record> candidateRecords = new ArrayList<Record>();

                // For each base subject that was found based on the subject identifier(s).
                for (Subject baseSubject : baseSubjects) {
                    // Load the subject match fields for this base subject.
                    Record candidateRecord = pm.loadSubjectMatchRecord(baseSubject.getInternalId(), MatchType.SUBJECT_FIND);
                    candidateRecords.add(candidateRecord);

                }
                MatchAlgorithm matchAlgo = MatchAlgorithm.getMatchAlgorithm(pm);

                // Now, run the matching algorithm.
                MatchResults matchResults = matchAlgo.findMatches(searchRecord, candidateRecords, MatchType.SUBJECT_FIND);

                // Now load subjects from the match results.
                this.loadEnterpriseSubjectsFromMatchResults(subjectSearchCriteria, matchResults, enterpriseSubjectLoader);
            } else {

                // Load unique enterprise subjects.
                for (Subject baseSubject : baseSubjects) {
                    EnterpriseSubjectLoaderResult loaderResult = enterpriseSubjectLoader.loadEnterpriseSubject(baseSubject);
                    loaderResult.getEnterpriseSubject().setMatchConfidencePercentage(100);
                }
            }

            // Get subject search response.
            subjectSearchResponse = this.getSubjectSearchResponse(subjectSearchCriteria, enterpriseSubjectLoader, null /* subjectIdentifierToRemove */);
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @param matchResults
     * @param enterpriseSubjectLoader
     * @throws EMPIException
     */
    private void loadEnterpriseSubjectsFromMatchResults(SubjectSearchCriteria subjectSearchCriteria,
            MatchResults matchResults, EnterpriseSubjectLoader enterpriseSubjectLoader) throws EMPIException {
        // Now get records that fall within the minimum degree match percentage (if specified).
        List<ScoredRecord> matchedRecords =
                this.filterMatchedRecordsByMinimumDegreeMatchPercentage(subjectSearchCriteria, matchResults.getMatches());

        for (ScoredRecord scoredRecord : matchedRecords) {
            Record record = scoredRecord.getRecord();
            InternalId systemSubjectId = record.getInternalId();
            EnterpriseSubjectLoaderResult loaderResult = enterpriseSubjectLoader.loadEnterpriseSubject(systemSubjectId);
            if (!loaderResult.isAlreadyExists()) {
                // Only set match confidence percentage on first load.
                Subject enterpriseSubject = loaderResult.getEnterpriseSubject();
                int matchConfidencePercentage = scoredRecord.getMatchScorePercentage();
                enterpriseSubject.setMatchConfidencePercentage(matchConfidencePercentage);
            }
        }
    }

  
    /**
     *
     * @param subjectSearchCriteria
     * @param matchedRecords
     * @return
     */
    private List<ScoredRecord> filterMatchedRecordsByMinimumDegreeMatchPercentage(SubjectSearchCriteria subjectSearchCriteria, List<ScoredRecord> matchedRecords) {
        List<ScoredRecord> results = new ArrayList<ScoredRecord>();
        boolean hasSpecifiedMinimumDegreeMatchPercentage = subjectSearchCriteria.hasSpecifiedMinimumDegreeMatchPercentage();
        int minimumDegreeMatchPercentage = subjectSearchCriteria.getMinimumDegreeMatchPercentage();
        for (ScoredRecord scoredRecord : matchedRecords) {
            int matchConfidencePercentage = scoredRecord.getMatchScorePercentage();
            if (logger.isTraceEnabled()) {
                logger.trace("match score = " + scoredRecord.getScore());
                logger.trace("gof score = " + scoredRecord.getGoodnessOfFitScore());
                logger.trace("... matchConfidencePercentage (int) = " + matchConfidencePercentage);
            }
            // See if there is a minimum degree match percentage.
            if (!hasSpecifiedMinimumDegreeMatchPercentage
                    || (matchConfidencePercentage >= minimumDegreeMatchPercentage)) {
                results.add(scoredRecord);
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("... not within specified minimum degree match percentage = " + minimumDegreeMatchPercentage);
                }
            }
        }
        return results;
    }

    /**
     * 
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    private SubjectSearchResponse loadSubjectIdentifiers(
            SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();

        // Make sure we at least have one identifier to search from.
        Subject searchSubject = subjectSearchCriteria.getSubject();
        List<SubjectIdentifier> searchSubjectIdentifiers = searchSubject.getSubjectIdentifiers();
        if (!searchSubjectIdentifiers.isEmpty()) {
            // Pull the first identifier (only 1 used for PIX queries).
            SubjectIdentifier searchSubjectIdentifier = searchSubjectIdentifiers.get(0);

            // Get the base subjects (only base-level information) to determine type and internal ids.
            List<Subject> baseSubjects = pm.loadBaseSubjectsByIdentifier(searchSubjectIdentifier);
            if (baseSubjects.isEmpty()) {
                throw new EMPIException(
                        searchSubjectIdentifier.getCXFormatted()
                        + " is not a known identifier",
                        EMPIException.ERROR_CODE_UNKNOWN_KEY_IDENTIFIER);
            }
            // Get EnterpriseSubjectManager to load unique enterprise subjects.
            EnterpriseSubjectLoader enterpriseSubjectLoader = new EnterpriseSubjectLoader(pm, false /* loadFullSubjects */);

            // Load unique enterprise subjects.
            for (Subject baseSubject : baseSubjects) {
                EnterpriseSubjectLoaderResult loaderResult = enterpriseSubjectLoader.loadEnterpriseSubject(baseSubject);
                loaderResult.getEnterpriseSubject().setMatchConfidencePercentage(100);
            }

            // Now, get subject search response.
            subjectSearchResponse = this.getSubjectSearchResponse(subjectSearchCriteria, enterpriseSubjectLoader, searchSubjectIdentifier);
        }
        return subjectSearchResponse;
    }

      /**
     *
     * @param subjectSearchCriteria
     * @param enterpriseSubjectLoader
     * @param subjectIdentifierToRemove
     * @return
     */
    private SubjectSearchResponse getSubjectSearchResponse(
            SubjectSearchCriteria subjectSearchCriteria,
            EnterpriseSubjectLoader enterpriseSubjectLoader,
            SubjectIdentifier subjectIdentifierToRemove) {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();

        // Now, filter results and add to response.
        for (Subject enterpriseSubject : enterpriseSubjectLoader.getEnterpriseSubjects()) {

            // Filter unwanted results (if required).
            this.filterSubjectIdentifiers(subjectSearchCriteria, enterpriseSubject, subjectIdentifierToRemove);

            // FIXME: What about "other ids"?

            // If we kept at least one identifier ...
            if (enterpriseSubject.hasSubjectIdentifiers()) {
                subjectSearchResponse.getSubjects().add(enterpriseSubject);
            }
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @param subject
     * @param subjectIdentifierToRemove
     */
    private void filterSubjectIdentifiers(SubjectSearchCriteria subjectSearchCriteria, Subject subject, SubjectIdentifier subjectIdentifierToRemove) {

        // Strip out the "subjectIdentifierToRemove" (if not null).
        if (subjectIdentifierToRemove != null) {
            subject.removeSubjectIdentifier(subjectIdentifierToRemove);
        }

        // Now should filter identifiers based upon scoping organization (assigning authority).
        if (subjectSearchCriteria.hasScopingAssigningAuthorities()) {

            List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
            List<SubjectIdentifier> copyOfSubjectIdentifiers = new ArrayList<SubjectIdentifier>();
            copyOfSubjectIdentifiers.addAll(subjectIdentifiers);

            // Go through each SubjectIdentifier.
            for (SubjectIdentifier subjectIdentifier : copyOfSubjectIdentifiers) {

                // Should we keep it?
                if (!this.shouldKeepSubjectIdentifier(subjectSearchCriteria, subjectIdentifier)) {

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
    private boolean shouldKeepSubjectIdentifier(SubjectSearchCriteria subjectSearchCriteria, SubjectIdentifier subjectIdentifier) {
        // Based on calling logic, already determined that scoping assigning authorities exist.

        // Get identifier domain for the given identifier.
        SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();

        // Now see if we should return the identifier or not.
        boolean shouldKeepSubjectIdentifier = false;
        for (SubjectIdentifierDomain scopingIdentifierDomain : subjectSearchCriteria.getScopingAssigningAuthorities()) {
            if (subjectIdentifierDomain.equals(scopingIdentifierDomain)) {
                shouldKeepSubjectIdentifier = true;
                break;
            }
        }
        return shouldKeepSubjectIdentifier;
    }
}
