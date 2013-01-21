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
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.QPD;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.subjectmodel.Address;
import com.vangent.hieos.subjectmodel.CodedValue;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.subjectmodel.SubjectName;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectSearchCriteriaBuilder {

    private static final Logger logger = Logger.getLogger(SubjectSearchCriteriaBuilder.class);
    private Terser terser;

    /**
     *
     * @param terser
     */
    public SubjectSearchCriteriaBuilder(Terser terser) {
        this.terser = terser;
    }

    /**
     *
     * @return @throws HL7Exception
     * @throws HL7Exception
     */
    public SubjectSearchCriteria buildSubjectSearchCriteriaFromPIXQuery() throws HL7Exception {
        // Build SubjectSearchCriteria.
        SubjectSearchCriteria subjectSearchCriteria = new SubjectSearchCriteria();
        Subject subject = new Subject();
        subjectSearchCriteria.setSubject(subject);

        // Pull fields from QPD segment.
        QPD qpd = (QPD) terser.getSegment("/QPD");

        // Patient id.
        Type[] patientIdTypes = qpd.getField(3);
        if (patientIdTypes.length == 0) {
            throw new HL7Exception("No patient identifier supplied");
        }
        String patientIdCXFormatted = patientIdTypes[0].encode(); // Only require first one for PIX query.
        SubjectIdentifier subjectIdentifier = new SubjectIdentifier(patientIdCXFormatted);
        subject.addSubjectIdentifier(subjectIdentifier);

        // Get list (repeating field 4) of assigning authorities of interest - "What Domains Returned?"
        Type[] scopedAssigningAuthorityTypes = qpd.getField(4);
        int numScopedAssigningAuthorityTypes = scopedAssigningAuthorityTypes.length;
        for (int i = 0; i < numScopedAssigningAuthorityTypes; i++) {
            String assigningAuthorityCXformatted = scopedAssigningAuthorityTypes[i].encode();
            SubjectIdentifierDomain scopingAssigningAuthority = new SubjectIdentifierDomain(assigningAuthorityCXformatted);
            subjectSearchCriteria.addScopingSubjectIdentifierDomain(scopingAssigningAuthority);
        }
        return subjectSearchCriteria;
    }

    /**
     *
     * @return @throws HL7Exception 
     * @throws HL7Exception
     */
    public SubjectSearchCriteria buildSubjectSearchCriteriaFromPDQ() throws HL7Exception {
        // Build SubjectSearchCriteria.
        SubjectSearchCriteria subjectSearchCriteria = new SubjectSearchCriteria();
        Subject subject = new Subject();
        subjectSearchCriteria.setSubject(subject);

        // Get target of demographics query.
        MSH msh = (MSH) terser.getSegment("/MSH");
        String receivingApplication = msh.getReceivingApplication().encode();
        logger.info("Receiving Application = " + receivingApplication);

        // Pull fields from QPD segment.
        QPD qpd = (QPD) terser.getSegment("/QPD");

        // Get list (repeating field 8) of assigning authorities of interest - "What Domains Returned?"
        Type[] scopedAssigningAuthorityTypes = qpd.getField(8);
        int numScopedAssigningAuthorityTypes = scopedAssigningAuthorityTypes.length;
        for (int i = 0; i < numScopedAssigningAuthorityTypes; i++) {
            String assigningAuthorityCXformatted = scopedAssigningAuthorityTypes[i].encode();
            SubjectIdentifierDomain scopingAssigningAuthority = new SubjectIdentifierDomain(assigningAuthorityCXformatted);
            subjectSearchCriteria.addScopingSubjectIdentifierDomain(scopingAssigningAuthority);
        }

        // QIP - Demographic Fields (QPD-3).
        Type[] demographicFieldTypes = qpd.getField(3);  // QPD-3
        int numDemographicFieldTypes = demographicFieldTypes.length;
        SubjectName subjectName = new SubjectName();
        Address subjectAddress = new Address();
        SubjectIdentifier accountNumberSubjectIdentifier = new SubjectIdentifier();
        SubjectIdentifier ssnSubjectIdentifier = new SubjectIdentifier();
        ssnSubjectIdentifier.setIdentifierType(SubjectIdentifier.Type.OTHER);
        boolean subjectNameQuery = false;
        boolean subjectAddressQuery = false;
        boolean subjectAccountNumberQuery = false;
        boolean subjectSSNQuery = false;
        for (int i = 0; i < numDemographicFieldTypes; i++) {
            String demographicFieldText = demographicFieldTypes[i].encode();
            logger.info("demographicFieldText = " + demographicFieldText);
            String fieldQueryParts[] = demographicFieldText.split("\\^");
            if (fieldQueryParts.length == 2) {
                String fieldName = fieldQueryParts[0];
                String fieldValue = fieldQueryParts[1];
                // @PID.3.1 (PID)
                // @PID.5.1.1 (Family Name)
                // @PID.5.2 (Given Name)
                // @PID.7.1 (Birth Date)
                // @PID.8 (Gender)
                // @PID.11 (Address)
                // TODO: @PID.18 (Account Number)
                // TODO: @PID.19 (SSN)
                // TODO: @PID.20 (Driver's License)

                // FIXME: Deal with sub-components (e.g. account number CX components).

                if (fieldName.equalsIgnoreCase("@PID.3.1")) // Patient identifier
                {
                    SubjectIdentifier subjectIdentifier = new SubjectIdentifier(fieldValue);
                    if (subjectIdentifier.getIdentifierDomain().getUniversalId() == null
                            && subjectIdentifier.getIdentifierDomain().getNamespaceId() == null) {
                        // FIXME - HACK
                        // Establish default domain.
                        SubjectIdentifierDomain subjectIdentifierDomain = new SubjectIdentifierDomain();
                        subjectIdentifierDomain.setNamespaceId("NIST2010");
                        subjectIdentifier.setIdentifierDomain(subjectIdentifierDomain);
                    }
                    subject.addSubjectIdentifier(subjectIdentifier);
                } else if (fieldName.equalsIgnoreCase("@PID.5.1.1")) // Family Name
                {
                    subjectName.setFamilyName(fieldValue);
                    subjectNameQuery = true;
                } else if (fieldName.equalsIgnoreCase("@PID.5.2")) // Given Name
                {
                    subjectName.setGivenName(fieldValue);
                    subjectNameQuery = true;
                } else if (fieldName.equalsIgnoreCase("@PID.7.1")) // Birth Date
                {
                    subject.setBirthTime(Hl7Date.toDate(fieldValue));
                } else if (fieldName.equalsIgnoreCase("@PID.8")) // Gender
                {
                    CodedValue genderCode = new CodedValue();
                    genderCode.setCode(fieldValue);
                    subject.setGender(genderCode);
                } else if (fieldName.equalsIgnoreCase("@PID.11.1.1")) // Street address
                {
                    subjectAddress.setStreetAddressLine1(fieldValue);
                    subjectAddressQuery = true;
                } else if (fieldName.equalsIgnoreCase("@PID.11.3")) // City
                {
                    subjectAddress.setCity(fieldValue);
                    subjectAddressQuery = true;
                } else if (fieldName.equalsIgnoreCase("@PID.11.4")) // State
                {
                    subjectAddress.setState(fieldValue);
                    subjectAddressQuery = true;
                } else if (fieldName.equalsIgnoreCase("@PID.11.5")) // Postal code
                {
                    subjectAddress.setPostalCode(fieldValue);
                    subjectAddressQuery = true;
                } else if (fieldName.equalsIgnoreCase("@PID.18.1")) { // Account number.
                    // TODO.
                    logger.info("Account number = " + fieldValue);
                    accountNumberSubjectIdentifier.setIdentifier(fieldValue);
                    accountNumberSubjectIdentifier.setIdentifierType(SubjectIdentifier.Type.OTHER);
                    subjectAccountNumberQuery = true;

                } // TODO - Account number identifier domain components (@PID.18.4.x).
                else if (fieldName.equalsIgnoreCase("@PID.19")) // SSN.
                {
                    // TODO.
                    logger.info("SSN = " + fieldValue);
                    ssnSubjectIdentifier.setIdentifier(fieldValue);
                    SubjectIdentifierDomain ssnIdentifierDomain = new SubjectIdentifierDomain();
                    ssnIdentifierDomain.setUniversalId(SubjectIdentifierDomain.SSN_UNIVERSAL_ID);
                    ssnSubjectIdentifier.setIdentifierDomain(ssnIdentifierDomain);
                    subjectSSNQuery = true;
                }
                // TODO: Add phone, etc.
                //else if (fieldName.equalsIgnoreCase("@PID.11.6")) {
                //    subjectAddress.setCountry(fieldValue);
                //}
            } else {
                // FIXME: Error ...
            }


        }
        if (subjectNameQuery) {
            subject.addSubjectName(subjectName);
        }
        if (subjectAddressQuery) {
            subject.addAddress(subjectAddress);
        }
        if (subjectAccountNumberQuery) {
            subject.getSubjectOtherIdentifiers().add(accountNumberSubjectIdentifier);
        }
        if (subjectSSNQuery) {
            subject.getSubjectOtherIdentifiers().add(ssnSubjectIdentifier);
        }
        return subjectSearchCriteria;
    }
}
