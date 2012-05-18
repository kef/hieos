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
import com.vangent.hieos.services.xds.registry.mu.validation.SubmitAssociationCommandValidator;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.IdParser;
import com.vangent.hieos.xutil.metadata.structure.Metadata;

import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class SubmitAssociationCommand extends MetadataUpdateCommand {

    private OMElement submittedRegistryObject;
    private OMElement submitAssociation;

    /**
     * 
     * @param submittedMetadata
     * @param metadataUpdateContext
     * @param metadataUpdateCommandValidator
     */
    public SubmitAssociationCommand(Metadata submittedMetadata, MetadataUpdateContext metadataUpdateContext,
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
    public OMElement getSubmitAssociation() {
        return submitAssociation;
    }

    /**
     *
     * @param submitAssociation
     */
    public void setSubmitAssociation(OMElement submitAssociation) {
        this.submitAssociation = submitAssociation;
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    @Override
    protected boolean executeUpdate() throws XdsException {
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        Metadata submittedMetadata = this.getSubmittedMetadata();

        // Change symbolic names to UUIDs.
        IdParser idParser = new IdParser(submittedMetadata);
        idParser.compileSymbolicNamesIntoUuids();
        submittedMetadata.reindex();

        // Set status to "Approved" on the target association.
        submittedMetadata.setStatusOnApprovableObjects();

        // Make registry submission.
        backendRegistry.setReason("Submit Association");
        OMElement result = backendRegistry.submit(submittedMetadata);
        // FIXME: result?
        return true;
    }
}
