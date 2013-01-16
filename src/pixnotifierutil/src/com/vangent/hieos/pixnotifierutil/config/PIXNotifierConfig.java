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
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIXNotifierConfig {

    private final static Logger logger = Logger.getLogger(PIXNotifierConfig.class);
    private static String CONFIG_FILE_NAME = "PIXNotifierConfig.xml";
    private static String CROSS_REFERENCE_CONSUMER_CONFIGS = "cross-reference-consumers.cross-reference-consumer";
    private static PIXNotifierConfig _instance = null;
    private Map<String, XConfigActor> crossReferenceConsumerConfigActorMap = new HashMap<String, XConfigActor>();
    private List<CrossReferenceConsumerConfig> crossReferenceConsumerConfigs = new ArrayList<CrossReferenceConsumerConfig>();

    /**
     *
     */
    private PIXNotifierConfig() {
        // Do not allow.
    }

    /**
     *
     * @return
     * @throws PIXNotifierUtilException
     */
    static public synchronized PIXNotifierConfig getInstance() throws PIXNotifierUtilException {
        if (_instance == null) {
            _instance = new PIXNotifierConfig();
            _instance.loadConfiguration();
        }
        return _instance;
    }

    /**
     *
     * @return
     */
    public List<CrossReferenceConsumerConfig> getCrossReferenceConsumerConfigs() {
        return crossReferenceConsumerConfigs;
    }

    /**
     * 
     * @return
     */
    public Map<String, XConfigActor> getCrossReferenceConsumerConfigActorMap() {
        return crossReferenceConsumerConfigActorMap;
    }

    /**
     * 
     * @throws PIXNotifierUtilException
     */
    private void loadConfiguration() throws PIXNotifierUtilException {
        String empiConfigDir = XConfig.getConfigLocation(XConfig.ConfigItem.EMPI_DIR);
        String configLocation = empiConfigDir + "/" + PIXNotifierConfig.CONFIG_FILE_NAME;
        try {
            XMLConfiguration xmlConfig = new XMLConfiguration(configLocation);

            // Load cross reference consumers.
            this.loadCrossReferenceConsumers(xmlConfig);

        } catch (ConfigurationException ex) {
            throw new PIXNotifierUtilException(
                    "PIXNotifierConfig: Could not load configuration from " + configLocation + " " + ex.getMessage());
        }
    }

    /**
     * 
     * @param hc
     * @throws PIXNotifierUtilException
     */
    private void loadCrossReferenceConsumers(HierarchicalConfiguration hc) throws PIXNotifierUtilException {
        // Load up XConfig configuration items.
        this.loadCrossReferenceConsumerConfigActorMap();

        // Get cross-reference consumer configurations.
        List crossReferenceConsumers = hc.configurationsAt(CROSS_REFERENCE_CONSUMER_CONFIGS);
        for (Iterator it = crossReferenceConsumers.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcCrossReferenceConsumer = (HierarchicalConfiguration) it.next();
            CrossReferenceConsumerConfig crossReferenceConsumerConfig = new CrossReferenceConsumerConfig();
            crossReferenceConsumerConfig.load(hcCrossReferenceConsumer, this);
            crossReferenceConsumerConfigs.add(crossReferenceConsumerConfig);
        }
    }

    /**
     *
     * @throws PIXNotifierUtilException
     */
    private void loadCrossReferenceConsumerConfigActorMap() throws PIXNotifierUtilException {
        XConfig xConfig;
        try {
            xConfig = XConfig.getInstance();
        } catch (XConfigException ex) {
            throw new PIXNotifierUtilException("Unable to load XConfig", ex);
        }
        List<XConfigObject> crossReferenceConsumerConfigObjects = xConfig.getXConfigObjectsOfType("PIXConsumerType");
        for (XConfigObject configObject : crossReferenceConsumerConfigObjects) {
            String deviceId = configObject.getProperty("DeviceId");
            if (deviceId != null) {
                crossReferenceConsumerConfigActorMap.put(deviceId, (XConfigActor) configObject);
            } else {
                logger.error("DeviceId not found in XConfig for PIX Consumer = " + configObject.getName());
            }
        }
    }
}
