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

    /**
     * 
     * @param submittedMetadata
     * @param metadataUpdateContext
     * @param metadataUpdateCommandValidator
     */
    public DeleteDocumentSetCommand(Metadata submittedMetadata, MetadataUpdateContext metadataUpdateContext,
            MetadataUpdateCommandValidator metadataUpdateCommandValidator) {
        super(submittedMetadata, metadataUpdateContext, metadataUpdateCommandValidator);
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    @Override
    protected boolean executeUpdate() throws XdsException {
        // Get list of object references (to delete) supplied by initiator.
        Metadata submittedMetadata = this.getSubmittedMetadata();
        List<String> objectRefIds = submittedMetadata.getObjectRefIds();

        // Get associations (to delete) connected to the registry objects targeted for deletion.
        List<String> assocIdsToDelete = this.getAssocsToDelete(objectRefIds);
        if (!assocIdsToDelete.isEmpty()) {
            // Add to associations to list of objects to delete (but only if not already supplied by initiator).
            for (String assocIdToDelete : assocIdsToDelete) {
                if (!objectRefIds.contains(assocIdToDelete)) {
                    objectRefIds.add(assocIdToDelete);
                }
            }
        }

        // Delete object's from registry.
        OMElement result = this.getBackendRegistry().submitRemoveObjectsRequest(objectRefIds);
        // FIXME: result?
        return true;
    }

    /**
     *
     * @param cmd
     * @param objectRefIds
     * @return
     * @throws XdsException
     */
    private List<String> getAssocsToDelete(List<String> objectRefIds) throws XdsException {
        List<String> assocIdsToDelete = new ArrayList<String>();

        // Placed in block to avoid redefining variable names.
        {
            // Get all associations to the registry objects.
            Metadata assocMetadata = this.getAssocs(objectRefIds,
                    null /* status */,
                    null /* assocType */,
                    false /* leafClass */,
                    "Get Registry Object Associations To Delete");
            List<String> assocIds = assocMetadata.getObjectRefIds();
            if (!assocIds.isEmpty()) {
                // Add them for deletion.
                assocIdsToDelete.addAll(assocIds);
            }
        }

        // Now find associations that link to the associations.
        if (!assocIdsToDelete.isEmpty()) {
            Metadata assocMetadata = this.getAssocs(assocIdsToDelete,
                    null /* status */,
                    null /* assocTypes */,
                    false /* leafClass */,
                    "Get Assoc Associations To Delete");
            List<String> assocIds = assocMetadata.getObjectRefIds();
            if (!assocIds.isEmpty()) {
                // Add them for deletion.
                assocIdsToDelete.addAll(assocIds);
            }
        }

        // Note: This may result in duplicate assocIds, but OK - will be removed later.
        return assocIdsToDelete;
    }
}
