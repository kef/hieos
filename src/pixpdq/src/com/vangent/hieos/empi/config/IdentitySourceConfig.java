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
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 *
 * @author Bernie Thuman
 */
public class IdentitySourceConfig implements ConfigItem {

    private static String NAME = "name";
    private static String DEVICE_ID = "device-id";
    private static String DEVICE_NAME = "device-name";
    private static String IDENTIFIER_DOMAINS = "identifier-domains.identifier-domain";
    private static String UNIVERSAL_ID = "universal-id";
    private static String UNIVERSAL_ID_TYPE = "universal-id-type";
    private static String NAMESPACE_ID = "namespace-id";
    private String name;
    private String deviceId;
    private String deviceName;
    private List<SubjectIdentifierDomain> identifierDomains = new ArrayList<SubjectIdentifierDomain>();

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
    public List<SubjectIdentifierDomain> getIdentifierDomains() {
        return identifierDomains;
    }

    /**
     * 
     * @param hc
     * @param empiConfig
     * @throws EMPIException
     */
    @Override
    public void load(HierarchicalConfiguration hc, EMPIConfig empiConfig) throws EMPIException {
        this.name = hc.getString(NAME);
        this.deviceId = hc.getString(DEVICE_ID);
        this.deviceName = hc.getString(DEVICE_NAME);
        List identifierDomains = hc.configurationsAt(IDENTIFIER_DOMAINS);
        for (Iterator it = identifierDomains.iterator(); it.hasNext();) {
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
    }
}
