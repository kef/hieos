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
package com.vangent.hieos.subjectmodel;

import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 *
 * @author Bernie Thuman
 */
public class DeviceInfo implements Cloneable {

    private String id;
    private String name;
    private String representedOrganizationId;
    private String telecom;

    /**
     *
     */
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

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

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
    public String getRepresentedOrganizationId() {
        return representedOrganizationId;
    }

    /**
     *
     * @param representedOrganizationId
     */
    public void setRepresentedOrganizationId(String representedOrganizationId) {
        this.representedOrganizationId = representedOrganizationId;
    }

    /**
     *
     * @return
     */
    public String getTelecom() {
        return telecom;
    }

    /**
     *
     * @param telecom
     */
    public void setTelecom(String telecom) {
        this.telecom = telecom;
    }

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
