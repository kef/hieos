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
import com.vangent.hieos.services.xds.registry.mu.command.UpdateFolderMetadataCommand;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.services.xds.registry.storedquery.RegistryObjectValidator;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateFolderMetadataCommandValidator extends MetadataUpdateCommandValidator {

    /**
     * 
     * @param metadataUpdateCommand
     */
    public UpdateFolderMetadataCommandValidator(MetadataUpdateCommand metadataUpdateCommand) {
        super(metadataUpdateCommand);
    }

    /**
     * 
     * @throws XdsException
     */
    public boolean validate() throws XdsException {
        UpdateFolderMetadataCommand cmd = (UpdateFolderMetadataCommand) this.getMetadataUpdateCommand();
        boolean validationSuccess = true;

        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = cmd.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        RegistryResponse registryResponse = metadataUpdateContext.getRegistryResponse();
        XConfigActor configActor = metadataUpdateContext.getConfigActor();

        // Make sure submission does not also include documents or other metadata we don't
        // care about.

        // Run initial validations on submitted metadata.
        RegistryObjectValidator rov = new RegistryObjectValidator(registryResponse, logMessage, backendRegistry);
        Metadata submittedMetadata = cmd.getSubmittedMetadata();
        rov.validatePatientId(submittedMetadata, configActor);

        //
        // Look for an existing document that 1) matches the lid, 2) status is "Approved"
        // and 3) matches the previous version.
        //
        this.getCurrentRegistryObject(cmd);

        // Save target patient id for later usage.
        OMElement targetObject = cmd.getTargetObject();
        cmd.setTargetPatientId(submittedMetadata.getPatientId(targetObject));

        // Run further validations.
        this.validateUniqueIdMatch(targetObject, submittedMetadata, cmd.getCurrentRegistryObject(), cmd.getCurrentMetadata());
        return validationSuccess;
    }

    /**
     *
     * @param cmd
     * @throws XdsException
     */
    private void getCurrentRegistryObject(UpdateFolderMetadataCommand cmd) throws XdsException {
        BackendRegistry backendRegistry = cmd.getMetadataUpdateContext().getBackendRegistry();
        MetadataUpdateStoredQuerySupport muSQ = cmd.getMetadataUpdateContext().getStoredQuerySupport();
        Metadata submittedMetadata = cmd.getSubmittedMetadata();
        OMElement targetObject = cmd.getTargetObject();
        String previousVersion = cmd.getPreviousVersion();
        String lid = submittedMetadata.getLID(targetObject);

        // Attempt to find existing folder.
        muSQ.setReturnLeafClass(true);
        backendRegistry.setReason("Locate Previous Approved Folder (by LID/Version)");
        OMElement queryResult = muSQ.getFoldersByLID(lid, MetadataSupport.status_type_approved, previousVersion);
        backendRegistry.setReason("");

        // Convert response into Metadata instance.
        Metadata currentMetadata = MetadataParser.parseNonSubmission(queryResult);
        //Metadata currentMetadata = cmd.getCurrentMetadata();
        List<OMElement> currentFolders = currentMetadata.getFolders();
        if (currentFolders.isEmpty()) {
            throw new XdsException("Existing approved folder entry not found for lid="
                    + lid + ", version=" + previousVersion);
        } else if (currentFolders.size() > 1) {
            throw new XdsException("> 1 existing folder entry found!");
        }

        // Fall through: we found a folder that matches.

        // Set current metadata and registry object in command instance.
        cmd.setCurrentMetadata(currentMetadata);
        cmd.setCurrentRegistryObject(currentMetadata.getFolder(0));
    }
}
