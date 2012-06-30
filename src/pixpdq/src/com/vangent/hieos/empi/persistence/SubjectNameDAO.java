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
import com.vangent.hieos.hl7v3util.model.subject.SubjectName;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.InternalId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectNameDAO extends AbstractDAO {

    private final static Logger logger = Logger.getLogger(SubjectNameDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectNameDAO(Connection connection) {
        super(connection);
    }

    /**
     *
     * @param parentSubject
     * @throws EMPIException
     */
    public void load(Subject parentSubject) throws EMPIException {
        List<SubjectName> subjectNames = parentSubject.getSubjectNames();
        // Load the subject names.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT seq_no,given_name,family_name,prefix,suffix,middle_name FROM subject_name WHERE subject_id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            stmt.setLong(1, subjectId);
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                SubjectName subjectName = new SubjectName();
                int seqNo = rs.getInt(1);
                InternalId internalId = new InternalId(subjectId, seqNo);
                subjectName.setInternalId(internalId);
                subjectName.setGivenName(rs.getString(2));
                subjectName.setFamilyName(rs.getString(3));
                subjectName.setPrefix(rs.getString(4));
                subjectName.setSuffix(rs.getString(5));
                subjectName.setMiddleName(rs.getString(6));
                subjectNames.add(subjectName);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading subject name(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    /**
     * 
     * @param subjectNames
     * @param parentSubject
     * @throws EMPIException
     */
    public void insert(List<SubjectName> subjectNames, Subject parentSubject) throws EMPIException {
        if (subjectNames.isEmpty()) {
            return; // Early exit!
        }
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO subject_name(subject_id,seq_no,given_name,family_name,prefix,suffix,middle_name) values(?,?,?,?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            int seqNo = 0;
            for (SubjectName subjectName : subjectNames) {
                if (logger.isTraceEnabled()) {
                    logger.trace("SQL = " + sql);
                }
                InternalId internalId = new InternalId(subjectId, seqNo);
                subjectName.setInternalId(internalId);
                stmt.setLong(1, subjectId);
                stmt.setInt(2, seqNo++);
                stmt.setString(3, subjectName.getGivenName());
                stmt.setString(4, subjectName.getFamilyName());
                stmt.setString(5, subjectName.getPrefix());
                stmt.setString(6, subjectName.getSuffix());
                stmt.setString(7, subjectName.getMiddleName());
                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectNameDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception inserting subject names", ex);
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
        this.deleteRecords(subjectId, "subject_name", "subject_id", this.getClass().getName());
    }
}
