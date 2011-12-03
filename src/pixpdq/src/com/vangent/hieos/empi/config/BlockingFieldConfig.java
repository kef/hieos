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
public class BlockingFieldConfig implements ConfigItem {

    private static String NAME = "name";
    private static String REQUIRED = "required";
    private String name;
    private boolean required;
    private FieldConfig fieldConfig;

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
    public boolean isRequired() {
        return required;
    }

    /**
     *
     * @param required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * 
     * @param hc
     * @param empiConfig
     * @throws EMPIException
     */
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {
        this.name = hc.getString(NAME);
        this.required = hc.getBoolean(REQUIRED, false);

        // Link to field configuration.
        this.fieldConfig = empiConfig.getFieldConfig(this.name);
    }
}
