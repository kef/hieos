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
     * @param return_objects
     * @param response
     * @param log_message
     * @param is_secure
     * @throws MetadataValidationException
     */
    public GetFolderAndContents(SqParams params, boolean return_objects, Response response, XLogMessage log_message)
            throws MetadataValidationException {
        super(params, return_objects, response, log_message);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSFolderEntryUUID", true, false, true, false, false, "$XDSFolderUniqueId");
        validateQueryParam("$XDSFolderUniqueId", true, false, true, false, false, "$XDSFolderEntryUUID");
        validateQueryParam("$XDSDocumentEntryFormatCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryConfidentialityCode", false, true, true, true, true, (String[]) null);

        if (this.has_validation_errors) {
            throw new MetadataValidationException("Metadata Validation error present");
        }
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    public Metadata run_internal() throws XdsException {
        Metadata metadata;
        String fol_uuid = params.getStringParm("$XDSFolderEntryUUID");
        String fol_uid = params.getStringParm("$XDSFolderUniqueId");
        if (fol_uuid != null) {
            // starting from uuid
            OMElement x = this.getFolderByUUID(fol_uuid);
            metadata = MetadataParser.parseNonSubmission(x);
            if (this.return_leaf_class) {
                if (metadata.getFolders().size() == 0) {
                    return metadata;
                }
            } else {
                if (metadata.getObjectRefs().size() == 0) {
                    return metadata;
                }
            }
        } else {
            // starting from uniqueid
            OMElement x = this.getFolderByUID(fol_uid);
            metadata = MetadataParser.parseNonSubmission(x);
            if (this.return_leaf_class) {
                if (metadata.getFolders().size() == 0) {
                    return metadata;
                }
            } else {
                if (metadata.getObjectRefs().size() == 0) {
                    return metadata;
                }
            }
            fol_uuid = metadata.getFolder(0).getAttributeValue(MetadataSupport.id_qname);
        }

        this.log_message.addOtherParam("Folder id", fol_uuid);

        List<String> folder_ids = new ArrayList<String>();
        folder_ids.add(fol_uuid);

        // fol_uuid has now been set
        List<String> content_ids = new ArrayList<String>();
        SQCodedTerm conf_codes = params.getCodedParm("$XDSDocumentEntryConfidentialityCode");
        SQCodedTerm format_codes = params.getCodedParm("$XDSDocumentEntryFormatCode");

        OMElement doc_metadata = this.getFolderDocuments(fol_uuid, format_codes, conf_codes);
        metadata.addMetadata(doc_metadata);
        List<String> docIds = this.getIdsFromRegistryResponse(doc_metadata);
        content_ids.addAll(docIds);
        this.log_message.addOtherParam("Doc ids", docIds.toString());
        List<String> assocIds;
        if (content_ids.size() > 0 && folder_ids.size() > 0) {
            OMElement assoc_metadata = this.getRegistryPackageAssociations(folder_ids, content_ids);
            assocIds = this.getIdsFromRegistryResponse(assoc_metadata);
            this.log_message.addOtherParam("Assoc ids", assocIds.toString());
            metadata.addMetadata(assoc_metadata);
        }
        this.log_message.addOtherParam("Assoc ids", "None");
        return metadata;
    }
}
