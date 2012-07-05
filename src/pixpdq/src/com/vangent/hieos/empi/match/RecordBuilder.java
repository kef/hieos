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
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class RecordBuilder {

    private final static Logger logger = Logger.getLogger(RecordBuilder.class);

    /**
     *
     * @param subject
     * @return
     * @throws EMPIException
     */
    public Record build(Subject subject) throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        Record record = new Record();
        record.setId(subject.getInternalId());

        // Go through each field (according to configuration).
        List<FieldConfig> fieldConfigs = empiConfig.getFieldConfigList();
        for (FieldConfig fieldConfig : fieldConfigs) {
            String sourceObjectPath = fieldConfig.getSourceObjectPath();
            String fieldName = fieldConfig.getName();
            // FIXME: Deal with different types (partially implemented)?
            if (logger.isTraceEnabled()) {
                logger.trace("field = " + fieldName);
                logger.trace(" ... sourceObjectPath = " + sourceObjectPath);
            }
            try {
                Object value;
                // Access the field value.
                if (sourceObjectPath.equalsIgnoreCase("subject")) {
                    value = subject;
                } else {
                    value = PropertyUtils.getProperty(subject, sourceObjectPath);
                }
                if (value != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(" ... value (before transforms) = " + value.toString());
                    }
                    // Now run any transforms (in order).
                    List<TransformFunctionConfig> transformFunctionConfigs = fieldConfig.getTransformFunctionConfigs();
                    for (TransformFunctionConfig transformFunctionConfig : transformFunctionConfigs) {
                        if (logger.isTraceEnabled()) {
                            logger.trace(" ... transformFunction = " + transformFunctionConfig.getName());
                        }
                        TransformFunction transformFunction = transformFunctionConfig.getTransformFunction();
                        value = transformFunction.transform(value);
                    }
                    if (value != null) {
                        if (logger.isTraceEnabled()) {
                            logger.trace(" ... value (after transforms) = " + value.toString());
                        }
                        Field field = new Field(fieldName, value.toString());
                        record.addField(field);
                    }
                }
            } catch (Exception ex) {
                logger.info(
                        "Unable to access '" + sourceObjectPath + "' field: "
                        + ex.getMessage());
            }
        }
        // Now, get rid of any superseded fields.
        this.removeSupersededFields(record, fieldConfigs);
        return record;
    }

    /**
     *
     * @param record
     * @param fieldConfigs
     */
    private void removeSupersededFields(Record record, List<FieldConfig> fieldConfigs) {
        // This should handle the case where a field has been removed along the way.
        for (FieldConfig fieldConfig : fieldConfigs) {
            String fieldName = fieldConfig.getName();
            Field field = record.getField(fieldName);
            if (field != null) {
                String supersedesFieldName = fieldConfig.getSupersedesField();
                if (supersedesFieldName != null) {
                    Field supersededField = record.getField(supersedesFieldName);
                    if (logger.isTraceEnabled()) {
                        logger.trace("+++++++++ REMOVING SUPERSEDED FIELD = " + supersedesFieldName);
                        logger.trace(" .... keeping field = " + fieldName);
                    }
                    record.removeField(supersededField);
                }
            }
        }
    }
}
