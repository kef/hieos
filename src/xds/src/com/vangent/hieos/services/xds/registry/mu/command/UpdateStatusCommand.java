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
import com.vangent.hieos.services.xds.registry.mu.validation.UpdateDocumentSetCommandValidator;
import com.vangent.hieos.services.xds.registry.mu.validation.UpdateStatusCommandValidator;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
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
public class UpdateStatusCommand extends MetadataUpdateCommand {

    private String targetObjectId;
    private String newStatus;
    private String originalStatus;

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public UpdateStatusCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        super(metadata, metadataUpdateContext);
    }

    /**
     *
     * @return
     */
    public String getTargetObjectId() {
        return targetObjectId;
    }

    /**
     *
     * @param targetObjectId
     */
    public void setTargetObjectId(String targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    /**
     *
     * @return
     */
    public String getNewStatus() {
        return newStatus;
    }

    /**
     *
     * @param newStatus
     */
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    /**
     *
     * @return
     */
    public String getOriginalStatus() {
        return originalStatus;
    }

    /**
     *
     * @param originalStatus
     */
    public void setOriginalStatus(String originalStatus) {
        this.originalStatus = originalStatus;
    }

    /**
     * 
     * @return
     */
    @Override
    protected UpdateDocumentSetCommandValidator getCommandValidator() {
        return new UpdateStatusCommandValidator(this);
    }

    /**
     *
     * @param validator
     * @return
     * @throws XdsException
     */
    @Override
    protected boolean execute(UpdateDocumentSetCommandValidator validator) throws XdsException {
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        String targetObjectId = this.getTargetObjectId();

        // Log parameters.
        this.getMetadataUpdateContext().getLogMessage().addOtherParam("Input Parameters",
                "NewStatus = " + newStatus
                + ", OriginalStatus = " + originalStatus
                + ", Target Registry Object Id = " + targetObjectId);

        // Validate the new status is a valid entry.
        if (!(newStatus.equals(MetadataSupport.status_type_approved)
                || newStatus.equals(MetadataSupport.status_type_deprecated))) {
            throw new XdsException(newStatus + " is not a valid status for this registry");
        }

        // Query registry and see what kind of object (Folder, ExtrinsicObject, Association) is being updated.
        // Should do in the following order:
        //   1. ExtrinsicObject
        //   2. Folder
        //   3. Association

        // Prepare to issue registry query.
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                metadataUpdateContext.getRegistryResponse(), logMessage,
                metadataUpdateContext.getBackendRegistry());
        muSQ.setReturnLeafClass(true);  // FIXME: Optimize to only get root and not composed elements.

        // Try document update first.
        boolean updateStatus = this.attemptStatusUpdateOnDocumentEntry(muSQ, targetObjectId);
        if (!updateStatus) {
            // Try to update folder.
            updateStatus = this.attemptStatusUpdateOnFolder(muSQ, targetObjectId);
            if (!updateStatus) {
                // Try to update association.
                updateStatus = this.attemptStatusUpdateOnAssociation(muSQ, targetObjectId);
            }
        }
        if (!updateStatus) {
            throw new XdsException("Could not find target object = " + targetObjectId);
        }
        return true; // Success.
    }

    // FIXME: Throughout ... do not want to get FULL objects when all we care about is status.
    /**
     *
     * @param muSQ
     * @param targetObjectId
     * @return
     * @throws XdsException
     */
    private boolean attemptStatusUpdateOnDocumentEntry(
            MetadataUpdateStoredQuerySupport muSQ, String targetObjectId) throws XdsException {
        boolean updated = false;
        OMElement queryResult = muSQ.getDocumentByUUID(targetObjectId);
        Metadata m = MetadataParser.parseNonSubmission(queryResult);
        if (!m.getExtrinsicObjects().isEmpty()) {
            OMElement targetDocument = m.getExtrinsicObject(0);
            this.enforceStatusConstraints("DocumentEntry", m, targetDocument);

            // Now, make sure that we don't create a situation where there is more than one
            // approved document for the same LID.
            if (this.newStatus.equals(MetadataSupport.status_type_approved)) {
                // We are attempting to change the status to approved.  Enforce rules.
                String lid = targetDocument.getAttributeValue(MetadataSupport.lid_qname);

                // Look up by LID.
                OMElement lidQueryResult = muSQ.getDocumentsByLID(lid, null /* status */, null /* version */);
                Metadata metadataVersions = MetadataParser.parseNonSubmission(lidQueryResult);
                List<OMElement> versionedDocuments = metadataVersions.getExtrinsicObjects();

                // Make sure we are not violating any version constraints.
                this.enforceVersionConstraints(targetObjectId, metadataVersions, targetDocument, versionedDocuments);
            }
            this.getMetadataUpdateContext().getLogMessage().addOtherParam("Document Updated", targetObjectId);
            // Update the document entry status.
            this.updateRegistryObjectStatus(muSQ.getBackendRegistry(), m.getExtrinsicObjectIds(), this.newStatus);
            updated = true;
        }
        return updated;
    }

    /**
     *
     * @param muSQ
     * @param targetObjectId
     * @return
     * @throws XdsException
     */
    private boolean attemptStatusUpdateOnFolder(
            MetadataUpdateStoredQuerySupport muSQ, String targetObjectId) throws XdsException {
        boolean updated = false;
        OMElement queryResult = muSQ.getFolderByUUID(targetObjectId);
        Metadata m = MetadataParser.parseNonSubmission(queryResult);
        if (!m.getFolders().isEmpty()) {
            OMElement targetFolder = m.getFolder(0);
            this.enforceStatusConstraints("Folder", m, targetFolder);

            // Now, make sure that we don't create a situation where there is more than one
            // approved document for the same LID.
            if (this.newStatus.equals(MetadataSupport.status_type_approved)) {
                // We are attempting to change the status to approved.  Enforce rules.
                String lid = targetFolder.getAttributeValue(MetadataSupport.lid_qname);

                // Look up by LID.
                OMElement lidQueryResult = muSQ.getFolderByLID(lid);
                Metadata metadataVersions = MetadataParser.parseNonSubmission(lidQueryResult);
                List<OMElement> versionedFolders = metadataVersions.getFolders();

                // Make sure we are not violating any version constraints.
                this.enforceVersionConstraints(targetObjectId, metadataVersions, targetFolder, versionedFolders);
            }
            this.getMetadataUpdateContext().getLogMessage().addOtherParam("Folder Updated", targetObjectId);
            // Update the folder status.
            this.updateRegistryObjectStatus(muSQ.getBackendRegistry(), m.getFolderIds(), this.newStatus);
            updated = true;
        }
        return updated;
    }

    /**
     *
     * @param muSQ
     * @param targetObjectId
     * @return
     * @throws XdsException
     */
    private boolean attemptStatusUpdateOnAssociation(
            MetadataUpdateStoredQuerySupport muSQ, String targetObjectId) throws XdsException {
        boolean updated = false;
        OMElement queryResult = muSQ.getAssociationByUUID(targetObjectId);
        Metadata m = MetadataParser.parseNonSubmission(queryResult);
        if (!m.getAssociations().isEmpty()) {
            OMElement targetAssoc = m.getAssociation(0);
            this.enforceStatusConstraints("Association", m, targetAssoc);

            // FIXME: Enforce rules?

            // Update the association status.
            this.getMetadataUpdateContext().getLogMessage().addOtherParam("Association Updated", targetObjectId);
            this.updateRegistryObjectStatus(muSQ.getBackendRegistry(), m.getAssociationIds(), this.newStatus);
            updated = true;
        }
        return updated;
    }

    /**
     *
     * @param targetObjectId
     * @param metadataVersions
     * @param targetedRegistryObject
     * @param versionedRegistryObjects
     * @throws XdsException
     */
    private void enforceVersionConstraints(String targetObjectId, Metadata metadataVersions, OMElement targetedRegistryObject, List<OMElement> versionedRegistryObjects) throws XdsException {
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
     * @param objectType
     * @param m
     * @param targetRegistryObject
     * @throws XdsException
     */
    private void enforceStatusConstraints(String objectType, Metadata m, OMElement targetRegistryObject) throws XdsException {
        String status = m.getStatus(targetRegistryObject);

        // Make sure current status on registry object matches the "originalStatus" provided.
        if (!status.equals(this.originalStatus)) {
            throw new XdsException(objectType + " found but it does not match originalStatus provided.  Status in registry = " + status);
        }

        // Make sure current status on registry object is not the same as the "newStatus".
        if (status.equals(this.newStatus)) {
            throw new XdsException(objectType + " found but already matches the newStatus provided.");
        }
    }

    /**
     * 
     * @param backendRegistry
     * @param objectIds
     * @param status
     * @throws XdsInternalException
     */
    private void updateRegistryObjectStatus(BackendRegistry backendRegistry,
            List<String> objectIds, String status) throws XdsInternalException {
        backendRegistry.submitSetStatusOnObjectsRequest(objectIds, status);
        // FIXME: Deal with response!!

    }
}
