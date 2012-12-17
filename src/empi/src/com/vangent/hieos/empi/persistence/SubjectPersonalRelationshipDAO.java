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
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectPersonalRelationship;
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
     * @param persistenceManager
     */
    public SubjectPersonalRelationshipDAO(PersistenceManager persistenceManager) {
        super(persistenceManager);
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
            SubjectDAO subjectDAO = new SubjectDAO(this.getPersistenceManager());
            String sql = "SELECT seq_no,subject_personal_relationship_code,personal_relationship_subject_id FROM subject_personal_relationship WHERE subject_id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            stmt.setLong(1, subjectId);
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                SubjectPersonalRelationship subjectPersonalRelationship = new SubjectPersonalRelationship();
                int seqNo = rs.getInt(1);
                InternalId internalId = new InternalId(subjectId, seqNo);
                subjectPersonalRelationship.setInternalId(internalId);

                // Load relationship type coded value.
                subjectPersonalRelationship.setRelationshipType(this.getCodedValue(rs.getString(2), CodesConfig.CodedType.PERSONAL_RELATIONSHIP));

                // Load related subject.
                Long personalRelationshipSubjectId = rs.getLong(3);
                internalId = new InternalId(personalRelationshipSubjectId);
                Subject relatedSubject = subjectDAO.load(internalId);
                subjectPersonalRelationship.setSubject(relatedSubject);

                // Add personal relationship to the list.
                subjectPersonalRelationships.add(subjectPersonalRelationship);
            }
        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception reading SubjectPersonalRelationship(s) from database", ex);
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
     * @throws EMPIExceptionUnknownIdentifierDomain  
     */
    public void insert(List<SubjectPersonalRelationship> subjectPersonalRelationships, Subject parentSubject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        if (subjectPersonalRelationships.isEmpty()) {
            return; // Early exit!
        }
        PreparedStatement stmt = null;
        try {
            SubjectDAO subjectDAO = new SubjectDAO(this.getPersistenceManager());
            String sql = "INSERT INTO subject_personal_relationship(subject_id,seq_no,subject_personal_relationship_code,personal_relationship_subject_id) values(?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            int seqNo = 0;
            for (SubjectPersonalRelationship subjectPersonalRelationship : subjectPersonalRelationships) {
                if (logger.isTraceEnabled()) {
                    logger.trace("SQL = " + sql);
                }
                InternalId internalId = new InternalId(subjectId, seqNo);
                subjectPersonalRelationship.setInternalId(internalId);
                stmt.setLong(1, subjectId);
                stmt.setInt(2, seqNo++);

                // Insert relationship type coded value.
                this.setCodedValue(stmt, 3, subjectPersonalRelationship.getRelationshipType(), CodesConfig.CodedType.PERSONAL_RELATIONSHIP);

                // Insert related subject.
                List<Subject> relatedSubjects = new ArrayList<Subject>();
                Subject relatedSubject = subjectPersonalRelationship.getSubject();
                relatedSubject.setType(Subject.SubjectType.PERSONAL_RELATIONSHIP);
                relatedSubjects.add(relatedSubject);
                subjectDAO.insert(relatedSubjects);
                stmt.setLong(4, relatedSubject.getInternalId().getId());

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
            throw PersistenceManager.getEMPIException("Exception inserting subject personal relationships", ex);
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
        this.deleteRecords(subjectId, "subject_personal_relationship", "subject_id", this.getClass().getName());
    }
}
