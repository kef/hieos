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
import com.vangent.hieos.xutil.exception.XDSRegistryOutOfResourcesException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.response.ErrorLogger;
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
public class FindDocuments extends StoredQuery {

    /**
     *
     * @param response
     * @param log_message
     */
    public FindDocuments(ErrorLogger response, XLogMessage log_message) {
        super(response, log_message);
    }

    /**
     *
     * @param params
     * @param return_objects
     * @param response
     * @param log_message
     * @param is_secure
     * @throws MetadataValidationException
     */
    public FindDocuments(SqParams params, boolean return_objects, Response response, XLogMessage log_message)
            throws MetadataValidationException {
        super(params, return_objects, response, log_message);
        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSDocumentEntryPatientId", true, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryClassCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryTypeCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryPracticeSettingCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryCreationTimeFrom", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryCreationTimeTo", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStartTimeFrom", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStartTimeTo", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStopTimeFrom", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStopTimeTo", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryHealthcareFacilityTypeCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryEventCodeList", false, true, true, true, true, (String[]) null);
        validateQueryParam("$XDSDocumentEntryConfidentialityCode", false, true, true, true, true, (String[]) null);
        validateQueryParam("$XDSDocumentEntryFormatCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryStatus", true, true, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryAuthorPerson", false, true, true, false, false, (String[]) null);

        if (this.has_validation_errors) {
            throw new MetadataValidationException("Metadata Validation error present");
        }
    }

    /**
     *
     * @return
     * @throws XdsInternalException
     * @throws XdsException
     * @throws XDSRegistryOutOfResourcesException
     */
    public Metadata run_internal() throws XdsInternalException, XdsException, XDSRegistryOutOfResourcesException {
        if (this.return_leaf_class == true) {
            this.return_leaf_class = false;
            OMElement refs = impl();
            Metadata m = MetadataParser.parseNonSubmission(refs);
            int objectRefsSize = m.getObjectRefs().size();
            // Guard against large leaf class queries
            if (objectRefsSize > this.getMaxLeafObjectsAllowedFromQuery()) {
                throw new XDSRegistryOutOfResourcesException(
                        "FindDocuments Stored Query for LeafClass is limited to " + this.getMaxLeafObjectsAllowedFromQuery() + " documents on this Registry. Your query targeted " + m.getObjectRefs().size() + " documents");
            }
            this.return_leaf_class = true;
            if (objectRefsSize == 0) {
                return m;  // No need to go further and issue another query.
            }
        }
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

        // Parse query parameters:
        String patient_id = params.getStringParm("$XDSDocumentEntryPatientId");
        SQCodedTerm class_codes = params.getCodedParm("$XDSDocumentEntryClassCode");
        SQCodedTerm type_codes = params.getCodedParm("$XDSDocumentEntryTypeCode");
        SQCodedTerm practice_setting_codes = params.getCodedParm("$XDSDocumentEntryPracticeSettingCode");
        String creation_time_from = params.getIntParm("$XDSDocumentEntryCreationTimeFrom");
        String creation_time_to = params.getIntParm("$XDSDocumentEntryCreationTimeTo");
        String service_start_time_from = params.getIntParm("$XDSDocumentEntryServiceStartTimeFrom");
        String service_start_time_to = params.getIntParm("$XDSDocumentEntryServiceStartTimeTo");
        String service_stop_time_from = params.getIntParm("$XDSDocumentEntryServiceStopTimeFrom");
        String service_stop_time_to = params.getIntParm("$XDSDocumentEntryServiceStopTimeTo");
        SQCodedTerm hcft_codes = params.getCodedParm("$XDSDocumentEntryHealthcareFacilityTypeCode");
        SQCodedTerm event_codes = params.getCodedParm("$XDSDocumentEntryEventCodeList");
        SQCodedTerm conf_codes = params.getCodedParm("$XDSDocumentEntryConfidentialityCode");
        SQCodedTerm format_codes = params.getCodedParm("$XDSDocumentEntryFormatCode");
        List<String> status = params.getListParm("$XDSDocumentEntryStatus");
        List<String> author_person = params.getListParm("$XDSDocumentEntryAuthorPerson");

        init();
        select("obj");
        append("FROM ExtrinsicObject obj, ExternalIdentifier patId");
        newline();
        if (class_codes != null) {
            append(declareClassifications(class_codes));
        }
        if (type_codes != null) {
            append(declareClassifications(type_codes));
        }
        if (practice_setting_codes != null) {
            append(declareClassifications(practice_setting_codes));
        }
        if (hcft_codes != null) {
            append(declareClassifications(hcft_codes));
        }
        if (event_codes != null) {
            append(declareClassifications(event_codes));
        }
        if (conf_codes != null) {
            append(declareClassifications(conf_codes));
        }
        if (format_codes != null) {
            append(declareClassifications(format_codes));
        }
        if (creation_time_from != null) {
            append(", Slot crTimef");
        }
        newline();
        if (creation_time_to != null) {
            append(", Slot crTimet");
        }
        newline();
        if (service_start_time_from != null) {
            append(", Slot serStartTimef");
        }
        newline();
        if (service_start_time_to != null) {
            append(", Slot serStartTimet");
        }
        newline();
        if (service_stop_time_from != null) {
            append(", Slot serStopTimef");
        }
        newline();
        if (service_stop_time_to != null) {
            append(", Slot serStopTimet");
        }
        if (author_person != null) {
            append(", Classification author");
            newline();
            append(", Slot authorperson");
        }
        newline();
        append("WHERE");
        newline();
        // patientID
        append("(obj.id = patId.registryobject AND	");
        newline();
        append(" patId.identificationScheme='" + 
                RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSDocumentEntry_patientid_uuid)
                + "' AND ");
        newline();
        append(" patId.value = '");
        append(patient_id);
        append("' ) ");
        newline();

        this.addCode(class_codes);
        this.addCode(type_codes);
        this.addCode(practice_setting_codes);

        this.addTimes("creationTime", "crTimef", "crTimet", creation_time_from, creation_time_to, "obj");
        this.addTimes("serviceStartTime", "serStartTimef", "serStartTimet", service_start_time_from, service_start_time_to, "obj");
        this.addTimes("serviceStopTime", "serStopTimef", "serStopTimet", service_stop_time_from, service_stop_time_to, "obj");

        this.addCode(hcft_codes);
        this.addCode(event_codes);
        this.addCode(conf_codes);
        this.addCode(format_codes);

        if (author_person != null) {
            for (String ap : author_person) {
                append("AND");
                newline();
                append("(obj.id = author.classifiedObject AND ");
                newline();
                append("  author.classificationScheme='urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d' AND ");
                newline();
                append("  authorperson.parent = author.id AND");
                newline();
                append("  authorperson.name_ = 'authorPerson' AND");
                newline();
                append("  authorperson.value LIKE '" + ap + "' )");
                newline();
            }
        }
        append("AND obj.status IN ");
        append(RegistryCodedValueMapper.convertStatus_ValueToCode(status));
        return query();
    }
}
