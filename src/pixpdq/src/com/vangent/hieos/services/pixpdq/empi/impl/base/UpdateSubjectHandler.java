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
import com.vangent.hieos.empi.model.SubjectCrossReference;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.services.pixpdq.empi.api.EMPINotification;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
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
     * @param configActor
     * @param persistenceManager
     */
    public UpdateSubjectHandler(XConfigActor configActor, PersistenceManager persistenceManager) {
        super(configActor, persistenceManager);
    }

    /**
     *
     * @param subject
     * @return
     * @throws EMPIException
     */
    public EMPINotification updateSubject(Subject subject) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        EMPINotification updateNotificationContent = new EMPINotification();

        // First validate identifier domains assocated with the subject's identifiers.
        this.validateSubjectIdentifierDomains(subject);

        // Make sure that subject identifiers are present.
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        if (subjectIdentifiers.isEmpty()) {
            throw new EMPIException("No identifiers provided for subject - skipping update.");
        }

        // Make sure that there is only one subject identifier to update.
        if (subjectIdentifiers.size() > 1) {
            throw new EMPIException("Only one identifier should be provided for the subject - skipping update.");
        }

        // Get the subject (using the first identifier).
        SubjectIdentifier subjectIdentifier = subjectIdentifiers.get(0);
        Subject baseSubject = pm.loadBaseSubjectByIdentifier(subjectIdentifier);
        if (baseSubject == null) {
            throw new EMPIException(
                    subjectIdentifier.getCXFormatted()
                    + " is not a known identifier",
                    EMPIException.ERROR_CODE_UNKNOWN_KEY_IDENTIFIER);
        }

        this.validateSubjectCodes(subject);

        if (baseSubject.getType().equals(Subject.SubjectType.SYSTEM)) {
            updateNotificationContent = this.updateSystemSubject(baseSubject, subject);
        } else {
            String enterpriseSubjectId = baseSubject.getInternalId();
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
    private EMPINotification updateSystemSubject(Subject baseSubject, Subject subject) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        EMPINotification notification = new EMPINotification();

        // Get the enterprise subject id.
        String enterpriseSubjectId = pm.getEnterpriseSubjectId(baseSubject);

        // delete the system-level subject.
        pm.deleteSubject(baseSubject);

        // See if this subject was the only cross reference to the enterprise.
        List<SubjectCrossReference> subjectCrossReferences = pm.loadEnterpriseSubjectCrossReferences(enterpriseSubjectId);
        if (subjectCrossReferences.isEmpty()) {
            // In this case, delete the enterprise record
            pm.deleteSubject(enterpriseSubjectId, Subject.SubjectType.ENTERPRISE);
        } else {
            // Update demographics on enterprise-subject with last updated system-level subject.
            System.out.println("+++ Updating enterprise subject with last updated demographics +++");
            String lastUpdatedSystemSubjectId = pm.getLastUpdatedSystemSubjectId(enterpriseSubjectId);
            Subject lastUpdatedSystemSubject = pm.loadSubject(lastUpdatedSystemSubjectId);
            pm.updateEnterpriseSubject(enterpriseSubjectId, lastUpdatedSystemSubject);
            this.addSubjectToNotification(notification, enterpriseSubjectId);
        }

        // FIXME: How about existing identifiers?

        // Now, run through normal add operation.
        AddSubjectHandler addSubjectHandler = new AddSubjectHandler(this.getConfigActor(), pm);
        EMPINotification addNotification = addSubjectHandler.addSubject(subject);
        notification.addNotification(addNotification);

        return notification;
    }
}
