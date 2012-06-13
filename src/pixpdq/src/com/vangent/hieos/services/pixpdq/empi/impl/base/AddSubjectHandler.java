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
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.match.MatchAlgorithm;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.match.RecordBuilder;
import com.vangent.hieos.empi.match.ScoredRecord;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.services.pixpdq.empi.api.EMPINotification;
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
     * @param subject
     * @return
     * @throws EMPIException
     */
    public EMPINotification addSubject(Subject subject) throws EMPIException {
        this.validateIdentitySource(subject);
        PersistenceManager pm = this.getPersistenceManager();

        // FIXME: This is not totally correct, how about "other ids"?
        // See if the subject exists (if it already has identifiers).
        if (subject.hasSubjectIdentifiers()) {

            // See if the subject already exists.
            if (pm.doesSubjectExist(subject.getSubjectIdentifiers())) {
                throw new EMPIException("Subject already exists!");
            }
        }
        // Fall through: The subject does not already exist.

        this.validateSubjectCodes(subject);

        // Store the subject @ system-level - will stamp with subjectId.
        subject.setType(Subject.SubjectType.SYSTEM);
        pm.insertSubject(subject);

        // Get prepared for next steps ..
        String systemSubjectId = subject.getInternalId();
        String enterpriseSubjectId = null;
        int matchScore = 100;    // Default.

        // Find matching records.
        RecordBuilder rb = new RecordBuilder();
        Record searchRecord = rb.build(subject);
        FindSubjectsHandler findSubjectsHandler = new FindSubjectsHandler(this.getConfigActor(), this.getPersistenceManager(), this.getSenderDeviceInfo());
        List<ScoredRecord> recordMatches = findSubjectsHandler.getRecordMatches(searchRecord, MatchAlgorithm.MatchType.SUBJECT_FEED);

        if (recordMatches.isEmpty()) { // No match.
            enterpriseSubjectId = this.insertEnterpriseSubject(subject);
        } else if (this.isAnyMatchedRecordInSameIdentifierDomain(subject, recordMatches)) {
            System.out.println("+++++ Not linking subject with same identifier domain +++++");
            // Do not place record along side any record within the same identifier domain.
            recordMatches.clear();  // Treat as though a match did not occur.

            // Insert a new enterprise record.
            enterpriseSubjectId = this.insertEnterpriseSubject(subject);
        } else {
            // >=1 matches

            // Cross reference will be to first matched record.  All other records will be merged later below.
            ScoredRecord matchedRecord = recordMatches.get(0);
            String matchedSystemSubjectId = matchedRecord.getRecord().getId();
            enterpriseSubjectId = pm.getEnterpriseSubjectId(matchedSystemSubjectId);
            matchScore = matchedRecord.getMatchScorePercentage();

            // Update enterprise subject with latest demographics.
            System.out.println("+++ Updating demographics on enterprise subject +++");
            pm.updateEnterpriseSubject(enterpriseSubjectId, subject);
        }

        // Insert system-level subject match fields (for subsequent find operations).
        searchRecord.setId(systemSubjectId);
        pm.insertSubjectMatchFields(searchRecord);

        // Create and store cross-reference.
        pm.insertSubjectCrossReference(systemSubjectId, enterpriseSubjectId, matchScore);

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
        EMPINotification notification = new EMPINotification();
        this.addSubjectToNotification(notification, enterpriseSubjectId);
        return notification;
    }

    /**
     *
     * @param subject
     * @param recordMatches
     * @return
     * @throws EMPIException
     */
    private boolean isAnyMatchedRecordInSameIdentifierDomain(Subject subject, List<ScoredRecord> recordMatches) throws EMPIException {

        // First check scored record against record matches to see if it is safe to link
        // the records.
        for (ScoredRecord recordMatch : recordMatches) {
            if (this.isMatchedRecordInSameIdentifierDomain(subject, recordMatch)) {
                return true;  // Early exit.
            }
        }

        // FIXME: NEED TO OPTIMIZE ... doing many DB reads where should only read once per
        // record.

        /* WORK ON THIS LATER ... need to think fully through
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
        }*/
        return false;
    }

    /**
     * 
     * @param subject
     * @param matchedRecord
     * @return
     * @throws EMPIException
     */
    private boolean isMatchedRecordInSameIdentifierDomain(Subject subject, ScoredRecord matchedRecord) throws EMPIException {
        String matchedSystemSubjectId = matchedRecord.getRecord().getId();
        // Load identifiers for matched system subject.
        PersistenceManager pm = this.getPersistenceManager();
        List<SubjectIdentifier> matchedSubjectIdentifiers = pm.loadSubjectIdentifiers(matchedSystemSubjectId);

        // Get list of the subject's identifiers.
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();

        return this.isMatchedRecordInSameIdentifierDomain(matchedSubjectIdentifiers, subjectIdentifiers);
    }

    /**
     * 
     * @param matchedSubjectIdentifiers
     * @param subjectIdentifiers
     * @return
     * @throws EMPIException
     */
    private boolean isMatchedRecordInSameIdentifierDomain(List<SubjectIdentifier> matchedSubjectIdentifiers, List<SubjectIdentifier> subjectIdentifiers) throws EMPIException {

        // NOTE: Small list, so brute force is OK.

        // See if there is a match between the "matched subject" and the subject.
        for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
            SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
            for (SubjectIdentifier matchedSubjectIdentifier : matchedSubjectIdentifiers) {
                SubjectIdentifierDomain matchedSubjectIdentifierDomain = matchedSubjectIdentifier.getIdentifierDomain();
                if (subjectIdentifierDomain.equals(matchedSubjectIdentifierDomain)) {
                    // No need to look further.
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
     * @param subject
     * @param updateNotificationContent
     * @return
     * @throws EMPIException
     */
    private String insertEnterpriseSubject(Subject subject) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Build (from received subject) and store enterprise-level subject.
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
        return subject.getInternalId();
    }
}
