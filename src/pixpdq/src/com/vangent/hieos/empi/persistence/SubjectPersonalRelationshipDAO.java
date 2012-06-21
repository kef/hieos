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
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.SubjectPersonalRelationship;
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
public class SubjectPersonalRelationshipDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(SubjectPersonalRelationshipDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectPersonalRelationshipDAO(Connection connection) {
        super(connection);
    }

    /**
     * 
     * @param parentSubject
     * @throws EMPIException
     */
    public void load(Subject parentSubject) throws EMPIException {
        List<SubjectPersonalRelationship> subjectPersonalRelationships = parentSubject.getSubjectPersonalRelationships();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            SubjectDAO subjectDAO = new SubjectDAO(this.getConnection());
            String sql = "SELECT id,subject_personal_relationship_code,personal_relationship_subject_id FROM subject_personal_relationship WHERE subject_id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, parentSubject.getInternalId());
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                SubjectPersonalRelationship subjectPersonalRelationship = new SubjectPersonalRelationship();
                subjectPersonalRelationship.setInternalId(rs.getString(1));

                // Load relationship type coded value.
                subjectPersonalRelationship.setRelationshipType(this.getCodedValue(rs.getString(2), CodesConfig.CodedType.PERSONAL_RELATIONSHIP));

                // Load related subject.
                String personalRelationshipSubjectId = rs.getString(3);
                Subject relatedSubject = subjectDAO.load(personalRelationshipSubjectId);
                subjectPersonalRelationship.setSubject(relatedSubject);

                // Add personal relationship to the list.
                subjectPersonalRelationships.add(subjectPersonalRelationship);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading SubjectPersonalRelationship(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    /**
     * 
     * @param subjectPersonalRelationships
     * @param parentSubject
     * @throws EMPIException
     */
    public void insert(List<SubjectPersonalRelationship> subjectPersonalRelationships, Subject parentSubject) throws EMPIException {
        if (subjectPersonalRelationships.isEmpty()) {
            return; // Early exit!
        }
        PreparedStatement stmt = null;
        try {
            SubjectDAO subjectDAO = new SubjectDAO(this.getConnection());
            String sql = "INSERT INTO subject_personal_relationship(id,subject_id,subject_personal_relationship_code,personal_relationship_subject_id) values(?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            for (SubjectPersonalRelationship subjectPersonalRelationship : subjectPersonalRelationships) {
                if (logger.isTraceEnabled()) {
                    logger.trace("SQL = " + sql);
                }
                subjectPersonalRelationship.setInternalId(PersistenceHelper.getUUID());
                stmt.setString(1, subjectPersonalRelationship.getInternalId());
                stmt.setString(2, parentSubject.getInternalId());

                // Insert relationship type coded value.
                this.setCodedValue(stmt, 3, subjectPersonalRelationship.getRelationshipType(), CodesConfig.CodedType.PERSONAL_RELATIONSHIP);

                // Insert related subject.
                List<Subject> relatedSubjects = new ArrayList<Subject>();
                Subject relatedSubject = subjectPersonalRelationship.getSubject();
                relatedSubject.setType(Subject.SubjectType.PERSONAL_RELATIONSHIP);
                relatedSubjects.add(relatedSubject);
                subjectDAO.insert(relatedSubjects);
                stmt.setString(4, relatedSubject.getInternalId());

                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectPersonalRelationshipDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception inserting subject personal relationships", ex);
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
        this.deleteRecords(subjectId, "subject_personal_relationship", "subject_id", this.getClass().getName());
    }
}
