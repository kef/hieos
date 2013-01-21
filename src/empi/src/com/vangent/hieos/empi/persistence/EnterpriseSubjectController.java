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

import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.config.EUIDConfig;
import com.vangent.hieos.empi.euid.EUIDGenerator;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.model.SubjectCrossReference;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.persistence.SubjectCrossReferenceDAO;
import com.vangent.hieos.empi.persistence.SubjectDAO;
import com.vangent.hieos.empi.persistence.SubjectNameDAO;
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class EnterpriseSubjectController {

    private static final Logger logger = Logger.getLogger(EnterpriseSubjectController.class);
    private PersistenceManager persistenceManager;

    /**
     *
     * @param persistenceManager
     */
    public EnterpriseSubjectController(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

   
    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public Subject load(InternalId enterpriseSubjectId) throws EMPIException {
        // Load enterprise subject.
        SubjectController subjectController = new SubjectController(persistenceManager);
        Subject enterpriseSubject = subjectController.load(enterpriseSubjectId);

        // load enterprise subject cross references.
        this.loadSubjectCrossReferencedIdentifiers(enterpriseSubject);

        return enterpriseSubject;
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public Subject loadSubjectIdentifiersAndNamesOnly(InternalId enterpriseSubjectId) throws EMPIException {
        return this.loadSubjectIdentifiersOnly(enterpriseSubjectId, true);
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public Subject loadSubjectIdentifiersOnly(InternalId enterpriseSubjectId) throws EMPIException {
        return this.loadSubjectIdentifiersOnly(enterpriseSubjectId, false);
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public List<SubjectCrossReference> loadSubjectCrossReferences(InternalId enterpriseSubjectId) throws EMPIException {
        SubjectCrossReferenceDAO dao = new SubjectCrossReferenceDAO(persistenceManager);
        return dao.loadEnterpriseSubjectCrossReferences(enterpriseSubjectId);
    }

    /**
     *
     * @param systemSubject
     * @return
     * @throws EMPIException
     */
    public InternalId getEnterpriseSubjectId(Subject systemSubject) throws EMPIException {
        return this.getEnterpriseSubjectId(systemSubject.getInternalId());
    }

    /**
     *
     * @param systemSubjectId
     * @return
     * @throws EMPIException
     */
    public InternalId getEnterpriseSubjectId(InternalId systemSubjectId) throws EMPIException {
        SubjectCrossReferenceDAO dao = new SubjectCrossReferenceDAO(persistenceManager);
        return dao.getEnterpriseSubjectId(systemSubjectId);
    }

    /**
     *
     * @param enterpriseSubjectId
     * @return
     * @throws EMPIException
     */
    public InternalId getLastUpdatedSystemSubjectId(InternalId enterpriseSubjectId) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(persistenceManager);
        return dao.getLastUpdatedSystemSubjectId(enterpriseSubjectId);
    }

     /**
     *
     * @param newEnterpriseSubject
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public InternalId insert(Subject newEnterpriseSubject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Build (from received subject) and store enterprise-level subject.
        newEnterpriseSubject.setType(Subject.SubjectType.ENTERPRISE);

        // Clear out the subject's identifier lists (since they are already stored at the system-level).
        newEnterpriseSubject.clearIdentifiers();

        // HACK:
        newEnterpriseSubject.setIdentitySource("EMPI");

        // Stamp the subject with an enterprise id (if configured to do so).
        EUIDConfig euidConfig = empiConfig.getEuidConfig();
        if (euidConfig.isEuidAssignEnabled()) {
            SubjectIdentifier enterpriseSubjectIdentifier = EUIDGenerator.getEUID();
            newEnterpriseSubject.addSubjectIdentifier(enterpriseSubjectIdentifier);
        }

        // Store the enterprise-level subject.
        SubjectController subjectController = new SubjectController(persistenceManager);
        subjectController.insert(newEnterpriseSubject);
        return newEnterpriseSubject.getInternalId();
    }

    /**
     *
     * @param targetEnterpriseSubjectId
     * @param subject
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public void update(InternalId targetEnterpriseSubjectId, Subject subject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        SubjectDAO dao = new SubjectDAO(persistenceManager);
        dao.updateEnterpriseSubject(targetEnterpriseSubjectId, subject);
    }

    /**
     *
     * @param survivingEnterpriseSubjectId
     * @param subsumedEnterpriseSubjectId
     * @throws EMPIException
     */
    public void merge(InternalId survivingEnterpriseSubjectId, InternalId subsumedEnterpriseSubjectId) throws EMPIException {
        SubjectDAO dao = new SubjectDAO(persistenceManager);
        dao.mergeEnterpriseSubjects(survivingEnterpriseSubjectId, subsumedEnterpriseSubjectId);
    }

    /**
     *
     * @param systemSubjectId
     * @param enterpriseSubjectId
     * @param matchScore
     * @throws EMPIException
     */
    public void insertSubjectCrossReference(InternalId systemSubjectId, InternalId enterpriseSubjectId, int matchScore) throws EMPIException {
        SubjectCrossReference subjectCrossReference = new SubjectCrossReference();
        subjectCrossReference.setMatchScore(matchScore);
        subjectCrossReference.setSystemSubjectId(systemSubjectId);
        subjectCrossReference.setEnterpriseSubjectId(enterpriseSubjectId);
        SubjectCrossReferenceDAO dao = new SubjectCrossReferenceDAO(persistenceManager);
        dao.insert(subjectCrossReference);
    }

    /**
     *
     * @param enterpriseSubjectId
     * @param loadNames
     * @return
     * @throws EMPIException
     */
    private Subject loadSubjectIdentifiersOnly(InternalId enterpriseSubjectId, boolean loadNames) throws EMPIException {
        // Create enterprise subject.
        Subject enterpriseSubject = new Subject();
        enterpriseSubject.setInternalId(enterpriseSubjectId);
        enterpriseSubject.setType(Subject.SubjectType.ENTERPRISE);

        // Load enterprise subject identifiers.
        SubjectController subjectController = new SubjectController(persistenceManager);
        List<SubjectIdentifier> enterpriseSubjectIdentifiers = subjectController.loadSubjectIdentifiers(enterpriseSubjectId);
        enterpriseSubject.getSubjectIdentifiers().addAll(enterpriseSubjectIdentifiers);

        // Load cross references for the enterprise subject.
        this.loadSubjectCrossReferencedIdentifiers(enterpriseSubject);

        // Load names if required.
        if (loadNames) {
            SubjectNameDAO subjectNameDAO = new SubjectNameDAO(persistenceManager);
            subjectNameDAO.load(enterpriseSubject);
        }
        return enterpriseSubject;
    }

    /**
     *
     * @param enterpriseSubject
     * @throws EMPIException
     */
    private void loadSubjectCrossReferencedIdentifiers(Subject enterpriseSubject) throws EMPIException {

        // Load cross references for the enterpriseSubject.
        List<SubjectCrossReference> subjectCrossReferences = this.loadSubjectCrossReferences(enterpriseSubject);

        // Loop through each cross reference.
        SubjectController subjectController = new SubjectController(persistenceManager);
        for (SubjectCrossReference subjectCrossReference : subjectCrossReferences) {

            // Load list of subject identifiers for the cross reference.
            List<SubjectIdentifier> subjectIdentifiers = subjectController.loadSubjectIdentifiers(subjectCrossReference.getSystemSubjectId());

            // FIXME: Can we have dups here (as we may in "other ids")???

            // Add subject identifiers to the given enterprise subject.
            enterpriseSubject.getSubjectIdentifiers().addAll(subjectIdentifiers);

            // Load list of other identifiers for the cross reference.
            List<SubjectIdentifier> subjectOtherIdentifiers = subjectController.loadSubjectOtherIdentifiers(subjectCrossReference.getSystemSubjectId());

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
     * @param enterpriseSubject
     * @return
     * @throws EMPIException
     */
    private List<SubjectCrossReference> loadSubjectCrossReferences(Subject enterpriseSubject) throws EMPIException {
        return this.loadSubjectCrossReferences(enterpriseSubject.getInternalId());
    }
}
