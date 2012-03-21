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
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XDSRegistryOutOfResourcesException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.SQCodedTerm;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.List;
import org.apache.axiom.om.OMElement;
import org.freebxml.omar.server.persistence.rdb.RegistryCodedValueMapper;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class FindFoldersForMultiplePatients extends StoredQuery {

    /**
     * 
     * @param params
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public FindFoldersForMultiplePatients(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSFolderPatientId", false, true, true, false, false, (String[]) null);
        validateQueryParam("$XDSFolderLastUpdateTimeFrom", false, false, false, false, false, (String[]) null);
        validateQueryParam("$XDSFolderLastUpdateTimeTo", false, false, false, false, false, (String[]) null);
        validateQueryParam("$XDSFolderCodeList", true, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSFolderStatus", true, true, true, false, false, (String[]) null);
        if (this.hasValidationErrors()) {
            throw new MetadataValidationException("Metadata Validation error present");
        }
    }

    /**
     * 
     * @return
     * @throws XdsException
     */
    public Metadata runInternal() throws XdsException, XDSRegistryOutOfResourcesException {
        if (this.isReturnLeafClass()) {
            this.setReturnLeafClass(false);
            OMElement refs = impl();
            Metadata m = MetadataParser.parseNonSubmission(refs);
            int objectRefsSize = m.getObjectRefs().size();
            // Guard against large leaf class queries.
            if (objectRefsSize > this.getMaxLeafObjectsAllowedFromQuery()) {
                throw new XDSRegistryOutOfResourcesException(
                        "FindFoldersForMultiplePatients Stored Query for LeafClass is limited to " + this.getMaxLeafObjectsAllowedFromQuery() + " documents on this Registry. Your query targeted " + m.getObjectRefs().size() + " documents");
            }
            this.setReturnLeafClass(true);  // Reset.
            if (objectRefsSize == 0) {
                return m;  // No need to go further and issue another query.
            }
        }
        OMElement results = impl();
        Metadata m = MetadataParser.parseNonSubmission(results);
        if (this.getLogMessage() != null) {
            this.getLogMessage().addOtherParam("Results structure", m.structure());
        }
        return m;
    }

    /**
     * 
     * @return
     * @throws XdsInternalException
     * @throws MetadataException
     * @throws XdsException
     */
    private OMElement impl() throws XdsInternalException, MetadataException, XdsException {
        SqParams params = this.getSqParams();
        List<String> patientId = params.getListParm("$XDSFolderPatientId");
        String lastUpdateTimeFrom = params.getIntParm("$XDSFolderLastUpdateTimeFrom");
        String lastUpdateTimeTo = params.getIntParm("$XDSFolderLastUpdateTimeTo");
        SQCodedTerm codes = params.getCodedParm("$XDSFolderCodeList");
        List<String> status = params.getListParm("$XDSFolderStatus");
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.isReturnLeafClass());
        //sqb.initQuery();
        sqb.select("obj");
        sqb.append("FROM RegistryPackage obj");
        if (patientId != null && patientId.size() > 0) {
            sqb.append(", ExternalIdentifier patId");
            sqb.newline();
        }
        if (lastUpdateTimeFrom != null) {
            sqb.newline();
            sqb.append(", Slot updateTimef");
        }
        if (lastUpdateTimeTo != null) {
            sqb.newline();
            sqb.append(", Slot updateTimet");
        }
        sqb.newline();
        sqb.appendClassificationDeclaration(codes);

        // WHERE clause ...
        sqb.newline();
        sqb.where();
        sqb.newline();

        // patient id
        if (patientId != null && patientId.size() > 0) {
            sqb.append("(obj.id = patId.registryobject AND	");
            sqb.newline();
            sqb.append(" patId.identificationScheme='"
                    + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSFolder_patientid_uuid)
                    + "' AND ");
            sqb.newline();
            sqb.append(" patId.value IN ");
            sqb.append(patientId);
            sqb.append(" ) ");
            sqb.newline();
        }
        sqb.newline();
        sqb.addCode(codes);
        sqb.addTimes("lastUpdateTime", "updateTimef", "updateTimet", lastUpdateTimeFrom, lastUpdateTimeTo, "obj");
        sqb.and();
        sqb.append(" obj.status IN ");
        sqb.append(RegistryCodedValueMapper.convertStatus_ValueToCode(status));
        return runQuery(sqb);
    }
}
