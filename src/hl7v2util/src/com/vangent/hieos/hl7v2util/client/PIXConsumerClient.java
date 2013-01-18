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
package com.vangent.hieos.hl7v2util.client;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import com.vangent.hieos.hl7v2util.exception.HL7v2UtilException;
import com.vangent.hieos.hl7v2util.model.message.PIXUpdateNotificationMessageBuilder;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIXConsumerClient {

    private static final Logger logger = Logger.getLogger(PIXConsumerClient.class);
    private XConfigActor pixConsumerActorConfig;

    /**
     *
     * @param pixConsumerActorConfig
     */
    public PIXConsumerClient(XConfigActor pixConsumerActorConfig) {
        this.pixConsumerActorConfig = pixConsumerActorConfig;
    }

    /**
     * 
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     * @param subject
     * @return
     */
    // FIXME: Create wrapper for "Message" result.
    public Message patientRegistryRecordRevised(
            DeviceInfo senderDeviceInfo,
            DeviceInfo receiverDeviceInfo,
            Subject subject) throws HL7v2UtilException {
        try {
            // Build PIXUpdateNotification
            PIXUpdateNotificationMessageBuilder pixUpdateNotificationMessageBuilder = new PIXUpdateNotificationMessageBuilder(senderDeviceInfo, receiverDeviceInfo);
            Message outMessage = pixUpdateNotificationMessageBuilder.buildPIXUpdateNotificationMessage(subject);

            // Get HL7v2Client and send out message.
            String endpoint = pixConsumerActorConfig.getTransaction("PatientRegistryRecordRevised").getEndpointURL();
            HL7v2Client hl7v2Client = new HL7v2Client(endpoint);
            Message responseMessage = hl7v2Client.sendMessage(outMessage);

            // Return response.
            return responseMessage;

        } catch (HL7Exception ex) {
            logger.error("HL7Exception: ", ex);
            throw new HL7v2UtilException("Exception sending PIX Update Notification", ex);
        } finally {
        }
    }
}
