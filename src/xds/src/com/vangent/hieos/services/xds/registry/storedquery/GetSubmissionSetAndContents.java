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
import com.vangent.hieos.xutil.exception.NoSubmissionSetException;
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
     * @param return_objects
     * @param response
     * @param log_message
     * @param is_secure
     * @throws MetadataValidationException
     */
    public GetSubmissionSetAndContents(SqParams params, boolean return_objects, Response response, XLogMessage log_message)
            throws MetadataValidationException {
        super(params, return_objects, response, log_message);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSSubmissionSetEntryUUID", true, false, true, false, false, "$XDSSubmissionSetUniqueId");
        validateQueryParam("$XDSSubmissionSetUniqueId", true, false, true, false, false, "$XDSSubmissionSetEntryUUID");
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
        String ss_uuid = params.getStringParm("$XDSSubmissionSetEntryUUID");
        String ss_uid = params.getStringParm("$XDSSubmissionSetUniqueId");
        if (ss_uuid != null) {
            // starting from uuid
            OMElement x = this.getRegistryPackageByUUID(ss_uuid, "urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8");
            metadata = MetadataParser.parseNonSubmission(x);
            if (this.return_leaf_class && metadata.getSubmissionSets().size() != 1) {
                return metadata;
            }
            if (!this.return_leaf_class && metadata.getObjectRefs().size() != 1) {
                return metadata;
            }
        } else {
            // starting from uniqueid
            OMElement x = this.getRegistryPackageByUID(ss_uid, "urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8");
            metadata = MetadataParser.parseNonSubmission(x);
            if (this.return_leaf_class && metadata.getSubmissionSets().size() != 1) {
                return metadata;
            }
            if (!this.return_leaf_class && metadata.getObjectRefs().size() != 1) {
                return metadata;
            }
            if (this.return_leaf_class) {
                ss_uuid = metadata.getSubmissionSet().getAttributeValue(MetadataSupport.id_qname);
            } else {
                ss_uuid = metadata.getObjectRefs().get(0).getAttributeValue(MetadataSupport.id_qname);
            }
        }

        // ss_uuid has now been set
        SQCodedTerm conf_codes = params.getCodedParm("$XDSDocumentEntryConfidentialityCode");
        SQCodedTerm format_codes = params.getCodedParm("$XDSDocumentEntryFormatCode");

        OMElement doc_metadata = this.getSubmissionSetDocuments(ss_uuid, format_codes, conf_codes);
        metadata.addMetadata(doc_metadata);

        OMElement fol_metadata = this.getSubmissionSetFolders(ss_uuid);
        metadata.addMetadata(fol_metadata);

        List<String> ssUuids = new ArrayList<String>();
        ssUuids.add(ss_uuid);
        OMElement assoc1_metadata = this.getRegistryPackageAssociations(ssUuids);
        if (assoc1_metadata != null) {
            metadata.addMetadata(assoc1_metadata);
        }

        List<String> folder_ids = metadata.getFolderIds();
        OMElement assoc2_metadata = this.getRegistryPackageAssociations(folder_ids);
        if (assoc2_metadata != null) {
            metadata.addMetadata(assoc2_metadata);
        }
        metadata.removeDuplicates();

        // some document may have been filtered out, remove the unnecessary Associations
        ArrayList<String> content_ids = new ArrayList<String>();
        content_ids.addAll(metadata.getSubmissionSetIds());
        content_ids.addAll(metadata.getExtrinsicObjectIds());
        content_ids.addAll(metadata.getFolderIds());

        // add in Associations that link the above parts
        content_ids.addAll(metadata.getIds(metadata.getAssociationsInclusive(content_ids)));

        // Assocs can link to Assocs to so repeat
        content_ids.addAll(metadata.getIds(metadata.getAssociationsInclusive(content_ids)));
        metadata.filter(content_ids);
        return metadata;
    }
}
