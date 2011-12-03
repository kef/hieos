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
            String sql = "SELECT id,given_name,family_name,prefix,suffix,middle_name FROM subject_name WHERE subject_id=?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, parentSubject.getInternalId());
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                SubjectName subjectName = new SubjectName();
                subjectName.setInternalId(rs.getString(1));
                subjectName.setGivenName(rs.getString(2));
                subjectName.setFamilyName(rs.getString(3));
                subjectName.setPrefix(rs.getString(4));
                subjectName.setSuffix(rs.getString(5));
                subjectName.setMiddleName(rs.getString(6));
                subjectNames.add(subjectName);
            }
        } catch (SQLException ex) {
            throw new EMPIException("Failure reading subject name(s) from database" + ex.getMessage());
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
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO subject_name(id,given_name,family_name,prefix,suffix,middle_name,subject_id) values(?,?,?,?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            for (SubjectName subjectName : subjectNames) {
                subjectName.setInternalId(PersistenceHelper.getUUID());
                stmt.setString(1, subjectName.getInternalId());
                stmt.setString(2, subjectName.getGivenName());
                stmt.setString(3, subjectName.getFamilyName());
                stmt.setString(4, subjectName.getPrefix());
                stmt.setString(5, subjectName.getSuffix());
                stmt.setString(6, subjectName.getMiddleName());
                stmt.setString(7, parentSubject.getInternalId());
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
    public void deleteSubjectRecords(String subjectId) throws EMPIException {
        this.deleteRecords(subjectId, "subject_name", "subject_id", this.getClass().getName());
    }
}
