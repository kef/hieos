/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.patientcorrelation.service;

import com.vangent.hieos.patientcorrelation.dao.PatientCorrelationDAO;
import com.vangent.hieos.patientcorrelation.exception.PatientCorrelationException;
import com.vangent.hieos.patientcorrelation.model.PatientCorrelation;
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
public class PatientCorrelationService {

    private final static Logger logger = Logger.getLogger(PatientCorrelationService.class);

    /**
     *
     * @param patientCorrelation
     * @throws PatientCorrelationException
     */
    public void store(PatientCorrelation patientCorrelation) throws PatientCorrelationException  {
        Connection connection = this.getConnection();
        try {
            PatientCorrelationDAO dao = new PatientCorrelationDAO(connection);
            dao.store(patientCorrelation);
        } catch (PatientCorrelationException ex) {
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
     * @param patientCorrelations
     * @throws PatientCorrelationException
     */
    public void store(List<PatientCorrelation> patientCorrelations) throws PatientCorrelationException {
        Connection connection = this.getConnection();
        try {
            PatientCorrelationDAO dao = new PatientCorrelationDAO(connection);
            dao.store(patientCorrelations);
        } catch (PatientCorrelationException ex) {
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
     * @throws PatientCorrelationException
     */
    public List<PatientCorrelation> lookup(String localPatientId, String localHomeCommunityId) throws PatientCorrelationException {
        Connection connection = this.getConnection();
        PatientCorrelationDAO dao = new PatientCorrelationDAO(connection);
        List<PatientCorrelation> patientCorrelations;
        try {
            patientCorrelations = dao.lookup(localPatientId, localHomeCommunityId);
            return patientCorrelations;
        } catch (PatientCorrelationException ex) {
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
     * @throws PatientCorrelationException
     */
    private Connection getConnection() throws PatientCorrelationException {
        try {
            Connection connection = new SQLConnectionWrapper().getConnection(SQLConnectionWrapper.adtJNDIResourceName);
            return connection;
        } catch (XdsInternalException ex) {
            logger.error("Could not open connection to support PatientCorrelation", ex);
            throw new PatientCorrelationException(ex.getMessage());
        }
    }
}
