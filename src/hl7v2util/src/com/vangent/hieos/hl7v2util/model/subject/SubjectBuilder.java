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
import ca.uhn.hl7v2.model.v231.datatype.CX;
import ca.uhn.hl7v2.model.v231.datatype.XAD;
import ca.uhn.hl7v2.model.v231.datatype.XPN;
import ca.uhn.hl7v2.model.v231.segment.PID;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.subjectmodel.Address;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectName;
import com.vangent.hieos.xutil.hl7.formatutil.HL7FormatUtil;
import com.vangent.hl7v2util.model.builder.BuilderHelper;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectBuilder {

    private Terser terser;

    /**
     *
     * @param terser
     */
    public SubjectBuilder(Terser terser) {
        this.terser = terser;
    }

    /**
     * 
     * @return
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
            if (!HL7FormatUtil.isCX_Formatted(patientIdentifierCXFormatted)) {
                throw new HL7Exception("Invalid CX format for patient identifier");
            }
            SubjectIdentifier subjectIdentifier = new SubjectIdentifier(patientIdentifierCXFormatted);
            subject.addSubjectIdentifier(subjectIdentifier);
        }

        // Other identifiers (SSN, account number, etc).
        // TBD

        // Coded values.
        subject.setGender(BuilderHelper.buildCodedValue(pid.getSex()));
        subject.setMaritalStatus(BuilderHelper.buildCodedValue(pid.getMaritalStatus()));

        // Birth time.
        subject.setBirthTime(pid.getDateTimeOfBirth().getTimeOfAnEvent().getValueAsDate());


        // Name(s).
        XPN[] patientNamesXPN = pid.getPatientName();
        for (int i = 0; i < patientNamesXPN.length; i++) {
            XPN patientNameXPN = patientNamesXPN[i];
            SubjectName subjectName = new SubjectName();
            subjectName.setGivenName(patientNameXPN.getGivenName().getValue());
            subjectName.setFamilyName(patientNameXPN.getFamilyLastName().getFamilyName().getValue());
            subjectName.setMiddleName(patientNameXPN.getMiddleInitialOrName().getValue());
            subjectName.setPrefix(patientNameXPN.getPrefixEgDR().getValue());
            subjectName.setSuffix(patientNameXPN.getSuffixEgJRorIII().getValue());
            subject.addSubjectName(subjectName);
        }

        // Address(es).
        XAD[] patientAddressesXAD = pid.getPatientAddress();
        for (int i = 0; i < patientAddressesXAD.length; i++) {
            XAD patientAddressXAD = patientAddressesXAD[i];
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

        // Multiple birth order/indicator.
        subject.setMultipleBirthOrderNumber(BuilderHelper.buildInteger(pid.getBirthOrder()));
        // Y - Yes, N - No
        subject.setMultipleBirthIndicator(BuilderHelper.buildBoolean(pid.getMultipleBirthIndicator()));
        return subject;
    }
}
