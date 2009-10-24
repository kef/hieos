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
import com.vangent.hieos.xutil.query.StoredQuery;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
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
    public GetFolderAndContents(SqParams params, boolean return_objects, Response response, XLogMessage log_message, boolean is_secure)
            throws MetadataValidationException {
        super(params, return_objects, response, log_message, is_secure);

        // param name, required?, multiple?, is string?, is code?, alternative
        validateQueryParam("$XDSFolderEntryUUID", true, false, true, false, "$XDSFolderUniqueId");
        validateQueryParam("$XDSFolderUniqueId", true, false, true, false, "$XDSFolderEntryUUID");
        validateQueryParam("$XDSDocumentEntryFormatCode", false, true, true, true, (String[]) null);
        validateQueryParam("$XDSDocumentEntryConfidentialityCode", false, true, true, true, (String[]) null);
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

        String fol_uuid = get_string_parm("$XDSFolderEntryUUID");
        if (fol_uuid != null) {
            // starting from uuid
            OMElement x = get_fol_by_uuid(fol_uuid);
            metadata = MetadataParser.parseNonSubmission(x);
            if (metadata.getFolders().size() == 0) {
                return metadata;
            }
        } else {
            // starting from uniqueid
            String fol_uid = get_string_parm("$XDSFolderUniqueId");
            OMElement x = getFolderByUID(fol_uid);
            metadata = MetadataParser.parseNonSubmission(x);
            if (metadata.getFolders().size() == 0) {
                return metadata;
            }

            fol_uuid = metadata.getFolder(0).getAttributeValue(MetadataSupport.id_qname);
        }

        // ss_uuid has now been set

        ArrayList<String> content_ids = new ArrayList<String>();
        SQCodedTerm conf_codes = params.getCodedParm("$XDSDocumentEntryConfidentialityCode");
        SQCodedTerm format_codes = params.getCodedParm("$XDSDocumentEntryFormatCode");

        OMElement doc_metadata = get_fol_docs(fol_uuid, format_codes, conf_codes);
        metadata.addMetadata(doc_metadata);
        content_ids.addAll(get_ids_from_registry_response(doc_metadata));

        ArrayList<String> folder_ids = metadata.getFolderIds();
        if (content_ids.size() > 0) {
            OMElement assoc_metadata = get_assocs(folder_ids, content_ids);
            metadata.addMetadata(assoc_metadata);
        }
        return metadata;
    }
}
