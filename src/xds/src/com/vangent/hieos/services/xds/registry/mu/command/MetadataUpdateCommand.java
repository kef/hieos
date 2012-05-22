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
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public abstract class MetadataUpdateCommand {

    private Metadata submittedMetadata;
    private MetadataUpdateContext metadataUpdateContext;
    private MetadataUpdateCommandValidator metadataUpdateCommandValidator;

    /**
     * 
     */
    private MetadataUpdateCommand() {
        // Not allowed.
    }

    /**
     * 
     * @param submittedMetadata
     * @param metadataUpdateContext
     * @param metadataUpdateCommandValidator
     */
    public MetadataUpdateCommand(
            Metadata submittedMetadata, MetadataUpdateContext metadataUpdateContext,
            MetadataUpdateCommandValidator metadataUpdateCommandValidator) {
        this.submittedMetadata = submittedMetadata;
        this.metadataUpdateContext = metadataUpdateContext;
        this.metadataUpdateCommandValidator = metadataUpdateCommandValidator;
        metadataUpdateCommandValidator.setMetadataUpdateCommand(this);  // Link.
    }

    /**
     *
     * @return
     */
    public Metadata getSubmittedMetadata() {
        return submittedMetadata;
    }

    /**
     *
     * @return
     */
    public MetadataUpdateContext getMetadataUpdateContext() {
        return metadataUpdateContext;
    }

    /**
     *
     * @return
     */
    public MetadataUpdateCommandValidator getMetadataUpdateCommandValidator() {
        return metadataUpdateCommandValidator;
    }

    /**
     * 
     * @return
     * @throws XdsException
     */
    public boolean run() throws XdsException {
        boolean runStatus = this.validate();
        if (runStatus) {
            runStatus = this.update();
        }
        return runStatus;
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    public boolean validate() throws XdsException {
        MetadataUpdateCommandValidator validator = this.getMetadataUpdateCommandValidator();
        return validator.validate();
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    public boolean update() throws XdsException {
        return this.executeUpdate();
    }

    /**
     *
     * @param muSQ
     * @param registryObjectId
     * @return
     * @throws XdsException
     */
    public Metadata getDocumentMetadata(MetadataUpdateStoredQuerySupport muSQ, String registryObjectId) throws XdsException {
        OMElement queryResult = muSQ.getDocumentByUUID(registryObjectId);
        Metadata m = MetadataParser.parseNonSubmission(queryResult);
        if (!m.getExtrinsicObjects().isEmpty()) {
            return m;
        }
        return null;
    }

    /**
     *
     * @param muSQ
     * @param registryObjectId
     * @return
     * @throws XdsException
     */
    public Metadata getFolderMetadata(MetadataUpdateStoredQuerySupport muSQ, String registryObjectId) throws XdsException {
        OMElement queryResult = muSQ.getFolderByUUID(registryObjectId);
        Metadata m = MetadataParser.parseNonSubmission(queryResult);
        if (!m.getFolders().isEmpty()) {
            return m;
        }
        return null;
    }

    /**
     * 
     * @param registryObjectId
     * @param leafClass
     * @return
     * @throws XdsException
     */
    public Metadata getApprovedHasMemberAssocs(String registryObjectId, boolean leafClass) throws XdsException {
        return this.getAssocs(registryObjectId,
                MetadataSupport.status_type_approved,
                MetadataSupport.xdsB_eb_assoc_type_has_member,
                leafClass, "Get Approved HasMember Associations");
    }

    /**
     * 
     * @param registryObjectId
     * @param leafClass
     * @return
     * @throws XdsException
     */
    public Metadata getApprovedAssocs(String registryObjectId, boolean leafClass) throws XdsException {
        return this.getAssocs(registryObjectId, MetadataSupport.status_type_approved,
                null /* assocType */, leafClass, "Get Approved Associations");
    }

    /**
     * 
     * @param registryObjectId
     * @param status
     * @param assocType
     * @param leafClass
     * @param reason
     * @return
     * @throws XdsException
     */
    public Metadata getAssocs(String registryObjectId,
            String status, String assocType, boolean leafClass, String reason) throws XdsException {
        // Look for associations that have registryObjectEntryId as source or target.
        List<String> sourceOrTargetIds = new ArrayList<String>();
        sourceOrTargetIds.add(registryObjectId);
        return this.getAssocs(sourceOrTargetIds, status, assocType, leafClass, reason);
    }

    /**
     *
     * @param sourceOrTargetIds
     * @param status
     * @param assocType
     * @param leafClass
     * @param reason
     * @return
     * @throws XdsException
     */
    public Metadata getAssocs(List<String> sourceOrTargetIds,
            String status, String assocType, boolean leafClass, String reason) throws XdsException {
        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        //XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        backendRegistry.setReason(reason);

        // Prepare for queries.
        MetadataUpdateStoredQuerySupport muSQ = metadataUpdateContext.getStoredQuerySupport();
        muSQ.setReturnLeafClass(leafClass);

        // Status.
        List<String> assocStatusValues = null;
        if (status != null) {
            assocStatusValues = new ArrayList<String>();
            assocStatusValues.add(status);
        }

        // Association type.
        List<String> assocTypes = null;
        if (assocType != null) {
            assocTypes = new ArrayList<String>();
            assocTypes.add(assocType);
        }

        // Run query.
        OMElement assocQueryResult = muSQ.getAssociations(sourceOrTargetIds, assocStatusValues, assocTypes);

        // Convert response into Metadata instance.
        Metadata assocMetadata = MetadataParser.parseNonSubmission(assocQueryResult);
        return assocMetadata;
    }

    /**
     *
     * @param registryObject
     * @throws XdsException
     */
    public void submitMetadataToRegistry(Metadata metadata) throws XdsException {
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // Log metadata (after id assignment).
        MetadataUpdateHelper.logMetadata(logMessage, metadata);

        // Submit new registry object version.
        backendRegistry.setReason("Submit Registry Object");
        metadata.setStatusOnApprovableObjects();
        OMElement result = backendRegistry.submit(metadata);
        // FIXME: result?
    }

    /**
     * 
     * @return
     * @throws XdsException
     */
    abstract protected boolean executeUpdate() throws XdsException;
}
