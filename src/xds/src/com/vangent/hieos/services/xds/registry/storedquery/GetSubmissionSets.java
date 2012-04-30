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
import com.vangent.hieos.xutil.exception.XdsResultNotSinglePatientException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.response.Response;
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
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public GetSubmissionSets(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$uuid", true, true, true, false, false, (String[]) null);
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
        List<String> uuids = params.getListParm("$uuid");
        if (uuids != null) {
            OMElement ele = this.getSubmissionSetsOfContents(uuids);
            // this may contain duplicates - parse differently
            metadata = new Metadata();
            metadata.addMetadata(ele, true);
            if (this.isReturnLeafClass()) {
                if (metadata.getSubmissionSetIds().size() > 0) {
                    OMElement assocsEle = this.getAssocations(MetadataSupport.xdsB_eb_assoc_type_has_member, metadata.getSubmissionSetIds(), uuids);
                    metadata.addMetadata(assocsEle, true);
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

    /**
     *
     * @param validateConsistentPatientId
     * @param metadata
     * @throws XdsException
     * @throws XdsResultNotSinglePatientException
     */
    @Override
    public void validateConsistentPatientId(boolean validateConsistentPatientId, Metadata metadata)
            throws XdsException, XdsResultNotSinglePatientException {
        // Default implementation.
        // Can't really do anything here, since metadata update is implemented.
    }
}
