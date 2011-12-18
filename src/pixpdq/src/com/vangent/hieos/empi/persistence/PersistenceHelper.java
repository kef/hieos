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
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PersistenceHelper {

    private final static Logger logger = Logger.getLogger(PersistenceHelper.class);

    /**
     *
     * @param date
     * @return
     */
    public static java.sql.Date getSQLDate(Date date) {
        if (date == null) {
            return null;
        } else {
            return new java.sql.Date(date.getTime());
        }
    }

    /**
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Get EMPI JDBC connection instance from connection pool.
     *
     * @return Database connection instance from pool.
     * @throws EMPIException
     */
    public static Connection getConnection() throws EMPIException {
        try {
            EMPIConfig empiConfig = EMPIConfig.getInstance();
            String jndiResourceName = empiConfig.getJndiResourceName();
            Connection connection = new SQLConnectionWrapper().getConnection(jndiResourceName);
            connection.setAutoCommit(false);
            // FIXME: Should we make the isolation level configurable?
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return connection;
        } catch (Exception ex) {
            logger.error("Could not open connection to support EMPI", ex);
            throw new EMPIException(ex.getMessage());
        }
    }

    /**
     *
     * @param connection
     */
    public static void commit(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit();
            }
        } catch (SQLException ex) {
            rollback(connection);
            logger.error("Could not commit EMPI connection", ex);
        } finally {
            close(connection);
        }
    }

    /**
     *
     * @param connection
     */
    public static void close(Connection connection) {
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
     * @param connection
     */
    public static void rollback(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException ex) {
            // Just let processing continue ....
            logger.error("Could not rollback EMPI connection", ex);
        } finally {
            close(connection);
        }
    }

    /**
     *
     * @param sqlException
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
