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
import org.apache.axiom.om.OMElement;

import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class GetAssociations extends StoredQuery {

    /**
     * 
     * @param params
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public GetAssociations(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$uuid", true, true, true, false, false, (String[]) null);
        validateQueryParam("$XDSAssociationStatus", false, true, true, false, false, (String[]) null);
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
        List<String> assocStatusValues = params.getListParm("$XDSAssociationStatus");

        if (assocStatusValues == null || assocStatusValues.isEmpty()) {
            // association status not specified.
            // Default association status to "Approved" if not specified.
            assocStatusValues = new ArrayList<String>();
            assocStatusValues.add(MetadataSupport.status_type_approved);
        }
        if (uuids != null) {
            OMElement ele = this.getAssociations(uuids, assocStatusValues, null /* assocTypes */);
            metadata = MetadataParser.parseNonSubmission(ele);
        } else {
            throw new XdsInternalException("GetAssociations Stored Query: $uuid not found as a multi-value parameter");
        }
        return metadata;
    }
}
