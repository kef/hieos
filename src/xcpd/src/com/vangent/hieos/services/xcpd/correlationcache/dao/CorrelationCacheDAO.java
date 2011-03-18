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
package com.vangent.hieos.services.xcpd.correlationcache.dao;

import com.vangent.hieos.services.xcpd.correlationcache.exception.CorrelationCacheException;
import com.vangent.hieos.services.xcpd.correlationcache.model.CorrelationCacheEntry;

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
public class CorrelationCacheDAO {

    private final static Logger logger = Logger.getLogger(CorrelationCacheDAO.class);
    private final Connection connection;

    private CorrelationCacheDAO() {
        // Do nothing.
        this.connection = null;
    }

    /**
     * 
     * @param connection
     */
    public CorrelationCacheDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * 
     * @param correlationCacheEntry
     * @throws CorrelationCacheException
     */
    public void store(CorrelationCacheEntry correlationCacheEntry) throws CorrelationCacheException {
        // Update times.
        this.setTimes(correlationCacheEntry);

        // First see if the correlation already exists.
        CorrelationCacheEntry foundCorrelationCacheEntry = this.lookup(correlationCacheEntry);
        if (foundCorrelationCacheEntry == null) {
            correlationCacheEntry.setId(UuidAllocator.allocate());
            logger.debug("Inserting correlation: " + correlationCacheEntry.getVitals());
            this.insert(correlationCacheEntry);
        } else {
            logger.debug("Updating correlation: " + foundCorrelationCacheEntry.getVitals());
            correlationCacheEntry.setId(foundCorrelationCacheEntry.getId());
            logger.debug(" .... to correlation: " + correlationCacheEntry.getVitals());
            this.update(correlationCacheEntry);
        }
    }

