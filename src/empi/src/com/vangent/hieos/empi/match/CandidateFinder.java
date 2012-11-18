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

import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.match.MatchAlgorithm.MatchType;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public abstract class CandidateFinder {

    private PersistenceManager persistenceManager;

    /**
     * 
     */
    public CandidateFinder() {
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
     * @return
     */
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
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
     * @param pm
     * @return
     * @throws EMPIException
     */
    static public CandidateFinder getCandidateFinder(PersistenceManager pm) throws EMPIException {

        // Get EMPI configuration.
        EMPIConfig empiConfig = EMPIConfig.getInstance();

        // Get candidate finder (configurable).
        CandidateFinder candidateFinder = empiConfig.getCandidateFinder();
        candidateFinder.setPersistenceManager(pm);

        return candidateFinder;
    }
}
