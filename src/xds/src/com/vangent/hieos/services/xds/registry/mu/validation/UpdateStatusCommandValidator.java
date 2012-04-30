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
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.services.xds.registry.storedquery.RegistryObjectValidator;
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

        // Validate the new status is a valid entry.
        String newStatus = cmd.getNewStatus();
        if (!(newStatus.equals(MetadataSupport.status_type_approved)
                || newStatus.equals(MetadataSupport.status_type_deprecated))) {
            throw new XdsException(newStatus + " is not a valid status for this registry");
        }

        Metadata submittedMetadata = cmd.getMetadata();
        RegistryObjectValidator rov = new RegistryObjectValidator(registryResponse, logMessage, backendRegistry);
        rov.validateMetadataStructure(submittedMetadata, true /* isSubmit */, registryResponse.registryErrorList);
        if (registryResponse.has_errors()) {
            validationSuccess = false;
        } else {
            // Make sure that submission set is unique.
            rov.validateSubmissionSetUniqueIds(submittedMetadata);

            // Prepare to issue registry queries.
            MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                    metadataUpdateContext.getRegistryResponse(), logMessage,
                    metadataUpdateContext.getBackendRegistry());
            muSQ.setReturnLeafClass(true);  // FIXME: Optimize to only get root and not composed elements.

            // Load metadata (based on target object id).
            Metadata loadedMetadata = this.loadMetadata(muSQ, cmd.getTargetObjectId());
            cmd.setLoadedMetadata(loadedMetadata);  // For usage by command.
            if (loadedMetadata == null) {
                throw new XdsException("Could not find target object = " + cmd.getTargetObjectId());
            }

            // Now, run validations.
            this.validateStatusUpdateOnRegistryObject(cmd, loadedMetadata, muSQ, cmd.getTargetObjectId());
        }
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
            OMElement targetDocument = loadedMetadata.getExtrinsicObject(0);
            this.validateStatusConstraints(cmd, "DocumentEntry", loadedMetadata, targetDocument);

            // Now, make sure that we don't create a situation where there is more than one
            // approved document for the same LID.
            if (cmd.getNewStatus().equals(MetadataSupport.status_type_approved)) {
                // We are attempting to change the status to approved.  Enforce rules.
                String lid = targetDocument.getAttributeValue(MetadataSupport.lid_qname);

                // Look up by LID.
                OMElement lidQueryResult = muSQ.getDocumentsByLID(lid, null /* status */, null /* version */);
                Metadata metadataVersions = MetadataParser.parseNonSubmission(lidQueryResult);
                List<OMElement> versionedDocuments = metadataVersions.getExtrinsicObjects();

                // Make sure we are not violating any version constraints.
                this.validateVersionConstraints(targetObjectId, metadataVersions, targetDocument, versionedDocuments);
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
            OMElement targetFolder = loadedMetadata.getFolder(0);
            this.validateStatusConstraints(cmd, "Folder", loadedMetadata, targetFolder);

            // Now, make sure that we don't create a situation where there is more than one
            // approved document for the same LID.
            if (cmd.getNewStatus().equals(MetadataSupport.status_type_approved)) {
                // We are attempting to change the status to approved.  Enforce rules.
                String lid = targetFolder.getAttributeValue(MetadataSupport.lid_qname);

                // Look up by LID.
                OMElement lidQueryResult = muSQ.getFolderByLID(lid);
                Metadata metadataVersions = MetadataParser.parseNonSubmission(lidQueryResult);
                List<OMElement> versionedFolders = metadataVersions.getFolders();

                // Make sure we are not violating any version constraints.
                this.validateVersionConstraints(targetObjectId, metadataVersions, targetFolder, versionedFolders);
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
            OMElement targetAssoc = loadedMetadata.getAssociation(0);
            this.validateStatusConstraints(cmd, "Association", loadedMetadata, targetAssoc);

            // FIXME: Enforce rules?
            // Need to look at both ends of association and make sure that an invalid
            // registry update is not going to occur.
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
