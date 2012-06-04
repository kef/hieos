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

import com.vangent.hieos.services.xds.registry.mu.command.UpdateDocumentEntryMetadataCommand;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateHelper;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.xutil.exception.XDSMetadataVersionException;
import com.vangent.hieos.xutil.exception.XDSNonIdenticalHashException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateDocumentEntryMetadataCommandValidator extends MetadataUpdateCommandValidator {

    /**
     *
     */
    public UpdateDocumentEntryMetadataCommandValidator() {
    }

    /**
     * 
     * @return 
     * @throws XdsException
     */
    public boolean validate() throws XdsException {
        UpdateDocumentEntryMetadataCommand cmd = (UpdateDocumentEntryMetadataCommand) this.getMetadataUpdateCommand();
        boolean validationSuccess = true;

        //
        // Look for an existing document that 1) matches the lid, 2) status is "Approved"
        // and 3) matches the previous version.
        //
        this.getCurrentRegistryObject(cmd);

        // Run validations.
        Metadata submittedMetadata = cmd.getSubmittedMetadata();
        this.validateUniqueIdMatch(cmd.getSubmittedRegistryObject(), submittedMetadata, cmd.getCurrentRegistryObject(), cmd.getCurrentMetadata());
        this.validateRepositoryUniqueIdMatch(cmd);
        this.validateHashAndSizeMatch(cmd);
        this.validateObjectTypeMatch(cmd);

        return validationSuccess;
    }

    /**
     *
     * @param cmd
     * @throws XdsException
     */
    private void getCurrentRegistryObject(UpdateDocumentEntryMetadataCommand cmd) throws XdsException {
        MetadataUpdateStoredQuerySupport muSQ = cmd.getMetadataUpdateContext().getStoredQuerySupport();
        Metadata submittedMetadata = cmd.getSubmittedMetadata();
        OMElement submittedRegistryObject = cmd.getSubmittedRegistryObject();
        String previousVersion = cmd.getPreviousVersion();
        String lid = submittedMetadata.getLID(submittedRegistryObject);
        if (!MetadataUpdateHelper.isUUID(lid)) {
            throw new XdsException("LID is not in UUID format");
        }

        //
        // Look for an existing document that 1) matches the lid, 2) status is "Approved"
        // and 3) matches the previous version.
        //
        muSQ.setReturnLeafClass(true);
        muSQ.setReason("Locate Previous Approved Document (by LID/Version)");
        OMElement queryResult = muSQ.getDocumentsByLID(lid, MetadataSupport.status_type_approved, previousVersion);
        muSQ.setReason("");

        // Convert response into Metadata instance.
        Metadata currentMetadata = MetadataParser.parseNonSubmission(queryResult);
        List<OMElement> currentDocuments = currentMetadata.getExtrinsicObjects();
        if (currentDocuments.isEmpty()) {
            throw new XDSMetadataVersionException("Existing approved document entry not found for lid="
                    + lid + ", version=" + previousVersion);
        } else if (currentDocuments.size() > 1) {
            throw new XDSMetadataVersionException("> 1 approved document entry found for lid="
                    + lid + ", version=" + previousVersion);
        }

        // Fall through: we found a document that matches.

        // Set current metadata and registry object in command instance.
        cmd.setCurrentMetadata(currentMetadata);
        cmd.setCurrentRegistryObject(currentMetadata.getExtrinsicObject(0));
    }

    /**
     * 
     * @param cmd
     * @throws XdsException
     */
    private void validateRepositoryUniqueIdMatch(UpdateDocumentEntryMetadataCommand cmd) throws XdsException {
        Metadata submittedMetadata = cmd.getSubmittedMetadata();
        OMElement submittedDocumentEntry = cmd.getSubmittedRegistryObject();
        Metadata currentMetadata = cmd.getCurrentMetadata();
        OMElement currentDocumentEntry = cmd.getCurrentRegistryObject();
        String currentDocumentRepositoryUniqueId = currentMetadata.getSlotValue(currentDocumentEntry, "repositoryUniqueId", 0);
        String submittedDocumentRepositoryUniqueId = submittedMetadata.getSlotValue(submittedDocumentEntry, "repositoryUniqueId", 0);
        if (!currentDocumentRepositoryUniqueId.equals(submittedDocumentRepositoryUniqueId)) {
            throw new XdsException("Submitted document and current document 'repositoryUniqueId' values do not match");
        }
    }

    /**
     *
     * @param cmd
     * @throws XdsException
     */
    private void validateHashAndSizeMatch(UpdateDocumentEntryMetadataCommand cmd) throws XdsException {
        // NOTE (BHT): I believe that hash validation is already handled in the RegistryObjectValidator
        // but leaving here anyway.
        Metadata submittedMetadata = cmd.getSubmittedMetadata();
        OMElement submittedDocumentEntry = cmd.getSubmittedRegistryObject();
        Metadata currentMetadata = cmd.getCurrentMetadata();
        OMElement currentDocumentEntry = cmd.getCurrentRegistryObject();

        // Validate that current document hash = submitted document hash
        String currentDocumentHash = currentMetadata.getSlotValue(currentDocumentEntry, "hash", 0);
        String submittedDocumentHash = submittedMetadata.getSlotValue(submittedDocumentEntry, "hash", 0);
        if (!currentDocumentHash.equals(submittedDocumentHash)) {
            throw new XDSNonIdenticalHashException("Submitted document and current document 'hash' value do not match");
        }

        // Validate that current document size = submitted document size
        String currentDocumentSize = currentMetadata.getSlotValue(currentDocumentEntry, "size", 0);
        String submittedDocumentSize = submittedMetadata.getSlotValue(submittedDocumentEntry, "size", 0);
        if (!currentDocumentSize.equals(submittedDocumentSize)) {
            throw new XdsException("Submitted document and current document 'size' value do not match");
        }
    }

    /**
     *
     * @param cmd
     * @throws XdsException
     */
    private void validateObjectTypeMatch(UpdateDocumentEntryMetadataCommand cmd) throws XdsException {
        OMElement currentDocumentEntry = cmd.getCurrentRegistryObject();
        OMElement submittedDocumentEntry = cmd.getSubmittedRegistryObject();

        // Validate that current document type = submitted document type
        String currentDocumentObjectType = currentDocumentEntry.getAttributeValue(MetadataSupport.object_type_qname);
        String submittedDocumentObjectType = submittedDocumentEntry.getAttributeValue(MetadataSupport.object_type_qname);
        if (!currentDocumentObjectType.equals(submittedDocumentObjectType)) {
            throw new XdsException("Submitted document and current document 'objectType' value do not match");
        }
    }
}
