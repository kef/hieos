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
package com.vangent.hieos.empi.impl.base;

import com.vangent.hieos.empi.adapter.EMPIAdapter;
import com.vangent.hieos.empi.adapter.EMPINotification;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownSubjectIdentifier;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.query.cache.QueryCache;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectMergeRequest;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class BaseEMPIAdapter implements EMPIAdapter {

    private static final Logger logger = Logger.getLogger(BaseEMPIAdapter.class);
    private DeviceInfo senderDeviceInfo = null;

    /**
     *
     * @param senderDeviceInfo
     */
    @Override
    public void setSenderDeviceInfo(DeviceInfo senderDeviceInfo) {
        this.senderDeviceInfo = senderDeviceInfo;
    }

    /**
     * 
     * @return
     */
    @Override
    public DeviceInfo getSenderDeviceInfo() {
        return this.senderDeviceInfo;
    }

    /**
     * 
     * @param subject
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     * @throws EMPIExceptionUnknownSubjectIdentifier
     */
    @Override
    public EMPINotification addSubject(Subject subject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier {
        PersistenceManager pm = new PersistenceManager();
        EMPINotification updateNotificationContent = null;
        boolean success = false;

        try {
            pm.open();  // Open transaction.
            AddSubjectHandler addSubjectHandler = new AddSubjectHandler(pm, this.senderDeviceInfo);
            updateNotificationContent = addSubjectHandler.addSubject(subject);
            pm.commit();
            success = true;
        } finally {
            if (success) {
                pm.close();  // To be sure.
            } else {
                pm.rollback();
            }
        }
        return updateNotificationContent;
    }

    /**
     * 
     * @param subject
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     * @throws EMPIExceptionUnknownSubjectIdentifier
     */
    @Override
    public EMPINotification updateSubject(Subject subject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier {
        PersistenceManager pm = new PersistenceManager();
        EMPINotification updateNotificationContent = null;
        boolean success = false;

        try {
            pm.open();  // Open transaction.
            UpdateSubjectHandler updateSubjectHandler = new UpdateSubjectHandler(pm, this.senderDeviceInfo);
            updateNotificationContent = updateSubjectHandler.updateSubject(subject);
            pm.commit();
            success = true;
        } finally {
            if (success) {
                pm.close();  // To be sure.
            } else {
                pm.rollback();
            }
        }
        return updateNotificationContent;
    }

    /**
     *
     * @param subjectMergeRequest
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownSubjectIdentifier
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    @Override
    public EMPINotification mergeSubjects(SubjectMergeRequest subjectMergeRequest) throws EMPIException, EMPIExceptionUnknownSubjectIdentifier, EMPIExceptionUnknownIdentifierDomain {
        PersistenceManager pm = new PersistenceManager();
        EMPINotification updateNotificationContent = null;
        boolean success = false;

        try {
            pm.open();  // Open transaction.
            MergeSubjectsHandler mergeSubjectsHandler = new MergeSubjectsHandler(pm, this.senderDeviceInfo);
            updateNotificationContent = mergeSubjectsHandler.mergeSubjects(subjectMergeRequest);
            pm.commit();
            success = true;
        } finally {
            if (success) {
                pm.close();  // To be sure.
            } else {
                pm.rollback();
            }
        }
        return updateNotificationContent;
    }

    /**
     * 
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     * @throws EMPIExceptionUnknownSubjectIdentifier
     */
    @Override
    public SubjectSearchResponse findSubjects(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        PersistenceManager pm = new PersistenceManager();
        boolean success = false;
        try {
            pm.open();
            FindSubjectsHandler findSubjectsHandler = new FindSubjectsHandler(pm, this.senderDeviceInfo);
            subjectSearchResponse = findSubjectsHandler.findSubjects(subjectSearchCriteria);
            success = true;
        } finally {
            if (success) {
                pm.close();  // To be sure.
            } else {
                pm.rollback();
            }
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     * @throws EMPIExceptionUnknownSubjectIdentifier
     */
    @Override
    public SubjectSearchResponse getBySubjectIdentifiers(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier {
        PersistenceManager pm = new PersistenceManager();
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        boolean success = false;
        try {
            pm.open();
            FindSubjectsHandler findSubjectsHandler = new FindSubjectsHandler(pm, this.senderDeviceInfo);
            subjectSearchResponse = findSubjectsHandler.getBySubjectIdentifiers(subjectSearchCriteria);
            success = true;
        } finally {
            if (success) {
                pm.close();  // To be sure.
            } else {
                pm.rollback();
            }
        }
        return subjectSearchResponse;
    }

    /**
     * 
     * @param queryId
     * @throws EMPIException
     */
    public void cancelQuery(String queryId) throws EMPIException {
        QueryCache queryCache = QueryCache.getInstance();
        queryCache.cancelQuery(queryId);
    }
}
