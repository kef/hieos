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
// FIXME: Do we need this class?
package com.vangent.hieos.services.xds.registry.storedquery;

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.xutil.exception.XDSRegistryOutOfResourcesException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

/**
 *
 * @author Bernie Thuman
 */
public class MetadataUpdateStoredQuerySupport extends StoredQuery {

    @Override
    public Metadata runInternal() throws XdsException, XDSRegistryOutOfResourcesException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param response
     * @param logMessage
     * @param backendRegistry
     */
    public MetadataUpdateStoredQuerySupport(ErrorLogger response, XLogMessage logMessage, BackendRegistry backendRegistry) {
        super(response, logMessage, backendRegistry);
    }

    /**
     * 
     * @param reason
     */
    public void setReason(String reason) {
        this.getBackendRegistry().setReason(reason);
    }
}
