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

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.IdParser;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class SubmitAssociationCommand extends MetadataUpdateCommand {

    private OMElement targetObject;
    private OMElement submitAssociation;

    /**
     * 
     * @param metadata
     * @param metadataUpdateContext
     */
    public SubmitAssociationCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
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
     */
    @Override
    protected UpdateDocumentSetCommandValidator getCommandValidator() {
        return new SubmitAssociationCommandValidator(this);
    }

    /**
     * 
     * @param validator
     * @return
     * @throws XdsException
     */
    @Override
    protected boolean execute(UpdateDocumentSetCommandValidator validator) throws XdsException {
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();

        Metadata metadata = this.getMetadata();
        // Hack: Change urn:ihe:iti:2010:AssociationType:SubmitAssociation to a HasMember assocation
        // for submission.
        // FIXME: May not be a good idea ... not returned in any queries ...
        OMAttribute assocTypeAttribute = submitAssociation.getAttribute(MetadataSupport.association_type_qname);
        assocTypeAttribute.setAttributeValue(MetadataSupport.xdsB_eb_assoc_type_has_member);

        // Set status to "Approved" on the submission set HasMember association
        //Metadata.setStatusOnRegistryObject(submitAssociation, MetadataSupport.status_type_approved);

        // Change symbolic names to UUIDs.
        IdParser idParser = new IdParser(metadata);
        idParser.compileSymbolicNamesIntoUuids();

        // Set status to "Approved" on the target association.
        metadata.setStatusOnApprovableObjects();
        //Metadata.setStatusOnRegistryObject(metadata.getSubmissionSet(), MetadataSupport.status_type_approved);
        //Metadata.setStatusOnRegistryObject(this.getTargetObject(), MetadataSupport.status_type_approved);

        // Make registry submission.
        backendRegistry.setReason("Submit Association");
        OMElement result = backendRegistry.submit(metadata);
        return true;
    }
}
