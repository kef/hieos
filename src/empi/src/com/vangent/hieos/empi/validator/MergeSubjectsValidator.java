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
package com.vangent.hieos.empi.validator;

import com.vangent.hieos.empi.persistence.SubjectIdentifierDomainLoader;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectMergeRequest;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class MergeSubjectsValidator extends Validator {

    private static final Logger logger = Logger.getLogger(MergeSubjectsValidator.class);
    private SubjectMergeRequest subjectMergeRequest;

    /**
     *
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    public MergeSubjectsValidator(PersistenceManager persistenceManager, DeviceInfo senderDeviceInfo) {
        super(persistenceManager, senderDeviceInfo);
    }

    /**
     *
     * @return
     */
    public SubjectMergeRequest getSubjectMergeRequest() {
        return subjectMergeRequest;
    }

    /**
     *
     * @param subjectMergeRequest
     */
    public void setSubjectMergeRequest(SubjectMergeRequest subjectMergeRequest) {
        this.subjectMergeRequest = subjectMergeRequest;
    }

    /**
     * 
     * @throws EMPIException
     */
    @Override
    public void validate() throws EMPIException {
        Subject survivingSubject = subjectMergeRequest.getSurvivingSubject();
        Subject subsumedSubject = subjectMergeRequest.getSubsumedSubject();
        this.validateIdentitySource(survivingSubject);
        this.validateIdentitySource(subsumedSubject);
        this.validateMergeSubjects(survivingSubject, subsumedSubject);
    }

    /**
     *
     * @throws EMPIException
     */
    @Override
    public void load() throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        SubjectIdentifierDomainLoader loader = new SubjectIdentifierDomainLoader(this.getPersistenceManager());
        loader.loadSubjectIdentifierDomains(subjectMergeRequest.getSurvivingSubject());
        loader.loadSubjectIdentifierDomains(subjectMergeRequest.getSubsumedSubject());
    }

    /**
     *
     * @param survivingSubject
     * @param subsumedSubject
     * @throws EMPIException
     */
    private void validateMergeSubjects(Subject survivingSubject, Subject subsumedSubject) throws EMPIException {
        this.valididateMergeSubject(survivingSubject, "surviving");
        this.valididateMergeSubject(subsumedSubject, "subsumed");
        // Only using first identifier for merge request.
        SubjectIdentifier survivingSubjectIdentifier = survivingSubject.getSubjectIdentifiers().get(0);
        SubjectIdentifier subsumedSubjectIdentifier = subsumedSubject.getSubjectIdentifiers().get(0);
        if (survivingSubjectIdentifier.equals(subsumedSubjectIdentifier)) {
            throw new EMPIException("Same identifier supplied - skipping merge");
        }
    }

    /**
     *
     * @param subject
     * @param subjectType
     * @throws EMPIException
     */
    private void valididateMergeSubject(Subject subject, String subjectType) throws EMPIException {
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        if (subjectIdentifiers.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("No ").append(subjectType).append(" subject identifier supplied - skipping merge");
            throw new EMPIException(sb.toString());
        }
        /* TEMPORARY HACK -- CONNECTATHON (ICW)
        if (subjectIdentifiers.size() > 1) {
        StringBuilder sb = new StringBuilder();
        sb.append(">1 ").append(subjectType).append(" subject identifier supplied - skipping merge");
        throw new EMPIException(sb.toString());
        }*/
        // NOTE: Decided to keep code above commented out - will use first identifier for merge.
    }
}
