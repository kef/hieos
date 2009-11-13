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

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$uuid", true, true, true, false, false, (String[]) null);
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
        if (uuids != null) {
            OMElement ele = this.getSubmissionSetsOfContents(uuids);
            // this may contain duplicates - parse differently
            metadata = new Metadata();
            metadata.addMetadata(ele, true);

            if (this.return_leaf_class) {
                if (metadata.getSubmissionSetIds().size() > 0) {
                    OMElement assocs_ele = this.getAssocations(MetadataSupport.xdsB_eb_assoc_type_has_member, metadata.getSubmissionSetIds(), uuids);
                    metadata.addMetadata(assocs_ele, true);
                }
            } else {
                if (metadata.getObjectRefIds().size() > 0) {
                    OMElement assocs_ele = this.getAssocations(MetadataSupport.xdsB_eb_assoc_type_has_member, metadata.getObjectRefIds(), uuids);
                    metadata.addMetadata(assocs_ele, true);
                }
            }
            return metadata;
        } else {
            throw new XdsException("GetSubmissionSets: internal error: no format selected");
        }
    }

   
    
}
