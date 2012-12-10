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
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class FindSubjectsValidator extends Validator {

    private static final Logger logger = Logger.getLogger(FindSubjectsValidator.class);
    private SubjectSearchCriteria subjectSearchCriteria;

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
     * @return
     */
    public SubjectSearchCriteria getSubjectSearchCriteria() {
        return subjectSearchCriteria;
    }

    /**
     *
     * @param subjectSearchCriteria
     */
    public void setSubjectSearchCriteria(SubjectSearchCriteria subjectSearchCriteria) {
        this.subjectSearchCriteria = subjectSearchCriteria;
    }

    /**
     * 
     * @throws EMPIException
     */
    @Override
    public void validate() throws EMPIException {
        // Do nothing ...
    }

    /**
     *
     * @throws EMPIException
     */
    @Override
    public void load() throws EMPIException, EMPIExceptionUnknownIdentifierDomain {
        SubjectIdentifierDomainLoader loader = new SubjectIdentifierDomainLoader(this.getPersistenceManager());

        // Load identifier domains for the subject (identifiers + other identifiers).
        loader.loadSubjectIdentifierDomains(subjectSearchCriteria.getSubject());

        // Load/replace subject identifier domains for any supplied scoping assigning authorities.
        if (subjectSearchCriteria.hasScopingSubjectIdentifierDomains()) {
            List<SubjectIdentifierDomain> scopingSubjectIdentifierDomains = new ArrayList<SubjectIdentifierDomain>();
            int pos = 0;
            for (SubjectIdentifierDomain subjectIdentifierDomain : subjectSearchCriteria.getScopingSubjectIdentifierDomains()) {
                try {
                    SubjectIdentifierDomain loadedSubjectIdentifierDomain = loader.loadSubjectIdentifierDomain(subjectIdentifierDomain);
                    scopingSubjectIdentifierDomains.add(loadedSubjectIdentifierDomain);
                    ++pos;
                } catch (EMPIExceptionUnknownIdentifierDomain ex) {
                    // Override the default identifier domain type in the exception and keep track of the list position.
                    ex.setIdentifierDomainType(EMPIExceptionUnknownIdentifierDomain.IdentifierDomainType.SCOPING_IDENTIFIER_DOMAIN);
                    ex.setListPosition(pos);
                    throw ex; // Rethrow.
                }
            }
            // Replace scoping identifier domain list.
            subjectSearchCriteria.setScopingSubjectIdentifierDomains(scopingSubjectIdentifierDomains);
        }
    }
}
