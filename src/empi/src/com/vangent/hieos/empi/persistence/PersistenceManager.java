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

import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.xutil.db.support.SQLConnectionWrapper;
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
public class PersistenceManager {

    private final static Logger logger = Logger.getLogger(PersistenceManager.class);
    private Connection connection = null;

    /**
     *
     */
    public PersistenceManager() {
        this.connection = null;
    }

    /**
     *
     * @return
     */
    // Keep Connnection hidden.
    //public Connection getConnection() {
    //    return connection;
    //}
    /**
     *
     * @throws EMPIException
     */
    public void open() throws EMPIException {
        this.close();  // Just in case.
        this.connection = this.getNewConnection();
    }

    /**
     * Get EMPI JDBC connection instance from connection pool.
     *
     * @return Database connection instance from pool.
     * @throws EMPIException
     */
    private Connection getNewConnection() throws EMPIException {
        try {
            EMPIConfig empiConfig = EMPIConfig.getInstance();
            String jndiResourceName = empiConfig.getJndiResourceName();
            Connection conn = new SQLConnectionWrapper().getConnection(jndiResourceName);
            conn.setAutoCommit(false);
            // FIXME: Should we make the isolation level configurable?
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return conn;
        } catch (Exception ex) {
            logger.error("Could not open connection to support EMPI", ex);
            throw new EMPIException(ex.getMessage());
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
        try {
            stmt = connection.prepareStatement(sql);
        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception getting prepared statement", ex);
        }
        return stmt;
    }

    /**
     *
     * @return @throws EMPIException
     * @throws EMPIException
     */
    public Statement getStatement() throws EMPIException {
        // Now, create (and return) the prepared statement with the generated SQL.
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception getting statement", ex);
        }
        return stmt;
    }

    /**
     *
     * @param stmt
     */
    public static void close(Statement stmt) {
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
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                logger.error("Could not close result set: " + ex.getMessage());
            }
        }
    }

    /**
     *
     */
    public void rollback() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            // Just let processing continue ....
            logger.error("Could not rollback EMPI connection", ex);
        } finally {
            close();
        }
    }

    /**
     *
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            // Just let processing continue ....
            logger.error("Could not close EMPI connection", ex);
        }
    }

    /**
     *
     */
    public void commit() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit();
            }
        } catch (SQLException ex) {
            rollback();
            logger.error("Could not commit EMPI connection", ex);
        } finally {
            close();
        }
    }

    /**
     *
     * @param text
     * @param ex
     * @return
     */
    public static EMPIException getEMPIException(String text, SQLException ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(text);
        sb.append("\n--- SQLException caught ---\n");
        while (ex != null) {
            sb.append("Message: ").append(ex.getMessage());
            ex = ex.getNextException();
            sb.append("\n");
        }
        String errorText = sb.toString();
        logger.error(errorText);
        return new EMPIException(errorText);
    }
}
