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
package com.vangent.hieos.patientcorrelation.dao;

import com.vangent.hieos.patientcorrelation.exception.PatientCorrelationException;
import com.vangent.hieos.patientcorrelation.model.PatientCorrelation;
import com.vangent.hieos.xutil.uuid.UuidAllocator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PatientCorrelationDAO {

    private final static Logger logger = Logger.getLogger(PatientCorrelationDAO.class);
    private final Connection connection;

    private PatientCorrelationDAO() {
        // Do nothing.
        this.connection = null;
    }

    /**
     * 
     * @param connection
     */
    public PatientCorrelationDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     *
     * @param patientCorrelation
     */
    public void store(PatientCorrelation patientCorrelation) throws PatientCorrelationException {
        // Update times.
        this.setTimes(patientCorrelation);

        // First see if the correlation already exists.
        PatientCorrelation foundPatientCorrelation = this.lookup(patientCorrelation);
        if (foundPatientCorrelation == null) {
            patientCorrelation.setId(UuidAllocator.allocate());
            logger.debug("Inserting correlation: " + patientCorrelation.getVitals());
            this.insert(patientCorrelation);
        } else {
            logger.debug("Updating correlation: " + foundPatientCorrelation.getVitals());
            patientCorrelation.setId(foundPatientCorrelation.getId());
            logger.debug(" .... to correlation: " + patientCorrelation.getVitals());
            this.update(patientCorrelation);
        }
    }

    /**
     *
     * @param patientCorrelations
     * @throws PatientCorrelationException
     */
    public void store(List<PatientCorrelation> patientCorrelations) throws PatientCorrelationException {
        for (PatientCorrelation patientCorrelation : patientCorrelations) {
            this.store(patientCorrelation);
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
        String sql = "SELECT id,localhome,localpatientid,remotehome,remotepatientid,status,lastupdatetime,expirationtime FROM patientcorrelation WHERE localpatientid = ? AND localhome = ?";
        List<PatientCorrelation> patientCorrelations = new ArrayList<PatientCorrelation>();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, localPatientId);
            stmt.setString(2, localHomeCommunityId);
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(PatientCorrelation) = " + sql);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                PatientCorrelation patientCorrelation = new PatientCorrelation();
                patientCorrelation.setId(rs.getString(1));
                patientCorrelation.setLocalHomeCommunityId(rs.getString(2));
                patientCorrelation.setLocalPatientId(rs.getString(3));
                patientCorrelation.setRemoteHomeCommunityId(rs.getString(4));
                patientCorrelation.setRemotePatientId(rs.getString(5));
                // FIXME ....
                //patientCorrelation.setStatus(rs.getString(6).charAt(0));
                //patientCorrelation.setLastUpdatedTime(this.getDate(rs.getTimestamp(7)));
                //patientCorrelation.setExpirationTime(this.getDate(rs.getTimestamp(8)));
                patientCorrelations.add(patientCorrelation);
            }
        } catch (SQLException ex) {
            throw new PatientCorrelationException("Failure reading Patient Correlations from database" + ex.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
                //connection.close();
            } catch (Exception e) {
                //Do nothing.
            }
        }
        return patientCorrelations;
    }

    /**
     *
     * @param patientCorrelation
     * @throws PatientCorrelationException
     */
    private void insert(PatientCorrelation patientCorrelation) throws PatientCorrelationException {
        String sql = "INSERT INTO patientcorrelation (id,localhome,localpatientid,remotehome,remotepatientid,status,lastupdatetime,expirationtime) values (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, patientCorrelation.getId());
            stmt.setString(2, patientCorrelation.getLocalHomeCommunityId());
            stmt.setString(3, patientCorrelation.getLocalPatientId());
            stmt.setString(4, patientCorrelation.getRemoteHomeCommunityId());
            stmt.setString(5, patientCorrelation.getRemotePatientId());
            // FIXME ?? ....
            stmt.setString(6, Character.toString(patientCorrelation.getStatus()));
            stmt.setTimestamp(7, this.getTimestamp(patientCorrelation.getLastUpdatedTime()));
            stmt.setTimestamp(8, this.getTimestamp(patientCorrelation.getExpirationTime()));
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(PatientCorrelation) = " + sql);
            }
            stmt.execute();
        } catch (SQLException ex) {
            logger.error("Failure inserting Patient Correlation", ex);
            throw new PatientCorrelationException(ex.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    // FIXME: ? Just eat this one.
                }
            }
        }
    }

    /**
     * 
     * @param patientCorrelation
     * @throws PatientCorrelationException
     */
    private void update(PatientCorrelation patientCorrelation) throws PatientCorrelationException {
        String sql = "UPDATE patientcorrelation SET localhome=?, localpatientid=?, remotehome=?, remotepatientid=?, status=?, lastupdatetime=?, expirationtime=? WHERE id = ?";
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, patientCorrelation.getLocalHomeCommunityId());
            stmt.setString(2, patientCorrelation.getLocalPatientId());
            stmt.setString(3, patientCorrelation.getRemoteHomeCommunityId());
            stmt.setString(4, patientCorrelation.getRemotePatientId());
            // FIXME? ....
            stmt.setString(5, Character.toString(patientCorrelation.getStatus()));
            stmt.setTimestamp(6, this.getTimestamp(patientCorrelation.getLastUpdatedTime()));
            stmt.setTimestamp(7, this.getTimestamp(patientCorrelation.getExpirationTime()));
            stmt.setString(8, patientCorrelation.getId());
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(PatientCorrelation) = " + sql);
            }
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            logger.error("Failure updating Patient Correlation", ex);
            throw new PatientCorrelationException(ex.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    // FIXME: ? Just eat this one.
                }
            }
        }
    }

    /**
     * 
     * @param patientCorrelation
     * @return
     * @throws PatientCorrelationException
     */
    private PatientCorrelation lookup(PatientCorrelation patientCorrelation) throws PatientCorrelationException {
        String sql = "SELECT id,localhome,localpatientid,remotehome,remotepatientid,status,lastupdatetime,expirationtime FROM patientcorrelation WHERE localpatientid = ? AND localhome = ? AND remotehome = ?";
        PatientCorrelation foundPatientCorrelation = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, patientCorrelation.getLocalPatientId());
            stmt.setString(2, patientCorrelation.getLocalHomeCommunityId());
            stmt.setString(3, patientCorrelation.getRemoteHomeCommunityId());
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(PatientCorrelation) = " + sql);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                // OK ... none found
            } else {
                foundPatientCorrelation = new PatientCorrelation();
                foundPatientCorrelation.setId(rs.getString(1));
                foundPatientCorrelation.setLocalHomeCommunityId(rs.getString(2));
                foundPatientCorrelation.setLocalPatientId(rs.getString(3));
                foundPatientCorrelation.setRemoteHomeCommunityId(rs.getString(4));
                foundPatientCorrelation.setRemotePatientId(rs.getString(5));
                foundPatientCorrelation.setStatus(rs.getString(6).charAt(0));
                foundPatientCorrelation.setLastUpdatedTime(this.getDate(rs.getTimestamp(7)));
                foundPatientCorrelation.setExpirationTime(this.getDate(rs.getTimestamp(8)));
            }
        } catch (SQLException ex) {
            throw new PatientCorrelationException("Failure reading Patient Correlations from database" + ex.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
                //connection.close();
            } catch (Exception e) {
                //Do nothing.
            }
        }
        return foundPatientCorrelation;
    }

    /**
     * 
     * @param date
     * @return
     */
    private Timestamp getTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     *
     * @param timestamp
     * @return
     */
    private Date getDate(Timestamp timestamp) {
        return new Date(timestamp.getTime());
    }

    /**
     *
     * @param patientCorrelation
     */
    private void setTimes(PatientCorrelation patientCorrelation) {
        Date currentDate = new Date();
        patientCorrelation.setLastUpdatedTime(currentDate);

        // FIXME ... need to set a proper expiration time
        patientCorrelation.setExpirationTime(currentDate);
    }
}
