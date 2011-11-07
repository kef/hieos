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
import com.vangent.hieos.empi.config.FieldConfig;
import com.vangent.hieos.empi.config.TransformFunctionConfig;
import com.vangent.hieos.empi.transform.TransformFunction;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.empi.exception.EMPIException;
import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author Bernie Thuman
 */
public class RecordBuilder {

    /**
     *
     * @param subject
     * @return
     * @throws EMPIException
     */
    public Record build(Subject subject) throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        Record record = new Record();
        record.setId(subject.getId());

        // Go through each field (according to configuration).
        List<FieldConfig> fieldConfigs = empiConfig.getFieldConfigList();
        for (FieldConfig fieldConfig : fieldConfigs) {
            String sourceObjectPath = fieldConfig.getSourceObjectPath();
            String fieldName = fieldConfig.getName();
            System.out.println("field name = " + fieldName);
            System.out.println(" ... sourceObjectPath = " + sourceObjectPath);
            try {
                // FIXME: Deal with different types?
                // Now access the field value.
                Object value = PropertyUtils.getProperty(subject, sourceObjectPath);
                if (value != null) {
                    System.out.println(" ... value = " + value.toString());

                    // Now run any transforms (in order).
                    List<TransformFunctionConfig> transformFunctionConfigs = fieldConfig.getTransformFunctionConfigs();
                    for (TransformFunctionConfig transformFunctionConfig : transformFunctionConfigs) {
                        System.out.println(" ... transformFunction = " + transformFunctionConfig.getName());
                        TransformFunction transformFunction = transformFunctionConfig.getTransformFunction();
                        value = transformFunction.transform(value);
                    }
                    System.out.println(" ... value (final) = " + value.toString());
                    Field field = new Field(fieldName, value.toString());
                    record.addField(field);
                }
            } catch (Exception ex) {
                // FIXME: Do something!!!
                System.out.println("Exception: " + ex.getMessage());
            }
        }

        return record;
    }
}
