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

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.SQCodedTerm;
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
public class FindFolders extends StoredQuery {

    /**
     *
     * @param params
     * @param return_objects
     * @param response
     * @param log_message
     * @param is_secure
     * @throws MetadataValidationException
     */
    public FindFolders(SqParams params, boolean return_objects, Response response, XLogMessage log_message)
            throws MetadataValidationException {
        super(params, return_objects, response, log_message);
        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSFolderPatientId", true, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSFolderLastUpdateTimeFrom", false, false, false, false, false, (String[]) null);
        validateQueryParam("$XDSFolderLastUpdateTimeTo", false, false, false, false, false, (String[]) null);
        validateQueryParam("$XDSFolderCodeList", false, true, true, true, true, (String[]) null);
        validateQueryParam("$XDSFolderStatus", true, true, true, false, false, (String[]) null);
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
        OMElement results = impl();
        Metadata m = MetadataParser.parseNonSubmission(results);
        if (log_message != null) {
            log_message.addOtherParam("Results structure", m.structure());
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
    OMElement impl() throws XdsInternalException, MetadataException, XdsException {
        String patient_id = params.getStringParm("$XDSFolderPatientId");
        String update_time_from = params.getIntParm("$XDSFolderLastUpdateTimeFrom");
        String update_time_to = params.getIntParm("$XDSFolderLastUpdateTimeTo");
        SQCodedTerm codes = params.getCodedParm("$XDSFolderCodeList");
        List<String> status = params.getListParm("$XDSFolderStatus");
        if (patient_id == null || patient_id.length() == 0) {
            throw new XdsException("Patient ID parameter empty");
        }
        if (status.size() == 0) {
            throw new XdsException("Status parameter empty");
        }
        init();
        select("obj");
        append("FROM RegistryPackage obj, ExternalIdentifier patId");
        newline();
        if (update_time_from != null) {
            append(", Slot updateTimef");
        }
        newline();
        if (update_time_to != null) {
            append(", Slot updateTimet");
        }
        newline();
        if (codes != null) {
            append(declareClassifications(codes));
        }
        newline();
        append("WHERE");
        newline();

        // patientID
        append("(obj.id = patId.registryobject AND	");
        newline();
        append(" patId.identificationScheme='urn:uuid:f64ffdf0-4b97-4e06-b79f-a52b38ec2f8a' AND ");
        newline();
        append(" patId.value = '");
        append(patient_id);
        append("' ) AND");
        newline();
        append(" obj.status IN ");
        append(status);
        newline();
        this.addTimes("lastUpdateTime", "updateTimef", "updateTimet", update_time_from, update_time_to, "obj");
        this.addCode(codes);
        return query();
    }
}
