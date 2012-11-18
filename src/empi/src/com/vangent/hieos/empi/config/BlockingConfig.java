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
public class BlockingConfig implements ConfigItem {

    private static String BLOCKING_PASSES = "blocking-pass";
    private List<BlockingPassConfig> blockingPassConfigs = new ArrayList<BlockingPassConfig>();

    /**
     *
     * @return
     */
    public List<BlockingPassConfig> getBlockingPassConfigs() {
        return blockingPassConfigs;
    }

    /**
     *
     * @param hc
     * @param empiConfig
     * @throws EMPIException
     */
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {

        // Get the blocking pass configuration.
        List blockingPasses = hc.configurationsAt(BLOCKING_PASSES);
        for (Iterator it = blockingPasses.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcBlockingPass = (HierarchicalConfiguration) it.next();
            BlockingPassConfig blockingPassConfig = new BlockingPassConfig();
            blockingPassConfig.load(hcBlockingPass, empiConfig);
            blockingPassConfigs.add(blockingPassConfig);
        }
    }
}
