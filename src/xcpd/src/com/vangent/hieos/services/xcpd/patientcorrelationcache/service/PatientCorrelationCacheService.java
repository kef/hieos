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
package com.vangent.hieos.services.xcpd.patientcorrelationcache.service;

import com.vangent.hieos.services.xcpd.patientcorrelationcache.dao.PatientCorrelationCacheDAO;
import com.vangent.hieos.services.xcpd.patientcorrelationcache.exception.PatientCorrelationCacheException;
import com.vangent.hieos.services.xcpd.patientcorrelationcache.model.PatientCorrelationCacheEntry;

import com.vangent.hieos.xutil.db.support.SQLConnectionWrapper;
import com.vangent.hieos.xutil.exception.XdsInternalException;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.List;

import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PatientCorrelationCacheService {

    private final static Logger logger = Logger.getLogger(PatientCorrelationCacheService.class);
    private final static int DEFAULT_EXPIRATION_DAYS = 2;
    private int expirationDays = DEFAULT_EXPIRATION_DAYS;

    /**
     * 
     * @param expirationDays
     */
    public void setExpirationDays(int expirationDays) {
        this.expirationDays = expirationDays;
    }

    /**
     *
     * @return
     */
    public int getExpirationDays() {
        return this.expirationDays;
    }

    /**
     *
     * @param patientCorrelationCacheEntry
     * @throws PatientCorrelationCacheException
     */
    public void store(PatientCorrelationCacheEntry patientCorrelationCacheEntry) throws PatientCorrelationCacheException {
        Connection connection = this.getConnection();
        try {
            PatientCorrelationCacheDAO dao = new PatientCorrelationCacheDAO(connection);
            dao.store(patientCorrelationCacheEntry, this.getExpirationDays());
        } catch (PatientCorrelationCacheException ex) {
            throw ex;  // rethrow.
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                // Just let processing continue ....
                logger.error("Could not close Patient Correlation connection", ex);
            }
        }
    }

    /**
     *
     * @param patientCorrelationCacheEntries
     * @throws PatientCorrelationCacheException
     */
    public void store(List<PatientCorrelationCacheEntry> patientCorrelationCacheEntries) throws PatientCorrelationCacheException {
        Connection connection = this.getConnection();
        try {
            PatientCorrelationCacheDAO dao = new PatientCorrelationCacheDAO(connection);
            dao.store(patientCorrelationCacheEntries, this.getExpirationDays());
        } catch (PatientCorrelationCacheException ex) {
            throw ex;  // rethrow
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                // Just let processing continue ....
                logger.error("Could not close Patient Correlation connection", ex);
            }
        }
    }

    /**
     * 
     * @param localPatientId
     * @param localHomeCommunityId
     * @return
     * @throws PatientCorrelationCacheException
     */
    public List<PatientCorrelationCacheEntry> lookup(String localPatientId, String localHomeCommunityId) throws PatientCorrelationCacheException {
        Connection connection = this.getConnection();
        try {
            PatientCorrelationCacheDAO dao = new PatientCorrelationCacheDAO(connection);
            List<PatientCorrelationCacheEntry> correlationCacheEntries = dao.lookup(localPatientId, localHomeCommunityId);
            return correlationCacheEntries;
        } catch (PatientCorrelationCacheException ex) {
            throw ex; // rethrow
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                // Just let processing continue ....
                logger.error("Could not close Patient Correlation connection", ex);
            }
        }
    }

    /**
     * 
     * @param localPatientId
     * @param localHomeCommunityId
     * @throws PatientCorrelationCacheException
     */
    public void deleteExpired(String localPatientId, String localHomeCommunityId) throws PatientCorrelationCacheException {
        Connection connection = this.getConnection();
        try {
            PatientCorrelationCacheDAO dao = new PatientCorrelationCacheDAO(connection);
            dao.deleteExpired(localPatientId, localHomeCommunityId);
        } catch (PatientCorrelationCacheException ex) {
            throw ex; // rethrow
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                // Just let processing continue ....
                logger.error("Could not close Patient Correlation connection", ex);
            }
        }
    }

    /**
     * Get ADT (for now) JDBC connection instance from connection pool.
     *
     * @return Database connection instance from pool.
     * @throws PatientCorrelationCacheException
     */
    private Connection getConnection() throws PatientCorrelationCacheException {
        try {
            Connection connection = new SQLConnectionWrapper().getConnection(SQLConnectionWrapper.adtJNDIResourceName);
            return connection;
        } catch (XdsInternalException ex) {
            logger.error("Could not open connection to support Patient Correlation", ex);
            throw new PatientCorrelationCacheException(ex.getMessage());
        }
    }
}
