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
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.match.RecordBuilder;
import com.vangent.hieos.empi.match.ScoredRecord;
import com.vangent.hieos.empi.model.SubjectCrossReference;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.services.pixpdq.empi.api.UpdateNotificationContent;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import java.util.List;
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
     */
    public AddSubjectHandler(XConfigActor configActor, PersistenceManager persistenceManager) {
        super(configActor, persistenceManager);
    }

    /**
     *
     * @param subject
     * @return
     * @throws EMPIException
     */
    public UpdateNotificationContent addSubject(Subject subject) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        UpdateNotificationContent updateNotificationContent = new UpdateNotificationContent();

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
        FindSubjectsHandler findSubjectsHandler = new FindSubjectsHandler(this.getConfigActor(), this.getPersistenceManager());
        List<ScoredRecord> recordMatches = findSubjectsHandler.getRecordMatches(searchRecord);

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
            matchScore = findSubjectsHandler.getMatchScore(matchedRecord);
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
            pm.mergeEnterpriseSubjects(enterpriseSubjectId, scoredRecord.getRecord().getId());
        }
        // FIXME: Fill-in update notification content.
        return updateNotificationContent;
    }
}
