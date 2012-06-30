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

package com.vangent.hieos.empi.sequence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
/**
 *
 * @author Bernie Thuman
 */
public class SequenceGenerator {

    private final static Logger logger = Logger.getLogger(SequenceGenerator.class);
    private String sql;
    private Connection connection;

    /**
     *
     */
    private SequenceGenerator() {
        // Do not allow instantiation with default constructor.
    }

    /**
     *
     * @param conn
     * @param sql
     */
    public SequenceGenerator(Connection conn, String sql) {
        //this.jndiResourceName = jndiResourceName;
        this.connection = conn;
        this.sql = sql;
    }

    /**
     *
     * @param resource
     * @throws LockManagerException
     */
    public long getNext() throws SequenceGeneratorException {
        // Get the database connection.
        long sequenceNumber = -1;

        try {
            //conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            //conn.setAutoCommit(false);
            // Try to acquire lock for the given resource.
            PreparedStatement stmt = connection.prepareStatement(sql);
            //stmt.setString(1, resource.getId());
            ResultSet rs = stmt.executeQuery();
            // Loop through the result set
            if (rs.next()) {
                sequenceNumber = rs.getLong(1);
            }
            stmt.close();
            rs.close();
            //conn.commit();
        } catch (SQLException ex) {
            //try {
            //    conn.rollback();
            //} catch (SQLException ex1) {
            // Do nothing (already had an exception).
            //}
            logger.info("Unable to acquire sequence number [sequence = " + this.sql + "]: " + ex.getMessage());
            throw new SequenceGeneratorException("Unable to acquire sequence number [sequence = " + this.sql + "]: " + ex.getMessage());
        } finally {
            // Do something.
        }
        return sequenceNumber;
    }
}
