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

import com.vangent.hieos.empi.config.BlockingFieldConfig;
import com.vangent.hieos.empi.config.BlockingPassConfig;
import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.config.FieldConfig;
import com.vangent.hieos.empi.config.MatchConfig;
import com.vangent.hieos.empi.config.MatchFieldConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.match.Field;
import com.vangent.hieos.empi.match.MatchAlgorithm.MatchType;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.subjectmodel.InternalId;
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
public class SubjectMatchFieldsDAO extends AbstractDAO {

    private final static Logger logger = Logger.getLogger(SubjectMatchFieldsDAO.class);

    /**
     *
     * @param persistenceManager
     */
    public SubjectMatchFieldsDAO(PersistenceManager persistenceManager) {
        super(persistenceManager);
    }

    /**
     * 
     * @param subjectId
     * @param matchType
     * @return
     * @throws EMPIException
     */
    public Record load(InternalId subjectId, MatchType matchType) throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        MatchConfig matchConfig = empiConfig.getMatchConfig(matchType);
        Record record = new Record();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = this.getSQLSelectStatement(matchConfig);
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            stmt.setLong(1, subjectId.getId());
            // Execute query.
            rs = stmt.executeQuery();
            if (rs.next()) {
                record = this.buildRecordFromResultSet(rs, subjectId.getId(), matchConfig);
            }
        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception reading subject_match_fields(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return record;
    }

    /**
     *
     * @param matchConfig
     * @param searchRecord
     * @param activeBlockingFieldConfigs
     * @return
     * @throws EMPIException
     */
    public String buildBlockingPassSQLSelectStatement(MatchConfig matchConfig, Record searchRecord, List<BlockingFieldConfig> activeBlockingFieldConfigs) throws EMPIException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT subject_id,");  // Always extract the subject_id.

        // Add list of fields to extract (only those we plan on matching against).
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
        sb.append(" FROM subject_match_fields WHERE ");

