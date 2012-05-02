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

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.mu.validation.DeleteDocumentSetCommandValidator;
import com.vangent.hieos.services.xds.registry.mu.validation.MetadataUpdateCommandValidator;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 * 
 * @author Bernie Thuman
 */
public class DeleteDocumentSetCommand extends MetadataUpdateCommand {

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public DeleteDocumentSetCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        super(metadata, metadataUpdateContext);
    }

    /**
     * 
     * @return
     */
    @Override
    protected MetadataUpdateCommandValidator getCommandValidator() {
        return new DeleteDocumentSetCommandValidator(this);
    }

    /**
     *
     * @param validator
     * @return
     * @throws XdsException
     */
    @Override
    protected boolean execute(MetadataUpdateCommandValidator validator) throws XdsException {
        Metadata submittedMetadata = this.getSubmittedMetadata();
        // Get list of object references.
        List<String> objectRefIds = submittedMetadata.getObjectRefIds();
        this.deleteRegistryObjects(objectRefIds);
        return true;
    }

    /**
     * 
     * @param objectRefIds
     * @throws XdsException
     */
    private OMElement deleteRegistryObjects(List<String> objectRefIds) throws XdsException {
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        // Submit RemoveObjectsRequest to registry.
        return backendRegistry.submitRemoveObjectsRequest(objectRefIds);
    }
}
