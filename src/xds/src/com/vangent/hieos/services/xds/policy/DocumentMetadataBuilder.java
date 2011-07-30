/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xds.policy;

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.hl7.formatutil.HL7FormatUtil;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

/**
 *
 * @author Bernie Thuman
 */
public class DocumentMetadataBuilder {

    public static final String EBXML_RIM_NS = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";
    public static final String EBXML_RIM_NS_PREFIX = "rim";
    public static final String EBXML_QUERY_NS = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0";
    public static final String EBXML_QUERY_NS_PREFIX = "query";
    public static final String XDS_EXTERNAL_ID_PATIENT_ID_SCHEME = "urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427";
    public static final String XDS_EXTERNAL_ID_DOCUMENT_ID_SCHEME = "urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab";
    public static final String XDS_CONFIDENTIALITY_CODE_CLASS_SCHEME = "urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f";
    public static final String XDS_CLASS_CODE_CLASS_SCHEME = "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a";
    public static final String XDS_TYPE_CODE_CLASS_SCHEME = "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983";
    public static final String XDS_FORMAT_CODE_CLASS_SCHEME = "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d";
    public static final String XDS_AUTHOR_CLASS_SCHEME = "urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d";

    /**
     *
     * @param registryObjects
     * @return
     */
    public List<DocumentMetadata> buildDocumentMetadataList(RegistryObjectElementList registryObjectElementList) {
        List<DocumentMetadata> documentMetadataList = new ArrayList<DocumentMetadata>();
        // Go through each ExtrinsicObject (document).
        List<OMElement> registryObjects = registryObjectElementList.getElementList();
        for (OMElement registryObject : registryObjects) {
            DocumentMetadata documentMetadata = this.buildDocumentMetadata(registryObject);
            documentMetadataList.add(documentMetadata);
        }
        return documentMetadataList;
    }

    /**
     * 
     * @param documentMetadata
     * @return
     */
    public DocumentMetadataElement buildDocumentMetadataElement(DocumentMetadata documentMetadata) {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();

        // DocumentMetadata
        OMElement documentMetadataNode = omfactory.createOMElement(new QName(PolicyConstants.HIEOS_PIP_NS, "DocumentMetadata", PolicyConstants.HIEOS_PIP_NS_PREFIX));

        // Patient id:
        String patientId = documentMetadata.getPatientId().getCXFormatted();
        OMElement patientIdNode = omfactory.createOMElement(new QName(PolicyConstants.HIEOS_PIP_NS, "PatientId", PolicyConstants.HIEOS_PIP_NS_PREFIX));
        patientIdNode.setText(patientId);
        documentMetadataNode.addChild(patientIdNode);

        // Type code:
        CodedValue typeCode = documentMetadata.getTypeCode();
        OMElement typeCodeNode = this.buildCodedValueNode(typeCode, "Type");
        documentMetadataNode.addChild(typeCodeNode);

        // Confidentiality codes (can be multiple):
        List<CodedValue> confidentialityCode = documentMetadata.getConfidentialityCodes();
        OMElement confidentialityCodedValuesNode = this.buildCodedValuesNode(confidentialityCode, "ConfidentialityCodes");
        documentMetadataNode.addChild(confidentialityCodedValuesNode);

        // Author(s).
        List<DocumentAuthorMetadata> documentAuthorMetadataList = documentMetadata.getDocumentAuthorMetadataList();
        OMElement authorsNode = this.buildAuthorsNode(documentAuthorMetadataList);
        documentMetadataNode.addChild(authorsNode);

        // TBD: Other ... we have more (e.g. repositoryid, documentid).

        // Return the OMElement (wrapped).
        return new DocumentMetadataElement(documentMetadataNode);
    }

   

    //<pip:Role codeSystem="String" displayName="String" codeSystemName="String" code="String"/>
    // FIXME: Move this logic to hl7v3util?
    /**
     *
     * @param codedValue
     * @param name
     * @return
     */
    private OMElement buildCodedValuesNode(List<CodedValue> codedValues, String name) {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMElement codeValuesNode = omfactory.createOMElement(new QName(PolicyConstants.HIEOS_PIP_NS, name, PolicyConstants.HIEOS_PIP_NS_PREFIX));
        for (CodedValue codedValue : codedValues) {
            OMElement codedValueNode = this.buildCodedValueNode(codedValue, "Code");
            codeValuesNode.addChild(codedValueNode);
        }
        return codeValuesNode;
    }

