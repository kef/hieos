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
package com.vangent.hieos.services.xds.registry.mu.command;

import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;

/**
 *
 * @author Bernie Thuman
 */
public abstract class MetadataUpdateController {

    // Allow subclasses to access directly (normally don't like to do this).
    /**
     *
     */
    protected MetadataUpdateContext metadataUpdateContext;
    /**
     *
     */
    protected Metadata submittedMetadata;

    /**
     *
     * @param metadataUpdateContext
     * @param submittedMetadata
     */
    public MetadataUpdateController(MetadataUpdateContext metadataUpdateContext, Metadata submittedMetadata) {
        this.metadataUpdateContext = metadataUpdateContext;
        this.submittedMetadata = submittedMetadata;
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    public boolean run() throws XdsException {
        this.enforcePolicy();
        return this.update();
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    protected abstract boolean update() throws XdsException;

    /**
     *
     * @return
     * @throws XdsException
     */
    protected abstract boolean enforcePolicy() throws XdsException;
}
