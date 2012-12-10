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
package com.vangent.hieos.empi.euid;

import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.config.EUIDConfig;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.persistence.PersistenceHelper;
import com.vangent.hieos.empi.persistence.SubjectIdentifierDomainDAO;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.empi.exception.EMPIException;
import java.sql.Connection;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class EUIDGenerator {

    private static final Logger logger = Logger.getLogger(EUIDGenerator.class);

    /**
     * 
     * @return
     * @throws EMPIException
     */
    public static SubjectIdentifier getEUID() throws EMPIException {
        Connection connection = null;
        SubjectIdentifier subjectIdentifier = null;
        try {
            connection = PersistenceHelper.getConnection();
            subjectIdentifier = EUIDGenerator.getEUID(connection);
        } finally {
            PersistenceHelper.close(connection);
        }
        return subjectIdentifier;
    }

    /**
     *
     * @param connection
     * @return
     * @throws EMPIException
     */
    public static SubjectIdentifier getEUID(Connection connection) throws EMPIException {
        // FIXME?: Would be nice to cache here.

        // Get configuration.
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        EUIDConfig euidConfig = empiConfig.getEuidConfig();

        // Load the SubjectIdentifierDomain for the EUID configuration.
        SubjectIdentifierDomainDAO sidDAO = new SubjectIdentifierDomainDAO(connection);
        SubjectIdentifierDomain subjectIdentifierDomain = new SubjectIdentifierDomain();
        subjectIdentifierDomain.setUniversalId(euidConfig.getEuidUniversalId());
        subjectIdentifierDomain.setUniversalIdType(euidConfig.getEuidUniversalIdType());
        SubjectIdentifierDomain loadedSubjectIdentifierDomain;
        try {
            loadedSubjectIdentifierDomain = sidDAO.load(subjectIdentifierDomain);
            // Create the subject identifier (assign the identifier domain).
            SubjectIdentifier subjectIdentifier = new SubjectIdentifier();
            subjectIdentifier.setIdentifierDomain(loadedSubjectIdentifierDomain);
            // Now, generate the ID.
            subjectIdentifier.setIdentifier(EUIDGenerator.getUniqueIdentifier());
            return subjectIdentifier;

        } catch (EMPIExceptionUnknownIdentifierDomain ex) {
            logger.error("Could not generate EUID", ex);
            // Rethrow as generic exception.
            throw new EMPIException(ex);
        }
    }

    /**
     *
     * @return
     */
    private static String getUniqueIdentifier() {
        // Generate UUID.
        UUID uuid = UUID.randomUUID();
        // Get rid of dashes before returning unique identifier.
        return uuid.toString().replaceAll("-", "");
    }
}
