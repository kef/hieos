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
    public String getSubjectId(SubjectIdentifier subjectIdentifier) throws EMPIException {
        String subjectId = null;
        // First, get the SubjectIdentifierDomainId
        SubjectIdentifierDomainDAO sidDAO = new SubjectIdentifierDomainDAO(this.getConnection());
        int subjectIdentifierDomainId = sidDAO.getId(subjectIdentifier.getIdentifierDomain());
        if (subjectIdentifierDomainId == -1) {
            // We have no knowledge of the identifier domain (so get out now).
            return subjectId;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Now, see if we can locate the subject/identifier within the given identifier domain.
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT subject_id FROM ").append(this.getTableName()).append(" WHERE identifier=? and subject_identifier_domain_id=?");
            String sql = sb.toString();
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, subjectIdentifier.getIdentifier());
            stmt.setInt(2, subjectIdentifierDomainId);
            // Execute query.
            rs = stmt.executeQuery();
            if (rs.next()) {
                // Found.
                subjectId = rs.getString(1);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading subject identifiers", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return subjectId;
    }

    /**
     *
     * @param parentSubject
     * @return
     * @throws EMPIException
     */
    public List<SubjectIdentifier> load(Subject parentSubject) throws EMPIException {
        return this.load(parentSubject.getInternalId());
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public List<SubjectIdentifier> load(String subjectId) throws EMPIException {
        List<SubjectIdentifier> subjectIdentifiers = new ArrayList<SubjectIdentifier>();
        // Load the subject names.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT id,identifier,subject_identifier_domain_id FROM ").append(this.getTableName()).append(" WHERE subject_id=?");
            String sql = sb.toString();
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, subjectId);
            // Execute query.
            rs = stmt.executeQuery();
            SubjectIdentifierDomainDAO sidDAO = new SubjectIdentifierDomainDAO(this.getConnection());
            while (rs.next()) {
                SubjectIdentifier subjectIdentifier = new SubjectIdentifier();
                subjectIdentifier.setSubjectId(subjectId);
                subjectIdentifier.setInternalId(rs.getString(1));
                subjectIdentifier.setIdentifier(rs.getString(2));

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
     * @param subjectIdentifiers
     * @param parentSubject
     * @throws EMPIException
     */
    public void insert(List<SubjectIdentifier> subjectIdentifiers, Subject parentSubject) throws EMPIException {
        if (subjectIdentifiers.isEmpty()) {
            return; // Early exit!
        }
        PreparedStatement stmt = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(this.getTableName()).append("(id,subject_id,identifier,subject_identifier_domain_id) values(?,?,?,?)");
            String sql = sb.toString();
            stmt = this.getPreparedStatement(sql);
            SubjectIdentifierDomainDAO sidDAO = new SubjectIdentifierDomainDAO(this.getConnection());
            for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
                if (logger.isTraceEnabled()) {
                    logger.trace("SQL = " + sql);
                }
                stmt.setString(1, PersistenceHelper.getUUID());
                stmt.setString(2, parentSubject.getInternalId());
                stmt.setString(3, subjectIdentifier.getIdentifier());
                // Get foreign key reference to subjectidentifierdomain.
                SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
                int subjectIdentifierDomainId = sidDAO.getId(subjectIdentifierDomain);
                if (subjectIdentifierDomainId == -1) {
                    throw new EMPIException(
                            subjectIdentifierDomain.getUniversalId()
                            + " is not a known identifier domain",
                            EMPIException.ERROR_CODE_UNKNOWN_KEY_IDENTIFIER);
                }
                stmt.setInt(4, subjectIdentifierDomainId);
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
    public void deleteSubjectRecords(String subjectId) throws EMPIException {
        this.deleteRecords(subjectId, this.getTableName(), "subject_id", this.getClass().getName());
    }

    /**
     *
     * @param subjectIdentifierId
     * @throws EMPIException
     */
    public void deleteSubjectIdentifier(String subjectIdentifierId) throws EMPIException {
        this.deleteRecords(subjectIdentifierId, this.getTableName(), "id", this.getClass().getName());
    }

    /**
     *
     * @return
     */
    public String getTableName() {
        return "subject_identifier";
    }
}
