/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xds.registry.storedquery;

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.SQCodedTerm;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class GetSubmissionSetAndContents extends StoredQuery {

    private final static Logger logger = Logger.getLogger(GetSubmissionSetAndContents.class);

    /**
     *
     * @param params
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public GetSubmissionSetAndContents(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSSubmissionSetEntryUUID", true, false, true, false, false, "$XDSSubmissionSetUniqueId");
        validateQueryParam("$XDSSubmissionSetUniqueId", true, false, true, false, false, "$XDSSubmissionSetEntryUUID");
        validateQueryParam("$XDSDocumentEntryFormatCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryConfidentialityCode", false, true, true, true, true, (String[]) null);
        validateQueryParam("$MetadataLevel", false, false, false, false, false, (String[]) null);
        if (this.hasValidationErrors()) {
            throw new MetadataValidationException("Metadata Validation error present");
        }
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    public Metadata runInternal() throws XdsException {
        Metadata metadata;
        SqParams params = this.getSqParams();
        String metadataLevel = params.getIntParm("$MetadataLevel");
        String ssUUID = params.getStringParm("$XDSSubmissionSetEntryUUID");
        String ssUID = params.getStringParm("$XDSSubmissionSetUniqueId");
        List<String> assocStatusValues = null;
        if (metadataLevel == null || metadataLevel.equals("1")) {
            // Default association status to "Approved".
            assocStatusValues = new ArrayList<String>();
            assocStatusValues.add(MetadataSupport.status_type_approved);
        }
        if (ssUUID != null) {
            // starting from uuid
            OMElement x = this.getRegistryPackageByUUID(ssUUID, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
            metadata = MetadataParser.parseNonSubmission(x);
            if (this.isReturnLeafClass() && metadata.getSubmissionSets().size() != 1) {
                return metadata;
            }
            if (!this.isReturnLeafClass() && metadata.getObjectRefs().size() != 1) {
                return metadata;
            }
        } else {
            // starting from uniqueid
            OMElement x = this.getRegistryPackageByUID(ssUID, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
            metadata = MetadataParser.parseNonSubmission(x);
            if (this.isReturnLeafClass() && metadata.getSubmissionSets().size() != 1) {
                return metadata;
            }
            if (!this.isReturnLeafClass() && metadata.getObjectRefs().size() != 1) {
                return metadata;
            }
            if (this.isReturnLeafClass()) {
                ssUUID = metadata.getSubmissionSet().getAttributeValue(MetadataSupport.id_qname);
            } else {
                ssUUID = metadata.getObjectRefs().get(0).getAttributeValue(MetadataSupport.id_qname);
            }
        }

        // ss_uuid has now been set
        SQCodedTerm confidentialityCodes = params.getCodedParm("$XDSDocumentEntryConfidentialityCode");
        SQCodedTerm formatCodes = params.getCodedParm("$XDSDocumentEntryFormatCode");

        // Add documents that match criteria.
        OMElement docMetadata = this.getSubmissionSetDocuments(ssUUID, assocStatusValues, formatCodes, confidentialityCodes);
        metadata.addMetadata(docMetadata);

        // Add folders that match criteria.
        OMElement folderMetadata = this.getSubmissionSetFolders(ssUUID, assocStatusValues);
        metadata.addMetadata(folderMetadata);

        // Add submission set associations that match criteria.
        List<String> ssUuids = new ArrayList<String>();
        ssUuids.add(ssUUID);
        OMElement assoc1Metadata = this.getRegistryPackageAssociations(ssUuids, assocStatusValues);
        if (assoc1Metadata != null) {
            metadata.addMetadata(assoc1Metadata);
        }

        // Add folder associations that match criteria (a little redundant passing in assocStatusValues but OK).
        List<String> folderIds = metadata.getFolderIds();
        OMElement assoc2Metadata = this.getRegistryPackageAssociations(folderIds, assocStatusValues);
        if (assoc2Metadata != null) {
            metadata.addMetadata(assoc2Metadata);
        }
        metadata.removeDuplicates();

        // some document may have been filtered out, remove the unnecessary Associations
        ArrayList<String> contentIds = new ArrayList<String>();
        contentIds.addAll(metadata.getSubmissionSetIds());
        contentIds.addAll(metadata.getExtrinsicObjectIds());
        contentIds.addAll(metadata.getFolderIds());

        // add in Associations that link the above parts
        contentIds.addAll(metadata.getObjectIds(metadata.getAssociationsInclusive(contentIds)));

        // Assocs can link to Assocs to so repeat
        contentIds.addAll(metadata.getObjectIds(metadata.getAssociationsInclusive(contentIds)));
        metadata.filter(contentIds);
        return metadata;
    }
}
