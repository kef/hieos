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
package com.vangent.hieos.xutil.metadata.validation;

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.RegistryErrorList;

import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class Attribute {

    private final static String DOCUMENT_TEXT = "Document";
    private final static String FOLDER_TEXT = "Folder";
    private final static String SUBMISSION_SET_TEXT = "Submission Set";
    private Metadata m;
    private RegistryErrorList rel;
    private ArrayList<String> ssSlots;
    private ArrayList<String> docSlots;
    private ArrayList<String> folSlots;
    private XLogMessage logMessage = null;

    /**
     *
     * @param m
     * @throws XdsInternalException
     */
    public Attribute(Metadata m) throws XdsInternalException {
        this.m = m;
        rel = new RegistryErrorList(false /* log */);
        init();
    }

    /**
     * 
     * @param m
     * @param rel
     * @param logMessage
     * @throws XdsInternalException
     */
    public Attribute(Metadata m, RegistryErrorList rel, XLogMessage logMessage) throws XdsInternalException {
        this.m = m;
        this.rel = rel;
        this.logMessage = logMessage;
        init();
    }

    /**
     *
     */
    private void init() {
        // Submission set slots:
        ssSlots = new ArrayList<String>();
        ssSlots.add("submissionTime");
        ssSlots.add("intendedRecipient");

        // Document slots:
        docSlots = new ArrayList<String>();
        docSlots.add("creationTime");
        docSlots.add("languageCode");
        docSlots.add("sourcePatientId");
        docSlots.add("sourcePatientInfo");
        docSlots.add("intendedRecipient");
        docSlots.add("legalAuthenticator");
        docSlots.add("serviceStartTime");
        docSlots.add("serviceStopTime");
        docSlots.add("hash");
        docSlots.add("size");
        docSlots.add("URI");
        docSlots.add("repositoryUniqueId");
        docSlots.add("documentAvailability");

        // Folder slots:
        folSlots = new ArrayList<String>();
        folSlots.add("lastUpdateTime");
    }

    /**
     *
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public void run() throws MetadataException, MetadataValidationException {
        validateSubmissionSet();
        validateDocuments();
        validateFolders();
        validateNecessaryAttributes();
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateDocuments() throws MetadataException {
        validateDocumentSlotsAreLegal();
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateSubmissionSet() throws MetadataException {
        validateSubmissionSetSlotsAreLegal();
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateFolders() throws MetadataException {
        validateFolderSlotsAreLegal();
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateNecessaryAttributes() throws MetadataException {
        // Submission set validations.
        validateSubmissionSetExternalIdentifiers();
        validateSubmissionSetSlots();
        validateSubmissionSetClassifications();

        // Document validations.
        validateDocumentSlots();
        validateDocumentExternalIdentifiers();
        validateDocumentClassifications();
        validateSpecialDocumentSlots();

        // Folder validations.
        validateFolderSlots();
        validateFolderExternalIdentifiers();
        validateFolderClassifications();

        validateRegistryPackageClassifications();

        // validate special structure in some classifications and slots and associations and externalids
        validateSpecialClassifications();
    }

    /**
     * Validate HL7 V2 (V2.5) Organization Name (XON) data type.
     *
     * FROM:
     * IHE ITI Technical Framework, Volume 3 (ITI TF-3): Cross-Transaction and Content Specifications
     *
     * This type provides the name and identification of an organization. This specification
     * restricts the coding to the following fields:
     * XON.1 – Organization Name – this field is required
     * XON.6.2 – Assigning Authority Universal Id – this field is required if XON.10 is valued and not an OID
     * XON.6.3 – Assigning Authority Universal Id Type – this field is required if XON.10 is valued and not
     *           an OID and shall have the value “ISO”
     * XON.10 – Organization Identifier – this field is optional
     *
     * Examples:
     *  Some Hospital
     *  Some Hospital^^^^^^^^^1.2.3.4.5.6.7.8.9.1789.45
     *  Some Hospital^^^^^&1.2.3.4.5.6.7.8.9.1789&ISO^^^^45
     *
     * @param value The slot value (supposably in XON format) to validate.
     * @return List<String> of errors.  The empty list if none found.
     */
    private List<String> validate_XON(String value) {
        List<String> errs = new ArrayList<String>();
        // Split the string into its component parts:
        String[] parts = value.split("\\^");
        if (parts.length < 1) {
            errs.add("No value specified for XON slot");
            return errs;  // EARLY EXIT!
        }
        // Get XON.1 part:
        String xon_1 = parts[0];
        if (xon_1.length() == 0) {
            errs.add("XON.1 missing");
        }

        // Get other XON parts:
        String xon_6 = (parts.length < 6) ? "" : parts[5];
        xon_6 = xon_6.replaceAll("\\&amp;", "&");
        String xon_10 = (parts.length < 10) ? "" : parts[9];
        String[] xon_6_parts = xon_6.split("\\&");
        String xon_6_2 = (xon_6_parts.length < 2) ? "" : xon_6_parts[1];
        String xon_6_3 = (xon_6_parts.length < 3) ? "" : xon_6_parts[2];

        // Perform validation rules:
        if (xon_10.length() > 0 && !isOID(xon_10)) {
            if (xon_6_2.length() == 0) {
                errs.add("XON.10 is valued and not an OID so XON.6.2 is required");
            } else if (!isOID(xon_6_2)) {
                errs.add("XON.6.2 must be an OID");
            }
            if (!xon_6_3.equals("ISO")) {
                errs.add("XON.10 is valued and not an OID so XON.6.3 is required to have the value ISO");
            }
        }
        // Check to see if other XON positions have any values:
        for (int i = 1; i <= 10; i++) {
            if (i == 1 || i == 6 || i == 10) {
                continue;
            }
            if (parts.length < i) {
                continue;
            }
            if (parts[i - 1].length() > 0) {
                errs.add("Only XON.1, XON.6, XON.10 are allowed to have values: found value in XON." + i);
            }
        }
        return errs;
    }

    /**
     *
     * @param authorInstitutionSlotName
     */
    private void validateAuthorInstitutionSlot(OMElement authorInstitutionSlotName) {
        OMElement valueList = MetadataSupport.firstChildWithLocalName(authorInstitutionSlotName, "ValueList");
        if (valueList == null) {
            err("authorInstitution Slot has no ValueList");
            return;
        }
        List<OMElement> values = MetadataSupport.childrenWithLocalName(valueList, "Value");
        for (OMElement valueElement : values) {
            String value = valueElement.getText();
            List<String> errs = this.validate_XON(value);
            for (String err : errs) {
                err("authorInstitution: " + err);
            }
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateSpecialClassifications() throws MetadataException {
        List<OMElement> classifications = m.getClassifications();

        for (OMElement classification : classifications) {
            //OMElement classElement = (OMElement) classifications.get(i);
            String classificationScheme = classification.getAttributeValue(MetadataSupport.classificationscheme_qname);
            String classifiedObjectId = classification.getAttributeValue(MetadataSupport.classified_object_qname);
            OMElement classifiedObject = m.getObjectById(classifiedObjectId);
            String classifiedObjectType = (classifiedObject == null) ? "Unknown type" : classifiedObject.getLocalName();
            String nodeRepresentation = classification.getAttributeValue(MetadataSupport.noderepresentation_qname);
            if (classificationScheme != null
                    && (classificationScheme.equals(MetadataSupport.XDSDocumentEntry_author_uuid)
                    || classificationScheme.equals(MetadataSupport.XDSSubmissionSet_author_uuid))) {
                // doc.author or ss.author

                if (nodeRepresentation == null || (nodeRepresentation != null && !nodeRepresentation.equals(""))) {
                    err(classifiedObjectType + " " + classifiedObjectId + " has a author type classification (classificationScheme="
                            + classificationScheme + ") with no nodeRepresentation attribute.  It is required and must be the empty string.");
                }

                String authorPereson = m.getSlotValue(classification, "authorPerson", 0);
                if (authorPereson == null) {
                    err(classifiedObjectType + " " + classifiedObjectId + " has a author type classification (classificationScheme="
                            + classificationScheme + ") with no authorPerson slot.  One is required.");
                }
//				if ( ! is_xcn_format(author_person))
//					err(classified_object_type + " " + classified_object_id + " has a author type classification (classificationScheme=" +
//							class_scheme + ") with authorPerson slot that is not in XCN format. The value found was " + author_person	);

                if (m.getSlotValue(classification, "authorPerson", 1) != null) {
                    err(classifiedObjectType + " " + classifiedObjectId + " has a author type classification (classificationScheme="
                            + classificationScheme + ") with multiple values in the authorPerson slot.  Only one is allowed. To document a second author, create a second Classification object");
                }

                for (OMElement slot : MetadataSupport.childrenWithLocalName(classification, "Slot")) {
                    String slotName = slot.getAttributeValue(MetadataSupport.slot_name_qname);
                    if (slotName != null
                            && (slotName.equals("authorPerson") || // FIXME: Should probably validate as XCN
                            slotName.equals("authorRole")
                            || slotName.equals("authorSpecialty"))) {
                        // Do nothing.
                    } else if (slotName != null && slotName.equals("authorInstitution")) {
                        validateAuthorInstitutionSlot(slot);
                    } else {
                        err(classifiedObjectType + " " + classifiedObjectId + " has a author type classification (classificationScheme="
                                + classificationScheme + ") with an unknown type of slot with name " + slotName + ".  Only XDS prescribed slots are allowed inside this classification");
                    }
                }
            }
        }
    }

    /**
     *
     * @param value
     * @return
     */
    /*private boolean is_xcn_format(String value) {
    int count = 0;
    for (int i = 0; i < value.length(); i++) {
    if (value.charAt(i) == '^') {
    count++;
    }
    }
    return (count == 5);
    }*/
    /**
     * 
     * @param pid
     * @return
     */
    private String validate_CX_datatype(String pid) {
        if (pid == null) {
            return "No Patient ID found";
        }
        String[] parts = pid.split("\\^\\^\\^");
        if (parts.length != 2) {
            return "Not Patient ID format: ^^^ not found:";
        }
        String part2 = parts[1];
        part2 = part2.replaceAll("&amp;", "&");
        String[] partsa = part2.split("&");
        if (partsa.length != 3) {
            return "Expected &OID&ISO after ^^^ in CX data type (bad pid = " + pid + ")";
        }
        if (partsa[0].length() != 0) {
            return "Expected &OID&ISO after ^^^ in CX data type (bad pid = " + pid + ")";
        }
        if (!partsa[2].equals("ISO")) {
            return "Expected &OID&ISO after ^^^ in CX data type (bad pid = " + pid + ")";
        }
        if (!isOID(partsa[1])) {
            return "Expected &OID&ISO after ^^^ in CX data type: OID part does not parse as an OID (bad pid = "
                    + pid + ")";
        }
        return null;
    }

    /**
     * 
     * @param slotName
     */
    private void validateSourcePatientIdSlot(OMElement slotName) {
        OMElement valueList = MetadataSupport.firstChildWithLocalName(slotName, "ValueList");
        List<OMElement> values = MetadataSupport.childrenWithLocalName(valueList, "Value");
        if (values.size() != 1) {
            err("sourcePatientId must have exactly one value");
            return;
        }
        String msg = validate_CX_datatype(values.get(0).getText());
        if (msg != null) {
            err("Slot sourcePatientId format error: " + msg);
        }
    }

    /**
     *
     * @param slotName
     */
    private void validateSourcePatientInfoSlot(OMElement slotName) {
        OMElement valueList = MetadataSupport.firstChildWithLocalName(slotName, "ValueList");
        for (OMElement value : MetadataSupport.childrenWithLocalName(valueList, "Value")) {
            String content = value.getText();
            if (content == null || content.equals("")) {
                err("Slot sourcePatientInfo has empty Slot value");
                continue;
            }
            String[] parts = content.split("\\|");
            if (parts.length != 2) {
                err("Slot sourcePatientInfo Value must have two parts separated by | ");
                continue;
            }
            if (!parts[0].startsWith("PID-")) {
                err("Slot sourcePatientInfo Values must start with PID- ");
                continue;
            }
            if (parts[0].startsWith("PID-3")) {
                String msg = validate_CX_datatype(parts[1]);
                if (msg != null) {
                    err("Slot sourcePatientInfo#PID-3 must be valid Patient ID: " + msg);
                }
            }
        }
    }

    /**
     *
     * @param slotName
     */
    private void validateDocumentAvailabilitySlot(OMElement slotName) {
        OMElement valueList = MetadataSupport.firstChildWithLocalName(slotName, "ValueList");
        for (OMElement value : MetadataSupport.childrenWithLocalName(valueList, "Value")) {
            String content = value.getText();
            if (content == null || content.equals("")) {
                err("Slot documentAvailability has empty Slot value");
            } else if (!(content.equals(MetadataSupport.document_availability_offline)
                    || content.equals(MetadataSupport.document_availability_online))) {
                err(content + " is not a valid value for Slot documentAvailability");
            }
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateSpecialDocumentSlots() throws MetadataException {
        List<String> docIds = m.getExtrinsicObjectIds();
        for (int i = 0; i < docIds.size(); i++) {
            String id = (String) docIds.get(i);
            List<OMElement> slots = m.getSlots(id);
            for (int s = 0; s < slots.size(); s++) {
                OMElement slot = slots.get(s);
                String slotName = slot.getAttributeValue(MetadataSupport.slot_name_qname);
                if (slotName == null) {
                    continue;
                }
                if (slotName.equals("legalAuthenticator")) {
                    // FIXME: Should validate against XCN format.
                } else if (slotName.equals("sourcePatientId")) {
                    validateSourcePatientIdSlot(slot);
                } else if (slotName.equals("sourcePatientInfo")) {
                    validateSourcePatientInfoSlot(slot);
                } else if (slotName.equals("intendedRecipient")) {
                    // FIXME: Should validate against XON/XCN format (multi-valued).
                } else if (slotName.equals("documentAvailability")) {
                    validateDocumentAvailabilitySlot(slot);
                } else if (slotName.equals("URI")) {
                }
            }
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateRegistryPackageClassifications() throws MetadataException {
        List<String> registryPackageIds = m.getRegistryPackageIds();
        for (int i = 0; i < registryPackageIds.size(); i++) {
            String registryPackageId = (String) registryPackageIds.get(i);
            int submissionSetClassificationCount = 0;
            int folderClassificationCount = 0;
            List<OMElement> classifications = m.getClassifications();
            for (int c = 0; c < classifications.size(); c++) {
                OMElement classification = classifications.get(c);
                String classifiedObjectId = classification.getAttributeValue(MetadataSupport.classified_object_qname);
                if (classifiedObjectId == null || !registryPackageId.equals(classifiedObjectId)) {
                    continue;
                }
                String classificationNode = classification.getAttributeValue(MetadataSupport.classificationnode_qname);
                if (classificationNode != null && classificationNode.equals(MetadataSupport.XDSSubmissionSet_classification_uuid)) {
                    // A submission set.
                    submissionSetClassificationCount++;
                } else if (classificationNode != null && classificationNode.equals(MetadataSupport.XDSFolder_classification_uuid)) {
                    // A folder.
                    folderClassificationCount++;
                }
            }
            if ((submissionSetClassificationCount + folderClassificationCount) == 0) {
                err("RegistryPackage" + " " + registryPackageId + " : is not Classified as either a Submission Set or Folder: "
                        + "Submission Set must have classification urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd "
                        + "and Folder must have classification urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2");
            }
            if ((submissionSetClassificationCount + folderClassificationCount) > 1) {
                err("RegistryPackage" + " " + registryPackageId + " : is Classified multiple times: "
                        + "Submission Set must have single classification urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd "
                        + "and Folder must have single classification urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2");
            }
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateDocumentClassifications() throws MetadataException {
        List<String> docIds = m.getExtrinsicObjectIds();
        for (int i = 0; i < docIds.size(); i++) {
            String docId = (String) docIds.get(i);
            List<OMElement> classifications = m.getClassifications(docId);
            //                                               classificationScheme								name							required	multiple
            this.validateClassification(DOCUMENT_TEXT, docId, classifications, MetadataSupport.XDSDocumentEntry_classCode_uuid, "classCode", true, false);
            this.validateClassification(DOCUMENT_TEXT, docId, classifications, MetadataSupport.XDSDocumentEntry_confCode_uuid, "confidentialityCode", true, true);
            this.validateClassification(DOCUMENT_TEXT, docId, classifications, MetadataSupport.XDSDocumentEntry_eventCode_uuid, "eventCodeList", false, true);
            this.validateClassification(DOCUMENT_TEXT, docId, classifications, MetadataSupport.XDSDocumentEntry_formatCode_uuid, "formatCode", true, false);
            this.validateClassification(DOCUMENT_TEXT, docId, classifications, MetadataSupport.XDSDocumentEntry_hcftCode_uuid, "healthCareFacilityTypeCode", true, false);
            this.validateClassification(DOCUMENT_TEXT, docId, classifications, MetadataSupport.XDSDocumentEntry_psCode_uuid, "practiceSettingCode", true, false);
            this.validateClassification(DOCUMENT_TEXT, docId, classifications, MetadataSupport.XDSDocumentEntry_classCode_uuid, "typeCode", true, false);
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateFolderClassifications() throws MetadataException {
        List<String> folderIds = m.getFolderIds();
        for (int i = 0; i < folderIds.size(); i++) {
            String folderId = (String) folderIds.get(i);
            List<OMElement> classifications = m.getClassifications(folderId);
            this.validateClassification(FOLDER_TEXT, folderId, classifications, "urn:uuid:1ba97051-7806-41a8-a48b-8fce7af683c5", "codeList", true, true);
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateSubmissionSetClassifications() throws MetadataException {
        List<String> submissionSetIds = m.getSubmissionSetIds();
        for (int i = 0; i < submissionSetIds.size(); i++) {
            String submissionSetId = submissionSetIds.get(i);
            List<OMElement> classifications = m.getClassifications(submissionSetId);
            this.validateClassification(SUBMISSION_SET_TEXT, submissionSetId, classifications, "urn:uuid:aa543740-bdda-424e-8c96-df4873be8500", "contentTypeCode", true, false);
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateSubmissionSetExternalIdentifiers() throws MetadataException {
        List<String> submissionSetIds = m.getSubmissionSetIds();
        for (int i = 0; i < submissionSetIds.size(); i++) {
            String submissionSetId = submissionSetIds.get(i);
            //ArrayList slots = m.getSlots(id);
            List<OMElement> externalIdentifiers = m.getExternalIdentifiers(submissionSetId);
            //	name, identificationScheme, OID required
            this.validateExternalIdentifier(SUBMISSION_SET_TEXT, submissionSetId, externalIdentifiers, "XDSSubmissionSet.patientId", MetadataSupport.XDSSubmissionSet_patientid_uuid, false);
            this.validateExternalIdentifier(SUBMISSION_SET_TEXT, submissionSetId, externalIdentifiers, "XDSSubmissionSet.sourceId", MetadataSupport.XDSSubmissionSet_sourceid_uuid, true);
            this.validateExternalIdentifier(SUBMISSION_SET_TEXT, submissionSetId, externalIdentifiers, "XDSSubmissionSet.uniqueId", MetadataSupport.XDSSubmissionSet_uniqueid_uuid, true);
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateSubmissionSetSlots() throws MetadataException {
        List<String> submissionSetIds = m.getSubmissionSetIds();
        for (int i = 0; i < submissionSetIds.size(); i++) {
            String submissionSetId = (String) submissionSetIds.get(i);
            List<OMElement> slots = m.getSlots(submissionSetId);
            //ArrayList ext_ids = m.getExternalIdentifiers(id);
            // name						multi	required	number
            validateSlot(SUBMISSION_SET_TEXT, submissionSetId, slots, "submissionTime", false, true, true);

        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateDocumentExternalIdentifiers() throws MetadataException {
        List<String> docIds = m.getExtrinsicObjectIds();
        for (int i = 0; i < docIds.size(); i++) {
            String docId = (String) docIds.get(i);
            //ArrayList slots = m.getSlots(id);
            List<OMElement> externalIdentifiers = m.getExternalIdentifiers(docId);
            // name							identificationScheme                    OID required
            this.validateExternalIdentifier(DOCUMENT_TEXT, docId, externalIdentifiers, "XDSDocumentEntry.patientId", MetadataSupport.XDSDocumentEntry_patientid_uuid, false);
            // the oid^ext format is tested in UniqueId.java?
            this.validateExternalIdentifier(DOCUMENT_TEXT, docId, externalIdentifiers, "XDSDocumentEntry.uniqueId", MetadataSupport.XDSDocumentEntry_uniqueid_uuid, false);
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateDocumentSlots() throws MetadataException {
        List<String> docIds = m.getExtrinsicObjectIds();
        for (int i = 0; i < docIds.size(); i++) {
            String docId = docIds.get(i);
            OMElement doc = m.getObjectById(docId);
            List<OMElement> slots = m.getSlots(docId);
            //ArrayList ext_ids = m.getExternalIdentifiers(id);
            // name, multi, required, number
            validateSlot(DOCUMENT_TEXT, docId, slots, "creationTime", false, true, true);
            validateSlot(DOCUMENT_TEXT, docId, slots, "hash", false, true, false);
            validateSlot(DOCUMENT_TEXT, docId, slots, "intendedRecipient", true, false, false);
            validateSlot(DOCUMENT_TEXT, docId, slots, "languageCode", false, true, false);
            validateSlot(DOCUMENT_TEXT, docId, slots, "legalAuthenticator", false, false, false);
            validateSlot(DOCUMENT_TEXT, docId, slots, "serviceStartTime", false, false, true);
            validateSlot(DOCUMENT_TEXT, docId, slots, "serviceStopTime", false, false, true);
            validateSlot(DOCUMENT_TEXT, docId, slots, "size", false, true, true);
            validateSlot(DOCUMENT_TEXT, docId, slots, "sourcePatientInfo", true, false, false);
            validateSlot(DOCUMENT_TEXT, docId, slots, "documentAvailability", false, false, false);
            validateSlot(DOCUMENT_TEXT, docId, slots, "URI", true, false, false);
            validateSlot(DOCUMENT_TEXT, docId, slots, "repositoryUniqueId", false, true, false);
            if (m.getSlot(docId, "URI") != null) {
                // This is kind of bogus ... validation is done by calling the getter below.
                m.getURIAttribute(doc);
            }
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateFolderExternalIdentifiers() throws MetadataException {
        List<String> folderIds = m.getFolderIds();
        for (int i = 0; i < folderIds.size(); i++) {
            String folderId = folderIds.get(i);
            //ArrayList slots = m.getSlots(id);
            List<OMElement> externalIdentifiers = m.getExternalIdentifiers(folderId);
            //													name							identificationScheme            OID required
            this.validateExternalIdentifier(FOLDER_TEXT, folderId, externalIdentifiers, "XDSFolder.patientId", MetadataSupport.XDSFolder_patientid_uuid, false);
            this.validateExternalIdentifier(FOLDER_TEXT, folderId, externalIdentifiers, "XDSFolder.uniqueId", MetadataSupport.XDSFolder_uniqueid_uuid, true);
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateFolderSlots() throws MetadataException {
        List<String> folderIds = m.getFolderIds();
        for (int i = 0; i < folderIds.size(); i++) {
            String folderId = folderIds.get(i);
            List<OMElement> slots = m.getSlots(folderId);
            boolean hasSubmissionSet = m.getSubmissionSet() != null;
            //  name, multi, required, number
            validateSlot(FOLDER_TEXT, folderId, slots, "lastUpdateTime", false, !hasSubmissionSet, true);
        }
    }

    /**
     * 
     * @param type
     * @param id
     * @param classifications
     * @param classificationScheme
     * @param class_name
     * @param required
     * @param multiple
     */
    private void validateClassification(String type, String id, List<OMElement> classifications, String classificationScheme, String class_name, boolean required, boolean multiple) {
        int count = 0;
        for (int i = 0; i < classifications.size(); i++) {
            OMElement classification = classifications.get(i);
            String scheme = classification.getAttributeValue(MetadataSupport.classificationscheme_qname);
            if (scheme == null || !scheme.equals(classificationScheme)) {
                continue;
            }
            count++;
            OMElement name = MetadataSupport.firstChildWithLocalName(classification, "Name");
            if (name == null) {
                err(type + " " + id + " : Classification of type " + classificationScheme + " ( " + class_name + " ) the name attribute is missing");
            }
            OMElement slot = MetadataSupport.firstChildWithLocalName(classification, "Slot");
            if (slot == null) {
                err(type + " " + id + " : Classification of type " + classificationScheme + " ( " + class_name + " ) the slot 'codingScheme' is missing");
                continue;
            }
            String slotName = slot.getAttributeValue(MetadataSupport.slot_name_qname);
            if (slotName == null || slotName.equals("")) {
                err(type + " " + id + " : Classification of type " + classificationScheme + " ( " + class_name + " ) the slot 'codingScheme' is missing");
            }
        }
        if (count == 0 && required) {
            err(type + " " + id + " : Classification of type " + classificationScheme + " ( " + class_name + " ) is missing");
        }
        if (count > 1 && !multiple) {
            err(type + " " + id + " : Classification of type " + classificationScheme + " ( " + class_name + " ) is duplicated");
        }
    }

    /**
     *
     * @param type
     * @param id
     * @param externalIdentifiers
     * @param name
     * @param idScheme
     * @param isOID
     */
    private void validateExternalIdentifier(String type, String id, List<OMElement> externalIdentifiers, String name, String idScheme, boolean isOID) {
        int count = 0;
        for (int i = 0; i < externalIdentifiers.size(); i++) {
            OMElement externalIdentifier = externalIdentifiers.get(i);
            String identificationScheme = externalIdentifier.getAttributeValue(MetadataSupport.identificationscheme_qname);
            if (identificationScheme == null || !identificationScheme.equals(idScheme)) {
                continue;
            }
            count++;
            String nameValue = m.getNameValue(externalIdentifier);
            if (nameValue == null) {
                err(type + " " + id + " : ExternalIdentifier of type " + idScheme + " (" + name + ") has no internal Name element");
            } else if (!nameValue.equals(name)) {
                err(type + " " + id + " : ExternalIdentifier of type " + idScheme + " (" + name + ") has incorrect internal Name element (" + nameValue + ")");
            }
            int childCount = 0;
            for (Iterator it = externalIdentifier.getChildElements(); it.hasNext();) {
                OMElement child = (OMElement) it.next();
                childCount++;
                String childType = child.getLocalName();
                if (!childType.equals("Name") && !childType.equals("Description") && !childType.equals("VersionInfo")) {
                    err(type + " " + id + " : ExternalIdentifier of type " + idScheme + " (" + name + ") has invalid internal element (" + childType + ")");
                }
            }
            if (isOID) {
                String value = externalIdentifier.getAttributeValue(MetadataSupport.value_qname);
                if (value == null || value.equals("") || !isOID(value)) {
                    err(type + " " + id + " : ExternalIdentifier of type " + idScheme + " (" + name + ") requires an OID format value, " + value + " was found");
                }
            }
        }
        if (count == 0) {
            err(type + " " + id + " : ExternalIdentifier of type " + idScheme + " (" + name + ") is missing");
        }
        if (count > 1) {
            err(type + " " + id + " : ExternalIdentifier of type " + idScheme + " (" + name + ") is duplicated");
        }
    }

    /**
     *
     * @param value
     * @return
     */
    public static boolean isOID(String value) {
        if (value == null) {
            return false;
        }
        return value.matches("\\d(?=\\d*\\.)(?:\\.(?=\\d)|\\d){0,255}");
    }

    /**
     *
     * @param type
     * @param id
     * @param slots
     * @param name
     * @param isMultiValue
     * @param isRequired
     * @param isNumber
     */
    private void validateSlot(String type, String id, List<OMElement> slots, String name, boolean isMultiValue, boolean isRequired, boolean isNumber) {
        boolean found = false;
        for (int i = 0; i < slots.size(); i++) {
            OMElement slot = slots.get(i);
            String slotName = slot.getAttributeValue(MetadataSupport.slot_name_qname);
            if (slotName == null || !slotName.equals(name)) {
                continue;
            }
            if (found) {
                err(type + " " + id + " has multiple slots with name " + name);
            }
            found = true;
            OMElement valueList = MetadataSupport.firstChildWithLocalName(slot, "ValueList");
            int valueCount = 0;
            for (Iterator it = valueList.getChildElements(); it.hasNext();) {
                OMElement value = (OMElement) it.next();
                String valueString = value.getText();
                valueCount++;
                if (isNumber) {
                    try {
                        proveInteger(valueString);
                    } catch (Exception e) {
                        err(type + " " + id + " the value of slot " + name + "(" + valueString + ") is required to be an integer");
                    }
                }
            }
            if ((valueCount > 1 && !isMultiValue)
                    || valueCount == 0) {
                err(type + " " + id + " has slot " + name + " is required to have a single value");
            }
        }
        if (!found && isRequired) {
            err(type + " " + id + " does not have the required slot " + name);
        }
    }

    /**
     *
     * @param value
     * @throws Exception
     */
    private void proveInteger(String value) throws Exception {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '0':
                    continue;
            }
            throw new Exception("Not an integer");
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateSubmissionSetSlotsAreLegal() throws MetadataException {
        String submissionSetId = m.getSubmissionSetId();
        List<OMElement> slots = m.getSlots(submissionSetId);
        for (int j = 0; j < slots.size(); j++) {
            OMElement slot = slots.get(j);
            String slotName = slot.getAttributeValue(MetadataSupport.slot_name_qname);
            if (slotName == null) {
                slotName = "";
            }
            if (!isLegalSubmissionSetSlotName(slotName)) {
                err("Submission Set " + submissionSetId + ": " + slotName + " is not a legal slot name for a Submission Set");
            }
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateFolderSlotsAreLegal() throws MetadataException {
        List<String> folderIds = m.getFolderIds();
        for (int i = 0; i < folderIds.size(); i++) {
            String folderId = folderIds.get(i);
            List<OMElement> slots = m.getSlots(folderId);
            for (int j = 0; j < slots.size(); j++) {
                OMElement slot = slots.get(j);
                String slotName = slot.getAttributeValue(MetadataSupport.slot_name_qname);
                if (slotName == null) {
                    slotName = "";
                }
                if (!isLegalFolderSlotName(slotName)) {
                    err("Folder " + folderId + ": " + slotName + " is not a legal slot name for a Folder");
                }
            }
        }
    }

    /**
     *
     * @throws MetadataException
     */
    private void validateDocumentSlotsAreLegal() throws MetadataException {
        List<String> docIds = m.getExtrinsicObjectIds();
        for (int i = 0; i < docIds.size(); i++) {
            String docId = docIds.get(i);
            List<OMElement> slots = m.getSlots(docId);
            for (int j = 0; j < slots.size(); j++) {
                OMElement slot = slots.get(j);
                String slotName = slot.getAttributeValue(MetadataSupport.slot_name_qname);
                if (slotName == null) {
                    slotName = "";
                }
                if (!isLegalDocumentSlotName(slotName)) {
                    err("Document " + docId + ": " + slotName + " is not a legal slot name for a Document");
                }
            }
        }
    }

    /**
     *
     * @param name
     * @return
     */
    private boolean isLegalSubmissionSetSlotName(String name) {
        if (name == null) {
            return false;
        }
        if (name.startsWith("urn:")) {
            return true;
        }
        return ssSlots.contains(name);
    }

    /**
     *
     * @param name
     * @return
     */
    private boolean isLegalDocumentSlotName(String name) {
        if (name == null) {
            return false;
        }
        if (name.startsWith("urn:")) {
            return true;
        }
        return docSlots.contains(name);
    }

    /**
     *
     * @param name
     * @return
     */
    private boolean isLegalFolderSlotName(String name) {
        if (name == null) {
            return false;
        }
        if (name.startsWith("urn:")) {
            return true;
        }
        return folSlots.contains(name);
    }

    /**
     *
     * @param msg
     */
    private void err(String msg) {
        rel.add_error(MetadataSupport.XDSRegistryMetadataError, msg, this.getClass().getName(), logMessage);
    }
}
