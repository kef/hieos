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
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.List;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class GetFolders extends StoredQuery {

    /**
     * 
     */
    public GetFolders(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSFolderEntryUUID", true, true, true, false, false, "$XDSFolderUniqueId");
        validateQueryParam("$XDSFolderUniqueId", true, true, true, false, false, "$XDSFolderEntryUUID");
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
        List<String> folderUUID = params.getListParm("$XDSFolderEntryUUID");
        if (folderUUID != null) {
            // starting from uuid
            OMElement x = getFolderByUUID(folderUUID);
            metadata = MetadataParser.parseNonSubmission(x);
            /*if (metadata.getFolders().size() == 0) {
            return metadata;
            }*/
        } else {
            // starting from uniqueid
            List<String> folderUID = params.getListParm("$XDSFolderUniqueId");
            OMElement x = getFolderByUID(folderUID);
            metadata = MetadataParser.parseNonSubmission(x);
        }
        return metadata;
    }
}