        // Build the where clause (on blocking fields).
        fieldIndex = 0;
        int numActiveBlockingFields = activeBlockingFieldConfigs.size();
        for (BlockingFieldConfig activeBlockingFieldConfig : activeBlockingFieldConfigs) {
            FieldConfig fieldConfig = activeBlockingFieldConfig.getFieldConfig();
            String dbColumnName = fieldConfig.getMatchDatabaseColumn();
            // FIXME:
            // HACK: See if % is at end of string.
            Field searchField = searchRecord.getField(activeBlockingFieldConfig.getName());
            String searchFieldValue = searchField.getValue();
            sb.append(dbColumnName);
            if (searchFieldValue.endsWith("%")) {
                sb.append(" LIKE ?");
            } else {
                sb.append(" = ?");
            }
            ++fieldIndex;
            if (fieldIndex != numActiveBlockingFields) {
                sb.append(" AND ");
            }
        }
        String sql = sb.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SQL = " + sql);
        }
        return sql;
    }

    /**
     *
     * @param matchConfig
     * @param searchRecord
     * @param blockingPassConfig
     * @return
     * @throws EMPIException
     */
    public PreparedStatement getBlockingPassPreparedStatement(MatchConfig matchConfig, Record searchRecord,
            BlockingPassConfig blockingPassConfig) throws EMPIException {
        // Get active blocking field configs based upon the search record.
        List<BlockingFieldConfig> activeBlockingFieldConfigs = this.getActiveBlockingFieldConfigs(searchRecord, blockingPassConfig);
        PreparedStatement stmt = null;
        if (!activeBlockingFieldConfigs.isEmpty()) {
            // Build prepared statement to support "blocking" phase.
            String sql = this.buildBlockingPassSQLSelectStatement(matchConfig, searchRecord, activeBlockingFieldConfigs);
            stmt = this.getPreparedStatement(sql);
            try {
                // Set WHERE clause values in the prepared statement.
                int fieldIndex = 0;
                for (BlockingFieldConfig activeBlockingFieldConfig : activeBlockingFieldConfigs) {
                    Field field = searchRecord.getField(activeBlockingFieldConfig.getName());
                    if (logger.isTraceEnabled()) {
                        logger.trace("Blocking field = " + activeBlockingFieldConfig.getName());
                        logger.trace(" ... WHERE "
                                + activeBlockingFieldConfig.getFieldConfig().getMatchDatabaseColumn()
                                + " (" + field.getName() + ") =" + field.getValue());
                    }
                    //stmt.setString(++fieldIndex, field.getValue() + "%");
                    stmt.setString(++fieldIndex, field.getValue());
                }
            } catch (SQLException ex) {
                this.close(stmt);
                throw PersistenceManager.getEMPIException("Exception reading 'subject_match_fields' records from database", ex);
            }
        }
        return stmt;
    }

    /**
     *
     * @param searchRecord
     * @return
     * @throws EMPIException
     */
    private List<BlockingFieldConfig> getActiveBlockingFieldConfigs(Record searchRecord, BlockingPassConfig blockingPassConfig) throws EMPIException {
        // Get blocking field configs.
        List<BlockingFieldConfig> blockingFieldConfigs = blockingPassConfig.getBlockingFieldConfigs();

        // Loop through all blocking configs and add only those where the search record
        // has a value (unless it is required).  If any required field is missing, the blocking
        // pass is considered invalid and no blocking field configurations are returned.
        List<BlockingFieldConfig> activeBlockingFieldConfigs = new ArrayList<BlockingFieldConfig>();
        for (BlockingFieldConfig blockingFieldConfig : blockingFieldConfigs) {
            Field field = searchRecord.getField(blockingFieldConfig.getName());
            if (field == null && blockingFieldConfig.isRequired() == true) {
                //System.out.println("+++++ Skipping blocking pass (missing required field = "
                //        + blockingFieldConfig.getName() + ") +++++");

                // There is no search field for the blocking field, yet it is required.
                // This blocking pass is now invalid.

                // Clear out any active blocking field configs.
                activeBlockingFieldConfigs.clear();
                break;  // Get out of the loop now!
            }
            if (field != null && field.getValue() != null) {
                activeBlockingFieldConfigs.add(blockingFieldConfig);
            }
        }
        return activeBlockingFieldConfigs;
    }

    /**
     *
     * @param rs
     * @param recordId
     * @param matchConfig
     * @return
     * @throws SQLException
     */
    public Record buildRecordFromResultSet(ResultSet rs, Long recordId, MatchConfig matchConfig) throws SQLException {
        Record record = new Record();
        InternalId internalId = new InternalId(recordId);
        record.setId(internalId);

        // Fill in the match fields from the result set.
        List<MatchFieldConfig> matchFieldConfigs = matchConfig.getMatchFieldConfigs();
        int fieldIndex = 1;
        for (MatchFieldConfig matchFieldConfig : matchFieldConfigs) {
            Field field = new Field(matchFieldConfig.getName(), rs.getString(++fieldIndex));
            record.addField(field);
        }
        return record;
    }

    /**
     * 
     * @param matchConfig
     * @return
     * @throws EMPIException
     */
    private String getSQLSelectStatement(MatchConfig matchConfig) throws EMPIException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT subject_id,");  // Always extract the subject_id.

        // Add list of fields to extract (only those we plan on matching against).
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
        sb.append(" FROM subject_match_fields WHERE subject_id=?");
        String sql = sb.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("SQL = " + sql);
        }
        return sql;
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
            throw PersistenceManager.getEMPIException("Exception inserting Subject match fields", ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param subjectId
     * @throws EMPIException
     */
    public void deleteSubjectRecords(InternalId subjectId) throws EMPIException {
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
            stmt.setLong(fieldIndex, record.getInternalId().getId());
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
            throw PersistenceManager.getEMPIException("Exception prepared statement", ex);
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
