/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.pixpdqv2.v2handler;

import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.empi.adapter.EMPINotification;
import com.vangent.hieos.hl7v2util.atna.ATNAAuditEventHelper;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAHL7v2AuditEventPatientIdentityFeed;
import com.vangent.hieos.xutil.atna.XATNALogger;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class ADTMessageHandler extends HL7V2MessageHandler {

    private final static Logger logger = Logger.getLogger(ADTMessageHandler.class);

    /**
     *
     * @param updateNotificationContent
     */
    public void sendUpdateNotifications(EMPINotification updateNotificationContent) {
        PIXUpdateNotificationHandler pixUpdateNotificationHandler = new PIXUpdateNotificationHandler();
        pixUpdateNotificationHandler.sendUpdateNotifications(updateNotificationContent);
    }

    /**
     * 
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     * @param inMessageTerser
     * @param sourceIP
     * @param eventActionCode
     * @param subjectIdentifiers
     */
    public void performAuditPatientIdentityFeed(
            DeviceInfo senderDeviceInfo,
            DeviceInfo receiverDeviceInfo,
            Terser inMessageTerser,
            String sourceIP,
            ATNAHL7v2AuditEventPatientIdentityFeed.EventActionCode eventActionCode,
            List<SubjectIdentifier> subjectIdentifiers) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                ATNAHL7v2AuditEventPatientIdentityFeed auditEvent =
                        ATNAAuditEventHelper.getATNAAuditEventPatientIdentityFeed(
                        ATNAAuditEvent.ActorType.PIX_MANAGER_V2,
                        senderDeviceInfo,
                        receiverDeviceInfo,
                        inMessageTerser,
                        sourceIP,
                        eventActionCode,
                        subjectIdentifiers);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            logger.error("PIXv2Manager EXCEPTION: Could not perform ATNA audit", ex);
        }
    }
}
