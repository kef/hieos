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
import com.vangent.hieos.pixnotifierutil.config.CrossReferenceConsumerConfig;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEventPatientRecord;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7v3Notifier extends HL7Notifier {

    private final static Logger logger = Logger.getLogger(HL7v3Notifier.class);

    /**
     *
     * @param senderDeviceInfo
     * @param crossReferenceConsumerConfig
     */
    public HL7v3Notifier(DeviceInfo senderDeviceInfo, CrossReferenceConsumerConfig crossReferenceConsumerConfig) {
        super(senderDeviceInfo, crossReferenceConsumerConfig);
    }

    /**
     * 
     * @param notificationSubject
     * @throws SOAPFaultException
     */
    @Override
    public void sendNotification(Subject notificationSubject) {
        CrossReferenceConsumerConfig crossReferenceConsumerConfig = this.getCrossReferenceConsumerConfig();
        XConfigActor pixConsumerActorConfig = crossReferenceConsumerConfig.getHL7v3ConfigActor();
        if (pixConsumerActorConfig != null) {
            DeviceInfo receiverDeviceInfo = new DeviceInfo(pixConsumerActorConfig);
            PIXConsumerClient pixConsumerClient = new PIXConsumerClient(pixConsumerActorConfig, null /* XLogMessage */);
            logger.info("Sending PIX Update Notification [device id = "
                    + receiverDeviceInfo.getId() + ", endpoint="
                    + pixConsumerActorConfig.getTransaction("PatientRegistryRecordRevised").getEndpointURL() + "]");
            try {
                HL7V3ClientResponse clientResponse;
                clientResponse = pixConsumerClient.patientRegistryRecordRevised(this.getSenderDeviceInfo(), receiverDeviceInfo, notificationSubject);
                this.performAuditPIXUpdateNotification(notificationSubject, clientResponse);
            } catch (Exception ex) {
                logger.error("Error sending PIX Update Notification to receiver [device id = "
                        + receiverDeviceInfo.getId() + "]", ex);
            }
        }
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
