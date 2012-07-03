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
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.InternalId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectIdentifierDAO extends AbstractDAO {

    private final static Logger logger = Logger.getLogger(SubjectIdentifierDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectIdentifierDAO(Connection connection) {
        super(connection);
    }

    /**
     *
     * @param subjectIdentifier
     * @return
     * @throws EMPIException
     */
    public InternalId getSubjectId(SubjectIdentifier subjectIdentifier) throws EMPIException {
        InternalId internalId = null;
        // First, get the SubjectIdentifierDomainId
        SubjectIdentifierDomainDAO sidDAO = new SubjectIdentifierDomainDAO(this.getConnection());
        int subjectIdentifierDomainId = sidDAO.getId(subjectIdentifier.getIdentifierDomain());
        if (subjectIdentifierDomainId == -1) {
            // We have no knowledge of the identifier domain (so get out now).
            return internalId;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Now, see if we can locate the subject/identifier within the given identifier domain.
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT subject_id,seq_no FROM ")
                    .append(this.getTableName())
                    .append(" WHERE identifier=? AND subject_identifier_domain_id=? AND type=?");
            String sql = sb.toString();
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, subjectIdentifier.getIdentifier());
            stmt.setInt(2, subjectIdentifierDomainId);
            stmt.setString(3, SubjectIdentifierDAO.getSubjectIdentifierTypeValue(subjectIdentifier.getIdentifierType()));
            // Execute query.
            rs = stmt.executeQuery();
            if (rs.next()) {
                // Found.
                Long subjectId = rs.getLong(1);
                int seqNo = rs.getInt(2);
                internalId = new InternalId(subjectId, seqNo);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading subject identifiers", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return internalId;
    }

    /**
     * 
     * @param parentSubject
     * @param idType
     * @return
     * @throws EMPIException
     */
    public List<SubjectIdentifier> load(Subject parentSubject, SubjectIdentifier.Type idType) throws EMPIException {
        return this.load(parentSubject.getInternalId(), idType);
    }

    /**
     *
     * @param subjectId
     * @param idType
     * @return
     * @throws EMPIException
     */
    public List<SubjectIdentifier> load(InternalId subjectId, SubjectIdentifier.Type idType) throws EMPIException {
        List<SubjectIdentifier> subjectIdentifiers = new ArrayList<SubjectIdentifier>();
        // Load the subject names.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT seq_no,identifier,subject_identifier_domain_id FROM ").append(this.getTableName()).append(" WHERE subject_id=? AND type=?");
            String sql = sb.toString();
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setLong(1, subjectId.getId());
            stmt.setString(2, SubjectIdentifierDAO.getSubjectIdentifierTypeValue(idType));
            // Execute query.
            rs = stmt.executeQuery();
            SubjectIdentifierDomainDAO sidDAO = new SubjectIdentifierDomainDAO(this.getConnection());
            while (rs.next()) {
                SubjectIdentifier subjectIdentifier = new SubjectIdentifier();
                int seqNo = rs.getInt(1);
                InternalId internalId = new InternalId(subjectId.getId(), seqNo);
                subjectIdentifier.setInternalId(internalId);
                subjectIdentifier.setIdentifier(rs.getString(2));
                subjectIdentifier.setIdentifierType(idType);

                // Get SubjectIdentifierDomain
                int subjectIdentifierDomainId = rs.getInt(3);
                SubjectIdentifierDomain subjectIdentifierDomain = sidDAO.load(subjectIdentifierDomainId);
                subjectIdentifier.setIdentifierDomain(subjectIdentifierDomain);

                // Add SubjectIdentifier to list.
                subjectIdentifiers.add(subjectIdentifier);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading subject identifiers", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return subjectIdentifiers;
    }

    /**
     * 
     * @param parentSubject
     * @throws EMPIException
     */
    public void insert(Subject parentSubject) throws EMPIException {
        // Insert all identifiers in one shot to retain sequence number assignment.
        List<SubjectIdentifier> subjectIdentifiers = parentSubject.getSubjectIdentifiers();
        subjectIdentifiers.addAll(parentSubject.getSubjectOtherIdentifiers());
        if (subjectIdentifiers.isEmpty()) {
            return; // Early exit!
        }
        PreparedStatement stmt = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ")
                    .append(this.getTableName())
                    .append("(subject_id,seq_no,type,identifier,subject_identifier_domain_id) values(?,?,?,?,?)");
            String sql = sb.toString();
            stmt = this.getPreparedStatement(sql);
            SubjectIdentifierDomainDAO sidDAO = new SubjectIdentifierDomainDAO(this.getConnection());
            Long subjectId = parentSubject.getInternalId().getId();
            int seqNo = 0;
            for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
                if (logger.isTraceEnabled()) {
                    logger.trace("SQL = " + sql);
                }
                InternalId internalId = new InternalId(subjectId, seqNo);
                subjectIdentifier.setInternalId(internalId);
                stmt.setLong(1, subjectId);
                stmt.setInt(2, seqNo++);
                stmt.setString(3, SubjectIdentifierDAO.getSubjectIdentifierTypeValue(subjectIdentifier.getIdentifierType()));
                stmt.setString(4, subjectIdentifier.getIdentifier());
                // Get foreign key reference to subjectidentifierdomain.
                SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
                int subjectIdentifierDomainId = sidDAO.getId(subjectIdentifierDomain);
                if (subjectIdentifierDomainId == -1) {
                    throw new EMPIException(
                            subjectIdentifierDomain.getUniversalId()
                            + " is not a known identifier domain",
                            EMPIException.ERROR_CODE_UNKNOWN_KEY_IDENTIFIER);
                }
                stmt.setInt(5, subjectIdentifierDomainId);
                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectIdentifierDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception inserting subject identifiers", ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param subjectId
     * @throws EMPIException
     */
    public void deleteSubjectRecords(InternalId subjectId) throws EMPIException {
        this.deleteRecords(subjectId, this.getTableName(), "subject_id", this.getClass().getName());
    }

    /**
     *
     * @param subjectIdentifierId
     * @throws EMPIException
     */
    public void deleteSubjectIdentifier(InternalId subjectIdentifierId) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM ").append(this.getTableName())
                    .append(" WHERE ").append("subject_id=? AND seq_no=?");
            String sql = sb.toString();
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setLong(1, subjectIdentifierId.getId());
            stmt.setInt(2, subjectIdentifierId.getSeqNo());
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                StringBuilder sbTrace = new StringBuilder();
                sbTrace.append("SubjectIdentifierDAO.deleteRecords: done executeUpdate elapedTimeMillis=").append((endTime - startTime));
                logger.trace(sbTrace.toString());
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception deleting records", ex);
        } finally {
            this.close(stmt);
        }
    }

     /**
     *
     * @param idType
     * @return
     */
    public static String getSubjectIdentifierTypeValue(SubjectIdentifier.Type idType) {
        String value = "";
        switch (idType) {
            case PID:
                value = "P";
                break;
            case OTHER:
            default:
                value = "O";
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
    public static SubjectIdentifier.Type getSubjectIdentifierType(String type) {
        SubjectIdentifier.Type idType = SubjectIdentifier.Type.PID;
        if (type.equalsIgnoreCase("P")) {
            idType = SubjectIdentifier.Type.PID;
        } else {
            idType = SubjectIdentifier.Type.OTHER;
        }
        return idType;
    }

    /**
     *
     * @return
     */
    public String getTableName() {
        return "subject_identifier";
    }
}
