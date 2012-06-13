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

import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.exception.EMPIException;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public abstract class MatchAlgorithm {

    private PersistenceManager persistenceManager = null;

    /**
     * 
     */
    public enum MatchType {

        /**
         * 
         */
        SUBJECT_FIND,
        /**
         * 
         */
        SUBJECT_FEED
    };

    /**
     *
     */
    public MatchAlgorithm() {
    }

    /**
     *
     * @return
     */
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    /**
     * 
     * @param persistenceManager
     */
    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    /**
     * 
     * @param searchRecord
     * @param matchType
     * @return
     * @throws EMPIException
     */
    abstract public List<Record> findCandidates(Record searchRecord, MatchType matchType) throws EMPIException;

    /**
     * 
     * @param searchRecord
     * @param matchType
     * @return
     * @throws EMPIException
     */
    abstract public MatchResults findMatches(Record searchRecord, MatchType matchType) throws EMPIException;

    /**
     * 
     * @param searchRecord
     * @param records
     * @param matchType
     * @return
     * @throws EMPIException
     */
    abstract public MatchResults findMatches(Record searchRecord, List<Record> records, MatchType matchType) throws EMPIException;
}
