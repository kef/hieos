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
import com.vangent.hieos.empi.config.BlockingPassConfig;
import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.config.MatchConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.match.MatchAlgorithm.MatchType;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.persistence.SubjectMatchFieldsDAO;
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
        Set<Long> candidateRecordIds = new HashSet<Long>();

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
                        Long recordId = rs.getLong(1); // id is always position 1.

                        // Avoid duplicates across blocking passes.
                        if (!candidateRecordIds.contains(recordId)) {
                            candidateRecordIds.add(recordId);  // Make sure to avoid duplicates on further passes.
                            Record record = this.buildRecordFromResultSet(rs, recordId, matchConfig);
                            records.add(record);
                        }
                    }
                }
            } catch (SQLException ex) {
                throw PersistenceManager.getEMPIException("Exception reading 'subject_match_fields' records from database", ex);
            } finally {
                PersistenceManager.close(stmt);
                PersistenceManager.close(rs);
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
        SubjectMatchFieldsDAO dao = new SubjectMatchFieldsDAO(this.getPersistenceManager());
        return dao.getBlockingPassPreparedStatement(matchConfig, searchRecord, blockingPassConfig);
    }

    /**
     * 
     * @param rs
     * @param recordId
     * @param matchConfig
     * @return
     * @throws SQLException
     */
    private Record buildRecordFromResultSet(ResultSet rs, Long recordId, MatchConfig matchConfig) throws SQLException {
        SubjectMatchFieldsDAO dao = new SubjectMatchFieldsDAO(this.getPersistenceManager());
        return dao.buildRecordFromResultSet(rs, recordId, matchConfig);
    }
}
