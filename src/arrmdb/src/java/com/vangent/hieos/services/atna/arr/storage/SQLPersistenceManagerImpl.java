/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.atna.arr.storage;

import com.vangent.hieos.services.atna.arr.support.ATNACodedValue;
import com.vangent.hieos.services.atna.arr.support.ATNALog;
import com.vangent.hieos.services.atna.arr.support.ATNAMessage;
import com.vangent.hieos.services.atna.arr.support.AuditException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class SQLPersistenceManagerImpl {

    private static final Logger log = Logger.getLogger(SQLPersistenceManagerImpl.class);

    public SQLPersistenceManagerImpl() {
    }


    /**
     * Creates an Audit Log even if Audit Message cannot be parsed
     *
     * @param auditLog
     * @throws AuditException
     */
    public void createAuditLog(ATNALog auditLog) throws AuditException {
        Connection conn = null;
        try {
            List<ATNALog> auditLogs = new ArrayList<ATNALog>();
            auditLogs.add(auditLog);

            conn = HelperDAO.getConnection();
            AuditLogDAO dao = new AuditLogDAO(conn);
            dao.insert(auditLogs);
            log.info("AUDIT LOG SAVED IN DB: " + auditLog.getUniqueID());
        } catch (Exception ex) {
            log.error(ex);
            ex.printStackTrace();
            throw new AuditException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    throw new AuditException("Error closing db connection: ", ex);
                }
            }
        }
    }

    /**
     * Creates the Audit Message record
     *
     * @param auditMessage
     * @throws AuditException
     */
    public void createAuditMessage(ATNAMessage auditMessage) throws AuditException {
        Connection conn = null;
        try {
            List<ATNAMessage> auditMessages = new ArrayList<ATNAMessage>();
            auditMessages.add(auditMessage);

            conn = HelperDAO.getConnection();
            AuditMessageDAO dao = new AuditMessageDAO(conn);
            dao.insert(auditMessages);
            log.info("AUDIT MESSAGE SAVED IN DB: " + auditMessage.getUniqueID());
        } catch (Exception ex) {
            log.error(ex);
            ex.printStackTrace();
            throw new AuditException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    throw new AuditException("Error closing db connection: ", ex);
                }
            }
        }
    }

    /**
     * Updates the Audit Message record if the AuditMessage XML was parsed successfully
     *
     * @param auditMessage
     * @throws AuditException
     */
    public void updateAuditMessage(ATNAMessage auditMessage) throws AuditException {
        Connection conn = null;
        try {
            List<ATNAMessage> auditMessages = new ArrayList<ATNAMessage>();
            auditMessages.add(auditMessage);

            conn = HelperDAO.getConnection();
            AuditMessageDAO dao = new AuditMessageDAO(conn);
            dao.update(auditMessages);
            log.info("AUDIT MESSAGE UPDATED: " + auditMessage.getUniqueID());
        } catch (Exception ex) {
            log.error(ex);
            ex.printStackTrace();
            throw new AuditException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    throw new AuditException("Error closing db connection: ", ex);
                }
            }
        }
    }

    /**
     * Delete an Audit Message
     *
     * @param auditMessage
     * @throws AuditException
     */
    public void deleteAuditMessage(ATNAMessage auditMessage) throws AuditException {
        List<ATNAMessage> auditMessages = new ArrayList<ATNAMessage>();
        auditMessages.add(auditMessage);
        deleteAuditMessage(auditMessages);
    }

    /**
     * Delete an Audit Message
     *
     * @param auditMessages
     * @throws AuditException
     */
    public void deleteAuditMessage(List<ATNAMessage> auditMessages) throws AuditException {
        Connection conn = null;
        try {
            log.info("DELETE AUDIT MESSAGES");
            conn = HelperDAO.getConnection();
            AuditMessageDAO dao = new AuditMessageDAO(conn);
            dao.delete(auditMessages);
            log.info("DONE DELETING AUDIT MESSAGES");
        } catch (Exception ex) {
            log.error(ex);
            ex.printStackTrace();
            throw new AuditException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    throw new AuditException("Error closing db connection: ", ex);
                }
            }
        }
    }

    /**
     * Retrieves an Audit Message for Display
     *
     * @param uniqueId
     * @return
     * @throws AuditException
     */
    public ATNAMessage retrieveATNAMessage(String uniqueId)
        throws AuditException {
                Connection conn = null;
        try {
            //log.info("RETRIVE AUDIT MESSAGE: " + uniqueId);
            conn = HelperDAO.getConnection();
            AuditMessageDAO dao = new AuditMessageDAO(conn);
            ATNAMessage message = dao.queryObject(uniqueId);
            return message;
        } catch (Exception ex) {
            log.error(ex);
            ex.printStackTrace();
            throw new AuditException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    throw new AuditException("Error closing db connection: ", ex);
                }
            }
        }
    }

    /**
     * Retrieves an Audit Log for Display
     *
     * @param uniqueId
     * @return
     * @throws AuditException
     */
    public ATNALog retrieveATNALog(String uniqueId)
        throws AuditException {
                Connection conn = null;
        try {
            //log.info("RETRIVE AUDIT LOG: " + uniqueId);
            conn = HelperDAO.getConnection();
            AuditLogDAO dao = new AuditLogDAO(conn);
            ATNALog log = dao.queryObject(uniqueId);
            return log;
        } catch (Exception ex) {
            log.error(ex);
            ex.printStackTrace();
            throw new AuditException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    throw new AuditException("Error closing db connection: ", ex);
                }
            }
        }
    }

    /**
     * Retrieves all ATNA Logs for an IP Address
     *
     * @param seachCriteria
     * @return
     * @throws AuditException
     */
    public List<ATNALog> retrieveATNALogs(ATNALog seachCriteria)
        throws AuditException {
                Connection conn = null;
        try {
            //log.info("RETRIVE AUDIT LOG FOR : " + seachCriteria.getClientIPAddress());
            List<ATNALog> logs = new ArrayList<ATNALog>();
            conn = HelperDAO.getConnection();
            AuditLogDAO dao = new AuditLogDAO(conn);
            logs = dao.getATNALogs(seachCriteria);
            log.info("NUMBER OF AUDIT LOGS FOR : " + seachCriteria.getClientIPAddress() +
                    " is: " + logs.size());

            return logs;
        } catch (Exception ex) {
            log.error(ex);
            ex.printStackTrace();
            throw new AuditException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    throw new AuditException("Error closing db connection: ", ex);
                }
            }
        }
    }

    /**
     * Retrieves a list of IP Addresses that have messages in the Repository
     *
     * @return
     * @throws AuditException
     */
    public List<String> retrieveIPAddresses()
        throws AuditException {
                Connection conn = null;
        try {
            log.info("RETRIVE IP Addresses in ATNA Repository ");
            List<String> ipAddresses = new ArrayList<String>();
            conn = HelperDAO.getConnection();
            AuditLogDAO dao = new AuditLogDAO(conn);
            ipAddresses = dao.getIPAddresses();

            return ipAddresses;
        } catch (Exception ex) {
            log.error(ex);
            ex.printStackTrace();
            throw new AuditException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    throw new AuditException("Error closing db connection: ", ex);
                }
            }
        }
    }

    /**
     * Retrieve a Coded Value
     * TODO - move this SQL logic into a join with AuditMessage Table for improved performance
     *
     * @param uniqueId
     * @param attributeName
     * @return
     * @throws AuditException
     */
    public ATNACodedValue retrieveATNACodedValue(String uniqueId, String attributeName)
        throws AuditException {
                Connection conn = null;
        try {
            conn = HelperDAO.getConnection();
            CodeValueDAO dao = new CodeValueDAO(conn);
            ATNACodedValue codedValue = dao.queryObject(uniqueId, attributeName);
            return codedValue;
        } catch (Exception ex) {
            log.error(ex);
            ex.printStackTrace();
            throw new AuditException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    throw new AuditException("Error closing db connection: ", ex);
                }
            }
        }
    }
}
