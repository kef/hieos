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
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.xutil.exception.XDSPatientIDReconciliationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
abstract public class MetadataUpdateCommandValidator {

    private MetadataUpdateCommand metadataUpdateCommand = null;

    /**
     *
     */
    public MetadataUpdateCommandValidator() {
    }

    /**
     * 
     * @param metadataUpdateCommand
     */
    public void setMetadataUpdateCommand(MetadataUpdateCommand metadataUpdateCommand) {
        this.metadataUpdateCommand = metadataUpdateCommand;
    }

    /**
     *
     * @return
     */
    public MetadataUpdateCommand getMetadataUpdateCommand() {
        return metadataUpdateCommand;
    }

    /**
     * 
     * @return 
     * @throws XdsException
     */
    abstract public boolean validate() throws XdsException;

    /**
     *
     * @param currentDocumentEntryId
     * @param targetPatientId
     * @return true if document was found, false otherwise.
     * 
     * @throws XdsException
     */
    public boolean validateDocumentPatientId(String currentDocumentEntryId, String targetPatientId) throws XdsException {
        MetadataUpdateCommand cmd = this.getMetadataUpdateCommand();
        MetadataUpdateContext metadataUpdateContext = metadataUpdateCommand.getMetadataUpdateContext();

        // Prepare for queries.
        MetadataUpdateStoredQuerySupport muSQ = metadataUpdateContext.getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);

        // Now make sure that we do not violate patient id constraints.
        muSQ.setReason("Validate document patient identifier constraint");
        Metadata documentMetadata = cmd.getDocumentMetadata(muSQ, currentDocumentEntryId);
        muSQ.setReason("");

        if (documentMetadata != null) {
            OMElement document = documentMetadata.getExtrinsicObject(0);
            this.validateDocumentPatientId(documentMetadata, document, targetPatientId);
            // Also, validate that the document is "approved".
            this.validateApprovedStatus(documentMetadata, document);
        }
        return documentMetadata != null;
    }

    /**
     *
     * @param documentMetadata
     * @param document
     * @param targetPatientId
     * @throws XdsException
     */
    public void validateDocumentPatientId(Metadata documentMetadata, OMElement document, String targetPatientId) throws XdsException {
        String documentPatientId = documentMetadata.getPatientId(document);
        String documentUUID = documentMetadata.getId(document);
        if (!documentPatientId.equals(targetPatientId)) {
            throw new XDSPatientIDReconciliationException("Update would violate patient id constraint - existing document patient id = "
                    + documentPatientId + ", document UUID = "
                    + documentUUID + " does not match updated registry object's patient id = "
                    + targetPatientId);
        }
    }

    /**
     *
     * @param currentFolderEntryId
     * @param targetPatientId
     * @return true if folder was found, false otherwise.
     * @throws XdsException
     */
    public boolean validateFolderPatientId(String currentFolderEntryId, String targetPatientId) throws XdsException {
        MetadataUpdateCommand cmd = this.getMetadataUpdateCommand();
        MetadataUpdateContext metadataUpdateContext = metadataUpdateCommand.getMetadataUpdateContext();

        // Prepare for queries.
        MetadataUpdateStoredQuerySupport muSQ = metadataUpdateContext.getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);

        // Now make sure that we do not violate patient id constraints.
        muSQ.setReason("Validate folder patient identifier constraint");
        Metadata folderMetadata = cmd.getFolderMetadata(muSQ, currentFolderEntryId);
        muSQ.setReason("");

        if (folderMetadata != null) {
            OMElement folder = folderMetadata.getFolder(0);
            this.validateFolderPatientId(folderMetadata, folder, targetPatientId);
            // Also, validate that the folder is "approved".
            this.validateApprovedStatus(folderMetadata, folder);
        }
        return folderMetadata != null;
    }

    /**
     * 
     * @param folderMetadata
     * @param folder
     * @param targetPatientId
     * @throws XdsException
     */
    public void validateFolderPatientId(Metadata folderMetadata, OMElement folder, String targetPatientId) throws XdsException {
        String folderPatientId = folderMetadata.getPatientId(folder);
        String folderUUID = folderMetadata.getId(folder);
        if (!folderPatientId.equals(targetPatientId)) {
            throw new XDSPatientIDReconciliationException("Update would violate patient id constraint - existing folder patient id = "
                    + folderPatientId + ", folder UUID = "
                    + folderUUID + " does not match updated registry object's patient id = "
                    + targetPatientId);
        }
    }

    /**
     *
     * @param metadata
     * @param registryObject
     * @throws XdsException
     */
    public void validateApprovedStatus(Metadata metadata, OMElement registryObject) throws XdsException {
        if (!metadata.getStatus(registryObject).equals(MetadataSupport.status_type_approved)) {
            throw new XdsException("Expected registry object status is approved");
        }
    }

    /**
     *
     * @param submittedRegistryObject
     * @param submittedMetadata
     * @param currentRegistryObject
     * @param currentMetadata
     * @throws XdsException
     */
    protected void validateUniqueIdMatch(OMElement submittedRegistryObject, Metadata submittedMetadata, OMElement currentRegistryObject, Metadata currentMetadata) throws XdsException {
        String submittedUUID = submittedMetadata.getId(submittedRegistryObject);
        String currentUUID = currentMetadata.getId(currentRegistryObject);
        String submittedUniqueId = submittedMetadata.getUniqueIdValue(submittedUUID);
        String currentUniqueId = currentMetadata.getUniqueIdValue(currentUUID);
        if (!submittedUniqueId.equals(currentUniqueId)) {
            throw new XdsException("Unique ID mismatch - UUID(submitted) = "
                    + submittedUUID + " has UID = " + submittedUniqueId
                    + " and UUID(current) = " + currentUUID + " has UID = " + currentUniqueId);
        }
    }
}
