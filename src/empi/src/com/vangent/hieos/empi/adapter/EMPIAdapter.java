/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.adapter;

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownSubjectIdentifier;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectMergeRequest;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;

/**
 *
 * @author Bernie Thuman
 */
public interface EMPIAdapter {

    /**
     *
     * @param senderDeviceInfo
     */
    public void setSenderDeviceInfo(DeviceInfo senderDeviceInfo);

    /**
     * 
     * @return
     */
    public DeviceInfo getSenderDeviceInfo();

    /**
     * 
     * @param subject
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     * @throws EMPIExceptionUnknownSubjectIdentifier
     */
    public EMPINotification addSubject(Subject subject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier;

    /**
     * 
     * @param subject
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     * @throws EMPIExceptionUnknownSubjectIdentifier
     */
    public EMPINotification updateSubject(Subject subject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier;

    /**
     * 
     * @param subjectMergeRequest
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     * @throws EMPIExceptionUnknownSubjectIdentifier
     */
    public EMPINotification mergeSubjects(SubjectMergeRequest subjectMergeRequest) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier;

    /**
     * 
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     * @throws EMPIExceptionUnknownSubjectIdentifier  
     */
    public SubjectSearchResponse findSubjects(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier;

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     * @throws EMPIExceptionUnknownSubjectIdentifier
     */
    public SubjectSearchResponse getBySubjectIdentifiers(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException, EMPIExceptionUnknownIdentifierDomain, EMPIExceptionUnknownSubjectIdentifier;
}
