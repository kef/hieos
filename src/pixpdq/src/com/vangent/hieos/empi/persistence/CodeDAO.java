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
public abstract class CodeDAO extends AbstractDAO {

    /**
     *
     * @param connection
     */
    public CodeDAO(Connection connection) {
        super(connection);
    }

    /**
     *
     * @return
     */
    abstract public String getTableName();

    /**
     * 
     * @param code
     * @return
     * @throws EMPIException
     */
    public int getId(String code) throws EMPIException {
        int id = -1;  // Not found if -1.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String tableName = this.getTableName();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT id FROM ").append(tableName).append(" WHERE code = ?");
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
            throw new EMPIException("Failure reading code from database table = " + tableName + ".." + ex.getMessage());
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return id;
    }

    /**
     *
     * @param id
     * @param codedValue 
     * @throws EMPIException
     */
    public void load(int id, CodedValue codedValue) throws EMPIException {
        String tableName = this.getTableName();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT code,displayname FROM ").append(tableName).append(" WHERE id = ?");
            stmt = this.getPreparedStatement(sb.toString());
            stmt.setInt(1, id);
            // Execute query.
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new EMPIException("id = " + id + " not found in table = " + tableName);
            } else {
                codedValue.setCode(rs.getString(1));
                codedValue.setDisplayName(rs.getString(2));
            }
        } catch (SQLException ex) {
            throw new EMPIException("Failure reading code from database table = " + tableName + ".." + ex.getMessage());
        } finally {
            this.close(stmt);
            this.close(rs);
        }
    }
}
