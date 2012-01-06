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

import com.vangent.hieos.empi.codes.CodesConfig.CodedType;
import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectCitizenship;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectLanguage;
import com.vangent.hieos.hl7v3util.model.subject.SubjectPersonalRelationship;
import com.vangent.hieos.services.pixpdq.empi.api.EMPINotification;
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

    /**
     *
     * @param subject
     * @throws EMPIException
     */
    protected void validateSubjectCodes(Subject subject) throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Validate individual code values.
        empiConfig.validateCode(subject.getGender(), CodedType.GENDER);
        empiConfig.validateCode(subject.getMaritalStatus(), CodedType.MARITAL_STATUS);
        empiConfig.validateCode(subject.getReligiousAffiliation(), CodedType.RELIGIOUS_AFFILIATION);
        empiConfig.validateCode(subject.getRace(), CodedType.RACE);
        empiConfig.validateCode(subject.getEthnicGroup(), CodedType.ETHNIC_GROUP);

        // Validate personal relationship codes.
        List<SubjectPersonalRelationship> subjectPersonalRelationships = subject.getSubjectPersonalRelationships();
        for (SubjectPersonalRelationship subjectPersonalRelationship : subjectPersonalRelationships) {
            empiConfig.validateCode(subjectPersonalRelationship.getRelationshipType(), CodedType.PERSONAL_RELATIONSHIP);
        }

        // Validate language codes.
        List<SubjectLanguage> subjectLanguages = subject.getSubjectLanguages();
        for (SubjectLanguage subjectLanguage : subjectLanguages) {
            empiConfig.validateCode(subjectLanguage.getLanguageCode(), CodedType.LANGUAGE);
        }

        // Validate nation codes.
        List<SubjectCitizenship> subjectCitizenships = subject.getSubjectCitizenships();
        for (SubjectCitizenship subjectCitizenship : subjectCitizenships) {
            empiConfig.validateCode(subjectCitizenship.getNationCode(), CodedType.NATION);
        }
    }

    /**
     * 
     * @param notification
     * @param enterpriseSubjectId
     * @throws EMPIException
     */
    protected void addSubjectToNotification(EMPINotification notification, String enterpriseSubjectId) throws EMPIException {
        PersistenceManager pm = this.getPersistenceManager();
        Subject subject = pm.loadEnterpriseSubjectIdentifiersAndNamesOnly(enterpriseSubjectId);
        notification.addSubject(subject);
    }
}
