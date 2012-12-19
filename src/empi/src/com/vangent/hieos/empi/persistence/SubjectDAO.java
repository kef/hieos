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

import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.model.SubjectCrossReference;
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.Subject.SubjectType;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(SubjectDAO.class);

    /**
     *
     * @param persistenceManager
     */
    public SubjectDAO(PersistenceManager persistenceManager) {
        super(persistenceManager);
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public Subject load(InternalId subjectId) throws EMPIException {
        // Load the base subject.
        Subject subject = this.loadBaseSubject(subjectId);

        // Now, load composed objects.
        PersistenceManager pm = this.getPersistenceManager();
        SubjectDemographicsDAO subjectDemographicsDAO = new SubjectDemographicsDAO(pm);
        subjectDemographicsDAO.load(subject);

        // Personal relationships.
        SubjectPersonalRelationshipDAO subjectPersonalRelationshipDAO = new SubjectPersonalRelationshipDAO(pm);
        subjectPersonalRelationshipDAO.load(subject);

        // Identifiers.
        SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(pm);
        List<SubjectIdentifier> subjectIdentifiers = subjectIdentifierDAO.load(subject, SubjectIdentifier.Type.PID);
        subject.setSubjectIdentifiers(subjectIdentifiers);

        // Other identifiers.
        List<SubjectIdentifier> subjectOtherIdentifiers = subjectIdentifierDAO.load(subject, SubjectIdentifier.Type.OTHER);
        subject.setSubjectOtherIdentifiers(subjectOtherIdentifiers);

        return subject;
    }

    /**
     *
     * @param subjectIdentifiers
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public List<Subject> loadBaseSubjectsByIdentifier(List<SubjectIdentifier> subjectIdentifiers) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(this.getPersistenceManager());

        // First the subject internal ids for the given subject identifier.
        List<InternalId> subjectIds = subjectIdentifierDAO.getSubjectIds(subjectIdentifiers);
        List<Subject> subjects = new ArrayList<Subject>();
        for (InternalId subjectId : subjectIds) {
            Subject subject = this.loadBaseSubject(subjectId);
            subjects.add(subject);
        }
        return subjects;
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public Subject loadBaseSubject(InternalId subjectId) throws EMPIException {
        Subject subject = new Subject();

        // Load the subject.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT type,last_updated_time FROM subject WHERE id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setLong(1, subjectId.getId());
            // Execute query.
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new EMPIException("No subject found for uniqueid = " + subjectId);
            } else {
                subject.setInternalId(subjectId);
                subject.setType(SubjectDAO.getSubjectType(rs.getString(1)));
                Date lastUpdatedTime = this.getDate(rs.getTimestamp(2));
                subject.setLastUpdatedTime(lastUpdatedTime);
            }
        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception reading Subject from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return subject;
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public InternalId getLastUpdatedSystemSubjectId(InternalId enterpriseSubjectId) throws EMPIException {
        InternalId lastUpdatedSystemSubjectId = null;
        // Get list of cross references.
        SubjectCrossReferenceDAO subjectCrossReferenceDAO = new SubjectCrossReferenceDAO(this.getPersistenceManager());
        List<SubjectCrossReference> subjectCrossReferences =
                subjectCrossReferenceDAO.loadEnterpriseSubjectCrossReferences(enterpriseSubjectId);
        // Now, get the system subject id with the last updated time stamp.
        Date compareLastUpdatedTime = null;
        for (SubjectCrossReference subjectCrossReference : subjectCrossReferences) {
            Date lastUpdatedTime = this.getLastUpdatedTime(subjectCrossReference.getSystemSubjectId());
            if (compareLastUpdatedTime == null || lastUpdatedTime.after(compareLastUpdatedTime)) {
                compareLastUpdatedTime = lastUpdatedTime;
                lastUpdatedSystemSubjectId = subjectCrossReference.getSystemSubjectId();
            }
        }
        return lastUpdatedSystemSubjectId;
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    private Date getLastUpdatedTime(InternalId subjectId) throws EMPIException {
        Date lastUpdatedTime = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT last_updated_time FROM subject WHERE id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setLong(1, subjectId.getId());
            // Execute query.
            rs = stmt.executeQuery();
            if (rs.next()) {
                lastUpdatedTime = this.getDate(rs.getTimestamp(1));
            }
        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception reading subject(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return lastUpdatedTime;
    }

    /**
     *
     * @param subjectIdentifiers
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public boolean doesSubjectExist(List<SubjectIdentifier> subjectIdentifiers) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        boolean subjectExists = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(this.getPersistenceManager());
            // Go through each identifier (assumes no duplicates across subjects).
            // See if the identifier has a subject.
            List<InternalId> subjectIds = subjectIdentifierDAO.getSubjectIds(subjectIdentifiers);
            if (!subjectIds.isEmpty()) {
                subjectExists = true;
            }
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return subjectExists;
    }

    /**
     *
     * @param subjects
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public void insert(List<Subject> subjects) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        if (subjects.isEmpty()) {
            return; // Early exit!
        }
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO subject(id,type,last_updated_time) values(?,?,?)";
            stmt = this.getPreparedStatement(sql);
            for (Subject subject : subjects) {
                if (logger.isTraceEnabled()) {
                    logger.trace("SQL = " + sql);
                }
                Long subjectId = this.generateSubjectUniqueId();
                InternalId internalId = new InternalId(subjectId);
                subject.setInternalId(internalId);
                String subjectTypeValue = SubjectDAO.getSubjectTypeValue(subject.getType());
                subject.setLastUpdatedTime(new Date());  // Update timestamp.
                stmt.setLong(1, subjectId);
                stmt.setString(2, subjectTypeValue);
                stmt.setTimestamp(3, this.getTimestamp(subject.getLastUpdatedTime()));
                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }

            // FIXME: Anyway to batch sub components????

            // Now, insert composed objects.
            PersistenceManager pm = this.getPersistenceManager();
            SubjectDemographicsDAO subjectDemographicsDAO = new SubjectDemographicsDAO(pm);
            SubjectPersonalRelationshipDAO subjectPersonalRelationshipDAO = new SubjectPersonalRelationshipDAO(pm);
            SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(pm);
            for (Subject subject : subjects) {
                subjectDemographicsDAO.insert(subject);
                subjectPersonalRelationshipDAO.insert(subject.getSubjectPersonalRelationships(), subject);
                subjectIdentifierDAO.insert(subject);
            }
        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception inserting subjects", ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @return @throws EMPIException
     */
    private Long generateSubjectUniqueId() throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        String sql = empiConfig.getSubjectSequenceGeneratorSQL();
        SequenceGenerator sg = new SequenceGenerator(this.getPersistenceManager(), sql);
        return sg.getNext();
    }

    /**
     *
     * @param targetEnterpriseSubjectId
     * @param subject Contains demographics to update.
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public void updateEnterpriseSubject(InternalId targetEnterpriseSubjectId, Subject subject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        // First, load the enterprise subject.
        //Subject enterpriseSubject = this.load(targetEnterpriseSubjectId);

        // Create updated enterprise subject.
        Subject updatedEnterpriseSubject;
        try {
            updatedEnterpriseSubject = (Subject) subject.clone();
            updatedEnterpriseSubject.setInternalId(targetEnterpriseSubjectId);
        } catch (CloneNotSupportedException ex) {
            throw new EMPIException("Internal exception - unable to clone subject");
        }
        updatedEnterpriseSubject.setLastUpdatedTime(new Date());

        // FIXME: For now, simply replace all demographics from the supplied subject.  Also, should move code.

        // Delete subject components (names, addresses, etc.)
        this.deleteSubjectComponents(targetEnterpriseSubjectId);

        // Now, insert composed objects.

        // Get DAO instances.
        PersistenceManager pm = this.getPersistenceManager();
        SubjectDemographicsDAO subjectDemographicsDAO = new SubjectDemographicsDAO(pm);
        SubjectPersonalRelationshipDAO subjectPersonalRelationshipDAO = new SubjectPersonalRelationshipDAO(pm);

        // Insert content.
        subjectDemographicsDAO.insert(updatedEnterpriseSubject);
        subjectPersonalRelationshipDAO.insert(updatedEnterpriseSubject.getSubjectPersonalRelationships(), updatedEnterpriseSubject);

        this.updateLastUpdateTime(updatedEnterpriseSubject);

        // Note, identifiers are not touched
    }
    
    /**
     *
     * @param subject
     * @throws EMPIException
     */
    private void updateLastUpdateTime(Subject subject) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            String sql = "UPDATE subject SET last_updated_time=? WHERE id=?";
            System.out.println("SQL = " + sql);
            stmt = this.getPreparedStatement(sql);
            this.setDate(stmt, 1, subject.getBirthTime());
            stmt.setLong(2, subject.getInternalId().getId());
            stmt.addBatch();
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectDAO.updateLastUpdateTime: done executeBatch elapedTimeMillis=" + (endTime - startTime));
            }
        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception updated subject", ex);
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
    public void mergeEnterpriseSubjects(InternalId survivingEnterpriseSubjectId, InternalId subsumedEnterpriseSubjectId) throws EMPIException {

        // Only perform merge if the ids are different.
        // Guard is here just in case higher-level logic does not account for this case.
        if (!survivingEnterpriseSubjectId.equals(subsumedEnterpriseSubjectId)) {

            // Delete "SubjectMatchFieldsDAO" record for subsumedEnterpriseSubjectId.
            //SubjectMatchFieldsDAO subjectMatchFieldsDAO = new SubjectMatchFieldsDAO(conn);
            //subjectMatchFieldsDAO.deleteSubjectRecords(subsumedEnterpriseSubjectId);

            // Move cross references from subsumedEnterpriseSubjectId to survivingEnterpriseSubjectId
            SubjectCrossReferenceDAO subjectCrossReferenceDAO = new SubjectCrossReferenceDAO(this.getPersistenceManager());
            subjectCrossReferenceDAO.mergeEnterpriseSubjects(survivingEnterpriseSubjectId, subsumedEnterpriseSubjectId);

            // Delete the subsumed enterprise subject.
            this.deleteSubject(subsumedEnterpriseSubjectId, Subject.SubjectType.ENTERPRISE);
        }
    }

    /**
     *
     * @param subjectId
     * @param subjectType
     * @throws EMPIException
     */
    public void deleteSubject(InternalId subjectId, Subject.SubjectType subjectType) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            PersistenceManager pm = this.getPersistenceManager();

            // First delete component parts.
            this.deleteSubjectComponents(subjectId);

            // Now delete identifiers.

            // Get DAO instances responsible for deletions.
            SubjectIdentifierDAO subjectIdentifierDAO = new SubjectIdentifierDAO(pm);
            //SubjectOtherIdentifierDAO subjectOtherIdentifierDAO = new SubjectOtherIdentifierDAO(conn);
            SubjectCrossReferenceDAO subjectCrossReferenceDAO = new SubjectCrossReferenceDAO(pm);

            // Run deletions.
            subjectIdentifierDAO.deleteSubjectRecords(subjectId);
            //subjectOtherIdentifierDAO.deleteSubjectRecords(subjectId);
            subjectCrossReferenceDAO.deleteSubjectCrossReferences(subjectId, subjectType);

            if (subjectType.equals(SubjectType.SYSTEM)) {
                // Delete subject match record.
                SubjectMatchFieldsDAO subjectMatchFieldsDAO = new SubjectMatchFieldsDAO(pm);
                subjectMatchFieldsDAO.deleteSubjectRecords(subjectId);
            }

            // Now, delete the subject record.
            this.deleteRecords(subjectId, "subject", "id", this.getClass().getName());

        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param subjectId
     * @throws EMPIException
     */
    private void deleteSubjectComponents(InternalId subjectId) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            PersistenceManager pm = this.getPersistenceManager();
            // Get DAO instances responsible for deletions.
            SubjectDemographicsDAO subjectDemographicsDAO = new SubjectDemographicsDAO(pm);
            SubjectPersonalRelationshipDAO subjectPersonalRelationshipDAO = new SubjectPersonalRelationshipDAO(pm);

            // Run deletions.
            subjectDemographicsDAO.deleteSubjectRecords(subjectId);
            subjectPersonalRelationshipDAO.deleteSubjectRecords(subjectId);

        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param subjectType
     * @return
     */
    private static String getSubjectTypeValue(Subject.SubjectType subjectType) {
        String value;
        switch (subjectType) {
            case ENTERPRISE:
                value = "E";
                break;
            case SYSTEM:
                value = "S";
                break;
            case PERSONAL_RELATIONSHIP:
                value = "P";
                break;
            default:
                value = "V";
                break;
        }
        return value;
    }

    /**
     * NOTE: Could have built a full enumeration, but decided to be overkill.
     *
     * @param type
     * @return
     */
    private static Subject.SubjectType getSubjectType(String type) {
        Subject.SubjectType subjectType;
        if (type.equalsIgnoreCase("E")) {
            subjectType = SubjectType.ENTERPRISE;
        } else if (type.equalsIgnoreCase("S")) {
            subjectType = SubjectType.SYSTEM;
        } else if (type.equalsIgnoreCase("P")) {
            subjectType = SubjectType.PERSONAL_RELATIONSHIP;
        } else {
            subjectType = SubjectType.VOIDED;
        }
        return subjectType;
    }
}
