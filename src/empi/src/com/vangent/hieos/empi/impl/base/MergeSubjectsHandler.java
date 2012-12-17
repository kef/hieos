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
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.persistence.EnterpriseSubjectController;
import com.vangent.hieos.empi.persistence.SubjectController;
import com.vangent.hieos.empi.validator.MergeSubjectsValidator;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectMergeRequest;
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
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    public MergeSubjectsHandler(PersistenceManager persistenceManager, DeviceInfo senderDeviceInfo) {
        super(persistenceManager, senderDeviceInfo);
    }

    /**
     *
     * @param subjectMergeRequest
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownSubjectIdentifier
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public EMPINotification mergeSubjects(SubjectMergeRequest subjectMergeRequest) throws EMPIException, EMPIExceptionUnknownSubjectIdentifier, EMPIExceptionUnknownIdentifierDomain {
        PersistenceManager pm = this.getPersistenceManager();

        // First, run validations on input.
        MergeSubjectsValidator validator = new MergeSubjectsValidator(pm, this.getSenderDeviceInfo());
        validator.setSubjectMergeRequest(subjectMergeRequest);
        validator.run();

        EMPINotification notification = new EMPINotification();

        Subject survivingSubject = subjectMergeRequest.getSurvivingSubject();
        Subject subsumedSubject = subjectMergeRequest.getSubsumedSubject();

        // Lookup surviving and subsumed subjects.
        Subject baseSurvivingSubject = this.getBaseSubject(survivingSubject, "surviving");
        Subject baseSubsumedSubject = this.getBaseSubject(subsumedSubject, "subsumed");

        SubjectController subjectController = new SubjectController(this.getPersistenceManager());
        EnterpriseSubjectController enterpriseSubjectController = new EnterpriseSubjectController(this.getPersistenceManager());

        if (baseSurvivingSubject.getType().equals(Subject.SubjectType.SYSTEM)
                && baseSubsumedSubject.getType().equals(Subject.SubjectType.SYSTEM)) {
            // Both are system-level subjects.
            InternalId survivingSubjectSystemSubjectId = baseSurvivingSubject.getInternalId();
            InternalId subsumedSubjectSystemSubjectId = baseSubsumedSubject.getInternalId();

            // See if this is referencing the same system-level subject.
            if (survivingSubjectSystemSubjectId.equals(subsumedSubjectSystemSubjectId)) {
                // Get all identifiers for the system-subject.
                List<SubjectIdentifier> subjectIdentifiers = subjectController.loadSubjectIdentifiers(subsumedSubjectSystemSubjectId);
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
                subjectController.deleteSubjectIdentifier(subsumedSubjectIdentifierToRemove.getInternalId());

                // FIXME: Should we even allow this case (see above).
                // FIXME: Do update notification.
            } else {

                // Get base enterprise subjects.

                InternalId baseEnterpriseSurvivingSubjectId = enterpriseSubjectController.getEnterpriseSubjectId(baseSurvivingSubject);
                InternalId baseEnterpriseSubsumedSubjectId = enterpriseSubjectController.getEnterpriseSubjectId(baseSubsumedSubject);

                // Delete the "subsumed" system-level subject.
                subjectController.delete(baseSubsumedSubject);

                // FIXME: MAKE CONFIGURABLE!!!

                // Now move all cross references.
                enterpriseSubjectController.merge(baseEnterpriseSurvivingSubjectId, baseEnterpriseSubsumedSubjectId);
                this.addSubjectToNotification(notification, baseEnterpriseSurvivingSubjectId);
            }
        } else if (baseSurvivingSubject.getType().equals(Subject.SubjectType.ENTERPRISE)
                && baseSubsumedSubject.getType().equals(Subject.SubjectType.ENTERPRISE)) {
            // Both are enterprise-level subjects.
            // Now move all cross references.
            InternalId baseEnterpriseSurvivingSubjectId = baseSurvivingSubject.getInternalId();
            InternalId baseEnterpriseSubsumedSubjectId = baseSubsumedSubject.getInternalId();
            enterpriseSubjectController.merge(baseEnterpriseSurvivingSubjectId, baseEnterpriseSubsumedSubjectId);
            this.addSubjectToNotification(notification, baseEnterpriseSurvivingSubjectId);
        }

        // FIXME: Complete ALL cases.
        return notification;
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
     * @param subject
     * @param subjectType
     * @return
     * @throws EMPIException
     */
    private Subject getBaseSubject(Subject subject, String subjectType) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        PersistenceManager pm = this.getPersistenceManager();
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        // Only using first identifier in list to support merge.
        SubjectIdentifier subjectIdentifier = subjectIdentifiers.get(0);
        SubjectController subjectController = new SubjectController(pm);
        List<Subject> baseMergeSubjects = subjectController.loadBaseSubjects(subjectIdentifier);
        if (baseMergeSubjects.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(subjectType).append(" subject not found - skipping merge");
            throw new EMPIException(sb.toString());
        }
        if (baseMergeSubjects.size() > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(subjectType).append(baseMergeSubjects.size()).append(" subjects (>1) found - skipping merge");
            throw new EMPIException(sb.toString());
        }
        // Given above constraints - the list has one item in it.  Return the first in list.
        return baseMergeSubjects.get(0);
    }
}
