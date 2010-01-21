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

import com.vangent.hieos.xutil.db.support.SQLConnectionWrapper;
import com.vangent.hieos.xutil.exception.XdsInternalException;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.log4j.Logger;


/**
 * Class allowing to connect the logger to the database, to create message, to delete it and to read it
 *
 * BHT: Streamlined and added more documentation.
 *
 * @author jbmeyer
 * @author Bernie Thuman (BHT), rewrite.
 *
 */
public class Log {

    private Connection connection = null;
    private String databaseType = null;

    private final static Logger logger = Logger.getLogger(Log.class);

    /**
     *
     */
    public Log() {
    }

    /**
     *
     * @return
     * @throws com.vangent.hieos.logbrowser.log.db.LoggerException
     */
    public Connection getConnection() throws LoggerException {
        if (connection == null) {
            try {
                connection = new SQLConnectionWrapper().getConnection(SQLConnectionWrapper.logJNDIResourceName);
            } catch (XdsInternalException e) {
                throw new LoggerException(e.getMessage());
            }
        }
        return connection;
    }

    /**
     *
     * @return String
     * @throws com.vangent.hieos.logbrowser.log.db.LoggerException
     */
    public String getDatabaseType() throws LoggerException {
        if (databaseType == null){
            if (connection == null) {
                getConnection();
            }
            try {
                databaseType = connection.getMetaData().getDatabaseProductName();
            } catch (SQLException ex) {
                logger.error(ex);
                throw new LoggerException(ex.getMessage());
            }
        }
        logger.info("Database Type: " + databaseType);
        return databaseType;
    }

    /**
     *
     * @param m
     * @throws LoggerException
     */
    public void deleteMessage(Message m) throws LoggerException {
        if (m != null) {
            m.deleteMessage();
        }
    }

    /**
     *
     * @param messageID
     * @throws LoggerException
     */
    public void deleteMessage(String messageID) throws LoggerException {
        Message m = null;
        if (messageID != null) {
            try {
                m = new Message(this.getConnection(), messageID);
                deleteMessage(m);
            }finally {
                m.close();
            }
        }
    }

    /**
     *
     * @param messageID
     * @return
     * @throws LoggerException
     */
    public Message readMessage(String messageID) throws LoggerException {
        Message m = null;
        if (messageID != null) {
            try{
                m = new Message(this.getConnection(), messageID);
                m.readMessage();
            }finally{
                m.close();
            }
        }
        return m;
    }

    /**
     *
     * @throws LoggerException
     */
    public void closeConnection() throws LoggerException {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException sql) {
            throw new LoggerException("Log::close() " + sql.getMessage());
        }
    }
}
