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
package com.vangent.hieos.empi.function;

import com.vangent.hieos.empi.config.FunctionConfig;

/**
 *
 * @author Bernie Thuman
 */
public class Function {

    private FunctionConfig functionConfig = null;

    /**
     *
     */
    public Function() {
    }

    /**
     *
     * @param functionConfig
     */
    public Function(FunctionConfig functionConfig) {
        this.functionConfig = functionConfig;
    }

    /**
     *
     * @return
     */
    public FunctionConfig getFunctionConfig() {
        return functionConfig;
    }

    /**
     * 
     * @param functionConfig
     */
    public void setFunctionConfig(FunctionConfig functionConfig) {
        this.functionConfig = functionConfig;
    }
}
