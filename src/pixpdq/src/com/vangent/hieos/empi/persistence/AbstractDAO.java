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

import com.vangent.hieos.empi.exception.EMPIException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
     * @param className
     * @throws EMPIException
     */
    protected void deleteRecords(String id, String tableName, String idColumnName, String className) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM ").append(tableName).append(" WHERE ").append(idColumnName).append("=?");
            String sql = sb.toString();
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, id);
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                StringBuilder sbTrace = new StringBuilder();
                sbTrace.append(className).append(".deleteRecords: done executeUpdate elapedTimeMillis=").append((endTime - startTime));
                logger.trace(sbTrace.toString());
            }
        } catch (SQLException ex) {
            throw new EMPIException(ex);
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
        // Now, create (and return) the prepared statement with the generated SQL.
        PreparedStatement stmt = null;
        Connection conn = this.getConnection();
        try {
            stmt = conn.prepareStatement(sql);
        } catch (SQLException ex) {
            throw new EMPIException(ex);
        }
        return stmt;
    }

    /**
     *
     * @param stmt
     */
    public void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                logger.error("Could not close prepared statement: " + ex.getMessage());
            }
        }
    }

    /**
     * 
     * @param rs
     */
    public void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                logger.error("Could not close result set: " + ex.getMessage());
            }
        }
    }
}
