/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.persistence;

import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.Subject.SubjectType;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.empi.exception.EMPIException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author thumbe
 */
public class SubjectDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(SubjectDAO.class);

    /**
     * 
     * @param connection
     */
    public SubjectDAO(Connection connection) {
        super(connection);
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public Subject load(String subjectId) throws EMPIException {
        Subject subject = new Subject();

        // Load the subject.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int genderCodeId;
        int maritalStatusCodeId;
        int religiousAffiliationCodeId;
        int raceCodeId;
        int ethnicGroupCodeId;
        try {
            String sql = "SELECT id,type,birth_time,gender_code_id,deceased_indicator,deceased_time,multiple_birth_indicator,multiple_birth_order_number,marital_status_code_id,religious_affiliation_code_id,race_code_id,ethnic_group_code_id FROM subject WHERE id=?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, subjectId);
            // Execute query.
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new EMPIException("No subject found for uniqueid = " + subjectId);
            } else {
                subject.setInternalId(subjectId);
                subject.setType(this.getSubjectType(rs.getString(2)));
                subject.setBirthTime(this.getDate(rs, 3));
                genderCodeId = rs.getInt(4);
                subject.setDeceasedIndicator(this.getBoolean(rs, 5));
                subject.setDeceasedTime(this.getDate(rs, 6));
                subject.setMultipleBirthIndicator(this.getBoolean(rs, 7));
                subject.setMultipleBirthOrderNumber(this.getInteger(rs, 8));
                maritalStatusCodeId = rs.getInt(9);
                religiousAffiliationCodeId = rs.getInt(10);
                raceCodeId = rs.getInt(11);
                ethnicGroupCodeId = rs.getInt(12);
            }
        } catch (SQLException ex) {
            throw new EMPIException("Failure reading Subject from database" + ex.getMessage());
        } finally {
            this.close(stmt);
            this.close(rs);
        }

        // Now, load composed objects.
        Connection conn = this.getConnection();

        // Coded values.
        CodeDAO codeDAO = new CodeDAO(conn);
        subject.setGender(codeDAO.load(genderCodeId, CodeDAO.CodeType.GENDER));
        subject.setMaritalStatus(codeDAO.load(maritalStatusCodeId, CodeDAO.CodeType.MARITAL_STATUS));
        subject.setReligiousAffiliation(codeDAO.load(religiousAffiliationCodeId, CodeDAO.CodeType.RELIGIOUS_AFFILIATION));
        subject.setRace(codeDAO.load(raceCodeId, CodeDAO.CodeType.RACE));
        subject.setEthnicGroup(codeDAO.load(ethnicGroupCodeId, CodeDAO.CodeType.ETHNIC_GROUP));

        // Names.
        SubjectNameDAO subjectNameDAO = new SubjectNameDAO(conn);
        subjectNameDAO.load(subject);

        // Addresses.
        SubjectAddressDAO subjectAddressDAO = new SubjectAddressDAO(conn);
        subjectAddressDAO.load(subject);

        // Telecom addresses.
        SubjectTelecomAddressDAO subjectTelecomAddressDAO = new SubjectTelecomAddressDAO(conn);
        subjectTelecomAddressDAO.load(subject);

        // Identifiers.
        SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(conn);
        List<SubjectIdentifier> subjectIdentifiers = subjectIdentifierDAO.load(subject);
        subject.setSubjectIdentifiers(subjectIdentifiers);

        // Other identifiers.
        SubjectOtherIdentifierDAO subjectOtherIdentifierDAO = new SubjectOtherIdentifierDAO(conn);
        List<SubjectIdentifier> subjectOtherIdentifiers = subjectOtherIdentifierDAO.load(subject);
        subject.setSubjectOtherIdentifiers(subjectOtherIdentifiers);

        return subject;
    }

    /**
     *
     * @param subjectIdentifier
     * @return
     * @throws EMPIException
     */
    public Subject loadBaseSubjectByIdentifier(SubjectIdentifier subjectIdentifier) throws EMPIException {
        SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(this.getConnection());

        // First find the subjectId given the subject identifier.
        String subjectId = subjectIdentifierDAO.getSubjectId(subjectIdentifier);
        Subject subject = null;
        if (subjectId != null) {
            subject = this.loadBaseSubject(subjectId);
        }
        return subject;
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public Subject loadBaseSubject(String subjectId) throws EMPIException {
        Subject subject = null;

        // Load the subject.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT id,type FROM subject WHERE id=?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, subjectId);
            // Execute query.
            rs = stmt.executeQuery();
            if (rs.next()) {
                subject = new Subject();
                subject.setInternalId(subjectId);
                subject.setType(this.getSubjectType(rs.getString(2)));
            }
        } catch (SQLException ex) {
            throw new EMPIException("Failure reading Subject from database" + ex.getMessage());
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return subject;
    }

    /**
     * 
     * @param subjectIdentifiers
     * @return
     * @throws EMPIException
     */
    public boolean doesSubjectExist(List<SubjectIdentifier> subjectIdentifiers) throws EMPIException {
        boolean subjectExists = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(this.getConnection());
            // Go through each identifier (assumes no duplicates across subjects).
            for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
                // See if the identifier has a subject.
                String subjectId = subjectIdentifierDAO.getSubjectId(subjectIdentifier);
                if (subjectId != null) {
                    subjectExists = true;
                    break;
                }
            }
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return subjectExists;
    }

    /**
     *
     * @param subjects
     * @throws EMPIException
     */
    public void insert(List<Subject> subjects) throws EMPIException {
        PreparedStatement stmt = null;
        Connection conn = this.getConnection();
        try {
            String sql = "INSERT INTO subject(id,type,birth_time,gender_code_id,deceased_indicator,deceased_time,multiple_birth_indicator,multiple_birth_order_number,marital_status_code_id,religious_affiliation_code_id,race_code_id,ethnic_group_code_id) values(?,?,?,?,?,?,?,?,?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            CodeDAO codeDAO = new CodeDAO(conn);
            for (Subject subject : subjects) {
                String subjectTypeValue = this.getSubjectTypeValue(subject);
                subject.setInternalId(PersistenceHelper.getUUID());
                stmt.setString(1, subject.getInternalId());
                stmt.setString(2, subjectTypeValue);
                this.setDate(stmt, 3, subject.getBirthTime());
                this.setCodedValueId(codeDAO, CodeDAO.CodeType.GENDER, stmt, 4, subject.getGender());
                this.setBoolean(stmt, 5, subject.getDeceasedIndicator());
                this.setDate(stmt, 6, subject.getDeceasedTime());
                this.setBoolean(stmt, 7, subject.getMultipleBirthIndicator());
                this.setInteger(stmt, 8, subject.getMultipleBirthOrderNumber());
                this.setCodedValueId(codeDAO, CodeDAO.CodeType.MARITAL_STATUS, stmt, 9, subject.getMaritalStatus());
                this.setCodedValueId(codeDAO, CodeDAO.CodeType.RELIGIOUS_AFFILIATION, stmt, 10, subject.getReligiousAffiliation());
                this.setCodedValueId(codeDAO, CodeDAO.CodeType.RACE, stmt, 11, subject.getRace());
                this.setCodedValueId(codeDAO, CodeDAO.CodeType.ETHNIC_GROUP, stmt, 12, subject.getEthnicGroup());
                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }

            // FIXME: Anyway to batch sub components????

            // Now, insert composed objects.
            SubjectNameDAO subjectNameDAO = new SubjectNameDAO(conn);
            SubjectAddressDAO subjectAddressDAO = new SubjectAddressDAO(conn);
            SubjectTelecomAddressDAO subjectTelecomAddressDAO = new SubjectTelecomAddressDAO(conn);
            SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(conn);
            SubjectOtherIdentifierDAO subjectOtherIdentifierDAO = new SubjectOtherIdentifierDAO(conn);
            for (Subject subject : subjects) {
                subjectNameDAO.insert(subject.getSubjectNames(), subject);
                subjectAddressDAO.insert(subject.getAddresses(), subject);
                subjectTelecomAddressDAO.insert(subject.getTelecomAddresses(), subject);
                subjectIdentifierDAO.insert(subject.getSubjectIdentifiers(), subject);
                subjectOtherIdentifierDAO.insert(subject.getSubjectOtherIdentifiers(), subject);
            }
        } catch (SQLException ex) {
            throw new EMPIException(ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     * 
     * @param enterpriseSubjectId
     * @throws EMPIException
     */
    /*
    public void voidEnterpriseSubject(String enterpriseSubjectId) throws EMPIException {
        Connection conn = this.getConnection();  // Get connection to use.

        // Mark enterprise subject as "Voided"
        this.voidSubject(enterpriseSubjectId);

        // Delete "SubjectMatch" record for enterpriseSubjectId.
        SubjectMatchFieldsDAO subjectMatchCriteriaDAO = new SubjectMatchFieldsDAO(conn);
        subjectMatchCriteriaDAO.deleteSubjectRecords(enterpriseSubjectId);

        // Delete all cross references to the enterprise subject.
        SubjectCrossReferenceDAO subjectCrossReferenceDAO = new SubjectCrossReferenceDAO(conn);
        subjectCrossReferenceDAO.deleteEnterpriseSubjectCrossReferences(enterpriseSubjectId);
    }*/

    /**
     * 
     * @param survivingEnterpriseSubjectId
     * @param subsumedEnterpriseSubjectId
     * @throws EMPIException
     */
    public void mergeEnterpriseSubjects(String survivingEnterpriseSubjectId, String subsumedEnterpriseSubjectId) throws EMPIException {

        // Only perform merge if the ids are different.
        // Guard is here just in case higher-level logic does not account for this case.
        if (!survivingEnterpriseSubjectId.equals(subsumedEnterpriseSubjectId)) {
            Connection conn = this.getConnection();  // Get connection to use.

            // Delete "SubjectMatchFieldsDAO" record for subsumedEnterpriseSubjectId.
            //SubjectMatchFieldsDAO subjectMatchFieldsDAO = new SubjectMatchFieldsDAO(conn);
            //subjectMatchFieldsDAO.deleteSubjectRecords(subsumedEnterpriseSubjectId);

            // Move cross references from subsumedEnterpriseSubjectId to survivingEnterpriseSubjectId
            SubjectCrossReferenceDAO subjectCrossReferenceDAO = new SubjectCrossReferenceDAO(conn);
            subjectCrossReferenceDAO.mergeEnterpriseSubjects(survivingEnterpriseSubjectId, subsumedEnterpriseSubjectId);

            // Delete the subsumed enterprise subject.
            this.deleteSubject(subsumedEnterpriseSubjectId, Subject.SubjectType.ENTERPRISE);
        }
    }

    /**
     *
     * @param subjectId
     * @param subjectType
     * @throws EMPIException
     */
    public void deleteSubject(String subjectId, Subject.SubjectType subjectType) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            Connection conn = this.getConnection();  // Get connection to use.

            // First delete component parts.

            // Get DAO instances responsible for deletions.
            SubjectNameDAO subjectNameDAO = new SubjectNameDAO(conn);
            SubjectAddressDAO subjectAddressDAO = new SubjectAddressDAO(conn);
            SubjectTelecomAddressDAO subjectTelecomAddressDAO = new SubjectTelecomAddressDAO(conn);
            SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(conn);
            SubjectOtherIdentifierDAO subjectOtherIdentifierDAO = new SubjectOtherIdentifierDAO(conn);
            SubjectCrossReferenceDAO subjectCrossReferenceDAO = new SubjectCrossReferenceDAO(conn);

            // Run deletions.
            subjectNameDAO.deleteSubjectRecords(subjectId);
            subjectAddressDAO.deleteSubjectRecords(subjectId);
            subjectTelecomAddressDAO.deleteSubjectRecords(subjectId);
            subjectIdentifierDAO.deleteSubjectRecords(subjectId);
            subjectOtherIdentifierDAO.deleteSubjectRecords(subjectId);
            subjectCrossReferenceDAO.deleteSubjectCrossReferences(subjectId, subjectType);

            if (subjectType.equals(SubjectType.SYSTEM)) {
                // Delete subject match record.
                SubjectMatchFieldsDAO subjectMatchFieldsDAO = new SubjectMatchFieldsDAO(conn);
                subjectMatchFieldsDAO.deleteSubjectRecords(subjectId);
            }

            // Now, delete the subject record.
            this.deleteRecords(subjectId, "subject", "id", this.getClass().getName());

        } finally {
            this.close(stmt);
        }
    }

    /**
     * NOTE: Could have built a full enumeration, but decided to be overkill.
     *
     * @param subjectType
     * @return
     */
    private String getSubjectTypeValue(Subject subject) {
        return this.getSubjectTypeValue(subject.getType());
    }

    /**
     * 
     * @param subjectType
     * @return
     */
    private String getSubjectTypeValue(Subject.SubjectType subjectType) {
        String value = "";
        switch (subjectType) {
            case ENTERPRISE:
                value = "E";
                break;
            case SYSTEM:
                value = "S";
                break;
            default:
                value = "V";
                break;
        }
        return value;
    }

    /**
     * NOTE: Could have built a full enumeration, but decided to be overkill.
     *
     * @param type
     * @return
     */
    private Subject.SubjectType getSubjectType(String type) {
        Subject.SubjectType subjectType = SubjectType.ENTERPRISE;
        if (type.equalsIgnoreCase("E")) {
            subjectType = SubjectType.ENTERPRISE;
        } else if (type.equalsIgnoreCase("S")) {
            subjectType = SubjectType.SYSTEM;
        } else {
            subjectType = SubjectType.VOIDED;
        }
        return subjectType;
    }
}
