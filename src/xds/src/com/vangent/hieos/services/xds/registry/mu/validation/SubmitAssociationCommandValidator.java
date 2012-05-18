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
import com.vangent.hieos.services.xds.registry.mu.command.SubmitAssociationCommand;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateHelper;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateHelper.RegistryObjectType;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.xutil.exception.XDSPatientIDReconciliationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class SubmitAssociationCommandValidator extends MetadataUpdateCommandValidator {

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
     * @return
     * @throws XdsException
     */
    public boolean validate() throws XdsException {
        SubmitAssociationCommand cmd = (SubmitAssociationCommand) this.getMetadataUpdateCommand();
        boolean validationSuccess = true;

        // Preconditions:
        // The following rules shall be used by the receiving actor to decode and validate a submission:
        //  1. The sourceObject and targetObject attributes of the new association reference objects
        //     already in the recipient system (implies UUID format).
        //  2. The sourceObject and targetObject shall not be deprecated.
        //  3. The sourceObject and targetObject shall not reference a SubmissionSet object.

        Metadata submittedMetadata = cmd.getSubmittedMetadata();
        OMElement submittedAssoc = cmd.getSubmittedRegistryObject();
        String sourceId = submittedMetadata.getAssocSource(submittedAssoc);
        String targetId = submittedMetadata.getAssocTarget(submittedAssoc);

        // Make sure source and target are in UUID format.
        this.validateRegistryObjectIds(sourceId, targetId);

        // Get metadata for source/target objects (updates scratch pad).
        this.getCurrentRegistryObjects(cmd, sourceId, targetId);

        // Validate that source and target objects have an APPROVED status.
        this.validateRegistryObjectsApprovedStatus();

        // Validate that the source and target objects have the same PID.
        this.validateRegistryObjectPatientIds();

        // Validate proper association type has been submitted.
        //   HasMember is only allowed for Folder->Document.
        //   Others are allowed for Document->Document
        this.validateAssociationType(submittedAssoc);

        // Validate that there is not(already) an association between the 2 objects with the same(submitted) association type.
        this.validateSubmittedAssocDoesNotExist(cmd, submittedMetadata, submittedAssoc, sourceId, targetId);

        return validationSuccess;
    }

    /**
     * 
     * @param cmd
     * @param sourceId
     * @param targetId
     * @throws XdsException
     */
    private void getCurrentRegistryObjects(MetadataUpdateCommand cmd, String sourceId, String targetId) throws XdsException {
        MetadataUpdateStoredQuerySupport muSQ = cmd.getMetadataUpdateContext().getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);

        // Get metadata for source object.
        sourceRegistryObjectType = RegistryObjectType.DOCUMENT;
        sourceRegistryObjectMetadata = cmd.getDocumentMetadata(muSQ, sourceId);
        if (sourceRegistryObjectMetadata == null) {
            // Try to find folder.
            sourceRegistryObjectType = RegistryObjectType.FOLDER;
            sourceRegistryObjectMetadata = cmd.getFolderMetadata(muSQ, sourceId);
        }
        if (sourceRegistryObjectMetadata == null) {
            throw new XdsException("Can not find source registry object (document or folder) for UUID = " + sourceId);
        }

        // Get metadata for target object.
        targetRegistryObjectType = RegistryObjectType.DOCUMENT;
        targetRegistryObjectMetadata = cmd.getDocumentMetadata(muSQ, targetId);
        if (targetRegistryObjectMetadata == null) {
            // Try to find folder.
            targetRegistryObjectType = RegistryObjectType.FOLDER;
            targetRegistryObjectMetadata = cmd.getFolderMetadata(muSQ, targetId);
        }
        if (targetRegistryObjectMetadata == null) {
            throw new XdsException("Can not find target registry object (document or folder) for UUID = " + targetId);
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
                    + targetId);
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
     * @param sourceId
     * @param targetId
     * @throws XdsException
     */
    private void validateRegistryObjectIds(String sourceId, String targetId) throws XdsException {
        if (!MetadataUpdateHelper.isUUID(sourceId)) {
            throw new XdsException("Source registry object is not in UUID format");
        }
        if (!MetadataUpdateHelper.isUUID(targetId)) {
            throw new XdsException("Target registry object is not in UUID format");
        }
        // Make sure that both UUIDs are not the same.
        if (sourceId.equals(targetId)) {
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
        Metadata submittedMetadata = this.getMetadataUpdateCommand().getSubmittedMetadata();
        String assocType = submittedMetadata.getAssocType(submittedAssoc);
        if (sourceRegistryObjectType.equals(RegistryObjectType.FOLDER)) {
            if (!assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
                throw new XdsException("Source registry object is a folder and only valid assocation type = "
                        + MetadataSupport.xdsB_eb_assoc_type_has_member);
            }
        } else {
            // A document ..
            boolean foundValidAssocType = Metadata.isValidDocumentAssociationType(assocType);
            if (!foundValidAssocType) {
                throw new XdsException("Source registry object is a document and valid assocation type not provided.");
            }
        }
    }

    /**
     * 
     * @param cmd
     * @param submittedMetadata
     * @param submittedAssoc
     * @param sourceId
     * @param targetId
     * @throws XdsException
     */
    private void validateSubmittedAssocDoesNotExist(SubmitAssociationCommand cmd, Metadata submittedMetadata,
            OMElement submittedAssoc, String sourceId, String targetId) throws XdsException {
        // Prepare to query registry.
        MetadataUpdateStoredQuerySupport muSQ = cmd.getMetadataUpdateContext().getStoredQuerySupport();
        muSQ.setReturnLeafClass(false);

        // Source or target ids.
        List<String> sourceOrTargetIds = new ArrayList<String>();
        sourceOrTargetIds.add(sourceId);
        sourceOrTargetIds.add(targetId);

        // Association type.
        String assocType = submittedMetadata.getAssocType(submittedAssoc);
        List<String> assocTypes = new ArrayList<String>();
        assocTypes.add(assocType);

        // Query registry for matching association(s) of the given type.
        OMElement assocQueryResult = muSQ.getAssociations(sourceOrTargetIds, null /* status */, assocTypes /* types */);
        Metadata assocMetadata = MetadataParser.parseNonSubmission(assocQueryResult);
        if (assocMetadata.getObjectRefs().size() > 0) {
            throw new XdsException("Registry already has an association between the source and target registry objects of type = "
                    + assocType);
        }
    }
}
