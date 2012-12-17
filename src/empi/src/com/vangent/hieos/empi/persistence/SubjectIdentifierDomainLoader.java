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
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public void loadSubjectIdentifierDomains(Subject subject) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {

        // Load/replace subject identifier domains for supplied subject identifiers.
        List<SubjectIdentifier> loadedSubjectIdentifiers = this.loadSubjectIdentifierDomainsForSubjectIdentifiers(subject.getSubjectIdentifiers());
        subject.setSubjectIdentifiers(loadedSubjectIdentifiers);

        // Do the same for "other identifiers".
        List<SubjectIdentifier> loadedSubjectOtherIdentifiers = this.loadSubjectIdentifierDomainsForSubjectIdentifiers(subject.getSubjectOtherIdentifiers());
        subject.setSubjectOtherIdentifiers(loadedSubjectOtherIdentifiers);
    }

    /**
     * Go to the EMPI database and fully load the identifier domains for the
     * provided input.
     *
     * @param subjectIdentifiers
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    private List<SubjectIdentifier> loadSubjectIdentifierDomainsForSubjectIdentifiers(List<SubjectIdentifier> subjectIdentifiers) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        SubjectIdentifierDomainDAO dao = new SubjectIdentifierDomainDAO(persistenceManager);
        return dao.loadForSubjectIdentifiers(subjectIdentifiers);
    }

    /**
     * Go to the EMPI database and fully load the identifier domains for the
     * provided input.
     *
     * @param subjectIdentifierDomains
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    //public List<SubjectIdentifierDomain> loadSubjectIdentifierDomains(List<SubjectIdentifierDomain> subjectIdentifierDomains) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
    //    SubjectIdentifierDomainDAO dao = new SubjectIdentifierDomainDAO(persistenceManager);
    //    return dao.load(subjectIdentifierDomains);
    //}

    /**
     * Go to the EMPI database and fully load the identifier domain for the
     * provided input.
     *
     * @param subjectIdentifierDomain
     * @return
     * @throws EMPIException
     * @throws EMPIExceptionUnknownIdentifierDomain
     */
    public SubjectIdentifierDomain loadSubjectIdentifierDomain(SubjectIdentifierDomain subjectIdentifierDomain) throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        SubjectIdentifierDomainDAO dao = new SubjectIdentifierDomainDAO(persistenceManager);
        return dao.load(subjectIdentifierDomain);
    }
}
