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
package com.vangent.hieos.services.pixpdqv3.transactions;

import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.pixnotifierutil.client.PIXNotifierClientException;
import com.vangent.hieos.empi.adapter.EMPINotification;
import com.vangent.hieos.pixnotifierutil.client.PIXNotifierClient;
import com.vangent.hieos.pixnotifierutil.client.PIXUpdateNotification;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
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

        // Send message to PIX Notifier for distribution to interested parties.
        PIXNotifierClient pixNotifierClient = new PIXNotifierClient();
        try {
            PIXUpdateNotification pixUpdateNotification = new PIXUpdateNotification();
            pixUpdateNotification.setText("This is a test notification!");
            pixUpdateNotification.setSubjects(updateNotificationContent.getSubjects());
            pixNotifierClient.sendNotification(pixUpdateNotification);
        } catch (PIXNotifierClientException ex) {
            logger.error("Unable to send message to PIX Notifier", ex);
        }
    }
        
}
