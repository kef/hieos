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

import com.vangent.hieos.empi.model.SubjectCrossReference;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
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
public class SubjectCrossReferenceDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(SubjectCrossReferenceDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectCrossReferenceDAO(Connection connection) {
        super(connection);
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public List<SubjectCrossReference> loadEnterpriseSubjectCrossReferences(String enterpriseSubjectId) throws EMPIException {
        List<SubjectCrossReference> subjectCrossReferences = new ArrayList<SubjectCrossReference>();
        // Load the subject names.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT system_subject_id, match_score FROM subject_xref WHERE enterprise_subject_id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, enterpriseSubjectId);
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                SubjectCrossReference subjectCrossReference = new SubjectCrossReference();
                subjectCrossReference.setEnterpriseSubjectId(enterpriseSubjectId);
                subjectCrossReference.setSystemSubjectId(rs.getString(1));
                subjectCrossReference.setMatchScore(rs.getDouble(2));

                // Add cross-reference to list.
                subjectCrossReferences.add(subjectCrossReference);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading SubjectCrossReference(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return subjectCrossReferences;
    }

    /**
     *
     * @param systemSubjectId
     * @return
     * @throws EMPIException
     */
    public String getEnterpriseSubjectId(String systemSubjectId) throws EMPIException {
        String enterpriseSubjectId = null;
        // Load the subject names.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT enterprise_subject_id FROM subject_xref WHERE system_subject_id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, systemSubjectId);
            // Execute query.
            rs = stmt.executeQuery();
            if (rs.next()) {
                enterpriseSubjectId = rs.getString(1);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading SubjectCrossReference(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return enterpriseSubjectId;
    }

    /**
     *
     * @param subjectCrossReference
     * @throws EMPIException
     */
    public void insert(SubjectCrossReference subjectCrossReference) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO subject_xref(enterprise_subject_id,system_subject_id, match_score) values(?,?,?)";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, subjectCrossReference.getEnterpriseSubjectId());
            stmt.setString(2, subjectCrossReference.getSystemSubjectId());
            stmt.setDouble(3, subjectCrossReference.getMatchScore());
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectCrossReferenceDAO.insert: done executeUpdate elapedTimeMillis=" + (endTime - startTime));
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception inserting subject cross references", ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param survivingEnterpriseSubjectId
     * @param subsumedEnterpriseSubjectId
     * @throws EMPIException
     */
    public void mergeEnterpriseSubjects(String survivingEnterpriseSubjectId, String subsumedEnterpriseSubjectId) throws EMPIException {

        // Move cross references from subsumedEnterpriseSubjectId to survivingEnterpriseSubjectId
        PreparedStatement stmt = null;
        try {
            String sql = "UPDATE subject_xref SET enterprise_subject_id=? WHERE enterprise_subject_id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, survivingEnterpriseSubjectId);
            stmt.setString(2, subsumedEnterpriseSubjectId);
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectCrossReferenceDAO.mergeEnterpriseSubjects: done executeUpdate elapedTimeMillis=" + (endTime - startTime));
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception updating subject cross references", ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     * 
     * @param subjectId
     * @param subjectType
     * @throws EMPIException
     */
    public void deleteSubjectCrossReferences(String subjectId, Subject.SubjectType subjectType) throws EMPIException {
        String columnName = "system_subject_id";
        if (subjectType.equals(Subject.SubjectType.ENTERPRISE)) {
            columnName = "enterprise_subject_id";
        }
        this.deleteRecords(subjectId, "subject_xref", columnName, this.getClass().getName());
    }

    /**
     *
     * @param enterpriseSubjectId
     * @throws EMPIException
     */
    public void deleteEnterpriseSubjectCrossReferences(String enterpriseSubjectId) throws EMPIException {
        this.deleteRecords(enterpriseSubjectId, "subject_xref", "enterprise_subject_id", this.getClass().getName());
    }
}
