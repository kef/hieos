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
package com.vangent.hieos.services.pixpdqv2.v2handler;

import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.pixnotifierutil.client.PIXNotifierClientException;
import com.vangent.hieos.empi.adapter.EMPINotification;
import com.vangent.hieos.pixnotifierutil.client.PIXNotifierClient;
import com.vangent.hieos.pixnotifierutil.client.PIXUpdateNotification;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIXUpdateNotificationHandler {

    private static final Logger logger = Logger.getLogger(PIXUpdateNotificationHandler.class);

    /**
     *
     */
    public PIXUpdateNotificationHandler() {
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
            pixUpdateNotification.setSubjects(updateNotificationContent.getSubjects());
            pixNotifierClient.sendNotification(pixUpdateNotification);
        } catch (PIXNotifierClientException ex) {
            logger.error("Unable to send message to PIX Notifier", ex);
        }
    }
        
}
