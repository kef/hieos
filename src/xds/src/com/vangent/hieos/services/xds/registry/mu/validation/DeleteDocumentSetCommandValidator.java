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
import com.vangent.hieos.services.xds.registry.mu.command.DeleteDocumentSetCommand;
import com.vangent.hieos.services.xds.registry.mu.command.MetadataUpdateCommand;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateHelper;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class DeleteDocumentSetCommandValidator extends MetadataUpdateCommandValidator {

    /**
     * 
     * @param metadataUpdateCommand
     */
    public DeleteDocumentSetCommandValidator(MetadataUpdateCommand metadataUpdateCommand) {
        super(metadataUpdateCommand);
    }

    /**
     * 
     * @throws XdsException
     */
    public boolean validate() throws XdsException {
        DeleteDocumentSetCommand cmd = (DeleteDocumentSetCommand) this.getMetadataUpdateCommand();
        Metadata submittedMetadata = cmd.getMetadata();
        // Get list of object references.
        List<String> objectRefIds = submittedMetadata.getObjectRefIds();
        if (objectRefIds.isEmpty()) {
            throw new XdsException("No object references specified");
        }
        boolean validationSuccess = true;
        Metadata loadedMetadata = this.loadRegistryObjects(cmd, objectRefIds);
        if (loadedMetadata.getExtrinsicObjects().isEmpty()
                && loadedMetadata.getFolders().isEmpty()
                && loadedMetadata.getAssociations().isEmpty()) {
            throw new XdsException("No documents, folders or associations found to delete");
        }
        // Verify that each specified object reference is in the loaded metadata.
        for (String objectRefId : objectRefIds) {
            OMElement registryObject = loadedMetadata.getObjectById(objectRefId);
            if (registryObject == null) {
                // FIXME: Add proper MU exception
                throw new XdsException("Can not find supplied object reference = " + objectRefId);
            }
        }
        // TBD: Do some validation here - or move to validation class.
        return validationSuccess;
    }

    /**
     * 
     * @param cmd
     * @param objectRefIds
     * @return
     * @throws XdsException
     */
    private Metadata loadRegistryObjects(DeleteDocumentSetCommand cmd, List<String> objectRefIds) throws XdsException {
        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = cmd.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        //RegistryResponse registryResponse = metadataUpdateContext.getRegistryResponse();
        //XConfigActor configActor = metadataUpdateContext.getConfigActor();

        // Prepare to issue registry query.
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                metadataUpdateContext.getRegistryResponse(), logMessage,
                backendRegistry);
        muSQ.setReturnLeafClass(true);

        // Get full metadata for request.
        Metadata metadata = new Metadata();

        // Load documents.
        backendRegistry.setReason("Get Existing Documents");
        OMElement documentQueryResult = muSQ.getDocumentByUUID(objectRefIds);
        metadata.addMetadata(documentQueryResult, true /* discard_duplicates */);

        // Load folders.
        backendRegistry.setReason("Get Existing Folders");
        OMElement folderQueryResult = muSQ.getFolderByUUID(objectRefIds);
        metadata.addMetadata(folderQueryResult, true /* discard_duplicates */);

        // Load associations.
        backendRegistry.setReason("Get Existing Associations");
        OMElement assocQueryResult = muSQ.getAssociationByUUID(objectRefIds);
        metadata.addMetadata(assocQueryResult, true /* discard_duplicates */);
        backendRegistry.setReason("");

        // Log metadata found.
        MetadataUpdateHelper.logMetadata(logMessage, metadata);
        return metadata;
    }
}
