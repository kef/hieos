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

/*
 * AdtJdbcConnection.java
 *
 * Created on October 4, 2004, 12:53 PM
 */
package com.vangent.hieos.adt.db;

import com.vangent.hieos.xutil.db.support.SQLConnectionWrapper;
import com.vangent.hieos.xutil.exception.XdsInternalException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 * For use in communicating with the ADT database.  At the moment, this is
 * hardcoded to talk to a PostgreSQL database, but this will become more
 * flexible in future releases.
 * @author Andrew McCaffrey
 */
public class AdtJdbcConnection {

    private final static Logger logger = Logger.getLogger(AdtJdbcConnection.class);
    private Connection con = null;
    private Statement stmt = null;
    /**
     * Constant representing the name of the ADT database table.
     */
    public static String ADT_MAIN_TABLE = "patient";
    /**
     *
     */
    public static String ADT_MAIN_UUID = "uuid";
    /**
     * Constant representing the patient ID column in the ADT database table.
     */
    public static String ADT_MAIN_PATIENTID = "id";
    /**
     *
     */
    public static String ADT_MAIN_STATUS = "status";
    /**
     *
     */
    public static String ADT_MAIN_ACTIVE_STATUS = "A";

    /**
     * Creates a new instance of JdbcConnection
     * @throws java.sql.SQLException Thrown if database access error.
     * @throws XdsInternalException
     */
    public AdtJdbcConnection() throws java.sql.SQLException, XdsInternalException {
        this.initialize();
    }

    /**
     *
     * @throws java.sql.SQLException
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void initialize() throws XdsInternalException, SQLException {
        con = this.getConnection();
        try {
            // Changed the resultset scroll type from insensitive to sensitive because MS SQL Server does not
            // support scroll type insensitive with concurrency type of update
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException ex) {
            logger.error("ADT: Could not create statement", ex);
            try {
                con.close();
                con = null;
            } catch (SQLException ex1) {
                logger.error("ADT: Could not close connection", ex1);
            }
            throw ex;
        }
    }

    /**
     *  Open ADT database connection.
     *
     * @return Database connection instance on success.  Null on failure.
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private Connection getConnection() throws XdsInternalException {
        return new SQLConnectionWrapper().getConnection(SQLConnectionWrapper.adtJNDIResourceName);
    }

    /**
     * Close the connection.
     */
    public void closeConnection() {
        try {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        } catch (SQLException e) {
            logger.error("ADT: Could not close statement", e);
        }
        try {
            if (con != null) {
                con.close();
                con = null;
            }
        } catch (SQLException e) {
            logger.error("ADT: Could not close connection", e);
        }
    }

    /**
     *
     * @param sql
     * @return
     * @throws java.sql.SQLException
     */
    private ResultSet executeQuery(String sql) throws SQLException {
        ResultSet result = null;
        logger.trace("SQL(adt) = " + sql);
        result = stmt.executeQuery(sql);
        return result;
    }

    /**
     * Executes the SQL update to the database.
     * @param sql The SQL of the update.
     * @throws java.sql.SQLException Thrown if database access error.
     * @return An int representing the number of rows affected by update.  (If zero,
     * then no update occured.)
     */
    private int executeUpdate(String sql) throws SQLException {
        logger.trace("SQL(adt) = " + sql);
        return stmt.executeUpdate(sql);
    }

