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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 *
 * @author Bernie Thuman
 */
public class BlockingPassConfig implements ConfigItem {

    private static String BLOCKING_FIELDS = "blocking-fields.blocking-field";
    private static String BLOCKING_FIELD_NAME = "name";
    private List<FieldConfig> blockingFieldConfigs = new ArrayList<FieldConfig>();

    /**
     *
     * @return
     */
    public List<FieldConfig> getBlockingFieldConfigs() {
        return blockingFieldConfigs;
    }

    /**
     *
     * @param hc
     * @param empiConfig
     * @throws EMPIException
     */
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {

        // Get the field-level configuration.
        List blockingFields = hc.configurationsAt(BLOCKING_FIELDS);
        for (Iterator it = blockingFields.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcBlockingField = (HierarchicalConfiguration) it.next();
            String blockingFieldName = hcBlockingField.getString(BLOCKING_FIELD_NAME);

            // Link it up.
            FieldConfig blockingFieldConfig = empiConfig.getFieldConfig(blockingFieldName);

            blockingFieldConfigs.add(blockingFieldConfig);
        }
    }
}
