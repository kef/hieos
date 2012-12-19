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
import com.vangent.hieos.services.atna.arr.support.ATNAParticipantObject;
import com.vangent.hieos.services.atna.arr.support.AuditException;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class ParticipantObjectDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(ParticipantObjectDAO.class);

    public ParticipantObjectDAO(Connection conn) {
        super(conn);
    }

    public static String getTableNameStatic() {
        return "participantobject";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    /**
     * This method will insert all records that are directly dependent on the
     * Participant Object by invoking the insert method of each sub DAO.
     * @param object
     * @throws loadObject
     */
    @Override
    protected void insertComposedObjects(Object object) throws AuditException {
        ATNAParticipantObject po = (ATNAParticipantObject) object;

        // Add Participant Object ID Type Code if it exists. There maybe zero or
        // one instance of idTypeCode so add it to a list for the insert method if it exists
        if (po.getIdTypeCode() != null) {
            CodeValueDAO codeValueDAO = new CodeValueDAO(conn);
            codeValueDAO.setTableName(CodeValueDAO.POCODEVALUE);
            List<ATNACodedValue> typeCodes = new ArrayList<ATNACodedValue>();
            typeCodes.add(po.getIdTypeCode());
            codeValueDAO.insert(typeCodes);
        }

        // Add the Participant Object Details, there can be zero or more Detail records
        if (po.getDetails() != null) {
            TypeValueDAO typeValueDAO = new TypeValueDAO(conn);
            typeValueDAO.setTableName(TypeValueDAO.POTYPEVALUE);
            typeValueDAO.insert(po.getDetails());
        }
    }

    @Override
    protected void loadObject(Object obj, ResultSet rs) throws AuditException {
    }

    @Override
    Object createObject() throws AuditException {
        return new ATNAParticipantObject();
    }

    /**
     * Initializes the prepared statement with the SQL code
     *
     */
    @Override
    protected PreparedStatement createPreparedStatement() throws AuditException {
        String stmtFragment = null;
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO " + getTableName()
                    + " values(?,?,?,?,?,?,?,?,?)";
        } else if (action == DAO_ACTION_DELETE) {
            stmtFragment = "DELETE " + getTableName()
                    + " where uniqueid = ?";
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
            ATNAParticipantObject po = (ATNAParticipantObject) object;
            logger.trace("PO Prepared Statement For: " + po.getUniqueID());
            if (action == DAO_ACTION_INSERT) {
                pstmt.setString(1, po.getUniqueID());
                pstmt.setString(2, getParentId());
                pstmt.setInt(3, po.getTypeCode());
                pstmt.setInt(4, po.getTypeCodeRole());
                pstmt.setInt(5, po.getDataLifeCycle());
                pstmt.setString(6, po.getSensitivity());
                if (po.getId() == null) {
                    pstmt.setBinaryStream(7, null, 0);
                } else {
                    pstmt.setBinaryStream(7, new ByteArrayInputStream(po.getId().getBytes()), po.getId().length());
                }
                pstmt.setString(8, po.getName());
                if (po.getQuery() == null) {
                    pstmt.setBinaryStream(9, null, 0);
                } else {
                    pstmt.setBinaryStream(9, new ByteArrayInputStream(po.getQuery()), po.getQuery().length);
                }
            } else if (action == DAO_ACTION_DELETE) {
                pstmt.setString(1, po.getUniqueID());
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
}
