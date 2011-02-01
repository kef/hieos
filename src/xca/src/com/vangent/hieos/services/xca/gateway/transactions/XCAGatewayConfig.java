/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xca.gateway.transactions;

import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 *
 * @author Bernie Thuman
 */
public class XCAGatewayConfig {

    private XConfigActor config;
    private String patientId;

    /**
     * 
     * @param config
     */
    public XCAGatewayConfig(XConfigActor config) {
        this.config = config;
    }

    /**
     * 
     * @return
     */
    public XConfigActor getConfig()
    {
        return this.config;
    }

    /**
     *
     * @return
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     *
     * @param patientId
     */
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    /**
     * 
     * @return
     */
    public String getHomeCommunityId() {
        return config.getUniqueId();
    }
}
