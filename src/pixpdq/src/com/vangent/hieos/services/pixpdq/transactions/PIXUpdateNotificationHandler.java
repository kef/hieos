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

import com.vangent.hieos.hl7v3util.client.PIXConsumerClient;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.services.pixpdq.empi.api.EMPINotification;
import com.vangent.hieos.xutil.xconfig.XConfig;
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
        try {
            XConfig xConfig = XConfig.getInstance();
            DeviceInfo senderDeviceInfo = new DeviceInfo(configActor);
            // FIXME: Make into a real implementation (put on queue and get out!!!).
            for (Subject subject : updateNotificationContent.getSubjects()) {
                XConfigActor pixConsumerConfig = xConfig.getXConfigActorByName("pixconsumer1", "PIXConsumerType");
                DeviceInfo receiverDeviceInfo = new DeviceInfo(pixConsumerConfig);
                PIXConsumerClient pixConsumerClient = new PIXConsumerClient(pixConsumerConfig);
                MCCI_IN000002UV01_Message ackMessage = pixConsumerClient.patientRegistryRecordRevised(senderDeviceInfo, receiverDeviceInfo, subject);
            }
        } catch (Exception ex) {
            // FIXME: Do something ...
            logger.error("Exception sending update notification", ex);
        } finally {
            // Do nothing.
        }
    }
}
