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

import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.services.xds.registry.storedquery.RegistryObjectValidator;
import com.vangent.hieos.xutil.exception.XDSNonIdenticalHashException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.IdParser;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.validation.Validator.MetadataType;
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
public class UpdateDocumentEntryMetadataCommand extends UpdateRegistryObjectMetadataCommand {

    // Scratch pad area.
    private OMElement currentDocumentEntry;
    private Metadata currentMetadata;

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public UpdateDocumentEntryMetadataCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        super(metadata, metadataUpdateContext);
    }

    /**
     * 
     * @return
     * @throws XdsException
     */
    @Override
    protected boolean execute() throws XdsException {

        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // FIXME: metadata includes the targetObject, but it may contain other details
        // we do not want.
        Metadata metadata = this.getMetadata();
        OMElement targetObject = this.getTargetObject();

        // Now, fixup the Metadata to be submitted.

        // Change symbolic names to UUIDs.
        IdParser idParser = new IdParser(metadata);
        idParser.compileSymbolicNamesIntoUuids();

        // Adjust the version number (current version number + 1).
        Metadata.updateRegistryObjectVersion(targetObject, this.getPreviousVersion());

        // DEBUG:
        logMessage.addOtherParam("Version to Submit", targetObject);

        // FIXME: MetadataTypes.METADATA_TYPE_Rb?
        //RegistryUtility.schema_validate_local(submitObjectsRequest, MetadataTypes.METADATA_TYPE_Rb);

        backendRegistry.setReason("Submit New Version");
        OMElement result = backendRegistry.submit(metadata);

        // FIXME: Should approve in one shot.
        // Approve.
        ArrayList approvableObjectIds = metadata.getApprovableObjectIds();
        if (approvableObjectIds.size() > 0) {
            backendRegistry.submitApproveObjectsRequest(approvableObjectIds);
        }

        // Deprecate old.
        String currentDocumentEntryId = currentMetadata.getId(currentDocumentEntry);
        ArrayList deprecateObjectIds = new ArrayList<String>();
        deprecateObjectIds.add(currentDocumentEntryId);
        backendRegistry.submitDeprecateObjectsRequest(deprecateObjectIds);
        return true;
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    @Override
    protected boolean validate() throws XdsException {
        boolean validationSuccess = true;

        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        RegistryResponse registryResponse = metadataUpdateContext.getRegistryResponse();
        XConfigActor configActor = metadataUpdateContext.getConfigActor();

        Metadata submittedMetadata = this.getMetadata();
        OMElement targetObject = this.getTargetObject();
        String previousVersion = this.getPreviousVersion();

        // Get lid.
        String lid = targetObject.getAttributeValue(MetadataSupport.lid_qname);
        System.out.println("... lid = " + lid);

        //
        // Look for an existing document that 1) matches the lid, 2) status is "Approved"
        // and 3) matches the previous version.
        //

        // Prepare to issue registry query.
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                metadataUpdateContext.getRegistryResponse(), logMessage,
                metadataUpdateContext.getBackendRegistry());

        // Issue query.
        muSQ.setReturnLeafClass(true);
        OMElement queryResult = muSQ.getDocumentsByLID(lid, MetadataSupport.status_type_approved, previousVersion);

        // Convert response into Metadata instance.
        currentMetadata = MetadataParser.parseNonSubmission(queryResult);
        List<OMElement> currentDocuments = currentMetadata.getExtrinsicObjects();
        if (currentDocuments.isEmpty()) {
            throw new XdsException("Existing approved document entry not found for lid="
                    + lid + ", version=" + previousVersion);
        } else if (currentDocuments.size() > 1) {
            throw new XdsException("> 1 existing document entry found!");
        }

        // Fall through: we found a document that matches.
        currentDocumentEntry = currentMetadata.getExtrinsicObject(0);

        // FIXME: BEEF UP VALIDATIONS!!!!
        // Validate that the SOR is internally consistent:
        // FIXME: Should this go in the TXN?
        //Validator val = new Validator(this.getMetadata(), registryResponse.registryErrorList, true, logMessage);
        //val.run();

        // Validate the submitted submission set along with its contained content.
        RegistryObjectValidator rov = new RegistryObjectValidator(registryResponse, logMessage, backendRegistry);
        rov.validate(submittedMetadata, MetadataType.UPDATE_SUBMISSION, registryResponse.registryErrorList, configActor);
        if (registryResponse.has_errors()) {
            validationSuccess = false;
        } else {
            // Run further validations.

            // FIXME: Validate same UID
            // FIXME: Validate same REPOID

            this.validateHashAndSize(submittedMetadata);
        }
        return validationSuccess;
    }

    /**
     * 
     * @param submittedMetadata
     * @throws XdsException
     */
    private void validateHashAndSize(Metadata submittedMetadata) throws XdsException {
        // NOTE (BHT): I believe that hash validation is already handled in the RegistryObjectValidator
        // but leaving here anyway.

        // Validate that current document hash = submitted document hash
        String currentDocumentHash = currentMetadata.getSlotValue(currentDocumentEntry, "hash", 0);
        String submittedDocumentHash = submittedMetadata.getSlotValue(this.getTargetObject(), "hash", 0);
        if (!currentDocumentHash.equals(submittedDocumentHash)) {
            // FIXME: throw exceptions or add to registry response?
            throw new XDSNonIdenticalHashException("Submitted document and current document 'hash' value does not match");
        }

        // Validate that current document size = submitted document size
        String currentDocumentSize = currentMetadata.getSlotValue(currentDocumentEntry, "size", 0);
        String submittedDocumentSize = submittedMetadata.getSlotValue(this.getTargetObject(), "size", 0);
        if (!currentDocumentSize.equals(submittedDocumentSize)) {
            // FIXME: throw exceptions or add to registry response?
            throw new XdsException("Submitted document and current document 'size' value does not match");
        }
    }
}
