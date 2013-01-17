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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.empi.adapter.EMPIAdapter;
import com.vangent.hieos.empi.adapter.EMPIAdapterFactory;
import com.vangent.hieos.empi.adapter.EMPINotification;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownSubjectIdentifier;
import com.vangent.hieos.hl7v2util.acceptor.impl.Connection;
import com.vangent.hieos.hl7v2util.model.subject.SubjectMergeRequestBuilder;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.SubjectMergeRequest;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class ADTPatientMergeMessageHandler extends ADTMessageHandler {

    private final static Logger logger = Logger.getLogger(ADTPatientMergeMessageHandler.class);

    /**
     * 
     * @param connection
     * @param inMessage
     * @return
     * @throws ApplicationException
     * @throws HL7Exception
     */
    @Override
    public Message processMessage(Connection connection, Message inMessage) throws ApplicationException, HL7Exception {
        logger.info("Processing ADT Patient Merge message ...");
        Message outMessage = null;
        try {
            // Get Terser.
            Terser terser = new Terser(inMessage);

            // FIXME: Create DeviceInfo properly; receiver device info not yet used.
            DeviceInfo senderDeviceInfo = getSenderDeviceInfo(terser);
            DeviceInfo receiverDeviceInfo = getReceiverDeviceInfo(terser);

            // Build subject from HL7v2 message.
            SubjectMergeRequestBuilder subjectMergeRequestBuilder = new SubjectMergeRequestBuilder(terser);
            SubjectMergeRequest subjectMergeRequest = subjectMergeRequestBuilder.buildSubjectMergeRequest();

            // Clone identifiers (for audit later).
            //List<SubjectIdentifier> subjectIdentifiers = SubjectIdentifier.clone(subject.getSubjectIdentifiers());

            // Go to EMPI to merge subjects.
            EMPIAdapter adapter = EMPIAdapterFactory.getInstance();
            adapter.setSenderDeviceInfo(senderDeviceInfo);
            EMPINotification updateNotificationContent = adapter.mergeSubjects(subjectMergeRequest);
            this.sendUpdateNotifications(updateNotificationContent);

            // Build response.
            outMessage = this.buildAck(inMessage, "Success!", null /* errorText */, null /* errorCode */);
        } catch (EMPIExceptionUnknownIdentifierDomain ex) {
            outMessage = this.buildAck(inMessage, null /* responseText */, ex.getMessage(),
                    EMPIExceptionUnknownIdentifierDomain.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        } catch (EMPIExceptionUnknownSubjectIdentifier ex) {
            outMessage = this.buildAck(inMessage, null /* responseText */, ex.getMessage(),
                    EMPIExceptionUnknownSubjectIdentifier.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        } catch (EMPIException ex) {
            outMessage = this.buildAck(inMessage, null /* responseText */, ex.getMessage(), null /* errorCode */);
        } catch (Exception ex) {
            outMessage = this.buildAck(inMessage, null /* responseText */, "Exception: " + ex.getClass().getName() + " - " + ex.getMessage(), null /* errorCode */);
        }
        return outMessage;
    }

    /**
     *
     * @param connection
     * @param in
     * @return
     */
    @Override
    public boolean canProcess(Connection connection, Message in) {
        return true;
    }
}
