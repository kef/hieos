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
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.empi.adapter.EMPINotification;
import com.vangent.hieos.empi.validator.AddSubjectValidator;
import com.vangent.hieos.subjectmodel.InternalId;
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
        InternalId systemSubjectId = newSubject.getInternalId();
        InternalId enterpriseSubjectId = null;
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
        List<ScoredRecord> matchedRecords = matchResults.getMatches();

        long start = System.currentTimeMillis();

        if (matchedRecords.isEmpty()) {
            // No matching records.
            enterpriseSubjectId = this.insertEnterpriseSubject(newSubject);

        } else if (!this.isLinkAllowed(newSubject, matchedRecords)) {
            logger.trace("+++++ Not linking subject with same identifier domain +++++");
            // Do not place record along side any record within the same identifier domain.
            matchedRecords.clear();  // Treat as though a match did not occur.

            // Insert a new enterprise record.
            enterpriseSubjectId = this.insertEnterpriseSubject(newSubject);
        } else {
            // >=1 matches

            // Cross reference will be to first matched record.  All other records will be merged later below.
            ScoredRecord matchedRecord = matchedRecords.get(0);
            InternalId matchedSystemSubjectId = matchedRecord.getRecord().getInternalId();
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
        // FIXME: Make this configurable.
        PersistenceManager pm = this.getPersistenceManager();

        // Merge all other matches (if any) into first matched record (surviving enterprise record).
        Set<Long> subsumedEnterpriseSubjectIds = new HashSet<Long>();
        for (int i = 1; i < matchedRecords.size(); i++) {
            ScoredRecord matchedRecord = matchedRecords.get(i);
            InternalId matchedSystemSubjectId = matchedRecord.getRecord().getInternalId();
            InternalId subsumedEnterpriseSubjectId = pm.getEnterpriseSubjectId(matchedSystemSubjectId);

            // Make sure that the subject has not already been merged.
            if (!subsumedEnterpriseSubjectIds.contains(subsumedEnterpriseSubjectId.getId())) {

                // Make sure that subsumed is not already linked to the surviving enterprise subject.
                if (!subsumedEnterpriseSubjectId.getId().equals(enterpriseSubjectId.getId())) {

                    // FIXME: Add more constraints here (run LinkConstraintController?) ...
                    subsumedEnterpriseSubjectIds.add(subsumedEnterpriseSubjectId.getId());
                    pm.mergeEnterpriseSubjects(enterpriseSubjectId, subsumedEnterpriseSubjectId);
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
    private boolean isLinkAllowed(Subject newSubject, List<ScoredRecord> matchedRecords) throws EMPIException {
        LinkConstraintController linkConstraintController = new LinkConstraintController(this.getPersistenceManager());
        return linkConstraintController.isLinkAllowed(newSubject, matchedRecords);
    }

    /**
     * 
     * @param newSubject
     * @param matchedRecord
     * @return
     * @throws EMPIException
     */
    private boolean isLinkAllowed(Subject newSubject, ScoredRecord matchedRecord) throws EMPIException {
        LinkConstraintController linkConstraintController = new LinkConstraintController(this.getPersistenceManager());
        return linkConstraintController.isLinkAllowed(newSubject, matchedRecord);
    }

    /**
     *
     * @param newEnterpriseSubject
     * @param updateNotificationContent
     * @return
     * @throws EMPIException
     */
    private InternalId insertEnterpriseSubject(Subject newEnterpriseSubject) throws EMPIException {
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