    /**
     * 
     * @param codedValue
     * @param name
     * @return
     */
    private OMElement buildCodedValueNode(CodedValue codedValue, String name) {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMElement codeNode = omfactory.createOMElement(new QName(PolicyConstants.HIEOS_PIP_NS, name, PolicyConstants.HIEOS_PIP_NS_PREFIX));
        codeNode.addAttribute("code", codedValue.getCode(), null);
        codeNode.addAttribute("displayName", codedValue.getDisplayName(), null);
        codeNode.addAttribute("codeSystem", codedValue.getCodeSystem(), null);
        if (codedValue.getCodeSystemName() != null) {
            //  May not be there.
            codeNode.addAttribute("codeSystemName", codedValue.getCodeSystemName(), null);
        }
        return codeNode;
    }

    /**
     *
     * @param documentAuthorMetadataList
     * @return
     */
    private OMElement buildAuthorsNode(List<DocumentAuthorMetadata> documentAuthorMetadataList) {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMElement authorsNode = omfactory.createOMElement(new QName(PolicyConstants.HIEOS_PIP_NS, "Authors", PolicyConstants.HIEOS_PIP_NS_PREFIX));
        for (DocumentAuthorMetadata documentAuthorMetadata : documentAuthorMetadataList) {
            OMElement authorNode = this.buildAuthorNode(documentAuthorMetadata);
            authorsNode.addChild(authorNode);
        }
        return authorsNode;
    }

    /**
     * 
     * @param documentAuthorMetadata
     * @return
     */
    private OMElement buildAuthorNode(DocumentAuthorMetadata documentAuthorMetadata) {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMElement authorNode = omfactory.createOMElement(new QName(PolicyConstants.HIEOS_PIP_NS, "Author", PolicyConstants.HIEOS_PIP_NS_PREFIX));
        OMElement personNode = omfactory.createOMElement(new QName(PolicyConstants.HIEOS_PIP_NS, "Person", PolicyConstants.HIEOS_PIP_NS_PREFIX));
        authorNode.addChild(personNode);
        personNode.setText(documentAuthorMetadata.getAuthorPerson());

        // Now add list of 0..n organizations.
        OMElement organizationsNode = omfactory.createOMElement(new QName(PolicyConstants.HIEOS_PIP_NS, "Organizations", PolicyConstants.HIEOS_PIP_NS_PREFIX));
        authorNode.addChild(organizationsNode);
        for (String organization : documentAuthorMetadata.getAuthorOrganizations()) {
            OMElement organizationNode = omfactory.createOMElement(new QName(PolicyConstants.HIEOS_PIP_NS, "Organization", PolicyConstants.HIEOS_PIP_NS_PREFIX));
            organizationsNode.addChild(organizationNode);
            organizationNode.setText(organization);
        }
        return authorNode;
    }

    /**
     * 
     * @param extrinsicObjects
     * @return
     */
    public List<DocumentMetadata> buildDocumentIdentifiersList(RegistryObjectElementList extrinsicObjectsElementList) {
        List<DocumentMetadata> documentMetadataList = new ArrayList<DocumentMetadata>();
        List<OMElement> extrinsicObjects = extrinsicObjectsElementList.getElementList();
        for (OMElement extrinsicObject : extrinsicObjects) {
            DocumentMetadata documentMetadata = this.buildDocumentIdentifiers(extrinsicObject);
            documentMetadataList.add(documentMetadata);
        }
        return documentMetadataList;
    }

    /**
     *
     * @param extrinsicObject
     * @return
     */
    public DocumentMetadata buildDocumentIdentifiers(OMElement extrinsicObject) {

        // Create the DocumentMetadata instance.
        DocumentMetadata documentMetadata = new DocumentMetadata();
        documentMetadata.setRegistryObject(extrinsicObject);
        documentMetadata.setIsExtrinsicObject(true);

        // Build identifiers.
        this.buildIdentifiers(documentMetadata, extrinsicObject);

        return documentMetadata;
    }