    /**
     * 
     * @param correlationCacheEntries
     * @throws CorrelationCacheException
     */
    public void store(List<CorrelationCacheEntry> correlationCacheEntries) throws CorrelationCacheException {
        for (CorrelationCacheEntry correlationCacheEntry : correlationCacheEntries) {
            this.store(correlationCacheEntry);
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
        String sql = "SELECT id,localhome,localpatientid,remotehome,remotepatientid,status,lastupdatetime,expirationtime FROM patientcorrelation WHERE localpatientid = ? AND localhome = ?";
        List<CorrelationCacheEntry> correlationCacheEntries = new ArrayList<CorrelationCacheEntry>();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, localPatientId);
            stmt.setString(2, localHomeCommunityId);
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(CorrelationCacheEntry) = " + sql);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                CorrelationCacheEntry correlationCacheEntry = new CorrelationCacheEntry();
                correlationCacheEntry.setId(rs.getString(1));
                correlationCacheEntry.setLocalHomeCommunityId(rs.getString(2));
                correlationCacheEntry.setLocalPatientId(rs.getString(3));
                correlationCacheEntry.setRemoteHomeCommunityId(rs.getString(4));
                correlationCacheEntry.setRemotePatientId(rs.getString(5));
                // FIXME ....
                //correlationCacheEntry.setStatus(rs.getString(6).charAt(0));
                //correlationCacheEntry.setLastUpdatedTime(this.getDate(rs.getTimestamp(7)));
                //correlationCacheEntry.setExpirationTime(this.getDate(rs.getTimestamp(8)));
                correlationCacheEntries.add(correlationCacheEntry);
            }
        } catch (SQLException ex) {
            throw new CorrelationCacheException("Failure reading Correlations from database" + ex.getMessage());
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
        return correlationCacheEntries;
    }

    /**
     *
     * @param correlationCacheEntry
     * @throws CorrelationCacheException
     */
    private void insert(CorrelationCacheEntry correlationCacheEntry) throws CorrelationCacheException {
        String sql = "INSERT INTO patientcorrelation (id,localhome,localpatientid,remotehome,remotepatientid,status,lastupdatetime,expirationtime) values (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, correlationCacheEntry.getId());
            stmt.setString(2, correlationCacheEntry.getLocalHomeCommunityId());
            stmt.setString(3, correlationCacheEntry.getLocalPatientId());
            stmt.setString(4, correlationCacheEntry.getRemoteHomeCommunityId());
            stmt.setString(5, correlationCacheEntry.getRemotePatientId());
            // FIXME ?? ....
            stmt.setString(6, Character.toString(correlationCacheEntry.getStatus()));
            stmt.setTimestamp(7, this.getTimestamp(correlationCacheEntry.getLastUpdatedTime()));
            stmt.setTimestamp(8, this.getTimestamp(correlationCacheEntry.getExpirationTime()));
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(CorrelationCacheEntry) = " + sql);
            }
            stmt.execute();
        } catch (SQLException ex) {
            logger.error("Failure inserting CorrelationCacheEntry", ex);
            throw new CorrelationCacheException(ex.getMessage());
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
     * @param correlationCacheEntry
     * @throws CorrelationCacheException
     */
    private void update(CorrelationCacheEntry correlationCacheEntry) throws CorrelationCacheException {
        String sql = "UPDATE patientcorrelation SET localhome=?, localpatientid=?, remotehome=?, remotepatientid=?, status=?, lastupdatetime=?, expirationtime=? WHERE id = ?";
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, correlationCacheEntry.getLocalHomeCommunityId());
            stmt.setString(2, correlationCacheEntry.getLocalPatientId());
            stmt.setString(3, correlationCacheEntry.getRemoteHomeCommunityId());
            stmt.setString(4, correlationCacheEntry.getRemotePatientId());
            // FIXME? ....
            stmt.setString(5, Character.toString(correlationCacheEntry.getStatus()));
            stmt.setTimestamp(6, this.getTimestamp(correlationCacheEntry.getLastUpdatedTime()));
            stmt.setTimestamp(7, this.getTimestamp(correlationCacheEntry.getExpirationTime()));
            stmt.setString(8, correlationCacheEntry.getId());
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(CorrelationCacheEntry) = " + sql);
            }
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            logger.error("Failure updating CorrelationCacheEntry", ex);
            throw new CorrelationCacheException(ex.getMessage());
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
     * @param correlationCacheEntry
     * @return
     * @throws CorrelationCacheException
     */
    private CorrelationCacheEntry lookup(CorrelationCacheEntry correlationCacheEntry) throws CorrelationCacheException {
        String sql = "SELECT id,localhome,localpatientid,remotehome,remotepatientid,status,lastupdatetime,expirationtime FROM patientcorrelation WHERE localpatientid = ? AND localhome = ? AND remotehome = ?";
        CorrelationCacheEntry foundCorrelationCacheEntry = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, correlationCacheEntry.getLocalPatientId());
            stmt.setString(2, correlationCacheEntry.getLocalHomeCommunityId());
            stmt.setString(3, correlationCacheEntry.getRemoteHomeCommunityId());
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(CorrelationCache) = " + sql);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                // OK ... none found
            } else {
                foundCorrelationCacheEntry = new CorrelationCacheEntry();
                foundCorrelationCacheEntry.setId(rs.getString(1));
                foundCorrelationCacheEntry.setLocalHomeCommunityId(rs.getString(2));
                foundCorrelationCacheEntry.setLocalPatientId(rs.getString(3));
                foundCorrelationCacheEntry.setRemoteHomeCommunityId(rs.getString(4));
                foundCorrelationCacheEntry.setRemotePatientId(rs.getString(5));
                foundCorrelationCacheEntry.setStatus(rs.getString(6).charAt(0));
                foundCorrelationCacheEntry.setLastUpdatedTime(this.getDate(rs.getTimestamp(7)));
                foundCorrelationCacheEntry.setExpirationTime(this.getDate(rs.getTimestamp(8)));
            }
        } catch (SQLException ex) {
            throw new CorrelationCacheException("Failure reading Correlations from database" + ex.getMessage());
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
        return foundCorrelationCacheEntry;
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
     * @param correlationCacheEntry
     */
    private void setTimes(CorrelationCacheEntry correlationCacheEntry) {
        Date currentDate = new Date();
        correlationCacheEntry.setLastUpdatedTime(currentDate);

        // FIXME ... need to set a proper expiration time
        correlationCacheEntry.setExpirationTime(currentDate);
    }
}
