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

import com.vangent.hieos.services.atna.arr.support.ATNATypeValue;
import com.vangent.hieos.services.atna.arr.support.AuditException;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class TypeValueDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(TypeValueDAO.class);
    private String tableName;

    // XXTYPEVALUE attributes contain the name of the physical database tables supported by this DAO
    public static final String POTYPEVALUE = "potypevalue";

    protected TypeValueDAO(Connection conn) {
        super(conn);
    }

    /**
     *  Sets the name of the actual TypeValue database table being processed
     *  This supports reusing this DAO for several similar tables
     *
     * @param tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Retrieves the name of the actual TypeValue database table being processed
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
        return new ATNATypeValue();
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
                    " values(?,?,?,?,?)";

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
            ATNATypeValue atv = (ATNATypeValue) object;
            if (action == DAO_ACTION_INSERT) {
                pstmt.setString(1, atv.getParent());
                pstmt.setString(2, atv.getAttributeName());
                pstmt.setInt(3, atv.getSeqNo());
                pstmt.setString(4, atv.getType());
                if (atv.getValue() == null) {
                    pstmt.setBinaryStream(5, null, 0);
                } else {
                    pstmt.setBinaryStream(5, new ByteArrayInputStream(atv.getValue()), atv.getValue().length);
                }
            } else if (action == DAO_ACTION_DELETE) {
                pstmt.setString(1, atv.getParent());
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
}
