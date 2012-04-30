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
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
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
public class GetFoldersForDocument extends StoredQuery {

    /**
     *
     * @param params
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public GetFoldersForDocument(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSDocumentEntryUniqueId", true, false, true, false, false, "$XDSDocumentEntryEntryUUID");
        validateQueryParam("$XDSDocumentEntryEntryUUID", true, false, true, false, false, "$XDSDocumentEntryUniqueId");
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
        SqParams params = this.getSqParams();
        String metadataLevel = params.getIntParm("$MetadataLevel");
        String uid = params.getStringParm("$XDSDocumentEntryUniqueId");
        String uuid = params.getStringParm("$XDSDocumentEntryEntryUUID");
        List<String> assocStatusValues = params.getListParm("$XDSAssociationStatus");
        if (assocStatusValues == null || assocStatusValues.isEmpty()) {
            // association status not specified.
            // Default association status to "Approved" if not specified.
            assocStatusValues = new ArrayList<String>();
            assocStatusValues.add(MetadataSupport.status_type_approved);
        }
        if (uuid == null || uuid.equals("")) {
            uuid = this.getDocumentUUIDFromUID(uid);
        }
        if (uuid == null) {
            throw new XdsException("Cannot identify referenced document (uniqueId = " + uid + ")");
        }
        OMElement folders = this.getFoldersForDocument(uuid, assocStatusValues);
        return MetadataParser.parseNonSubmission(folders);
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
