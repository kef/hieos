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

        Metadata submittedMetadata = cmd.getMetadata();
        OMElement targetObject = cmd.getTargetObject();
        String previousVersion = cmd.getPreviousVersion();

        // Save target patient id for later usage.
        cmd.setTargetPatientId(submittedMetadata.getPatientId(targetObject));

        // Get lid.
        String lid = submittedMetadata.getLID(targetObject);

        //
        // Look for an existing document that 1) matches the lid, 2) status is "Approved"
        // and 3) matches the previous version.
        //

        // Prepare to issue registry query.
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                metadataUpdateContext.getRegistryResponse(), logMessage,
                backendRegistry);
        muSQ.setReturnLeafClass(true);

        // Attempt to find existing folder.
        backendRegistry.setReason("Locate Previous Approved Folder (by LID/Version)");
        OMElement queryResult = muSQ.getFoldersByLID(lid, MetadataSupport.status_type_approved, previousVersion);
        backendRegistry.setReason("");

        // Convert response into Metadata instance.
        cmd.setCurrentMetadata(MetadataParser.parseNonSubmission(queryResult));
        Metadata currentMetadata = cmd.getCurrentMetadata();
        List<OMElement> currentFolders = currentMetadata.getFolders();
        if (currentFolders.isEmpty()) {
            throw new XdsException("Existing approved folder entry not found for lid="
                    + lid + ", version=" + previousVersion);
        } else if (currentFolders.size() > 1) {
            throw new XdsException("> 1 existing folder entry found!");
        }

        // Fall through: we found a folder that matches.
        cmd.setCurrentRegistryObject(currentMetadata.getFolder(0));

        // FIXME: BEEF UP VALIDATIONS!!!!
        // Make sure submission does not also include documents or other metadata we don't
        // care about.

        // Validate the submitted submission set along with its contained content.
        RegistryObjectValidator rov = new RegistryObjectValidator(registryResponse, logMessage, backendRegistry);
        rov.validateMetadataStructure(submittedMetadata, true /* isSubmit */, registryResponse.registryErrorList);
        if (registryResponse.has_errors()) {
            validationSuccess = false;
        } else {
            // Validate unique identifier's match.
            this.validateUniqueIdMatch(targetObject, submittedMetadata, cmd.getCurrentRegistryObject(), currentMetadata);
            // Run further validations.
            rov.validateSubmissionSetUniqueIds(submittedMetadata);
            rov.validatePatientId(submittedMetadata, configActor);
            //rov.validate(submittedMetadata, MetadataType.UPDATE_SUBMISSION, registryResponse.registryErrorList, configActor);
            //if (registryResponse.has_errors()) {
            //    validationSuccess = false;
            //} else {
            //}
        }
        return validationSuccess;
    }
}
