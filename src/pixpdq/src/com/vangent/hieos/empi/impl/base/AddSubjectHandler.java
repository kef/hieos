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

import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.config.EUIDConfig;
import com.vangent.hieos.empi.euid.EUIDGenerator;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.match.MatchAlgorithm;
import com.vangent.hieos.empi.match.MatchResults;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.match.RecordBuilder;
import com.vangent.hieos.empi.match.ScoredRecord;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.empi.api.EMPINotification;
import com.vangent.hieos.empi.validator.AddSubjectValidator;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class AddSubjectHandler extends BaseHandler {

    private static final Logger logger = Logger.getLogger(AddSubjectHandler.class);

    /**
     * 
     * @param configActor
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    public AddSubjectHandler(XConfigActor configActor, PersistenceManager persistenceManager, DeviceInfo senderDeviceInfo) {
        super(configActor, persistenceManager, senderDeviceInfo);
    }

    /**
     *
     * @param newSubject
     * @return
     * @throws EMPIException
     */
    public EMPINotification addSubject(Subject newSubject) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();

        // First, run validations on input.
        AddSubjectValidator validator = new AddSubjectValidator(pm, this.getSenderDeviceInfo());
        validator.validate(newSubject);

        // Store the subject @ system-level - will stamp with subjectId.
        newSubject.setType(Subject.SubjectType.SYSTEM);
        pm.insertSubject(newSubject);

        // Get prepared for next steps ..
        String systemSubjectId = newSubject.getInternalId();
        String enterpriseSubjectId = null;
        int matchScore = 100;    // Default.

        // Find matching records.
        RecordBuilder rb = new RecordBuilder();
        Record searchRecord = rb.build(newSubject);
        MatchAlgorithm matchAlgo = MatchAlgorithm.getMatchAlgorithm(pm);
        MatchResults matchResults = matchAlgo.findMatches(searchRecord, MatchAlgorithm.MatchType.SUBJECT_FEED);
        if (!matchResults.getPossibleMatches().isEmpty()) {
            // FIXME!!!!
            System.out.println("+++++ DO SOMETHING HERE ... store possible matches");
        }
        List<ScoredRecord> recordMatches = matchResults.getMatches();

        long start = System.currentTimeMillis();

        if (recordMatches.isEmpty()) { // No match.
            enterpriseSubjectId = this.insertEnterpriseSubject(newSubject);

        } else if (this.isAnyMatchedRecordInSameIdentifierDomain(newSubject, recordMatches)) {
            logger.trace("+++++ Not linking subject with same identifier domain +++++");
            // Do not place record along side any record within the same identifier domain.
            recordMatches.clear();  // Treat as though a match did not occur.

            // Insert a new enterprise record.
            enterpriseSubjectId = this.insertEnterpriseSubject(newSubject);
        } else {
            // >=1 matches

            // Cross reference will be to first matched record.  All other records will be merged later below.
            ScoredRecord matchedRecord = recordMatches.get(0);
            String matchedSystemSubjectId = matchedRecord.getRecord().getId();
            enterpriseSubjectId = pm.getEnterpriseSubjectId(matchedSystemSubjectId);
            matchScore = matchedRecord.getMatchScorePercentage();

            // Update enterprise subject with latest demographics.
            logger.trace("+++ Updating demographics on enterprise subject +++");
            pm.updateEnterpriseSubject(enterpriseSubjectId, newSubject);
        }

        // Insert system-level subject match fields (for subsequent find operations).
        searchRecord.setId(systemSubjectId);
        pm.insertSubjectMatchFields(searchRecord);

        // Create and store cross-reference.
        pm.insertSubjectCrossReference(systemSubjectId, enterpriseSubjectId, matchScore);

        // Merge all other matches (if any) into first matched record (surviving enterprise record).
        this.mergeRecordMatches(recordMatches, enterpriseSubjectId);

        if (logger.isDebugEnabled()) {
            logger.debug("EMPI persistence TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
        }
        EMPINotification notification = new EMPINotification();
        this.addSubjectToNotification(notification, enterpriseSubjectId);
        return notification;
    }

    /**
     *
     * @param recordMatches
     * @param enterpriseSubjectId
     * @throws EMPIException
     */
    private void mergeRecordMatches(List<ScoredRecord> recordMatches, String enterpriseSubjectId) throws EMPIException {
        // FIXME: Make this configurable.
        PersistenceManager pm = this.getPersistenceManager();

        // Merge all other matches (if any) into first matched record (surviving enterprise record).
        Set<String> subsumedEnterpriseSubjectIds = new HashSet<String>();
        for (int i = 1; i < recordMatches.size(); i++) {
            ScoredRecord matchedRecord = recordMatches.get(i);
            String matchedSystemSubjectId = matchedRecord.getRecord().getId();
            String subsumedEnterpriseSubjectId = pm.getEnterpriseSubjectId(matchedSystemSubjectId);
            // Make sure that the subject has not already been merged.
            if (!subsumedEnterpriseSubjectIds.contains(subsumedEnterpriseSubjectId)) {
                subsumedEnterpriseSubjectIds.add(subsumedEnterpriseSubjectId);
                pm.mergeEnterpriseSubjects(enterpriseSubjectId, subsumedEnterpriseSubjectId);
            }
        }
    }

    /**
     *
     * @param newSubject
     * @param recordMatches
     * @return
     * @throws EMPIException
     */
    private boolean isAnyMatchedRecordInSameIdentifierDomain(Subject newSubject, List<ScoredRecord> recordMatches) throws EMPIException {

        // First check scored record against record matches to see if it is safe to link
        // the records.
        for (ScoredRecord recordMatch : recordMatches) {
            if (this.isMatchedRecordInSameIdentifierDomain(newSubject, recordMatch)) {
                return true;  // Early exit.
            }
        }

        /* RETHINK!!!
        // FIXME: NEED TO OPTIMIZE ... doing many DB reads where should only read once per
        // record.

        // Now, go through each pair in the merge set.
        PersistenceManager pm = this.getPersistenceManager();
        for (ScoredRecord matchedRecord : recordMatches) {
        String matchedSystemSubjectId = matchedRecord.getRecord().getId();
        // Load identifiers for matched system subject.
        List<SubjectIdentifier> matchedSubjectIdentifiers = pm.loadSubjectIdentifiers(matchedSystemSubjectId);

        for (ScoredRecord compareMatchedRecord : recordMatches) {
        String compareMatchedSystemSubjectId = compareMatchedRecord.getRecord().getId();
        if (!matchedSystemSubjectId.equals(compareMatchedSystemSubjectId)) {
        // Load identifiers for matched system subject (to compare).
        List<SubjectIdentifier> compareMatchedSubjectIdentifiers = pm.loadSubjectIdentifiers(compareMatchedSystemSubjectId);
        boolean foundMatch = this.isMatchedRecordInSameIdentifierDomain(matchedSubjectIdentifiers, compareMatchedSubjectIdentifiers);
        if (foundMatch) {
        // Found a match.
        System.out.println("+++++ Not linking subject with same identifier domain (multi-merge) +++++");
        return true;  // Early exit!
        }
        }
        }
        }
         */
        return false;
    }

    /**
     * 
     * @param newSubject
     * @param matchedRecord
     * @return
     * @throws EMPIException
     */
    private boolean isMatchedRecordInSameIdentifierDomain(Subject newSubject, ScoredRecord matchedRecord) throws EMPIException {
        String matchedSystemSubjectId = matchedRecord.getRecord().getId();
        // Load identifiers for matched system subject.
        PersistenceManager pm = this.getPersistenceManager();
        List<SubjectIdentifier> matchedSubjectIdentifiers = pm.loadSubjectIdentifiers(matchedSystemSubjectId);

        // Get list of the subject's identifiers.
        List<SubjectIdentifier> newSubjectIdentifiers = newSubject.getSubjectIdentifiers();

        return this.isMatchedRecordInSameIdentifierDomain(matchedSubjectIdentifiers, newSubjectIdentifiers);
    }

    /**
     * 
     * @param matchedSubjectIdentifiers
     * @param newSubjectIdentifiers
     * @return
     * @throws EMPIException
     */
    private boolean isMatchedRecordInSameIdentifierDomain(List<SubjectIdentifier> matchedSubjectIdentifiers, List<SubjectIdentifier> newSubjectIdentifiers) throws EMPIException {

        // NOTE: Small list, so brute force is OK.

        // See if there is a match between the "matched subject" and the subject.
        for (SubjectIdentifier subjectIdentifier : newSubjectIdentifiers) {
            SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
            for (SubjectIdentifier matchedSubjectIdentifier : matchedSubjectIdentifiers) {
                SubjectIdentifierDomain matchedSubjectIdentifierDomain = matchedSubjectIdentifier.getIdentifierDomain();
                if (subjectIdentifierDomain.equals(matchedSubjectIdentifierDomain)) {
                    // No need to look further.
                    if (logger.isTraceEnabled()) {
                        logger.trace("+++++ Not linking +++++");
                        logger.trace(" ... subject identifier = " + subjectIdentifier.getCXFormatted());
                        logger.trace(" ... matched subject identifier (same assigning authority) = " + matchedSubjectIdentifier.getCXFormatted());
                    }
                    System.out.println("+++++ Not linking +++++");
                    System.out.println(" ... subject identifier = " + subjectIdentifier.getCXFormatted());
                    System.out.println(" ... matched subject identifier (same assigning authority) = " + matchedSubjectIdentifier.getCXFormatted());

                    return true;  // Early exit!
                }
            }
        }
        return false;
    }

    /**
     *
     * @param newEnterpriseSubject
     * @param updateNotificationContent
     * @return
     * @throws EMPIException
     */
    private String insertEnterpriseSubject(Subject newEnterpriseSubject) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Build (from received subject) and store enterprise-level subject.
        newEnterpriseSubject.setType(Subject.SubjectType.ENTERPRISE);

        // Clear out the subject's identifier lists (since they are already stored at the system-level).
        newEnterpriseSubject.clearIdentifiers();

        // Stamp the subject with an enterprise id (if configured to do so).
        EUIDConfig euidConfig = empiConfig.getEuidConfig();
        if (euidConfig.isEuidAssignEnabled()) {
            SubjectIdentifier enterpriseSubjectIdentifier = EUIDGenerator.getEUID();
            newEnterpriseSubject.addSubjectIdentifier(enterpriseSubjectIdentifier);
        }

        // Store the enterprise-level subject.
        pm.insertSubject(newEnterpriseSubject);
        return newEnterpriseSubject.getInternalId();
    }
}
