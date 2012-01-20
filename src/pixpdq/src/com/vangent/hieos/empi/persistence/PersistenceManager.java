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
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.match.MatchAlgorithm.MatchType;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PersistenceManager {

    private final static Logger logger = Logger.getLogger(PersistenceManager.class);
    private Connection connection = null;

    /**
     * 
     */
    public PersistenceManager() {
        this.connection = null;
    }

    /**
     *
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * 
     * @param connection
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * 
     * @throws EMPIException
     */
    public void open() throws EMPIException {
        this.close();  // Just in case.
        this.connection = PersistenceHelper.getConnection();
    }

    /**
     *
     */
    public void rollback() {
        PersistenceHelper.rollback(connection);
    }

    /**
     *
     */
    public void close() {
        PersistenceHelper.close(connection);
    }

    /**
     * 
     * @throws EMPIException
     */
    public void commit() throws EMPIException {
        PersistenceHelper.commit(connection);
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public Subject loadSubject(String subjectId) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        return dao.load(subjectId);
    }

    /**
     * 
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public List<SubjectIdentifier> loadSubjectIdentifiers(String subjectId) throws EMPIException {
        SubjectIdentifierDAO dao = new SubjectIdentifierDAO(connection);
        return dao.load(subjectId);
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public List<SubjectIdentifier> loadSubjectOtherIdentifiers(String subjectId) throws EMPIException {
        SubjectOtherIdentifierDAO dao = new SubjectOtherIdentifierDAO(connection);
        return dao.load(subjectId);
    }

    /**
     *
     * @param subjectIdentifier
     * @return
     * @throws EMPIException
     */
    public Subject loadBaseSubjectByIdentifier(SubjectIdentifier subjectIdentifier) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        return dao.loadBaseSubjectByIdentifier(subjectIdentifier);
    }

    /**
     * 
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public Subject loadBaseSubject(String subjectId) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        return dao.loadBaseSubject(subjectId);
    }

    /**
     *
     * @param enterpriseSubject
     * @return
     * @throws EMPIException
     */
    public List<SubjectCrossReference> loadEnterpriseSubjectCrossReferences(Subject enterpriseSubject) throws EMPIException {
        return this.loadEnterpriseSubjectCrossReferences(enterpriseSubject.getInternalId());
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public List<SubjectCrossReference> loadEnterpriseSubjectCrossReferences(String enterpriseSubjectId) throws EMPIException {
        SubjectCrossReferenceDAO dao = new SubjectCrossReferenceDAO(connection);
        return dao.loadEnterpriseSubjectCrossReferences(enterpriseSubjectId);
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public String getLastUpdatedSystemSubjectId(String enterpriseSubjectId) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        return dao.getLastUpdatedSystemSubjectId(enterpriseSubjectId);
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public Subject loadEnterpriseSubject(String enterpriseSubjectId) throws EMPIException {
        // Load subject.
        Subject enterpriseSubject = this.loadSubject(enterpriseSubjectId);

        // Get cross references to the subject ...
        this.loadEnterpriseSubjectCrossReferencedIdentifiers(enterpriseSubject);

        return enterpriseSubject;
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public Subject loadEnterpriseSubjectIdentifiersOnly(String enterpriseSubjectId) throws EMPIException {
        return this.loadEnterpriseSubjectIdentifiersOnly(enterpriseSubjectId, false);
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public Subject loadEnterpriseSubjectIdentifiersAndNamesOnly(String enterpriseSubjectId) throws EMPIException {
        return this.loadEnterpriseSubjectIdentifiersOnly(enterpriseSubjectId, true);
    }

    /**
     *
     * @param enterpriseSubjectId
     * @param loadNames
     * @return
     * @throws EMPIException
     */
    private Subject loadEnterpriseSubjectIdentifiersOnly(String enterpriseSubjectId, boolean loadNames) throws EMPIException {
        // Create enterprise subject.
        Subject enterpriseSubject = new Subject();
        enterpriseSubject.setInternalId(enterpriseSubjectId);
        enterpriseSubject.setType(Subject.SubjectType.ENTERPRISE);

        // Load enterprise subject identifiers.
        List<SubjectIdentifier> enterpriseSubjectIdentifiers = this.loadSubjectIdentifiers(enterpriseSubjectId);
        enterpriseSubject.getSubjectIdentifiers().addAll(enterpriseSubjectIdentifiers);

        // Load cross references for the enterprise subject.
        this.loadEnterpriseSubjectCrossReferencedIdentifiers(enterpriseSubject);

        // Load names if required.
        if (loadNames) {
            SubjectNameDAO subjectNameDAO = new SubjectNameDAO(connection);
            subjectNameDAO.load(enterpriseSubject);
        }
        return enterpriseSubject;
    }

    /**
     * 
     * @param enterpriseSubject
     * @throws EMPIException
     */
    public void loadEnterpriseSubjectCrossReferencedIdentifiers(Subject enterpriseSubject) throws EMPIException {

        // Load cross references for the enterpriseSubject.
        List<SubjectCrossReference> subjectCrossReferences = this.loadEnterpriseSubjectCrossReferences(enterpriseSubject);

        // Loop through each cross reference.
        for (SubjectCrossReference subjectCrossReference : subjectCrossReferences) {

            // Load list of subject identifiers for the cross reference.
            List<SubjectIdentifier> subjectIdentifiers = this.loadSubjectIdentifiers(subjectCrossReference.getSystemSubjectId());

            // FIXME: Can we have dups here (as we may in "other ids")???

            // Add subject identifiers to the given enterprise subject.
            enterpriseSubject.getSubjectIdentifiers().addAll(subjectIdentifiers);

            // Load list of other identifiers for the cross reference.
            List<SubjectIdentifier> subjectOtherIdentifiers = this.loadSubjectOtherIdentifiers(subjectCrossReference.getSystemSubjectId());

            // NOTE: Duplicates (across system-level subjects) of other ids is allowed, yet we remove them here).
            for (SubjectIdentifier subjectOtherIdentifier : subjectOtherIdentifiers) {
                if (!enterpriseSubject.hasSubjectOtherIdentifier(subjectOtherIdentifier)) {
                    enterpriseSubject.getSubjectOtherIdentifiers().add(subjectOtherIdentifier);
                }
            }
        }
    }

    /**
     * 
     * @param systemSubject
     * @return
     * @throws EMPIException
     */
    public String getEnterpriseSubjectId(Subject systemSubject) throws EMPIException {
        return this.getEnterpriseSubjectId(systemSubject.getInternalId());
    }

    /**
     *
     * @param systemSubjectId
     * @return
     * @throws EMPIException
     */
    public String getEnterpriseSubjectId(String systemSubjectId) throws EMPIException {
        SubjectCrossReferenceDAO dao = new SubjectCrossReferenceDAO(connection);
        return dao.getEnterpriseSubjectId(systemSubjectId);
    }

    /**
     * 
     * @param subjectIdentifiers
     * @return
     * @throws EMPIException
     */
    public boolean doesSubjectExist(List<SubjectIdentifier> subjectIdentifiers) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        return dao.doesSubjectExist(subjectIdentifiers);
    }

    /**
     *
     * @param subjectIdentifier
     * @return
     * @throws EMPIException
     */
    public int getSubjectIdentifierDomainId(SubjectIdentifier subjectIdentifier) throws EMPIException {
        SubjectIdentifierDomainDAO dao = new SubjectIdentifierDomainDAO(connection);
        SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
        return dao.getId(subjectIdentifierDomain);
    }

    /**
     * 
     * @param subjectIdentifierDomain
     * @return
     * @throws EMPIException
     */
    public int getSubjectIdentifierDomainId(SubjectIdentifierDomain subjectIdentifierDomain) throws EMPIException {
        SubjectIdentifierDomainDAO dao = new SubjectIdentifierDomainDAO(connection);
        return dao.getId(subjectIdentifierDomain);
    }

    /**
     *
     * @param subjectIdentifier
     * @return
     * @throws EMPIException
     */
    public boolean doesSubjectIdentifierDomainExist(SubjectIdentifier subjectIdentifier) throws EMPIException {
        int subjectIdentifierDomainId = this.getSubjectIdentifierDomainId(subjectIdentifier);
        return subjectIdentifierDomainId != -1;
    }

    /**
     * 
     * @param subjectIdentifierDomain
     * @return
     * @throws EMPIException
     */
    public boolean doesSubjectIdentifierDomainExist(SubjectIdentifierDomain subjectIdentifierDomain) throws EMPIException {
        int subjectIdentifierDomainId = this.getSubjectIdentifierDomainId(subjectIdentifierDomain);
        return subjectIdentifierDomainId != -1;
    }

    /**
     *
     * @param subject
     * @throws EMPIException
     */
    public void insertSubject(Subject subject) throws EMPIException {
        List<Subject> subjects = new ArrayList<Subject>();
        subjects.add(subject);
        this.insertSubjects(subjects);
    }

    /**
     * 
     * @param subjects
     * @throws EMPIException
     */
    public void insertSubjects(List<Subject> subjects) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        dao.insert(subjects);
    }

    /**
     * 
     * @param targetEnterpriseSubjectId
     * @param subject
     * @throws EMPIException
     */
    public void updateEnterpriseSubject(String targetEnterpriseSubjectId, Subject subject) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        dao.updateEnterpriseSubject(targetEnterpriseSubjectId, subject);
    }

    /**
     *
     * @param systemSubjectId
     * @param enterpriseSubjectId
     * @param matchScore
     * @throws EMPIException
     */
    public void insertSubjectCrossReference(String systemSubjectId, String enterpriseSubjectId, int matchScore) throws EMPIException {
        SubjectCrossReference subjectCrossReference = new SubjectCrossReference();
        subjectCrossReference.setMatchScore(matchScore);
        subjectCrossReference.setSystemSubjectId(systemSubjectId);
        subjectCrossReference.setEnterpriseSubjectId(enterpriseSubjectId);
        this.insertSubjectCrossReference(subjectCrossReference);
    }

    /**
     *
     * @param subjectCrossReference
     * @throws EMPIException
     */
    public void insertSubjectCrossReference(SubjectCrossReference subjectCrossReference) throws EMPIException {
        SubjectCrossReferenceDAO dao = new SubjectCrossReferenceDAO(connection);
        dao.insert(subjectCrossReference);
    }

    /**
     *
     * @param record
     * @throws EMPIException
     */
    public void insertSubjectMatchFields(Record record) throws EMPIException {
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        this.insertSubjectMatchFields(records);
    }

    /**
     *
     * @param records
     * @throws EMPIException
     */
    public void insertSubjectMatchFields(List<Record> records) throws EMPIException {
        SubjectMatchFieldsDAO dao = new SubjectMatchFieldsDAO(connection);
        dao.insert(records);
    }

    /**
     * 
     * @param survivingEnterpriseSubjectId
     * @param subsumedEnterpriseSubjectId
     * @throws EMPIException
     */
    public void mergeEnterpriseSubjects(String survivingEnterpriseSubjectId, String subsumedEnterpriseSubjectId) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        dao.mergeEnterpriseSubjects(survivingEnterpriseSubjectId, subsumedEnterpriseSubjectId);
    }

    /**
     *
     * @param enterpriseSubjectId
     * @throws EMPIException
     */
    /*
    public void voidEnterpriseSubject(String enterpriseSubjectId) throws EMPIException {
    SubjectDAO dao = new SubjectDAO(connection);
    dao.voidEnterpriseSubject(enterpriseSubjectId);
    }*/
    /**
     *
     * @param subject
     * @throws EMPIException
     */
    public void deleteSubject(Subject subject) throws EMPIException {
        this.deleteSubject(subject.getInternalId(), subject.getType());
    }

    /**
     * 
     * @param subjectId 
     * @param subjectType
     * @throws EMPIException
     */
    public void deleteSubject(String subjectId, Subject.SubjectType subjectType) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        dao.deleteSubject(subjectId, subjectType);
    }

    /**
     * 
     * @param subjectIdentifier
     * @throws EMPIException
     */
    public void deleteSubjectIdentifier(SubjectIdentifier subjectIdentifier) throws EMPIException {
        this.deleteSubjectIdentifier(subjectIdentifier.getInternalId());
    }

    /**
     *
     * @param subjectIdentifierId
     * @throws EMPIException
     */
    public void deleteSubjectIdentifier(String subjectIdentifierId) throws EMPIException {
        SubjectIdentifierDAO dao = new SubjectIdentifierDAO(connection);
        dao.deleteSubjectIdentifier(subjectIdentifierId);
    }

    /**
     *
     * @param searchRecord
     * @param matchType
     * @return
     * @throws EMPIException
     */
    public List<Record> findCandidates(Record searchRecord, MatchType matchType) throws EMPIException {
        SubjectMatchFieldsDAO dao = new SubjectMatchFieldsDAO(connection);
        return dao.findCandidates(searchRecord, matchType);
    }
}
