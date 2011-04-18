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

import com.vangent.hieos.services.atna.arr.support.ATNAActiveParticipant;
import com.vangent.hieos.services.atna.arr.support.ATNAMessage;
import com.vangent.hieos.services.atna.arr.support.AuditException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class ActiveParticipantDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(ActiveParticipantDAO.class);

    protected ActiveParticipantDAO(Connection conn) {
        super(conn);
    }

    public static String getTableNameStatic() {
        return "activeparticipant";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    /**
     * Initializes the prepared statement with the SQL code
     *
     */
    @Override
    protected PreparedStatement createPreparedStatement() throws AuditException {
        String stmtFragment = null;
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO " + getTableName() +
                    " values(?,?,?,?,?,?,?,?)";
        } else if (action == DAO_ACTION_DELETE) {
            stmtFragment = "DELETE " + getTableName() +
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
            ATNAActiveParticipant ap = (ATNAActiveParticipant) object;
            logger.debug("AP Prepared Statement For: " + ap.getUniqueID());
            if (action == DAO_ACTION_INSERT) {
                pstmt.setString(1, ap.getUniqueID());
                pstmt.setString(2, getParentId());
                pstmt.setString(3, ap.getUserID());
                pstmt.setString(4, ap.getAlternativeUserID());
                pstmt.setString(5, ap.getUserName());
                pstmt.setString(6, ap.getUserIsRequestor() == true ? "T" : "F");
                pstmt.setInt(7, ap.getNetworkAccessPointTypeCode());
                pstmt.setString(8, ap.getNetworkAccessPointID());

            } else if (action == DAO_ACTION_DELETE) {
                pstmt.setString(1, ap.getUniqueID());
            }
            return pstmt;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new AuditException(ex);
        }
    }

    @Override
    protected String getParentId() {
        ATNAMessage am = (ATNAMessage) parent;
        String parentId = am.getUniqueID();
        return parentId;
    }

    @Override
    protected void loadObject(Object obj, ResultSet rs) throws AuditException {
    }

    @Override
    Object createObject() throws AuditException {
        return new ATNAActiveParticipant();
    }

    /**
     * This method will insert all records that are directly dependent on the
     * ActiveParticipant by invoking the insert method of each sub DAO.
     * @param object
     * @throws AuditException
     */
    @Override
    protected void insertComposedObjects(Object object) throws AuditException {
        ATNAActiveParticipant ap = (ATNAActiveParticipant) object;

        CodeValueDAO codeValueDAO = new CodeValueDAO(conn);
        codeValueDAO.setTableName(CodeValueDAO.APCODEVALUE);
        codeValueDAO.insert(ap.getRoleIDCodes());
    }
}
