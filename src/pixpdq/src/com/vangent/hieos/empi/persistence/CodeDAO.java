/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.persistence;

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.empi.exception.EMPIException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Bernie Thuman
 */
public class CodeDAO extends AbstractDAO {

    /**
     *
     */
    public enum CodeType {

        /**
         *
         */
        GENDER,
        /**
         *
         */
        MARITAL_STATUS,
        /**
         *
         */
        RELIGIOUS_AFFILIATION,
        /**
         *
         */
        RACE,
        /**
         *
         */
        ETHNIC_GROUP,
        /**
         *
         */
        PERSONAL_RELATIONSHIP,
        /**
         *
         */
        LANGUAGE
    };

    /**
     *
     * @param connection
     */
    public CodeDAO(Connection connection) {
        super(connection);
    }

    /**
     * 
     * @param code
     * @param type
     * @return
     * @throws EMPIException
     */
    public int getId(String code, CodeType type) throws EMPIException {
        int id = -1;  // Not found if -1.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String tableName = this.getTableName(type);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT id FROM ").append(tableName).append(" WHERE code=?");
            stmt = this.getPreparedStatement(sb.toString());
            stmt.setString(1, code);
            // Execute query.
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new EMPIException("Code = " + code + " not found in table = " + tableName);
            } else {
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading code from database table = " + tableName, ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return id;
    }

    /**
     * 
     * @param id
     * @param type
     * @return
     * @throws EMPIException
     */
    public CodedValue load(int id, CodeType type) throws EMPIException {
        CodedValue codedValue = null;
        String tableName = this.getTableName(type);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT code,display_name FROM ").append(tableName).append(" WHERE id=?");
            stmt = this.getPreparedStatement(sb.toString());
            stmt.setInt(1, id);
            // Execute query.
            rs = stmt.executeQuery();
            if (!rs.next()) {
                // Return null if not found.
                //throw new EMPIException("id = " + id + " not found in table = " + tableName);
            } else {
                codedValue = new CodedValue();
                codedValue.setCode(rs.getString(1));
                codedValue.setDisplayName(rs.getString(2));
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading code from database table = " + tableName, ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return codedValue;
    }

    /**
     *
     * @return
     */
    private String getTableName(CodeType type) {
        switch (type) {
            case GENDER:
                return "gender_code";
            case MARITAL_STATUS:
                return "marital_status_code";
            case RELIGIOUS_AFFILIATION:
                return "religious_affiliation_code";
            case RACE:
                return "race_code";
            case ETHNIC_GROUP:
                return "ethnic_group_code";
            case PERSONAL_RELATIONSHIP:
                return "personal_relationship_code";
            case LANGUAGE:
            default:
                return "language_code";
        }
    }
}
