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
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateHelper.RegistryObjectType;
import com.vangent.hieos.services.xds.registry.mu.validation.MetadataUpdateCommandValidator;
import com.vangent.hieos.services.xds.registry.mu.validation.UpdateStatusCommandValidator;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.IdParser;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 * 
 * @author Bernie Thuman
 */
public class UpdateStatusCommand extends MetadataUpdateCommand {

    private RegistryObjectType currentRegistryObjectType;
    private String targetObjectId;
    private String newStatus;
    private String originalStatus;
    private Metadata currentMetadata;
    private OMElement currentRegistryObject;

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public UpdateStatusCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        super(metadata, metadataUpdateContext);
    }

    /**
     *
     * @return
     */
    public RegistryObjectType getCurrentRegistryObjectType() {
        return currentRegistryObjectType;
    }

    /**
     *
     * @param currentRegistryObjectType
     */
    public void setCurrentRegistryObjectType(RegistryObjectType currentRegistryObjectType) {
        this.currentRegistryObjectType = currentRegistryObjectType;
    }

    /**
     *
     * @return
     */
    public String getTargetObjectId() {
        return targetObjectId;
    }

    /**
     *
     * @param targetObjectId
     */
    public void setTargetObjectId(String targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    /**
     *
     * @return
     */
    public String getNewStatus() {
        return newStatus;
    }

    /**
     *
     * @param newStatus
     */
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    /**
     *
     * @return
     */
    public String getOriginalStatus() {
        return originalStatus;
    }

    /**
     *
     * @param originalStatus
     */
    public void setOriginalStatus(String originalStatus) {
        this.originalStatus = originalStatus;
    }

    /**
     *
     * @return
     */
    public Metadata getCurrentMetadata() {
        return currentMetadata;
    }

    /**
     *
     * @param loadedMetadata
     */
    public void setCurrentMetadata(Metadata loadedMetadata) {
        this.currentMetadata = loadedMetadata;
    }

    /**
     *
     * @return
     */
    public OMElement getCurrentRegistryObject() {
        return currentRegistryObject;
    }

    /**
     * 
     * @param currentRegistryObject
     */
    public void setCurrentRegistryObject(OMElement currentRegistryObject) {
        this.currentRegistryObject = currentRegistryObject;
    }

    /**
     * 
     * @return
     */
    @Override
    protected MetadataUpdateCommandValidator getCommandValidator() {
        return new UpdateStatusCommandValidator(this);
    }

    /**
     *
     * @param validator
     * @return
     * @throws XdsException
     */
    @Override
    protected boolean execute(MetadataUpdateCommandValidator validator) throws XdsException {
        XLogMessage logMessage = this.getMetadataUpdateContext().getLogMessage();
        String targetObjectId = this.getTargetObjectId();
        if (logMessage.isLogEnabled()) {
            //Metadata currentMetadata = this.getCurrentMetadata();
            switch (this.getCurrentRegistryObjectType()) {
                case DOCUMENT:
                    logMessage.addOtherParam("Document Updated", targetObjectId);
                    break;
                case FOLDER:
                    logMessage.addOtherParam("Folder Updated", targetObjectId);
                    break;
                case ASSOCIATION:
                    logMessage.addOtherParam("Association Updated", targetObjectId);
                    break;
            }
        }
        this.updateRegistryObjectStatus(targetObjectId, this.newStatus);
        this.registerSubmission(this.getSubmittedMetadata());
        return true; // Success.
    }

    /**
     * 
     * @param submittedMetadata
     * @throws XdsException
     */
    private void registerSubmission(Metadata submittedMetadata) throws XdsException {
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        // Now, fixup the Metadata to be submitted.
        // Change symbolic names to UUIDs.
        IdParser idParser = new IdParser(submittedMetadata);
        idParser.compileSymbolicNamesIntoUuids();
        submittedMetadata.reindex();

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
     * @param objectId
     * @param status
     * @throws XdsInternalException
     */
    private void updateRegistryObjectStatus(String objectId, String status) throws XdsInternalException {
        BackendRegistry backendRegistry = this.getMetadataUpdateContext().getBackendRegistry();
        List<String> objectIds = new ArrayList<String>();
        objectIds.add(objectId);
        OMElement result = backendRegistry.submitSetStatusOnObjectsRequest(objectIds, status);
        // FIXME: Deal with response!!
    }
}