    /**
     *
     * @param registryObject
     * @return
     */
    public DocumentMetadata buildDocumentMetadata(OMElement registryObject) {

        // Create the DocumentMetadata instance.
        DocumentMetadata documentMetadata = new DocumentMetadata();
        documentMetadata.setRegistryObject(registryObject);

        // If this is not an ExtrinsicObject ... get out immediately
        if (!registryObject.getLocalName().equalsIgnoreCase("ExtrinsicObject")) {
            documentMetadata.setIsExtrinsicObject(false);
            return documentMetadata;  // Now, get out.
        }
        documentMetadata.setIsExtrinsicObject(true);

        // Build identifiers.
        this.buildIdentifiers(documentMetadata, registryObject);

        // Get list of Classifications (to use later).
        List<OMElement> classifications = this.getClassifications(registryObject);

        // Format code.
        CodedValue formatCode = this.getFormatCode(classifications);
        documentMetadata.setFormatCode(formatCode);

        // Type code.
        CodedValue typeCode = this.getTypeCode(classifications);
        documentMetadata.setTypeCode(typeCode);

        // Class code.
        CodedValue classCode = this.getClassCode(classifications);
        documentMetadata.setClassCode(classCode);

        // Confidentiality code.
        List<CodedValue> confidentialityCodes = this.getConfidentialityCode(classifications);
        documentMetadata.setConfidentialityCodes(confidentialityCodes);

        // Author(s).
        List<DocumentAuthorMetadata> documentAuthorMetadataList = this.getAuthors(classifications);
        documentMetadata.setDocumentAuthorMetadataList(documentAuthorMetadataList);

        // TBD (Other) ...
        return documentMetadata;
    }

    /**
     *
     * @param documentMetadata
     * @param extrinsicObject
     */
    private void buildIdentifiers(DocumentMetadata documentMetadata, OMElement extrinsicObject)
    {
        // Get list of ExternalIdentifiers (to use below).
        List<OMElement> externalIdentifiers = this.getExternalIdentifiers(extrinsicObject);

        // Document id.
        String documentId = this.getDocumentId(externalIdentifiers);
        documentMetadata.setDocumentId(documentId);

        // Patient id.
        String patientIdCXFormatted = this.getPatientId(externalIdentifiers);
        SubjectIdentifier patientId = new SubjectIdentifier(patientIdCXFormatted);
        documentMetadata.setPatientId(patientId);

        // Repository id.
        String repositoryId = this.getRepositoryId(extrinsicObject);
        documentMetadata.setRepositoryId(repositoryId);
    }

    /**
     *
     * @param extrinsicObject
     * @return
     */
    public String getRepositoryId(OMElement extrinsicObject) {
        return this.getSlotSingleValue(extrinsicObject, "repositoryUniqueId");
    }

    /**
     *
     * @param extrinsicObject
     * @return
     */
    public List<OMElement> getExternalIdentifiers(OMElement extrinsicObject) {
        return this.getNodes(extrinsicObject, "./ns:ExternalIdentifier");
    }

    /**
     *
     * @param extrinsicObject
     * @return
     */
    public List<OMElement> getClassifications(OMElement extrinsicObject) {
        return this.getNodes(extrinsicObject, "./ns:Classification");
    }

    /**
     *
     * @param externalIdentifiers
     * @return
     */
    public String getPatientId(List<OMElement> externalIdentifiers) {
        return this.getExternalIdentifierValue(externalIdentifiers, XDS_EXTERNAL_ID_PATIENT_ID_SCHEME);
    }

    /**
     *
     * @param externalIdentifiers
     * @return
     */
    public String getDocumentId(List<OMElement> externalIdentifiers) {
        return this.getExternalIdentifierValue(externalIdentifiers, XDS_EXTERNAL_ID_DOCUMENT_ID_SCHEME);
    }

    /**
     *
     * @param externalIdentifiers
     * @param identificationScheme
     * @return
     */
    public String getExternalIdentifierValue(List<OMElement> externalIdentifiers, String identificationScheme) {
        for (OMElement externalIdentifier : externalIdentifiers) {
            String externalIdentifierIdScheme = externalIdentifier.getAttributeValue(new QName("identificationScheme"));
            if (externalIdentifierIdScheme.equalsIgnoreCase(identificationScheme)) {
                return externalIdentifier.getAttributeValue(new QName("value"));
            }
        }
        return null;  // Not found.
    }

