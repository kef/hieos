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
import ca.uhn.hl7v2.model.v25.datatype.FN;
import ca.uhn.hl7v2.model.v25.datatype.HD;
import ca.uhn.hl7v2.model.v25.datatype.XAD;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.group.RSP_K21_QUERY_RESPONSE;
import ca.uhn.hl7v2.model.v25.message.RSP_K21;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.PD1;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.QAK;
import ca.uhn.hl7v2.model.v25.segment.QPD;
import ca.uhn.hl7v2.model.v25.segment.QRI;
import com.vangent.hieos.subjectmodel.Address;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.subjectmodel.SubjectName;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;
import com.vangent.hieos.subjectmodel.TelecomAddress;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import java.io.IOException;

/**
 *
 * @author Bernie Thuman
 */
public class PDQResponseMessageBuilder extends QueryResponseMessageBuilder {

    /**
     *
     * @param inMessage
     */
    public PDQResponseMessageBuilder(Message inMessage) {
        super(inMessage);
    }

    /**
     *
     * @param subjectSearchResponse
     * @return
     * @throws HL7Exception
     */
    public Message buildPDQResponse(SubjectSearchResponse subjectSearchResponse) throws HL7Exception {
        // Inbound message header.
        Message inMessage = this.getInMessage();
        MSH inMessageMSH = (MSH) inMessage.get("MSH");

        // Create outbound message (and initialize).
        RSP_K21 outMessage = new RSP_K21();
        try {
            // SEGMENT: Message Header (MSH)
            outMessage.initQuickstart("RSP", "K22", "P");
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

        // Go through list of returned subjects.
        int subjectRepCount = 0;
        for (Subject subject : subjectSearchResponse.getSubjects()) {

            // Create a new query response group for each subject
            RSP_K21_QUERY_RESPONSE queryResponse = outMessage.getQUERY_RESPONSE(subjectRepCount++);
            PID pid = queryResponse.getPID();

            // Gender.
            pid.getAdministrativeSex().setValue(subject.getGender().getCode());

            // DOB.
            pid.getDateTimeOfBirth().getTime().setValue(Hl7Date.toHL7format(subject.getBirthTime()));
            
            // Account number.
            // TODO - many ...
            
            // SSN.
            // TBD.

            // Subject identifiers.
            int repCount = 0;
            for (SubjectIdentifier subjectIdentifier : subject.getSubjectIdentifiers()) {

                // Convert SubjectIdentifier to CX format.  Must be fully qualified identifier with
                // all three subcomponents filled in for the assigning authority associated with the
                // patient id.
                CX pidCX = pid.insertPatientIdentifierList(repCount++);
                pidCX.getIDNumber().setValue(subjectIdentifier.getIdentifier());
                SubjectIdentifierDomain identifierDomain = subjectIdentifier.getIdentifierDomain();
                HD assigningAuthority = pidCX.getAssigningAuthority();
                assigningAuthority.getNamespaceID().setValue(identifierDomain.getNamespaceId());
                assigningAuthority.getUniversalID().setValue(identifierDomain.getUniversalId());
                assigningAuthority.getUniversalIDType().setValue(identifierDomain.getUniversalIdType());
            }
            
            // Subject other identifiers.
            // TODO - subject other identifiers.

            // Subject names.
            repCount = 0;
            for (SubjectName subjectName : subject.getSubjectNames()) {
                // Convert SubjectName to XPN format.  
                XPN patientNameXPN = pid.insertPatientName(repCount++);
                patientNameXPN.getGivenName().setValue(subjectName.getGivenName());
                patientNameXPN.getPrefixEgDR().setValue(subjectName.getPrefix());
                patientNameXPN.getSuffixEgJRorIII().setValue(subjectName.getSuffix());
                FN familyNameFN = patientNameXPN.getFamilyName();
                familyNameFN.getSurname().setValue(subjectName.getFamilyName());

                // FIXME: Need to get Name Type Code on Add/Update to store.
                patientNameXPN.getNameTypeCode().setValue("L");

                // FIXME: How to deal with middle names?
            }

            // Subject addresses.
            repCount = 0;
            for (Address address : subject.getAddresses()) {
                // Convert SubjectAddress to XAD format.  
                XAD patientAddressXAD = pid.insertPatientAddress(repCount++);
                patientAddressXAD.getStreetAddress().getStreetOrMailingAddress().setValue(address.getStreetAddressLine1());
                patientAddressXAD.getCity().setValue(address.getCity());
                patientAddressXAD.getStateOrProvince().setValue(address.getState());
                patientAddressXAD.getZipOrPostalCode().setValue(address.getPostalCode());
                patientAddressXAD.getCountry().setValue(address.getCountry());
                patientAddressXAD.getAddressType().setValue(address.getUse());
            }

            // TODO finish
            // Subject telecom addresses.
            repCount = 0;
            for (TelecomAddress telecomAddresses : subject.getTelecomAddresses()) {
                // Convert TelecomAddress to XTN format.
            }

            // PD1
            PD1 pd1 = queryResponse.getPD1();
            // TODO.

            // QR1
            QRI qri = queryResponse.getQRI();
            qri.getCandidateConfidence().setValue(new Integer(subject.getMatchConfidencePercentage()).toString());
        }


        // SEGMENT: Query Acknowledgement (QAK)
        QPD inMessageQPD = (QPD) inMessage.get("QPD");
        QAK qak = outMessage.getQAK();
        qak.getQueryTag().setValue(inMessageQPD.getQueryTag().getValue());
        qak.getQueryResponseStatus().setValue(subjectRepCount == 0 ? "NF" : "OK");

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
        RSP_K21 outMessage = this.buildBaseErrorResponse();

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
    public RSP_K21 buildBaseErrorResponse() throws HL7Exception {
        // Inbound message header.
        Message inMessage = this.getInMessage();
        MSH inMessageMSH = (MSH) inMessage.get("MSH");

        // Create outbound message (and initialize).
        RSP_K21 outMessage = new RSP_K21();

        // SEGMENT: Message Header (MSH)
        try {
            outMessage.initQuickstart("RSP", "K22", "P");
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
