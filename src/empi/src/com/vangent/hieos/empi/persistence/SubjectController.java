/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.persistence;

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.match.MatchAlgorithm;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.persistence.SubjectDAO;
import com.vangent.hieos.empi.persistence.SubjectIdentifierDAO;
import com.vangent.hieos.empi.persistence.SubjectMatchFieldsDAO;
import com.vangent.hieos.empi.persistence.SubjectReviewItemDAO;
import com.vangent.hieos.empi.subjectreview.model.SubjectReviewItem;
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectController {

    private static final Logger logger = Logger.getLogger(SubjectController.class);
    private PersistenceManager persistenceManager;

    /**
     *
     * @param persistenceManager
     */
    public SubjectController(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public Subject load(InternalId subjectId) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(persistenceManager);
        return dao.load(subjectId);
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public List<SubjectIdentifier> loadSubjectIdentifiers(InternalId subjectId) throws EMPIException {
        SubjectIdentifierDAO dao = new SubjectIdentifierDAO(persistenceManager);
        return dao.load(subjectId, SubjectIdentifier.Type.PID);
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public List<SubjectIdentifier> loadSubjectOtherIdentifiers(InternalId subjectId) throws EMPIException {
        SubjectIdentifierDAO dao = new SubjectIdentifierDAO(persistenceManager);
        return dao.load(subjectId, SubjectIdentifier.Type.OTHER);
    }

    /**
     *
     * @param subjectIdentifier
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public List<Subject> loadBaseSubjects(SubjectIdentifier subjectIdentifier) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        List<SubjectIdentifier> subjectIdentifiers = new ArrayList<SubjectIdentifier>();
        subjectIdentifiers.add(subjectIdentifier);
        return this.loadBaseSubjects(subjectIdentifiers);
    }

    /**
     *
     * @param subjectIdentifiers
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public List<Subject> loadBaseSubjects(List<SubjectIdentifier> subjectIdentifiers) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        SubjectDAO dao = new SubjectDAO(persistenceManager);
        return dao.loadBaseSubjectsByIdentifier(subjectIdentifiers);
    }

    /**
     *
     * @param subjectId
     * @return
     * @throws EMPIException
     */
    public Subject loadBaseSubject(InternalId subjectId) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(persistenceManager);
        return dao.loadBaseSubject(subjectId);
    }

    /**
     *
     * @param subjectIdentifiers
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public boolean doesSubjectExist(List<SubjectIdentifier> subjectIdentifiers) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        SubjectDAO dao = new SubjectDAO(persistenceManager);
        return dao.doesSubjectExist(subjectIdentifiers);
    }

    /**
     *
     * @param subject
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public void insert(Subject subject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        List<Subject> subjects = new ArrayList<Subject>();
        subjects.add(subject);
        SubjectDAO dao = new SubjectDAO(persistenceManager);
        dao.insert(subjects);
    }
    
      /**
     *
     * @param record
     * @throws EMPIException
     */
    public void insert(Record record) throws EMPIException {
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        SubjectMatchFieldsDAO dao = new SubjectMatchFieldsDAO(persistenceManager);
        dao.insert(records);
    }
    
     /**
     *
     * @param subjectId
     * @param matchType
     * @return
     * @throws EMPIException
     */
    public Record load(InternalId subjectId, MatchAlgorithm.MatchType matchType) throws EMPIException {
        SubjectMatchFieldsDAO dao = new SubjectMatchFieldsDAO(persistenceManager);
        return dao.load(subjectId, matchType);
    }
    
    
    /**
     *
     * @param subjectReviewItems
     * @throws EMPIException
     */
    public void insertSubjecReviewItems(List<SubjectReviewItem> subjectReviewItems) throws EMPIException {
        SubjectReviewItemDAO dao = new SubjectReviewItemDAO(persistenceManager);
        dao.insert(subjectReviewItems);
    }

    /**
     *
     * @param subject
     * @throws EMPIException
     */
    public void delete(Subject subject) throws EMPIException {
        this.delete(subject.getInternalId(), subject.getType());
    }

    /**
     *
     * @param subjectId
     * @param subjectType
     * @throws EMPIException
     */
    public void delete(InternalId subjectId, Subject.SubjectType subjectType) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(persistenceManager);
        dao.deleteSubject(subjectId, subjectType);
    }

    /**
     *
     * @param subjectIdentifierId
     * @throws EMPIException
     */
    public void deleteSubjectIdentifier(InternalId subjectIdentifierId) throws EMPIException {
        SubjectIdentifierDAO dao = new SubjectIdentifierDAO(persistenceManager);
        dao.deleteSubjectIdentifier(subjectIdentifierId);
    }
}
