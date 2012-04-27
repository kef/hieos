/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
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
import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;

import java.util.List;

/**
 * 
 * @author Bernie Thuman (many rewrites from NIST code base).
 */
public class SubmitObjectsRequestStoredQuerySupport extends StoredQuery {

    /**
     *
     * @param response
     * @param logMessage
     * @param backendRegistry
     */
    public SubmitObjectsRequestStoredQuerySupport(ErrorLogger response, XLogMessage logMessage, BackendRegistry backendRegistry) {
        super(response, logMessage, backendRegistry);
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    public List<String> getMissingDocuments(List<String> uuids) throws XdsException {
        StoredQueryBuilder sqb = StoredQuery.getSQL_DocumentByUUID(uuids, false /* LeafClass */, null);
        List<String> results = this.runQueryForObjectRefs(sqb);
        return this.findMissingIds(uuids, results);
    }

    /**
     * 
     * @param uuids
     * @return
     * @throws XdsException
     */
    public List<String> getMissingFolders(ArrayList<String> uuids) throws XdsException {
        BackendRegistry backendRegistry = this.getBackendRegistry();
        backendRegistry.setReason("Verify are Folders");
        StoredQueryBuilder sqb = StoredQuery.getSQL_ApprovedFolders(uuids, false /* LeafClass */);
        List<String> queryResults = this.runQueryForObjectRefs(sqb);
        return this.findMissingIds(uuids, queryResults);
    }

    /**
     *
     * @param uuids
     * @return
     * @throws XdsException
     */
    public List<String> getXFRMandAPNDDocuments(List<String> uuids) throws XdsException {
        if (uuids.isEmpty()) {
            return new ArrayList<String>();
        }
        StoredQueryBuilder sqb = StoredQuery.getSQL_XFRMandAPNDDocuments(uuids, false /* LeafClass */);
        return this.runQueryForObjectRefs(sqb);
    }

   

    /**
     *
     * @return
     * @throws XdsException
     */
    @Override
    public Metadata runInternal() throws XdsException {
        // TODO Auto-generated method stub
        return null;
    }
}
