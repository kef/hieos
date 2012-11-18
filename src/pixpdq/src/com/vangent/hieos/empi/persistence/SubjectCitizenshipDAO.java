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

import com.vangent.hieos.empi.codes.CodesConfig;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.SubjectCitizenship;
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
public class SubjectCitizenshipDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(SubjectCitizenshipDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectCitizenshipDAO(Connection connection) {
        super(connection);
    }

    /**
     * 
     * @param parentSubject
     * @throws EMPIException
     */
    public void load(Subject parentSubject) throws EMPIException {
        List<SubjectCitizenship> subjectCitizenships = parentSubject.getSubjectCitizenships();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT seq_no,nation_code,nation_name FROM subject_citizenship WHERE subject_id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            stmt.setLong(1, subjectId);
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                SubjectCitizenship subjectCitizenship = new SubjectCitizenship();
                int seqNo = rs.getInt(1);
                InternalId internalId = new InternalId(subjectId, seqNo);
                subjectCitizenship.setInternalId(internalId);

                // Load language coded value.
                subjectCitizenship.setNationCode(this.getCodedValue(rs.getString(2), CodesConfig.CodedType.NATION));

                subjectCitizenship.setNationName(rs.getString(3));

                // Add subject's language to the list.
                subjectCitizenships.add(subjectCitizenship);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading subject citizenship(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    /**
     *
     * @param subjectCitizenships 
     * @param parentSubject
     * @throws EMPIException
     */
    public void insert(List<SubjectCitizenship> subjectCitizenships, Subject parentSubject) throws EMPIException {
        if (subjectCitizenships.isEmpty()) {
            return;  // Early exit!
        }
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO subject_citizenship(subject_id,seq_no,nation_code,nation_name) values(?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            int seqNo = 0;
            for (SubjectCitizenship subjectCitizenship : subjectCitizenships) {
                if (logger.isTraceEnabled()) {
                    logger.trace("SQL = " + sql);
                }
                InternalId internalId = new InternalId(subjectId, seqNo);
                subjectCitizenship.setInternalId(internalId);
                stmt.setLong(1, subjectId);
                stmt.setInt(2, seqNo++);

                // Insert nation coded value.
                this.setCodedValue(stmt, 3, subjectCitizenship.getNationCode(), CodesConfig.CodedType.NATION);

                stmt.setString(4, subjectCitizenship.getNationName());

                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectCitizenshipDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception inserting subject language(s)", ex);
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
        this.deleteRecords(subjectId, "subject_citizenship", "subject_id", this.getClass().getName());
    }
}
