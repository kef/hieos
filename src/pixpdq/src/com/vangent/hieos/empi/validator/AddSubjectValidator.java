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
import com.vangent.hieos.subjectmodel.Subject;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class AddSubjectValidator extends Validator {

    private static final Logger logger = Logger.getLogger(AddSubjectValidator.class);

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
     * @param newSubject
     * @throws EMPIException
     */
    public void validate(Subject newSubject) throws EMPIException {
        this.validateIdentitySource(newSubject);
        this.validateSubjectIdentifierDomains(newSubject);
        this.validateSubjectCodes(newSubject);

        // FIXME: This is not totally correct, how about "other ids"?
        // See if the subject exists (if it already has identifiers).
        if (newSubject.hasSubjectIdentifiers()) {

            // See if the subject already exists.
            PersistenceManager pm = this.getPersistenceManager();
            if (pm.doesSubjectExist(newSubject.getSubjectIdentifiers())) {
                throw new EMPIException("Subject already exists!");
            }
        }
    }
}
