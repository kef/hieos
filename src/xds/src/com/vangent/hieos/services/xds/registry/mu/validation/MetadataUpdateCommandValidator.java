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
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.xutil.exception.XDSPatientIDReconciliationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
abstract public class MetadataUpdateCommandValidator {

    private MetadataUpdateCommand metadataUpdateCommand;

    /**
     *
     * @param metadataUpdateCommand
     */
    public MetadataUpdateCommandValidator(MetadataUpdateCommand metadataUpdateCommand) {
        this.metadataUpdateCommand = metadataUpdateCommand;
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
        boolean foundDocument = false;

        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = metadataUpdateCommand.getMetadataUpdateContext();
        //XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // Prepare for queries.
        MetadataUpdateStoredQuerySupport muSQ = metadataUpdateContext.getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);

        // Now make sure that we do not violate patient id constraints.
        backendRegistry.setReason("Validate document patient identifier constraint");
        OMElement documentQueryResult = muSQ.getDocumentByUUID(currentDocumentEntryId);
        backendRegistry.setReason("");

        Metadata documentMetadata = MetadataParser.parseNonSubmission(documentQueryResult);
        foundDocument = documentMetadata.getExtrinsicObjects().size() > 0;
        if (foundDocument) {
            OMElement document = documentMetadata.getExtrinsicObject(0);
            String documentPatientId = documentMetadata.getPatientId(document);
            if (!documentPatientId.equals(targetPatientId)) {
                throw new XDSPatientIDReconciliationException("Update would violate patient id constraint - existing document patient id = "
                        + documentPatientId + ", document UUID = "
                        + currentDocumentEntryId + " does not match updated registry object's patient id = "
                        + targetPatientId);
            }
        }
        return foundDocument;
    }

    /**
     *
     * @param currentFolderEntryId
     * @param targetPatientId
     * @return true if folder was found, false otherwise.
     * @throws XdsException
     */
    public boolean validateFolderPatientId(String currentFolderEntryId, String targetPatientId) throws XdsException {
        boolean foundFolder = false;

        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = metadataUpdateCommand.getMetadataUpdateContext();
        //XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // Prepare for queries.
        MetadataUpdateStoredQuerySupport muSQ = metadataUpdateContext.getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);

        // Now make sure that we do not violate patient id constraints.
        backendRegistry.setReason("Validate folder patient identifier constraint");
        OMElement folderQueryResult = muSQ.getFolderByUUID(currentFolderEntryId);
        backendRegistry.setReason("");

        Metadata folderMetadata = MetadataParser.parseNonSubmission(folderQueryResult);
        foundFolder = folderMetadata.getFolders().size() > 0;
        if (foundFolder) {
            OMElement folder = folderMetadata.getFolder(0);
            String folderPatientId = folderMetadata.getPatientId(folder);
            if (!folderPatientId.equals(targetPatientId)) {
                throw new XDSPatientIDReconciliationException("Update would violate patient id constraint - existing folder patient id = "
                        + folderPatientId + ", folder UUID = "
                        + currentFolderEntryId + " does not match updated registry object's patient id = "
                        + targetPatientId);
            }
        }
        return foundFolder;
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
