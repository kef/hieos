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
public class MatchFieldConfig extends ConfigItem {

    private static String NAME = "name";
    private static String ACCEPT_THRESHOLD = "accept-threshold";
    private static String REJECT_THRESHOLD = "reject-threshold";
    private static String WEIGHT = "weight";
    private static String DISTANCE_FUNCTION = "distance-function";
    private String name;
    private double acceptThreshold;
    private double rejectThreshold;
    private double weight;
    private DistanceFunctionConfig distanceFunctionConfig;
    private FieldConfig fieldConfig;

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
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
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
    public double getWeight() {
        return weight;
    }

    /**
     *
     * @param weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
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
     * @param distanceFunctionConfig
     */
    public void setDistanceFunctionConfig(DistanceFunctionConfig distanceFunctionConfig) {
        this.distanceFunctionConfig = distanceFunctionConfig;
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
     * @param fieldConfig
     */
    public void setFieldConfig(FieldConfig fieldConfig) {
        this.fieldConfig = fieldConfig;
    }

    /**
     * 
     * @param hc
     * @param empiConfig
     * @throws EMPIException
     */
    @Override
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {
        this.name = hc.getString(NAME);
        this.acceptThreshold = hc.getDouble(ACCEPT_THRESHOLD);
        this.rejectThreshold = hc.getDouble(REJECT_THRESHOLD);
        this.weight = hc.getDouble(WEIGHT);

        // Link to distance function configuration.
        String distanceFunctionName = hc.getString(DISTANCE_FUNCTION);
        this.distanceFunctionConfig = empiConfig.getDistanceFunctionConfig(distanceFunctionName);

        // Link to field configuration.
        this.fieldConfig = empiConfig.getFieldConfig(this.name);
    }
}
