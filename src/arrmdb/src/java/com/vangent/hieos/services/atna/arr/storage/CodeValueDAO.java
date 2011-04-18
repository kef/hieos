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
public class CodeValueDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(CodeValueDAO.class);
    private String tableName;
    // XXCODEVALUE attributes contain the name of the physical database tables supported by this DAO
    public static final String AMCODEVALUE = "amcodevalue";
    public static final String APCODEVALUE = "apcodevalue";
    public static final String ASCODEVALUE = "ascodevalue";
    public static final String POCODEVALUE = "pocodevalue";

    protected CodeValueDAO(Connection conn) {
        super(conn);
    }

    /**
     *  Sets the name of the actual CodeValue database table being processed
     *  This supports reusing this DAO for several similar tables
     *
     * @param tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Retrieves the name of the actual CodeValue database table being processed
     * This supports reusing this DAO for several similar tables
     *
     * @return
     */
    public String getTableName() {
        return tableName;
    }

    @Override
    protected void loadObject(Object obj, ResultSet rs) throws AuditException {
    }

    @Override
    protected Object createObject() throws AuditException {
        return new ATNACodedValue();
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
                    " where parentid = ?";
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
            ATNACodedValue acv = (ATNACodedValue) object;
            if (action == DAO_ACTION_INSERT) {
                pstmt.setString(1, acv.getParent());
                pstmt.setString(2, acv.getAttributeName());
                pstmt.setInt(3, acv.getSeqNo());
                pstmt.setString(4, acv.getCode());
                pstmt.setString(5, acv.getCodeSystem());
                pstmt.setString(6, acv.getDisplayName());
                pstmt.setString(7, acv.getCodeSystemName());
                pstmt.setString(8, acv.getOriginalText());
                
            } else if (action == DAO_ACTION_DELETE) {
                pstmt.setString(1, acv.getParent());
            }
            return pstmt;
        } catch (SQLException ex) {
            logger.error(ex);
            throw new AuditException(ex);
        }
    }

    /**
     * Gets the column name that is foreign key ref into parent table.
     * 
     */
    @Override
    protected String getParentAttribute() {
        return "parentid";
    }

    /**
     * 
     * @param uniqueId
     * @param attributeName
     * @return
     * @throws AuditException
     */
    public ATNACodedValue queryObject(String uniqueId, String attributeName) throws AuditException {
        ATNACodedValue atnaCodedValue = new ATNACodedValue();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT codevalue, displayname FROM amcodevalue " +
                    " WHERE parentid = ? and attributename = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uniqueId);
            stmt.setString(2, "T");
            //logger.info("SQL = " + sql);

            rs = stmt.executeQuery();
            while (rs.next()) {
                atnaCodedValue.setCode(rs.getString(1));
                atnaCodedValue.setDisplayName(rs.getString(2));
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
        return atnaCodedValue;
    }
}
