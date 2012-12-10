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

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.model.SubjectCrossReference;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.empi.adapter.EMPINotification;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownSubjectIdentifier;
import com.vangent.hieos.empi.validator.UpdateSubjectValidator;
import com.vangent.hieos.subjectmodel.InternalId;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateSubjectHandler extends BaseHandler {

    private static final Logger logger = Logger.getLogger(UpdateSubjectHandler.class);

    /**
     * 
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    public UpdateSubjectHandler(PersistenceManager persistenceManager, DeviceInfo senderDeviceInfo) {
        super(persistenceManager, senderDeviceInfo);
    }

    /**
     *
     * @param subject
     * @return
     * @throws EMPIException
     */
    public EMPINotification updateSubject(Subject subject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier {
        PersistenceManager pm = this.getPersistenceManager();

        // First, run validations on input.
        UpdateSubjectValidator validator = new UpdateSubjectValidator(pm, this.getSenderDeviceInfo());
        validator.setSubject(subject);
        validator.run();

        EMPINotification updateNotificationContent = new EMPINotification();


        // Make sure that there is only one subject identifier to update.
        /* CONNECTATHON HACK (for ICW).
        if (subjectIdentifiers.size() > 1) {
        throw new EMPIException("Only one identifier should be provided for the subject - skipping update.");
        }*/
        // NOTE: Decided to keep code above commented out - will use first identifier for update.

        // Get the subject (using the first identifier).
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        SubjectIdentifier subjectIdentifier = subjectIdentifiers.get(0);
        List<Subject> baseSubjects = pm.loadBaseSubjectsByIdentifier(subjectIdentifier);
        if (baseSubjects.isEmpty()) {
            throw new EMPIExceptionUnknownSubjectIdentifier(
                    subjectIdentifier.getCXFormatted()
                    + " is not a known identifier");
        }
        if (baseSubjects.size() > 1) {
            throw new EMPIException(
                    subjectIdentifier.getCXFormatted()
                    + " identifier resulted in more than one record to update .. no update performed");
        }
        Subject baseSubject = baseSubjects.get(0);
        if (baseSubject.getType().equals(Subject.SubjectType.SYSTEM)) {
            updateNotificationContent = this.updateSystemSubject(baseSubject, subject);
        } else {
            InternalId enterpriseSubjectId = baseSubject.getInternalId();
            // FIXME: MUCH TO DO HERE!!!!
        }
        // FIXME: MUCH TO DO HERE!!!!

        // FIXME: Fill-in update notification content.
        return updateNotificationContent;
    }

    /**
     *
     * @param baseSubject
     * @param subject
     * @throws EMPIException
     */
    private EMPINotification updateSystemSubject(Subject baseSubject, Subject subject) throws EMPIException, EMPIExceptionUnknownSubjectIdentifier, EMPIExceptionUnknownIdentifierDomain {
        PersistenceManager pm = this.getPersistenceManager();
        EMPINotification notification = new EMPINotification();

        // Get the enterprise subject id.
        InternalId enterpriseSubjectId = pm.getEnterpriseSubjectId(baseSubject);

        // delete the system-level subject.
        pm.deleteSubject(baseSubject);

        // See if this subject was the only cross reference to the enterprise.
        List<SubjectCrossReference> subjectCrossReferences = pm.loadEnterpriseSubjectCrossReferences(enterpriseSubjectId);
        if (subjectCrossReferences.isEmpty()) {
            // In this case, delete the enterprise record
            pm.deleteSubject(enterpriseSubjectId, Subject.SubjectType.ENTERPRISE);
        } else {
            // Update demographics on enterprise-subject with last updated system-level subject.
            logger.trace("+++ Updating enterprise subject with last updated demographics +++");
            InternalId lastUpdatedSystemSubjectId = pm.getLastUpdatedSystemSubjectId(enterpriseSubjectId);
            Subject lastUpdatedSystemSubject = pm.loadSubject(lastUpdatedSystemSubjectId);
            pm.updateEnterpriseSubject(enterpriseSubjectId, lastUpdatedSystemSubject);
            this.addSubjectToNotification(notification, enterpriseSubjectId);
        }

        // FIXME: How about existing identifiers?

        // Now, run through normal add operation.
        AddSubjectHandler addSubjectHandler = new AddSubjectHandler(pm, this.getSenderDeviceInfo());
        EMPINotification addNotification = addSubjectHandler.addSubject(subject);
        notification.addNotification(addNotification);

        return notification;
    }
}
