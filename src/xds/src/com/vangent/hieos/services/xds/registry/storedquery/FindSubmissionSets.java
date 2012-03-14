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
public class FindSubmissionSets extends StoredQuery {

    /**
     * 
     * @param params
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public FindSubmissionSets(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);
        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSSubmissionSetPatientId", true, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetSourceId", false, true, true, false, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetSubmissionTimeFrom", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetSubmissionTimeTo", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetAuthorPerson", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetContentType", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetStatus", true, true, true, false, false, (String[]) null);

        if (this.hasValidationErrors()) {
            throw new MetadataValidationException("Metadata Validation error present");
        }
    }

    /**
     * 
     * @return
     * @throws XdsInternalException
     * @throws XdsException
     */
    public Metadata runInternal() throws XdsInternalException, XdsException {
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
     * @throws XdsException
     */
    private OMElement impl() throws XdsInternalException, XdsException {
        SqParams params = this.getSqParams();
        String patientId = params.getStringParm("$XDSSubmissionSetPatientId");
        List<String> sourceId = params.getListParm("$XDSSubmissionSetSourceId");
        String submissionTimeFrom = params.getIntParm("$XDSSubmissionSetSubmissionTimeFrom");
        String submissionTimeTo = params.getIntParm("$XDSSubmissionSetSubmissionTimeTo");
        String authorPerson = params.getStringParm("$XDSSubmissionSetAuthorPerson");
        SQCodedTerm contentType = params.getCodedParm("$XDSSubmissionSetContentType");
        List<String> status = params.getListParm("$XDSSubmissionSetStatus");

        StoredQueryBuilder sqb = new StoredQueryBuilder(this.isReturnLeafClass());
        sqb.select("obj");
        sqb.append("FROM RegistryPackage obj, ExternalIdentifier patId");
        sqb.newline();
        if (sourceId != null) {
            sqb.append(", ExternalIdentifier srcId");
        }
        sqb.newline();
        if (submissionTimeFrom != null) {
            sqb.append(", Slot sTimef");
        }
        sqb.newline();
        if (submissionTimeTo != null) {
            sqb.append(", Slot sTimet");
        }
        sqb.newline();
        if (authorPerson != null) {
            sqb.append(", Classification author");
            sqb.newline();
            sqb.append(", Slot authorperson");
        }
        sqb.newline();
        sqb.appendClassificationDeclaration(contentType);
        sqb.newline();

        sqb.append("WHERE");
        sqb.newline();
        // patientID
        sqb.append("(obj.id = patId.registryobject AND	");
        sqb.newline();
        sqb.append("  patId.identificationScheme='"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSSubmissionSet_patientid_uuid)
                + "' AND ");
        sqb.newline();
        sqb.append("  patId.value = '");
        sqb.append(patientId);
        sqb.append("' ) ");
        sqb.newline();

        if (sourceId != null) {
            sqb.append("AND");
            sqb.newline();
            sqb.append("(obj.id = srcId.registryobject AND	");
            sqb.newline();
            sqb.append("  srcId.identificationScheme='"
                    + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSSubmissionSet_sourceid_uuid)
                    + "' AND ");
            sqb.newline();
            sqb.append("  srcId.value IN ");
            sqb.append(sourceId);
            sqb.append(" ) ");
            sqb.newline();
        }
        sqb.addTimes("submissionTime", "sTimef", "sTimet", submissionTimeFrom, submissionTimeTo, "obj");
        if (authorPerson != null) {
            sqb.append("AND");
            sqb.newline();
            sqb.append("(obj.id = author.classifiedObject AND ");
            sqb.newline();
            sqb.append("  author.classificationScheme='urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d' AND ");
            sqb.newline();
            sqb.append("  authorperson.parent = author.id AND");
            sqb.newline();
            sqb.append("  authorperson.name_ = 'authorPerson' AND");
            sqb.newline();
            sqb.append("  authorperson.value LIKE '" + authorPerson + "' )");
            sqb.newline();
        }
        sqb.addCode(contentType);
        sqb.append("AND obj.status IN ");
        sqb.append(RegistryCodedValueMapper.convertStatus_ValueToCode(status));
        return runQuery(sqb);
    }
}
