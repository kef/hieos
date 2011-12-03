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

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectMergeRequest;
import com.vangent.hieos.services.pixpdq.empi.api.UpdateNotificationContent;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class MergeSubjectsHandler extends BaseHandler {

    private static final Logger logger = Logger.getLogger(MergeSubjectsHandler.class);

    /**
     *
     * @param configActor
     * @param persistenceManager
     */
    public MergeSubjectsHandler(XConfigActor configActor, PersistenceManager persistenceManager) {
        super(configActor, persistenceManager);
    }

    /**
     *
     * @param subjectMergeRequest
     * @return
     * @throws EMPIException
     */
    public UpdateNotificationContent mergeSubjects(SubjectMergeRequest subjectMergeRequest) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        UpdateNotificationContent updateNotificationContent = new UpdateNotificationContent();

        Subject survivingSubject = subjectMergeRequest.getSurvivingSubject();
        Subject subsumedSubject = subjectMergeRequest.getSubsumedSubject();

        // Validate input is usable.
        this.validateSubjects(survivingSubject, subsumedSubject);

        // Lookup surviving and subsumed subjects.
        Subject baseSurvivingSubject = this.getBaseSubject(survivingSubject, "surviving");
        Subject baseSubsumedSubject = this.getBaseSubject(subsumedSubject, "subsumed");

        if (baseSurvivingSubject.getType().equals(Subject.SubjectType.SYSTEM)
                && baseSubsumedSubject.getType().equals(Subject.SubjectType.SYSTEM)) {
            // Both are system-level subjects.
            String survivingSubjectSystemSubjectId = baseSurvivingSubject.getInternalId();
            String subsumedSubjectSystemSubjectId = baseSubsumedSubject.getInternalId();

            // See if this is referencing the same system-level subject.
            if (survivingSubjectSystemSubjectId.equals(subsumedSubjectSystemSubjectId)) {
                // Get all identifiers for the system-subject.
                List<SubjectIdentifier> subjectIdentifiers = pm.loadSubjectIdentifiers(subsumedSubjectSystemSubjectId);
                if (subjectIdentifiers.size() == 1) {
                    // This case should never happen given prior validation checks.
                    // FIXME????
                    throw new EMPIException("Should never happen - skipping merge");
                }
                // Now, remove the "subsumed" identifier.
                // Find the identifier to remove.
                SubjectIdentifier subsumedSubjectIdentifierToRemove =
                        this.findSubjectIdentifier(subsumedSubject.getSubjectIdentifiers().get(0), subjectIdentifiers);

                // Delete the identifier.
                pm.deleteSubjectIdentifier(subsumedSubjectIdentifierToRemove.getInternalId());
            } else {

                // Get base enterprise subjects.
                String baseEnterpriseSurvivingSubjectId = pm.getEnterpriseSubjectId(baseSurvivingSubject);
                String baseEnterpriseSubsumedSubjectId = pm.getEnterpriseSubjectId(baseSubsumedSubject);

                // Delete the "subsumed" system-level subject.
                pm.deleteSubject(baseSubsumedSubject);

                // FIXME: MAKE CONFIGURABLE!!!

                // Now move all cross references.
                pm.mergeEnterpriseSubjects(baseEnterpriseSurvivingSubjectId, baseEnterpriseSubsumedSubjectId);
            }
        } else if (baseSurvivingSubject.getType().equals(Subject.SubjectType.ENTERPRISE)
                && baseSubsumedSubject.getType().equals(Subject.SubjectType.ENTERPRISE)) {
            // Both are enterprise-level subjects.
            // Now move all cross references.
            String baseEnterpriseSurvivingSubjectId = baseSurvivingSubject.getInternalId();
            String baseEnterpriseSubsumedSubjectId = baseSubsumedSubject.getInternalId();
            pm.mergeEnterpriseSubjects(baseEnterpriseSurvivingSubjectId, baseEnterpriseSubsumedSubjectId);
        }

        // FIXME: Complete ALL cases.
        // FIXME: Fill-in update notification content.
        return updateNotificationContent;
    }

    /**
     *
     * @param searchSubjectIdentifier
     * @param subjectIdentifiers
     * @return
     */
    private SubjectIdentifier findSubjectIdentifier(SubjectIdentifier searchSubjectIdentifier, List<SubjectIdentifier> subjectIdentifiers) {
        // TBD: Move this method ...
        for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
            if (subjectIdentifier.equals(searchSubjectIdentifier)) {
                return subjectIdentifier;
            }
        }
        return null;
    }

    /**
     *
     * @param survivingSubject
     * @param subsumedSubject
     * @throws EMPIException
     */
    private void validateSubjects(Subject survivingSubject, Subject subsumedSubject) throws EMPIException {
        this.valididateSubject(survivingSubject, "surviving");
        this.valididateSubject(subsumedSubject, "subsumed");
        SubjectIdentifier survivingSubjectIdentifier = survivingSubject.getSubjectIdentifiers().get(0);
        SubjectIdentifier subsumedSubjectIdentifier = subsumedSubject.getSubjectIdentifiers().get(0);
        if (survivingSubjectIdentifier.equals(subsumedSubjectIdentifier)) {
            throw new EMPIException("Same identifier supplied - skipping merge");
        }
    }

    /**
     *
     * @param subject
     * @param subjectType
     * @throws EMPIException
     */
    private void valididateSubject(Subject subject, String subjectType) throws EMPIException {
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        if (subjectIdentifiers.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("No ").append(subjectType).append(" subject identifier supplied - skipping merge");
            throw new EMPIException(sb.toString());
        }
        if (subjectIdentifiers.size() > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(">1 ").append(subjectType).append(" subject identifier supplied - skipping merge");
            throw new EMPIException(sb.toString());
        }
    }

    /**
     *
     * @param subject
     * @param subjectType
     * @return
     * @throws EMPIException
     */
    private Subject getBaseSubject(Subject subject, String subjectType) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        SubjectIdentifier subjectIdentifier = subjectIdentifiers.get(0);
        Subject baseMergeSubject = pm.loadBaseSubjectByIdentifier(subjectIdentifier);
        if (baseMergeSubject == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(subjectType).append(" subject not found - skipping merge");
            throw new EMPIException(sb.toString());
        }
        return baseMergeSubject;
    }
}
