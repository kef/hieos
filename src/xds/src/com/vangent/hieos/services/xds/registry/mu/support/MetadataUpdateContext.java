/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xds.registry.mu;

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

/**
 *
 * @author Bernie Thuman
 */
public class MetadataUpdateContext {

    private XLogMessage logMessage;
    private BackendRegistry backendRegistry;
    private RegistryResponse registryResponse;
    private XConfigActor configActor;

    /**
     *
     * @return
     */
    public BackendRegistry getBackendRegistry() {
        return backendRegistry;
    }

    /**
     *
     * @param backendRegistry
     */
    public void setBackendRegistry(BackendRegistry backendRegistry) {
        this.backendRegistry = backendRegistry;
    }

    /**
     *
     * @return
     */
    public XLogMessage getLogMessage() {
        return logMessage;
    }

    /**
     *
     * @param logMessage
     */
    public void setLogMessage(XLogMessage logMessage) {
        this.logMessage = logMessage;
    }

    /**
     *
     * @return
     */
    public RegistryResponse getRegistryResponse() {
        return registryResponse;
    }

    /**
     * 
     * @param registryResponse
     */
    public void setRegistryResponse(RegistryResponse registryResponse) {
        this.registryResponse = registryResponse;
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
     * @param configActor
     */
    public void setConfigActor(XConfigActor configActor) {
        this.configActor = configActor;
    }
}
