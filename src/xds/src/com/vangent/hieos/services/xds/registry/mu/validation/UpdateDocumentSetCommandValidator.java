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
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
abstract public class UpdateDocumentSetCommandValidator {

    private MetadataUpdateCommand metadataUpdateCommand;

    /**
     *
     * @param muCommand
     */
    public UpdateDocumentSetCommandValidator(MetadataUpdateCommand metadataUpdateCommand) {
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
     * @throws XdsException
     */
    abstract public boolean validate() throws XdsException;

    /**
     *
     * @param currentDocumentEntryId
     * @param targetPatientId
     * @throws XdsException
     */
    public void validateDocumentPatientId(String currentDocumentEntryId, String targetPatientId) throws XdsException {
        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = metadataUpdateCommand.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // Prepare for queries.
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                metadataUpdateContext.getRegistryResponse(), logMessage,
                metadataUpdateContext.getBackendRegistry());
        muSQ.setReturnLeafClass(true);

        // Now make sure that we do not violate patient id constraints.
        backendRegistry.setReason("Validate document patient identifier constraint");
        OMElement documentQueryResult = muSQ.getDocumentByUUID(currentDocumentEntryId);
        backendRegistry.setReason("");

        Metadata documentMetadata = MetadataParser.parseNonSubmission(documentQueryResult);
        OMElement document = documentMetadata.getExtrinsicObject(0);
        String documentPatientId = documentMetadata.getPatientId(document);
        if (!documentPatientId.equals(targetPatientId)) {
            throw new XDSPatientIDReconciliationException("Update would violate patient id constraint - existing document patient id = "
                    + documentPatientId + ", document UUID = "
                    + currentDocumentEntryId + " does not match updated registry object's patient id = "
                    + targetPatientId);
        }
    }

    /**
     *
     * @param currentFolderEntryId
     * @param targetPatientId
     * @throws XdsException
     */
    public void validateFolderPatientId(String currentFolderEntryId, String targetPatientId) throws XdsException {
        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = metadataUpdateCommand.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // Prepare for queries.
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                metadataUpdateContext.getRegistryResponse(), logMessage,
                metadataUpdateContext.getBackendRegistry());
        muSQ.setReturnLeafClass(true);

        // Now make sure that we do not violate patient id constraints.
        backendRegistry.setReason("Validate folder patient identifier constraint");
        OMElement folderQueryResult = muSQ.getFolderByUUID(currentFolderEntryId);
        backendRegistry.setReason("");

        Metadata folderMetadata = MetadataParser.parseNonSubmission(folderQueryResult);
        OMElement folder = folderMetadata.getFolder(0);
        String folderPatientId = folderMetadata.getPatientId(folder);
        if (!folderPatientId.equals(targetPatientId)) {
            throw new XDSPatientIDReconciliationException("Update would violate patient id constraint - existing folder patient id = "
                    + folderPatientId + ", folder UUID = "
                    + currentFolderEntryId + " does not match updated registry object's patient id = "
                    + targetPatientId);
        }
    }

    /**
     *
     * @param targetRegistryObject
     * @param submittedMetadata
     * @param currentRegistryObject
     * @param currentMetadata
     * @throws XdsException
     */
    protected void validateUniqueIdMatch(OMElement targetRegistryObject, Metadata submittedMetadata, OMElement currentRegistryObject, Metadata currentMetadata) throws XdsException {
        String currentUniqueId = submittedMetadata.getUniqueIdValue(submittedMetadata.getId(targetRegistryObject));
        String targetUniqueId = currentMetadata.getUniqueIdValue(currentMetadata.getId(currentRegistryObject));
        if (!currentUniqueId.equals(targetUniqueId)) {
            // FIXME: Be more descriptive.
            throw new XdsException("Unique ID Mismatch!");
        }
    }
}
