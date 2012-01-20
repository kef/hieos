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
public class FieldConfig implements ConfigItem {

    private static String FIELD_NAME = "name";
    private static String SOURCE_OBJECT_PATH = "source-object-path";
    private static String MATCH_DB_COLUMN = "match-db-column";
    private static String STORE_FIELD = "store-field";
    private static String SUPERSEDES_FIELD = "supersedes-field";
    private static String TRANSFORM_FUNCTIONS = "transform-functions.transform-function";
    private static String TRANSFORM_FUNCTION_NAME = "name";
    private String name;
    private String sourceObjectPath;
    private String matchDatabaseColumn;
    private String supersedesField = null;
    private boolean storeField;
    private List<TransformFunctionConfig> transformFunctionConfigs = new ArrayList<TransformFunctionConfig>();

    /**
     *
     * @return
     */
    public String getMatchDatabaseColumn() {
        return matchDatabaseColumn;
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
     * @return
     */
    public String getSourceObjectPath() {
        return sourceObjectPath;
    }

    /**
     * 
     * @return
     */
    public boolean isStoreField() {
        return storeField;
    }

    /**
     *
     * @return
     */
    public String getSupersedesField() {
        return supersedesField;
    }

    /**
     *
     * @return
     */
    public List<TransformFunctionConfig> getTransformFunctionConfigs() {
        return transformFunctionConfigs;
    }

    /**
     *
     * @param hc
     * @param empiConfig
     * @throws EMPIException
     */
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {
        this.name = hc.getString(FIELD_NAME);
        this.sourceObjectPath = hc.getString(SOURCE_OBJECT_PATH);
        this.matchDatabaseColumn = hc.getString(MATCH_DB_COLUMN);
        this.supersedesField = hc.getString(SUPERSEDES_FIELD);
        this.storeField = hc.getBoolean(STORE_FIELD, true);

        // Link up transforms.
        List transformFunctions = hc.configurationsAt(TRANSFORM_FUNCTIONS);
        for (Iterator it = transformFunctions.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcTransformFunction = (HierarchicalConfiguration) it.next();
            String transformFunctionName = hcTransformFunction.getString(TRANSFORM_FUNCTION_NAME);
            TransformFunctionConfig transformFunctionConfig = this.getTransformFunctionConfig(empiConfig, hcTransformFunction, transformFunctionName);
            transformFunctionConfigs.add(transformFunctionConfig);
        }
    }

    /**
     * 
     * @param empiConfig
     * @param hcFunction
     * @param functionName
     * @return
     * @throws EMPIException
     */
    private TransformFunctionConfig getTransformFunctionConfig(
            EMPIConfig empiConfig, HierarchicalConfiguration hcFunction, String functionName) throws EMPIException {
        TransformFunctionConfig functionConfig = empiConfig.getTransformFunctionConfig(functionName);
        functionConfig = (TransformFunctionConfig) functionConfig.loadFunctionConfig(hcFunction);
        return functionConfig;
    }
}
