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
package com.vangent.hieos.services.xds.registry.mu.validation;

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.services.xds.registry.mu.command.MetadataUpdateCommand;
import com.vangent.hieos.services.xds.registry.mu.command.UpdateStatusCommand;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateHelper;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateStatusCommandValidator extends MetadataUpdateCommandValidator {

    /**
     * 
     * @param metadataUpdateCommand
     */
    public UpdateStatusCommandValidator(MetadataUpdateCommand metadataUpdateCommand) {
        super(metadataUpdateCommand);
    }

    /**
     * 
     * @throws XdsException
     */
    public boolean validate() throws XdsException {
        UpdateStatusCommand cmd = (UpdateStatusCommand) this.getMetadataUpdateCommand();
        boolean validationSuccess = true;

        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = cmd.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        RegistryResponse registryResponse = metadataUpdateContext.getRegistryResponse();

        if (logMessage.isLogEnabled()) {
            // Log parameters.
            logMessage.addOtherParam("Input Parameters",
                    "NewStatus = " + cmd.getNewStatus()
                    + ", OriginalStatus = " + cmd.getOriginalStatus()
                    + ", Target Registry Object Id = " + cmd.getTargetObjectId());
        }

        // Run further validations.
        // Validate the new status is a valid entry.
        String newStatus = cmd.getNewStatus();
        if (!(newStatus.equals(MetadataSupport.status_type_approved)
                || newStatus.equals(MetadataSupport.status_type_deprecated))) {
            throw new XdsException(newStatus + " is not a valid status for this registry");
        }

        // Make sure target object id is in UUID format.
        String targetObjectId = cmd.getTargetObjectId();
        if (!MetadataUpdateHelper.isUUID(targetObjectId)) {
            // FIXME: Use proper exception?
            throw new XdsException(targetObjectId + " is not in UUID format");
        }

        // Prepare to issue registry queries.
        MetadataUpdateStoredQuerySupport muSQ = metadataUpdateContext.getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);  // FIXME: Optimize to only get root and not composed elements.

        // Load metadata (based on target object id).
        Metadata loadedMetadata = this.loadMetadata(muSQ, targetObjectId);
        cmd.setLoadedMetadata(loadedMetadata);  // For usage by command.
        if (loadedMetadata == null) {
            throw new XdsException("Could not find target object = " + targetObjectId);
        }

        // Now, run validations.
        this.validateStatusUpdateOnRegistryObject(cmd, loadedMetadata, muSQ, targetObjectId);
        return validationSuccess;
    }

    /**
     * 
     * @param muSQ
     * @param targetObjectId
     * @return
     * @throws XdsException
     */
    private Metadata loadMetadata(
            MetadataUpdateStoredQuerySupport muSQ, String targetObjectId) throws XdsException {

        // Try to load document.
        OMElement docQueryResult = muSQ.getDocumentByUUID(targetObjectId);
        Metadata docMetadata = MetadataParser.parseNonSubmission(docQueryResult);
        if (!docMetadata.getExtrinsicObjects().isEmpty()) {
            return docMetadata;
        }
        // Fall through: Try to load folder.
        OMElement folderQueryResult = muSQ.getFolderByUUID(targetObjectId);
        Metadata folderMetadata = MetadataParser.parseNonSubmission(folderQueryResult);
        if (!folderMetadata.getFolders().isEmpty()) {
            return folderMetadata;
        }

        // Fall through: Try to load association.
        OMElement assocQueryResult = muSQ.getAssociationByUUID(targetObjectId);
        Metadata assocMetadata = MetadataParser.parseNonSubmission(assocQueryResult);
        if (!assocMetadata.getAssociations().isEmpty()) {
            return assocMetadata;
        }
        return null;  // No registry objects found.
    }

    /**
     * 
     * @param cmd
     * @param loadedMetadata
     * @param muSQ
     * @param targetObjectId
     * @throws XdsException
     */
    private void validateStatusUpdateOnRegistryObject(UpdateStatusCommand cmd,
            Metadata loadedMetadata,
            MetadataUpdateStoredQuerySupport muSQ, String targetObjectId) throws XdsException {
        this.validateStatusUpdateOnDocumentEntry(cmd, loadedMetadata, muSQ, targetObjectId);
        this.validateStatusUpdateOnFolder(cmd, loadedMetadata, muSQ, targetObjectId);
        this.validateStatusUpdateOnAssociation(cmd, loadedMetadata, muSQ, targetObjectId);
    }

    /**
     *
     * @param cmd
     * @param loadedMetadata
     * @param muSQ
     * @param targetObjectId
     * @throws XdsException
     */
    private void validateStatusUpdateOnDocumentEntry(UpdateStatusCommand cmd,
            Metadata loadedMetadata,
            MetadataUpdateStoredQuerySupport muSQ, String targetObjectId) throws XdsException {
        if (!loadedMetadata.getExtrinsicObjects().isEmpty()) {
            OMElement currentDocument = loadedMetadata.getExtrinsicObject(0);
            this.validateStatusConstraints(cmd, "DocumentEntry", loadedMetadata, currentDocument);

            // Now, make sure that we don't create a situation where there is more than one
            // approved document for the same LID.
            if (cmd.getNewStatus().equals(MetadataSupport.status_type_approved)) {
                // We are attempting to change the status to approved.  Enforce rules.
                String lid = currentDocument.getAttributeValue(MetadataSupport.lid_qname);

                // Look up by LID.
                OMElement lidQueryResult = muSQ.getDocumentsByLID(lid, null /* status */, null /* version */);
                Metadata metadataVersions = MetadataParser.parseNonSubmission(lidQueryResult);
                List<OMElement> versionedDocuments = metadataVersions.getExtrinsicObjects();

                // Make sure we are not violating any version constraints.
                this.validateVersionConstraints(targetObjectId, metadataVersions, currentDocument, versionedDocuments);

                // Get all approved associations.
                Metadata assocMetadata = cmd.getApprovedAssocs(cmd.getTargetObjectId());
                List<OMElement> assocs = assocMetadata.getAssociations();
                for (OMElement assoc : assocs) {
                    String assocType = assocMetadata.getAssocType(assoc);
                    String sourceId = assocMetadata.getSourceObject(assoc);
                    String targetId = assocMetadata.getAssocTarget(assoc);
                    String targetPatientId = loadedMetadata.getPatientId(currentDocument);
                    if (sourceId.equals(targetObjectId)) {
                        // If source is a document, then the target is a document.
                        // Now make sure that we do not violate patient id constraints.
                        this.validateDocumentPatientId(targetId, targetPatientId);
                    } else {
                        // Target is the current document.  See about the source.
                        if (!assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
                            // If the association type is not a has member, then the source must be a document.
                            // For optimization reasons, assuming a document (not verifying here).
                            // Now make sure that we do not violate patient id constraints.
                            this.validateDocumentPatientId(sourceId, targetPatientId);

                        } else {
                            // Now make sure that we do not violate patient id constraints.
                            // Note: The below method will not fail if source refers to a submission set.
                            this.validateFolderPatientId(sourceId, targetPatientId);
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param cmd
     * @param loadedMetadata
     * @param muSQ
     * @param targetObjectId
     * @throws XdsException
     */
    private void validateStatusUpdateOnFolder(UpdateStatusCommand cmd,
            Metadata loadedMetadata,
            MetadataUpdateStoredQuerySupport muSQ, String targetObjectId) throws XdsException {
        if (!loadedMetadata.getFolders().isEmpty()) {
            OMElement currentFolder = loadedMetadata.getFolder(0);
            this.validateStatusConstraints(cmd, "Folder", loadedMetadata, currentFolder);

            // Now, make sure that we don't create a situation where there is more than one
            // approved document for the same LID.
            if (cmd.getNewStatus().equals(MetadataSupport.status_type_approved)) {
                // We are attempting to change the status to approved.  Enforce rules.
                String lid = currentFolder.getAttributeValue(MetadataSupport.lid_qname);

                // Look up by LID.
                OMElement lidQueryResult = muSQ.getFolderByLID(lid);
                Metadata metadataVersions = MetadataParser.parseNonSubmission(lidQueryResult);
                List<OMElement> versionedFolders = metadataVersions.getFolders();

                // Make sure we are not violating any version constraints.
                this.validateVersionConstraints(targetObjectId, metadataVersions, currentFolder, versionedFolders);

                // Now, validate PID constraints.
                // Scan for existing non-deprecated HasMember associations (in approved status).
                Metadata assocMetadata = cmd.getApprovedHasMemberAssocs(targetObjectId);
                for (OMElement assoc : assocMetadata.getAssociations()) {
                    String sourceId = assocMetadata.getSourceObject(assoc);
                    if (sourceId.equals(targetObjectId)) {
                        // Source is the folder; target should be a document.
                        // Now make sure that we do not violate patient id constraints.
                        String targetId = assocMetadata.getTargetObject(assoc);
                        String targetPatientId = loadedMetadata.getPatientId(currentFolder);
                        this.validateDocumentPatientId(targetId, targetPatientId);
                    }
                }
            }
        }
    }

    /**
     *
     * @param cmd
     * @param loadedMetadata
     * @param muSQ
     * @param targetObjectId
     * @throws XdsException
     */
    private void validateStatusUpdateOnAssociation(UpdateStatusCommand cmd,
            Metadata loadedMetadata,
            MetadataUpdateStoredQuerySupport muSQ, String targetObjectId) throws XdsException {
        if (!loadedMetadata.getAssociations().isEmpty()) {
            OMElement currentAssoc = loadedMetadata.getAssociation(0);
            this.validateStatusConstraints(cmd, "Association", loadedMetadata, currentAssoc);
            String assocType = loadedMetadata.getAssocType(currentAssoc);
            if (cmd.getNewStatus().equals(MetadataSupport.status_type_approved)) {
                // We are attempting to change the status to approved.  Enforce rules.
                String sourceRegistryObjectId = loadedMetadata.getAssocSource(currentAssoc);
                String targetRegistryObjectId = loadedMetadata.getAssocTarget(currentAssoc);
                if (Metadata.isValidDocumentAssociationType(assocType)) {
                    // In this case, the source and targets must be documents.
                    // Get the source/target documents.
                    muSQ.setReturnLeafClass(true);
                    Metadata sourceMetadata = cmd.getDocumentMetadata(muSQ, sourceRegistryObjectId);
                    if (sourceMetadata == null) {
                        throw new XdsException("Source registry object metadata not found");
                    }
                    String sourcePatientId = sourceMetadata.getPatientId(sourceMetadata.getExtrinsicObject(0));
                    this.validateDocumentPatientId(targetRegistryObjectId, sourcePatientId);
                } else if (assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
                    // HasMember association.
                    // Make sure that the source object points to a folder and the target objects points
                    // to a document.
                    // Get the source folder (must be a folder)
                    muSQ.setReturnLeafClass(true);
                    Metadata sourceMetadata = cmd.getFolderMetadata(muSQ, sourceRegistryObjectId);
                    if (sourceMetadata == null) {
                        throw new XdsException("Source folder metadata not found");
                    }
                    String sourcePatientId = sourceMetadata.getPatientId(sourceMetadata.getFolder(0));
                    this.validateDocumentPatientId(targetRegistryObjectId, sourcePatientId);
                }
            }
        }
    }

    /**
     *
     * @param targetObjectId
     * @param metadataVersions
     * @param targetedRegistryObject
     * @param versionedRegistryObjects
     * @throws XdsException
     */
    private void validateVersionConstraints(String targetObjectId, Metadata metadataVersions, OMElement targetedRegistryObject, List<OMElement> versionedRegistryObjects) throws XdsException {
        // Make sure there is not already an approved registry object for this LID
        // (but not the registry object that we found, we need to skip that one).
        for (OMElement versionedRegistryObject : versionedRegistryObjects) {
            String versionedRegistryObjectId = metadataVersions.getId(versionedRegistryObject);
            String currentStatus = metadataVersions.getStatus(versionedRegistryObject);
            if (!versionedRegistryObjectId.equals(targetObjectId)
                    && currentStatus.equals(MetadataSupport.status_type_approved)) {
                Double version = Metadata.getRegistryObjectVersion(versionedRegistryObject);
                throw new XdsException(
                        "Another version of this registry object is already approved - registry object UUID = "
                        + versionedRegistryObjectId + " with version = " + version);
            }
        }

        // Only allow setting the latest version to approved.
        Double latestVersion = Metadata.getRegistryObjectVersion(targetedRegistryObject);
        String latestVersionId = targetObjectId;
        for (OMElement versionedRegistryObject : versionedRegistryObjects) {
            String versionedRegistryObjectId = metadataVersions.getId(versionedRegistryObject);
            Double version = Metadata.getRegistryObjectVersion(versionedRegistryObject);
            if (version > latestVersion) {
                latestVersion = version;
                latestVersionId = versionedRegistryObjectId;
            }
        }
        if (!targetObjectId.equals(latestVersionId)) {
            throw new XdsException("Targeted registry object is not the latest version - latest versioned registry object has UUID = "
                    + latestVersionId + " with version = " + latestVersion);
        }
    }

    /**
     * 
     * @param cmd
     * @param objectType
     * @param m
     * @param targetRegistryObject
     * @throws XdsException
     */
    private void validateStatusConstraints(UpdateStatusCommand cmd, String objectType, Metadata m, OMElement targetRegistryObject) throws XdsException {
        String status = m.getStatus(targetRegistryObject);

        // Make sure current status on registry object matches the "originalStatus" provided.
        if (!status.equals(cmd.getOriginalStatus())) {
            throw new XdsException(objectType + " found but it does not match originalStatus provided.  Status in registry = " + status);
        }

        // Make sure current status on registry object is not the same as the "newStatus".
        if (status.equals(cmd.getNewStatus())) {
            throw new XdsException(objectType + " found but already matches the newStatus provided.");
        }
    }
}
