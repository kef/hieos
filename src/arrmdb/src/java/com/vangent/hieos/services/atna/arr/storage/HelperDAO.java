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
package com.vangent.hieos.services.atna.arr.storage;

import com.vangent.hieos.xutil.socket.ServerProperties;
import com.vangent.hieos.xutil.db.support.SQLConnectionWrapper;
import java.sql.Connection;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class HelperDAO {

    private static final Logger logger = Logger.getLogger(HelperDAO.class);
    static final public String arrJNDIResourceName = "jdbc/hieos-arr";

    /**
     * Get the ATNA repository JDBC connection instance from connection pool.
     *
     * @return Database connection instance from pool.
     * @throws java.util.Exception
     */
    public static Connection getConnection() throws Exception{
        //TODO - Should get JNDI name from XConfig property instead of static variable
        return new SQLConnectionWrapper().getConnection(arrJNDIResourceName);
    }

    /**
     *
     * @return String - a system generated unique id
     */
    public static String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Get the ATNA repository JDBC connection by directly loading the driver.
     *
     * @return Database connection instance from pool.
     * @throws java.util.Exception
     */
    public static Connection getConnectionDirect() throws Exception {
        try {
            ServerProperties props = new ServerProperties("atnaserver.properties");
            String driver = props.getProperty("jdbc_driver");
            Class.forName(driver);
            logger.info("Loaded jdbc driver: " + driver);
                        
            String url = props.getProperty("jdbc_url");
            String user = props.getProperty("jdbc_user");
            String password = props.getProperty("jdbc_password");

            Connection conn = java.sql.DriverManager.getConnection(url, user, password);
            logger.info("Connected to database: " + url);
            return conn;

        } catch (ClassNotFoundException e) {
            logger.error(e);
            throw new Exception(e);
        }
    }

}
