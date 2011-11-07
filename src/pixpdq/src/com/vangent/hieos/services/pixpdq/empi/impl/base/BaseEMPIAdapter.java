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
import com.vangent.hieos.services.pixpdq.empi.exception.EMPIException;
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
     * @param subject
     * @return
     * @throws EMPIException
     */
    public Subject addSubject(Subject subject) throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        PersistenceManager ps = new PersistenceManager();
        try {
            ps.open();

            // Create internal search criteria.
            SubjectSearchCriteria subjectSearchCriteria = new SubjectSearchCriteria();
            subjectSearchCriteria.setSubject(subject);

            // See if the subject exists (if it already has identifiers).
            if (subjectSearchCriteria.hasSubjectIdentifiers()) {
                // See if the subject already exists.
                if (ps.doesSubjectExist(subject.getSubjectIdentifiers())) {
                    throw new EMPIException("Subject already exists!");
                }
            }
            // Fall through: The subject does not already exist.

            // Find matching records.
            RecordBuilder rb = new RecordBuilder();
            Record searchRecord = rb.build(subject);
            List<ScoredRecord> recordMatches = this.getRecordMatches(ps, searchRecord);

            // Store the subject (system-level).
            subject.setType(Subject.SubjectType.SYSTEM);
            ps.insertSubject(subject);

            if (recordMatches.isEmpty()) { // No match.

                // Get ready to store cross reference.
                SubjectCrossReference subjectCrossReference = new SubjectCrossReference();
                subjectCrossReference.setMatchScore(100.0);
                subjectCrossReference.setSystemSubjectId(subject.getId());

                // Store the subject (enterprise-level).

                // Set type type to ENTERPRISE.
                subject.setType(Subject.SubjectType.ENTERPRISE);

                // Clear out the subject's identifier list (since they are stored at the system-level).
                subject.getSubjectIdentifiers().clear();

                // Stamp the subject with an enterprise id (if configured to do so).
                EUIDConfig euidConfig = empiConfig.getEuidConfig();
                if (euidConfig.isEuidAssignEnabled()) {
                    SubjectIdentifier enterpriseSubjectIdentifier = EUIDGenerator.getEUID();
                    subject.addSubjectIdentifier(enterpriseSubjectIdentifier);
                }

                // Store the enterprise-level subject
                ps.insertSubject(subject);
                subjectCrossReference.setEnterpriseSubjectId(subject.getId());

                // Store the match criteria.
                searchRecord.setId(subject.getId());
                ps.insertSubjectMatchRecord(searchRecord);

                // Store cross-reference (between enterprise and system).
                ps.insertSubjectCrossReference(subjectCrossReference);
            } else {
                // >=1 matches

                // Store cross reference to first matched record.
                SubjectCrossReference subjectCrossReference = new SubjectCrossReference();
                ScoredRecord matchedRecord = recordMatches.get(0);
                String enterpriseSubjectId = matchedRecord.getRecord().getId();
                subjectCrossReference.setEnterpriseSubjectId(enterpriseSubjectId);
                subjectCrossReference.setSystemSubjectId(subject.getId());
                subjectCrossReference.setMatchScore(this.getMatchConfidencePercentage(matchedRecord));
                ps.insertSubjectCrossReference(subjectCrossReference);

                // Merge all other matches (if any) into first matched record (surviving enterprise record).
                for (int i = 1; i < recordMatches.size(); i++) {
                    ScoredRecord scoredRecord = recordMatches.get(i);
                    ps.mergeSubjects(enterpriseSubjectId, scoredRecord.getRecord().getId());
                }
            }
            ps.commit();  // Will close connection.
        } catch (EMPIException ex) {
            ps.rollback();  // Will close connection.
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
        PersistenceManager ps = new PersistenceManager();
        try {
            ps.open();
            // Run matching algorithm.
            List<Subject> subjectMatches = this.getSubjectMatches(ps, subjectSearchCriteria);

            for (Subject subject : subjectMatches) {
                // Get cross references to the subject ...
                this.loadCrossReferencedIdentifiers(ps, subject);
            }

            subjectSearchResponse.setSubjects(subjectMatches);
        } finally {
            ps.close();  // No matter what.
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param subject
     * @throws EMPIException
     */
    private void loadCrossReferencedIdentifiers(PersistenceManager ps, Subject subject) throws EMPIException {

        // Load cross references for the subject.
        List<SubjectCrossReference> subjectCrossReferences = ps.loadSubjectCrossReferences(subject.getId());

        // Get list of subject identifiers (@ system-level).
        for (SubjectCrossReference subjectCrossReference : subjectCrossReferences) {

            // Load list of subject identifiers for the cross reference.
            List<SubjectIdentifier> subjectIdentifiers = ps.loadSubjectIdentifiers(subjectCrossReference.getSystemSubjectId());

            // Add all of this to the given subject.
            subject.getSubjectIdentifiers().addAll(subjectIdentifiers);
        }
    }

    /**
     *
     * @param ps
     * @param searchRecord
     * @return
     * @throws EMPIException
     */
    private List<ScoredRecord> getRecordMatches(PersistenceManager ps, Record searchRecord) throws EMPIException {
        // Get EMPI configuration.
        EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Get match algorithm (configurable).
        MatchAlgorithm algo = empiConfig.getMatchAlgorithm();
        algo.setPersistenceService(ps);

        // Run the algorithm to get matches.
        long startTime = System.currentTimeMillis();
        MatchResults matchResults = algo.findMatches(searchRecord);
        long endTime = System.currentTimeMillis();
        if (logger.isTraceEnabled()) {
            logger.trace("BaseEMPIAdapter.getRecordMatches.findMatches: elapedTimeMillis=" + (endTime - startTime));
        }
        return matchResults.getMatches();
    }

    /**
     *
     * @param ps
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    private List<Subject> getSubjectMatches(PersistenceManager ps, SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        // Get the search subject.
        Subject searchSubject = subjectSearchCriteria.getSubject();

        // Convert search subject into a record that can be used for matching.
        RecordBuilder rb = new RecordBuilder();
        Record searchRecord = rb.build(searchSubject);

        // Run the matching algorithm.
        List<ScoredRecord> recordMatches = this.getRecordMatches(ps, searchRecord);

        // Now load subjects from the match results.
        List<Subject> subjectMatches = new ArrayList<Subject>();
        long startTime = System.currentTimeMillis();
        for (ScoredRecord scoredRecord : recordMatches) {
            Record record = scoredRecord.getRecord();
            Subject subject = ps.loadSubject(record.getId());
            System.out.println("match score = " + scoredRecord.getScore());
            System.out.println("gof score = " + scoredRecord.getGoodnessOfFitScore());
            int matchConfidencePercentage = this.getMatchConfidencePercentage(scoredRecord);
            System.out.println("... matchConfidencePercentage (int) = " + matchConfidencePercentage);
            subject.setMatchConfidencePercentage(matchConfidencePercentage);
            subjectMatches.add(subject);
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
        PersistenceManager ps = new PersistenceManager();
        // Prepare default response.
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        try {
            ps.open();
            // Make sure we at least have one identifier to search from.
            Subject searchSubject = subjectSearchCriteria.getSubject();
            List<SubjectIdentifier> searchSubjectIdentifiers = searchSubject.getSubjectIdentifiers();
            if (searchSubjectIdentifiers.size() > 0) {
                // Pull the first identifier.
                SubjectIdentifier subjectIdentifier = searchSubjectIdentifiers.get(0);

                // Get the subject (only base-level information) to determine type and internal id.
                Subject subject = ps.loadSubjectBaseByIdentifier(subjectIdentifier);
                Subject enterpriseSubject = null;
                if (subject != null) {  // Found a match.

                    // We now may have either an enterprise-level or system-level subject.
                    String enterpriseSubjectId = null;
                    if (subject.getType().equals(Subject.SubjectType.ENTERPRISE)) {
                        enterpriseSubjectId = subject.getId();
                        enterpriseSubject = subject;
                    } else {
                        String systemSubjectId = subject.getId();

                        //  Get enterpiseSubjectId for the system-level subject.
                        enterpriseSubjectId = ps.getEnterpriseSubjectId(systemSubjectId);
                        
                        // Create enterprise subject.
                        enterpriseSubject = new Subject();
                        enterpriseSubject.setId(enterpriseSubjectId);
                        enterpriseSubject.setType(Subject.SubjectType.ENTERPRISE);
                    }

                    // Load enterprise subject identifiers.
                    List<SubjectIdentifier> enterpriseSubjectIdentifiers = ps.loadSubjectIdentifiers(enterpriseSubjectId);
                    enterpriseSubject.getSubjectIdentifiers().addAll(enterpriseSubjectIdentifiers);

                    // Load cross references for the enterprise subject.
                    this.loadCrossReferencedIdentifiers(ps, enterpriseSubject);

                    // Put enterprise subject in the response.
                    subjectSearchResponse.getSubjects().add(enterpriseSubject);
                }
            }
        } catch (EMPIException ex) {
            throw ex; // Rethrow.
        } finally {
            ps.close();
        }
        return subjectSearchResponse;
    }
}
