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
package com.vangent.hieos.xutil.db.support;

import com.vangent.hieos.xutil.exception.XdsInternalException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized class to get database connection instances given a JNDI/JDBC
 * resource name.
 *
 * @author Bernie Thuman
 */
public class SQLConnectionWrapper {

    // Place here to centralize naming (not a great solution, but it works).
    /**
     * HIEOS logger JNDI/JDBC resource name.
     */
    static final public String logJNDIResourceName = "jdbc/hieos-log";
    /**
     * HIEOS ADT JNDI/JDBC resource name.
     */
    static final public String adtJNDIResourceName = "jdbc/hieos-adt";
    /**
     * HIEOS Registry JNDI/JDBC resource name.
     */
    static final public String registryJNDIResourceName = "jdbc/hieos-registry";
    /**
     * HIEOS Repository JNDI/JDBC resource name.
     */
    static final public String repoJNDIResourceName = "jdbc/hieos-repo";

    /**
     * Returns a Connection instance for a given JNDI resource name.
     *
     * @param jndiResourceName  Name of JNDI resource.
     * @return Database connection.
     * @throws XdsInternalException
     */
    public Connection getConnection(String jndiResourceName) throws XdsInternalException {
        Connection con = null;
        try {
            DataSource source = (DataSource) new InitialContext().lookup(jndiResourceName);
            con = source.getConnection();
        } catch (SQLException ex) {
            // log error
            Logger.getLogger(SQLConnectionWrapper.class.getName()).log(Level.SEVERE, null, ex);
            throw new XdsInternalException("Could not get repository data source: " + ex.getMessage());
        } catch (NamingException ex) {
            // DataSource wasn't found in JNDI
            Logger.getLogger(SQLConnectionWrapper.class.getName()).log(Level.SEVERE, null, ex);
            throw new XdsInternalException("Could not get repository data source: " + ex.getMessage());
        }
        return con;  // All should be well here.
    }
}
