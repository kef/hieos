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
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.Subject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectDemographicsDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(SubjectDemographicsDAO.class);

    /**
     *
     * @param persistenceManager
     */
    public SubjectDemographicsDAO(PersistenceManager persistenceManager) {
        super(persistenceManager);
    }

    /**
     *
     * @param parentSubject
     * @throws EMPIException
     */
    public void load(Subject parentSubject) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();

        // Load the demographics.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT birth_time,gender_code,deceased_indicator,deceased_time,multiple_birth_indicator,multiple_birth_order_number,marital_status_code,religious_affiliation_code,race_code,ethnic_group_code FROM subject_demographics WHERE subject_id=?";

            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            stmt.setLong(1, subjectId);
            // Execute query.
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new EMPIException("No subject_demographics found for uniqueid = " + subjectId);
            } else {
                parentSubject.setBirthTime(this.getDate(rs, 1));
                parentSubject.setGender(this.getCodedValue(rs.getString(2), CodesConfig.CodedType.GENDER));
                parentSubject.setDeceasedIndicator(this.getBoolean(rs, 3));
                parentSubject.setDeceasedTime(this.getDate(rs, 4));
                parentSubject.setMultipleBirthIndicator(this.getBoolean(rs, 5));
                parentSubject.setMultipleBirthOrderNumber(this.getInteger(rs, 6));
                parentSubject.setMaritalStatus(this.getCodedValue(rs.getString(7), CodesConfig.CodedType.MARITAL_STATUS));
                parentSubject.setReligiousAffiliation(this.getCodedValue(rs.getString(8), CodesConfig.CodedType.RELIGIOUS_AFFILIATION));
                parentSubject.setRace(this.getCodedValue(rs.getString(9), CodesConfig.CodedType.RACE));
                parentSubject.setEthnicGroup(this.getCodedValue(rs.getString(10), CodesConfig.CodedType.ETHNIC_GROUP));
            }
        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception reading subject_demographics from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }

        // Names.
        SubjectNameDAO subjectNameDAO = new SubjectNameDAO(pm);
        subjectNameDAO.load(parentSubject);

        // Addresses.
        SubjectAddressDAO subjectAddressDAO = new SubjectAddressDAO(pm);
        subjectAddressDAO.load(parentSubject);

        // Telecom addresses.
        SubjectTelecomAddressDAO subjectTelecomAddressDAO = new SubjectTelecomAddressDAO(pm);
        subjectTelecomAddressDAO.load(parentSubject);

        // Languages.
        SubjectLanguageDAO subjectLanguageDAO = new SubjectLanguageDAO(pm);
        subjectLanguageDAO.load(parentSubject);

        // Citizenships.
        SubjectCitizenshipDAO subjectCitizenshipDAO = new SubjectCitizenshipDAO(pm);
        subjectCitizenshipDAO.load(parentSubject);
    }

    /**
     *
     * @param addresses
     * @param parentSubject
     * @throws EMPIException
     */
    public void insert(Subject parentSubject) throws EMPIException {

        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO subject_demographics(subject_id,birth_time,gender_code,deceased_indicator,deceased_time,multiple_birth_indicator,multiple_birth_order_number,marital_status_code,religious_affiliation_code,race_code,ethnic_group_code) values(?,?,?,?,?,?,?,?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            Long subjectId = parentSubject.getInternalId().getId();
            stmt.setLong(1, subjectId);
            this.setDate(stmt, 2, parentSubject.getBirthTime());
            this.setCodedValue(stmt, 3, parentSubject.getGender(), CodesConfig.CodedType.GENDER);
            this.setBoolean(stmt, 4, parentSubject.getDeceasedIndicator());
            this.setDate(stmt, 5, parentSubject.getDeceasedTime());
            this.setBoolean(stmt, 6, parentSubject.getMultipleBirthIndicator());
            this.setInteger(stmt, 7, parentSubject.getMultipleBirthOrderNumber());
            this.setCodedValue(stmt, 8, parentSubject.getMaritalStatus(), CodesConfig.CodedType.MARITAL_STATUS);
            this.setCodedValue(stmt, 9, parentSubject.getReligiousAffiliation(), CodesConfig.CodedType.RELIGIOUS_AFFILIATION);
            this.setCodedValue(stmt, 10, parentSubject.getRace(), CodesConfig.CodedType.RACE);
            this.setCodedValue(stmt, 11, parentSubject.getEthnicGroup(), CodesConfig.CodedType.ETHNIC_GROUP);
            stmt.addBatch();
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectDemographicsDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }

            // Insert composed parts.
            PersistenceManager pm = this.getPersistenceManager();

            // Get DAO instanes for composed parts.
            SubjectNameDAO subjectNameDAO = new SubjectNameDAO(pm);
            SubjectAddressDAO subjectAddressDAO = new SubjectAddressDAO(pm);
            SubjectTelecomAddressDAO subjectTelecomAddressDAO = new SubjectTelecomAddressDAO(pm);
            SubjectLanguageDAO subjectLanguageDAO = new SubjectLanguageDAO(pm);
            SubjectCitizenshipDAO subjectCitizenshipDAO = new SubjectCitizenshipDAO(pm);

            // Conduct inserts.
            subjectNameDAO.insert(parentSubject.getSubjectNames(), parentSubject);
            subjectAddressDAO.insert(parentSubject.getAddresses(), parentSubject);
            subjectTelecomAddressDAO.insert(parentSubject.getTelecomAddresses(), parentSubject);
            subjectLanguageDAO.insert(parentSubject.getSubjectLanguages(), parentSubject);
            subjectCitizenshipDAO.insert(parentSubject.getSubjectCitizenships(), parentSubject);

        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception inserting subjects", ex);
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
        PersistenceManager pm = this.getPersistenceManager();
        SubjectNameDAO subjectNameDAO = new SubjectNameDAO(pm);
        SubjectAddressDAO subjectAddressDAO = new SubjectAddressDAO(pm);
        SubjectTelecomAddressDAO subjectTelecomAddressDAO = new SubjectTelecomAddressDAO(pm);
        SubjectLanguageDAO subjectLanguageDAO = new SubjectLanguageDAO(pm);
        SubjectCitizenshipDAO subjectCitizenshipDAO = new SubjectCitizenshipDAO(pm);

        // Run deletions.
        subjectNameDAO.deleteSubjectRecords(subjectId);
        subjectAddressDAO.deleteSubjectRecords(subjectId);
        subjectTelecomAddressDAO.deleteSubjectRecords(subjectId);
        subjectLanguageDAO.deleteSubjectRecords(subjectId);
        subjectCitizenshipDAO.deleteSubjectRecords(subjectId);
        this.deleteRecords(subjectId, "subject_demographics", "subject_id", this.getClass().getName());
    }
}
