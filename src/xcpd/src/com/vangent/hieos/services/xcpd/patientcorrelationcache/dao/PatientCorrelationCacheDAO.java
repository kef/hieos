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
package com.vangent.hieos.services.xcpd.patientcorrelationcache.dao;

import com.vangent.hieos.services.xcpd.patientcorrelationcache.exception.PatientCorrelationCacheException;
import com.vangent.hieos.services.xcpd.patientcorrelationcache.model.PatientCorrelationCacheEntry;

import com.vangent.hieos.xutil.uuid.UuidAllocator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PatientCorrelationCacheDAO {

    private final static Logger logger = Logger.getLogger(PatientCorrelationCacheDAO.class);
    private final Connection connection;

    /**
     *
     */
    private PatientCorrelationCacheDAO() {
        // Do nothing.
        this.connection = null;
    }

    /**
     * 
     * @param connection
     */
    public PatientCorrelationCacheDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     *
     * @param patientCorrelationCacheEntry
     * @param expirationDays
     * @throws PatientCorrelationCacheException
     */
    public void store(PatientCorrelationCacheEntry patientCorrelationCacheEntry, int expirationDays) throws PatientCorrelationCacheException {
        // Update times.
        this.setTimes(patientCorrelationCacheEntry, expirationDays);

        // First see if the correlation already exists.
        PatientCorrelationCacheEntry foundPatientCorrelationCacheEntry = this.lookup(patientCorrelationCacheEntry);
        if (foundPatientCorrelationCacheEntry == null) {
            patientCorrelationCacheEntry.setId(UuidAllocator.allocate());
            logger.debug("Inserting correlation: " + patientCorrelationCacheEntry.getVitals());
            this.insert(patientCorrelationCacheEntry);
        } else {
            logger.debug("Updating correlation: " + foundPatientCorrelationCacheEntry.getVitals());
            patientCorrelationCacheEntry.setId(foundPatientCorrelationCacheEntry.getId());
            logger.debug(" .... to correlation: " + patientCorrelationCacheEntry.getVitals());
            this.update(patientCorrelationCacheEntry);
        }
    }

    /**
     * 
     * @param patientCorrelationCacheEntries
     * @param expirationDays
     * @throws PatientCorrelationCacheException
     */
    public void store(List<PatientCorrelationCacheEntry> patientCorrelationCacheEntries, int expirationDays) throws PatientCorrelationCacheException {
        for (PatientCorrelationCacheEntry patientCorrelationCacheEntry : patientCorrelationCacheEntries) {
            this.store(patientCorrelationCacheEntry, expirationDays);
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
        String sql = "SELECT id,localhome,localpatientid,remotehome,remotepatientid,status,lastupdatetime,expirationtime FROM patientcorrelation WHERE localpatientid = ? AND localhome = ?";
        List<PatientCorrelationCacheEntry> patientCorrelationCacheEntries = new ArrayList<PatientCorrelationCacheEntry>();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, localPatientId);
            stmt.setString(2, localHomeCommunityId);
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(PatientCorrelationCacheEntry) = " + sql);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                PatientCorrelationCacheEntry patientCorrelationCacheEntry = new PatientCorrelationCacheEntry();
                patientCorrelationCacheEntry.setId(rs.getString(1));
                patientCorrelationCacheEntry.setLocalHomeCommunityId(rs.getString(2));
                patientCorrelationCacheEntry.setLocalPatientId(rs.getString(3));
                patientCorrelationCacheEntry.setRemoteHomeCommunityId(rs.getString(4));
                patientCorrelationCacheEntry.setRemotePatientId(rs.getString(5));
                patientCorrelationCacheEntry.setStatus(rs.getString(6).charAt(0));
                patientCorrelationCacheEntry.setLastUpdatedTime(this.getDate(rs.getTimestamp(7)));
                patientCorrelationCacheEntry.setExpirationTime(this.getDate(rs.getTimestamp(8)));
                patientCorrelationCacheEntries.add(patientCorrelationCacheEntry);
            }
        } catch (SQLException ex) {
            throw new PatientCorrelationCacheException("Failure reading Patient Correlations from database" + ex.getMessage());
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
        return patientCorrelationCacheEntries;
    }

    /**
     *
     * @param localPatientId
     * @param localHomeCommunityId
     * @throws PatientCorrelationCacheException
     */
    public void deleteExpired(String localPatientId, String localHomeCommunityId) throws PatientCorrelationCacheException {
        List<PatientCorrelationCacheEntry> patientCorrelationCacheEntries = this.lookup(localPatientId, localHomeCommunityId);
        for (PatientCorrelationCacheEntry patientCorrelationCacheEntry : patientCorrelationCacheEntries) {
            // Check the time.
            Date now = new Date();
            if (patientCorrelationCacheEntry.getExpirationTime().before(now)) {
                this.delete(patientCorrelationCacheEntry);
            }
        }
    }

    /**
     * 
     * @param patientCorrelationCacheEntry
     * @throws PatientCorrelationCacheException
     */
    private void delete(PatientCorrelationCacheEntry patientCorrelationCacheEntry) throws PatientCorrelationCacheException {
        String sql = "DELETE FROM patientcorrelation WHERE id = ?";
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, patientCorrelationCacheEntry.getId());
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(PatientCorrelationCacheEntry) = " + sql);
            }
            stmt.execute();
        } catch (SQLException ex) {
            logger.error("Failure deleting PatientCorrelationCacheEntry", ex);
            throw new PatientCorrelationCacheException(ex.getMessage());
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
     * @param patientCorrelationCacheEntry
     * @throws PatientCorrelationCacheException
     */
    private void insert(PatientCorrelationCacheEntry patientCorrelationCacheEntry) throws PatientCorrelationCacheException {
        String sql = "INSERT INTO patientcorrelation (id,localhome,localpatientid,remotehome,remotepatientid,status,lastupdatetime,expirationtime) values (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, patientCorrelationCacheEntry.getId());
            stmt.setString(2, patientCorrelationCacheEntry.getLocalHomeCommunityId());
            stmt.setString(3, patientCorrelationCacheEntry.getLocalPatientId());
            stmt.setString(4, patientCorrelationCacheEntry.getRemoteHomeCommunityId());
            stmt.setString(5, patientCorrelationCacheEntry.getRemotePatientId());
            stmt.setString(6, Character.toString(patientCorrelationCacheEntry.getStatus()));
            stmt.setTimestamp(7, this.getTimestamp(patientCorrelationCacheEntry.getLastUpdatedTime()));
            stmt.setTimestamp(8, this.getTimestamp(patientCorrelationCacheEntry.getExpirationTime()));
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(PatientCorrelationCacheEntry) = " + sql);
            }
            stmt.execute();
        } catch (SQLException ex) {
            logger.error("Failure inserting PatientCorrelationCacheEntry", ex);
            throw new PatientCorrelationCacheException(ex.getMessage());
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
     * @param patientCorrelationCacheEntry
     * @throws PatientCorrelationCacheException
     */
    private void update(PatientCorrelationCacheEntry patientCorrelationCacheEntry) throws PatientCorrelationCacheException {
        String sql = "UPDATE patientcorrelation SET localhome=?, localpatientid=?, remotehome=?, remotepatientid=?, status=?, lastupdatetime=?, expirationtime=? WHERE id = ?";
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, patientCorrelationCacheEntry.getLocalHomeCommunityId());
            stmt.setString(2, patientCorrelationCacheEntry.getLocalPatientId());
            stmt.setString(3, patientCorrelationCacheEntry.getRemoteHomeCommunityId());
            stmt.setString(4, patientCorrelationCacheEntry.getRemotePatientId());
            stmt.setString(5, Character.toString(patientCorrelationCacheEntry.getStatus()));
            stmt.setTimestamp(6, this.getTimestamp(patientCorrelationCacheEntry.getLastUpdatedTime()));
            stmt.setTimestamp(7, this.getTimestamp(patientCorrelationCacheEntry.getExpirationTime()));
            stmt.setString(8, patientCorrelationCacheEntry.getId());
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(PatientCorrelationCacheEntry) = " + sql);
            }
            stmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Failure updating CorrelationCacheEntry", ex);
            throw new PatientCorrelationCacheException(ex.getMessage());
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
     * @param patientCorrelationCacheEntry
     * @return
     * @throws PatientCorrelationCacheException
     */
    private PatientCorrelationCacheEntry lookup(PatientCorrelationCacheEntry patientCorrelationCacheEntry) throws PatientCorrelationCacheException {
        String sql = "SELECT id,localhome,localpatientid,remotehome,remotepatientid,status,lastupdatetime,expirationtime FROM patientcorrelation WHERE localpatientid = ? AND localhome = ? AND remotehome = ?";
        PatientCorrelationCacheEntry foundPatientCorrelationCacheEntry = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, patientCorrelationCacheEntry.getLocalPatientId());
            stmt.setString(2, patientCorrelationCacheEntry.getLocalHomeCommunityId());
            stmt.setString(3, patientCorrelationCacheEntry.getRemoteHomeCommunityId());
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(PatientCorrelationCache) = " + sql);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                // OK ... none found
            } else {
                foundPatientCorrelationCacheEntry = new PatientCorrelationCacheEntry();
                foundPatientCorrelationCacheEntry.setId(rs.getString(1));
                foundPatientCorrelationCacheEntry.setLocalHomeCommunityId(rs.getString(2));
                foundPatientCorrelationCacheEntry.setLocalPatientId(rs.getString(3));
                foundPatientCorrelationCacheEntry.setRemoteHomeCommunityId(rs.getString(4));
                foundPatientCorrelationCacheEntry.setRemotePatientId(rs.getString(5));
                foundPatientCorrelationCacheEntry.setStatus(rs.getString(6).charAt(0));
                foundPatientCorrelationCacheEntry.setLastUpdatedTime(this.getDate(rs.getTimestamp(7)));
                foundPatientCorrelationCacheEntry.setExpirationTime(this.getDate(rs.getTimestamp(8)));
            }
        } catch (SQLException ex) {
            throw new PatientCorrelationCacheException("Failure reading Patient Correlations from database" + ex.getMessage());
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
        return foundPatientCorrelationCacheEntry;
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
     * @param patientCorrelationCacheEntry
     * @param expirationDays
     */
    private void setTimes(PatientCorrelationCacheEntry patientCorrelationCacheEntry, int expirationDays) {
        Date now = new Date();
        patientCorrelationCacheEntry.setLastUpdatedTime(now);

        // FIXME ... need to set a proper expiration time
        Date expirationTime = this.addDaysToDate(now, expirationDays);
        patientCorrelationCacheEntry.setExpirationTime(expirationTime);
    }

    /**
     * 
     * @param date
     * @param daysOffset
     * @return
     */
    private Date addDaysToDate(Date date, int daysOffset) {
        Calendar c = new GregorianCalendar();
        // FIXME: ? should we use UTC for all dates?
        //c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setTime(date);
        c.add(Calendar.DATE, daysOffset);
        return c.getTime();
    }
}
