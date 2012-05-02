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

    /**
     *
     * @param submittedMetadata
     * @param metadataUpdateContext
     */
    public MetadataUpdateCommand(Metadata submittedMetadata, MetadataUpdateContext metadataUpdateContext) {
        this.submittedMetadata = submittedMetadata;
        this.metadataUpdateContext = metadataUpdateContext;
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
     * @throws XdsException
     */
    public boolean run() throws XdsException {
        // FIXME: Probably can't put here (since a transaction can include > 1 command).
        XLogMessage logMessage = this.getMetadataUpdateContext().getLogMessage();
        if (logMessage.isLogEnabled()) {
            String className = this.getClass().getSimpleName();
            logMessage.setTestMessage("MU." + className);
            logMessage.addOtherParam("Command", className);
        }
        MetadataUpdateCommandValidator validator = this.getCommandValidator();
        boolean runStatus = validator.validate();
        if (runStatus) {
            runStatus = this.execute(validator);
        }
        return runStatus;
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
     * @param registryObjectEntryId
     * @return
     * @throws XdsException
     */
    public Metadata getApprovedHasMemberAssocs(String registryObjectEntryId) throws XdsException {
        return this.getAssocs(registryObjectEntryId,
                MetadataSupport.status_type_approved,
                MetadataSupport.xdsB_eb_assoc_type_has_member, "Get Approved HasMember Associations");
    }

    /**
     *
     * @param registryObjectEntryId
     * @return
     * @throws XdsException
     */
    public Metadata getApprovedAssocs(String registryObjectEntryId) throws XdsException {
        return this.getAssocs(registryObjectEntryId,
                MetadataSupport.status_type_approved,
                null, "Get Approved Associations");
    }

    /**
     * 
     * @param registryObjectEntryId
     * @param status
     * @param assocType
     * @param reason
     * @return
     * @throws XdsException
     */
    public Metadata getAssocs(String registryObjectEntryId, String status, String assocType, String reason) throws XdsException {
        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        backendRegistry.setReason(reason);

        // Prepare for queries.
        MetadataUpdateStoredQuerySupport muSQ = metadataUpdateContext.getStoredQuerySupport();
        muSQ.setReturnLeafClass(true);

        // Look for associations that have registryObjectEntryId as source or target.
        List<String> sourceOrTargetIds = new ArrayList<String>();
        sourceOrTargetIds.add(registryObjectEntryId);

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
            assocTypes.add(MetadataSupport.xdsB_eb_assoc_type_has_member);
        }

        // Run query.
        OMElement assocQueryResult = muSQ.getAssociations(sourceOrTargetIds, assocStatusValues, assocTypes);

        // Convert response into Metadata instance.
        Metadata assocMetadata = MetadataParser.parseNonSubmission(assocQueryResult);
        return assocMetadata;
    }

    /**
     * 
     * @return
     * @throws XdsException
     */
    abstract protected boolean execute(MetadataUpdateCommandValidator validator) throws XdsException;

    /**
     *
     * @return
     * @throws XdsException
     */
    abstract protected MetadataUpdateCommandValidator getCommandValidator();
}
