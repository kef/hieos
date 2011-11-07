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

import com.vangent.hieos.empi.config.BlockingConfig;
import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.config.FieldConfig;
import com.vangent.hieos.empi.config.MatchConfig;
import com.vangent.hieos.empi.config.MatchFieldConfig;
import com.vangent.hieos.empi.match.Field;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.exception.EMPIException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectMatchDAO extends AbstractDAO {

    private final static Logger logger = Logger.getLogger(SubjectMatchDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectMatchDAO(Connection connection) {
        super(connection);
    }

    /**
     * 
     * @param searchRecord
     * @return
     * @throws EMPIException
     */
    public List<Record> lookup(Record searchRecord) throws EMPIException {
        List<Record> records = new ArrayList<Record>();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            // Get active blocking field configs based upon the search record.
            List<FieldConfig> activeBlockingFieldConfigs = this.getActiveBlockingFieldConfigs(searchRecord);

            // Build prepared statement to support "blocking" phase.
            stmt = this.buildSQLSelectPreparedStatement(activeBlockingFieldConfigs);

            // Set WHERE clause values in the prepared statement.
            int fieldIndex = 0;
            for (FieldConfig activeBlockingFieldConfig : activeBlockingFieldConfigs) {
                System.out.println("Blocking field = " + activeBlockingFieldConfig.getName());
                Field field = searchRecord.getField(activeBlockingFieldConfig.getName());
                System.out.println(" ... WHERE " + field.getName() + "=" + field.getValue());
                stmt.setString(++fieldIndex, field.getValue());
            }
            // Execure query.
            rs = stmt.executeQuery();

            // Process each query result.
            EMPIConfig empiConfig = EMPIConfig.getInstance();
            MatchConfig matchConfig = empiConfig.getMatchConfig();
            while (rs.next()) {
                Record record = new Record();
                record.setId(rs.getString(1)); // id is always position 1.

                // Now fill in the match fields from the result set.
                List<MatchFieldConfig> matchFieldConfigs = matchConfig.getMatchFieldConfigs();
                fieldIndex = 1;
                for (MatchFieldConfig matchFieldConfig : matchFieldConfigs) {
                    Field field = new Field(matchFieldConfig.getName(), rs.getString(++fieldIndex));
                    record.addField(field);
                }
                records.add(record);
            }
        } catch (SQLException ex) {
            throw new EMPIException("Failure reading 'subjectmatch' records from database" + ex.getMessage());
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return records;
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
                logger.trace("SubjectMatchDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }
        } catch (SQLException ex) {
            throw new EMPIException(ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param subjectId
     * @throws EMPIException
     */
    public void delete(String subjectId) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            String sql = "DELETE FROM subjectmatch WHERE id=?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, subjectId);
            long startTime = System.currentTimeMillis();
            stmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectMatch.delete: done executeBatch elapedTimeMillis=" + (endTime - startTime));
            }
        } catch (SQLException ex) {
            throw new EMPIException(ex);
        } finally {
            this.close(stmt);
        }
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
                String fieldName = fieldConfig.getName();
                Field field = record.getField(fieldName);
                String fieldValue = (field == null) ? null : field.getValue();
                stmt.setString(++fieldIndex, fieldValue);
            }
        } catch (SQLException ex) {
            throw new EMPIException(ex);
        }
    }

    /**
     *
     * @return
     * @throws EMPIException
     */
    private PreparedStatement buildSQLInsertPreparedStatement() throws EMPIException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO subjectmatch(id,");

        // Get EMPI configuration.
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        List<FieldConfig> fieldConfigs = empiConfig.getFieldConfigList();

        // Go through each field and build SQL INSERT string.

        // First, get the database columns (to insert).
        int fieldIndex = 0;
        int numFields = fieldConfigs.size();
        for (FieldConfig fieldConfig : fieldConfigs) {
            String dbColumnName = fieldConfig.getMatchDatabaseColumn();
            sb.append(dbColumnName);
            ++fieldIndex;
            if (fieldIndex != numFields) {
                sb.append(",");
            }
        }
        sb.append(") values(?,");  // Single ? for identifier.

        // Now, add the ? to correspond to each database column.
        for (fieldIndex = 1; fieldIndex <= numFields; fieldIndex++) {
            sb.append("?");
            if (fieldIndex != numFields) {
                sb.append(",");
            }
        }
        sb.append(")");
        String sql = sb.toString();
        System.out.println("INSERT SQL = " + sql);

        return this.getPreparedStatement(sql);
    }

    /**
     *
     * @param activeBlockingFieldConfigs
     * @return
     * @throws EMPIException
     */
    private PreparedStatement buildSQLSelectPreparedStatement(List<FieldConfig> activeBlockingFieldConfigs) throws EMPIException {
        // Get EMPI configuration.
        EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Build
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT id,");  // Always extract the id.

        // Add list of fields to extract (only those we plan on matching against).
        MatchConfig matchConfig = empiConfig.getMatchConfig();
        List<MatchFieldConfig> matchFieldConfigs = matchConfig.getMatchFieldConfigs();
        int fieldIndex = 0;
        int mumMatchFields = matchFieldConfigs.size();
        for (MatchFieldConfig matchFieldConfig : matchFieldConfigs) {
            FieldConfig fieldConfig = matchFieldConfig.getFieldConfig();
            String dbColumnName = fieldConfig.getMatchDatabaseColumn();
            sb.append(dbColumnName);
            ++fieldIndex;
            if (fieldIndex != mumMatchFields) {
                sb.append(",");
            }
        }
        sb.append(" FROM subjectmatch WHERE ");

        // Build the where clause (on blocking fields).
        fieldIndex = 0;
        int numActiveBlockingFields = activeBlockingFieldConfigs.size();
        for (FieldConfig activeBlockingFieldConfig : activeBlockingFieldConfigs) {
            String dbColumnName = activeBlockingFieldConfig.getMatchDatabaseColumn();
            sb.append(dbColumnName).append(" = ?");
            ++fieldIndex;
            if (fieldIndex != numActiveBlockingFields) {
                sb.append(" AND ");
            }
        }
        String sql = sb.toString();
        System.out.println("SELECT SQL = " + sql);

        return this.getPreparedStatement(sql);
    }

    /**
     * 
     * @param searchRecord
     * @return
     * @throws EMPIException
     */
    private List<FieldConfig> getActiveBlockingFieldConfigs(Record searchRecord) throws EMPIException {
        // Get EMPI configuration.
        EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Get blocking field configs.
        BlockingConfig blockingConfig = empiConfig.getBlockingConfig();
        List<FieldConfig> blockingFieldConfigs = blockingConfig.getBlockingFieldConfigs();

        // Loop through all blocking configs and add only those where the search record
        // has a value.
        List<FieldConfig> activeBlockingConfigs = new ArrayList<FieldConfig>();
        for (FieldConfig blockingFieldConfig : blockingFieldConfigs) {
            Field field = searchRecord.getField(blockingFieldConfig.getName());
            if (field != null && field.getValue() != null) {
                activeBlockingConfigs.add(blockingFieldConfig);
            }
        }
        return activeBlockingConfigs;
    }
}
