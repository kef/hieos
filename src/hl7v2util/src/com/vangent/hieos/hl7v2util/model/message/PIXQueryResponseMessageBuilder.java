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
package com.vangent.hieos.hl7v2util.model.message;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.HD;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.group.RSP_K23_QUERY_RESPONSE;
import ca.uhn.hl7v2.model.v25.message.RSP_K23;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.QAK;
import ca.uhn.hl7v2.model.v25.segment.QPD;
import com.vangent.hieos.hl7v2util.model.builder.BuilderConfig;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;
import java.io.IOException;

/**
 *
 * @author Bernie Thuman
 */
public class PIXQueryResponseMessageBuilder extends QueryResponseMessageBuilder {

    /**
     *
     * @param builderConfig
     * @param inMessage
     */
    public PIXQueryResponseMessageBuilder(BuilderConfig builderConfig, Message inMessage) {
        super(builderConfig, inMessage);
    }

    /**
     *
     * @param subjectSearchResponse
     * @return
     * @throws HL7Exception
     */
    public Message buildPIXQueryResponse(SubjectSearchResponse subjectSearchResponse) throws HL7Exception {
        // Inbound message header.
        Message inMessage = this.getInMessage();
        MSH inMessageMSH = (MSH) inMessage.get("MSH");

        // Create outbound message (and initialize).
        RSP_K23 outMessage = new RSP_K23();
        try {
            // SEGMENT: Message Header (MSH)
            outMessage.initQuickstart("RSP", "K23", "P");
        } catch (IOException ex) {
            throw new HL7Exception(ex);
        }
        MSH msh = outMessage.getMSH();

        // Set receiving application/facility to inbound message sending application/facility.
        msh.getReceivingApplication().parse(inMessageMSH.getSendingApplication().encode());
        msh.getReceivingFacility().parse(inMessageMSH.getSendingFacility().encode());

        // FIXME: Where do we check that we agree with the inbound message's receiving application?

        // Set sending application/facility to inbound message receiving application/facility.
        msh.getSendingApplication().parse(inMessageMSH.getReceivingApplication().encode());
        msh.getSendingFacility().parse(inMessageMSH.getReceivingFacility().encode());

        // SEGMENT: Message Acknowledgement (MSA)
        MSA msa = outMessage.getMSA();
        msa.getAcknowledgmentCode().setValue("AA");  // Application Accept
        msa.getMessageControlID().setValue(inMessageMSH.getMessageControlID().getValue());

        // SEGMENT: Query Parameter Definition (QPD)
        // Echo contents of request.

        // SEGMENT: PID
        RSP_K23_QUERY_RESPONSE queryResponse = outMessage.getQUERY_RESPONSE();
        PID pid = queryResponse.getPID();

        // Go through list of returned subjects.
        int idCount = 0;
        for (Subject subject : subjectSearchResponse.getSubjects()) {
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
        }
        if (idCount != 0) {
            // Set PID-5 according to IHE PIX v2 spec
            pid.insertPatientName(0);  // Empty (repetition 0)
            XPN patientName = pid.insertPatientName(1);
            patientName.getNameTypeCode().setValue("S");
        }

        // SEGMENT: Query Acknowledgement (QAK)
        QPD inMessageQPD = (QPD) inMessage.get("QPD");
        QAK qak = outMessage.getQAK();
        qak.getQueryTag().setValue(inMessageQPD.getQueryTag().getValue());
        qak.getQueryResponseStatus().setValue(idCount == 0 ? "NF" : "OK");

        // SEGMENT: Query Parameter Definition (QPD)
        // Echo back in-bound message QPD segment.
        QPD qpd = outMessage.getQPD();
        qpd.parse(inMessageQPD.encode());

        return outMessage;
    }

    /**
     *
     * @param errorText
     * @return
     * @throws HL7Exception
     */
    public Message buildErrorResponse(String errorText) throws HL7Exception {
        RSP_K23 outMessage = this.buildBaseErrorResponse();

        // SEGMENT: Error Segment [ERR]
        ERR err = outMessage.getERR();

        // FIXME - review IHE_ITI_TF_Vol2a.
        err.getSeverity().setValue("E");  // E - Error, F - Fatal, I - Informational, W - Warning
        err.getHL7ErrorCode().getIdentifier().setValue("207");
        err.getHL7ErrorCode().getText().setValue(errorText);
        return outMessage;
    }

    /**
     *
     * @return @throws HL7Exception
     * @throws HL7Exception
     */
    public RSP_K23 buildBaseErrorResponse() throws HL7Exception {
        // Inbound message header.
        Message inMessage = this.getInMessage();
        MSH inMessageMSH = (MSH) inMessage.get("MSH");

        // Create outbound message (and initialize).
        RSP_K23 outMessage = new RSP_K23();

        // SEGMENT: Message Header (MSH)
        try {
            outMessage.initQuickstart("RSP", "K23", "P");
        } catch (IOException ex) {
            throw new HL7Exception(ex);
        }
        MSH msh = outMessage.getMSH();

        // Set receiving application/facility to inbound message sending application/facility.
        msh.getReceivingApplication().parse(inMessageMSH.getSendingApplication().encode());
        msh.getReceivingFacility().parse(inMessageMSH.getSendingFacility().encode());

        // FIXME: Where do we check that we agree with the inbound message's receiving application?

        // Set sending application/facility to inbound message receiving application/facility.
        msh.getSendingApplication().parse(inMessageMSH.getReceivingApplication().encode());
        msh.getSendingFacility().parse(inMessageMSH.getReceivingFacility().encode());

        // SEGMENT: Message Acknowledgement (MSA)
        MSA msa = outMessage.getMSA();
        msa.getAcknowledgmentCode().setValue("AE");  // Application Error
        msa.getMessageControlID().setValue(inMessageMSH.getMessageControlID().getValue());

        // SEGMENT: Query Acknowledgement (QAK)
        QPD inMessageQPD = (QPD) inMessage.get("QPD");
        QAK qak = outMessage.getQAK();
        qak.getQueryTag().setValue(inMessageQPD.getQueryTag().getValue());
        qak.getQueryResponseStatus().setValue("AE");

        // SEGMENT: Query Parameter Definition (QPD)
        // Echo back in-bound message QPD segment.
        QPD qpd = outMessage.getQPD();
        qpd.parse(inMessageQPD.encode());

        return outMessage;
    }
}
