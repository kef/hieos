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

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class BaseHandler {

    private static final Logger logger = Logger.getLogger(BaseHandler.class);
    private XConfigActor configActor = null;
    PersistenceManager persistenceManager = null;

    /**
     * 
     * @param configActor
     * @param persistenceManager
     */
    protected BaseHandler(XConfigActor configActor, PersistenceManager persistenceManager) {
        this.configActor = configActor;
        this.persistenceManager = persistenceManager;
    }

    /**
     *
     * @return
     */
    protected XConfigActor getConfigActor() {
        return this.configActor;
    }

    /**
     * 
     * @return
     */
    protected PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    /**
     *
     * @param subject
     * @throws EMPIException
     */
    protected void validateSubjectIdentifierDomains(Subject subject) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        // Validate identifier domains assocated with the subject's identifiers.
        List<SubjectIdentifier> subjectIdentifiers = subject.getSubjectIdentifiers();
        for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
            boolean subjectIdentifierDomainExists = pm.doesSubjectIdentifierDomainExist(subjectIdentifier);
            if (!subjectIdentifierDomainExists) {
                SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
                throw new EMPIException(
                        subjectIdentifierDomain.getUniversalId()
                        + " is not a known identifier domain",
                        EMPIException.ERROR_CODE_UNKNOWN_KEY_IDENTIFIER);
            }
        }
    }
}
