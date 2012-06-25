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

import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.config.FieldConfig;
import com.vangent.hieos.empi.match.Field;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.exception.EMPIException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectMatchFieldsDAO extends AbstractDAO {

    private final static Logger logger = Logger.getLogger(SubjectMatchFieldsDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectMatchFieldsDAO(Connection connection) {
        super(connection);
    }

    /**
     *
     * @param records
     * @throws EMPIException
     */
    public void insert(List<Record> records) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            // Get the prepared statement (based on configuration).
            stmt = this.buildSQLInsertPreparedStatement();

            // Now insert each record (add to the batch).
            for (Record record : records) {
                this.setSQLInsertPreparedStatementValues(stmt, record);
                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectMatchFieldsDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception inserting Subject match fields", ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param subjectId
     * @throws EMPIException
     */
    public void deleteSubjectRecords(String subjectId) throws EMPIException {
        this.deleteRecords(subjectId, "subject_match_fields", "subject_id", this.getClass().getName());
    }

    /**
     * 
     * @param stmt
     * @param record
     * @throws EMPIException
     */
    private void setSQLInsertPreparedStatementValues(PreparedStatement stmt, Record record) throws EMPIException {
        // Get EMPI configuration.
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        List<FieldConfig> fieldConfigs = empiConfig.getFieldConfigList();

        try {
            // Go through each field and set the proper values in the prepared statement.
            int fieldIndex = 1;
            stmt.setString(fieldIndex, record.getId());
            for (FieldConfig fieldConfig : fieldConfigs) {
                boolean isStoreField = fieldConfig.isStoreField();
                if (isStoreField) {
                    String fieldName = fieldConfig.getName();
                    Field field = record.getField(fieldName);
                    String fieldValue = (field == null) ? null : field.getValue();
                    stmt.setString(++fieldIndex, fieldValue);
                }
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception prepared statement", ex);
        }
    }

    /**
     *
     * @return
     * @throws EMPIException
     */
    private PreparedStatement buildSQLInsertPreparedStatement() throws EMPIException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO subject_match_fields(subject_id,");

        // Get EMPI configuration.
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        List<FieldConfig> fieldConfigs = empiConfig.getFieldConfigList();

        // Go through each field and build SQL INSERT string.

        // Determine how many fields should be stored.
        int numFieldsToStore = 0;
        for (FieldConfig fieldConfig : fieldConfigs) {
            boolean isStoreField = fieldConfig.isStoreField();
            if (isStoreField) {
                ++numFieldsToStore;
            }
        }

        // First, get the database columns (to insert).
        int fieldIndex = 0;
        for (FieldConfig fieldConfig : fieldConfigs) {
            boolean isStoreField = fieldConfig.isStoreField();
            if (isStoreField) {
                String dbColumnName = fieldConfig.getMatchDatabaseColumn();
                sb.append(dbColumnName);
                ++fieldIndex;
                if (fieldIndex != numFieldsToStore) {
                    sb.append(",");
                }
            }
        }
        sb.append(") values(?,");  // Single ? for identifier.

        // Now, add the ? to correspond to each database column.
        for (fieldIndex = 1; fieldIndex <= numFieldsToStore; fieldIndex++) {
            sb.append("?");
            if (fieldIndex != numFieldsToStore) {
                sb.append(",");
            }
        }
        sb.append(")");
        String sql = sb.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SQL = " + sql);
        }
        return this.getPreparedStatement(sql);
    }
}
