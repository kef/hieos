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

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class FindSubjectsValidator extends Validator {

    private static final Logger logger = Logger.getLogger(FindSubjectsValidator.class);

    /**
     *
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    public FindSubjectsValidator(PersistenceManager persistenceManager, DeviceInfo senderDeviceInfo) {
        super(persistenceManager, senderDeviceInfo);
    }

    /**
     *
     * @param subjectSearchCriteria
     * @throws EMPIException
     */
    public void validate(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        // First validate identifier domains assocated with the search subject's identifiers.
        this.validateSubjectIdentifierDomains(subjectSearchCriteria.getSubject());

        // Now validate identifiers in any scoping organizations.
        this.validateScopingAssigningAuthorities(subjectSearchCriteria);
    }

    /**
     *
     * @param subjectSearchCriteria
     * @throws EMPIException
     */
    private void validateScopingAssigningAuthorities(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();

        // Validate identifiers in any scoping organizations.
        for (SubjectIdentifierDomain scopingIdentifierDomain : subjectSearchCriteria.getScopingAssigningAuthorities()) {
            boolean subjectIdentifierDomainExists = pm.doesSubjectIdentifierDomainExist(scopingIdentifierDomain);
            if (!subjectIdentifierDomainExists) {
                throw new EMPIException(
                        scopingIdentifierDomain.getUniversalId()
                        + " is not a known identifier domain",
                        EMPIException.ERROR_CODE_UNKNOWN_KEY_IDENTIFIER);
            }
        }
    }
}
