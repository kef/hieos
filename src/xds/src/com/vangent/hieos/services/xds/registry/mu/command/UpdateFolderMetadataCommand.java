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
import com.vangent.hieos.services.xds.registry.mu.validation.MetadataUpdateCommandValidator;
import com.vangent.hieos.services.xds.registry.mu.validation.UpdateFolderMetadataCommandValidator;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateFolderMetadataCommand extends UpdateRegistryObjectMetadataCommand {

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public UpdateFolderMetadataCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        super(metadata, metadataUpdateContext);
    }

    /**
     * 
     * @return
     */
    @Override
    protected MetadataUpdateCommandValidator getCommandValidator() {
        return new UpdateFolderMetadataCommandValidator(this);
    }

    /**
     * 
     * @param targetPatientId
     * @param newFolderEntryId
     * @param currentFolderEntryId
     * @throws XdsException
     */
    @Override
    protected void handleAssociationPropagation(String targetPatientId, String newFolderEntryId, String currentFolderEntryId) throws XdsException {
        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        MetadataUpdateCommandValidator validator = this.getCommandValidator();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // Scan for existing non-deprecated HasMember associations (in approved status).
        Metadata assocMetadata = this.getApprovedHasMemberAssocs(currentFolderEntryId);

        // Create new metadata instance.
        Metadata newAssocMetadata = new Metadata();

        // Now, go through each association and create a new one.
        List<OMElement> assocs = assocMetadata.getAssociations();
        for (OMElement assoc : assocs) {
            String sourceId = assocMetadata.getSourceObject(assoc);
            if (sourceId.equals(currentFolderEntryId)) {
                // source is the folder; target must be a document.
                String targetId = assocMetadata.getTargetObject(assoc);

                // Now make sure that we do not violate patient id constraints.
                validator.validateDocumentPatientId(targetId, targetPatientId);

                // Create association between new folder version and target document.
                OMElement newAssoc = newAssocMetadata.makeAssociation(MetadataSupport.xdsB_eb_assoc_type_has_member, newFolderEntryId, targetId);
                newAssocMetadata.addAssociation(newAssoc);
            }
        }

        // FIXME: MetadataTypes.METADATA_TYPE_Rb?
        //RegistryUtility.schema_validate_local(submitObjectsRequest, MetadataTypes.METADATA_TYPE_Rb);
        if (!newAssocMetadata.getAssociations().isEmpty()) {
            backendRegistry.setReason("Association Propagation Submission");
            OMElement result = backendRegistry.submit(newAssocMetadata);
        }
    }
}
