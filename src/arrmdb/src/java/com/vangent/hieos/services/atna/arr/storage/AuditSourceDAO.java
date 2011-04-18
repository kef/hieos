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

import com.vangent.hieos.services.atna.arr.support.ATNAAuditSource;
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
public class AuditSourceDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(AuditSourceDAO.class);

    protected AuditSourceDAO(Connection conn) {
        super(conn);
    }

    public static String getTableNameStatic() {
        return "auditsource";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    @Override
    protected void loadObject(Object obj, ResultSet rs) throws AuditException {
    }

    @Override
    protected Object createObject() throws AuditException {
        return new ATNAAuditSource();
    }

    /**
     * Creates the prepared statement with the SQL code
     *
     */
    @Override
    protected PreparedStatement createPreparedStatement() throws AuditException {
        String stmtFragment = null;
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO " + getTableName() +
                    " values(?,?,?,?)";
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
            ATNAAuditSource as = (ATNAAuditSource) object;
            logger.trace("AS Prepared Statement For: " + as.getUniqueID());
            if (action == DAO_ACTION_INSERT) {
                pstmt.setString(1, as.getUniqueID());
                pstmt.setString(2, getParentId());
                pstmt.setString(3, as.getId());
                pstmt.setString(4, as.getEnterpriseSiteID());

            } else if (action == DAO_ACTION_DELETE) {
                pstmt.setString(1, as.getUniqueID());
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

    /**
     * This method will insert all records that are directly dependent on the
     * AuditSource by invoking the insert method of each sub DAO.
     * @param object
     * @throws AuditException
     */
    @Override
    protected void insertComposedObjects(Object object) throws AuditException {
        ATNAAuditSource as = (ATNAAuditSource) object;

        // Add the Audit Source Type Codes
        CodeValueDAO codeValueDAO = new CodeValueDAO(conn);
        codeValueDAO.setTableName(CodeValueDAO.ASCODEVALUE);
        codeValueDAO.insert(as.getTypeCodes());
    }
}
