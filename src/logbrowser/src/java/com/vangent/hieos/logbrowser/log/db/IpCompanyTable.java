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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class storing a pair &lt; IP Address , Company Name &gt; in database.
 *
 * @author jbmeyer
 * @author Bernie Thuman (BHT): Streamlined, added more comments, removed "on-the-fly" thinking.
 *
 */
public class IpCompanyTable extends AbstractLogTable {

    // Column names.
    public static String IP_ADDRESS = "ip";
    public static String COMPANY = "company_name";
    public static String EMAIL = "email";
    public static String TABLE_NAME = "ip";

    // To support SQL commands (BHT: removed static usage). 
    public String readSqlCommand = null;
    private PreparedStatement readPreparedStatement;

    // Internal variables.
    private String ipAddress;
    private String companyName;
    private String email;


    /**
     * 
     */
    private IpCompanyTable() {
    }

    /**
     *
     * @param c
     * @throws java.sql.SQLException
     */
    public IpCompanyTable(Connection c) throws SQLException {
        tableName = TABLE_NAME;
        conn = c;
        readSqlCommand = "select " + IP_ADDRESS + " , " + COMPANY + "," + EMAIL + " FROM " + tableName + " where " + IP_ADDRESS + " = ? ;";

        if (conn == null || conn.isClosed()) {
            throw new SQLException("Database null or closed");
        }
        readPreparedStatement = conn.prepareStatement(readSqlCommand);
    }

    /**
     * Query database for IP data.
     *
     * @param ipAddress
     * @throws java.sql.SQLException
     */
    void readToDB(String ipAddress) throws SQLException {
        if (ipAddress != null) {
            ResultSet res = null;
            try {
                readPreparedStatement.setString(1, ipAddress);
                res = readPreparedStatement.executeQuery();
                res.next();
                this.ipAddress = res.getString(1);
                this.companyName = res.getString(2);
            } catch (SQLException e) {
                throw e;
            } finally {
                try{
                    if (res != null){
                        res.close();
                    }
                }catch (SQLException e) {

                }
            }
        }
    }

    /**
     *
     * @throws java.sql.SQLException
     */
    void readToDB() throws SQLException {
        if (ipAddress != null) {
            ResultSet res = null;
            try {
                readPreparedStatement.setString(1, ipAddress);
                res = readPreparedStatement.executeQuery();
                res.next();
                ipAddress = res.getString(1);
                companyName = res.getString(2);
                email = res.getString(3);
            }catch (SQLException e) {
                throw e;
            } finally {
                try{
                    if (res != null){
                        res.close();
                    }
                }catch (SQLException e) {
                }
            }
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Test if the current ipAddress exists
     * @param ipAddress
     * @return
     * @throws LoggerException
     */
    public boolean ipExist(String ipAddress) throws LoggerException {
        Statement st = null;
        ResultSet res = null;
        try {
            if (conn == null || conn.isClosed()) {
                throw new LoggerException("Database null or closed");
            }
            String sqlRequest = "SELECT count(*) from " + TABLE_NAME + " where  " + IP_ADDRESS + "='" + ipAddress + "'";
            st = conn.createStatement();
            res = st.executeQuery(sqlRequest);
            res.next();
            if (res.getInt(1) == 0) {
                return false;
            } else if (res.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            throw new LoggerException("IPcompanyTable:ipExist( String ipAddress ) problem : (" + e.getErrorCode() + " )" + e.getMessage());
        } finally {
            try{
                if (res != null){
                    res.close();
                }
                if (st != null){
                    st.close();
                }
            }catch (SQLException e) {

            }
        }
        return false;
    }

    /**
     * 
     * @param c
     * @param ipAddress
     * @return
     * @throws LoggerException
     */
    public static boolean IpExist(Connection c, String ipAddress) throws LoggerException {
        Statement st = null;
        ResultSet res = null;
        try {
            if (c == null || c.isClosed()) {
                throw new LoggerException("Database null or closed");
            }
            String sqlRequest = "SELECT count(*) from " + TABLE_NAME + " where  " + IP_ADDRESS + "='" + ipAddress + "'";
            st = c.createStatement();
            res = st.executeQuery(sqlRequest);
            res.next();
            if (res.getInt(1) == 0) {
                return false;
            } else if (res.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            throw new LoggerException("IPcompanyTable: IpExist ( Connection c , String ipAddress   ) problem : (" + e.getErrorCode() + " )" + e.getMessage());
        }finally {
            try{
                if (res != null){
                    res.close();
                }
                if (st != null){
                    st.close();
                }
            }catch (SQLException e) {

            }
        }
        return false;
    }
}
