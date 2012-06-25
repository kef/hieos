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
package com.vangent.hieos.empi.match;

import com.vangent.hieos.empi.config.BlockingConfig;
import com.vangent.hieos.empi.config.BlockingFieldConfig;
import com.vangent.hieos.empi.config.BlockingPassConfig;
import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.config.FieldConfig;
import com.vangent.hieos.empi.config.MatchConfig;
import com.vangent.hieos.empi.config.MatchFieldConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.match.MatchAlgorithm.MatchType;
import com.vangent.hieos.empi.persistence.PersistenceHelper;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class BlockCandidateFinder extends CandidateFinder {

    // FIXME: Make configurable / abstract / interface ?

    private final static Logger logger = Logger.getLogger(BlockCandidateFinder.class);

    /**
     * 
     */
    public BlockCandidateFinder() {
    }

    /**
     *
     * @param searchRecord
     * @param matchType
     * @return
     * @throws EMPIException
     */
    @Override
    public List<Record> findCandidates(Record searchRecord, MatchType matchType) throws EMPIException {
        List<Record> records = new ArrayList<Record>();
        Set<String> candidateRecordIds = new HashSet<String>();

        // Get configuration items.
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        MatchConfig matchConfig = empiConfig.getMatchConfig(matchType);
        BlockingConfig blockingConfig = matchConfig.getBlockingConfig();
        List<BlockingPassConfig> blockingPassConfigs = blockingConfig.getBlockingPassConfigs();

        // Run through each blocking pass.
        for (BlockingPassConfig blockingPassConfig : blockingPassConfigs) {
            ResultSet rs = null;
            PreparedStatement stmt = null;
            try {
                // Get prepared statement to support the blocking pass and then execute query.
                stmt = this.getBlockingPassPreparedStatement(matchConfig, searchRecord, blockingPassConfig);
                if (stmt != null) {  // Only process if blocking pass is active based upon search criteria.
                    rs = stmt.executeQuery();

                    // Process each query result (and avoid duplicates across blocking passes).
                    while (rs.next()) {
                        String recordId = rs.getString(1); // id is always position 1.

                        // Avoid duplicates across blocking passes.
                        if (!candidateRecordIds.contains(recordId)) {
                            candidateRecordIds.add(recordId);  // Make sure to avoid duplicates on further passes.
                            Record record = this.buildRecordFromResultSet(rs, recordId, matchConfig);
                            records.add(record);
                        }
                    }
                }
            } catch (SQLException ex) {
                throw PersistenceHelper.getEMPIException("Exception reading 'subject_match_fields' records from database", ex);
            } finally {
                PersistenceHelper.close(stmt);
                PersistenceHelper.close(rs);
            }
        }

        return records;
    }

    /**
     *
     * @param matchConfig
     * @param searchRecord
     * @param blockingPassConfig
     * @return
     * @throws EMPIException
     */
    private PreparedStatement getBlockingPassPreparedStatement(MatchConfig matchConfig, Record searchRecord,
            BlockingPassConfig blockingPassConfig) throws EMPIException {
        // Get active blocking field configs based upon the search record.
        List<BlockingFieldConfig> activeBlockingFieldConfigs = this.getActiveBlockingFieldConfigs(searchRecord, blockingPassConfig);
        PreparedStatement stmt = null;
        if (!activeBlockingFieldConfigs.isEmpty()) {
            // Build prepared statement to support "blocking" phase.
            String sql = this.buildBlockingPassSQLSelectStatement(matchConfig, searchRecord, activeBlockingFieldConfigs);
            stmt = PersistenceHelper.getPreparedStatement(sql, this.getPersistenceManager().getConnection());
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
                PersistenceHelper.close(stmt);
                throw PersistenceHelper.getEMPIException("Exception reading 'subject_match_fields' records from database", ex);
            }
        }
        return stmt;
    }

    /**
     * 
     * @param rs
     * @param recordId
     * @param matchConfig
     * @return
     * @throws SQLException
     */
    private Record buildRecordFromResultSet(ResultSet rs, String recordId, MatchConfig matchConfig) throws SQLException {
        Record record = new Record();
        record.setId(recordId);

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
     * @param searchRecord
     * @param activeBlockingFieldConfigs
     * @return
     * @throws EMPIException
     */
    private String buildBlockingPassSQLSelectStatement(MatchConfig matchConfig, Record searchRecord, List<BlockingFieldConfig> activeBlockingFieldConfigs) throws EMPIException {
        // Get EMPI configuration.
        //EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Build
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT subject_id,");  // Always extract the subject_id.

        // Add list of fields to extract (only those we plan on matching against).
        //MatchConfig matchConfig = empiConfig.getMatchConfig();
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
}
