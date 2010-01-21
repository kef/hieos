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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.json.JSONArray;

/**
 * A generic table is a table containing 3 fields :
 *  <ui>
 *    <li>messageid</li>
 *    <li>name</li>
 *    <li>value</li>
 *   </ui>
 *  This table allow to store for a message , several parameters with their values
 * @author jbmeyer
 * @author Bernie Thuman (BHT) Clean up, more documentation, removed "on-the-fly" thinking.
 *
 */
public class GenericTable extends AbstractLogTable {

    public static String MESSAGE_ID = "messageid";
    public static String SEQUENCE_ID = "seqid";  // Added (BHT)
    public static String NAME = "name";
    public static String VALUE = "value";
    private String readSqlCommand = null;
    private PreparedStatement readPreparedStatement;
    private String parameterName;
    private String parameterValue;
    private String parameterType;
    private int sequenceId = 0;
    private final static Logger logger = Logger.getLogger(GenericTable.class);

    private GenericTable() {
    }

    /**
     * Initiate prepared statements to retrieve log details from the specified database
     * @param message
     * @throws LoggerException
     * 
     */
    public GenericTable(Message m) throws LoggerException {
        tableName = "logdetail";
        conn = m.getConnection();
        readSqlCommand = "select messageid, type, name, value FROM logdetail where messageid = ? order by  seqid";

        try {
            if (conn == null || conn.isClosed()) {
                throw new LoggerException("Database null or closed");
            }
            readPreparedStatement = conn.prepareStatement(readSqlCommand);
        } catch (SQLException sqlException) {
            logger.error("Database problem (SqlException ) " + sqlException.getMessage());
            throw new LoggerException("Database problem (SqlException ) " + sqlException.getMessage());
        }
    }

    /**
     * Initiate prepared statements for the log detail table in the database
     * @param message
     * @param inLogType
     * @throws LoggerException
     * @throws SQLException
     */
    public GenericTable(Message m, String inLogType) throws LoggerException {
        tableName = "logdetail";
        conn = m.getConnection();
        readSqlCommand = "select messageid, type, name, value FROM logdetail where type = '" + inLogType + "' and messageid = ? order by  seqid";
        try {
            if (conn == null || conn.isClosed()) {
                throw new LoggerException("Database null or closed");
            }
            readPreparedStatement = conn.prepareStatement(readSqlCommand);
        } catch (SQLException sqlException) {
            logger.error("Database problem (SqlException ) " + sqlException.getMessage());
            throw new LoggerException("Database problem (SqlException ) " + sqlException.getMessage());
        }
    }

    /**
     * Read a message in the detabase and return an array of parameters and values
     * @param inMessageId
     * @return GenericTable[], an array of generic table containing the messageId, parameters names and values
     * @throws SQLException
     */
    public Vector<GenericTable> readFromDB(String inMessageId) throws LoggerException {
        Vector<GenericTable> vector = null;
        if (inMessageId != null) {
            vector = new Vector<GenericTable>();
             ResultSet res = null;
            try {
                readPreparedStatement.setString(1, inMessageId);
                res = readPreparedStatement.executeQuery();
                while (res.next()) {
                    GenericTable gt = new GenericTable();
                    gt.setParameterType(res.getString(2));
                    gt.setParameterName(res.getString(3));
                    gt.setParameterValue(res.getString(4));
                    vector.add(gt);
                }
            } catch (SQLException sqlException) {
                logger.error("Database problem (SqlException ) " + sqlException.getMessage());
                throw new LoggerException("Database problem (SqlException ) " + sqlException.getMessage());
            }
            finally{
                if (res != null){
                    try {
                        res.close();
                    } catch (SQLException ex) {
                        logger.error("Error Closing ResultSet: " + ex);
                    }
                }
            }
        }
        return vector;
    }

    public void setMessageID(String messageID) {
        messageId = messageID;
    }

    public String getMessageID(String messageID) {
        return messageId;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String toString() {
        return parameterName + ":" + parameterValue + "\n";
    }

    public String toXml() {
        return "<node name=\"" + parameterName + ":\" xvalue=\"" + parameterValue + "\" />";
    }

    public String[] toStringArray() {
        String[] vals = {parameterName, parameterValue};
        return vals;
    }

    public String toJSon() {
        JSONArray array = new JSONArray();
        array.put(parameterName);
        array.put(parameterValue);
        return array.toString();
    }

    /**
     * Close the PreparedStatement and DB Connection
     * @throws SQLException
     */
    public void close() throws LoggerException {
        try{
            if (readPreparedStatement != null){
                readPreparedStatement.close(); 
            }
        }catch (SQLException ex){
            logger.error("Error Closing Connection: " + ex);
        }
    }

}
