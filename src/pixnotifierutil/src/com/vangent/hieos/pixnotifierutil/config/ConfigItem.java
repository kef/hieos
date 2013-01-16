/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.pixnotifierutil.config;

import com.vangent.hieos.pixnotifierutil.exception.PIXNotifierUtilException;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 *
 * @author Bernie Thuman
 */
public interface ConfigItem {

    /**
     * 
     * @param hc
     * @param pixNotifierConfig
     * @throws PIXNotifierUtilException
     */
    public void load(HierarchicalConfiguration hc, PIXNotifierConfig pixNotifierConfig) throws PIXNotifierUtilException;
}
