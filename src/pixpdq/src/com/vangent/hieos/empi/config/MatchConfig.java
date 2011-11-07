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

import com.vangent.hieos.services.pixpdq.empi.exception.EMPIException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 *
 * @author Bernie Thuman
 */
public class MatchConfig extends ConfigItem {

    private static String ACCEPT_THRESHOLD = "accept-threshold";
    private static String REJECT_THRESHOLD = "reject-threshold";
    private static String MATCH_FIELDS = "match-fields.match-field";
    private double acceptThreshold;
    private double rejectThreshold;
    private List<MatchFieldConfig> matchFieldConfigs = new ArrayList<MatchFieldConfig>();

    /**
     *
     * @return
     */
    public double getAcceptThreshold() {
        return acceptThreshold;
    }

    /**
     *
     * @param acceptThreshold
     */
    public void setAcceptThreshold(double acceptThreshold) {
        this.acceptThreshold = acceptThreshold;
    }

    /**
     *
     * @return
     */
    public double getRejectThreshold() {
        return rejectThreshold;
    }

    /**
     *
     * @param rejectThreshold
     */
    public void setRejectThreshold(double rejectThreshold) {
        this.rejectThreshold = rejectThreshold;
    }

    /**
     *
     * @return
     */
    public List<MatchFieldConfig> getMatchFieldConfigs() {
        return matchFieldConfigs;
    }

    /**
     * 
     * @param fieldName
     * @return
     */
    public MatchFieldConfig getMatchFieldConfig(String fieldName) {
        // FIXME? - Should be ok to do sequential lookup (small number)
        for (MatchFieldConfig matchFieldConfig : matchFieldConfigs) {
            if (matchFieldConfig.getName().equalsIgnoreCase(fieldName)) {
                return matchFieldConfig;
            }
        }
        return null;  // Not found.
    }

    /**
     * 
     * @param hc
     * @param empiConfig
     */
    @Override
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {
        this.acceptThreshold = hc.getDouble(ACCEPT_THRESHOLD);
        this.rejectThreshold = hc.getDouble(REJECT_THRESHOLD);

        // Get the field-level configuration.
        List matchFields = hc.configurationsAt(MATCH_FIELDS);
        for (Iterator it = matchFields.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcSub = (HierarchicalConfiguration) it.next();
            MatchFieldConfig matchFieldConfig = new MatchFieldConfig();
            matchFieldConfig.load(hcSub, empiConfig);
            // FIXME: Put in map?
            matchFieldConfigs.add(matchFieldConfig);
        }
    }
}
