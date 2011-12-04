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
import com.vangent.hieos.hl7v3util.client.PIXConsumerClient;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.services.pixpdq.empi.api.EMPINotification;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIXUpdateNotificationHandler {

    private static final Logger logger = Logger.getLogger(PIXUpdateNotificationHandler.class);
    private XConfigActor configActor = null;

    /**
     *
     * @param configActor
     */
    public PIXUpdateNotificationHandler(XConfigActor configActor) {
        this.configActor = configActor;
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
                    PIXConsumerClient pixConsumerClient = new PIXConsumerClient(pixConsumerActorConfig);

                    // Now send notifications for each individual subject on notification list.
                    for (Subject subject : updateNotificationContent.getSubjects()) {
                        try {
                            logger.info("Sending PIX Update Notification [device id = "
                                    + receiverDeviceInfo.getId() + "]");

                            // FIXME: Need to only send interested identifier domains.
                            MCCI_IN000002UV01_Message ackMessage = pixConsumerClient.patientRegistryRecordRevised(
                                    senderDeviceInfo, receiverDeviceInfo, subject);
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
}
