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
import com.vangent.hieos.xutil.exception.MetadataException;
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

    private OMElement submittedRegistryObject;
    private String previousVersion;
    private boolean associationPropagation;
    // Scratch pad area.
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
    public OMElement getSubmittedRegistryObject() {
        return submittedRegistryObject;
    }

    /**
     *
     * @param submittedRegistryObject
     */
    public void setSubmittedRegistryObject(OMElement submittedRegistryObject) {
        this.submittedRegistryObject = submittedRegistryObject;
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
     * @throws MetadataException
     */
    public String getSubmittedPatientId() throws MetadataException {
        return this.getSubmittedMetadata().getPatientId(submittedRegistryObject);
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

        Metadata submittedMetadata = this.getSubmittedMetadata();
        OMElement submittedRegistryObject = this.getSubmittedRegistryObject();

        // Now, fixup the Metadata to be submitted.
        // Change symbolic names to UUIDs.
        IdParser idParser = new IdParser(submittedMetadata);
        idParser.compileSymbolicNamesIntoUuids();

        // Log metadata (after id assignment).
        MetadataUpdateHelper.logMetadata(logMessage, submittedMetadata);

        // Adjust the version number (current version number + 1).
        Metadata.updateRegistryObjectVersion(submittedRegistryObject, this.getPreviousVersion());

        // Get current/new registry object ids.
        String currentRegistryObjectId = this.getCurrentMetadata().getId(this.getCurrentRegistryObject());
        String newRegistryObjectId = submittedMetadata.getId(submittedRegistryObject);

        // Submit new registry object version.
        backendRegistry.setReason("Submit New Version");
        submittedMetadata.setStatusOnApprovableObjects();
        OMElement result = backendRegistry.submit(submittedMetadata);
        // FIXME: result?

        // Deprecate old.
        List<String> deprecateObjectIds = new ArrayList<String>();
        deprecateObjectIds.add(currentRegistryObjectId);
        result = backendRegistry.submitDeprecateObjectsRequest(deprecateObjectIds);
        // FIXME: result?

        // Deal with association propagation if required.
        if (this.isAssociationPropagation()) {
            this.handleAssociationPropagation(this.getSubmittedPatientId(), newRegistryObjectId, currentRegistryObjectId);
        }
        return true;
    }

    /**
     *
     * @param submittedPatientId
     * @param newRegistryObjectId
     * @param currentRegistryObjectId
     * @throws XdsException
     */
    abstract protected void handleAssociationPropagation(String submittedPatientId, String newRegistryObjectId, String currentRegistryObjectId) throws XdsException;
}
