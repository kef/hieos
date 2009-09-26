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
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.query.StoredQuery;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.axiom.om.OMElement;

/**
 * 
 * @author thumbe
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
    public FindDocuments(HashMap params, boolean return_objects, Response response, XLogMessage log_message, boolean is_secure)
            throws MetadataValidationException {
        super(params, return_objects, response, log_message, is_secure);
        //                         param name,                                      required?, multiple?, is string?,   same size as,                                alternative
        validate_parm(params, "$XDSDocumentEntryPatientId", true, false, true, null, null);
        validate_parm(params, "$XDSDocumentEntryClassCode", false, true, true, null, null);
        validate_parm(params, "$XDSDocumentEntryClassCodeScheme", false, true, true, "$XDSDocumentEntryClassCode", null);
        validate_parm(params, "$XDSDocumentEntryPracticeSettingCode", false, true, true, null, null);
        validate_parm(params, "$XDSDocumentEntryPracticeSettingCodeScheme", false, true, true, "$XDSDocumentEntryPracticeSettingCode", null);
        validate_parm(params, "$XDSDocumentEntryCreationTimeFrom", false, false, true, null, null);
        validate_parm(params, "$XDSDocumentEntryCreationTimeTo", false, false, true, null, null);
        validate_parm(params, "$XDSDocumentEntryServiceStartTimeFrom", false, false, true, null, null);
        validate_parm(params, "$XDSDocumentEntryServiceStartTimeTo", false, false, true, null, null);
        validate_parm(params, "$XDSDocumentEntryServiceStopTimeFrom", false, false, true, null, null);
        validate_parm(params, "$XDSDocumentEntryServiceStopTimeTo", false, false, true, null, null);
        validate_parm(params, "$XDSDocumentEntryHealthcareFacilityTypeCode", false, true, true, null, null);
        validate_parm(params, "$XDSDocumentEntryHealthcareFacilityTypeCodeScheme", false, true, true, "$XDSDocumentEntryHealthcareFacilityTypeCode", null);
        validate_parm(params, "$XDSDocumentEntryEventCodeList", false, true, true, null, null);
        validate_parm(params, "$XDSDocumentEntryEventCodeListScheme", false, true, true, "$XDSDocumentEntryEventCodeList", null);
        validate_parm(params, "$XDSDocumentEntryConfidentialityCode", false, true, true, null, null);
        validate_parm(params, "$XDSDocumentEntryConfidentialityCodeScheme", false, true, true, "$XDSDocumentEntryConfidentialityCode", null);
        validate_parm(params, "$XDSDocumentEntryFormatCode", false, true, true, null, null);
        validate_parm(params, "$XDSDocumentEntryStatus", true, true, true, null, null);
        validate_parm(params, "$XDSDocumentEntryAuthorPerson", false, true, true, null, null);

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
            if (m.getObjectRefs().size() > 25) {
                throw new XDSRegistryOutOfResourcesException("GetDocuments Stored Query for LeafClass is limited to 25 documents on this Registry. Your query targeted " + m.getObjectRefs().size() + " documents");
            }
            this.return_leaf_class = true;
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
        String patient_id = this.get_string_parm("$XDSDocumentEntryPatientId");
        ArrayList<Object> class_codes = this.get_andor_parm("$XDSDocumentEntryClassCode");
        ArrayList<String> class_code_varnames = this.getAndorVarNames(class_codes, "clCode");
        ArrayList<String> class_code_schemes = this.get_arraylist_parm("$XDSDocumentEntryClassCodeScheme");
        if (class_code_varnames.size() > 1 && class_code_schemes.size() > 1) {
            throw new MetadataValidationException("classCode: both classCodeScheme and AND logic specified");
        }
        ArrayList<Object> practice_setting_codes = this.get_andor_parm("$XDSDocumentEntryPracticeSettingCode");
        ArrayList<String> practice_setting_code_varnames = this.getAndorVarNames(practice_setting_codes, "psc");
        ArrayList<String> practice_setting_codes_schemes = this.get_arraylist_parm("$XDSDocumentEntryPracticeSettingCodeScheme");
        if (practice_setting_code_varnames != null && practice_setting_code_varnames.size() > 1 &&
                practice_setting_codes_schemes != null && practice_setting_codes_schemes.size() > 1) {
            throw new MetadataValidationException("practiceSettingCode: both practiceSettingCodeScheme and AND logic specified");
        }

        String creation_time_from = this.get_int_parm("$XDSDocumentEntryCreationTimeFrom");
        String creation_time_to = this.get_int_parm("$XDSDocumentEntryCreationTimeTo");
        String service_start_time_from = this.get_int_parm("$XDSDocumentEntryServiceStartTimeFrom");
        String service_start_time_to = this.get_int_parm("$XDSDocumentEntryServiceStartTimeTo");
        String service_stop_time_from = this.get_int_parm("$XDSDocumentEntryServiceStopTimeFrom");
        String service_stop_time_to = this.get_int_parm("$XDSDocumentEntryServiceStopTimeTo");
        ArrayList<Object> hcft_codes = this.get_andor_parm("$XDSDocumentEntryHealthcareFacilityTypeCode");
        ArrayList<String> hcft_code_varnames = this.getAndorVarNames(hcft_codes, "hftc");
        ArrayList<String> hcft_code_schemes = this.get_arraylist_parm("$XDSDocumentEntryHealthcareFacilityTypeCodeScheme");
        if (hcft_code_varnames != null && hcft_code_varnames.size() > 1 &&
                hcft_code_schemes != null && hcft_code_schemes.size() > 1) {
            throw new MetadataValidationException("healthcareFacilityTypeCode: both healthcareFacilityTypeCodeScheme and AND logic specified");
        }

        ArrayList<Object> event_codes = this.get_andor_parm("$XDSDocumentEntryEventCodeList");
        ArrayList<String> event_code_varnames = this.getAndorVarNames(event_codes, "ecl");
        ArrayList<String> event_code_schemes = this.get_arraylist_parm("$XDSDocumentEntryEventCodeListScheme");
        if (event_code_varnames != null && event_code_varnames.size() > 1 &&
                event_code_schemes != null && event_code_schemes.size() > 1) {
            throw new MetadataValidationException("eventCode: both eventCodeScheme and AND logic specified");
        }

        ArrayList<Object> conf_codes = this.get_andor_parm("$XDSDocumentEntryConfidentialityCode");
        ArrayList<String> conf_code_schemes = this.get_arraylist_parm("$XDSDocumentEntryConfidentialityCodeScheme");
        ArrayList<String> conf_code_varnames = this.getAndorVarNames(conf_codes, "conf");
        if (conf_code_varnames != null && conf_code_varnames.size() > 1 &&
                conf_code_schemes != null && conf_code_schemes.size() > 1) {
            throw new MetadataValidationException("confidentialityCode: both confidentialityCodeScheme and AND logic specified");
        }

        ArrayList<String> format_codes = this.get_arraylist_parm("$XDSDocumentEntryFormatCode");
        ArrayList<String> status = this.get_arraylist_parm("$XDSDocumentEntryStatus");
        ArrayList<String> author_person = this.get_arraylist_parm("$XDSDocumentEntryAuthorPerson");

        init();

        if (this.return_leaf_class) {
            a("SELECT *  ");
            n();
        } else {
            a("SELECT doc.id  ");
            n();
        }
        a("FROM ExtrinsicObject doc, ExternalIdentifier patId");
        n();
        if (class_codes != null) {
            a(declareClassifications(class_code_varnames));
        }
        if (class_code_schemes != null) {
            a(", Slot clCodeScheme");
        }
        n();
        if (practice_setting_codes != null) {
            a(declareClassifications(practice_setting_code_varnames));
        }
        if (practice_setting_codes_schemes != null) {
            a(", Slot psCodeScheme");
        }
        n();
        if (hcft_codes != null) {
            a(declareClassifications(hcft_code_varnames));  // $XDSDocumentEntryHealthcareFacilityTypeCode
        }
        if (hcft_code_schemes != null) {
            a(", Slot hftcScheme");
        }
        n();                    // $XDSDocumentEntryHealthcareFacilityTypeCodeScheme
        if (event_codes != null) {
            a(declareClassifications(event_code_varnames)); // $XDSDocumentEntryEventCodeList
        }
        if (event_code_schemes != null) {
            a(", Slot eclScheme");
        }
        n();                     // $XDSDocumentEntryEventCodeListScheme
        if (creation_time_from != null) {
            a(", Slot crTimef");
        }
        n();                       // $XDSDocumentEntryCreationTimeFrom
        if (creation_time_to != null) {
            a(", Slot crTimet");
        }
        n();                       // $XDSDocumentEntryCreationTimeTo
        if (service_start_time_from != null) {
            a(", Slot serStartTimef");
        }
        n();                 // $XDSDocumentEntryServiceStartTimeFrom
        if (service_start_time_to != null) {
            a(", Slot serStartTimet");
        }
        n();                 // $XDSDocumentEntryServiceStartTimeTo
        if (service_stop_time_from != null) {
            a(", Slot serStopTimef");
        }
        n();                  // $XDSDocumentEntryServiceStopTimeFrom
        if (service_stop_time_to != null) {
            a(", Slot serStopTimet");
        }
        n();                  // $XDSDocumentEntryServiceStopTimeTo
        if (conf_codes != null) {
            a(declareClassifications(conf_code_varnames));   // $XDSDocumentEntryConfidentialityCode
        }
        if (conf_code_schemes != null) {
            a(", Slot confScheme");
        }
        n();
        if (format_codes != null) {
            a(", Classification fmtCode");
        }
        n();             // $XDSDocumentEntryFormatCode
        if (author_person != null) {
            a(", Classification author");
        }
        n();
        if (author_person != null) {
            a(", Slot authorperson");
        }
        n();


        a("WHERE");
        n();
        //   a("doc.objectType = 'urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1'"); n();
        //   a("AND ");
        // patientID
        a("(doc.id = patId.registryobject AND	");
        n();
        a("  patId.identificationScheme='urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427' AND ");
        n();
        a("  patId.value = '");
        a(patient_id);
        a("' ) ");
        n();

        this.add_code(class_code_varnames, "clCodeScheme", MetadataSupport.XDSDocumentEntry_classCode_uuid, class_codes, class_code_schemes);
        this.add_code(practice_setting_code_varnames, "psCodeScheme", MetadataSupport.XDSDocumentEntry_psCode_uuid, practice_setting_codes, practice_setting_codes_schemes);

        this.add_times("creationTime", "crTimef", "crTimet", creation_time_from, creation_time_to, "doc");
        this.add_times("serviceStartTime", "serStartTimef", "serStartTimet", service_start_time_from, service_start_time_to, "doc");
        this.add_times("serviceStopTime", "serStopTimef", "serStopTimet", service_stop_time_from, service_stop_time_to, "doc");

        this.add_code(hcft_code_varnames, "hftcScheme", MetadataSupport.XDSDocumentEntry_hcftCode_uuid, hcft_codes, hcft_code_schemes);
        this.add_code(event_code_varnames, "eclScheme", MetadataSupport.XDSDocumentEntry_eventCode_uuid, event_codes, event_code_schemes);
        this.add_code(conf_code_varnames, "confScheme", MetadataSupport.XDSDocumentEntry_confCode_uuid, conf_codes, conf_code_schemes);

        this.add_code("fmtCode", "", "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d", format_codes, null);

        if (author_person != null) {
            for (String ap : author_person) {
                a("AND");
                n();
                a("(doc.id = author.classifiedObject AND ");
                n();
                a("  author.classificationScheme='urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d' AND ");
                n();
                a("  authorperson.parent = author.id AND");
                n();
                a("  authorperson.name = 'authorPerson' AND");
                n();
                a("  authorperson.value LIKE '" + ap + "' )");
                n();
            }
        }
        a("AND doc.status IN ");
        a(status);
        return query(this.return_leaf_class);
    }
}
