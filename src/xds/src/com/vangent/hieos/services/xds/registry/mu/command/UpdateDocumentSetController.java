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
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateHelper;
import com.vangent.hieos.services.xds.registry.mu.validation.MetadataUpdateCommandValidator;
import com.vangent.hieos.services.xds.registry.mu.validation.SubmitAssociationCommandValidator;
import com.vangent.hieos.services.xds.registry.mu.validation.UpdateDocumentEntryMetadataCommandValidator;
import com.vangent.hieos.services.xds.registry.mu.validation.UpdateFolderMetadataCommandValidator;
import com.vangent.hieos.services.xds.registry.mu.validation.UpdateStatusCommandValidator;
import com.vangent.hieos.services.xds.registry.storedquery.RegistryObjectValidator;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.IdParser;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateDocumentSetController extends MetadataUpdateController {

    /**
     *
     * @param metadataUpdateContext
     * @param submittedMetadata
     */
    public UpdateDocumentSetController(MetadataUpdateContext metadataUpdateContext, Metadata submittedMetadata) {
        super(metadataUpdateContext, submittedMetadata);
    }

    /**
     * 
     * @return
     * @throws XdsException
     */
    @Override
    public boolean update() throws XdsException {
        // Run initial validations.
        boolean runStatus = this.runBaseValidation();
        if (runStatus) {
            // Build list of MU commands (Command Pattern) to execute.
            List<MetadataUpdateCommand> muCommands = this.buildMetadataUpdateCommands();

            // Execute each MU command.
            runStatus = this.runMetadataUpdateCommands(muCommands);
            if (runStatus) {
                // Register the submission set.
                this.registerSubmission();
            }
        }
        return runStatus;
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    @Override
    public boolean enforcePolicy() throws XdsException {
        // TBD: Implement.
        return true;
    }

    /**
     * 
     * @return
     * @throws XdsException
     */
    public boolean runBaseValidation() throws XdsException {
        boolean validationStatus = false;
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        XConfigActor configActor = metadataUpdateContext.getConfigActor();
        RegistryResponse response = metadataUpdateContext.getRegistryResponse();
        // Validate metadata structure.
        RegistryObjectValidator rov = new RegistryObjectValidator(response, logMessage, backendRegistry);
        rov.validateMetadataStructure(submittedMetadata, true /* isSubmit */, response.registryErrorList);
        if (!response.has_errors()) {
            // Make sure that submission set is unique.
            rov.validateSubmissionSetUniqueIds(submittedMetadata);

            // Validate any document unique ids.
            rov.validateDocumentUniqueIds(submittedMetadata);

            // Make sure registry knows about the patient id.
            rov.validatePatientId(submittedMetadata, configActor);
            rov.validateConsistentPatientId(true, submittedMetadata);
            validationStatus = true;
        }
        return validationStatus;
    }

    /**
     *
     * @param muCommands
     * @return
     * @throws XdsException
     */
    private boolean runMetadataUpdateCommands(List<MetadataUpdateCommand> muCommands) throws XdsException {
        // TBD: Do we need to order commands?
        boolean runStatus = this.runValidations(muCommands);
        if (runStatus) {
            runStatus = this.runUpdates(muCommands);
        }
        return runStatus;
    }

    /**
     *
     * @param muCommands
     * @return
     * @throws XdsException
     */
    private boolean runValidations(List<MetadataUpdateCommand> muCommands) throws XdsException {
        // TBD: Do we need to order commands?
        boolean runStatus = false;

        // Run validations.
        for (MetadataUpdateCommand muCommand : muCommands) {
            runStatus = muCommand.validate();
            if (!runStatus) {
                break;  // Get out - do not run any more commands on first failure.
            }
        }
        return runStatus;
    }

    /**
     *
     * @param muCommands
     * @return
     * @throws XdsException
     */
    private boolean runUpdates(List<MetadataUpdateCommand> muCommands) throws XdsException {
        // TBD: Do we need to order commands?
        boolean runStatus = false;

        // Run validations.
        for (MetadataUpdateCommand muCommand : muCommands) {
            runStatus = muCommand.update();
            if (!runStatus) {
                break;  // Get out - do not run any more commands on first failure.
            }
        }
        return runStatus;
    }

    /**
     *
     * @throws XdsException
     */
    private void registerSubmission() throws XdsException {
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // Now, fixup the Metadata to be submitted.
        // Change symbolic names to UUIDs.
        IdParser idParser = new IdParser(submittedMetadata);
        idParser.compileSymbolicNamesIntoUuids();
        submittedMetadata.reindex();  // Only so logging will work.

        // Log metadata (after id assignment).
        MetadataUpdateHelper.logMetadata(logMessage, submittedMetadata);

        // Submit new registry object version.
        backendRegistry.setReason("Register Submission Set");
        submittedMetadata.setStatusOnApprovableObjects();
        OMElement result = backendRegistry.submit(submittedMetadata);
        // FIXME: result?
    }

    /**
     *
     * @return
     * @throws MetadataException
     * @throws XdsException
     */
    private List<MetadataUpdateCommand> buildMetadataUpdateCommands() throws MetadataException, XdsException {
        // Technical operation cases:
        // - Update DocumentEntry Metadata
        // - Update DocumentEntry Status
        // - Update Folder Metadata
        // - Update Folder Status
        // - Update Association Status
        // - Submit new Assoication object(s)

        // Build list of commands (Command Pattern) to execute.
        List<MetadataUpdateCommand> muCommands = new ArrayList<MetadataUpdateCommand>();
        for (OMElement assoc : submittedMetadata.getAssociations()) {
            // See if we are dealing with a proper association.
            MetadataUpdateCommand muCommand = null;
            if (MetadataSupport.xdsB_ihe_assoc_type_update_availability_status.equals(submittedMetadata.getAssocType(assoc))) {
                muCommand = this.handleUpdateAvailabilityStatusAssociation(assoc);
            } else if (MetadataSupport.xdsB_eb_assoc_type_has_member.equals(submittedMetadata.getAssocType(assoc))) {
                muCommand = this.handleHasMemberAssociation(assoc);
            } else if (MetadataSupport.xdsB_ihe_assoc_type_submit_association.equals(submittedMetadata.getAssocType(assoc))) {
                muCommand = this.handleSubmitAssociation(assoc);
            } else {
                // Do nothing.
            }
            if (muCommand != null) {
                muCommands.add(muCommand);
            }
        }
        if (muCommands.isEmpty()) {
            // FIXME: Use proper exception.
            throw new XdsException("No trigger event detected - No updates made to registry");
        }
        return muCommands;
    }

    /**
     * 
     * @param assoc
     * @return
     * @throws MetadataException
     */
    private MetadataUpdateCommand handleHasMemberAssociation(OMElement assoc) throws MetadataException {
        MetadataUpdateCommand muCommand = null;
        String submissionSetId = submittedMetadata.getSubmissionSetId();
        String sourceObjectId = submittedMetadata.getSourceObject(assoc);
        String targetObjectId = submittedMetadata.getTargetObject(assoc);

        // See what type of target object we are dealing with.
        String targetObjectType = submittedMetadata.getObjectTypeById(targetObjectId);
        OMElement submittedRegistryObject = submittedMetadata.getObjectById(targetObjectId);
        if (sourceObjectId.equals(submissionSetId)) {
            // See if we have a PreviousVersion slot name.
            String perviousVersion = submittedMetadata.getSlotValue(assoc, "PreviousVersion", 0);
            if (perviousVersion != null) {
                boolean associationPropagation = true;
                // See if "AssociationPropagation" slot is in request.
                String associationPropagationValueText = submittedMetadata.getSlotValue(assoc, "AssociationPropagation", 0);
                // If missing, default is "yes".
                if (associationPropagationValueText != null) {
                    if (associationPropagationValueText.equalsIgnoreCase("no")) {
                        associationPropagation = false;
                    }
                }
                if (targetObjectType.equals("Folder")) {
                    // Updating a folder.
                    MetadataUpdateCommandValidator validator = new UpdateFolderMetadataCommandValidator();
                    UpdateFolderMetadataCommand updateFolderCommand =
                            new UpdateFolderMetadataCommand(submittedMetadata, metadataUpdateContext, validator);
                    updateFolderCommand.setPreviousVersion(perviousVersion);
                    updateFolderCommand.setSubmittedRegistryObject(submittedRegistryObject);
                    updateFolderCommand.setAssociationPropagation(associationPropagation);
                    muCommand = updateFolderCommand;
                } else if (targetObjectType.equals("ExtrinsicObject")) {
                    // Updating a document.
                    MetadataUpdateCommandValidator validator = new UpdateDocumentEntryMetadataCommandValidator();
                    UpdateDocumentEntryMetadataCommand updateDocumentEntryCommand =
                            new UpdateDocumentEntryMetadataCommand(submittedMetadata, metadataUpdateContext, validator);
                    updateDocumentEntryCommand.setPreviousVersion(perviousVersion);
                    updateDocumentEntryCommand.setSubmittedRegistryObject(submittedRegistryObject);
                    updateDocumentEntryCommand.setAssociationPropagation(associationPropagation);
                    muCommand = updateDocumentEntryCommand;
                }
                // FIXME: Should make sure the association is OK.
            }
        }
        return muCommand;
    }

    /**
     *
     * @param assoc
     * @return
     * @throws MetadataException
     */
    private MetadataUpdateCommand handleUpdateAvailabilityStatusAssociation(OMElement assoc) throws MetadataException {
        String targetObjectId = submittedMetadata.getTargetObject(assoc);
        // Get "NewStatus" and "OriginalStatus".
        String newStatus = submittedMetadata.getSlotValue(assoc, "NewStatus", 0);
        String originalStatus = submittedMetadata.getSlotValue(assoc, "OriginalStatus", 0);
        MetadataUpdateCommandValidator validator = new UpdateStatusCommandValidator();
        UpdateStatusCommand updateStatusCommand =
                new UpdateStatusCommand(submittedMetadata, metadataUpdateContext, validator);
        updateStatusCommand.setNewStatus(newStatus);
        updateStatusCommand.setOriginalStatus(originalStatus);
        updateStatusCommand.setTargetObjectId(targetObjectId);
        return updateStatusCommand;
    }

    /**
     * 
     * @param assoc
     * @return
     * @throws MetadataException
     */
    private MetadataUpdateCommand handleSubmitAssociation(OMElement assoc) throws MetadataException {
        String targetObjectId = submittedMetadata.getTargetObject(assoc);
        OMElement submittedRegistryObject = submittedMetadata.getObjectById(targetObjectId);
        MetadataUpdateCommandValidator validator = new SubmitAssociationCommandValidator();
        SubmitAssociationCommand submitAssociationCommand =
                new SubmitAssociationCommand(submittedMetadata, metadataUpdateContext, validator);
        submitAssociationCommand.setSubmittedRegistryObject(submittedRegistryObject);
        submitAssociationCommand.setSubmitAssociation(assoc);
        return submitAssociationCommand;
    }
}
