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
package com.vangent.hieos.services.xds.registry.transactions;

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.services.xds.registry.mu.MetadataUpdateCommand;
import com.vangent.hieos.services.xds.registry.mu.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.mu.SubmitAssociationCommand;
import com.vangent.hieos.services.xds.registry.mu.UpdateDocumentEntryMetadataCommand;
import com.vangent.hieos.services.xds.registry.mu.UpdateFolderMetadataCommand;
import com.vangent.hieos.services.xds.registry.mu.UpdateStatusCommand;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.exception.XDSNonIdenticalHashException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.xutil.registry.RegistryUtility;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateDocumentSetRequest extends XBaseTransaction {
    //Message context was added when trying to send audit message

    MessageContext messageContext;
    private final static Logger logger = Logger.getLogger(UpdateDocumentSetRequest.class);

    /**
     *
     * @param logMessage
     * @param messageContext
     */
    public UpdateDocumentSetRequest(XLogMessage logMessage, MessageContext messageContext) {
        this.log_message = logMessage;
        this.messageContext = messageContext;
        try {
            init(new RegistryResponse(), messageContext);
        } catch (XdsInternalException e) {
            logger.fatal(logger_exception_details(e));
        }
    }

    /**
     *
     * @param submitObjectsRequest
     * @return
     */
    public OMElement run(OMElement submitObjectsRequest) {
        try {
            submitObjectsRequest.build();
            this.handleUpdateDocumentSetRequest(submitObjectsRequest);
        } catch (XdsInternalException e) {
            response.add_error(MetadataSupport.XDSRegistryError, "XDS Internal Error: " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (XDSNonIdenticalHashException e) {
            response.add_error(MetadataSupport.XDSNonIdenticalHash, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (SchemaValidationException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError, "Schema Validation Errors: " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (MetadataException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError, e.getMessage(), this.getClass().getName(), log_message);
        } catch (MetadataValidationException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError, e.getMessage(), this.getClass().getName(), log_message);
        } catch (XdsException e) {
            response.add_error(MetadataSupport.XDSRegistryError, "XDS Error: " + e.getMessage(), this.getClass().getName(), log_message);
        }
        OMElement res = null;
        try {
            res = response.getResponse();
            this.log_response();
        } catch (XdsInternalException e) {
        }
        return res;

        // TBD: Implement
        //return submitObjectsRequest;
    }

    /**
     * 
     * @param submitObjectsRequest
     * @throws XdsInternalException
     * @throws SchemaValidationException
     */
    private void handleUpdateDocumentSetRequest(OMElement submitObjectsRequest) throws XdsInternalException, SchemaValidationException, MetadataException, MetadataValidationException, XdsException {
        // Validate input message against XML schema.

        // FIXME: MetadataTypes.METADATA_TYPE_Rb?
        RegistryUtility.schema_validate_local(submitObjectsRequest, MetadataTypes.METADATA_TYPE_Rb);

        // Get backend registry instance.
        BackendRegistry backendRegistry = new BackendRegistry(log_message);

        // Create the context.
        MetadataUpdateContext metadataUpdateContext = new MetadataUpdateContext();
        metadataUpdateContext.setBackendRegistry(backendRegistry);
        metadataUpdateContext.setLogMessage(log_message);
        metadataUpdateContext.setRegistryResponse((RegistryResponse) response);
        metadataUpdateContext.setConfigActor(this.getConfigActor());

        // Create Metadata instance for SOR.
        Metadata m = new Metadata(submitObjectsRequest);  // Create meta-data instance for SOR.

        List<MetadataUpdateCommand> muCommands = new ArrayList<MetadataUpdateCommand>();

        // Technical operation cases:
        // - Update DocumentEntry Metadata
        // - Update DocumentEntry Status
        // - Update Folder Metadata
        // - Update Folder Status
        // - Update Association Status
        // - Submit new Assoication object(s)

        String submissionSetId = m.getSubmissionSetId();
        String submissionSetObjectType = m.getObjectTypeById(submissionSetId);
        System.out.println("... submissionSetObjectType = " + submissionSetObjectType);
        for (OMElement assoc : m.getAssociations()) {
            // See if we are dealing with a proper association.
            String sourceObjectId = m.getSourceObject(assoc);
            String targetObjectId = m.getTargetObject(assoc);

            System.out.println("... sourceObjectId = " + sourceObjectId);
            System.out.println("... targetObjectId = " + targetObjectId);
            MetadataUpdateCommand muCommand = null;
            if (MetadataSupport.xdsB_ihe_assoc_type_update_availability_status.equals(m.getAssocType(assoc))) {
                muCommand = this.handleUpdateAvailabilityStatusAssociation(m, metadataUpdateContext, assoc);
            } else if (MetadataSupport.xdsB_eb_assoc_type_has_member.equals(m.getAssocType(assoc))) {
                muCommand = this.handleHasMemberAssociation(m, metadataUpdateContext, assoc);
            } else if (MetadataSupport.xdsB_ihe_assoc_type_submit_association.equals(m.getAssocType(assoc))) {
                muCommand = this.handleSubmitAssociation(m, metadataUpdateContext, assoc);
            } else {
                System.out.println("Association is not a trigger!");
            }
            if (muCommand != null) {
                muCommands.add(muCommand);
            }
        }

        boolean commitCompleted = false;
        try {
            // TBD: Do we need to order commands?
            // Execute each command.
            boolean executionSuccess = false;
            for (MetadataUpdateCommand muCommand : muCommands) {
                executionSuccess = muCommand.execute();
                if (!executionSuccess) {
                    break;  // Get out.
                }
            }
            if (executionSuccess) {
                backendRegistry.commit();
                commitCompleted = true;
            }
        } finally {
            if (!commitCompleted) {
                backendRegistry.rollback();
            }
        }
    }

    /**
     *
     * @param id
     * @return
     */
    // FIXME: Move
    private boolean isUUID(String id) {
        return id.startsWith("urn:uuid:");
    }

    /**
     *
     * @param m
     * @param metadataUpdateContext
     * @param assoc
     * @return
     * @throws MetadataException
     */
    private MetadataUpdateCommand handleHasMemberAssociation(Metadata m, MetadataUpdateContext metadataUpdateContext, OMElement assoc) throws MetadataException {
        MetadataUpdateCommand muCommand = null;

        System.out.println("HasMember association found");

        String submissionSetId = m.getSubmissionSetId();
        String submissionSetObjectType = m.getObjectTypeById(submissionSetId);
        System.out.println("... submissionSetObjectType = " + submissionSetObjectType);

        String sourceObjectId = m.getSourceObject(assoc);
        String targetObjectId = m.getTargetObject(assoc);

        System.out.println("... sourceObjectId = " + sourceObjectId);
        System.out.println("... targetObjectId = " + targetObjectId);

        // See what type of target object we are dealing with.
        String targetObjectType = m.getObjectTypeById(targetObjectId);
        System.out.println("... targetObjectType = " + targetObjectType);
        OMElement targetObject = m.getObjectById(targetObjectId);
        if (sourceObjectId.equals(submissionSetId)) {
            // See if we have a PreviousVersion slot name.
            String perviousVersion = m.getSlotValue(assoc, "PreviousVersion", 0);
            if (perviousVersion != null) {
                System.out.println("... PreviousVersion = " + perviousVersion);

                boolean associationPropagation = true;
                // See if "AssociationPropagation" slot is in request.
                String associationPropagationValueText = m.getSlotValue(assoc, "AssociationPropagation", 0);
                // If missing, default is "yes".
                if (associationPropagationValueText != null) {
                    if (associationPropagationValueText.equalsIgnoreCase("no")) {
                        associationPropagation = false;
                    }
                }
                System.out.println("... association propagation = " + associationPropagation);

                // Get logical id (lid).
                String lid = targetObject.getAttributeValue(MetadataSupport.lid_qname);
                System.out.println("... lid = " + lid);

                if (targetObjectType.equals("Folder")) {
                    UpdateFolderMetadataCommand updateFolderCommand =
                            new UpdateFolderMetadataCommand(m, metadataUpdateContext);
                    updateFolderCommand.setPreviousVersion(perviousVersion);
                    updateFolderCommand.setTargetObject(targetObject);
                    updateFolderCommand.setAssociationPropagation(associationPropagation);
                    muCommand = updateFolderCommand;
                } else if (targetObjectType.equals("ExtrinsicObject")) {
                    UpdateDocumentEntryMetadataCommand updateDocumentEntryCommand = new UpdateDocumentEntryMetadataCommand(m, metadataUpdateContext);
                    updateDocumentEntryCommand.setPreviousVersion(perviousVersion);
                    updateDocumentEntryCommand.setTargetObject(targetObject);
                    updateDocumentEntryCommand.setAssociationPropagation(associationPropagation);
                    muCommand = updateDocumentEntryCommand;
                }
            }
        }
        return muCommand;
    }

    /**
     *
     * @param m
     * @param metadataUpdateContext
     * @param assoc
     * @return
     * @throws MetadataException
     */
    private MetadataUpdateCommand handleUpdateAvailabilityStatusAssociation(Metadata m, MetadataUpdateContext metadataUpdateContext, OMElement assoc) throws MetadataException {
        MetadataUpdateCommand muCommand = null;

        System.out.println("UpdateAvailabilityStatus association found");

        String targetObjectId = m.getTargetObject(assoc);
        System.out.println("... targetObjectId = " + targetObjectId);
        // Get "NewStatus" and "OriginalStatus".
        String newStatus = m.getSlotValue(assoc, "NewStatus", 0);
        String originalStatus = m.getSlotValue(assoc, "OriginalStatus", 0);
        System.out.println("... NewStatus = " + newStatus);
        System.out.println("... OriginalStatus = " + originalStatus);
        if (!isUUID(targetObjectId)) {
            // TBD: Throw exception.
            // Should place into command?
            System.out.println("*** ERROR: " + targetObjectId + " is not in UUID format");
        }
        UpdateStatusCommand updateStatusCommand = new UpdateStatusCommand(m, metadataUpdateContext);
        updateStatusCommand.setNewStatus(newStatus);
        updateStatusCommand.setOriginalStatus(originalStatus);
        updateStatusCommand.setTargetObjectId(targetObjectId);
        muCommand = updateStatusCommand;
        return muCommand;
    }

    /**
     *
     * @param m
     * @param metadataUpdateContext
     * @param assoc
     * @return
     * @throws MetadataException
     */
    private MetadataUpdateCommand handleSubmitAssociation(Metadata m, MetadataUpdateContext metadataUpdateContext, OMElement assoc) throws MetadataException {
        MetadataUpdateCommand muCommand = null;

        System.out.println("SubmitAssociation association found");
        String targetObjectId = m.getTargetObject(assoc);
        System.out.println("... targetObjectId = " + targetObjectId);
        // See what type of target object we are dealing with.
        String targetObjectType = m.getObjectTypeById(targetObjectId);
        System.out.println("... targetObjectType = " + targetObjectType);
        OMElement targetObject = m.getObjectById(targetObjectId);
        SubmitAssociationCommand submitAssociationCommand = new SubmitAssociationCommand(m, metadataUpdateContext);
        submitAssociationCommand.setTargetObject(targetObject);
        muCommand = submitAssociationCommand;
        return muCommand;
    }
}
