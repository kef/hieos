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
import com.vangent.hieos.empi.exception.EMPIException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public abstract class FunctionConfig implements ConfigItem {

    private final static Logger logger = Logger.getLogger(FunctionConfig.class);
    private static String NAME = "name";
    private static String CLASS_NAME = "class";
    private static String FUNCTION_PARAMETERS = "parameters.parameter";
    private static String PARAMETER_NAME = "name";
    private static String PARAMETER_VALUE = "value";
    private String name;
    private String className;
    private Function function;
    private Map<String, String> parameters = new HashMap<String, String>();

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
     * @return
     */
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    /**
     *
     * @param name
     * @return
     */
    public String getParameter(String name) {
        return parameters.get(name.toLowerCase());
    }

    /**
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public boolean getParameterAsBoolean(String name, boolean defaultValue) {
        String value = this.getParameter(name);
        if (value != null) {
            return value.equalsIgnoreCase("true") ? true : false;
        }
        return defaultValue;
    }

    /**
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public int getParameterAsInteger(String name, int defaultValue) {
        String value = this.getParameter(name);
        if (value != null) {
            return Integer.valueOf(value);
        }
        return defaultValue;
    }

    /**
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public double getParameterAsDouble(String name, double defaultValue) {
        String value = this.getParameter(name);
        if (value != null) {
            return Double.valueOf(value);
        }
        return defaultValue;
    }

    /**
     * 
     * @param hc
     * @param empiConfig
     * @throws EMPIException
     */
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {
        this.name = hc.getString(NAME);
        this.className = hc.getString(CLASS_NAME);
        logger.info("... className = " + this.className);

        // Get an instance of the function and set the configuration.
        this.function = (Function) ConfigHelper.loadClassInstance(this.className);
        this.function.setFunctionConfig(this);

        // Load function parameters.
        this.loadFunctionParameters(hc);
    }

    /**
     * 
     * @param hc
     */
    public void loadFunctionParameters(HierarchicalConfiguration hc) {
        // Load function parameters.
        List functionParameters = hc.configurationsAt(FUNCTION_PARAMETERS);
        if (functionParameters != null) {
            for (Iterator it = functionParameters.iterator(); it.hasNext();) {
                HierarchicalConfiguration hcParameter = (HierarchicalConfiguration) it.next();
                String parameterName = hcParameter.getString(PARAMETER_NAME);
                String parameterValue = hcParameter.getString(PARAMETER_VALUE);
                this.parameters.put(parameterName.toLowerCase(), parameterValue);
            }
        }
    }

    /**
     *
     * @return
     * @throws EMPIException
     */
    abstract public FunctionConfig copyNoParameters() throws EMPIException;

    /**
     *
     * @param copyFunctionConfig
     * @throws EMPIException
     */
    protected void copyNoParameters(FunctionConfig copyFunctionConfig) throws EMPIException {
        copyFunctionConfig.name = name;
        copyFunctionConfig.className = className;
        copyFunctionConfig.function = (Function) ConfigHelper.loadClassInstance(className);
        copyFunctionConfig.function.setFunctionConfig(copyFunctionConfig);

        // Copy parameters (must be easier way).
        /*
        copyFunctionConfig.parameters = new HashMap<String, String>();
        Set<String> parameterNames = this.parameters.keySet();
        for (String parameterName : parameterNames) {
        String parameterValue = this.parameters.get(parameterName);
        copyFunctionConfig.parameters.put(parameterName, parameterValue);
        }*/
    }

    /**
     * 
     * @param hcFunction
     * @return
     * @throws EMPIException
     */
    public FunctionConfig loadFunctionConfig(HierarchicalConfiguration hcFunction) throws EMPIException {
        FunctionConfig functionConfig = this;  // Default is to return self.
        
        // See if the function has parameters.
        List functionParameters = hcFunction.configurationsAt(FUNCTION_PARAMETERS);
        if ((functionParameters != null) && !functionParameters.isEmpty()) {
            // Create a copy of the function and replace parameters.
            functionConfig = this.copyNoParameters();
            functionConfig.loadFunctionParameters(hcFunction);
        }
        return functionConfig;
    }
}
