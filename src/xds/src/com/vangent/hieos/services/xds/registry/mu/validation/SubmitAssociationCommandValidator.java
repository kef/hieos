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
import com.vangent.hieos.services.xds.registry.mu.command.SubmitAssociationCommand;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateHelper;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.services.xds.registry.storedquery.RegistryObjectValidator;
import com.vangent.hieos.xutil.exception.XDSPatientIDReconciliationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
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
public class SubmitAssociationCommandValidator extends MetadataUpdateCommandValidator {

    private enum RegistryObjectType {
        DOCUMENT, FOLDER
    };
    // Scratch pad area.
    private RegistryObjectType sourceRegistryObjectType;
    private RegistryObjectType targetRegistryObjectType;
    private OMElement sourceRegistryObject;
    private OMElement targetRegistryObject;
    private Metadata sourceRegistryObjectMetadata;
    private Metadata targetRegistryObjectMetadata;

    /**
     * 
     * @param metadataUpdateCommand
     */
    public SubmitAssociationCommandValidator(MetadataUpdateCommand metadataUpdateCommand) {
        super(metadataUpdateCommand);
    }

    /**
     * 
     * @throws XdsException
     */
    public boolean validate() throws XdsException {
        SubmitAssociationCommand cmd = (SubmitAssociationCommand) this.getMetadataUpdateCommand();
        boolean validationSuccess = true;

        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = cmd.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        RegistryResponse registryResponse = metadataUpdateContext.getRegistryResponse();
        XConfigActor configActor = metadataUpdateContext.getConfigActor();

        // Preconditions:
        // The following rules shall be used by the receiving actor to decode and validate a submission:
        //  1. The sourceObject and targetObject attributes of the new association reference objects
        //     already in the recipient system (implies UUID format).
        //  2. The sourceObject and targetObject shall not be deprecated.
        //  3. The sourceObject and targetObject shall not reference a SubmissionSet object.

        // FIXME: Metadata could include other associations we do not care about.

        // Run initial validations on submitted metadata.
        RegistryObjectValidator rov = new RegistryObjectValidator(registryResponse, logMessage, backendRegistry);
        Metadata submittedMetadata = cmd.getSubmittedMetadata();
        rov.validatePatientId(submittedMetadata, configActor);

        OMElement submittedAssoc = cmd.getSubmittedRegistryObject();
        String sourceObjectId = submittedMetadata.getAssocSource(submittedAssoc);
        String targetObjectId = submittedMetadata.getAssocTarget(submittedAssoc);

        // Make sure source and target are in UUID format.
        this.validateRegistryObjectIds(sourceObjectId, targetObjectId);

        // Prepare to issue registry queries.
        MetadataUpdateStoredQuerySupport muSQ = metadataUpdateContext.getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);

        // Get metadata for source/target objects (updates scratch pad).
        this.queryMetadata(cmd, muSQ, sourceObjectId, targetObjectId);

        // Validate that source and target objects have an APPROVED status.
        this.validateRegistryObjectsApprovedStatus();

        // Validate that the source and target objects have the same PID.
        this.validateRegistryObjectPatientIds();

        // Validate proper association type has been submitted.
        //   HasMember is only allowed for Folder->Document.
        //   Others are allowed for Document->Document
        this.validateAssociationType(submittedAssoc);

        // Validate that there is not(already) an association between the 2 objects with the same(submitted) association type.
        this.validateSubmittedAssocDoesNotExist(muSQ, submittedMetadata, submittedAssoc, sourceObjectId, targetObjectId);

