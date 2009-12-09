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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vangent.hieos.xlog.server;

import com.vangent.hieos.xutil.db.support.SQLConnectionWrapper;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xlog.client.XLogMessage.XLogMessageNameValue;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.ObjectMessage;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.ResultSet;

/**
 *
 * @author Bernie Thuman
 */
@MessageDriven(mappedName = "jms/XLogger", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class XLoggerBean implements MessageListener {

    private final static Logger logger = Logger.getLogger(XLoggerBean.class);

    /**
     *
     */
    public XLoggerBean() {
    }

    /**
     *
     * @param message
     */
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage m = (ObjectMessage) message;
                Object so = m.getObject();  // Serialized object.
                if (so instanceof XLogMessage) {
                    XLogMessage logMessage = (XLogMessage) so;
                    logger.info("SERVER logMessage id = " + logMessage.toString());
                    logger.info("SERVER logMessage (computed id) = " + logMessage.getMessageID());
                    this.persist(logMessage);
                }
            } else if (message instanceof TextMessage) {
                TextMessage m = (TextMessage) message;
                logger.info("--- Received message ");
                logger.info(m.getText());
                logger.info("----------");
            } else {
                logger.info("Received message of type " + message.getClass().getName());
            }
        } catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace(System.err);
        }
    }

    /**
     * Invokes the methods to create the audit log entries
     * @param logMessage
     */
    private void persist(XLogMessage logMessage) {
        Connection conn = this.getConnection();
        if (conn == null) {
            // Keep going..
            return;
        }

        try {
            conn.setAutoCommit(false);
            persistIp(conn, logMessage);
            persistMain(conn, logMessage);
            persistEntries(conn, logMessage);
            conn.commit();
        } catch (SQLException ex) {
            logger.error("SQLException: ", ex);
            ex.printStackTrace();
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
     * @throws java.sql.SQLException
     */
    private void persistIp(Connection conn, XLogMessage logMessage) throws SQLException {
        // First see if the IP table needs to have an entry.
        if (this.ipExists(conn, logMessage.getIpAddress()) == false) {
            String sql = "INSERT INTO IP (ip,company_name,email) VALUES (" +
                this.getSQLQuotedString(logMessage.getIpAddress()) + "," +
                this.getSQLQuotedString(logMessage.getIpAddress()) + "," +
                "'UNKNOWN')";
            if(logger.isTraceEnabled())
                logger.trace("LOG IP SQL: " + sql);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            if (stmt !=null)
                stmt.close();
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
        if(logger.isTraceEnabled())
            logger.trace("LOG LOOKUP IP = " + sql);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        boolean result = false;
        if (rs.getInt(1) == 0) {
            result = false;
        } else if (rs.getInt(1) > 0) {
            result = true;
        }
        if(logger.isTraceEnabled())
            logger.trace("IP Found = " + result);
        if (rs !=null)
            rs.close();
        if (stmt !=null)
            stmt.close();
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
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, logMessage.getMessageID());
        stmt.setString(2, convertBooleanToString(logMessage.isSecureConnection()));
        stmt.setString(3, logMessage.getIpAddress());
        stmt.setTimestamp(4, timestamp);
        stmt.setString(5, logMessage.getTestMessage());
        stmt.setString(6, convertBooleanToString(logMessage.isPass()));

        if (logger.isTraceEnabled())
            logger.trace("SQL(LOG-MAIN) = " + sql);
        stmt.execute();
        if (stmt !=null)
            stmt.close();
    }

    /**
     * Creates the LOGDETAIL records for the log entry details
     *
     * @param conn
     * @param logMessage
     * @throws java.sql.SQLException
     */
    private void persistEntries(Connection conn, XLogMessage logMessage) throws SQLException {
        HashMap<String, Vector<XLogMessageNameValue>> entries = logMessage.getEntries();

        // Setup the prepared statement for the log entries
        String sql = "INSERT INTO LOGDETAIL (type,messageid,name,value,seqid) VALUES(?,?,?,?,?)";
        if(logger.isTraceEnabled())
            logger.trace("SQL(LOG-LOGDETAIL) = " + sql);
        PreparedStatement pstmt = conn.prepareStatement(sql);

        // Now iterate over each detailed entry in the hashmap.
        Set<String> keys = entries.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            //logger.trace("Log processing - " + key);
            // Now, process all entries.
            Vector<XLogMessageNameValue> nameValues = entries.get(key);
            Iterator nameValueIterator = nameValues.iterator();
            int seqId = 0;
            while (nameValueIterator.hasNext()) {
                XLogMessageNameValue nameValue = (XLogMessageNameValue) nameValueIterator.next();

                // create the insert statement
                this.getSQLInsertStatementForParam(pstmt, logMessage, key, nameValue, ++seqId);
            }
        }

        // submit the batch of statements for execution
        int[] updateCounts = pstmt.executeBatch();
        if (pstmt !=null)
            pstmt.close();
        if(logger.isTraceEnabled())
            logger.trace("Number of LOG Rows Inserted: " + updateCounts.length);
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
      private void getSQLInsertStatementForParam(PreparedStatement pstmt, XLogMessage logMessage, String logType, XLogMessageNameValue nameValue, int seqId)
            throws SQLException{
        if(logger.isTraceEnabled())
            logger.trace("(LOG-DETAIL) ID, NAME, SEQ, SIZE & VALUE: " + logType + ";" + logMessage.getMessageID() + ";" + nameValue.getName() + ";" + seqId
                    + ";" + nameValue.getValue().length() + ";" + nameValue.getValue());
        pstmt.setString(1, logType);
        pstmt.setString(2, logMessage.getMessageID());
        pstmt.setString(3, nameValue.getName());
        pstmt.setBinaryStream(4, new ByteArrayInputStream(nameValue.getValue().getBytes()), nameValue.getValue().length());
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
    private String convertBooleanToString(boolean value){
        if (value)
            return "T";
        else
            return "F";
    }
}
