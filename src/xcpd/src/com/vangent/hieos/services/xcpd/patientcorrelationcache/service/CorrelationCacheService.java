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
package com.vangent.hieos.services.xcpd.correlationcache.service;

import com.vangent.hieos.services.xcpd.correlationcache.dao.CorrelationCacheDAO;
import com.vangent.hieos.services.xcpd.correlationcache.exception.CorrelationCacheException;
import com.vangent.hieos.services.xcpd.correlationcache.model.CorrelationCacheEntry;

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
public class CorrelationCacheService {

    private final static Logger logger = Logger.getLogger(CorrelationCacheService.class);

    /**
     * 
     * @param correlationCacheEntry
     * @throws CorrelationCacheException
     */
    public void store(CorrelationCacheEntry correlationCacheEntry) throws CorrelationCacheException {
        Connection connection = this.getConnection();
        try {
            CorrelationCacheDAO dao = new CorrelationCacheDAO(connection);
            dao.store(correlationCacheEntry);
        } catch (CorrelationCacheException ex) {
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
     * @param correlationCacheEntries
     * @throws CorrelationCacheException
     */
    public void store(List<CorrelationCacheEntry> correlationCacheEntries) throws CorrelationCacheException {
        Connection connection = this.getConnection();
        try {
            CorrelationCacheDAO dao = new CorrelationCacheDAO(connection);
            dao.store(correlationCacheEntries);
        } catch (CorrelationCacheException ex) {
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
     * @throws CorrelationCacheException
     */
    public List<CorrelationCacheEntry> lookup(String localPatientId, String localHomeCommunityId) throws CorrelationCacheException {
        Connection connection = this.getConnection();
        try {
            CorrelationCacheDAO dao = new CorrelationCacheDAO(connection);
            List<CorrelationCacheEntry> correlationCacheEntries = dao.lookup(localPatientId, localHomeCommunityId);
            return correlationCacheEntries;
        } catch (CorrelationCacheException ex) {
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
     * @throws CorrelationCacheException
     */
    private Connection getConnection() throws CorrelationCacheException {
        try {
            Connection connection = new SQLConnectionWrapper().getConnection(SQLConnectionWrapper.adtJNDIResourceName);
            return connection;
        } catch (XdsInternalException ex) {
            logger.error("Could not open connection to support PatientCorrelation", ex);
            throw new CorrelationCacheException(ex.getMessage());
        }
    }
}
