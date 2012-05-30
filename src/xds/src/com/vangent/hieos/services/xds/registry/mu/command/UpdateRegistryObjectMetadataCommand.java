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
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
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
     * @param submittedMetadata
     * @param metadataUpdateContext
     * @param metadataUpdateCommandValidator
     */
    public UpdateRegistryObjectMetadataCommand(Metadata submittedMetadata, MetadataUpdateContext metadataUpdateContext,
            MetadataUpdateCommandValidator metadataUpdateCommandValidator) {
        super(submittedMetadata, metadataUpdateContext, metadataUpdateCommandValidator);
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
    protected boolean executeUpdate() throws XdsException {
        Metadata submittedMetadata = this.getSubmittedMetadata();

        // Adjust the version number (current version number + 1).
        Metadata.updateRegistryObjectVersion(submittedRegistryObject, this.getPreviousVersion());

        // Get current/new registry object ids.
        String currentRegistryObjectId = this.getCurrentMetadata().getId(this.getCurrentRegistryObject());
        String newRegistryObjectId = submittedMetadata.getId(submittedRegistryObject);

        // Deprecate old.
        List<String> deprecateObjectIds = new ArrayList<String>();
        deprecateObjectIds.add(currentRegistryObjectId);
        BackendRegistry backendRegistry = this.getBackendRegistry();
        OMElement result = backendRegistry.submitDeprecateObjectsRequest(deprecateObjectIds);
        // FIXME: result?

        // Deal with association propagation if required.
        if (this.isAssociationPropagation()) {
            this.handleAssociationPropagation(
                    this.getSubmittedPatientId(), newRegistryObjectId, currentRegistryObjectId);
        } else {
            this.deprecateRegistryObjectAssociations(currentRegistryObjectId);
        }

        // Now, install new version.
        this.submitNewRegistryObjectVersion();
        return true;
    }

    /**
     * 
     * @throws XdsException
     */
    abstract protected void submitNewRegistryObjectVersion() throws XdsException;

    /**
     *
     * @param submittedPatientId
     * @param newRegistryObjectId
     * @param currentRegistryObjectId
     * @throws XdsException
     */
    abstract protected void handleAssociationPropagation(String submittedPatientId, String newRegistryObjectId, String currentRegistryObjectId) throws XdsException;
}
