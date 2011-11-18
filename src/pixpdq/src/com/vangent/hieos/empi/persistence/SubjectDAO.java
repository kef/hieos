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
import com.vangent.hieos.hl7v3util.model.subject.SubjectGender;
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
        try {
            String sql = "SELECT id,type,birth_time,gender_code_id FROM subject WHERE id=?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, subjectId);
            // Execute query.
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new EMPIException("No subject found for uniqueid = " + subjectId);
            } else {
                subject.setId(subjectId);
                subject.setType(this.getSubjectType(rs.getString(2)));
                subject.setBirthTime(rs.getDate(3));
                genderCodeId = rs.getInt(4);
            }
        } catch (SQLException ex) {
            throw new EMPIException("Failure reading subject from database" + ex.getMessage());
        } finally {
            this.close(stmt);
            this.close(rs);
        }

        // Now, load composed objects.
        Connection conn = this.getConnection();

        // Names.
        SubjectNameDAO subjectNameDAO = new SubjectNameDAO(conn);
        subjectNameDAO.load(subject);

        // Addresses.
        SubjectAddressDAO subjectAddressDAO = new SubjectAddressDAO(conn);
        subjectAddressDAO.load(subject);

        // Telecom addresses.
        SubjectTelecomAddressDAO subjectTelecomAddressDAO = new SubjectTelecomAddressDAO(conn);
        subjectTelecomAddressDAO.load(subject);

        // Gender.
        SubjectGenderDAO subjectGenderDAO = new SubjectGenderDAO(conn);
        SubjectGender subjectGender = subjectGenderDAO.load(genderCodeId);
        subject.setGender(subjectGender);

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
     * @param subjectIdentifiers
     * @return
     * @throws EMPIException
     */
    /*
    public List<Subject> findSubjectsByIdentifiers(List<SubjectIdentifier> subjectIdentifiers) throws EMPIException {
    List<Subject> subjects = new ArrayList<Subject>();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
    SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(this.getConnection());
    // Go through each identifier (assumes no duplicates across subjects).
    for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
    // See if the identifier has a subject.
    String subjectId = subjectIdentifierDAO.getSubjectId(subjectIdentifier);
    if (subjectId != null) {
    // Load the subject and add to list.
    Subject subject = this.load(subjectId);
    subjects.add(subject);
    }
    }
    } finally {
    this.close(stmt);
    this.close(rs);
    }
    return subjects;
    }*/
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
                subject.setId(subjectId);
                subject.setType(this.getSubjectType(rs.getString(2)));
            }
        } catch (SQLException ex) {
            throw new EMPIException("Failure reading subject from database" + ex.getMessage());
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
            String sql = "INSERT INTO subject(id,type,birth_time,gender_code_id) values(?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            SubjectGenderDAO subjectGenderDAO = new SubjectGenderDAO(conn);
            for (Subject subject : subjects) {
                String subjectTypeValue = this.getSubjectTypeValue(subject);
                subject.setId(PersistenceHelper.getUUID());
                stmt.setString(1, subject.getId());
                stmt.setString(2, subjectTypeValue);
                stmt.setDate(3, PersistenceHelper.getSQLDate(subject.getBirthTime()));

                // Get gender code id.
                SubjectGender subjectGender = subject.getGender();
                if (subjectGender != null) {
                    int genderCodeId = subjectGenderDAO.getId(subject.getGender().getCode());
                    stmt.setInt(4, genderCodeId);
                }
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
    public void voidEnterpriseSubject(String enterpriseSubjectId) throws EMPIException {
        Connection conn = this.getConnection();  // Get connection to use.

        // Mark enterprise subject as "Voided"
        this.markVoid(enterpriseSubjectId);

        // Delete "SubjectMatch" record for enterpriseSubjectId.
        SubjectMatchDAO subjectMatchDAO = new SubjectMatchDAO(conn);
        subjectMatchDAO.delete(enterpriseSubjectId);

        // Delete all cross references to the enterprise subject.
        SubjectCrossReferenceDAO subjectCrossReferenceDAO = new SubjectCrossReferenceDAO(conn);
        subjectCrossReferenceDAO.deleteEnterpriseSubjectCrossReferences(enterpriseSubjectId);
    }

    /**
     * 
     * @param survivingSubjectId
     * @param subsumedSubjectId
     * @throws EMPIException
     */
    public void merge(String survivingSubjectId, String subsumedSubjectId) throws EMPIException {
        Connection conn = this.getConnection();  // Get connection to use.

        // Mark subsumedSubjectId as "Voided"
        this.markVoid(subsumedSubjectId);

        // Delete "SubjectMatch" record for subsumedSubjectId.
        SubjectMatchDAO subjectMatchDAO = new SubjectMatchDAO(conn);
        subjectMatchDAO.delete(subsumedSubjectId);

        // Move cross references from subsumedSubjectId to survivingSubjectId
        SubjectCrossReferenceDAO subjectCrossReferenceDAO = new SubjectCrossReferenceDAO(conn);
        subjectCrossReferenceDAO.merge(survivingSubjectId, subsumedSubjectId);
    }

    /**
     * 
     * @param systemSubjectId
     * @throws EMPIException
     */
    public void deleteSystemSubject(String systemSubjectId) throws EMPIException {
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
            subjectNameDAO.deleteSubjectNames(systemSubjectId);
            subjectAddressDAO.deleteSubjectAddresses(systemSubjectId);
            subjectTelecomAddressDAO.deleteSubjectTelecomAddresses(systemSubjectId);
            subjectIdentifierDAO.deleteSubjectIdentifiers(systemSubjectId);
            subjectOtherIdentifierDAO.deleteSubjectIdentifiers(systemSubjectId);
            subjectCrossReferenceDAO.deleteSystemSubjectCrossReferences(systemSubjectId);

            String sql = "DELETE FROM subject WHERE id=?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, systemSubjectId);
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("Subject.delete: done executeBatch elapedTimeMillis=" + (endTime - startTime));
            }
        } catch (SQLException ex) {
            throw new EMPIException(ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param subjectId
     * @throws EMPIException
     */
    public void markVoid(String subjectId) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            String sql = "UPDATE subject SET type=? WHERE id=?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, this.getSubjectTypeValue(SubjectType.VOIDED));
            stmt.setString(2, subjectId);
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectDAO.markVoid: done executeBatch elapedTimeMillis=" + (endTime - startTime));
            }
        } catch (SQLException ex) {
            throw new EMPIException(ex);
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
