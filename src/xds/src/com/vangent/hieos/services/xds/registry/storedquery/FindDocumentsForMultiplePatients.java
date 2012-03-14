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
public class FindDocumentsForMultiplePatients extends StoredQuery {

    /**
     * 
     * @param response
     * @param logMessage
     * @param backendRegistry
     */
    public FindDocumentsForMultiplePatients(ErrorLogger response, XLogMessage logMessage, BackendRegistry backendRegistry) {
        super(response, logMessage, backendRegistry);
    }

    /**
     * 
     * @param params
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public FindDocumentsForMultiplePatients(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSDocumentEntryPatientId", false, true, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryClassCode", false, true, true, true, false, "$XDSDocumentEntryEventCodeList", "$XDSDocumentEntryHealthcareFacilityTypeCode");
        validateQueryParam("$XDSDocumentEntryPracticeSettingCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryCreationTimeFrom", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryCreationTimeTo", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStartTimeFrom", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStartTimeTo", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStopTimeFrom", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStopTimeTo", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryHealthcareFacilityTypeCode", false, true, true, true, false, "$XDSDocumentEntryEventCodeList", "$XDSDocumentEntryClassCode");
        validateQueryParam("$XDSDocumentEntryEventCodeList", false, true, true, true, true, "$XDSDocumentEntryClassCode", "$XDSDocumentEntryHealthcareFacilityTypeCode");
        validateQueryParam("$XDSDocumentEntryConfidentialityCode", false, true, true, true, true, (String[]) null);
        validateQueryParam("$XDSDocumentEntryFormatCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryStatus", true, true, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryAuthorPerson", false, true, true, false, false, (String[]) null);
        if (this.hasValidationErrors()) {
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
    public Metadata runInternal() throws XdsInternalException, XdsException, XDSRegistryOutOfResourcesException {
        if (this.isReturnLeafClass()) {
            this.setReturnLeafClass(false);
            OMElement refs = impl();
            Metadata m = MetadataParser.parseNonSubmission(refs);
            int objectRefsSize = m.getObjectRefs().size();
            // Guard against large leaf class queries.
            if (objectRefsSize > this.getMaxLeafObjectsAllowedFromQuery()) {
                throw new XDSRegistryOutOfResourcesException("FindDocumentsForMultiplePatients Stored Query for LeafClass is limited to 25 documents on this Registry. Your query targeted " + m.getObjectRefs().size() + " documents");
            }
            this.setReturnLeafClass(true); // Reset.
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
     * @throws XdsException
     */
    private OMElement impl() throws XdsInternalException, XdsException {
        SqParams params = this.getSqParams();
        List<String> patientId = params.getListParm("$XDSDocumentEntryPatientId");
        SQCodedTerm classCodes = params.getCodedParm("$XDSDocumentEntryClassCode");
        SQCodedTerm typeCodes = params.getCodedParm("$XDSDocumentEntryTypeCode");
        SQCodedTerm practiceSettingCodes = params.getCodedParm("$XDSDocumentEntryPracticeSettingCode");
        String creationTimeFrom = params.getIntParm("$XDSDocumentEntryCreationTimeFrom");
        String creationTimeTo = params.getIntParm("$XDSDocumentEntryCreationTimeTo");
        String serviceStartTimeFrom = params.getIntParm("$XDSDocumentEntryServiceStartTimeFrom");
        String serviceStartTimeTo = params.getIntParm("$XDSDocumentEntryServiceStartTimeTo");
        String serviceStopTimeFrom = params.getIntParm("$XDSDocumentEntryServiceStopTimeFrom");
        String serviceStopTimeTo = params.getIntParm("$XDSDocumentEntryServiceStopTimeTo");
        SQCodedTerm facilityTypeCodes = params.getCodedParm("$XDSDocumentEntryHealthcareFacilityTypeCode");
        SQCodedTerm eventCodes = params.getCodedParm("$XDSDocumentEntryEventCodeList");
        SQCodedTerm confidentialityCodes = params.getCodedParm("$XDSDocumentEntryConfidentialityCode");
        SQCodedTerm formatCodes = params.getCodedParm("$XDSDocumentEntryFormatCode");
        List<String> status = params.getListParm("$XDSDocumentEntryStatus");
        List<String> authorPerson = params.getListParm("$XDSDocumentEntryAuthorPerson");
        StoredQueryBuilder sqb = new StoredQueryBuilder(this.isReturnLeafClass());
        //sqb.initQuery();
        sqb.select("obj");
        sqb.append("FROM ExtrinsicObject obj");
        if (patientId != null && patientId.size() > 0) {
            sqb.append(", ExternalIdentifier patId");
            sqb.newline();
        }
        sqb.appendClassificationDeclaration(classCodes);
        sqb.appendClassificationDeclaration(typeCodes);
        sqb.appendClassificationDeclaration(practiceSettingCodes);
        sqb.appendClassificationDeclaration(facilityTypeCodes);  // $XDSDocumentEntryHealthcareFacilityTypeCode
        sqb.appendClassificationDeclaration(eventCodes); // $XDSDocumentEntryEventCodeList
        if (creationTimeFrom != null) {
            sqb.append(", Slot crTimef");
        }
        sqb.newline();                       // $XDSDocumentEntryCreationTimeFrom
        if (creationTimeTo != null) {
            sqb.append(", Slot crTimet");
        }
        sqb.newline();                       // $XDSDocumentEntryCreationTimeTo
        if (serviceStartTimeFrom != null) {
            sqb.append(", Slot serStartTimef");
        }
        sqb.newline();                 // $XDSDocumentEntryServiceStartTimeFrom
        if (serviceStartTimeTo != null) {
            sqb.append(", Slot serStartTimet");
        }
        sqb.newline();                 // $XDSDocumentEntryServiceStartTimeTo
        if (serviceStopTimeFrom != null) {
            sqb.append(", Slot serStopTimef");
        }
        sqb.newline();                  // $XDSDocumentEntryServiceStopTimeFrom
        if (serviceStopTimeTo != null) {
            sqb.append(", Slot serStopTimet");
        }
        sqb.newline();                  // $XDSDocumentEntryServiceStopTimeTo
        sqb.appendClassificationDeclaration(confidentialityCodes);  // $XDSDocumentEntryConfidentialityCode
        if (formatCodes != null) {
            sqb.append(", Classification fmtCode");
        }
        sqb.newline();             // $XDSDocumentEntryFormatCode
        if (authorPerson != null) {
            sqb.append(", Classification author");
            sqb.newline();
            sqb.append(", Slot authorperson");
        }
        sqb.newline();
        sqb.where();
        sqb.newline();

        // patient id
        if (patientId != null && patientId.size() > 0) {
            sqb.append("(obj.id = patId.registryobject AND	");
            sqb.newline();
            sqb.append(" patId.identificationScheme='"
                    + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSDocumentEntry_patientid_uuid)
                    + "' AND ");
            sqb.newline();
            sqb.append(" patId.value IN ");
            sqb.append(patientId);
            sqb.append(" ) ");
            sqb.newline();
        }
        sqb.addCode(classCodes);
        sqb.addCode(typeCodes);
        sqb.addCode(practiceSettingCodes);
        sqb.addTimes("creationTime", "crTimef", "crTimet", creationTimeFrom, creationTimeTo, "obj");
        sqb.addTimes("serviceStartTime", "serStartTimef", "serStartTimet", serviceStartTimeFrom, serviceStartTimeTo, "obj");
        sqb.addTimes("serviceStopTime", "serStopTimef", "serStopTimet", serviceStopTimeFrom, serviceStopTimeTo, "obj");
        sqb.addCode(facilityTypeCodes);
        sqb.addCode(eventCodes);
        sqb.addCode(confidentialityCodes);
        sqb.addCode(formatCodes);
        if (authorPerson != null) {
            for (String ap : authorPerson) {
                sqb.and();
                sqb.newline();
                sqb.append("(obj.id = author.classifiedObject AND ");
                sqb.newline();
                sqb.append("  author.classificationScheme='urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d' AND ");
                sqb.newline();
                sqb.append("  authorperson.parent = author.id AND");
                sqb.newline();
                sqb.append("  authorperson.name_ = 'authorPerson' AND");
                sqb.newline();
                sqb.append("  authorperson.value LIKE '" + ap + "' )");
                sqb.newline();
            }
        }
        sqb.and();
        sqb.append(" obj.status IN ");
        sqb.append(RegistryCodedValueMapper.convertStatus_ValueToCode(status));
        return runQuery(sqb);
    }
}
