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

import com.vangent.hieos.services.atna.arr.support.ATNACodedValue;
import com.vangent.hieos.services.atna.arr.support.ATNAMessage;
import com.vangent.hieos.services.atna.arr.support.AuditException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class AuditMessageDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(AuditMessageDAO.class);

    public AuditMessageDAO(Connection conn) {
        super(conn);
    }

    public static String getTableNameStatic() {
        return "auditmessage";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    /**
     * This method will insert all records that are directly dependent on the
     * AuditMessage by invoking the insert method of each sub DAO.
     * @param object
     * @throws loadObject
     */
    @Override
    protected void insertComposedObjects(Object object) throws AuditException {
        super.insertComposedObjects(object);
        ATNAMessage am = (ATNAMessage) object;

        // Add Event ID (one) & Type Code (one or more)
        CodeValueDAO codeValueDAO = new CodeValueDAO(conn);
        codeValueDAO.setTableName(CodeValueDAO.AMCODEVALUE);

        // First add event id record to a list for insert method
        List<ATNACodedValue> eventIds = new ArrayList<ATNACodedValue>();
        eventIds.add(am.getEventID());
        codeValueDAO.insert(eventIds);

        // Now add the event type codes
        codeValueDAO.insert(am.getEventTypeCodes());

        // Should always be one or more Audit Sources - usually one
        AuditSourceDAO auditSourceDAO = new AuditSourceDAO(conn);
        auditSourceDAO.setParent(object);
        auditSourceDAO.insert(am.getAuditSources());

        // Should always be one or more activeparticipants
        ActiveParticipantDAO activeParticipantDAO = new ActiveParticipantDAO(conn);
        activeParticipantDAO.setParent(object);
        activeParticipantDAO.insert(am.getActiveParticipants());

        // Participant Object is optional, there may be zero or more records
        ParticipantObjectDAO participantObjectDAO = new ParticipantObjectDAO(conn);
        participantObjectDAO.setParent(object);
        participantObjectDAO.insert(am.getParticipantObjects());
    }

    @Override
    protected void loadObject(Object obj, ResultSet rs) throws AuditException {
    }

    @Override
    protected Object createObject() throws AuditException {
        ATNAMessage message = new ATNAMessage();
        return message;
    }

    /**
     * Create the prepared statement with the SQL code
     *
     */
    @Override
    protected PreparedStatement createPreparedStatement() throws AuditException {
        String stmtFragment = null;
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO " + getTableName() +
                    " values(?,?,?,?,?)";

        } else if (action == DAO_ACTION_DELETE) {
            stmtFragment = "DELETE " + getTableName() +
                    " where uniqueid = ?";
            
        } else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE " + getTableName() +
                    " set status = ?" +
                    " where uniqueid = ?";
        }

        try {
            PreparedStatement pstmt = conn.prepareStatement(stmtFragment);
            return pstmt;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new AuditException(ex);
        }
    }

    /**
     * Returns the prepared statement with bind variables populated
     *
     */
    @Override
    protected PreparedStatement setPreparedStatement(PreparedStatement pstmt, Object object) throws AuditException {
        try {
            ATNAMessage am = (ATNAMessage) object;
            logger.trace("AM Prepared Statement For: " + am.getUniqueID());
            if (action == DAO_ACTION_INSERT) {
                pstmt.setString(1, am.getUniqueID());
                pstmt.setString(2, am.getEventActionCode());
                pstmt.setTimestamp(3, new Timestamp(am.getEventDateTime().getTime()));
                pstmt.setInt(4, am.getEventOutcomeIndicator());
                pstmt.setString(5, "E");

            } else if (action == DAO_ACTION_DELETE) {
                pstmt.setString(1, am.getUniqueID());

            } else if (action == DAO_ACTION_UPDATE) {
                pstmt.setString(1, am.getStatus());
                pstmt.setString(2, am.getUniqueID());
            }
            return pstmt;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new AuditException(ex);
        }
    }

    /**
     * Retrieve an Audit Message based on Unique Id
     *
     * @param uniqueId
     * @return
     * @throws AuditException
     */
    public ATNAMessage queryObject(String uniqueId) throws AuditException {
        ATNAMessage message = new ATNAMessage();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT uniqueid, eventactioncode, eventdatetime, eventoutcomeindicator, status FROM auditmessage " +
                    " WHERE uniqueid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uniqueId);
            //logger.info("SQL = " + sql);

            rs = stmt.executeQuery();
            while (rs.next()) {
                message.setUniqueID(rs.getString(1));
                message.setEventActionCode(rs.getString(2));
                message.setEventDateTime(rs.getDate(3));
                message.setEventOutcomeIndicator(rs.getInt(4));
                message.setStatus(rs.getString(5));
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new AuditException(e);
        } finally {
            try {
                stmt.close();
                rs.close();
            } catch (SQLException ex) {
                logger.error("Error Closing DB Statement & ResultSet " + ex);
            }
        }
        return message;
    }
}
