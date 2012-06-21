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
package com.vangent.hieos.empi.lockmanager;

import com.vangent.hieos.xutil.db.support.SQLConnectionWrapper;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class LockManager {

    private final static Logger logger = Logger.getLogger(LockManager.class);
    private String jndiResourceName;

    /**
     *
     */
    private LockManager() {
        // Do not allow instantiation with default constructor.
    }

    /**
     * 
     * @param conn
     */
    public LockManager(String jndiResourceName) {
        this.jndiResourceName = jndiResourceName;
    }

    /**
     *
     * @param resource
     * @throws LockManagerException
     */
    public void acquireLock(LockResource resource) throws LockManagerException {
        // Get the database connection.
        Connection conn = this.getConnection();

        try {
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            // Try to acquire lock for the given resource.
            String sql = "INSERT INTO resource_lock (resource_id) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, resource.getId());
            stmt.execute();
            stmt.close();
            conn.commit();
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                // Do nothing (already had an exception).
            }
            logger.info("Unable to acquire resource lock [id = " + resource.getId() + "]: " + ex.getMessage());
            throw new LockManagerException("Unable to acquire resource lock [id = " + resource.getId() + "]: " + ex.getMessage());
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception e) {
                //Do nothing.
            }
        }
    }

    /**
     *
     * @param resource
     * @throws LockManagerException
     */
    public void releaseLock(LockResource resource) throws LockManagerException {
        // Get the database connection.
        Connection conn = this.getConnection();

        try {
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            // Try to release lock for the given resource.
            // FIXME: Add db timestamp ....
            String sql = "DELETE FROM resource_lock WHERE resource_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, resource.getId());
            stmt.executeUpdate();
            stmt.close();
            conn.commit();
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                // Do nothing (already had an exception).
            }
            logger.info("Unable to release resource lock [id = " + resource.getId() + "]: " + ex.getMessage());
            throw new LockManagerException("Unable to release resource lock [id = " + resource.getId() + "]: " + ex.getMessage());
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception e) {
                //Do nothing.
            }
        }
    }

    /**
     * 
     * @return
     * @throws LockManagerException
     */
    private Connection getConnection() throws LockManagerException {
        try {
            Connection conn = new SQLConnectionWrapper().getConnection(this.jndiResourceName);
            return conn;
        } catch (XdsInternalException ex) {
            throw new LockManagerException(ex);
        }
    }
}
