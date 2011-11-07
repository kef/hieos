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
     * @param subjectIdentifier
     * @return
     * @throws EMPIException
     */
    public Subject loadSubjectBaseByIdentifier(SubjectIdentifier subjectIdentifier) throws EMPIException {
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
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public List<SubjectCrossReference> loadSubjectCrossReferences(String enterpriseSubjectId) throws EMPIException {
        SubjectCrossReferenceDAO dao = new SubjectCrossReferenceDAO(connection);
        return dao.load(enterpriseSubjectId);
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
     * @param subjectIdentifiers
     * @return
     * @throws EMPIException
     */
    public List<Subject> findSubjectsByIdentifiers(List<SubjectIdentifier> subjectIdentifiers) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        return dao.findSubjectsByIdentifiers(subjectIdentifiers);
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
     * @param subjectCrossReference
     * @throws EMPIException
     */
    public void insertSubjectCrossReference(
            SubjectCrossReference subjectCrossReference) throws EMPIException {
        SubjectCrossReferenceDAO dao = new SubjectCrossReferenceDAO(connection);
        dao.insert(subjectCrossReference);
    }

    /**
     *
     * @param record
     * @throws EMPIException
     */
    public void insertSubjectMatchRecord(Record record) throws EMPIException {
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        this.insertSubjectMatchRecords(records);
    }

    /**
     *
     * @param records
     * @throws EMPIException
     */
    public void insertSubjectMatchRecords(List<Record> records) throws EMPIException {
        SubjectMatchDAO dao = new SubjectMatchDAO(connection);
        dao.insert(records);
    }

    /**
     * 
     * @param survivingSubjectId
     * @param subsumedSubjectId
     * @throws EMPIException
     */
    public void mergeSubjects(String survivingSubjectId, String subsumedSubjectId) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(connection);
        dao.merge(survivingSubjectId, subsumedSubjectId);
    }

    /**
     * 
     * @param searchRecord
     * @return
     * @throws EMPIException
     */
    public List<Record> lookup(Record searchRecord) throws EMPIException {
        SubjectMatchDAO dao = new SubjectMatchDAO(connection);
        return dao.lookup(searchRecord);
    }
}
