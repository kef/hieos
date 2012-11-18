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

/**
 *
 * @author Bernie Thuman
 */
public class MatchFieldConfig implements ConfigItem {

    private static String NAME = "name";
    private static String ACCEPT_THRESHOLD = "accept-threshold";
    private static String REJECT_THRESHOLD = "reject-threshold";
    private static String WEIGHT = "weight";
    private static String DISTANCE_FUNCTION = "distance-function(0)";
    //private static String ENABLED_DURING_SUBJECT_ADD = "enabled-during-subject-add";
    private static String DISTANCE_FUNCTION_NAME = "name";
    private String name;
    private double acceptThreshold;
    private double rejectThreshold;
    private double weight;
    private DistanceFunctionConfig distanceFunctionConfig;
    private FieldConfig fieldConfig;
    //private boolean enabledDuringSubjectAdd;

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    //public boolean isEnabledDuringSubjectAdd() {
    //    return enabledDuringSubjectAdd;
    //}

    /**
     *
     * @return
     */
    public double getAcceptThreshold() {
        return acceptThreshold;
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
     * @return
     */
    public double getWeight() {
        return weight;
    }

    /**
     *
     * @return
     */
    public DistanceFunctionConfig getDistanceFunctionConfig() {
        return distanceFunctionConfig;
    }

    /**
     *
     * @return
     */
    public FieldConfig getFieldConfig() {
        return fieldConfig;
    }

    /**
     * 
     * @param hc
     * @param empiConfig
     * @throws EMPIException
     */
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {
        this.name = hc.getString(NAME);
        //this.enabledDuringSubjectAdd = hc.getBoolean(ENABLED_DURING_SUBJECT_ADD, true);
        this.acceptThreshold = hc.getDouble(ACCEPT_THRESHOLD);
        this.rejectThreshold = hc.getDouble(REJECT_THRESHOLD);
        this.weight = hc.getDouble(WEIGHT);

        // Link to distance function configuration.
        HierarchicalConfiguration hcDistanceFunction = hc.configurationAt(DISTANCE_FUNCTION);
        String distanceFunctionName = hcDistanceFunction.getString(DISTANCE_FUNCTION_NAME);
        this.distanceFunctionConfig = this.getDistanceFunctionConfig(empiConfig, hcDistanceFunction, distanceFunctionName);

        // Link to field configuration.
        this.fieldConfig = empiConfig.getFieldConfig(this.name);
    }

    /**
     *
     * @param empiConfig
     * @param hcFunction
     * @param functionName
     * @return
     * @throws EMPIException
     */
    private DistanceFunctionConfig getDistanceFunctionConfig(
            EMPIConfig empiConfig, HierarchicalConfiguration hcFunction, String functionName) throws EMPIException {
        DistanceFunctionConfig functionConfig = empiConfig.getDistanceFunctionConfig(functionName);
        functionConfig = (DistanceFunctionConfig) functionConfig.loadFunctionConfig(hcFunction);
        return functionConfig;
    }
}
