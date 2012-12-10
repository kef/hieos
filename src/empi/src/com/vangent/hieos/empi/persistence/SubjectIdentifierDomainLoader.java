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

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectIdentifierDomainLoader {

    private static final Logger logger = Logger.getLogger(SubjectIdentifierDomainLoader.class);
    private PersistenceManager persistenceManager = null;

    /**
     *
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    public SubjectIdentifierDomainLoader(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    /**
     *
     * @return
     */
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    /**
     *
     * @param subject
     * @throws EMPIException
     */
    public void loadSubjectIdentifierDomains(Subject subject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        PersistenceManager pm = this.getPersistenceManager();

        // Load/replace subject identifier domains for supplied subject identifiers.
        List<SubjectIdentifier> loadedSubjectIdentifiers = pm.loadSubjectIdentifierDomainsForSubjectIdentifiers(subject.getSubjectIdentifiers());
        subject.setSubjectIdentifiers(loadedSubjectIdentifiers);

        // Do the same for "other identifiers".
        List<SubjectIdentifier> loadedSubjectOtherIdentifiers = pm.loadSubjectIdentifierDomainsForSubjectIdentifiers(subject.getSubjectOtherIdentifiers());
        subject.setSubjectOtherIdentifiers(loadedSubjectOtherIdentifiers);
    }

    /**
     *
     * @param subjectIdentifierDomains
     * @return
     * @throws EMPIException
     */
    public List<SubjectIdentifierDomain> loadSubjectIdentifierDomains(List<SubjectIdentifierDomain> subjectIdentifierDomains) throws EMPIException, EMPIExceptionUnknownIdentifierDomain
    {
        PersistenceManager pm = this.getPersistenceManager();
        return pm.loadSubjectIdentifierDomains(subjectIdentifierDomains);
    }

    /**
     * 
     * @param subjectIdentifierDomain
     * @return
     * @throws EMPIException
     */
    public SubjectIdentifierDomain loadSubjectIdentifierDomain(SubjectIdentifierDomain subjectIdentifierDomain) throws EMPIException, EMPIExceptionUnknownIdentifierDomain
    {
        PersistenceManager pm = this.getPersistenceManager();
        return pm.loadSubjectIdentifierDomain(subjectIdentifierDomain);
    }
}
