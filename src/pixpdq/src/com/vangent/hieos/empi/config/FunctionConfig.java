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

import com.vangent.hieos.empi.function.Function;
import com.vangent.hieos.services.pixpdq.empi.exception.EMPIException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class FunctionConfig extends ConfigItem {

    private final static Logger logger = Logger.getLogger(FunctionConfig.class);
    private static String NAME = "name";
    private static String CLASS_NAME = "class";
    private String name;
    private String className;
    private Function function;

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
    public String getClassName() {
        return className;
    }

    /**
     *
     * @param className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     *
     * @return
     */
    public Function getFunction() {
        return function;
    }

    /**
     *
     * @param function
     */
    public void setFunction(Function function) {
        this.function = function;
    }

    /**
     *
     * @param hc
     * @throws EMPIException
     */
    @Override
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {
        this.name = hc.getString(NAME);
        this.className = hc.getString(CLASS_NAME);
        logger.info("... className = " + this.className);

        // TBD: Parse Function parameters ...

        // Get an instance of the function and set the configuration.
        this.function = (Function) ConfigHelper.loadClass(this.className);
        this.function.setFunctionConfig(this);
    }
}
