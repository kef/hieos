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

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class GetFolderAndContents extends StoredQuery {

    /**
     *
     * @param params
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public GetFolderAndContents(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSFolderEntryUUID", true, false, true, false, false, "$XDSFolderUniqueId");
        validateQueryParam("$XDSFolderUniqueId", true, false, true, false, false, "$XDSFolderEntryUUID");
        validateQueryParam("$XDSDocumentEntryFormatCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryConfidentialityCode", false, true, true, true, true, (String[]) null);

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
        String folderUUID = params.getStringParm("$XDSFolderEntryUUID");
        String folderUID = params.getStringParm("$XDSFolderUniqueId");
        if (folderUUID != null) {
            // starting from uuid
            OMElement x = this.getFolderByUUID(folderUUID);
            metadata = MetadataParser.parseNonSubmission(x);
            if (this.isReturnLeafClass()) {
                if (metadata.getFolders().isEmpty()) {
                    return metadata;
                }
            } else {
                if (metadata.getObjectRefs().isEmpty()) {
                    return metadata;
                }
            }
        } else {
            // starting from uniqueid
            OMElement x = this.getFolderByUID(folderUID);
            metadata = MetadataParser.parseNonSubmission(x);
            if (this.isReturnLeafClass()) {
                if (metadata.getFolders().isEmpty()) {
                    return metadata;
                }
            } else {
                if (metadata.getObjectRefs().isEmpty()) {
                    return metadata;
                }
            }
            folderUUID = metadata.getFolder(0).getAttributeValue(MetadataSupport.id_qname);
        }

        XLogMessage logMessage = this.getLogMessage();
        logMessage.addOtherParam("Folder id", folderUUID);

        List<String> folderIds = new ArrayList<String>();
        folderIds.add(folderUUID);

        // fol_uuid has now been set
        List<String> contentIds = new ArrayList<String>();
        SQCodedTerm confidentialityCodes = params.getCodedParm("$XDSDocumentEntryConfidentialityCode");
        SQCodedTerm formatCodes = params.getCodedParm("$XDSDocumentEntryFormatCode");

        OMElement documentMetadata = this.getFolderDocuments(folderUUID, formatCodes, confidentialityCodes);
        metadata.addMetadata(documentMetadata);
        List<String> docIds = this.getIdsFromRegistryResponse(documentMetadata);
        contentIds.addAll(docIds);
        logMessage.addOtherParam("Doc ids", docIds.toString());
        List<String> assocIds;
        if (contentIds.size() > 0 && folderIds.size() > 0) {
            OMElement assocMetadata = this.getRegistryPackageAssociations(folderIds, contentIds);
            assocIds = this.getIdsFromRegistryResponse(assocMetadata);
            logMessage.addOtherParam("Assoc ids", assocIds.toString());
            metadata.addMetadata(assocMetadata);
        }
        logMessage.addOtherParam("Assoc ids", "None");
        return metadata;
    }
}
