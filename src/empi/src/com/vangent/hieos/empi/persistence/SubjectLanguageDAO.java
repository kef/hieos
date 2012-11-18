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
import com.vangent.hieos.subjectmodel.SubjectLanguage;
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
public class SubjectLanguageDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(SubjectLanguageDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectLanguageDAO(Connection connection) {
        super(connection);
    }

    /**
     * 
     * @param parentSubject
     * @throws EMPIException
     */
    public void load(Subject parentSubject) throws EMPIException {
        List<SubjectLanguage> subjectLanguages = parentSubject.getSubjectLanguages();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT seq_no,preference_indicator,language_code FROM subject_language WHERE subject_id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            stmt.setLong(1, subjectId);
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                SubjectLanguage subjectLanguage = new SubjectLanguage();
                int seqNo = rs.getInt(1);
                InternalId internalId = new InternalId(subjectId, seqNo);
                subjectLanguage.setInternalId(internalId);
                subjectLanguage.setPreferenceIndicator(this.getBoolean(rs, 2));

                // Load language coded value.
                subjectLanguage.setLanguageCode(this.getCodedValue(rs.getString(3), CodesConfig.CodedType.LANGUAGE));

                // Add subject's language to the list.
                subjectLanguages.add(subjectLanguage);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading subject language(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    /**
     *
     * @param subjectLanguages
     * @param parentSubject
     * @throws EMPIException
     */
    public void insert(List<SubjectLanguage> subjectLanguages, Subject parentSubject) throws EMPIException {
        if (subjectLanguages.isEmpty()) {
            return; // Early exit!
        }
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO subject_language(subject_id,seq_no,language_code,preference_indicator) values(?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            int seqNo = 0;
            for (SubjectLanguage subjectLanguage : subjectLanguages) {
                if (logger.isTraceEnabled()) {
                    logger.trace("SQL = " + sql);
                }
                InternalId internalId = new InternalId(subjectId, seqNo);
                subjectLanguage.setInternalId(internalId);
                stmt.setLong(1, subjectId);
                stmt.setInt(2, seqNo++);

                // Insert language type coded value.
                this.setCodedValue(stmt, 3, subjectLanguage.getLanguageCode(), CodesConfig.CodedType.LANGUAGE);

                this.setBoolean(stmt, 4, subjectLanguage.getPreferenceIndicator());

                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectLanguageDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
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
        this.deleteRecords(subjectId, "subject_language", "subject_id", this.getClass().getName());
    }
}
