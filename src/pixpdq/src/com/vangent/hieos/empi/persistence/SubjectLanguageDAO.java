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
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.SubjectLanguage;
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
            CodeDAO codeDAO = new CodeDAO(this.getConnection());
            String sql = "SELECT id,preference_indicator,language_code_id FROM subject_language WHERE subject_id=?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, parentSubject.getInternalId());
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                SubjectLanguage subjectLanguage = new SubjectLanguage();
                subjectLanguage.setInternalId(rs.getString(1));
                subjectLanguage.setPreferenceIndicator(this.getBoolean(rs, 2));

                // Load language coded value.
                int subjectLanguageCodeId = rs.getInt(3);
                subjectLanguage.setLanguageCode(codeDAO.load(subjectLanguageCodeId, CodeDAO.CodeType.LANGUAGE));

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
        PreparedStatement stmt = null;
        try {
            CodeDAO codeDAO = new CodeDAO(this.getConnection());
            String sql = "INSERT INTO subject_language(id,subject_id,language_code_id,preference_indicator) values(?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            for (SubjectLanguage subjectLanguage : subjectLanguages) {
                subjectLanguage.setInternalId(PersistenceHelper.getUUID());
                stmt.setString(1, subjectLanguage.getInternalId());
                stmt.setString(2, parentSubject.getInternalId());

                // Insert language type coded value.
                this.setCodedValueId(codeDAO, CodeDAO.CodeType.LANGUAGE, stmt, 3, subjectLanguage.getLanguageCode());

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
    public void deleteSubjectRecords(String subjectId) throws EMPIException {
        this.deleteRecords(subjectId, "subject_language", "subject_id", this.getClass().getName());
    }
}
