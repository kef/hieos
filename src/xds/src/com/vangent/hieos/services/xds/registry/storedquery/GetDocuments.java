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
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.exception.XdsResultNotSinglePatientException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class GetDocuments extends StoredQuery {

    /**
     * 
     * @param params
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public GetDocuments(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);

        // param name, required?, multiple?, is string?, is code?, support AND/OR?, alternative
        validateQueryParam("$XDSDocumentEntryUniqueId", true, true, true, false, false, "$XDSDocumentEntryEntryUUID", "$XDSDocumentEntryLogicalID");
        validateQueryParam("$XDSDocumentEntryEntryUUID", true, true, true, false, false, "$XDSDocumentEntryUniqueId", "$XDSDocumentEntryLogicalID");
        validateQueryParam("$XDSDocumentEntryLogicalID", false, true, true, false, false, "$XDSDocumentEntryEntryUUID", "$XDSDocumentEntryUniqueId");
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
        List<String> uids = params.getListParm("$XDSDocumentEntryUniqueId");
        List<String> uuids = params.getListParm("$XDSDocumentEntryEntryUUID");
        List<String> lids = params.getListParm("$XDSDocumentEntryLogicalID");
        OMElement ele;
        if (uids != null) {
            ele = getDocumentByUID(uids);
        } else if (uuids != null) {
            ele = getDocumentByUUID(uuids);
        } else if (lids != null) {
            ele = getDocumentByLID(lids);
        } else {
            throw new XdsInternalException("GetDocuments Stored Query: UUID, UID or LID not specified in query");
        }
        metadata = MetadataParser.parseNonSubmission(ele);
        return metadata;
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
