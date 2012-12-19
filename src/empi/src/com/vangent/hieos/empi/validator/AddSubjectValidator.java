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
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.persistence.SubjectController;
import com.vangent.hieos.empi.persistence.SubjectIdentifierDomainLoader;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class AddSubjectValidator extends Validator {

    private static final Logger logger = Logger.getLogger(AddSubjectValidator.class);
    private Subject subject;

    /**
     *
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    public AddSubjectValidator(PersistenceManager persistenceManager, DeviceInfo senderDeviceInfo) {
        super(persistenceManager, senderDeviceInfo);
    }

    /**
     *
     * @return
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     *
     * @param subject
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    /**
     *
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    @Override
    public void validate() throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        this.validateIdentitySource(subject);
        //this.validateSubjectIdentifierDomains(subject);
        this.validateSubjectCodes(subject);

        // FIXME: This is not totally correct, how about "other ids"?
        // See if the subject exists (if it already has identifiers).
        if (subject.hasSubjectIdentifiers()) {

            // See if the subject already exists.
            SubjectController subjectController = new SubjectController(this.getPersistenceManager());
            if (subjectController.doesSubjectExist(subject.getSubjectIdentifiers())) {
                throw new EMPIException("Subject already exists!");
            }
        }
    }

    /**
     *
     * @throws EMPIException
     */
    public void load() throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        SubjectIdentifierDomainLoader loader = new SubjectIdentifierDomainLoader(this.getPersistenceManager());
        loader.loadSubjectIdentifierDomains(subject);
    }
}
