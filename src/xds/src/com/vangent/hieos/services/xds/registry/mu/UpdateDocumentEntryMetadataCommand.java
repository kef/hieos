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
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.IdParser;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;

/**
 * 
 * @author Bernie Thuman
 */
public class UpdateDocumentEntryMetadataCommand extends MetadataUpdateCommand {

    private String previousVersion;
    private OMElement targetObject;

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
     * @throws XdsException
     */
    @Override
    public void execute() throws XdsException {
        //throw new UnsupportedOperationException("Not supported yet.");
        System.out.println("Executing command ... " + this.getClass().getName());
        XLogMessage logMessage = this.getMetadataUpdateContext().getLogMessage();
        logMessage.addOtherParam("Command", "Update DocumentEntry Metadata");

        // Validate.
        this.validate();

        // FIXME: metadata includes the targetObject, but it may contain other details
        // we do not want.
        Metadata metadata = this.getMetadata();

        // Now, fixup the Metadata to be submitted.

        // Change symbolic names to UUIDs.
        IdParser idParser = new IdParser(metadata);
        idParser.compileSymbolicNamesIntoUuids();

        // Adjust the version number (current version number + 1).
        this.updateVersion(targetObject);

        // DEBUG:
        logMessage.addOtherParam("Version to Submit", targetObject);

        // FIXME: MetadataTypes.METADATA_TYPE_Rb?
        //RegistryUtility.schema_validate_local(submitObjectsRequest, MetadataTypes.METADATA_TYPE_Rb);

        //BackendRegistry backendRegistry = this.getMetadataUpdateContext().getBackendRegistry();
        //backendRegistry.setReason("Submit New Version");
        //OMElement result = backendRegistry.submit(metadata);

    }

    /**
     * 
     * @throws XdsException
     */
    private void validate() throws XdsException {
        // Get lid.
        String lid = targetObject.getAttributeValue(MetadataSupport.lid_qname);
        System.out.println("... lid = " + lid);

        //
        // Look for an existing document that 1) matches the lid, 2) status is "Approved"
        // and 3) matches the previous version.
        //

        // Prepare to issue registry query.
        MetadataUpdateContext metadataUpdateContext = this.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                metadataUpdateContext.getResponse(), logMessage,
                metadataUpdateContext.getBackendRegistry());
        // Issue query.
        muSQ.setReturnLeafClass(true);
        OMElement queryResult = muSQ.getDocumentsByLID(lid,
                MetadataSupport.status_type_approved,
                this.previousVersion);
        // Convert response into Metadata instance.
        Metadata currentMetadata = MetadataParser.parseNonSubmission(queryResult);
        List<OMElement> currentDocuments = currentMetadata.getExtrinsicObjects();
        if (currentDocuments.isEmpty()) {
            throw new XdsException("Existing document entry not found");
        } else if (currentDocuments.size() > 1) {
            throw new XdsException("> 1 existing document entry found!");
        }

        // Fall through: we found a single document that matches.
        OMElement currentDocument = currentMetadata.getExtrinsicObject(0);

        // FIXME: BEEF UP VALIDATIONS!!!!
    }

    /**
     * 
     */
    private void updateVersion(OMElement obj) {
        // Get version
        //<rim:VersionInfo versionName="1" />
        OMElement versionInfoEle = MetadataSupport.firstChildWithLocalName(obj, "VersionInfo");
        if (versionInfoEle == null) {
            versionInfoEle = MetadataSupport.om_factory.createOMElement("VersionInfo", MetadataSupport.ebRIMns3);

            OMElement classificationEle = MetadataSupport.firstChildWithLocalName(targetObject, "Classification");
            // Attach to the target object (before first Classification).
            classificationEle.insertSiblingBefore(versionInfoEle);
            //targetObject.addChild(versionInfoEle);
        }
        Double nextVersion = new Double(previousVersion) + 1.0;
        OMAttribute versionNameAttr = versionInfoEle.getAttribute(new QName("versionName"));
        if (versionNameAttr == null) {
            versionInfoEle.addAttribute("versionName", nextVersion.toString(), null);
        } else {
            versionNameAttr.setAttributeValue(nextVersion.toString());
        }
    }
}
