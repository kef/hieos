/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.pixmgr.notifier;

import com.vangent.hieos.hl7v3util.atna.ATNAAuditEventHelper;
import com.vangent.hieos.hl7v3util.client.HL7V3ClientResponse;
import com.vangent.hieos.hl7v3util.client.PIXConsumerClient;
import com.vangent.hieos.pixnotifierutil.client.PIXUpdateNotification;
import com.vangent.hieos.pixnotifierutil.config.CrossReferenceConsumerConfig;
import com.vangent.hieos.pixnotifierutil.config.PIXNotifierConfig;
import com.vangent.hieos.pixnotifierutil.exception.PIXNotifierUtilException;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEventPatientRecord;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
@MessageDriven(mappedName = "jms/PIXNotifierMsgQ", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class PIXNotifierMessageBean implements MessageListener {

    private final static Logger logger = Logger.getLogger(PIXNotifierMessageBean.class);

    /**
     * 
     */
    public PIXNotifierMessageBean() {
    }

    /**
     *
     * @param message
     */
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage m = (ObjectMessage) message;
                Object so = m.getObject();  // Serialized object.
                if (so instanceof PIXUpdateNotification) {
                    PIXUpdateNotification pixUpdateNotification = (PIXUpdateNotification) so;
                    logger.info("Received message ");
                    logger.info(pixUpdateNotification.getText());
                    this.sendNotifications(pixUpdateNotification);
                }
            } else if (message instanceof TextMessage) {
                TextMessage m = (TextMessage) message;
                logger.info("--- Received message ");
                logger.info(m.getText());
                logger.info("----------");
            } else {
                logger.info("Received message of type " + message.getClass().getName());
            }
        } catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace(System.err);
        }

    }

    /**
     * 
     * @param pixUpdateNotification
     */
    public void sendNotifications(PIXUpdateNotification pixUpdateNotification) {

        // Get EMPI configuration.
        PIXNotifierConfig pixNotifierConfig;
        try {
            pixNotifierConfig = PIXNotifierConfig.getInstance();
        } catch (PIXNotifierUtilException ex) {
            logger.error("Error getting PIX Notifier configuration when trying to send PIX Update Notifications", ex);
            return;
        }

        // Get PIX Manager configuration (mainly for device id info)..
        XConfigActor pixManagerConfig;
        try {
            pixManagerConfig = this.getPIXManagerConfig();
        } catch (XConfigException ex) {
            logger.error("Error getting PIX Manager configuration when trying to send PIX Update Notifications", ex);
            return;
        }
        DeviceInfo senderDeviceInfo = new DeviceInfo(pixManagerConfig);

        // Go through each cross reference consumer configuration.
        for (CrossReferenceConsumerConfig crossReferenceConsumerConfig : pixNotifierConfig.getCrossReferenceConsumerConfigs()) {

            // Only send notifications if enabled for the consumer.
            if (crossReferenceConsumerConfig.isEnabled()) {
                XConfigActor pixConsumerActorConfig = crossReferenceConsumerConfig.getConfigActor();
                if (pixConsumerActorConfig != null) {

                    DeviceInfo receiverDeviceInfo = new DeviceInfo(pixConsumerActorConfig);
                    PIXConsumerClient pixConsumerClient = new PIXConsumerClient(pixConsumerActorConfig, null /* XLogMessage */);
                    // Now send notifications for each individual subject on notification list.
                    for (Subject subject : pixUpdateNotification.getSubjects()) {
                        try {
                            Subject notificationSubject = this.getNotificationSubject(subject, crossReferenceConsumerConfig);
                            if ((notificationSubject != null) && !notificationSubject.getSubjectIdentifiers().isEmpty()) {
                                logger.info("Sending PIX Update Notification [device id = "
                                        + receiverDeviceInfo.getId() + ", endpoint=" + 
                                        pixConsumerActorConfig.getTransaction("PatientRegistryRecordRevised").getEndpointURL() + "]");
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
            // FIXME: Do something.
            logger.error("Error cloning Subject", ex);
        }
        return notificationSubject;
    }

    /**
     * 
     * @return
     * @throws XConfigException
     */
    private XConfigActor getPIXManagerConfig() throws XConfigException {
        XConfig xconf;
        xconf = XConfig.getInstance();
        XConfigObject homeCommunity = xconf.getHomeCommunityConfig();
        return (XConfigActor) homeCommunity.getXConfigObjectWithName("pix", XConfig.PIX_MANAGER_TYPE);
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
            logger.error("PIX Notifier EXCEPTION: Could not perform ATNA audit", ex);
        }
    }
}
