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

package com.vangent.hieos.logbrowser.log.db;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import org.apache.log4j.Logger;

enum MainTableFields {

    messageid, ip, timereceived, test, pass, is_secure
};

/**
 * Class reprensenting the main informations to display in the table of messages.
 * @author jbmeyer
 *
 */
public class MainTable extends AbstractLogTable {

    public static final String MESSAGE_ID = "messageid";
    public static final String IP = "ip";
    public static final String TIMESTAMP = "timereceived";
    public static final String TEST = "test";
    public static final String PASS = "pass";
    public static final String IS_SECURE = "is_secure";
    public static final String TABLE_NAME = "main";

    public static final String readSqlCommand = "SELECT messageid, is_secure, ip, timereceived, test, pass FROM main WHERE messageid = ?";
    public static final String deleteMessageCommand = "DELETE FROM main WHERE messageid = ?";

    private PreparedStatement readPreparedStatement;
    private PreparedStatement deletePreparedStatement;
    private InetAddress ipAddress;
    private Timestamp timestamp;
    private String test;
    private String pass;
    private String isSecure;

    private final static Logger logger = Logger.getLogger(MainTable.class);

    /**
     *
     * @param c
     * @throws LoggerException
     */
    public MainTable(Connection c) throws LoggerException {
        conn = c;
        tableName = TABLE_NAME;
        //readSqlCommand = "select " + MESSAGE_ID + " , " + IS_SECURE + " , " + IP + " ," + TIMESTAMP + " , " + TEST + " , " + PASS + " FROM " + TABLE_NAME + " where " + MESSAGE_ID + " = ?";
        //deleteMessageCommand = "delete FROM " + TABLE_NAME + " WHERE " + MESSAGE_ID + " =?";
        try {
            if (conn == null || conn.isClosed()) {
                throw new LoggerException("Database null or closed");
            }

            readPreparedStatement = conn.prepareStatement(readSqlCommand);
            deletePreparedStatement = conn.prepareStatement(deleteMessageCommand);
            test = new String();
        } catch (SQLException sqlException) {
            throw new LoggerException("Database problem (SqlException ) " + sqlException.getMessage());
        }
    }

    /*************GETTERS AND SETTERS*********************/
    /**
     * 
     * @return
     */
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    /**
     *
     * @param ipAddress
     */
    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     *
     * @return
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     *
     * @param messageId
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     *
     * @return
     */
    public String isPass() {
        return pass;
    }

    /**
     *
     * @param pass
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     *
     * @return
     */
    public String getTest() {
        return test;
    }

    /**
     *
     * @param test
     */
    public void setTest(String test) {
        this.test = test;
    }

    /**
     *
     * @return
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @param timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     *
     * @param inMessageId
     * @return
     * @throws LoggerException
     */
    public int readFromDB(String inMessageId) throws LoggerException {
        ResultSet result = null;
        try {
            if (conn == null || conn.isClosed()) {
                throw new LoggerException("Database null or closed");
            }

            if (readSqlCommand != null) {
                if (messageExist(inMessageId, MainTable.TABLE_NAME)) {
                    if (readPreparedStatement != null) {
                        readPreparedStatement.setString(1, inMessageId);
                        result = readPreparedStatement.executeQuery();
                        result.next();
                        messageId = result.getString(1);
                        isSecure = result.getString(2);
                        try {
                            ipAddress = InetAddress.getByName(result.getString(3));
                        } catch (UnknownHostException e) {
                        }
                        timestamp = result.getTimestamp(4);
                        test = result.getString(5);
                        pass = result.getString(6);
                    }
                } else {
                    return -1;
                }
            } else {
                return -1;
            }

            return 0;
        } catch (SQLException sqlException) {
            logger.error("Database problem (SqlException ) " + sqlException.getMessage());
            throw new LoggerException("Database problem (SqlException ) " + sqlException.getMessage());
        }
        finally {
            if (result != null){
                try {
                    result.close();
                } catch (SQLException ex) {
                    logger.error(ex);
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public String toString() {
        return "MessageId:" + messageId +
                "\nIP :" + ipAddress.getHostAddress() +
                "\nTimestamp:" + timestamp.toString() +
                "\nPass :" + pass +
                "\nTest :" + test + "\n";
    }

    /**
     *
     * @return
     */
    public String toXml() {
        StringBuffer stringBuff = new StringBuffer();
        stringBuff.append("<mainMessage>");
        stringBuff.append("	<node name=\"MessageId\" value=\"" + messageId + "\" />");
        stringBuff.append("	<node name=\"IP\" value=\"" + ipAddress.getHostAddress() + "\" />");
        stringBuff.append("	<node name=\"Timestamp\" value=\"" + timestamp.toString() + "\" />");
        stringBuff.append("	<node name=\"Pass\" value=\"" + pass + "\" />");
        stringBuff.append("	<node name=\"Test\" value=\"" + test + "\" />");
        stringBuff.append("</mainMessage>");
        return stringBuff.toString();
    }

    /**
     *
     * @return
     */
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("MessageId", messageId);
        map.put("IP", ipAddress.getHostAddress());
        map.put("Timestamp", timestamp.toString());
        map.put("Pass", pass);
        map.put("Test", test);
        return map;
    }

    /**
     *
     * @return
     */
    public String toJSon() {
        StringBuffer stringBuff = new StringBuffer();
        stringBuff.append("{ \"name\"  : \"mainMessage\"  , \n");
        stringBuff.append("  \"values\" : [\n ");
        stringBuff.append(" [ \"MessageId\" , \"" + messageId + "\"],\n ");
        if (ipAddress != null) {
            stringBuff.append("[ \"IP\"        , \"" + ipAddress.getHostAddress() + "\"] ,\n ");
        }
        if (timestamp != null) {
            stringBuff.append("[ \"Timestamp\" , \"" + timestamp.toString() + "\"],\n ");
        }
        stringBuff.append("[ \"Pass\"      , \"" + pass + "\" ], \n ");
        stringBuff.append("[ \"Test\"      , \"" + test + "\" ]\n]} ");
        return stringBuff.toString();
    }

    /**
     *
     * @return
     */
    public String isSecure() {
        return isSecure;
    }

    /**
     *
     * @param isSecure
     */
    public void setSecure(String isSecure) {
        this.isSecure = isSecure;
    }

    /**
     *
     * @param messageId
     */
    public void deleteMessage(String messageId) {
        deleteMessage(messageId, deletePreparedStatement);
    }

    /**
     * Close the Prepared Statements
     *
     */
    public void close() {
        try {
            if (readPreparedStatement != null) {
                readPreparedStatement.close();
            }
            if (deletePreparedStatement != null) {
                deletePreparedStatement.close();
            }
        } catch (SQLException ex) {
            logger.error("Error Closing PreparedStatements: " + ex);
        }
    }
}
