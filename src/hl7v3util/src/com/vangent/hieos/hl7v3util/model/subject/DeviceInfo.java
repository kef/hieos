/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v3util.model.subject;

import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 *
 * @author Bernie Thuman
 */
public class DeviceInfo {

    private String id;
    private String name;
    private String representedOrganizationId;
    private String telecom;

    public DeviceInfo() {
        this.id = null;
        this.name = null;
        this.representedOrganizationId = null;
        this.telecom = null;
    }

    /**
     * 
     * @param actorConfig
     */
    public DeviceInfo(XConfigActor actorConfig) {
        this.id = actorConfig.getProperty("DeviceId");
        this.name = actorConfig.getProperty("DeviceName");
        if (actorConfig.getType().endsWith("GatewayType")) {
            String homeCommunityId = actorConfig.getUniqueId();
            this.representedOrganizationId = homeCommunityId;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepresentedOrganizationId() {
        return representedOrganizationId;
    }

    public void setRepresentedOrganizationId(String representedOrganizationId) {
        this.representedOrganizationId = representedOrganizationId;
    }

    public String getTelecom() {
        return telecom;
    }

    public void setTelecom(String telecom) {
        this.telecom = telecom;
    }
}
