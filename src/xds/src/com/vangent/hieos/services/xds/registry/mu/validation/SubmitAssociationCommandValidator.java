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
package com.vangent.hieos.services.xds.registry.mu;

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
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
public class SubmitAssociationCommandValidator extends UpdateDocumentSetCommandValidator {

    private enum RegistryObjectType {
        DOCUMENT, FOLDER
    };

    private static final String[] validDocumentToDocumentAssocTypes = {
        MetadataSupport.xdsB_ihe_assoc_type_rplc,
        MetadataSupport.xdsB_ihe_assoc_type_xfrm,
        MetadataSupport.xdsB_ihe_assoc_type_apnd,
        MetadataSupport.xdsB_ihe_assoc_type_xfrm_rplc,
        MetadataSupport.xdsB_ihe_assoc_type_signs
    };
    //private static final String[] validFolderToDocumentAssocTypes = {
    //    MetadataSupport.xdsB_eb_assoc_type_has_member
    //};

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

        // Prepare to issue registry query.
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                metadataUpdateContext.getRegistryResponse(), logMessage,
                metadataUpdateContext.getBackendRegistry());
        muSQ.setReturnLeafClass(true);

        Metadata submittedMetadata = cmd.getMetadata();

        // FIXME: Metadata could include other associations we do not care about.
        OMElement submittedAssoc = cmd.getTargetObject();
        String sourceObjectUUID = submittedMetadata.getAssocSource(submittedAssoc);
        String targetObjectUUID = submittedMetadata.getAssocTarget(submittedAssoc);

        // Make sure source and target are in UUID format.
        this.validateRegistryObjectIds(sourceObjectUUID, targetObjectUUID);

        // Get metadata for source/target objects (updates scratch pad).
        this.queryMetadata(muSQ, sourceObjectUUID, targetObjectUUID);

        // Validate that source and target objects have an APPROVED status.
        this.validateRegistryObjectsApprovedStatus();

        // Validate that the source and target objects have the same PID.
        this.validateRegistryObjectPatientIds();

        // Validate proper association type has been submitted.
        //   HasMember is only allowed for Folder->Document.
        //   Others are allowed for Document->Document
        this.validateAssociationType(submittedAssoc);

        // Validate that there is not(already) an association between the 2 objects with the same(submitted) association type.
        this.validateSubmittedAssocDoesNotExist(muSQ, submittedMetadata, submittedAssoc, sourceObjectUUID, targetObjectUUID);

        // FIXME?? Validate the submitted submission set along with its contained content.
        RegistryObjectValidator rov = new RegistryObjectValidator(registryResponse, logMessage, backendRegistry);
        rov.validateStructure(submittedMetadata, true /* isSubmit */, registryResponse.registryErrorList);
        if (registryResponse.has_errors()) {
            validationSuccess = false;
        } else {
            // TBD: Run further validations.
            // Run further validations.
            rov.validateSubmissionSetUniqueIds(submittedMetadata);
            rov.validatePatientId(submittedMetadata, configActor);
            // TBD: Validate status of association is valid (or did this happen before)?
            // FIXME: Should we deal with current assoc status also here?
        }
        return validationSuccess;
    }

    /**
     *
     * @param muSQ
     * @param sourceObjectUUID
     * @param targetObjectUUID
     * @throws XdsException
     */
    private void queryMetadata(MetadataUpdateStoredQuerySupport muSQ, String sourceObjectUUID, String targetObjectUUID) throws XdsException {
        // Get metadata for source object.
        sourceRegistryObjectType = RegistryObjectType.DOCUMENT;
        sourceRegistryObjectMetadata = this.getDocumentMetadata(muSQ, sourceObjectUUID);
        if (sourceRegistryObjectMetadata == null) {
            // Try to find folder.
            sourceRegistryObjectType = RegistryObjectType.FOLDER;
            sourceRegistryObjectMetadata = this.getFolderMetadata(muSQ, sourceObjectUUID);
        }
        if (sourceRegistryObjectMetadata == null) {
            throw new XdsException("Can not find source registry object (document or folder) for UUID = " + sourceObjectUUID);
        }

        // Get metadata for target object.
        targetRegistryObjectType = RegistryObjectType.DOCUMENT;
        targetRegistryObjectMetadata = this.getDocumentMetadata(muSQ, targetObjectUUID);
        if (targetRegistryObjectMetadata == null) {
            // Try to find folder.
            targetRegistryObjectType = RegistryObjectType.FOLDER;
            targetRegistryObjectMetadata = this.getFolderMetadata(muSQ, targetObjectUUID);
        }
        if (targetRegistryObjectMetadata == null) {
            throw new XdsException("Can not find target registry object (document or folder) for UUID = " + targetObjectUUID);
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
                    + targetObjectUUID);
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
     * @param sourceObjectUUID
     * @param targetObjectUUID
     * @throws XdsException
     */
    private void validateRegistryObjectIds(String sourceObjectUUID, String targetObjectUUID) throws XdsException {
        if (!MetadataUpdateHelper.isUUID(sourceObjectUUID)) {
            throw new XdsException("Source registry object is not in UUID format");
        }
        if (!MetadataUpdateHelper.isUUID(targetObjectUUID)) {
            throw new XdsException("Target registry object is not in UUID format");
        }
        // Make sure that both UUIDs are not the same.
        if (sourceObjectUUID.equals(targetObjectUUID)) {
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
        Metadata m = this.getMetadataUpdateCommand().getMetadata();
        String assocType = m.getAssocType(submittedAssoc);
        if (sourceRegistryObjectType.equals(RegistryObjectType.FOLDER)) {
            if (!assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
                throw new XdsException("Source registry object is a folder and only valid assocation type = "
                        + MetadataSupport.xdsB_eb_assoc_type_has_member);
            }
        } else {
            boolean foundValidAssocType = false;
            for (String validDocumentToDocumentAssocType : validDocumentToDocumentAssocTypes) {
                if (assocType.equals(validDocumentToDocumentAssocType)) {
                    foundValidAssocType = true;
                    break;
                }
            }
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
     * @param sourceObjectUUID
     * @param targetObjectUUID
     * @throws XdsException
     */
    private void validateSubmittedAssocDoesNotExist(MetadataUpdateStoredQuerySupport muSQ, Metadata submittedMetadata,
            OMElement submittedAssoc, String sourceObjectUUID, String targetObjectUUID) throws XdsException {
        String assocType = submittedMetadata.getAssocType(submittedAssoc);
        List<String> sourceOrTargetIds = new ArrayList<String>();
        sourceOrTargetIds.add(sourceObjectUUID);
        sourceOrTargetIds.add(targetObjectUUID);
        List<String> assocTypes = new ArrayList<String>();
        assocTypes.add(assocType);
        OMElement assocQueryResult = muSQ.getAssociations(sourceOrTargetIds, null /* status */, assocTypes /* types */);
        Metadata assocMetadata = MetadataParser.parseNonSubmission(assocQueryResult);
        if (assocMetadata.getAssociationIds().size() > 0) {
            throw new XdsException("Registry already has an association between the source and target registry objects of type = "
                    + assocType);
        }
    }

    /**
     *
     * @param muSQ
     * @param uuid
     * @return
     * @throws XdsException
     */
    private Metadata getDocumentMetadata(MetadataUpdateStoredQuerySupport muSQ, String uuid) throws XdsException {
        OMElement queryResult = muSQ.getDocumentByUUID(uuid);
        Metadata m = MetadataParser.parseNonSubmission(queryResult);
        if (!m.getExtrinsicObjects().isEmpty()) {
            return m;
        }
        return null;
    }

    /**
     *
     * @param muSQ
     * @param uuid
     * @return
     * @throws XdsException
     */
    private Metadata getFolderMetadata(MetadataUpdateStoredQuerySupport muSQ, String uuid) throws XdsException {
        OMElement queryResult = muSQ.getFolderByUUID(uuid);
        Metadata m = MetadataParser.parseNonSubmission(queryResult);
        if (!m.getFolders().isEmpty()) {
            return m;
        }
        return null;
    }
}
