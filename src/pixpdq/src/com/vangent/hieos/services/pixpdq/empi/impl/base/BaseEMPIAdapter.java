/*
 * This code is baseSubject to the HIEOS License, Version 1.0
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
package com.vangent.hieos.services.pixpdq.empi.impl.base;

import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.config.EUIDConfig;
import com.vangent.hieos.empi.euid.EUIDGenerator;
import com.vangent.hieos.empi.match.MatchAlgorithm;
import com.vangent.hieos.empi.match.MatchResults;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.match.RecordBuilder;
import com.vangent.hieos.empi.match.ScoredRecord;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.model.SubjectCrossReference;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.services.pixpdq.empi.api.EMPIAdapter;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class BaseEMPIAdapter implements EMPIAdapter {

    private static final Logger logger = Logger.getLogger(BaseEMPIAdapter.class);

    /**
     *
     * @param classLoader
     */
    public void startup(ClassLoader classLoader) {
        // Do nothing here.
    }

    /**
     *
     * @param baseSubject
     * @return
     * @throws EMPIException
     */
    public Subject addSubject(Subject subject) throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        PersistenceManager persistenceManager = new PersistenceManager();
        try {
            persistenceManager.open();  // Open transaction.

            // See if the baseSubject exists (if it already has identifiers).
            if (subject.hasSubjectIdentifiers()) {

                // See if the baseSubject already exists.
                if (persistenceManager.doesSubjectExist(subject.getSubjectIdentifiers())) {
                    throw new EMPIException("Subject already exists!");
                }
            }
            // Fall through: The baseSubject does not already exist.

            // Find matching records.
            RecordBuilder rb = new RecordBuilder();
            Record searchRecord = rb.build(subject);
            List<ScoredRecord> recordMatches = this.getRecordMatches(persistenceManager, searchRecord);

            // Store the baseSubject (system-level) - will stamp with subjectId.
            subject.setType(Subject.SubjectType.SYSTEM);
            persistenceManager.insertSubject(subject);

            if (recordMatches.isEmpty()) { // No match.

                // Get ready to store cross reference.
                SubjectCrossReference subjectCrossReference = new SubjectCrossReference();
                subjectCrossReference.setMatchScore(100.0);
                subjectCrossReference.setSystemSubjectId(subject.getId());

                // Store the baseSubject (enterprise-level).

                // Set type type to ENTERPRISE.
                subject.setType(Subject.SubjectType.ENTERPRISE);

                // Clear out the baseSubject's identifier list (since they are stored at the system-level).
                subject.getSubjectIdentifiers().clear();

                // Stamp the baseSubject with an enterprise id (if configured to do so).
                EUIDConfig euidConfig = empiConfig.getEuidConfig();
                if (euidConfig.isEuidAssignEnabled()) {
                    SubjectIdentifier enterpriseSubjectIdentifier = EUIDGenerator.getEUID();
                    subject.addSubjectIdentifier(enterpriseSubjectIdentifier);
                }

                // Store the enterprise-level baseSubject and update cross-reference.
                persistenceManager.insertSubject(subject);
                subjectCrossReference.setEnterpriseSubjectId(subject.getId());

                // Store the match criteria.
                searchRecord.setId(subject.getId());
                persistenceManager.insertSubjectMatchRecord(searchRecord);

                // Store cross-reference (between enterprise and system).
                persistenceManager.insertSubjectCrossReference(subjectCrossReference);
            } else {
                // >=1 matches

                // Store cross reference to first matched record.
                SubjectCrossReference subjectCrossReference = new SubjectCrossReference();
                ScoredRecord matchedRecord = recordMatches.get(0);
                String enterpriseSubjectId = matchedRecord.getRecord().getId();
                subjectCrossReference.setEnterpriseSubjectId(enterpriseSubjectId);
                subjectCrossReference.setSystemSubjectId(subject.getId());
                subjectCrossReference.setMatchScore(this.getMatchConfidencePercentage(matchedRecord));
                persistenceManager.insertSubjectCrossReference(subjectCrossReference);

                // Merge all other matches (if any) into first matched record (surviving enterprise record).
                for (int i = 1; i < recordMatches.size(); i++) {
                    ScoredRecord scoredRecord = recordMatches.get(i);
                    persistenceManager.mergeSubjects(enterpriseSubjectId, scoredRecord.getRecord().getId());
                }
            }
            persistenceManager.commit();  // Will close connection.
        } catch (EMPIException ex) {
            persistenceManager.rollback();  // Will close connection.
            throw ex; // Rethrow.
        }
        return subject;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    public SubjectSearchResponse findSubjects(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        // FIXME: LOOK AT OLD CODE AND REFINE
        // Create response instance.
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        PersistenceManager persistenceManager = new PersistenceManager();
        try {
            persistenceManager.open();

            // Run matching algorithm.
            List<Subject> subjectMatches = this.getSubjectMatches(persistenceManager, subjectSearchCriteria);

            for (Subject subject : subjectMatches) {
                // Get cross references to the baseSubject ...
                this.loadCrossReferencedIdentifiers(persistenceManager, subject);
            }

            subjectSearchResponse.setSubjects(subjectMatches);
        } finally {
            persistenceManager.close();  // No matter what.
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param baseSubject
     * @throws EMPIException
     */
    private void loadCrossReferencedIdentifiers(PersistenceManager persistenceManager, Subject subject) throws EMPIException {

        // Load cross references for the baseSubject.
        List<SubjectCrossReference> subjectCrossReferences = persistenceManager.loadSubjectCrossReferences(subject.getId());

        // Get list of baseSubject identifiers (@ system-level).
        for (SubjectCrossReference subjectCrossReference : subjectCrossReferences) {

            // Load list of baseSubject identifiers for the cross reference.
            List<SubjectIdentifier> subjectIdentifiers = persistenceManager.loadSubjectIdentifiers(subjectCrossReference.getSystemSubjectId());

            // Add all of this to the given baseSubject.
            subject.getSubjectIdentifiers().addAll(subjectIdentifiers);
        }
    }

    /**
     *
     * @param persistenceManager
     * @param searchRecord
     * @return
     * @throws EMPIException
     */
    private List<ScoredRecord> getRecordMatches(PersistenceManager persistenceManager, Record searchRecord) throws EMPIException {
        // Get EMPI configuration.
        EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Get match algorithm (configurable).
        MatchAlgorithm matchAlgorithm = empiConfig.getMatchAlgorithm();
        matchAlgorithm.setPersistenceService(persistenceManager);

        // Run the algorithm to get matches.
        long startTime = System.currentTimeMillis();
        MatchResults matchResults = matchAlgorithm.findMatches(searchRecord);
        long endTime = System.currentTimeMillis();
        if (logger.isTraceEnabled()) {
            logger.trace("BaseEMPIAdapter.getRecordMatches.findMatches: elapedTimeMillis=" + (endTime - startTime));
        }
        return matchResults.getMatches();
    }

    /**
     *
     * @param persistenceManager
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    private List<Subject> getSubjectMatches(PersistenceManager persistenceManager, SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        // Get the search baseSubject.
        Subject searchSubject = subjectSearchCriteria.getSubject();

        // Convert search baseSubject into a record that can be used for matching.
        RecordBuilder rb = new RecordBuilder();
        Record searchRecord = rb.build(searchSubject);

        // Run the matching algorithm.
        List<ScoredRecord> recordMatches = this.getRecordMatches(persistenceManager, searchRecord);

        // Now load subjects from the match results.
        List<Subject> subjectMatches = new ArrayList<Subject>();
        long startTime = System.currentTimeMillis();
        for (ScoredRecord scoredRecord : recordMatches) {
            Record record = scoredRecord.getRecord();
            Subject subject = persistenceManager.loadSubject(record.getId());
            int matchConfidencePercentage = this.getMatchConfidencePercentage(scoredRecord);
            subject.setMatchConfidencePercentage(matchConfidencePercentage);
            subjectMatches.add(subject);
            if (logger.isDebugEnabled()) {
                logger.debug("match score = " + scoredRecord.getScore());
                logger.debug("gof score = " + scoredRecord.getGoodnessOfFitScore());
                logger.debug("... matchConfidencePercentage (int) = " + matchConfidencePercentage);
            }
        }
        long endTime = System.currentTimeMillis();
        if (logger.isTraceEnabled()) {
            logger.trace("BaseEMPIAdapter.findSubjects.loadSubjects: elapedTimeMillis=" + (endTime - startTime));
        }
        return subjectMatches;
    }

    /**
     * 
     * @param scoredRecord
     * @return
     */
    private int getMatchConfidencePercentage(ScoredRecord scoredRecord) {
        // FIXME: Roundup.
        return (int) (scoredRecord.getScore() * 100.0);
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    public SubjectSearchResponse findSubjectByIdentifier(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        PersistenceManager persistenceManager = new PersistenceManager();
        // Prepare default response.
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        try {
            persistenceManager.open();
            // Make sure we at least have one identifier to search from.
            Subject searchSubject = subjectSearchCriteria.getSubject();
            List<SubjectIdentifier> searchSubjectIdentifiers = searchSubject.getSubjectIdentifiers();
            if (searchSubjectIdentifiers.size() > 0) {
                // Pull the first identifier.
                SubjectIdentifier searchSubjectIdentifier = searchSubjectIdentifiers.get(0);

                // Get the baseSubject (only base-level information) to determine type and internal id.
                Subject baseSubject = persistenceManager.loadBaseSubjectByIdentifier(searchSubjectIdentifier);
                Subject enterpriseSubject = null;
                if (baseSubject != null) {  // Found a match.

                    // We now may have either an enterprise-level or system-level baseSubject.
                    String enterpriseSubjectId = null;
                    if (baseSubject.getType().equals(Subject.SubjectType.ENTERPRISE)) {
                        enterpriseSubjectId = baseSubject.getId();
                        enterpriseSubject = baseSubject;
                    } else {
                        String systemSubjectId = baseSubject.getId();

                        //  Get enterpiseSubjectId for the system-level baseSubject.
                        enterpriseSubjectId = persistenceManager.getEnterpriseSubjectId(systemSubjectId);

                        // Create enterprise baseSubject.
                        enterpriseSubject = new Subject();
                        enterpriseSubject.setId(enterpriseSubjectId);
                        enterpriseSubject.setType(Subject.SubjectType.ENTERPRISE);
                    }

                    // Load enterprise baseSubject identifiers.
                    List<SubjectIdentifier> enterpriseSubjectIdentifiers = persistenceManager.loadSubjectIdentifiers(enterpriseSubjectId);
                    enterpriseSubject.getSubjectIdentifiers().addAll(enterpriseSubjectIdentifiers);

                    // Load cross references for the enterprise baseSubject.
                    this.loadCrossReferencedIdentifiers(persistenceManager, enterpriseSubject);

                    // Now, filter identifiers based upon query.
                    this.filterSubjectIdentifiers(subjectSearchCriteria, enterpriseSubject, searchSubjectIdentifier);

                    // Only return the enterprise subject if identifiers remain (after filtering).
                    if (enterpriseSubject.getSubjectIdentifiers().size() > 0) {

                        // Put enterprise baseSubject in the response.
                        subjectSearchResponse.getSubjects().add(enterpriseSubject);
                    }
                }
            }
        } catch (EMPIException ex) {
            throw ex; // Rethrow.
        } finally {
            persistenceManager.close();
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
