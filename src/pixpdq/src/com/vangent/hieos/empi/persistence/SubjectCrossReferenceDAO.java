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
import com.vangent.hieos.services.pixpdq.empi.exception.EMPIException;
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
    public List<SubjectCrossReference> load(String enterpriseSubjectId) throws EMPIException {
        List<SubjectCrossReference> subjectCrossReferences = new ArrayList<SubjectCrossReference>();
        // Load the subject names.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT systemsubjectid, matchscore FROM subjectxref WHERE enterprisesubjectid = ?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, enterpriseSubjectId);
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                SubjectCrossReference subjectCrossReference = new SubjectCrossReference();
                subjectCrossReference.setEnterpriseSubjectId(enterpriseSubjectId);
                subjectCrossReference.setSystemSubjectId(rs.getString(1));
                subjectCrossReference.setMatchScore(rs.getDouble(2));

                // Add SubjectIdentifier to list.
                subjectCrossReferences.add(subjectCrossReference);
            }
        } catch (SQLException ex) {
            throw new EMPIException("Failure reading subjectxref(s) from database" + ex.getMessage());
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
            String sql = "SELECT enterprisesubjectid FROM subjectxref WHERE systemsubjectid = ?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, systemSubjectId);
            // Execute query.
            rs = stmt.executeQuery();
            if (rs.next()) {
                enterpriseSubjectId = rs.getString(1);
            }
        } catch (SQLException ex) {
            throw new EMPIException("Failure reading subjectxref(s) from database" + ex.getMessage());
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
            String sql = "INSERT INTO subjectxref(enterprisesubjectid,systemsubjectid, matchscore) values(?,?,?)";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, subjectCrossReference.getEnterpriseSubjectId());
            stmt.setString(2, subjectCrossReference.getSystemSubjectId());
            stmt.setDouble(3, subjectCrossReference.getMatchScore());
            stmt.addBatch();
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectCrossReferenceDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }
        } catch (SQLException ex) {
            throw new EMPIException(ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param survivingSubjectId
     * @param subsumedSubjectId
     * @throws EMPIException
     */
    public void merge(String survivingSubjectId, String subsumedSubjectId) throws EMPIException {

        // Move cross references from subsumedSubjectId to survivingSubjectId
        PreparedStatement stmt = null;
        try {
            String sql = "UPDATE subjectxref SET enterprisesubjectid=? WHERE enterprisesubjectid=?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, survivingSubjectId);
            stmt.setString(2, subsumedSubjectId);
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectCrossReferenceDAO.merge: done executeBatch elapedTimeMillis=" + (endTime - startTime));
            }
        } catch (SQLException ex) {
            throw new EMPIException(ex);
        } finally {
            this.close(stmt);
        }
    }
}
