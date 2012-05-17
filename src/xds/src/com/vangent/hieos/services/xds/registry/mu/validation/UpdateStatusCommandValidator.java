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

import com.vangent.hieos.services.xds.registry.mu.command.MetadataUpdateCommand;
import com.vangent.hieos.services.xds.registry.mu.command.UpdateStatusCommand;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateHelper;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.xutil.exception.XDSMetadataVersionException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
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

        if (logMessage.isLogEnabled()) {
            // Log parameters.
            logMessage.addOtherParam("Input Parameters",
                    "NewStatus = " + cmd.getNewStatus()
                    + ", OriginalStatus = " + cmd.getOriginalStatus()
                    + ", Target Registry Object Id = " + cmd.getTargetObjectId());
        }

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

        // Load metadata (based on target object id).
        Metadata loadedMetadata = this.loadMetadata(cmd, targetObjectId);
        cmd.setLoadedMetadata(loadedMetadata);  // For usage by command later (and below).
        if (loadedMetadata == null) {
            throw new XdsException("Could not find target object = " + targetObjectId);
        }

        // Now, run validations.
        this.validateStatusUpdateOnRegistryObject(cmd);
        return validationSuccess;
    }

    /**
     * 
     * @param cmd
     * @param targetObjectId
     * @return
     * @throws XdsException
     */
    private Metadata loadMetadata(UpdateStatusCommand cmd, String targetObjectId) throws XdsException {
        MetadataUpdateStoredQuerySupport muSQ = cmd.getMetadataUpdateContext().getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);

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
     * @throws XdsException
     */
    private void validateStatusUpdateOnRegistryObject(UpdateStatusCommand cmd) throws XdsException {
        Metadata loadedMetadata = cmd.getLoadedMetadata();
        boolean newStatusIsTypeApproved = cmd.getNewStatus().equals(MetadataSupport.status_type_approved);
        if (!loadedMetadata.getExtrinsicObjects().isEmpty()) {
            OMElement currentDocument = loadedMetadata.getExtrinsicObject(0);
            this.validateStatusConstraints(cmd, "DocumentEntry", loadedMetadata, currentDocument);
            if (newStatusIsTypeApproved) {
                this.validateApprovedStatusUpdateOnDocumentEntry(cmd);
            }
        } else if (!loadedMetadata.getFolders().isEmpty()) {
            OMElement currentFolder = loadedMetadata.getFolder(0);
            this.validateStatusConstraints(cmd, "Folder", loadedMetadata, currentFolder);
            if (newStatusIsTypeApproved) {
                this.validateApprovedStatusUpdateOnFolder(cmd);
            }
        } else if (!loadedMetadata.getAssociations().isEmpty()) {
            OMElement currentAssoc = loadedMetadata.getAssociation(0);
            this.validateStatusConstraints(cmd, "Association", loadedMetadata, currentAssoc);
            if (newStatusIsTypeApproved) {
                this.validateApprovedStatusUpdateOnAssociation(cmd);
            }
        }
    }

    /**
     * 
     * @param cmd
     * @throws XdsException
     */
    private void validateApprovedStatusUpdateOnDocumentEntry(UpdateStatusCommand cmd) throws XdsException {
        // We are attempting to change the status to approved.  Enforce rules.
        Metadata loadedMetadata = cmd.getLoadedMetadata();
        OMElement currentDocument = loadedMetadata.getExtrinsicObject(0);

        // Now, make sure that we don't create a situation where there is more than one
        // approved document for the same LID.
        MetadataUpdateStoredQuerySupport muSQ = cmd.getMetadataUpdateContext().getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);
        String targetObjectId = cmd.getTargetObjectId();

        // Look up by LID.
        String lid = currentDocument.getAttributeValue(MetadataSupport.lid_qname);
        OMElement lidQueryResult = muSQ.getDocumentsByLID(lid, null /* status */, null /* version */);
        Metadata versionedMetadata = MetadataParser.parseNonSubmission(lidQueryResult);
        List<OMElement> versionedRegistryObjects = versionedMetadata.getExtrinsicObjects();

        // Make sure we are not violating any version constraints.
        this.validateVersionConstraints(targetObjectId, versionedMetadata, currentDocument, versionedRegistryObjects);

        // Now, validate PID constraints.

        // Get all approved associations.
        Metadata assocMetadata = cmd.getApprovedAssocs(cmd.getTargetObjectId(), true /* leafClass */);
        List<OMElement> assocs = assocMetadata.getAssociations();
        String currentPatientId = loadedMetadata.getPatientId(currentDocument);
        for (OMElement assoc : assocs) {
            String assocType = assocMetadata.getAssocType(assoc);
            String sourceId = assocMetadata.getSourceObject(assoc);
            String targetId = assocMetadata.getAssocTarget(assoc);
            if (sourceId.equals(targetObjectId)) {
                // If source is the document, then the target is a document.
                // Now make sure that we do not violate patient id constraints.
                this.validateDocumentPatientId(targetId, currentPatientId);
            } else {
                // Target is the current document.  See about the source.
                if (!assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
                    // If the association type is not a has member, then the source must be a document.
                    // For optimization reasons, assuming a document (not verifying here).
                    // Now make sure that we do not violate patient id constraints.
                    this.validateDocumentPatientId(sourceId, currentPatientId);

                } else {
                    // Source is either a folder or submission set.
                    // Now make sure that we do not violate patient id constraints.
                    // Note: The below method will not fail if source refers to a submission set.
                    this.validateFolderPatientId(sourceId, currentPatientId);
                }
            }
        }
    }

    /**
     *
     * @param cmd
     * @throws XdsException
     */
    private void validateApprovedStatusUpdateOnFolder(UpdateStatusCommand cmd) throws XdsException {
        // We are attempting to change the status to approved.  Enforce rules.
        Metadata loadedMetadata = cmd.getLoadedMetadata();
        OMElement currentFolder = loadedMetadata.getFolder(0);

        // Now, make sure that we don't create a situation where there is more than one
        // approved document for the same LID.
        MetadataUpdateStoredQuerySupport muSQ = cmd.getMetadataUpdateContext().getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);
        String targetObjectId = cmd.getTargetObjectId();

        // Look up by LID.
        String lid = currentFolder.getAttributeValue(MetadataSupport.lid_qname);
        OMElement lidQueryResult = muSQ.getFolderByLID(lid);
        Metadata versionedMetadata = MetadataParser.parseNonSubmission(lidQueryResult);
        List<OMElement> versionedRegistryObjects = versionedMetadata.getFolders();

        // Make sure we are not violating any version constraints.
        this.validateVersionConstraints(targetObjectId, versionedMetadata, currentFolder, versionedRegistryObjects);

        // Now, validate PID constraints.
        // Scan for existing non-deprecated HasMember associations (in approved status).
        Metadata assocMetadata = cmd.getApprovedHasMemberAssocs(targetObjectId, true /* leafClass */);
        String currentPatientId = loadedMetadata.getPatientId(currentFolder);
        for (OMElement assoc : assocMetadata.getAssociations()) {
            String sourceId = assocMetadata.getSourceObject(assoc);
            if (sourceId.equals(targetObjectId)) {
                // Source is the folder; target should be a document.
                // Now make sure that we do not violate patient id constraints.
                String targetId = assocMetadata.getTargetObject(assoc);
                this.validateDocumentPatientId(targetId, currentPatientId);
            }
        }
    }

    /**
     *
     * @param cmd
     * @throws XdsException
     */
    private void validateApprovedStatusUpdateOnAssociation(UpdateStatusCommand cmd) throws XdsException {
        // We are attempting to change the status to approved.  Enforce rules.
        Metadata loadedMetadata = cmd.getLoadedMetadata();
        OMElement currentAssoc = loadedMetadata.getAssociation(0);
        MetadataUpdateStoredQuerySupport muSQ = cmd.getMetadataUpdateContext().getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);
        String sourceId = loadedMetadata.getAssocSource(currentAssoc);
        String targetId = loadedMetadata.getAssocTarget(currentAssoc);
        String assocType = loadedMetadata.getAssocType(currentAssoc);
        if (Metadata.isValidDocumentAssociationType(assocType)) {
            // In this case, the source and targets must be documents.
            // Validate that source(document) and target(document) reference the same PID.
            Metadata sourceMetadata = cmd.getDocumentMetadata(muSQ, sourceId);
            if (sourceMetadata == null) {
                throw new XdsException("Source registry object metadata not found");
            }
            String sourcePatientId = sourceMetadata.getPatientId(sourceMetadata.getExtrinsicObject(0));
            this.validateDocumentPatientId(targetId, sourcePatientId);
        } else if (assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
            // HasMember association - source could be either a submission set (invalid here) or a folder.
            // Make sure that the source object points to a folder.
            Metadata sourceMetadata = cmd.getFolderMetadata(muSQ, sourceId);
            if (sourceMetadata == null) {
                throw new XdsException("Source folder metadata not found");
            }
            // If the source is a folder, the target must be a document.
            String sourcePatientId = sourceMetadata.getPatientId(sourceMetadata.getFolder(0));
            // Validate that source(folder) and target(document) reference the same PID.
            this.validateDocumentPatientId(targetId, sourcePatientId);
        }
    }

    /**
     * 
     * @param targetObjectId
     * @param versionedMetadata
     * @param targetedRegistryObject
     * @param versionedRegistryObjects
     * @throws XdsException
     */
    private void validateVersionConstraints(String targetObjectId, Metadata versionedMetadata, OMElement targetedRegistryObject, List<OMElement> versionedRegistryObjects) throws XdsException {
        // Make sure there is not already an approved registry object for this LID
        // (but not the registry object that we found, we need to skip that one).
        for (OMElement versionedRegistryObject : versionedRegistryObjects) {
            String versionedRegistryObjectId = versionedMetadata.getId(versionedRegistryObject);
            String currentStatus = versionedMetadata.getStatus(versionedRegistryObject);
            if (!versionedRegistryObjectId.equals(targetObjectId)
                    && currentStatus.equals(MetadataSupport.status_type_approved)) {
                Double version = Metadata.getRegistryObjectVersion(versionedRegistryObject);
                throw new XDSMetadataVersionException(
                        "Another version of this registry object is already approved - registry object UUID = "
                        + versionedRegistryObjectId + " with version = " + version);
            }
        }

        // Only allow setting the latest version to approved.
        Double latestVersion = Metadata.getRegistryObjectVersion(targetedRegistryObject);
        String latestVersionId = targetObjectId;
        for (OMElement versionedRegistryObject : versionedRegistryObjects) {
            String versionedRegistryObjectId = versionedMetadata.getId(versionedRegistryObject);
            Double version = Metadata.getRegistryObjectVersion(versionedRegistryObject);
            if (version > latestVersion) {
                latestVersion = version;
                latestVersionId = versionedRegistryObjectId;
            }
        }
        if (!targetObjectId.equals(latestVersionId)) {
            throw new XDSMetadataVersionException("Targeted registry object is not the latest version - latest versioned registry object has UUID = "
                    + latestVersionId + " with version = " + latestVersion);
        }
    }

    /**
     * 
     * @param cmd
     * @param objectType
     * @param currentMetadata
     * @param currentRegistryObject
     * @throws XdsException
     */
    private void validateStatusConstraints(UpdateStatusCommand cmd, String objectType, Metadata currentMetadata, OMElement currentRegistryObject) throws XdsException {
        String currentStatus = currentMetadata.getStatus(currentRegistryObject);

        // Make sure current status on registry object matches the "originalStatus" provided.
        if (!currentStatus.equals(cmd.getOriginalStatus())) {
            throw new XdsException(objectType + " found but it does not match originalStatus provided.  Status in registry = " + currentStatus);
        }

        // Make sure current status on registry object is not the same as the "newStatus".
        if (currentStatus.equals(cmd.getNewStatus())) {
            throw new XdsException(objectType + " found but already matches the newStatus provided.");
        }
    }
}
