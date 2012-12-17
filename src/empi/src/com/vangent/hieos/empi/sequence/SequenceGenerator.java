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

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.persistence.PersistenceManager;
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
    private PersistenceManager persistenceManager;

    /**
     *
     */
    private SequenceGenerator() {
        // Do not allow instantiation with default constructor.
    }

    /**
     *
     * @param persistenceManager 
     * @param sql
     */
    public SequenceGenerator(PersistenceManager persistenceManager, String sql) {
        //this.jndiResourceName = jndiResourceName;
        this.persistenceManager = persistenceManager;
        this.sql = sql;
    }

    /**
     *
     * @return @throws EMPIException 
     * @throws EMPIException ]
     */
    public long getNext() throws EMPIException {
        long sequenceNumber = -1;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = persistenceManager.getPreparedStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                sequenceNumber = rs.getLong(1);
            }
        } catch (SQLException ex) {
            logger.info("Unable to acquire sequence number [sequence = " + this.sql + "]: " + ex.getMessage());
            throw new EMPIException("Unable to acquire sequence number [sequence = " + this.sql + "]: " + ex.getMessage());
        } finally {
            PersistenceManager.close(stmt);
            PersistenceManager.close(rs);
        }
        return sequenceNumber;
    }
}