    /**
     *
     * @param classifications
     * @return
     */
    public CodedValue getTypeCode(List<OMElement> classifications) {
        return this.getClassificationCodedValue(classifications, XDS_TYPE_CODE_CLASS_SCHEME);
    }

    /**
     *
     * @param classifications
     * @return
     */
    public CodedValue getFormatCode(List<OMElement> classifications) {
        return this.getClassificationCodedValue(classifications, XDS_FORMAT_CODE_CLASS_SCHEME);
    }

    /**
     *
     * @param classifications
     * @return
     */
    public CodedValue getClassCode(List<OMElement> classifications) {
        return this.getClassificationCodedValue(classifications, XDS_CLASS_CODE_CLASS_SCHEME);
    }

    /**
     *
     * @param classifications
     * @return
     */
    public List<CodedValue> getConfidentialityCode(List<OMElement> classifications) {
        return this.getClassificationCodedValues(classifications, XDS_CONFIDENTIALITY_CODE_CLASS_SCHEME);
    }

    /**
     *
     * @param classifications
     * @param classificationScheme
     * @return
     */
    public CodedValue getClassificationCodedValue(List<OMElement> classifications, String classificationScheme) {
        List<OMElement> matchedClassifications = this.getMatchedClassifications(classifications, classificationScheme);
        if (!matchedClassifications.isEmpty()) {
            // Should not have had > 1 (get the first one).
            OMElement classification = matchedClassifications.get(0);
            return this.getClassificationCodedValue(classification);
        }
        return null;  // Not found.
    }

    /**
     *
     * @param classifications
     * @param classificationScheme
     * @return
     */
    public List<CodedValue> getClassificationCodedValues(List<OMElement> classifications, String classificationScheme) {
        List<OMElement> matchedClassifications = this.getMatchedClassifications(classifications, classificationScheme);
        List<CodedValue> codedValues = new ArrayList<CodedValue>();
        for (OMElement classification : matchedClassifications) {
            CodedValue codedValue = this.getClassificationCodedValue(classification);
            codedValues.add(codedValue);
        }
        return codedValues;
    }

    /**
     *
     * @param classifications
     * @param classificationScheme
     * @return
     */
    public List<OMElement> getMatchedClassifications(List<OMElement> classifications, String classificationScheme) {
        List<OMElement> matchedClassifications = new ArrayList<OMElement>();
        for (OMElement classification : classifications) {
            String classScheme = classification.getAttributeValue(new QName("classificationScheme"));
            if (classScheme.equalsIgnoreCase(classificationScheme)) {
                matchedClassifications.add(classification);
            }
        }
        return matchedClassifications;
    }

// <ns1:Classification classificationScheme="urn:uuid:f0306f51-975f-434e-a61c-c59651d33983" classifiedObject="urn:uuid:d35324bb-9032-4a30-b2fc-647547086086" nodeRepresentation="34108-1" lid="urn:uuid:2250bfda-ede8-45da-a6ef-1da41164698a" objectType="urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification" status="urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted" id="urn:uuid:2250bfda-ede8-45da-a6ef-1da41164698a" home="urn:oid:1.3.6.1.4.1.21367.13.3070">
//   <ns1:Slot name="codingScheme">
//     <ns1:ValueList>
//       <ns1:Value>LOINC</ns1:Value>
//     </ns1:ValueList>
//   </ns1:Slot>
//   <ns1:Name>
//     <ns1:LocalizedString charset="UTF-8" value="Outpatient Evaluation And Management" xml:lang="en-us" />
//   </ns1:Name>
//   <ns1:Description />
//   <ns1:VersionInfo versionName="1.1" />
// </ns1:Classification>
    /**
     *
     * @param classification
     * @return
     */
    public CodedValue getClassificationCodedValue(OMElement classification) {
        CodedValue codedValue = new CodedValue();

        // Code.
        String code = classification.getAttributeValue(new QName("nodeRepresentation"));
        codedValue.setCode(code);

        // Code system.
        String codeSystem = this.getSlotSingleValue(classification, "codingScheme");
        codedValue.setCodeSystem(codeSystem);

        // Code display name.
        String displayName = "UNKNOWN";
        OMElement displayNameNode;
        try {
            displayNameNode = XPathHelper.selectSingleNode(classification, "./ns:Name/ns:LocalizedString[1]", EBXML_RIM_NS);
            if (displayNameNode != null) {
                displayName = displayNameNode.getAttributeValue(new QName("value"));
            }
        } catch (XPathHelperException ex) {
            // FIXME: Do something.
        }

        codedValue.setDisplayName(displayName);

        // Code system name.
        // TBD: Not sure if available in meta-data.
        String codeSystemName = "UNKNOWN";
        codedValue.setCodeSystemName(codeSystemName);
        return codedValue;
    }

