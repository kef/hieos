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
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateSubjectValidator extends Validator {

    private static final Logger logger = Logger.getLogger(UpdateSubjectValidator.class);
    private Subject subject;

    /**
     *
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    public UpdateSubjectValidator(PersistenceManager persistenceManager, DeviceInfo senderDeviceInfo) {
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
     */
    @Override
    public void validate() throws EMPIException {
        this.validateIdentitySource(subject);
        this.validateSubjectCodes(subject);

        // Make sure that subject identifiers are present.
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        if (subjectIdentifiers.isEmpty()) {
            throw new EMPIException("No identifiers provided for subject - skipping update.");
        }
    }

    /**
     *
     * @throws EMPIException
     */
    @Override
    public void load() throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        SubjectIdentifierDomainLoader loader = new SubjectIdentifierDomainLoader(this.getPersistenceManager());
        loader.loadSubjectIdentifierDomains(subject);
    }
}