    /**
     *
     * @return
     */
    private String getDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        return df.format(new Date());
    }

    /**
     *
     * @param record
     * @return
     * @throws java.sql.SQLException
     */
    public boolean addAdtRecord(AdtRecord record) throws SQLException {

        // First see if we are in INSERT or UPDATE mode.
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT uuid FROM ");
        sb.append(ADT_MAIN_TABLE);
        sb.append(" WHERE " + ADT_MAIN_UUID + " = '" + record.getUuid() + "'");
        ResultSet resultSet = this.executeQuery(sb.toString());
        boolean updateMode = resultSet.first();
        sb = new StringBuffer();
        if (updateMode == false) {
            // Insert:
            sb.append("INSERT INTO ");
            sb.append(ADT_MAIN_TABLE);
            sb.append(" (");
            sb.append(ADT_MAIN_PATIENTID + "," + ADT_MAIN_UUID + "," + ADT_MAIN_STATUS + "," + "timestamp" + ")");
            sb.append(" VALUES ");
            sb.append("('");
            sb.append(record.getPatientId() + "','" + record.getUuid() + "','" + record.getPatientStatus() + "','" + getDate() + "')");
            if (logger.isDebugEnabled()) {
                logger.debug("Patient ADD SQL: " + sb.toString());
            }
        } else {
            // Update:
            sb.append("UPDATE ");
            sb.append(ADT_MAIN_TABLE);
            sb.append(" SET ");
            sb.append("timestamp" + " = " + "'" + getDate() + "'");
            sb.append(" WHERE " + ADT_MAIN_UUID + " = '" + record.getUuid() + "'");
        }
        int rowsAffected = this.executeUpdate(sb.toString());
        return rowsAffected > 0;
    }

    /**
     * Queries the database to see if the given ID already exists in the database.
     * This method does an exist, case-sensitive search only.  Use
     * getInexactMatch(String) for wild-cards.
     * @param id The ID to query on.
     * @throws java.sql.SQLException Thrown if database access error.
     * @return Boolean.  True if ID does exist.  False if ID does not exist.
     */
    public boolean doesIdExist(String id) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT id ");
        sb.append("FROM " + ADT_MAIN_TABLE + " ");
        //sb.append("WHERE " + this.ADT_MAIN_PATIENTID + " = '" + id + "';");
        sb.append("WHERE " + ADT_MAIN_PATIENTID + " = '" + id + "'");
        ResultSet result = this.executeQuery(sb.toString());
        return result.next();
    }

    /**
     * This method checks if an Active Patient exists
     *
     * @param id The ID to query on.
     * @throws java.sql.SQLException Thrown if database access error.
     * @return Boolean.  True if active patient exists.  False if active patient does not exist.
     */
    public boolean doesActiveIdExist(String id) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT id ");
        sb.append("FROM " + ADT_MAIN_TABLE + " ");
        sb.append("WHERE " + ADT_MAIN_PATIENTID + " = '" + id + "' ");
        sb.append("AND " + ADT_MAIN_STATUS + " = '" + ADT_MAIN_ACTIVE_STATUS + "'");
        ResultSet result = this.executeQuery(sb.toString());
        return result.next();
    }

    /**
     *
     * @param patientId
     * @return
     * @throws java.sql.SQLException
     */
    public String getPatientUUID(String patientId) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT UUID ");
        sb.append("FROM " + ADT_MAIN_TABLE + " ");
        //sb.append("WHERE " + this.ADT_MAIN_PATIENTID + " = '" + patientId + "';");
        sb.append("WHERE " + ADT_MAIN_PATIENTID + " = '" + patientId + "'");
        ResultSet result = this.executeQuery(sb.toString());
        if (result.first() == false) {
            // not found.
            return null;  // Early exit.
        }
        return result.getString(ADT_MAIN_UUID);
    }

    /**
     *
     * @param uuid
     * @return
     * @throws java.sql.SQLException
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    public AdtRecord getAdtRecord(String uuid) throws SQLException, XdsInternalException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * ");
        sb.append("FROM " + ADT_MAIN_TABLE + " ");
        //sb.append("WHERE " + ADT_MAIN_UUID + " = '" + uuid + "';");
        sb.append("WHERE " + ADT_MAIN_UUID + " = '" + uuid + "'");
        ResultSet result = this.executeQuery(sb.toString());
        try {
            result.next();
        } catch (SQLException e) {
            // not found.
            return null;
        }
        AdtRecord record = new AdtRecord();
        record.setUuid(uuid);
        record.setPatientId(result.getString(ADT_MAIN_PATIENTID));
        record.setPatientStatus(result.getString(ADT_MAIN_STATUS));
        return record;
    }

    /**
     *
     * @param uuid
     * @throws SQLException
     */
    public void deleteAdtRecord(String uuid) throws SQLException {
        // Delete the main table entries.
        StringBuffer sb = new StringBuffer();
        sb.append("DELETE ");
        sb.append("FROM " + ADT_MAIN_TABLE + " ");
        //sb.append("WHERE " + this.ADT_MAIN_UUID + " = '" + uuid + "';");
        sb.append("WHERE " + ADT_MAIN_UUID + " = '" + uuid + "'");
        this.executeUpdate(sb.toString());
    }

    /**
     * Updates the Patient Status to Active (A) or Inactive (I)
     * @param uuid
     * @param status
     * @throws SQLException
     */
    public void updateAdtRecordStatus(String uuid, String status) throws SQLException {
        StringBuffer sb;
        int rowsAffected;

        // update the Patient table status.
        sb = new StringBuffer();
        sb.append("UPDATE ");
        sb.append(ADT_MAIN_TABLE);
        sb.append(" SET ");
        sb.append(ADT_MAIN_STATUS + " = " + "'" + status + "'").append(",");
        sb.append("timestamp" + " = " + "'" + getDate() + "'");
        sb.append(" WHERE " + ADT_MAIN_UUID + " = '" + uuid + "'");

        rowsAffected = this.executeUpdate(sb.toString());
        if (rowsAffected == 0) {
            throw new SQLException("ADT Patient not found with UUID = " + uuid);
        }
    }

    /**
     * This method retrieves the patient status
     * @param id The ID to query on.
     * @throws java.sql.SQLException Thrown if database access error.
     * @return String with Status.  NULL if patient does not exist.
     */
    public String getPatientStatus(String id) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT status ");
        sb.append("FROM " + ADT_MAIN_TABLE + " ");
        sb.append("WHERE " + ADT_MAIN_PATIENTID + " = '" + id + "'");
        ResultSet result = this.executeQuery(sb.toString());
        if (result.next()) {
            return result.getString(1);
        } else {
            return null;
        }
    }

    /**
     *
     * @return
     */
    public static String getCurrentTimestamp() {
        return new Date().toString();
    }

    /**
     * Creates a record of two patient records being merged or unmerged
     * @param survivingPatientId
     * @param priorRegistrationPatientId
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    public void createMergeHistory(String survivingPatientId, String priorRegistrationPatientId,
            String action, List externalIdentifierIds) throws SQLException {

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement("INSERT INTO MERGEHISTORY VALUES(?,?,?,?,?)");
            String uuid = UUID.randomUUID().toString();
            preparedStatement.setString(1, uuid);
            preparedStatement.setString(2, survivingPatientId);
            preparedStatement.setString(3, priorRegistrationPatientId);
            preparedStatement.setString(4, action);
            preparedStatement.setTimestamp(5, new Timestamp(new Date().getTime()));
            preparedStatement.executeUpdate();

            // Create a history of the merged identifiers
            createMergedIdentifiers(uuid, externalIdentifierIds);

        } catch (SQLException ex) {
            throw ex;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                logger.error("Could not close connection", e);
            }
        }
    }

    /** 
     * Checks if a patient has been merged into another patient.
     * This check is done before an unmerge is allowed
     *
     * @param survivingPatientId
     * @param priorRegistrationPatientId
     * @param action
     * @throws SQLException
     */
    public boolean isPatientMerged(String survivingPatientId, String priorRegistrationPatientId)
            throws SQLException {

        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            preparedStatement = con.prepareStatement("SELECT ACTION FROM MERGEHISTORY WHERE " +
                    "SURVIVINGPATIENTID = ? AND SUBSUMEDPATIENTID = ? " +
                    "ORDER BY DATETIMEPERFORMED DESC");
            preparedStatement.setString(1, survivingPatientId);
            preparedStatement.setString(2, priorRegistrationPatientId);

            rs = preparedStatement.executeQuery();

            if (rs.next()) {
                if (rs.getString(1).equals("M")) {
                    return true;
                } else {
                    // This means an unmerge has already taken place
                    return false;
                }
            } else {
                // This means a merge never occured for these ids
                return false;
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("Could not close DB objects", e);
            }
        }
    }

    /**
     * Creates a record of registry external identifiers involved in a merge or unmerge
     * @param mergedHistoryId
     * @param externalIdentifierIds
     * @throws SQLException
     */
    public void createMergedIdentifiers(String mergedHistoryId, List<String> externalIdentifierIds)
            throws SQLException {

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement("INSERT INTO MERGEDOBJECTS VALUES(?,?)");
            for (String id : externalIdentifierIds) {
                preparedStatement.setString(1, mergedHistoryId);
                preparedStatement.setString(2, id);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                logger.error("Could not close DB objects", e);
            }
        }
    }

    /**
     * Retrieves a list of registry external identifier IDs involved in a merge or unmerge
     *
     * @param survivingPatientId
     * @param priorRegistrationPatientId
     * @throws SQLException
     */
    public List retrieveMergedRecords(String survivingPatientId, String priorRegistrationPatientId,
            String action) throws SQLException {
        // Check for the merge history record
        String mergeHistoryId = getMergeHistoryID(survivingPatientId, priorRegistrationPatientId, action);
        if (mergeHistoryId == null) {
            // This means a merge or unmerge did not occur for these ids
            return null;
        }
        logger.debug("Merge History Id: " + mergeHistoryId);

        // Get the list of identifiers involved in the merge
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List externalIdentifierIds = new ArrayList<String>();
        try {
            preparedStatement = con.prepareStatement("SELECT EXTERNALIDENTIFIERID " +
                    "FROM MERGEDOBJECTS WHERE PARENTID = ?");
            preparedStatement.setString(1, mergeHistoryId);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                externalIdentifierIds.add(rs.getString(1));
                logger.debug("EXTERNALIDENTIFIERID: " + rs.getString(1));
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("Could not close DB Objects", e);
            }
        }
        return externalIdentifierIds;
    }

    /**
     * Retrieves the id of the most recent merge or unmerge history record for
     * the specified patient ids
     *
     * @param survivingPatientId
     * @param priorRegistrationPatientId
     * @param action
     * @throws SQLException
     */
    private String getMergeHistoryID(String survivingPatientId, String priorRegistrationPatientId,
            String action) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        String historyId;
        try {
            preparedStatement = con.prepareStatement("SELECT UNIQUEID FROM MERGEHISTORY WHERE " +
                    "SURVIVINGPATIENTID = ? AND SUBSUMEDPATIENTID = ? AND ACTION = ? " +
                    "ORDER BY DATETIMEPERFORMED DESC");
            preparedStatement.setString(1, survivingPatientId);
            preparedStatement.setString(2, priorRegistrationPatientId);
            preparedStatement.setString(3, action);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                historyId = rs.getString(1);
                return historyId;
            } else {
                // This means a merge or unmerge did not occur for these ids
                return null;
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                logger.error("Could not close DB objects", e);
            }
        }
    }
}
