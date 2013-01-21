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

import com.vangent.hieos.empi.adapter.EMPINotification;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownSubjectIdentifier;
import com.vangent.hieos.empi.match.MatchAlgorithm;
import com.vangent.hieos.empi.match.MatchResults;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.match.RecordBuilder;
import com.vangent.hieos.empi.match.ScoredRecord;
import com.vangent.hieos.empi.model.SubjectReviewItem;
import com.vangent.hieos.empi.persistence.EnterpriseSubjectController;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.persistence.SubjectController;
import com.vangent.hieos.empi.validator.AddSubjectValidator;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.Subject;
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
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    public AddSubjectHandler(PersistenceManager persistenceManager, DeviceInfo senderDeviceInfo) {
        super(persistenceManager, senderDeviceInfo);
    }

    /**
     *
     * @param newSubject
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownSubjectIdentifier
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public EMPINotification addSubject(Subject newSubject) throws EMPIException, EMPIExceptionUnknownSubjectIdentifier, EMPIExceptionUnknownIdentifierDomain {
        PersistenceManager pm = this.getPersistenceManager();
        EnterpriseSubjectController enterpriseSubjectController = new EnterpriseSubjectController(pm);
        SubjectController subjectController = new SubjectController(pm);

        // First, run validations on input.
        AddSubjectValidator validator = new AddSubjectValidator(pm, this.getSenderDeviceInfo());
        validator.setSubject(newSubject);
        validator.run();

        // Store the subject @ system-level - will stamp with subjectId.
        String identitySource = newSubject.getIdentitySource();
        newSubject.setType(Subject.SubjectType.SYSTEM);
        subjectController.insert(newSubject);

        // Get prepared for next steps ..
        InternalId systemSubjectId = newSubject.getInternalId();
        InternalId enterpriseSubjectId;
        int matchScore = 100;    // Default.

        // Find matching records.
        RecordBuilder rb = new RecordBuilder();
        Record searchRecord = rb.build(newSubject);
        MatchAlgorithm matchAlgo = MatchAlgorithm.getMatchAlgorithm(pm);
        MatchResults matchResults = matchAlgo.findMatches(searchRecord, MatchAlgorithm.MatchType.SUBJECT_FEED);
        if (!matchResults.getPossibleMatches().isEmpty()) {
            // FIXME!!!!
            logger.warn("+++++ DO SOMETHING HERE ... store possible matches");
        }

        long start = System.currentTimeMillis();
        List<ScoredRecord> matchedRecords = matchResults.getMatches();
        if (matchedRecords.isEmpty()) {
            // No matching records - insert new enterprise record.
            enterpriseSubjectId = enterpriseSubjectController.insert(newSubject);

        } else {
            // Look for potential duplicates.
            List<SubjectReviewItem> potentialDuplicates = this.getPotentialDuplicates(newSubject, matchedRecords);
            if (!potentialDuplicates.isEmpty()) {
                logger.info("+++++ Not linking subject with same identifier domain +++++");
                // Do not place record along side any record within the same identifier domain.
                matchedRecords.clear();  // Treat as though a match did not occur.

                // Keep track of potential duplicates (for later review).
                subjectController.insertSubjecReviewItems(potentialDuplicates);

                // Insert a new enterprise record.
                enterpriseSubjectId = enterpriseSubjectController.insert(newSubject);
            } else {
                // >=1 matches

                // Cross reference will be to first matched record.  All other records will be merged later below.
                ScoredRecord matchedRecord = matchedRecords.get(0);
                InternalId matchedSystemSubjectId = matchedRecord.getRecord().getInternalId();

                enterpriseSubjectId = enterpriseSubjectController.getEnterpriseSubjectId(matchedSystemSubjectId);
                matchScore = matchedRecord.getMatchScorePercentage();

                // Update enterprise subject with latest demographics.
                logger.trace("+++ Updating demographics on enterprise subject +++");
                enterpriseSubjectController.update(enterpriseSubjectId, newSubject);
            }
        }

        // Insert system-level subject match fields (for subsequent find operations).
        searchRecord.setId(systemSubjectId);
        searchRecord.setIdentitySource(identitySource);
        subjectController.insert(searchRecord);

        // Create and store cross-reference to enterprise subject.
        enterpriseSubjectController.insertSubjectCrossReference(systemSubjectId, enterpriseSubjectId, matchScore);

        // Merge all other matches (if any) into first matched record (surviving enterprise record).
        this.mergeMatchedRecords(matchedRecords, enterpriseSubjectId);

        if (logger.isDebugEnabled()) {
            logger.debug("EMPI persistence TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
        }
        EMPINotification notification = new EMPINotification();
        this.addSubjectToNotification(notification, enterpriseSubjectId);
        return notification;
    }

    /**
     *
     * @param matchedRecords
     * @param enterpriseSubjectId
     * @throws EMPIException
     */
    private void mergeMatchedRecords(List<ScoredRecord> matchedRecords, InternalId enterpriseSubjectId) throws EMPIException {
        // FIXME: Make this method configurable.
        EnterpriseSubjectController enterpriseSubjectController = new EnterpriseSubjectController(this.getPersistenceManager());

        // Merge all other matches (if any) into first matched record (surviving enterprise record).
        Set<Long> subsumedEnterpriseSubjectIds = new HashSet<Long>();
        for (int i = 1; i < matchedRecords.size(); i++) {
            ScoredRecord matchedRecord = matchedRecords.get(i);
            InternalId matchedSystemSubjectId = matchedRecord.getRecord().getInternalId();
            InternalId subsumedEnterpriseSubjectId = enterpriseSubjectController.getEnterpriseSubjectId(matchedSystemSubjectId);

            // Make sure that the subject has not already been merged.
            if (!subsumedEnterpriseSubjectIds.contains(subsumedEnterpriseSubjectId.getId())) {

                // Make sure that subsumed is not already linked to the surviving enterprise subject.
                if (!subsumedEnterpriseSubjectId.getId().equals(enterpriseSubjectId.getId())) {

                    // FIXME: Add more constraints here (run LinkConstraintController?) ...
                    subsumedEnterpriseSubjectIds.add(subsumedEnterpriseSubjectId.getId());
                    enterpriseSubjectController.merge(enterpriseSubjectId, subsumedEnterpriseSubjectId);
                }
            }
        }
    }

    /**
     *
     * @param newSubject
     * @param matchedRecords
     * @return
     * @throws EMPIException
     */
    private List<SubjectReviewItem> getPotentialDuplicates(Subject newSubject, List<ScoredRecord> matchedRecords) throws EMPIException {
        LinkConstraintController linkConstraintController = new LinkConstraintController(this.getPersistenceManager());
        return linkConstraintController.getPotentialDuplicates(newSubject, matchedRecords);
    }
}