    /**
     *
     * @param rootNode
     * @param slotName
     * @return
     */
    public String getSlotSingleValue(OMElement rootNode, String slotName) {
        String xpathExpression = "./ns:Slot[@name='" + slotName + "']/ns:ValueList/ns:Value[1]";
        String value = "UNKNOWN";
        OMElement valueNode;
        try {
            valueNode = XPathHelper.selectSingleNode(rootNode, xpathExpression, EBXML_RIM_NS);
            if (valueNode != null) {
                value = valueNode.getText();
            }
        } catch (XPathHelperException ex) {
            // FIXME: Do something.
        }
        return value;
    }

    /**
     *
     * @param rootNode
     * @param slotName
     * @return
     */
    public List<String> getSlotMultiValue(OMElement rootNode, String slotName) {
        String xpathExpression = "./ns:Slot[@name='" + slotName + "']/ns:ValueList/ns:Value";
        List<String> values = new ArrayList<String>();
        List<OMElement> valueNodes;
        try {
            valueNodes = XPathHelper.selectNodes(rootNode, xpathExpression, EBXML_RIM_NS);
            for (OMElement valueNode : valueNodes) {
                String value = valueNode.getText();
                values.add(value);
            }
        } catch (XPathHelperException ex) {
            // FIXME: Do something.
        }
        return values;
    }

    /**
     * 
     * @param classifications
     * @return
     */
    public List<DocumentAuthorMetadata> getAuthors(List<OMElement> classifications) {
        List<DocumentAuthorMetadata> documentAuthorMetadataList = new ArrayList<DocumentAuthorMetadata>();
        List<OMElement> authorClassifications = this.getMatchedClassifications(classifications, XDS_AUTHOR_CLASS_SCHEME);
        for (OMElement authorClassification : authorClassifications) {
            DocumentAuthorMetadata documentAuthorMetadata = this.buildDocumentAuthorMetadata(authorClassification);
            documentAuthorMetadataList.add(documentAuthorMetadata);
        }
        return documentAuthorMetadataList;
    }

    /**
     *
     * @param authorClassification
     * @return
     */
    public DocumentAuthorMetadata buildDocumentAuthorMetadata(OMElement authorClassification) {

        // Create the DocumentMetadata instance.
        DocumentAuthorMetadata documentAuthorMetadata = new DocumentAuthorMetadata();

        // Author person (should only be one value and it is required).
        String authorPerson = this.getSlotSingleValue(authorClassification, "authorPerson");

        // Normalize author name XCN identifiers.
        String xcnIdentifier = HL7FormatUtil.getXCN_Identifier(authorPerson);
        documentAuthorMetadata.setAuthorPerson(xcnIdentifier);

        // Author organization (can be 0..n).
        List<String> authorOrganizations = this.getSlotMultiValue(authorClassification, "authorInstitution");

        // Normalize author institution XON identifiers.
        int i = 0;
        for (String authorOrganization : authorOrganizations) {
            String xonIdentifier = HL7FormatUtil.getXON_Identifier(authorOrganization);
            authorOrganizations.set(i++, xonIdentifier);
        }
        documentAuthorMetadata.setAuthorOrganizations(authorOrganizations);

        return documentAuthorMetadata;
    }

    /**
     *
     * @param rootNode
     * @param xpath
     * @return
     */
    private List<OMElement> getNodes(OMElement rootNode, String xpath) {
        List<OMElement> nodes = new ArrayList<OMElement>();
        try {
            nodes = XPathHelper.selectNodes(rootNode, xpath, EBXML_RIM_NS);
        } catch (XPathHelperException ex) {
            // FIXME: Do something.
        }
        return nodes;
    }
}
