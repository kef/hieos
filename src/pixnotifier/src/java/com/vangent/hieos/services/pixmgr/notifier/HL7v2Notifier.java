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

import ca.uhn.hl7v2.model.Message;
import com.vangent.hieos.hl7v2util.client.PIXConsumerClient;
import com.vangent.hieos.hl7v2util.exception.HL7v2UtilException;
import com.vangent.hieos.pixnotifierutil.config.CrossReferenceConsumerConfig;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7v2Notifier extends HL7Notifier {

    private final static Logger logger = Logger.getLogger(HL7v2Notifier.class);

    /**
     *
     * @param senderDeviceInfo
     * @param crossReferenceConsumerConfig
     */
    public HL7v2Notifier(DeviceInfo senderDeviceInfo, CrossReferenceConsumerConfig crossReferenceConsumerConfig) {
        super(senderDeviceInfo, crossReferenceConsumerConfig);
    }

    /**
     * 
     * @param notificationSubject
     */
    @Override
    public void sendNotification(Subject notificationSubject) {
        CrossReferenceConsumerConfig crossReferenceConsumerConfig = this.getCrossReferenceConsumerConfig();
        XConfigActor pixConsumerActorConfig = crossReferenceConsumerConfig.getHL7v2ConfigActor();
        if (pixConsumerActorConfig != null) {
            DeviceInfo receiverDeviceInfo = new DeviceInfo(pixConsumerActorConfig);
            logger.info("Sending PIX Update Notification [device id = "
                    + receiverDeviceInfo.getId() + ", endpoint="
                    + pixConsumerActorConfig.getTransaction("PatientRegistryRecordRevised").getEndpointURL() + "]");
            PIXConsumerClient pixConsumerClient = new PIXConsumerClient(pixConsumerActorConfig);
            try {
                Message pixConsumerResponse = pixConsumerClient.patientRegistryRecordRevised(this.getSenderDeviceInfo(), receiverDeviceInfo, notificationSubject);
                // TODO: Implement ATNA audit log.
            } catch (HL7v2UtilException ex) {
                logger.error("Error sending PIX Update Notification to receiver [device id = "
                        + receiverDeviceInfo.getId() + "]", ex);
            }
        }
    }
}
