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

import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.mu.validation.UpdateDocumentEntryMetadataCommandValidator;
import com.vangent.hieos.services.xds.registry.mu.validation.UpdateDocumentSetCommandValidator;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
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
    protected UpdateDocumentSetCommandValidator getCommandValidator() {
        return new UpdateDocumentEntryMetadataCommandValidator(this);
    }

    /**
     *
     * @param targetPatientId
     * @param newDocumentEntryId
     * @param currentDocumentEntryId
     * @throws XdsException
     */
    @Override
    protected void handleAssociationPropagation(String targetPatientId, String newDocumentEntryId, String currentDocumentEntryId) throws XdsException {
        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        UpdateDocumentSetCommandValidator validator = this.getCommandValidator();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // Prepare for queries.
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                metadataUpdateContext.getRegistryResponse(), logMessage,
                metadataUpdateContext.getBackendRegistry());
        muSQ.setReturnLeafClass(false);

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
            if (sourceId.equals(currentDocumentEntryId)) {
                // If source is a document, then the target is a document.

                // Now make sure that we do not violate patient id constraints.
                validator.validateDocumentPatientId(targetId, targetPatientId);

                // Create association between new document version and target document.
                OMElement newAssoc = newAssocMetadata.makeAssociation(assocType, newDocumentEntryId, targetId);
                newAssocMetadata.addAssociation(newAssoc);
            } else {
                // Target is the current document.  See about the source.
                if (!assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
                    // If the association type is not a has member, then the source must be a document.
                    // For optimization reasons, assuming a document (not verifying here).

                    // Now make sure that we do not violate patient id constraints.
                    validator.validateDocumentPatientId(sourceId, targetPatientId);

                    // Create association between source document and new document version.
                    OMElement newAssoc = newAssocMetadata.makeAssociation(assocType, sourceId, newDocumentEntryId);
                    newAssocMetadata.addAssociation(newAssoc);
                } else {
                    // Make sure that the source is a folder (and not a submission set).
                    backendRegistry.setReason("Assoc Propagation - Validate Source is Folder");
                    OMElement folderQueryResult = muSQ.getFolderByUUID(sourceId);
                    backendRegistry.setReason("");
                    Metadata folderMetadata = MetadataParser.parseNonSubmission(folderQueryResult);
                    if (!folderMetadata.getObjectRefIds().isEmpty()) {

                        // Now make sure that we do not violate patient id constraints.
                        validator.validateFolderPatientId(sourceId, targetPatientId);

                        // Create association between source folder entry and new document version.
                        OMElement newAssoc = newAssocMetadata.makeAssociation(assocType, sourceId, newDocumentEntryId);
                        newAssocMetadata.addAssociation(newAssoc);

                        // Will need to deprecate the prior Folder->Document association.
                        deprecateAssocIds.add(assocId);
                    }
                }
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
