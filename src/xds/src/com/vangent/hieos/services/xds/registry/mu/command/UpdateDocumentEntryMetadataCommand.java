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
import com.vangent.hieos.services.xds.registry.mu.validation.UpdateDocumentEntryMetadataCommandValidator;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 * 
 * @author Bernie Thuman
 */
public class UpdateDocumentEntryMetadataCommand extends UpdateRegistryObjectMetadataCommand {

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public UpdateDocumentEntryMetadataCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        super(metadata, metadataUpdateContext);
    }

    /**
     * 
     * @return
     */
    @Override
    protected MetadataUpdateCommandValidator getCommandValidator() {
        return new UpdateDocumentEntryMetadataCommandValidator(this);
    }

    /**
     *
     * @param submittedPatientId
     * @param newDocumentEntryId
     * @param currentDocumentEntryId
     * @throws XdsException
     */
    @Override
    protected void handleAssociationPropagation(String submittedPatientId, String newDocumentEntryId, String currentDocumentEntryId) throws XdsException {
        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        MetadataUpdateCommandValidator validator = this.getCommandValidator();
        //XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // Rules:
        //  Look for non-deprecated HasMember associations linking the existing DocumentEntry to a Folder.
        //  Scan for existing non-deprecated HasMember associations (in approved status).
        //  When found, a new HasMember association is generated linking the new DocumentEntry to the same Folder.
        //
        //  Look for non-deprecated relationship associations linked to the existing DocumentEntry.
        //  When found, these associations are replicated referencing the new DocumentEntry instead of the
        //  existing DocumentEntry.

        // Get all approved associations.
        Metadata assocMetadata = this.getApprovedAssocs(currentDocumentEntryId);

        // Create new metadata instance.
        Metadata newAssocMetadata = new Metadata();

        // Keep track of which associations to deprecate.
        List<String> deprecateAssocIds = new ArrayList<String>();

        // Now, go through each association and create a new one.
        List<OMElement> assocs = assocMetadata.getAssociations();
        for (OMElement assoc : assocs) {
            String assocId = assocMetadata.getId(assoc);
            String assocType = assocMetadata.getAssocType(assoc);
            String sourceId = assocMetadata.getSourceObject(assoc);
            String targetId = assocMetadata.getAssocTarget(assoc);
            OMElement newAssoc = null;
            if (sourceId.equals(currentDocumentEntryId)) {
                // If source is a document, then the target is a document.
                // Now make sure that we do not violate patient id constraints.
                validator.validateDocumentPatientId(targetId, submittedPatientId);
                // Create association between new document version and target document.
                newAssoc = newAssocMetadata.makeAssociation(assocType, newDocumentEntryId, targetId);
                newAssocMetadata.addAssociation(newAssoc);
            } else {
                // Target is the current document.  See about the source.
                if (!assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
                    // If the association type is not a has member, then the source must be a document.
                    // For optimization reasons, assuming a document (not verifying here).
                    // Now make sure that we do not violate patient id constraints.
                    validator.validateDocumentPatientId(sourceId, submittedPatientId);
                    // Create association between source document and new document version.
                    newAssoc = newAssocMetadata.makeAssociation(assocType, sourceId, newDocumentEntryId);
                    newAssocMetadata.addAssociation(newAssoc);
                } else {
                    // Make sure that the source is a folder (and not a submission set).
                    // Now make sure that we do not violate patient id constraints.
                    boolean foundFolder = validator.validateFolderPatientId(sourceId, submittedPatientId);
                    if (foundFolder) {
                        // Create association between source folder entry and new document version.
                        newAssoc = newAssocMetadata.makeAssociation(assocType, sourceId, newDocumentEntryId);
                        newAssocMetadata.addAssociation(newAssoc);
                    }
                }
            }
            if (newAssoc != null) {
                deprecateAssocIds.add(assocId);
            }
        }

        // Submit new associations to registry.
        //logMessage.addOtherParam("Association Propagation Submission", newAssocMetadata);

        // FIXME: MetadataTypes.METADATA_TYPE_Rb?
        //RegistryUtility.schema_validate_local(submitObjectsRequest, MetadataTypes.METADATA_TYPE_Rb);
        if (!newAssocMetadata.getAssociations().isEmpty()) {
            backendRegistry.setReason("Association Propagation Submission");
            OMElement result = backendRegistry.submit(newAssocMetadata);
        }

        // Now, run deprecations.
        if (!deprecateAssocIds.isEmpty()) {
            backendRegistry.submitDeprecateObjectsRequest(deprecateAssocIds);
        }
    }
}
