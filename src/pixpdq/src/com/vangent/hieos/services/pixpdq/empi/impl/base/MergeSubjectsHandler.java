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

        // Lookup surviving and subsumed subjects.
        Subject survivingSubject = subjectMergeRequest.getSurvivingSubject();
        Subject subsumedSubject = subjectMergeRequest.getSubsumedSubject();
        Subject baseSurvivingSubject = this.getBaseSubjectForMerge(survivingSubject, "surviving");
        Subject baseSubsumedSubject = this.getBaseSubjectForMerge(subsumedSubject, "subsumed");

        if (baseSurvivingSubject.getType().equals(Subject.SubjectType.SYSTEM)
                && baseSubsumedSubject.getType().equals(Subject.SubjectType.SYSTEM)) {
            // Both are system-level subjects.

            // Get base enterprise subjects.
            String baseEnterpriseSurvivingSubjectId = pm.getEnterpriseSubjectId(baseSurvivingSubject.getId());
            String baseEnterpriseSubsumedSubjectId = pm.getEnterpriseSubjectId(baseSubsumedSubject.getId());

            // Delete the "subsumed" subject.
            pm.deleteSystemSubject(baseSubsumedSubject.getId());

            // FIXME: MAKE CONFIGURABLE!!!

            // Now move all cross references.
            pm.mergeEnterpriseSubjects(baseEnterpriseSurvivingSubjectId, baseEnterpriseSubsumedSubjectId);

        } else if (baseSurvivingSubject.getType().equals(Subject.SubjectType.ENTERPRISE)
                && baseSubsumedSubject.getType().equals(Subject.SubjectType.ENTERPRISE)) {
            // Both are enterprise-level subjects.
            // Now move all cross references.
            String baseEnterpriseSurvivingSubjectId = baseSurvivingSubject.getId();
            String baseEnterpriseSubsumedSubjectId = baseSubsumedSubject.getId();
            pm.mergeEnterpriseSubjects(baseEnterpriseSurvivingSubjectId, baseEnterpriseSubsumedSubjectId);
        }

        // FIXME: Complete ALL cases.
        // FIXME: Fill-in update notification content.
        return updateNotificationContent;
    }

    /**
     *
     * @param subject
     * @param subjectType
     * @return
     * @throws EMPIException
     */
    private Subject getBaseSubjectForMerge(Subject subject, String subjectType) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        Subject baseSubject = null;
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
        SubjectIdentifier subjectIdentifier = subjectIdentifiers.get(0);
        baseSubject = pm.loadBaseSubjectByIdentifier(subjectIdentifier);
        if (baseSubject == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(subjectType).append(" subject not found - skipping merge");
            throw new EMPIException(sb.toString());
        }
        return baseSubject;
    }
}
