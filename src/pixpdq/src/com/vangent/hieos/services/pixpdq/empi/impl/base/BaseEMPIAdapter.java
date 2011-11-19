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
package com.vangent.hieos.services.pixpdq.empi.impl.base;

import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.services.pixpdq.empi.api.EMPIAdapter;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.SubjectMergeRequest;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class BaseEMPIAdapter implements EMPIAdapter {

    private static final Logger logger = Logger.getLogger(BaseEMPIAdapter.class);
    private XConfigActor configActor = null;

    /**
     * 
     * @param configActor
     */
    public void setConfig(XConfigActor configActor) {
        this.configActor = configActor;
    }

    /**
     *
     * @return
     */
    private XConfigActor getConfigActor() {
        return configActor;
    }

    /**
     *
     * @param classLoader
     */
    @Override
    public void startup(ClassLoader classLoader) {
        // Do nothing here.
    }

    /**
     * 
     * @param subject
     * @return
     * @throws EMPIException
     */
    @Override
    public Subject addSubject(Subject subject) throws EMPIException {
        PersistenceManager pm = new PersistenceManager();
        String enterpriseSubjectId = null;
        try {
            pm.open();  // Open transaction.
            AddSubjectHandler addSubjectHandler = new AddSubjectHandler(this.getConfigActor(), pm);
            enterpriseSubjectId = addSubjectHandler.addSubject(subject);
            pm.commit();
        } catch (EMPIException ex) {
            pm.rollback();
            throw ex; // Rethrow.
        } catch (Exception ex) {
            pm.rollback();
            throw new EMPIException(ex);
        } finally {
            pm.close();  // To be sure.
        }
        //this.sendUpdateNotifications(enterpriseSubjectId);
        return subject;
    }

    /**
     *
     * @param subject
     * @return
     * @throws EMPIException
     */
    @Override
    public Subject updateSubject(Subject subject) throws EMPIException {
        PersistenceManager pm = new PersistenceManager();
        String enterpriseSubjectId = null;
        try {
            pm.open();  // Open transaction.
            UpdateSubjectHandler updateSubjectHandler = new UpdateSubjectHandler(this.getConfigActor(), pm);
            enterpriseSubjectId = updateSubjectHandler.updateSubject(subject);
            pm.commit();
        } catch (EMPIException ex) {
            pm.rollback();
            throw ex; // Rethrow.
        } catch (Exception ex) {
            pm.rollback();
            throw new EMPIException(ex);
        } finally {
            pm.close();  // To be sure.
        }
        //this.sendUpdateNotifications(enterpriseSubjectId);
        return subject;
    }

    /**
     * 
     * @param subjectMergeRequest
     * @return
     * @throws EMPIException
     */
    @Override
    public Subject mergeSubjects(SubjectMergeRequest subjectMergeRequest) throws EMPIException {
        PersistenceManager pm = new PersistenceManager();
        Subject survivingSubject = null;
        try {
            pm.open();  // Open transaction.
            MergeSubjectsHandler mergeSubjectsHandler = new MergeSubjectsHandler(this.getConfigActor(), pm);
            survivingSubject = mergeSubjectsHandler.mergeSubjects(subjectMergeRequest);
            pm.commit();
        } catch (EMPIException ex) {
            pm.rollback();
            throw ex; // Rethrow.
        } catch (Exception ex) {
            pm.rollback();
            throw new EMPIException(ex);
        } finally {
            pm.close();  // To be sure.
        }
        return survivingSubject; // FIXME!!!!
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    @Override
    public SubjectSearchResponse findSubjects(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        PersistenceManager pm = new PersistenceManager();
        try {
            pm.open();
            FindSubjectsHandler findSubjectsHandler = new FindSubjectsHandler(this.getConfigActor(), pm);
            subjectSearchResponse = findSubjectsHandler.findSubjects(subjectSearchCriteria);
        } finally {
            pm.close();  // No matter what.
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    @Override
    public SubjectSearchResponse findSubjectByIdentifier(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        PersistenceManager pm = new PersistenceManager();
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        try {
            pm.open();
            FindSubjectsHandler findSubjectsHandler = new FindSubjectsHandler(this.getConfigActor(), pm);
            subjectSearchResponse = findSubjectsHandler.findSubjectByIdentifier(subjectSearchCriteria);
        } finally {
            pm.close();
        }
        return subjectSearchResponse;
    }
}
