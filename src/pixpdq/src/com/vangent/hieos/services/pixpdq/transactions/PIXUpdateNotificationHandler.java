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
package com.vangent.hieos.services.pixpdq.transactions;

import com.vangent.hieos.empi.config.CrossReferenceConsumerConfig;
import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.atna.ATNAAuditEventHelper;
import com.vangent.hieos.hl7v3util.client.HL7V3ClientResponse;
import com.vangent.hieos.hl7v3util.client.PIXConsumerClient;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.empi.api.EMPINotification;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEventPatientRecord;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIXUpdateNotificationHandler {

    private static final Logger logger = Logger.getLogger(PIXUpdateNotificationHandler.class);
    private XConfigActor configActor = null;
    private XLogMessage logMessage;

    /**
     *
     * @param configActor
     */
    public PIXUpdateNotificationHandler(XConfigActor configActor, XLogMessage logMessage) {
        this.configActor = configActor;
        this.logMessage = logMessage;
    }

    /**
     *
     * @param updateNotificationContent
     */
    public void sendUpdateNotifications(EMPINotification updateNotificationContent) {
        EMPIConfig empiConfig;
        try {
            empiConfig = EMPIConfig.getInstance();
        } catch (EMPIException ex) {
            logger.error("Error getting EMPI configuration when trying to send PIX Update Notifications", ex);
            return;
        }
        if (!empiConfig.isUpdateNotificationEnabled()) {
            // Notifications are turned off -- get out now.
            return; // Early exit!!
        }

        // FIXME: Put notifications onto a queue ... this will delay synchronous web service process.
        DeviceInfo senderDeviceInfo = new DeviceInfo(configActor);

        // Go through each cross reference consumer configuration.
        for (CrossReferenceConsumerConfig crossReferenceConsumerConfig : empiConfig.getCrossReferenceConsumerConfigs()) {

            // Only send notifications if enabled for the consumer.
            if (crossReferenceConsumerConfig.isEnabled()) {
                XConfigActor pixConsumerActorConfig = crossReferenceConsumerConfig.getConfigActor();
                if (pixConsumerActorConfig != null) {

                    DeviceInfo receiverDeviceInfo = new DeviceInfo(pixConsumerActorConfig);
                    PIXConsumerClient pixConsumerClient = new PIXConsumerClient(pixConsumerActorConfig, logMessage);
                    // Now send notifications for each individual subject on notification list.
                    for (Subject subject : updateNotificationContent.getSubjects()) {
                        try {
                            Subject notificationSubject = this.getNotificationSubject(subject, crossReferenceConsumerConfig);
                            if ((notificationSubject != null) && !notificationSubject.getSubjectIdentifiers().isEmpty()) {
                                logger.info("Sending PIX Update Notification [device id = "
                                        + receiverDeviceInfo.getId() + "]");
                                HL7V3ClientResponse clientResponse = pixConsumerClient.patientRegistryRecordRevised(
                                        senderDeviceInfo, receiverDeviceInfo, notificationSubject);
                                this.performAuditPIXUpdateNotification(notificationSubject, clientResponse);
                            }
                        } catch (Exception ex) {
                            logger.error("Error sending PIX Update Notification to receiver [device id = "
                                    + receiverDeviceInfo.getId() + "]", ex);
                        }
                    }
                } else {
                    logger.error("Error sending PIX Update Notification (no XConfig entry) to receiver [device id = "
                            + crossReferenceConsumerConfig.getDeviceId() + "]");
                }
            }
        }
    }

    /**
     * 
     * @param subject
     * @param crossReferenceConsumerConfig
     * @return
     */
    private Subject getNotificationSubject(Subject subject, CrossReferenceConsumerConfig crossReferenceConsumerConfig) {
        Subject notificationSubject = null;
        try {
            notificationSubject = (Subject) subject.clone();
            List<SubjectIdentifier> subjectIdentifiersToKeep = new ArrayList<SubjectIdentifier>();
            // Get list of interested identifier domains.
            List<SubjectIdentifierDomain> interestedIdentifierDomains = crossReferenceConsumerConfig.getIdentifierDomains();
            List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
            for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
                // See if we should keep this identifier.
                SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
                for (SubjectIdentifierDomain interestedIdentifierDomain : interestedIdentifierDomains) {
                    if (subjectIdentifierDomain.equals(interestedIdentifierDomain)) {
                        // Keep this identifier.
                        if (logger.isTraceEnabled()) {
                            logger.trace("Found subject identifier of interest: " + subjectIdentifier.getCXFormatted());
                        }
                        subjectIdentifiersToKeep.add(subjectIdentifier);
                        break;
                    }
                }
            }
            notificationSubject.setSubjectIdentifiers(subjectIdentifiersToKeep);
        } catch (Exception ex) {
            // FIXME: Do sometehing.
            logger.error("Error cloning Subject", ex);
        }
        return notificationSubject;
    }

    /**
     * 
     * @param subject
     * @param pixConsumerActorConfig
     */
    private void performAuditPIXUpdateNotification(
            Subject subject, HL7V3ClientResponse clientResponse) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                ATNAAuditEventPatientRecord auditEvent = ATNAAuditEventHelper.getATNAAuditEventPatientRecord(
                        ATNAAuditEvent.ActorType.PIX_MANAGER, subject,
                        clientResponse.getTargetEndpoint(), clientResponse.getMessageId());
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            logger.error("PIXManager EXCEPTION: Could not perform ATNA audit", ex);
        }
    }
}
