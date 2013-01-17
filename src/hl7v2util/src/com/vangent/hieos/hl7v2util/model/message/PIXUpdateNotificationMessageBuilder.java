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
package com.vangent.hieos.hl7v2util.model.message;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.HD;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.message.ADT_A05;
import ca.uhn.hl7v2.model.v25.segment.EVN;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import java.io.IOException;

/**
 *
 * @author Bernie Thuman
 */
public class PIXUpdateNotificationMessageBuilder {

    private DeviceInfo senderDeviceInfo;
    private DeviceInfo receiverDeviceInfo;

    /**
     * 
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     */
    public PIXUpdateNotificationMessageBuilder(DeviceInfo senderDeviceInfo, DeviceInfo receiverDeviceInfo) {
        this.senderDeviceInfo = senderDeviceInfo;
        this.receiverDeviceInfo = receiverDeviceInfo;
    }

    /**
     * 
     * @param subject
     * @return
     * @throws HL7Exception
     */
    public Message buildPIXUpdateNotificationMessage(Subject subject) throws HL7Exception {

        // Create outbound message (and initialize).
        ADT_A05 outMessage = new ADT_A05();

        // SEGMENT: Message Header (MSH)
        try {
            outMessage.initQuickstart("ADT", "A31", "P");
        } catch (IOException ex) {
            throw new HL7Exception(ex);
        }
        MSH msh = outMessage.getMSH();

        // Set receiving application/facility to inbound message sending application/facility.

        // Receiver:
        msh.getReceivingApplication().parse(receiverDeviceInfo.getId());
        msh.getReceivingFacility().parse(receiverDeviceInfo.getName());

        // Sender:
        // FIXME: HOW DO WE GET THIS FROM XCONFIG OR OTHERWISE in concert with HL7v3 PIX????
        msh.getSendingApplication().parse(senderDeviceInfo.getId());
        msh.getSendingFacility().parse(senderDeviceInfo.getName());

        // SEGMENT: PID
        PID pid = outMessage.getPID();

        // Go through list of returned subjects.
        int idCount = 0;
        // Go through list of identifiers for the subject.
        for (SubjectIdentifier subjectIdentifier : subject.getSubjectIdentifiers()) {

            // Convert SubjectIdentifier to CX format.  Must be fully qualified identifier with
            // all three subcomponents filled in for the assigning authority associated with the
            // patient id.
            CX pidCX = pid.insertPatientIdentifierList(idCount++);
            pidCX.getIDNumber().setValue(subjectIdentifier.getIdentifier());
            //pidCX.parse(subjectIdentifier.getCXFormatted());
            SubjectIdentifierDomain identifierDomain = subjectIdentifier.getIdentifierDomain();
            HD assigningAuthority = pidCX.getAssigningAuthority();
            assigningAuthority.getNamespaceID().setValue(identifierDomain.getNamespaceId());
            assigningAuthority.getUniversalID().setValue(identifierDomain.getUniversalId());
            assigningAuthority.getUniversalIDType().setValue(identifierDomain.getUniversalIdType());
        }
        if (idCount != 0) {
            // Set PID-5 according to IHE PIX v2 spec
            XPN patientName = pid.insertPatientName(0);  // Repetition 0.
            patientName.parse(" ");  // Insert single space according to IHE PIX v2 spec.
        }

        // SEGMENT: PV1
        PV1 pv1 = outMessage.getPV1();
        pv1.getPatientClass().parse("N");

        // SEGMENT: EVN
        EVN evn = outMessage.getEVN();
        String currentTime = Hl7Date.now();
        evn.getRecordedDateTime().parse(currentTime);
        evn.getEventOccurred().parse(currentTime);

        return outMessage;
    }
}
