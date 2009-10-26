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
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.query.StoredQuery;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class GetSubmissionSets extends StoredQuery {

    /**
     *
     * @param params
     * @param return_objects
     * @param response
     * @param log_message
     * @param is_secure
     * @throws MetadataValidationException
     */
    public GetSubmissionSets(SqParams params, boolean return_objects, Response response, XLogMessage log_message)
            throws MetadataValidationException {
        super(params, return_objects, response, log_message);

        // param name, required?, multiple?, is string?, is code?, alternative
        validateQueryParam("$uuid", true, true, true, false, (String[]) null);
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
        List<String> uuids = params.getListParm("$uuid");
        if (uuids != null && uuids.size() > 0) {
            OMElement ele = get_submissionsets(uuids);
            // this may contain duplicates - parse differently
            metadata = new Metadata();
            //metadata.setGrokMetadata(false);
            metadata.addMetadata(ele, true);
            if (metadata.getSubmissionSetIds().size() > 0) {
                OMElement assocs_ele = this.get_associations(MetadataSupport.xdsB_eb_assoc_type_has_member, metadata.getSubmissionSetIds(), uuids);
                metadata.addMetadata(assocs_ele, true);
            }
        } else {
            throw new XdsInternalException("GetSubmissionSets Stored Query: $uuid not found");
        }
        return metadata;
    }

    /**
     * 
     * @param uuids
     * @return
     * @throws XdsException
     */
    protected OMElement get_submissionsets(List<String> uuids) throws XdsException {
        init();
        if (this.return_leaf_class) {
            append("SELECT * FROM RegistryPackage rp, Association a, ExternalIdentifier ei");
        } else {
            append("SELECT rp.id FROM RegistryPackage rp, Association a, ExternalIdentifier ei");
        }
        newline();
        append("WHERE ");
        newline();
        append(" a.sourceObject = rp.id AND");
        newline();
        append(" a.associationType = '");
        append(MetadataSupport.xdsB_eb_assoc_type_has_member);
        append("' AND");
        newline();
        append(" a.targetObject IN ");
        append(uuids);
        append(" AND");
        newline();
        append(" ei.registryObject = rp.id AND");
        newline();
        append(" ei.identificationScheme = 'urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446'");
        newline();
        return query(this.return_leaf_class);
    }

    /**
     *
     * @param type
     * @param froms
     * @param tos
     * @return
     * @throws XdsException
     */
    protected OMElement get_associations(String type, List<String> froms, List<String> tos)
            throws XdsException {
        init();
        if (this.return_leaf_class) {
            append("SELECT * FROM Association a");
        } else {
            append("SELECT a.id FROM Association a");
        }
        newline();
        append("WHERE ");
        newline();
        append(" a.associationType = '" + type + "' AND");
        newline();
        append(" a.sourceObject IN");
        append(froms);
        append(" AND");
        newline();
        append(" a.targetObject IN");
        append(tos);
        newline();
        return query(this.return_leaf_class);
    }
}
