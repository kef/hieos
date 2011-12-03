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

import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.hl7v3util.client.PIXConsumerClient;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.services.pixpdq.empi.api.UpdateNotificationContent;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateNotificationHandler extends BaseHandler {

    private static final Logger logger = Logger.getLogger(UpdateNotificationHandler.class);

    /**
     *
     * @param configActor
     * @param persistenceManager
     */
    public UpdateNotificationHandler(XConfigActor configActor, PersistenceManager persistenceManager) {
        super(configActor, persistenceManager);
    }

    /**
     *
     * @param updateNotificationContent 
     */
    public void sendUpdateNotifications(UpdateNotificationContent updateNotificationContent) {
        if (updateNotificationContent == null) {
            // FIXME: Should we ever get here?
            return;  // Early exit!
        }
        /*
        PersistenceManager pm = new PersistenceManager();
        try {
            // MUST BE CALLED OUTSIDE EXISTING TRANSACTION
            pm.open();
            // FIXME: Add queing, and make a real implementation.
            Subject subject = pm.loadEnterpriseSubjectIdentifiersAndNamesOnly(enterpriseSubjectId);
            XConfig xConfig = XConfig.getInstance();
            XConfigActor pixConsumerConfig = xConfig.getXConfigActorByName("pixconsumer1", "PIXConsumerType");
            DeviceInfo senderDeviceInfo = new DeviceInfo(this.getConfigActor());
            DeviceInfo receiverDeviceInfo = new DeviceInfo(pixConsumerConfig);
            PIXConsumerClient pixConsumerClient = new PIXConsumerClient(pixConsumerConfig);
            MCCI_IN000002UV01_Message ackMessage = pixConsumerClient.patientRegistryRecordRevised(senderDeviceInfo, receiverDeviceInfo, subject);
        } catch (Exception ex) {
            // FIXME: Do something ...
            logger.error("Exception sending update notification", ex);
        } finally {
            pm.close();
        }
         */
    }
}
