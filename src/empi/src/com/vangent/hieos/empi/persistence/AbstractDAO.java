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
package com.vangent.hieos.empi.persistence;

import com.vangent.hieos.empi.codes.CodesConfig;
import com.vangent.hieos.empi.codes.CodesConfig.CodedType;
import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.subjectmodel.CodedValue;
import com.vangent.hieos.subjectmodel.InternalId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class AbstractDAO {

    private static final Logger logger = Logger.getLogger(AbstractDAO.class);
    private Connection connection = null;

    /**
     * 
     * @param connection
     */
    public AbstractDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     *
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * 
     * @param connection
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     *
     * @param id
     * @param tableName 
     * @param idColumnName 
     * @param className
     * @throws EMPIException
     */
    protected void deleteRecords(InternalId id, String tableName, String idColumnName, String className) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM ").append(tableName).append(" WHERE ").append(idColumnName).append("=?");
            String sql = sb.toString();
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setLong(1, id.getId());
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                StringBuilder sbTrace = new StringBuilder();
                sbTrace.append(className).append(".deleteRecords: done executeUpdate elapedTimeMillis=").append((endTime - startTime));
                logger.trace(sbTrace.toString());
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception deleting records", ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param sql
     * @return
     * @throws EMPIException
     */
    public PreparedStatement getPreparedStatement(String sql) throws EMPIException {
        return PersistenceHelper.getPreparedStatement(sql, this.getConnection());
    }

    /**
     * 
     * @return
     * @throws EMPIException
     */
    public Statement getStatement() throws EMPIException {
        return PersistenceHelper.getStatement(this.getConnection());
    }

    /**
     *
     * @param code
     * @param codedType
     * @return
     */
    protected CodedValue getCodedValue(String code, CodesConfig.CodedType codedType) {
        CodedValue codedValue = null;
        try {
            if (code != null) {
                EMPIConfig empiConfig = EMPIConfig.getInstance();
                codedValue = empiConfig.getCodedValue(code, codedType);
                if (codedValue == null) {
                    // Just echo back with code.
                    logger.info("Coded value is not part of this configuration");
                    logger.info("... code = " + code);
                    logger.info("... coded type = " + codedType.toString());
                    codedValue = new CodedValue();
                    codedValue.setCode(code);
                }
            }
        } catch (EMPIException ex) {
            logger.error("Unable to get coded value", ex);
        }
        return codedValue;
    }

    /**
     * 
     * @param stmt
     * @param index
     * @param codedValue
     * @param codedType
     * @throws SQLException
     */
    protected void setCodedValue(PreparedStatement stmt, int index, CodedValue codedValue, CodedType codedType) throws SQLException {
        // It is assumed that the coded value has already been validated.  However, if in the future, the
        // desire is to validate here, "codedType" can be used to help.
        if (codedValue != null) {
            stmt.setString(index, codedValue.getCode());
        } else {
            stmt.setString(index, null);
        }
    }

    /**
     *
     * @param stmt
     * @param index
     * @param value
     * @throws SQLException
     */
    protected void setBoolean(PreparedStatement stmt, int index, Boolean value) throws SQLException {
        if (value != null) {
            stmt.setBoolean(index, value);
        } else {
            stmt.setNull(index, java.sql.Types.BOOLEAN);
        }
    }

    /**
     *
     * @param stmt
     * @param index
     * @param value
     * @throws SQLException
     */
    protected void setDate(PreparedStatement stmt, int index, Date value) throws SQLException {
        if (value != null) {
            stmt.setDate(index, PersistenceHelper.getSQLDate(value));
        } else {
            stmt.setNull(index, java.sql.Types.DATE);
        }
    }

    /**
     *
     * @param stmt
     * @param index
     * @param value
     * @throws SQLException
     */
    protected void setInteger(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, java.sql.Types.INTEGER);
        }
    }

    /**
     * 
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    protected Integer getInteger(ResultSet rs, int index) throws SQLException {
        int nValue = rs.getInt(index);
        if (rs.wasNull()) {
            return null;
        }
        return nValue;
    }

    /**
     *
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    protected Boolean getBoolean(ResultSet rs, int index) throws SQLException {
        boolean nValue = rs.getBoolean(index);
        if (rs.wasNull()) {
            return null;
        }
        return nValue;
    }

    /**
     *
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    protected Date getDate(ResultSet rs, int index) throws SQLException {
        Date nValue = rs.getDate(index);
        if (rs.wasNull()) {
            return null;
        }
        return nValue;
    }

    /**
     *
     * @param date
     * @return
     */
    protected Timestamp getTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     *
     * @param timestamp
     * @return
     */
    protected Date getDate(Timestamp timestamp) {
        return new Date(timestamp.getTime());
    }

    /**
     *
     * @param stmt
     */
    public void close(Statement stmt) {
        PersistenceHelper.close(stmt);
    }

    /**
     * 
     * @param rs
     */
    public void close(ResultSet rs) {
        PersistenceHelper.close(rs);
    }
}
