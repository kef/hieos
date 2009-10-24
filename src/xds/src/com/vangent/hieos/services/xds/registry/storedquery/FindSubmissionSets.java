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
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.SQCodedTerm;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.query.StoredQuery;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class FindSubmissionSets extends StoredQuery {

    /**
     *
     * @param params
     * @param return_objects
     * @param response
     * @param log_message
     * @param is_secure
     * @throws MetadataValidationException
     */
    public FindSubmissionSets(SqParams params, boolean return_objects, Response response, XLogMessage log_message, boolean is_secure)
            throws MetadataValidationException {
        super(params, return_objects, response, log_message, is_secure);

        // param name, required?, multiple?, is string?, is code?, alternative
        validateQueryParam("$XDSSubmissionSetPatientId", true, false, true, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetSourceId", false, true, true, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetSubmissionTimeFrom", false, false, true, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetSubmissionTimeTo", false, false, true, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetAuthorPerson", false, false, true, false, (String[]) null);
        validateQueryParam("$XDSSubmissionSetContentType", false, true, true, true, (String[]) null);
        validateQueryParam("$XDSSubmissionSetStatus", true, true, true, false, (String[]) null);
        if (this.has_validation_errors) {
            throw new MetadataValidationException("Metadata Validation error present");
        }
    }

    /**
     * 
     * @return
     * @throws XdsInternalException
     * @throws XdsException
     */
    public Metadata run_internal() throws XdsInternalException, XdsException {
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
     * @throws XdsException
     */
    OMElement impl() throws XdsInternalException, XdsException {
        String patient_id = this.get_string_parm("$XDSSubmissionSetPatientId");
        ArrayList<String> source_id = this.get_arraylist_parm("$XDSSubmissionSetSourceId");
        String submission_time_from = this.get_int_parm("$XDSSubmissionSetSubmissionTimeFrom");
        String submission_time_to = this.get_int_parm("$XDSSubmissionSetSubmissionTimeTo");
        String author_person = this.get_string_parm("$XDSSubmissionSetAuthorPerson");
        SQCodedTerm content_type = params.getCodedParm("$XDSSubmissionSetContentType");
        ArrayList<String> status = this.get_arraylist_parm("$XDSSubmissionSetStatus");

        init();
        if (this.return_leaf_class) {
            append("SELECT *  ");
            newline();
        } else {
            append("SELECT obj.id  ");
            newline();
        }
        append("FROM RegistryPackage obj, ExternalIdentifier patId");
        newline();
        if (source_id != null) {
            append(", ExternalIdentifier srcId");
        }
        newline();
        if (submission_time_from != null) {
            append(", Slot sTimef");
        }
        newline();
        if (submission_time_to != null) {
            append(", Slot sTimet");
        }
        newline();
        if (author_person != null) {
            append(", Classification author");
        }
        newline();
        if (author_person != null) {
            append(", Slot authorperson");
        }
        newline();
        if (content_type != null) {
            append(declareClassifications(content_type));
        }
        newline();


        append("WHERE");
        newline();
        // patientID
        append("(obj.id = patId.registryobject AND	");
        newline();
        append("  patId.identificationScheme='urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446' AND ");
        newline();
        append("  patId.value = '");
        append(patient_id);
        append("' ) ");
        newline();

        if (source_id != null) {
            append("AND");
            newline();
            append("(obj.id = srcId.registryobject AND	");
            newline();
            append("  srcId.identificationScheme='urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832' AND ");
            newline();
            append("  srcId.value IN ");
            append(source_id);
            append(" ) ");
            newline();
        }
        this.addTimes("submissionTime", "sTimef", "sTimet", submission_time_from, submission_time_to, "obj");
        if (author_person != null) {
            append("AND");
            newline();
            append("(obj.id = author.classifiedObject AND ");
            newline();
            append("  author.classificationScheme='urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d' AND ");
            newline();
            append("  authorperson.parent = author.id AND");
            newline();
            append("  authorperson.name = 'authorPerson' AND");
            newline();
            append("  authorperson.value LIKE '" + author_person + "' )");
            newline();
        }
        this.addCode(content_type);
        append("AND obj.status IN ");
        append(status);
        return query(this.return_leaf_class);
    }
}
