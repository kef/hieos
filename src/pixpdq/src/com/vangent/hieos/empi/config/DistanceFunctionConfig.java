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

import com.vangent.hieos.empi.distance.DistanceFunction;
import com.vangent.hieos.empi.exception.EMPIException;

/**
 *
 * @author Bernie Thuman
 */
public class DistanceFunctionConfig extends FunctionConfig {

    /**
     *
     * @return
     */
    public DistanceFunction getDistanceFunction() {
        return (DistanceFunction) this.getFunction();
    }

    /**
     *
     * @return
     * @throws EMPIException
     */
    @Override
    public FunctionConfig copyNoParameters() throws EMPIException {
        DistanceFunctionConfig copyFunctionConfig = new DistanceFunctionConfig();
        super.copyNoParameters(copyFunctionConfig);
        return copyFunctionConfig;
    }
}
