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
import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 * 
 * @author Bernie Thuman
 */
public class DeleteDocumentSetCommand extends MetadataUpdateCommand {

    private Metadata loadedMetadata;

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
     * @return
     */
    public Metadata getLoadedMetadata() {
        return loadedMetadata;
    }

    /**
     * 
     * @param loadedMetadata
     */
    public void setLoadedMetadata(Metadata loadedMetadata) {
        this.loadedMetadata = loadedMetadata;
    }

    /**
     *
     * @param validator
     * @return
     * @throws XdsException
     */
    @Override
    protected boolean execute(MetadataUpdateCommandValidator validator) throws XdsException {
        // Get list of object references (to delete) supplied by initiator.
        Metadata submittedMetadata = this.getSubmittedMetadata();
        List<String> objectRefIds = submittedMetadata.getObjectRefIds();

        // Get associations (to delete) connected to registry objects targeted for deletion.
        List<String> assocIdsToDelete = this.getAssocsToDelete(this, loadedMetadata);
        if (!assocIdsToDelete.isEmpty()) {
            // Add to associations to list of objects to delete (but only if not already supplied by initiator).
            for (String assocIdToDelete : assocIdsToDelete) {
                if (!objectRefIds.contains(assocIdToDelete)) {
                    objectRefIds.add(assocIdToDelete);
                }
            }
        }

        // Delete object's from registry.
        OMElement result = this.deleteRegistryObjects(objectRefIds);
        // FIXME: result?
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

    /**
     *
     * @param cmd
     * @param loadedMetadata
     * @return
     * @throws XdsException
     */
    private List<String> getAssocsToDelete(DeleteDocumentSetCommand cmd, Metadata loadedMetadata) throws XdsException {
        List<String> assocIdsToDelete = new ArrayList<String>();

        // Go through each document/folder targeted for deletion.
        List<String> registryObjectIdsToDelete = loadedMetadata.getExtrinsicObjectIds();
        List<String> folderIdsToDelete = loadedMetadata.getFolderIds();
        if (!folderIdsToDelete.isEmpty()) {
            registryObjectIdsToDelete.addAll(folderIdsToDelete);
        }

        // Placed in block to avoid redefining variable names.
        {
            // Get all associations to the registry objects.
            Metadata assocMetadata = cmd.getAssocs(registryObjectIdsToDelete, null /* status */, null /* assocType */, "Get Registry Object Associations To Delete");
            List<String> assocIds = assocMetadata.getAssociationIds();
            if (!assocIds.isEmpty()) {
                // Add them for deletion.
                assocIdsToDelete.addAll(assocIds);
            }
        }

        // Now find associations that link to the associations.
        if (!assocIdsToDelete.isEmpty()) {
            Metadata assocMetadata = cmd.getAssocs(assocIdsToDelete, null /* status */, null /* assocTypes */, "Get Assoc Associations To Delete");
            List<String> assocIds = assocMetadata.getAssociationIds();
            if (!assocIds.isEmpty()) {
                // Add them for deletion.
                assocIdsToDelete.addAll(assocIds);
            }
        }

        // Note: This may result in duplicate assocIds, but OK - will be removed later.
        return assocIdsToDelete;
    }
}
