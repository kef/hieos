/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vangent.hieos.xutil.xlog.client;

import com.vangent.hieos.xutil.db.support.SQLConnectionWrapper;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xlog.client.XLogMessage.XLogMessageNameValue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;


/**
 * 
 * @author Bernie Thuman
 */
public class XLogMessageDAO {

    private final static Logger logger = Logger.getLogger(XLogMessageDAO.class);

    /**
     *
     */
    public XLogMessageDAO() {
    }

    /**
     * Invokes the methods to create the audit log entries
     * @param logMessage
     */
    public void persist(XLogMessage logMessage) {
        Connection conn = this.getConnection();
        if (conn == null) {
            return;
        }

        try {
            // Need to commit the IP first to avoid a race condition with other log requests.
            conn.setAutoCommit(true);
            persistIp(conn, logMessage);

            // Now go back to auto commit mode = false for efficiency purposes.
            conn.setAutoCommit(false);
            persistMain(conn, logMessage);
            persistEntries(conn, logMessage);
            conn.commit();
        } catch (SQLException ex) {
            logger.error("SQLException: ", ex);
            ex.printStackTrace(System.out);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                // Keep going.
                logger.error("SQLException: ", ex);
            }
        }
    }

    /**
     * Creates the IP record if it does not already exist
     *
     * @param conn
     * @param logMessage
     */
    private void persistIp(Connection conn, XLogMessage logMessage) throws SQLException {
        // First see if the IP table needs to have an entry.
        if (this.ipExists(conn, logMessage.getIpAddress()) == false) {
            String sql = getSQLInsertStatementForIp(logMessage);
            if (logger.isTraceEnabled()) {
                logger.trace("LOG IP SQL: " + sql);
            }
            Statement stmt = null;
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate(sql);
            } catch (SQLException ex) {
                logger.error("SQLState: " + ex.getSQLState());
                logger.error("SQLException: " + ex.getMessage());
                if (this.ipExists(conn, logMessage.getIpAddress()) == true){
                    // This is OK ... ignore this error since this addresses a "race condition".
                    logger.error("Error OK: IP record already exists");
                } else {
                    throw ex;
                }
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException ex) {
                        // Keep going.
                        logger.error("SQLException: ", ex);
                    }
                }
            }
        }
    }

    /**
     * Checks if an IP record already exists for the server/ip address
     *
     * @param conn
     * @param ipAddress
     * @return boolean
     * @throws java.sql.SQLException
     */
    private boolean ipExists(Connection conn, String ipAddress) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ip WHERE ip = " + this.getSQLQuotedString(ipAddress);
        if (logger.isTraceEnabled()) {
            logger.trace("LOG LOOKUP IP = " + sql);
        }
        Statement stmt = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            rs.next();
            if (rs.getInt(1) == 0) {
                result = false;
            } else if (rs.getInt(1) > 0) {
                result = true;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("IP Found = " + result);
            }
        } catch (SQLException ex) {
            logger.error("SQLException: ", ex);
            ex.printStackTrace(System.out);
            throw ex;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    // Keep going.
                    logger.error("SQLException: ", ex);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    // Keep going.
                    logger.error("SQLException: ", ex);
                }
            }
        }
        return result;
    }

    /**
     * Creates a MAIN record for the log entries
     *
     * @param conn
     * @param logMessage
     * @throws java.sql.SQLException
     */
    private void persistMain(Connection conn, XLogMessage logMessage) throws SQLException {
        // Get the timestamp properly formatted.
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(logMessage.getTimeStamp());
        Timestamp timestamp = new Timestamp(gc.getTimeInMillis());

        String sql = "INSERT INTO MAIN (messageid,is_secure,ip,timereceived,test,pass) VALUES(?,?,?,?,?,?)";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, logMessage.getMessageID());
            stmt.setString(2, convertBooleanToString(logMessage.isSecureConnection()));
            stmt.setString(3, logMessage.getIpAddress());
            stmt.setTimestamp(4, timestamp);
            stmt.setString(5, logMessage.getTestMessage());
            stmt.setString(6, convertBooleanToString(logMessage.isPass()));
            if (logger.isTraceEnabled()) {
                logger.trace("SQL(LOG-MAIN) = " + sql);
            }
            stmt.execute();
        } catch (SQLException ex) {
            logger.error("SQLException: ", ex);
            ex.printStackTrace(System.out);
            throw ex;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    // Keep going.
                    logger.error("SQLException: ", ex);
                }
            }
        }
    }

    /**
     * Creates the LOGDETAIL records for the log entry details
     *
     * @param conn
     * @param logMessage
     */
    private void persistEntries(Connection conn, XLogMessage logMessage) {
        HashMap<String, List<XLogMessageNameValue>> entries = logMessage.getEntries();

        // Setup the prepared statement for the log entries
        String sql = "INSERT INTO LOGDETAIL (type,messageid,name,value,seqid) VALUES(?,?,?,?,?)";
        if (logger.isTraceEnabled()) {
            logger.trace("SQL(LOG-LOGDETAIL) = " + sql);
        }
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);

            // Now iterate over each detailed entry in the hashmap.
            Set<String> keys = entries.keySet();
            Iterator it = keys.iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                //logger.trace("Log processing - " + key);
                // Now, process all entries.
                List<XLogMessageNameValue> nameValues = entries.get(key);
                Iterator nameValueIterator = nameValues.iterator();
                int seqId = 0;
                while (nameValueIterator.hasNext()) {
                    XLogMessageNameValue nameValue = (XLogMessageNameValue) nameValueIterator.next();
                    this.addLogDetailEntryToBatch(pstmt, logMessage, key, nameValue, seqId);
                    ++seqId;
                }
            }

            // submit the batch of statements for execution
            int[] updateCounts = pstmt.executeBatch();
            if (logger.isTraceEnabled()) {
                logger.trace("Number of LOG Rows Inserted: " + updateCounts.length);
            }
        } catch (SQLException ex) {
            logger.error("SQLException: ", ex);
            ex.printStackTrace(System.out);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    // Keep going.
                    logger.error("SQLException: ", ex);
                }
            }
        }
    }

    /**
     *
     * @param logMessage
     * @return
     */
    private String getSQLInsertStatementForIp(XLogMessage logMessage) {
        return "INSERT INTO IP (ip,company_name,email) VALUES (" +
                    this.getSQLQuotedString(logMessage.getIpAddress()) + "," +
                    this.getSQLQuotedString(logMessage.getIpAddress()) + "," +
                    "'UNKNOWN')";
    }

    /**
     * Sets up a batch of prepared statements to create the Log detail records
     *
     * @param pstmt
     * @param logMessage
     * @param logType
     * @param nameValue
     * @param seqId
     * @throws java.sql.SQLException
     */
    private void addLogDetailEntryToBatch(PreparedStatement pstmt, XLogMessage logMessage, String logType, XLogMessageNameValue nameValue, int seqId)
            throws SQLException {
        if (logger.isTraceEnabled()) {
            logger.trace("(LOG-DETAIL) ID, NAME, SEQ, SIZE & VALUE: " + logType + ";" + logMessage.getMessageID() + ";" + nameValue.getName() + ";" + seqId + ";" + nameValue.getValue().length());
        }
        pstmt.setString(1, logType);
        pstmt.setString(2, logMessage.getMessageID());
        pstmt.setString(3, nameValue.getName());
        pstmt.setString(4, nameValue.getValue());
        pstmt.setInt(5, new Integer(seqId));
        pstmt.addBatch();
    }

    /**
     *
     * @param toReplace
     * @return
     */
    private String replaceQuotes(String toReplace) {
        StringBuffer buff = new StringBuffer(toReplace);
        for (int i = 0; i < buff.length(); i++) {
            if (buff.charAt(i) == '\'') {
                buff.insert(i, '\'');
                i++;
            }
        }
        return new String(buff);
    }

    /**
     * Wraps a string in single quotes
     * @param val
     * @return
     */
    private String getSQLQuotedString(String val) {
        return "'" + replaceQuotes(val) + "'";
    }

    /**
     * Obtain database connection
     * @return Database connection on success.  Otherwise, null.
     */
    private Connection getConnection() {
        Connection con = null;
        SQLConnectionWrapper conWrapper = new SQLConnectionWrapper();
        try {
            con = conWrapper.getConnection(SQLConnectionWrapper.logJNDIResourceName);
        } catch (XdsInternalException ex) {
            logger.error("ERROR Retrieving DB Connection: ", ex);
        }
        return con;
    }

    /**
     * Converts a boolean to a string  - T or F
     * @param value
     * @return String
     */
    private String convertBooleanToString(boolean value) {
        return value == true ? "T" : "F";
    }
}
