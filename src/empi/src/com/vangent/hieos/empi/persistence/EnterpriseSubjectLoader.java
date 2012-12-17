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
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.match.ScoredRecord;
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class EnterpriseSubjectLoader {

    private List<Subject> enterpriseSubjects = new ArrayList<Subject>();
    private PersistenceManager persistenceManager;
    private boolean loadFullSubjects = true;

    /**
     *
     */
    private EnterpriseSubjectLoader() {
        // Do not allow.
    }

    /**
     *
     * @param persistenceManager
     * @param loadFullSubjects
     */
    public EnterpriseSubjectLoader(PersistenceManager persistenceManager, boolean loadFullSubjects) {
        this.persistenceManager = persistenceManager;
        this.loadFullSubjects = loadFullSubjects;
    }

    /**
     *
     * @return
     */
    public List<Subject> getEnterpriseSubjects() {
        return enterpriseSubjects;
    }

    /**
     *
     * @param systemSubjectId
     * @return
     * @throws EMPIException
     */
    public EnterpriseSubjectLoaderResult loadEnterpriseSubject(InternalId systemSubjectId) throws EMPIException {
        // Get enterpiseSubjectId for the system-level subject.
        EnterpriseSubjectController enterpriseSubjectController = new EnterpriseSubjectController(persistenceManager);
        InternalId enterpriseSubjectId = enterpriseSubjectController.getEnterpriseSubjectId(systemSubjectId);

        // Return cached enterprise subject.
        return this.getEnterpriseSubject(enterpriseSubjectId);
    }

    /**
     *
     * @param baseSubject
     * @return
     * @throws EMPIException
     */
    public EnterpriseSubjectLoaderResult loadEnterpriseSubject(Subject baseSubject) throws EMPIException {
        InternalId enterpriseSubjectId;
        if (baseSubject.getType().equals(Subject.SubjectType.ENTERPRISE)) {
            enterpriseSubjectId = baseSubject.getInternalId();
        } else {
            // Get enterpiseSubjectId for the system-level subject.
            EnterpriseSubjectController enterpriseSubjectController = new EnterpriseSubjectController(persistenceManager);
            enterpriseSubjectId = enterpriseSubjectController.getEnterpriseSubjectId(baseSubject);
        }

        // See if we already know this enterprise subject.
        return this.getEnterpriseSubject(enterpriseSubjectId);
    }
    
    /**
     * 
     * @param baseSubjects
     * @throws EMPIException 
     */
    public void loadEnterpriseSubjects(List<Subject> baseSubjects) throws EMPIException {
        // Load unique enterprise subjects.
        for (Subject baseSubject : baseSubjects) {
            EnterpriseSubjectLoaderResult loaderResult = this.loadEnterpriseSubject(baseSubject);
            loaderResult.getEnterpriseSubject().setMatchConfidencePercentage(100);
        }
    }
    
    /**
     * 
     * @param matchedRecords
     * @throws EMPIException 
     */
    public void loadEnterpriseSubjectsForMatchedRecords(List<ScoredRecord> matchedRecords) throws EMPIException
    {
        // Go through each record and load the enterprise subject and set the match confidence percentage.
        for (ScoredRecord scoredRecord : matchedRecords) {
            Record record = scoredRecord.getRecord();
            InternalId systemSubjectId = record.getInternalId();
            EnterpriseSubjectLoaderResult loaderResult = this.loadEnterpriseSubject(systemSubjectId);
            if (!loaderResult.isAlreadyExists()) {
                // Only set match confidence percentage on first load.
                Subject enterpriseSubject = loaderResult.getEnterpriseSubject();
                int matchConfidencePercentage = scoredRecord.getMatchScorePercentage();
                enterpriseSubject.setMatchConfidencePercentage(matchConfidencePercentage);
            }
        }
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    private EnterpriseSubjectLoaderResult getEnterpriseSubject(InternalId enterpriseSubjectId) throws EMPIException {
        EnterpriseSubjectLoaderResult loadResult = new EnterpriseSubjectLoaderResult();
        loadResult.setEnterpriseSubject(null);

        // See if enterprise subject is in list already.
        for (Subject currentEnterpriseSubject : enterpriseSubjects) {
            InternalId currentEnterpriseSubjectId = currentEnterpriseSubject.getInternalId();
            if (currentEnterpriseSubjectId.equals(enterpriseSubjectId)) {
                loadResult.setEnterpriseSubject(currentEnterpriseSubject);
                loadResult.setAlreadyExists(true);
                break;  // Found.
            }
        }
        if (loadResult.getEnterpriseSubject() == null) {
            // .. not in list, load it.
            Subject enterpriseSubject;
            EnterpriseSubjectController enterpriseSubjectController = new EnterpriseSubjectController(persistenceManager);
            if (loadFullSubjects) {
                // Load full enterprise subject.
                enterpriseSubject = enterpriseSubjectController.load(enterpriseSubjectId);
            } else {
                // Load enterprise subject (id's only).
                enterpriseSubject = enterpriseSubjectController.loadSubjectIdentifiersOnly(enterpriseSubjectId);
            }
            loadResult.setAlreadyExists(false);
            loadResult.setEnterpriseSubject(enterpriseSubject);
            enterpriseSubjects.add(enterpriseSubject);
        }
        return loadResult;
    }
}
