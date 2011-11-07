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
public class FieldConfig extends ConfigItem {

    private static String NAME = "name";
    private static String SOURCE_OBJECT_PATH = "source-object-path";
    private static String MATCH_DB_COLUMN = "match-db-column";
    private static String TRANSFORM_FUNCTIONS = "transform-functions.transform-function";
    private String name;
    private String sourceObjectPath;
    private String matchDatabaseColumn;
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
     * @param matchDatabaseColumn
     */
    public void setMatchDatabaseColumn(String matchDatabaseColumn) {
        this.matchDatabaseColumn = matchDatabaseColumn;
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
    public String getSourceObjectPath() {
        return sourceObjectPath;
    }

    /**
     *
     * @param sourceObjectPath
     */
    public void setSourceObjectPath(String sourceObjectPath) {
        this.sourceObjectPath = sourceObjectPath;
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
     */
    @Override
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {
        this.name = hc.getString(NAME);
        this.sourceObjectPath = hc.getString(SOURCE_OBJECT_PATH);
        this.matchDatabaseColumn = hc.getString(MATCH_DB_COLUMN);

        // Link up transforms.
        List transformFunctionNames = hc.getList(TRANSFORM_FUNCTIONS);
        for (Iterator it = transformFunctionNames.iterator(); it.hasNext();) {
            String transformFunctionName = (String) it.next();
            TransformFunctionConfig transformFunctionConfig = empiConfig.getTransformFunctionConfig(transformFunctionName);
            transformFunctionConfigs.add(transformFunctionConfig);
        }
    }
}
