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
import java.math.BigDecimal;
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
    @Override
    public void startup(ClassLoader classLoader) {
        // Do nothing here.
    }

    /**
     * 
     * @param subject
     * @return
     * @throws EMPIException
     */
    @Override
    public Subject addSubject(Subject subject) throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        PersistenceManager pm = new PersistenceManager();
        try {
            pm.open();  // Open transaction.

            // FIXME: This is not totally correct, how about "other ids"?
            // See if the subject exists (if it already has identifiers).
            if (subject.hasSubjectIdentifiers()) {

                // See if the subject already exists.
                if (pm.doesSubjectExist(subject.getSubjectIdentifiers())) {
                    throw new EMPIException("Subject already exists!");
                }
            }
            // Fall through: The subject does not already exist.

            // Store the subject @ system-level - will stamp with subjectId.
            subject.setType(Subject.SubjectType.SYSTEM);
            pm.insertSubject(subject);

            // Get prepared for next steps ..
            String systemSubjectId = subject.getId();
            String enterpriseSubjectId = null;
            int matchScore = 100;    // Default.

            // Find matching records.
            RecordBuilder rb = new RecordBuilder();
            Record searchRecord = rb.build(subject);
            List<ScoredRecord> recordMatches = this.getRecordMatches(pm, searchRecord);

            if (recordMatches.isEmpty()) { // No match.

                // Store the subject @ enterprise-level.

                // Set type type to ENTERPRISE.
                subject.setType(Subject.SubjectType.ENTERPRISE);

                // Clear out the subject's identifier lists (since they are already stored at the system-level).
                subject.clearIdentifiers();

                // Stamp the subject with an enterprise id (if configured to do so).
                EUIDConfig euidConfig = empiConfig.getEuidConfig();
                if (euidConfig.isEuidAssignEnabled()) {
                    SubjectIdentifier enterpriseSubjectIdentifier = EUIDGenerator.getEUID();
                    subject.addSubjectIdentifier(enterpriseSubjectIdentifier);
                }

                // Store the enterprise-level subject.
                pm.insertSubject(subject);
                enterpriseSubjectId = subject.getId();

                // Store the match criteria.
                searchRecord.setId(enterpriseSubjectId);
                pm.insertSubjectMatchRecord(searchRecord);
            } else {
                // >=1 matches

                // Cross reference will be to first matched record.  All other records will be merged later below.
                ScoredRecord matchedRecord = recordMatches.get(0);
                enterpriseSubjectId = matchedRecord.getRecord().getId();
                matchScore = this.getMatchScore(matchedRecord);
            }

            // Create and store cross-reference.
            SubjectCrossReference subjectCrossReference = new SubjectCrossReference();
            subjectCrossReference.setMatchScore(matchScore);
            subjectCrossReference.setSystemSubjectId(systemSubjectId);
            subjectCrossReference.setEnterpriseSubjectId(enterpriseSubjectId);
            pm.insertSubjectCrossReference(subjectCrossReference);

            // Merge all other matches (if any) into first matched record (surviving enterprise record).
            for (int i = 1; i < recordMatches.size(); i++) {
                ScoredRecord scoredRecord = recordMatches.get(i);
                pm.mergeSubjects(enterpriseSubjectId, scoredRecord.getRecord().getId());
            }
            pm.commit();
        } catch (EMPIException ex) {
            pm.rollback();
            throw ex; // Rethrow.
        } catch (Exception ex) {
            pm.rollback();
            throw new EMPIException(ex);
        } finally {
            pm.close();  // To be sure.
        }
        return subject;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    @Override
    public SubjectSearchResponse findSubjects(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        PersistenceManager pm = new PersistenceManager();
        try {
            pm.open();

            // First, make sure that we are configured to support supplied identifier domains.
            this.validateSubjectIdentifierDomains(pm, subjectSearchCriteria);

            // FIXME: This is not entirely accurate, how about "other ids"?
            // Determine which path to take.
            if (subjectSearchCriteria.hasSubjectIdentifiers()) {
                logger.debug("Searching based on identifiers ...");
                subjectSearchResponse = this.findSubjectByIdentifier(pm, subjectSearchCriteria, false /* onlyLoadIdentifiers */);
            } else if (subjectSearchCriteria.hasSubjectDemographics()) {
                logger.debug("Searching based on demographics ...");
                subjectSearchResponse = this.findSubjectMatches(pm, subjectSearchCriteria);
            } else {
                // Do nothing ...
                logger.debug("Not searching at all!!");
            }
        } finally {
            pm.close();  // No matter what.
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    @Override
    public SubjectSearchResponse findSubjectByIdentifier(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        PersistenceManager pm = new PersistenceManager();
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        try {
            pm.open();

            // First, make sure that we are configured to support supplied identifier domains.
            this.validateSubjectIdentifierDomains(pm, subjectSearchCriteria);

            // Now, conduct lookup.
            subjectSearchResponse = this.findSubjectByIdentifier(pm, subjectSearchCriteria, true /* onlyLoadIdentifiers */);
        } finally {
            pm.close();
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param pm
     * @param subjectSearchCriteria
     * @param onlyLoadIdentifiers
     * @return
     * @throws EMPIException
     */
    private SubjectSearchResponse findSubjectByIdentifier(
            PersistenceManager pm, SubjectSearchCriteria subjectSearchCriteria, boolean onlyLoadIdentifiers) throws EMPIException {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();

        // Make sure we at least have one identifier to search from.
        Subject searchSubject = subjectSearchCriteria.getSubject();
        List<SubjectIdentifier> searchSubjectIdentifiers = searchSubject.getSubjectIdentifiers();
        if (!searchSubjectIdentifiers.isEmpty()) {
            // Pull the first identifier.
            // FIXME: Need to deal with multiple search identifiers (for PDQ .. not sure about PIX) ....
            SubjectIdentifier searchSubjectIdentifier = searchSubjectIdentifiers.get(0);

            // Get the baseSubject (only base-level information) to determine type and internal id.
            Subject baseSubject = pm.loadBaseSubjectByIdentifier(searchSubjectIdentifier);
            Subject enterpriseSubject = null;
            if (baseSubject != null) {  // Found a match.

                // We now may have either an enterprise-level or system-level subject.
                String enterpriseSubjectId = null;
                if (baseSubject.getType().equals(Subject.SubjectType.ENTERPRISE)) {
                    enterpriseSubjectId = baseSubject.getId();
                } else {
                    // Get enterpiseSubjectId for the system-level subject.
                    enterpriseSubjectId = pm.getEnterpriseSubjectId(baseSubject.getId());
                }

                if (!onlyLoadIdentifiers) {
                    // Load the complete subject.
                    enterpriseSubject = pm.loadEnterpriseSubject(enterpriseSubjectId);

                    // Do not return the search identifier in this case.
                    searchSubjectIdentifier = null;
                } else {
                    // Load enterprise subject (id's only).
                    enterpriseSubject = pm.loadEnterpriseSubjectIdentifiersOnly(enterpriseSubjectId);
                }

                // Now, strip out identifiers (if required).
                this.filterSubjectIdentifiers(subjectSearchCriteria, enterpriseSubject, searchSubjectIdentifier);

                // FIXME: What about "other ids"?
                if (!onlyLoadIdentifiers
                        || !enterpriseSubject.getSubjectIdentifiers().isEmpty()) {

                    // Put enterprise subject in the response.
                    enterpriseSubject.setMatchConfidencePercentage(100);
                    subjectSearchResponse.getSubjects().add(enterpriseSubject);
                }
            }
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param pm
     * @param searchRecord
     * @return
     * @throws EMPIException
     */
    private List<ScoredRecord> getRecordMatches(PersistenceManager pm, Record searchRecord) throws EMPIException {
        // Get EMPI configuration.
        EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Get match algorithm (configurable).
        MatchAlgorithm matchAlgorithm = empiConfig.getMatchAlgorithm();
        matchAlgorithm.setPersistenceService(pm);

        // Run the algorithm to get matches.
        long startTime = System.currentTimeMillis();
        MatchResults matchResults = matchAlgorithm.findMatches(searchRecord);
        long endTime = System.currentTimeMillis();
        if (logger.isTraceEnabled()) {
            logger.trace("BaseEMPIAdapter.getRecordMatches.findMatches: elapedTimeMillis=" + (endTime - startTime));
        }
        // Only return matches.
        return matchResults.getMatches();
    }

    /**
     *
     * @param pm
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    private SubjectSearchResponse findSubjectMatches(PersistenceManager pm, SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        Subject searchSubject = subjectSearchCriteria.getSubject();

        // Convert search subject into a record that can be used for matching.
        RecordBuilder rb = new RecordBuilder();
        Record searchRecord = rb.build(searchSubject);

        // Run the matching algorithm.
        List<ScoredRecord> recordMatches = this.getRecordMatches(pm, searchRecord);

        // Now load subjects from the match results.
        List<Subject> subjectMatches = new ArrayList<Subject>();
        long startTime = System.currentTimeMillis();
        for (ScoredRecord scoredRecord : recordMatches) {
            Record record = scoredRecord.getRecord();
            Subject enterpriseSubject = pm.loadEnterpriseSubject(record.getId());
            int matchConfidencePercentage = this.getMatchScore(scoredRecord);
            enterpriseSubject.setMatchConfidencePercentage(matchConfidencePercentage);

            if (logger.isDebugEnabled()) {
                logger.debug("match score = " + scoredRecord.getScore());
                logger.debug("gof score = " + scoredRecord.getGoodnessOfFitScore());
                logger.debug("... matchConfidencePercentage (int) = " + matchConfidencePercentage);
            }

            // Filter unwanted results (if required).
            this.filterSubjectIdentifiers(subjectSearchCriteria, enterpriseSubject, null);

            // FIXME: What about "other ids"?
            // If we kept at least one identifier ...
            if (enterpriseSubject.hasSubjectIdentifiers()) {
                subjectMatches.add(enterpriseSubject);
            }

        }
        long endTime = System.currentTimeMillis();
        if (logger.isTraceEnabled()) {
            logger.trace("BaseEMPIAdapter.getSubjectMatches.loadSubjects: elapedTimeMillis=" + (endTime - startTime));
        }
        subjectSearchResponse.getSubjects().addAll(subjectMatches);
        return subjectSearchResponse;
    }

    /**
     * 
     * @param scoredRecord
     * @return
     */
    private int getMatchScore(ScoredRecord scoredRecord) {
        // Round-up match score.
        BigDecimal bd = new BigDecimal(scoredRecord.getScore() * 100.0);
        bd = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
        return bd.intValue();
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

    /**
     * 
     * @param pm
     * @param subjectSearchCriteria
     * @throws EMPIException
     */
    private void validateSubjectIdentifierDomains(PersistenceManager pm, SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        Subject searchSubject = subjectSearchCriteria.getSubject();

        // First validate identifier domains assocated with the search subject's identifiers.
        List<SubjectIdentifier> subjectIdentifiers = searchSubject.getSubjectIdentifiers();
        for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
            boolean subjectIdentifierDomainExists = pm.doesSubjectIdentifierDomainExist(subjectIdentifier);
            if (!subjectIdentifierDomainExists) {
                SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
                throw new EMPIException(
                        subjectIdentifierDomain.getUniversalId()
                        + " is not a known identifier domain", 
                        EMPIException.ERROR_CODE_UNKOWN_KEY_IDENTIFIER);
            }
        }

        // Now validate identifiers in any scoping organizations.
        for (SubjectIdentifierDomain scopingIdentifierDomain : subjectSearchCriteria.getScopingAssigningAuthorities()) {
            boolean subjectIdentifierDomainExists = pm.doesSubjectIdentifierDomainExist(scopingIdentifierDomain);
            if (!subjectIdentifierDomainExists) {
                throw new EMPIException(
                        scopingIdentifierDomain.getUniversalId()
                        + " is not a known identifier domain",
                        EMPIException.ERROR_CODE_UNKOWN_KEY_IDENTIFIER);
            }
        }
    }
}
