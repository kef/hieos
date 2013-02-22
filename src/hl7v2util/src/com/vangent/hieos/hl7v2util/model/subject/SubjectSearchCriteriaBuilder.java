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
import ca.uhn.hl7v2.model.v25.datatype.CQ;
import ca.uhn.hl7v2.model.v25.datatype.NM;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.segment.DSC;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.QPD;
import ca.uhn.hl7v2.model.v25.segment.RCP;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.hl7v2util.model.builder.BuilderConfig;
import com.vangent.hieos.subjectmodel.Address;
import com.vangent.hieos.subjectmodel.CodedValue;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.subjectmodel.SubjectName;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectSearchCriteriaBuilder {

    private static final Logger logger = Logger.getLogger(SubjectSearchCriteriaBuilder.class);
    private BuilderConfig builderConfig;
    private Terser terser;

    /**
     *
     * @param builderConfig
     * @param terser
     */
    public SubjectSearchCriteriaBuilder(BuilderConfig builderConfig, Terser terser) {
        this.builderConfig = builderConfig;
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
     * @param defaultTargetDomainUniversalId
     * @return
     * @throws HL7Exception
     */
    public SubjectSearchCriteria buildSubjectSearchCriteriaFromPDQ(String defaultTargetDomainUniversalId) throws HL7Exception {
        // Build SubjectSearchCriteria.
        SubjectSearchCriteria subjectSearchCriteria = new SubjectSearchCriteria();
        Subject subject = new Subject();
        subjectSearchCriteria.setSubject(subject);

        // Get target of demographics query.
        MSH msh = (MSH) terser.getSegment("/MSH");
        String receivingApplication = msh.getReceivingApplication().encode();
        logger.info("Receiving Application = " + receivingApplication);

        // Continuation handling ...

        // SEGMENT: DSC (Omitted on first request).
        DSC dsc = (DSC) terser.getSegment("/DSC");
        if (dsc != null) {
            ST continuationPointerST = dsc.getContinuationPointer();
            if (continuationPointerST != null && continuationPointerST.getValue() != null) {
                String continuationPointer = continuationPointerST.getValue();
                logger.info("continuationPointer = " + continuationPointer);
                subjectSearchCriteria.setContinuationPointerId(continuationPointer);
                return subjectSearchCriteria;  // Early exit!
            }
        }

        // SEGMENT: RCP
        RCP rcp = (RCP) terser.getSegment("/RCP");
        if (rcp != null) {
            //ID queryPriorityID = rcp.getQueryPriority();
            CQ quantityLimitedRequestCQ = rcp.getQuantityLimitedRequest();
            NM numberOfIncrementsNM = quantityLimitedRequestCQ.getQuantity();
            String numberOfIncrementsAsString = numberOfIncrementsNM.getValue();
            if (numberOfIncrementsAsString != null) {
                int incrementQuantity = new Integer(numberOfIncrementsAsString);
                logger.info("incrementQuantity = " + incrementQuantity);
                subjectSearchCriteria.setIncrementQuantity(incrementQuantity);
            }
        }

        // Pull fields from QPD segment.
        QPD qpd = (QPD) terser.getSegment("/QPD");

        // Get query tag.
        ST queryTagST = qpd.getQueryTag();
        if (queryTagST == null || queryTagST.getValue() == null) {
            throw new HL7Exception("No query tag specified");
        }
        subjectSearchCriteria.setQueryId(queryTagST.getValue());

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

        // Patient identifier.
        SubjectIdentifier patientIdentifier = new SubjectIdentifier();
        SubjectIdentifierDomain patientIdentifierDomain = new SubjectIdentifierDomain();
        patientIdentifier.setIdentifierDomain(patientIdentifierDomain);
        boolean patientIdentifierQuery = false;
        boolean patientIdentifierIsQualified = false;

        // Name.
        SubjectName subjectName = new SubjectName();
        boolean subjectNameQuery = false;

        // Address.
        Address subjectAddress = new Address();
        boolean subjectAddressQuery = false;

        // Account number.
        SubjectIdentifier accountNumberIdentifier = new SubjectIdentifier();
        boolean subjectAccountNumberQuery = false;

        // SSN.
        SubjectIdentifier ssnIdentifier = new SubjectIdentifier();
        ssnIdentifier.setIdentifierType(SubjectIdentifier.Type.OTHER);
        boolean subjectSSNQuery = false;

        // Loop through query parameters.
        for (int i = 0; i < numDemographicFieldTypes; i++) {
            String demographicFieldText = demographicFieldTypes[i].encode();
            logger.info("demographicFieldText = " + demographicFieldText);
            String fieldQueryParts[] = demographicFieldText.split("\\^");
            if (fieldQueryParts.length == 2) {
                String fieldName = fieldQueryParts[0];
                String fieldValue = fieldQueryParts[1];
                // TODO: @PID.18 (Account Number) sub-components.
                // TODO: @PID.20 (Driver's License)

                // FIXME: Deal with sub-components (e.g. account number CX components).

                /* Patient identifier */
                if (fieldName.equalsIgnoreCase("@PID.3.1")) {
                    patientIdentifier.setIdentifier(fieldValue);
                    patientIdentifierQuery = true;

                    /* Assigning authority (w/ only Namespace id). */
                } else if (fieldName.equalsIgnoreCase("@PID.3.4")) {
                    patientIdentifierDomain.setNamespaceId(fieldValue);
                    patientIdentifierIsQualified = true;

                    /* Namespace id */
                } else if (fieldName.equalsIgnoreCase("@PID.3.4.1")) {
                    patientIdentifierDomain.setNamespaceId(fieldValue);
                    patientIdentifierIsQualified = true;

                    /* Universal id */
                } else if (fieldName.equalsIgnoreCase("@PID.3.4.2")) {
                    patientIdentifierDomain.setUniversalId(fieldValue);
                    patientIdentifierIsQualified = true;

                    /* Universal id type */
                } else if (fieldName.equalsIgnoreCase("@PID.3.4.3")) {
                    patientIdentifierDomain.setUniversalIdType(fieldValue);

                    /* Identifier type code */
                } else if (fieldName.equalsIgnoreCase("@PID.3.5")) {
                    // TODO: not implemented

                    /* Assigning facility */
                } else if (fieldName.equalsIgnoreCase("@PID.3.6")) {
                    // TODO: not implemented

                    /* Family name */
                } else if (fieldName.equalsIgnoreCase("@PID.5.1.1")) {
                    subjectName.setFamilyName(fieldValue);
                    subjectNameQuery = true;

                    /* Given name */
                } else if (fieldName.equalsIgnoreCase("@PID.5.2")) {
                    subjectName.setGivenName(fieldValue);
                    subjectNameQuery = true;

                    /* Birth date */
                } else if (fieldName.equalsIgnoreCase("@PID.7.1")) {
                    subject.setBirthTime(Hl7Date.toDate(fieldValue));

                    /* Gender */
                } else if (fieldName.equalsIgnoreCase("@PID.8")) {
                    CodedValue genderCode = new CodedValue();
                    genderCode.setCode(fieldValue);
                    subject.setGender(genderCode);

                    /* Street address */
                } else if (fieldName.equalsIgnoreCase("@PID.11.1.1")) {
                    subjectAddress.setStreetAddressLine1(fieldValue);
                    subjectAddressQuery = true;

                    /* City */
                } else if (fieldName.equalsIgnoreCase("@PID.11.3")) {
                    subjectAddress.setCity(fieldValue);
                    subjectAddressQuery = true;

                    /* State */
                } else if (fieldName.equalsIgnoreCase("@PID.11.4")) {
                    subjectAddress.setState(fieldValue);
                    subjectAddressQuery = true;

                    /* Postal code */
                } else if (fieldName.equalsIgnoreCase("@PID.11.5")) {
                    subjectAddress.setPostalCode(fieldValue);
                    subjectAddressQuery = true;

                    /* Account number */
                } else if (fieldName.equalsIgnoreCase("@PID.18.1")) {
                    // TODO.
                    logger.info("Account number = " + fieldValue);

                    // FIXME: What if account number is qualified?
                    // Not qualified by an assigning authority ...
                    accountNumberIdentifier.setIdentifier(fieldValue);
                    accountNumberIdentifier.setIdentifierType(SubjectIdentifier.Type.OTHER);
                    SubjectIdentifierDomain identifierDomain = new SubjectIdentifierDomain();
                    identifierDomain.setUniversalId(builderConfig.getDefaultAccountNumberUniversalId());
                    accountNumberIdentifier.setIdentifierDomain(identifierDomain);
                    subjectAccountNumberQuery = true;

                } /* SSN */ else if (fieldName.equalsIgnoreCase("@PID.19")) {
                    logger.info("SSN = " + fieldValue);
                    ssnIdentifier.setIdentifier(fieldValue);
                    SubjectIdentifierDomain ssnIdentifierDomain = new SubjectIdentifierDomain();
                    ssnIdentifierDomain.setUniversalId(SubjectIdentifierDomain.SSN_UNIVERSAL_ID);
                    ssnIdentifier.setIdentifierDomain(ssnIdentifierDomain);
                    subjectSSNQuery = true;
                }

            } else {
                // FIXME: Error ...
            }
        }
        if (patientIdentifierQuery) {
            if (!patientIdentifierIsQualified) {

                // See if we have a default target domain (for the receiver).
                if (defaultTargetDomainUniversalId != null) {
                    logger.info("Defaulting PID domain (from configuration) universal id = " + defaultTargetDomainUniversalId);
                    patientIdentifierDomain.setUniversalId(defaultTargetDomainUniversalId);
                } else {

                    // FIXME: Not ideal according to ITI Vol 2x Appendix M.

                    // See if domains are specified.  If only one specified, use that one.
                    List<SubjectIdentifierDomain> subjectIdentifierDomains = subjectSearchCriteria.getScopingSubjectIdentifierDomains();
                    if (subjectIdentifierDomains.size() == 1) {
                        SubjectIdentifierDomain defaultIdentifierDomain = subjectIdentifierDomains.get(0);
                        patientIdentifier.setIdentifierDomain(defaultIdentifierDomain);
                        logger.info("Defaulting PID domain (from QPD-8) universal id = " + defaultIdentifierDomain.getUniversalId());
                    } else {
                        throw new HL7Exception(
                                "No default patient identifier domain configured for receiver - must qualify patient identifier domain of interest in @PID.3.4, @PID.3.4.1 or @PID3.4.2");
                    }
                }
            }
            subject.getSubjectIdentifiers().add(patientIdentifier);
        }
        if (subjectNameQuery) {
            // Check for wild cards.
            String familyName = subjectName.getFamilyName();
            String givenName = subjectName.getGivenName();

            // FIXME: Currently, only deal with end of name wild cards
            if (familyName != null && familyName.endsWith("*")) {
                familyName = familyName.replace("*", "");
                subjectName.setFamilyName(familyName);
                subjectName.setFuzzySearchMode(true);
                logger.info("Fuzzy familyName match = " + familyName);
            }
            if (givenName != null && givenName.endsWith("*")) {
                givenName = familyName.replace("*", "");
                subjectName.setGivenName(givenName);
                subjectName.setFuzzySearchMode(true);
                logger.info("Fuzzy givenName match = " + givenName);
            }

            subject.addSubjectName(subjectName);
        }
        if (subjectAddressQuery) {
            subject.addAddress(subjectAddress);
        }
        if (subjectAccountNumberQuery) {
            subject.getSubjectIdentifiers().add(accountNumberIdentifier);
        }
        if (subjectSSNQuery) {
            subject.getSubjectIdentifiers().add(ssnIdentifier);
        }
        return subjectSearchCriteria;
    }
}
