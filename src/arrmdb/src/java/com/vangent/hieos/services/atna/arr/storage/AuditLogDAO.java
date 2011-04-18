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
package com.vangent.hieos.services.atna.arr.storage;

import com.vangent.hieos.services.atna.arr.support.ATNALog;
import com.vangent.hieos.services.atna.arr.support.AuditException;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class AuditLogDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(AuditLogDAO.class);

    public AuditLogDAO(Connection conn) {
        super(conn);
    }

    /**
     * Retrieves the database table name
     *
     */
    public static String getTableNameStatic() {
        return "auditlog";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    @Override
    protected void loadObject(Object obj, ResultSet rs) throws AuditException {
    }

    @Override
    protected Object createObject() throws AuditException {
        ATNALog log = new ATNALog();
        return log;
    }

    /**
     * Create the prepared statement with the SQL code
     *
     */
    @Override
    protected PreparedStatement createPreparedStatement() throws AuditException {
        String stmtFragment = null;
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO " + getTableName() +
                    " values(?,?,?,?,?,?,?)";

        } else if (action == DAO_ACTION_DELETE) {
            stmtFragment = "DELETE " + getTableName() +
                    " where uniqueid = ?";
        }

        try {
            PreparedStatement pstmt = conn.prepareStatement(stmtFragment);
            return pstmt;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new AuditException(ex);
        }
    }

    /**
     * Returns the prepared statement with bind variables populated
     *
     */
    @Override
    protected PreparedStatement setPreparedStatement(PreparedStatement pstmt, Object object) throws AuditException {
        try {
            ATNALog al = (ATNALog) object;
            if (logger.isTraceEnabled()) {
                logger.trace("AL Prepared Statement For: " + al.getUniqueID());
            }
            if (action == DAO_ACTION_INSERT) {
                pstmt.setString(1, al.getUniqueID());
                pstmt.setString(2, al.getClientIPAddress());
                pstmt.setString(3, al.getClientPort());
                pstmt.setTimestamp(4, new Timestamp((al.getReceivedDateTime()).getTime()));
                if (al.getXml() == null) {
                    pstmt.setBinaryStream(5, null, 0);
                } else {
                    pstmt.setBinaryStream(5, new ByteArrayInputStream(al.getXml().getBytes()), al.getXml().length());
                }
                pstmt.setString(6, al.getProtocol());
                if (al.getErrorMessage() == null || al.getErrorMessage().length() < 500) {
                    pstmt.setString(7, al.getErrorMessage());
                } else {
                    logger.warn("Error Message Truncated in AuditLog: " + al.getUniqueID() + ", FULL ERROR: " + al.getErrorMessage());
                    pstmt.setString(7, al.getErrorMessage().substring(0, 500));
                }

            } else if (action == DAO_ACTION_DELETE) {
                pstmt.setString(1, al.getUniqueID());
            }
            return pstmt;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new AuditException(ex);
        }
    }

    /**
     * Retrieves all ATNA Logs for a specified IP Address
     *
     * @param searchCriteria
     * @return
     * @throws AuditException
     */
    public List<ATNALog> getATNALogs(ATNALog searchCriteria) throws AuditException {
        List<ATNALog> atnaLogs = new ArrayList<ATNALog>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT uniqueid, clientipaddress, receiveddatetime, protocol, xml, errorMessage " +
                    " FROM auditlog WHERE clientipaddress = ? order by receiveddatetime desc";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, searchCriteria.getClientIPAddress());
            //logger.info("SQL = " + sql);

            rs = stmt.executeQuery();
            while (rs.next()) {
                ATNALog log = new ATNALog();
                log.setUniqueID(rs.getString(1));
                log.setClientIPAddress(rs.getString(2));
                log.setReceivedDateTime((java.util.Date) rs.getTimestamp(3));
                log.setProtocol(rs.getString(4));
                log.setXml(rs.getString(5));
                log.setErrorMessage(rs.getString(6));
                atnaLogs.add(log);
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new AuditException(e);
        } finally {
            try {
                stmt.close();
                rs.close();
            } catch (SQLException ex) {
                logger.error("Error Closing DB Statement & ResultSet " + ex);
            }
        }
        return atnaLogs;
    }

    /**
     * Gets a List of all unique IP Address in the AuditLogs
     * 
     * @return
     * @throws AuditException
     */
    public List<String> getIPAddresses() throws AuditException {
        List<String> ipAddresses = new ArrayList<String>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT distinct clientipaddress FROM auditlog";
            stmt = conn.prepareStatement(sql);
            logger.info("SQL = " + sql);

            rs = stmt.executeQuery();
            while (rs.next()) {
                logger.info("IPAddress: " + rs.getString(1));
                ipAddresses.add(rs.getString(1));
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new AuditException(e);
        } finally {
            try {
                stmt.close();
                rs.close();
            } catch (SQLException ex) {
                logger.error("Error Closing DB Statement & ResultSet " + ex);
            }
        }
        return ipAddresses;
    }

    /**
     * Retrieve an ATNA Log based on unique id
     *
     * @param uniqueId
     * @return
     * @throws AuditException
     */
    public ATNALog queryObject(String uniqueId) throws AuditException {
        ATNALog log = new ATNALog();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT uniqueid, clientipaddress, clientport, receiveddatetime, xml, protocol, errormessage FROM auditlog " +
                    " WHERE clientipaddress = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uniqueId);
            logger.info("SQL = " + sql);

            rs = stmt.executeQuery();
            while (rs.next()) {
                log.setUniqueID(rs.getString(1));
                log.setClientIPAddress(rs.getString(2));
                log.setClientPort(rs.getString(3));
                log.setReceivedDateTime(rs.getDate(4));
                log.setXml(rs.getString(5));
                log.setProtocol(rs.getString(6));
                log.setErrorMessage(rs.getString(7));
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new AuditException(e);
        } finally {
            try {
                stmt.close();
                rs.close();
            } catch (SQLException ex) {
                logger.error("Error Closing DB Statement & ResultSet " + ex);
            }
        }
        return log;
    }
    
}
