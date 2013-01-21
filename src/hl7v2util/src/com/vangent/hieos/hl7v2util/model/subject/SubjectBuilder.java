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
package com.vangent.hieos.hl7v2util.model.subject;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v231.datatype.CE;
import ca.uhn.hl7v2.model.v231.datatype.CX;
import ca.uhn.hl7v2.model.v231.datatype.DLN;
import ca.uhn.hl7v2.model.v231.datatype.XAD;
import ca.uhn.hl7v2.model.v231.datatype.XPN;
import ca.uhn.hl7v2.model.v231.datatype.XTN;
import ca.uhn.hl7v2.model.v231.segment.PID;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.hl7v2util.model.builder.BuilderConfig;
import com.vangent.hieos.subjectmodel.Address;
import com.vangent.hieos.subjectmodel.CodedValue;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectCitizenship;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectName;
import com.vangent.hieos.subjectmodel.SubjectPersonalRelationship;
import com.vangent.hieos.subjectmodel.TelecomAddress;
import com.vangent.hieos.xutil.hl7.formatutil.HL7FormatUtil;
import com.vangent.hieos.hl7v2util.model.builder.BuilderHelper;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectBuilder {

    private static final Logger logger = Logger.getLogger(SubjectBuilder.class);
    private BuilderConfig builderConfig;
    private Terser terser;

    /**
     * 
     * @param builderConfig
     * @param terser
     */
    public SubjectBuilder(BuilderConfig builderConfig, Terser terser) {
        this.builderConfig = builderConfig;
        this.terser = terser;
    }

    /**
     *
     * @return @throws HL7Exception
     * @throws HL7Exception
     */
    public Subject buildSubject() throws HL7Exception {
        // Build Subject.
        Subject subject = new Subject();

        // SEGMENT: PID
        PID pid = (PID) terser.getSegment("/.PID");

        // Identifiers.
        CX[] patientIdentfierListCX = pid.getPatientIdentifierList();
        for (int i = 0; i < patientIdentfierListCX.length; i++) {
            // FIXME?: Should we just pick the first one?  According to PIX v2, only the first
            // Patient id is used.  We will build the list for now.
            CX patientIdentifierCX = patientIdentfierListCX[i];
            String patientIdentifierCXFormatted = patientIdentifierCX.encode();
            logger.info("patientIdentifierCXFormatted = " + patientIdentifierCXFormatted);
            if (!HL7FormatUtil.isCX_Formatted_WithExtraComponents(patientIdentifierCXFormatted)) {
                throw new HL7Exception("Invalid CX format for patient identifier");
            }
            SubjectIdentifier subjectIdentifier = new SubjectIdentifier(patientIdentifierCXFormatted);
            subject.addSubjectIdentifier(subjectIdentifier);
        }

        // Other identifiers (SSN, account number, etc).

        // SSN:
        String ssn = BuilderHelper.buildString(pid.getSSNNumberPatient());
        if (ssn != null) {
            logger.info("SSN = " + ssn);
            SubjectIdentifier ssnSubjectIdentifier = new SubjectIdentifier();
            ssnSubjectIdentifier.setIdentifier(ssn);
            ssnSubjectIdentifier.setIdentifierType(SubjectIdentifier.Type.OTHER);
            SubjectIdentifierDomain ssnIdentifierDomain = new SubjectIdentifierDomain();
            ssnIdentifierDomain.setUniversalId(SubjectIdentifierDomain.SSN_UNIVERSAL_ID);
            ssnSubjectIdentifier.setIdentifierDomain(ssnIdentifierDomain);
            subject.getSubjectOtherIdentifiers().add(ssnSubjectIdentifier);
        }

        // Account number:
        //  TODO - Account number ... problem with no identifier domain specified.
        CX patientAccountNumberCX = pid.getPatientAccountNumber();
        if (patientAccountNumberCX != null && patientAccountNumberCX.getID() != null && patientAccountNumberCX.getID().getValue() != null) {
            String patientAccountNumberCXFormatted = patientAccountNumberCX.encode();
            logger.info("patientAccountNumberCXFormatted = " + patientAccountNumberCXFormatted);

            // FIXME: What if account number is qualified?
            
            // Not qualified by an assigning authority ...
            SubjectIdentifier accountSubjectIdentifier = new SubjectIdentifier();
            accountSubjectIdentifier.setIdentifier(patientAccountNumberCXFormatted);
            accountSubjectIdentifier.setIdentifierType(SubjectIdentifier.Type.OTHER);
            SubjectIdentifierDomain identifierDomain = new SubjectIdentifierDomain();
            identifierDomain.setUniversalId(builderConfig.getDefaultAccountNumberUniversalId());
            accountSubjectIdentifier.setIdentifierDomain(identifierDomain);
            subject.getSubjectOtherIdentifiers().add(accountSubjectIdentifier);
        }

        // Driver's license number:
        DLN driversLicenseNumberDLN = pid.getDriverSLicenseNumberPatient();
        if (driversLicenseNumberDLN != null) {
            logger.info("driversLicenseNumberDLN = " + driversLicenseNumberDLN.encode());
        }
        // TBD - Driver's License Number

        // Coded values.
        subject.setGender(BuilderHelper.buildCodedValue(pid.getSex()));
        subject.setMaritalStatus(BuilderHelper.buildCodedValue(pid.getMaritalStatus()));

        // Birth time.
        subject.setBirthTime(pid.getDateTimeOfBirth().getTimeOfAnEvent().getValueAsDate());

        // Multiple birth order/indicator.
        subject.setMultipleBirthOrderNumber(BuilderHelper.buildInteger(pid.getBirthOrder()));
        subject.setMultipleBirthIndicator(BuilderHelper.buildBoolean(pid.getMultipleBirthIndicator())); // Y - Yes, N - No

        // More complex types ...

        // Name(s).
        XPN[] patientNamesXPN = pid.getPatientName();
        for (int i = 0; i < patientNamesXPN.length; i++) {
            XPN patientNameXPN = patientNamesXPN[i];
            logger.info("patientNameXPN = " + patientNameXPN.encode());
            SubjectName subjectName = BuilderHelper.buildSubjectName(patientNameXPN);
            subject.addSubjectName(subjectName);
        }

        // Address(es).
        XAD[] patientAddressesXAD = pid.getPatientAddress();
        for (int i = 0; i < patientAddressesXAD.length; i++) {
            XAD patientAddressXAD = patientAddressesXAD[i];
            logger.info("patientAddressXAD = " + patientAddressXAD.encode());
            Address subjectAddress = new Address();
            subjectAddress.setStreetAddressLine1(patientAddressXAD.getStreetAddress().getValue());
            // FIXME: Multiple address lines?
            //subjectAddress.setStreetAddressLine2(streetAddressLine2);
            //subjectAddress.setStreetAddressLine3(streetAddressLine3);
            subjectAddress.setCity(patientAddressXAD.getCity().getValue());
            subjectAddress.setState(patientAddressXAD.getStateOrProvince().getValue());
            subjectAddress.setPostalCode(patientAddressXAD.getZipOrPostalCode().getValue());
            subjectAddress.setCountry(patientAddressXAD.getCountry().getValue());
            subjectAddress.setUse(patientAddressXAD.getAddressType().getValue());
            subject.addAddress(subjectAddress);
        }

        // Telecom addresses [Home]
        XTN[] homePhoneNumbersXTN = pid.getPhoneNumberHome();
        // FIXME: Decompose into finer grain parts -- impact on HL7 v3 also
        for (int i = 0; i < homePhoneNumbersXTN.length; i++) {
            XTN homePhoneNumberXTN = homePhoneNumbersXTN[i];
            logger.info("homePhoneNumberXTN = " + homePhoneNumberXTN.encode());
            TelecomAddress subjectTelecomAddress = new TelecomAddress();
            subjectTelecomAddress.setUse(homePhoneNumberXTN.getTelecommunicationUseCode().getValue());
            subjectTelecomAddress.setValue(homePhoneNumberXTN.getPhoneNumber().getValue());
            subject.addTelecomAddress(subjectTelecomAddress);
        }

        // Telecom addresses [Business]:
        // FIXME: Decompose into finer grain parts -- impact on HL7 v3 also
        XTN[] bizPhoneNumbersXTN = pid.getPhoneNumberBusiness();
        for (int i = 0; i < bizPhoneNumbersXTN.length; i++) {
            XTN bizPhoneNumberXTN = bizPhoneNumbersXTN[i];
            logger.info("bizPhoneNumberXTN = " + bizPhoneNumberXTN.encode());
            TelecomAddress subjectTelecomAddress = new TelecomAddress();
            subjectTelecomAddress.setUse(bizPhoneNumberXTN.getTelecommunicationUseCode().getValue());
            subjectTelecomAddress.setValue(bizPhoneNumberXTN.getPhoneNumber().getValue());
            subject.addTelecomAddress(subjectTelecomAddress);
        }

        // Ethnic group:
        if (pid.getEthnicGroupReps() > 0) {
            CE ethnicGroupCE = pid.getEthnicGroup(0);  // FIXME: ?We only grab the first ethnic group
            if (ethnicGroupCE != null) {
                logger.info("ethnicGroupCE = " + ethnicGroupCE.encode());
            }
            subject.setEthnicGroup(BuilderHelper.buildCodedValue(ethnicGroupCE));
        }

        // Race:
        if (pid.getRaceReps() > 0) {
            CE raceCE = pid.getRace(0); // FIXME: ?We only grab the first race
            if (raceCE != null) {
                logger.info("raceCE = " + raceCE.encode());
            }
            subject.setRace(BuilderHelper.buildCodedValue(raceCE));
        }

        // Religion:
        CE religionCE = pid.getReligion();
        if (religionCE != null) {
            logger.info("religionCE = " + religionCE.encode());
        }
        subject.setReligiousAffiliation(BuilderHelper.buildCodedValue(religionCE));

        // Mother's maiden name:
        // TODO - Mother's maiden name

        // Deceased indicator/time:
        subject.setDeceasedIndicator(BuilderHelper.buildBoolean(pid.getPatientDeathIndicator())); // Y - Yes, N - No
        if (subject.getDeceasedIndicator() != null) {
            if (pid.getPatientDeathDateAndTime() != null && pid.getPatientDeathDateAndTime().getTimeOfAnEvent() != null) {
                logger.info("Deceased Time = " + pid.getPatientDeathDateAndTime().getTimeOfAnEvent().getValueAsDate());
                subject.setDeceasedTime(pid.getPatientDeathDateAndTime().getTimeOfAnEvent().getValueAsDate());
            }
        }

        // Citizenships:
        CE[] citizenshipsCE = pid.getCitizenship();
        for (int i = 0; i < citizenshipsCE.length; i++) {
            CE citizenshipCE = citizenshipsCE[i];
            logger.info("citizenshipCE = " + citizenshipCE.encode());
            SubjectCitizenship subjectCitizenship = new SubjectCitizenship();
            subjectCitizenship.setNationCode(BuilderHelper.buildCodedValue(citizenshipCE));
            subjectCitizenship.setNationName(citizenshipCE.getName());
            subject.addSubjectCitizenship(subjectCitizenship);
        }

        // Language:
        // TODO - Language

        // Mother's maiden name:
        //XPN[] mothersMaidenNameXPN[] = pid.getMotherSMaidenName();
        // FIXME - just use one maiden name.
        if (pid.getMotherSMaidenNameReps() > 0) {
            XPN mothersMaidenNameXPN = pid.getMotherSMaidenName(0);
            logger.info("Mothers Maiden Name XPN = " + mothersMaidenNameXPN.encode());
            // FIXME: Simplify storing maiden names (@ root level of subject), etc.
            SubjectPersonalRelationship subjectPersonalRelationship = new SubjectPersonalRelationship();
            CodedValue relationshipTypeCode = new CodedValue();
            relationshipTypeCode.setCode("MTH");
            subjectPersonalRelationship.setRelationshipType(relationshipTypeCode);
            Subject relatedSubject = new Subject();
            SubjectName relatedSubjectName = BuilderHelper.buildSubjectName(mothersMaidenNameXPN);
            relatedSubject.addSubjectName(relatedSubjectName);
            subjectPersonalRelationship.setSubject(relatedSubject);
            subject.getSubjectPersonalRelationships().add(subjectPersonalRelationship);
        }

        return subject;
    }
}
