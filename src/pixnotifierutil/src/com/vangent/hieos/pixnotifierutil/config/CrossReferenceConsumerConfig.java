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
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 *
 * @author Bernie Thuman
 */
public class CrossReferenceConsumerConfig implements ConfigItem {

    private static String NAME = "name";
    private static String ENABLED = "enabled";
    private static String DEVICE_ID = "device-id";
    private static String DEVICE_NAME = "device-name";
    private static String IDENTIFIER_DOMAINS = "identifier-domains.identifier-domain";
    private static String UNIVERSAL_ID = "universal-id";
    private static String UNIVERSAL_ID_TYPE = "universal-id-type";
    private static String NAMESPACE_ID = "namespace-id";
    private String name;
    private boolean enabled;
    private String deviceId;
    private String deviceName;
    private List<SubjectIdentifierDomain> identifierDomains = new ArrayList<SubjectIdentifierDomain>();
    private XConfigActor configActor = null;

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
    public String getDeviceId() {
        return deviceId;
    }

    /**
     *
     * @return
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     *
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     *
     * @return
     */
    public List<SubjectIdentifierDomain> getIdentifierDomains() {
        return identifierDomains;
    }

    /**
     * 
     * @return
     */
    public XConfigActor getConfigActor() {
        return configActor;
    }

    /**
     * 
     * @param hc
     * @param pixNotifierConfig
     * @throws PIXNotifierUtilException
     */
    @Override
    public void load(HierarchicalConfiguration hc, PIXNotifierConfig pixNotifierConfig) throws PIXNotifierUtilException {
        this.enabled = hc.getBoolean(ENABLED, Boolean.FALSE);
        this.name = hc.getString(NAME);
        this.deviceId = hc.getString(DEVICE_ID);
        this.deviceName = hc.getString(DEVICE_NAME);
        List idDomains = hc.configurationsAt(IDENTIFIER_DOMAINS);
        for (Iterator it = idDomains.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcIdentifierDomain = (HierarchicalConfiguration) it.next();
            String universalId = hcIdentifierDomain.getString(UNIVERSAL_ID);
            String universalIdType = hcIdentifierDomain.getString(UNIVERSAL_ID_TYPE);
            String namespaceId = hcIdentifierDomain.getString(NAMESPACE_ID);
            SubjectIdentifierDomain identifierDomain = new SubjectIdentifierDomain();
            identifierDomain.setUniversalId(universalId);
            identifierDomain.setUniversalIdType(universalIdType);
            identifierDomain.setNamespaceId(namespaceId);
            this.identifierDomains.add(identifierDomain);
        }
        // Set XConfigActor given "deviceId".
        Map<String, XConfigActor> crossReferenceConsumerConfigActorMap = pixNotifierConfig.getCrossReferenceConsumerConfigActorMap();
        configActor = crossReferenceConsumerConfigActorMap.get(this.deviceId);
    }
}