        // TBD: Run further validations.
        // TBD: Validate status of association is valid (or did this happen before)?
        // FIXME: Should we deal with current assoc status also here?
        return validationSuccess;
    }

    /**
     * 
     * @param cmd
     * @param muSQ
     * @param sourceObjectId
     * @param targetObjectId
     * @throws XdsException
     */
    private void queryMetadata(MetadataUpdateCommand cmd, MetadataUpdateStoredQuerySupport muSQ, String sourceObjectId, String targetObjectId) throws XdsException {
        // Get metadata for source object.
        sourceRegistryObjectType = RegistryObjectType.DOCUMENT;
        sourceRegistryObjectMetadata = cmd.getDocumentMetadata(muSQ, sourceObjectId);
        if (sourceRegistryObjectMetadata == null) {
            // Try to find folder.
            sourceRegistryObjectType = RegistryObjectType.FOLDER;
            sourceRegistryObjectMetadata = cmd.getFolderMetadata(muSQ, sourceObjectId);
        }
        if (sourceRegistryObjectMetadata == null) {
            throw new XdsException("Can not find source registry object (document or folder) for UUID = " + sourceObjectId);
        }

        // Get metadata for target object.
        targetRegistryObjectType = RegistryObjectType.DOCUMENT;
        targetRegistryObjectMetadata = cmd.getDocumentMetadata(muSQ, targetObjectId);
        if (targetRegistryObjectMetadata == null) {
            // Try to find folder.
            targetRegistryObjectType = RegistryObjectType.FOLDER;
            targetRegistryObjectMetadata = cmd.getFolderMetadata(muSQ, targetObjectId);
        }
        if (targetRegistryObjectMetadata == null) {
            throw new XdsException("Can not find target registry object (document or folder) for UUID = " + targetObjectId);
        }

        // Make sure that the target object is not a folder.

        // Valid source/target combinations include:
        //   source = DOCUMENT AND target = DOCUMENT
        //   source = FOLDER AND target = DOCUMENT

        // Invalid source/target combinations include:
        //   source = FOLDER AND target = FOLDER
        //   source = DOCUMENT AND target = FOLDER
        if (targetRegistryObjectType.equals(RegistryObjectType.FOLDER)) {
            throw new XdsException("Target registry object is a folder and is not allowed.  Folder UUID = "
                    + targetObjectId);
        }

        // Set source and target registry objects.
        if (sourceRegistryObjectType.equals(RegistryObjectType.DOCUMENT)) {
            sourceRegistryObject = sourceRegistryObjectMetadata.getExtrinsicObject(0);
        } else {
            // A folder.
            sourceRegistryObject = sourceRegistryObjectMetadata.getFolder(0);
        }
        if (targetRegistryObjectType.equals(RegistryObjectType.DOCUMENT)) {
            targetRegistryObject = targetRegistryObjectMetadata.getExtrinsicObject(0);
        } else {
            // Should never get here.
            // A folder.
            targetRegistryObject = targetRegistryObjectMetadata.getFolder(0);
        }
    }

    /**
     *
     * @param sourceObjectId
     * @param targetObjectId
     * @throws XdsException
     */
    private void validateRegistryObjectIds(String sourceObjectId, String targetObjectId) throws XdsException {
        if (!MetadataUpdateHelper.isUUID(sourceObjectId)) {
            throw new XdsException("Source registry object is not in UUID format");
        }
        if (!MetadataUpdateHelper.isUUID(targetObjectId)) {
            throw new XdsException("Target registry object is not in UUID format");
        }
        // Make sure that both UUIDs are not the same.
        if (sourceObjectId.equals(targetObjectId)) {
            throw new XdsException("Source and target registry objects can not be the same");
        }
    }

    /**
     *
     * @throws XdsException
     */
    private void validateRegistryObjectPatientIds() throws XdsException {
        String sourcePatientId = sourceRegistryObjectMetadata.getPatientId(sourceRegistryObject);
        String targetPatientId = targetRegistryObjectMetadata.getPatientId(targetRegistryObject);
        if (!sourcePatientId.equals(targetPatientId)) {
            throw new XDSPatientIDReconciliationException("Source registry object's patient identifier = "
                    + sourcePatientId
                    + " does not match target registry object's patient identifier = "
                    + targetPatientId);
        }
    }

    /**
     *
     * @throws XdsException
     */
    private void validateRegistryObjectsApprovedStatus() throws XdsException {
        String sourceStatus = sourceRegistryObjectMetadata.getStatus(sourceRegistryObject);
        if (!sourceStatus.equals(MetadataSupport.status_type_approved)) {
            throw new XdsException("Source registry object found but status is not approved.  Status in registry = " + sourceStatus);
        }
        String targetStatus = targetRegistryObjectMetadata.getStatus(targetRegistryObject);
        if (!targetStatus.equals(MetadataSupport.status_type_approved)) {
            throw new XdsException("Target registry object found but status is not approved.  Status in registry = " + targetStatus);
        }
    }

    /**
     *
     * @param submittedAssoc
     * @throws XdsException
     */
    private void validateAssociationType(OMElement submittedAssoc) throws XdsException {
        Metadata m = this.getMetadataUpdateCommand().getSubmittedMetadata();
        String assocType = m.getAssocType(submittedAssoc);
        if (sourceRegistryObjectType.equals(RegistryObjectType.FOLDER)) {
            if (!assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
                throw new XdsException("Source registry object is a folder and only valid assocation type = "
                        + MetadataSupport.xdsB_eb_assoc_type_has_member);
            }
        } else {
            //boolean foundValidAssocType = false;
            boolean foundValidAssocType = Metadata.isValidDocumentAssociationType(assocType);
            //for (String validDocumentToDocumentAssocType : validDocumentToDocumentAssocTypes) {
            //    if (assocType.equals(validDocumentToDocumentAssocType)) {
            //        foundValidAssocType = true;
            //        break;
            //    }
            //}
            if (!foundValidAssocType) {
                throw new XdsException("Source registry object is a document and valid assocation type not provided.");
            }
        }
    }

    /**
     *
     * @param muSQ
     * @param submittedMetadata
     * @param submittedAssoc
     * @param sourceObjectId
     * @param targetObjectId
     * @throws XdsException
     */
    private void validateSubmittedAssocDoesNotExist(MetadataUpdateStoredQuerySupport muSQ, Metadata submittedMetadata,
            OMElement submittedAssoc, String sourceObjectId, String targetObjectId) throws XdsException {
        String assocType = submittedMetadata.getAssocType(submittedAssoc);
        List<String> sourceOrTargetIds = new ArrayList<String>();
        sourceOrTargetIds.add(sourceObjectId);
        sourceOrTargetIds.add(targetObjectId);
        List<String> assocTypes = new ArrayList<String>();
        assocTypes.add(assocType);
        OMElement assocQueryResult = muSQ.getAssociations(sourceOrTargetIds, null /* status */, assocTypes /* types */);
        Metadata assocMetadata = MetadataParser.parseNonSubmission(assocQueryResult);
        if (assocMetadata.getAssociationIds().size() > 0) {
            throw new XdsException("Registry already has an association between the source and target registry objects of type = "
                    + assocType);
        }
    }
}
