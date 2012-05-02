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
import com.vangent.hieos.xutil.exception.XdsException;
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
public abstract class UpdateRegistryObjectMetadataCommand extends MetadataUpdateCommand {

    private OMElement targetObject;
    private String previousVersion;
    private boolean associationPropagation;
    // Scratch pad area.
    private String targetPatientId;
    private OMElement currentRegistryObject;
    private Metadata currentMetadata;

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public UpdateRegistryObjectMetadataCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        super(metadata, metadataUpdateContext);
    }

    /**
     *
     * @return
     */
    public OMElement getTargetObject() {
        return targetObject;
    }

    /**
     *
     * @param targetObject
     */
    public void setTargetObject(OMElement targetObject) {
        this.targetObject = targetObject;
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
    public Metadata getCurrentMetadata() {
        return currentMetadata;
    }

    /**
     *
     * @param currentMetadata
     */
    public void setCurrentMetadata(Metadata currentMetadata) {
        this.currentMetadata = currentMetadata;
    }

    /**
     *
     * @return
     */
    public String getPreviousVersion() {
        return previousVersion;
    }

    /**
     *
     * @param previousVersion
     */
    public void setPreviousVersion(String previousVersion) {
        this.previousVersion = previousVersion;
    }

    /**
     * 
     * @return
     */
    public String getTargetPatientId() {
        return targetPatientId;
    }

    /**
     *
     * @param targetPatientId
     */
    public void setTargetPatientId(String targetPatientId) {
        this.targetPatientId = targetPatientId;
    }

    /**
     *
     * @return
     */
    public boolean isAssociationPropagation() {
        return associationPropagation;
    }

    /**
     *
     * @param associationPropagation
     */
    public void setAssociationPropagation(boolean associationPropagation) {
        this.associationPropagation = associationPropagation;
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    @Override
    protected boolean execute(MetadataUpdateCommandValidator validator) throws XdsException {

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

        // Log metadata (after id assignment).
        MetadataUpdateHelper.logMetadata(logMessage, metadata);

        // Adjust the version number (current version number + 1).
        Metadata.updateRegistryObjectVersion(targetObject, this.getPreviousVersion());

        // Get current/new registry object ids.
        String currentRegistryObjectId = this.getCurrentMetadata().getId(this.getCurrentRegistryObject());
        String newRegistryObjectId = metadata.getId(targetObject);

        // DEBUG:
        // logMessage.addOtherParam("Version to Submit", targetObject);

        // FIXME: MetadataTypes.METADATA_TYPE_Rb?
        //RegistryUtility.schema_validate_local(submitObjectsRequest, MetadataTypes.METADATA_TYPE_Rb);

        backendRegistry.setReason("Submit New Version");
        metadata.setStatusOnApprovableObjects();
        OMElement result = backendRegistry.submit(metadata);

        // FIXME: Should approve in one shot.
        // Approve.
        //List<String> approvableObjectIds = metadata.getApprovableObjectIds();
        //if (approvableObjectIds.size() > 0) {
        //    backendRegistry.submitApproveObjectsRequest(approvableObjectIds);
        //}

        // Deprecate old.
        List<String> deprecateObjectIds = new ArrayList<String>();
        deprecateObjectIds.add(currentRegistryObjectId);
        backendRegistry.submitDeprecateObjectsRequest(deprecateObjectIds);

        // Deal with association propagation if required.
        if (this.isAssociationPropagation()) {
            this.handleAssociationPropagation(this.getTargetPatientId(), newRegistryObjectId, currentRegistryObjectId);
        }
        return true;
    }

    /**
     *
     * @param targetPatientId
     * @param newRegistryObjectId
     * @param currentRegistryObjectId
     * @throws XdsException
     */
    abstract protected void handleAssociationPropagation(String targetPatientId, String newRegistryObjectId, String currentRegistryObjectId) throws XdsException;
}
