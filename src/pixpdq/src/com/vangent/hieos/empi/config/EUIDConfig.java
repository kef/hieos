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
package com.vangent.hieos.empi.config;

import com.vangent.hieos.empi.exception.EMPIException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class EUIDConfig implements ConfigItem {

    private final static Logger logger = Logger.getLogger(EUIDConfig.class);
    private static String EUID_ASSIGN_ENABLED = "euid-assign-enabled";
    private static String EUID_UNIVERSALID = "euid-universalid";
    private static String EUID_UNIVERSALID_TYPE = "euid-universalid-type";
    private boolean euidAssignEnabled = true;
    private String euidUniversalId = null;
    private String euidUniversalIdType = null;

    /**
     *
     * @return
     */
    public boolean isEuidAssignEnabled() {
        return euidAssignEnabled;
    }

    /**
     *
     * @param euidAssignEnabled
     */
    public void setEuidAssignEnabled(boolean euidAssignEnabled) {
        this.euidAssignEnabled = euidAssignEnabled;
    }

    /**
     *
     * @return
     */
    public String getEuidUniversalId() {
        return euidUniversalId;
    }

    /**
     * 
     * @param euidUniversalId
     */
    public void setEuidUniversalId(String euidUniversalId) {
        this.euidUniversalId = euidUniversalId;
    }

    /**
     *
     * @return
     */
    public String getEuidUniversalIdType() {
        return euidUniversalIdType;
    }

    /**
     *
     * @param euidUniversalIdType
     */
    public void setEuidUniversalIdType(String euidUniversalIdType) {
        this.euidUniversalIdType = euidUniversalIdType;
    }

    /**
     * 
     * @param hc
     * @param empiConfig
     * @throws EMPIException
     */
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {
        this.euidUniversalId = hc.getString(EUID_UNIVERSALID);
        this.euidUniversalIdType = hc.getString(EUID_UNIVERSALID_TYPE);
        this.euidAssignEnabled = hc.getBoolean(EUID_ASSIGN_ENABLED, true);
    }
}
