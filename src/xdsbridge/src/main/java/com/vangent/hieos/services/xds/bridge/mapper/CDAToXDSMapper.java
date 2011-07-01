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

package com.vangent.hieos.services.xds.bridge.mapper;

import java.util.Map;
import java.util.UUID;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.services.xds.bridge.message.XDSPnRMessage;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.utils.SubjectIdentifierUtils;
import com.vangent.hieos.services.xds.bridge.utils.UUIDUtils;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.exception.XdsValidationException;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import com.vangent.hieos.xutil.template.TemplateUtil;
import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.XML;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class CDAToXDSMapper implements IXDSMapper {

    /** Field description */
    private static final Logger logger = Logger.getLogger(CDAToXDSMapper.class);

    /** Field description */
    private final ContentParser contentParser;

    /** Field description */
    private final ContentParserConfig contentParserConfig;

    /**
     * Constructs ...
     *
     *
     *
     * @param parser
     * @param config
     */
    public CDAToXDSMapper(ContentParser parser, ContentParserConfig config) {

        super();
        this.contentParser = parser;
        this.contentParserConfig = config;
    }

    /**
     * Method description
     *
     *
     * @param sequence
     *
     * @retur
     *
     * @return
     */
    protected String createExtrinsicObjectId(int sequence) {
        return String.format("Document%02d", sequence);
    }

    /**
     * Method description
     *
     *
     *
     * @param document
     *
     * @return
     *
     *
     * @throws XMLParserException
     * @throws XPathHelperException
     */
    protected Map<String, String> createReplaceVariables(Document document)
            throws XMLParserException, XPathHelperException {

        ContentParser parser = getContentParser();
        ContentParserConfig cfg = getContentParserConfig();

        Map<String, String> result = parser.parse(cfg, document.getContent());

        // ////
        // apply business rules

        // ///
        // ExtrinsicObject

        // authorInstitution
        remapForXON(result, ContentVariableName.AuthorInstitutionRoot,
                    ContentVariableName.AuthorInstitutionExtension);

        // authorPerson
        remapForXON(result, ContentVariableName.AuthorPersonRoot,
                    ContentVariableName.AuthorPersonExtension);

        // entryUUID, symbolic Document01
        String symbolicId = createExtrinsicObjectId(1);

        result.put(ContentVariableName.EntryUUID.toString(), symbolicId);
        document.setSymbolicId(symbolicId);

        // formatCode
        CodedValue format = document.getFormat();

        result.put(ContentVariableName.DocumentFormatCode.toString(),
                   format.getCode());
        result.put(ContentVariableName.DocumentFormatCodeSystem.toString(),
                   format.getCodeSystem());
        result.put(ContentVariableName.DocumentFormatDisplayName.toString(),
                   format.getDisplayName());

        // mime type
        result.put(ContentVariableName.DocumentMimeType.toString(),
                   document.getMimeType());

        // legal Authenticator
        remapForXON(result, ContentVariableName.LegalAuthenticatorRoot,
                    ContentVariableName.LegalAuthenticatorExtension);

        // patientId, create an identifier and store in document
        String patIdExt =
            result.get(ContentVariableName.PatientIdExtension.toString());

        String patIdRoot =
            result.get(ContentVariableName.PatientIdRoot.toString());

        SubjectIdentifier patId =
            SubjectIdentifierUtils.createSubjectIdentifier(patIdRoot, patIdExt);

        document.setPatientId(patId);

        // patientId
        remapForCX(result, ContentVariableName.PatientIdRoot,
                   ContentVariableName.PatientIdExtension,
                   ContentVariableName.PatientIdCX);

        // sourcePatientId
        remapForCX(result, ContentVariableName.SourcePatientIdRoot,
                   ContentVariableName.SourcePatientIdExtension,
                   ContentVariableName.SourcePatientIdCX);

        // title
        String title = result.get(ContentVariableName.DocumentTitle.toString());

        if (StringUtils.isBlank(title)) {

            // default to displayName
            result.put(
                ContentVariableName.DocumentTitle.toString(),
                result.get(ContentVariableName.DocumentDisplayName.toString()));
        }

        // uniqueId
        String uuidField = ContentVariableName.DocumentUniqueId.toString();

        if (StringUtils.isNotBlank(document.getId())) {

            // document id from SDR is preferred
            String repoDocId = document.getId();

            if (UUIDUtils.isUUID(repoDocId)) {

                // if UUID then we need to convert to OID
                repoDocId = UUIDUtils.toOIDFromUUIDString(repoDocId);
            }

            result.put(uuidField, repoDocId);
            document.setRepositoryId(repoDocId);

        } else {

            // if document content had internal id use that
            // else generate one

            String docId = result.get(uuidField);

            if (StringUtils.isBlank(docId)) {

                // TODO uses 2.25 prefix
                docId = UUIDUtils.toOID(UUID.randomUUID());

                result.put(uuidField, docId);

                // sync up with the document object
                document.setId(docId);
                document.setRepositoryId(docId);

            } else {

                String repoDocId = docId;

                // if inner document id is a UUID then we need to convert
                if (UUIDUtils.isUUID(docId)) {

                    // TODO uses 2.25 prefix
                    repoDocId = UUIDUtils.toOIDFromUUIDString(docId);
                    result.put(uuidField, repoDocId);
                }

                // sync up with the document object
                document.setRepositoryId(repoDocId);
            }
        }

        // ///
        // Submission Set

        String sourceIdRoot =
            result.get(ContentVariableName.SourceIdRoot.toString());
        String sourceIdExt =
            result.get(ContentVariableName.SourceIdExtension.toString());

        if (StringUtils.isBlank(sourceIdExt)) {

            result.put(ContentVariableName.SourceId.toString(), sourceIdRoot);

        } else {

            result.put(ContentVariableName.SourceId.toString(),
                       String.format("%s^%s", sourceIdRoot, sourceIdExt));
        }

        result.put(ContentVariableName.SubmissionTime.toString(),
                   Hl7Date.now());

        // TODO uses 2.25 prefix
        result.put(ContentVariableName.SubmissionSetUniqueId.toString(),
                   UUIDUtils.toOID(UUID.randomUUID()));

        // ///
        // Static Values
        // set all the static values (or overrides)
        result.putAll(cfg.getStaticValues());

        return result;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected ContentParser getContentParser() {
        return contentParser;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected ContentParserConfig getContentParserConfig() {
        return contentParserConfig;
    }

    /**
     * Method description
     *
     *
     * @param patientId
     * @param document
     *
     * @return
     *
     *
     * @throws Exception
     */
    @Override
    public XDSPnRMessage map(SubjectIdentifier patientId, Document document)
            throws Exception {

        Map<String, String> repl = createReplaceVariables(document);

        validate(patientId, document, repl);

        for (Map.Entry<String, String> entry : repl.entrySet()) {

            // sanitize values (& becomes &amp;)
            String value = entry.getValue();

            if (StringUtils.isNotBlank(value)) {
                entry.setValue(XML.escape(value));
            }
        }

        String template =
            this.contentParserConfig.retrieveTemplateFileAsString();

        OMElement elem = TemplateUtil.getOMElementFromTemplate(template, repl);

        XDSPnRMessage result = new XDSPnRMessage(elem);

        result.attachDocument(document);

        return result;
    }

    /**
     * Method description
     *
     *
     *
     * @param contentVariables
     * @param rootField
     * @param extensionField
     * @param cxField
     */
    private void remapForCX(Map<String, String> contentVariables,
                            ContentVariableName rootField,
                            ContentVariableName extensionField,
                            ContentVariableName cxField) {

        String root = contentVariables.get(rootField.toString());
        String ext = contentVariables.get(extensionField.toString());

        SubjectIdentifier subjectIdentifier =
            SubjectIdentifierUtils.createSubjectIdentifier(root, ext);

        contentVariables.put(cxField.toString(),
                             subjectIdentifier.getCXFormatted());
    }

    /**
     * Method description
     *
     *
     * @param result
     * @param rootField
     * @param extensionField
     */
    private void remapForXON(Map<String, String> result,
                             ContentVariableName rootField,
                             ContentVariableName extensionField) {

        String root = result.get(rootField.toString());

        root = StringUtils.trimToEmpty(root);

        String ext = result.get(extensionField.toString());

        ext = StringUtils.trimToEmpty(ext);

        if (StringUtils.isBlank(ext)) {

            // "Case 1 - root only"
            // extension becomes root, root becomes blank
            result.put(extensionField.toString(), root);
            result.put(rootField.toString(), "");

        } else {

            // "Case 2 - root and extension"
            // put root into proper format
            String isoroot = String.format("&%s&ISO", root);

            result.put(rootField.toString(), isoroot);
        }
    }

    /**
     * Method description
     *
     *
     * @param patientId
     * @param document
     * @param repl
     *
     * @throws XdsValidationException
     */
    private void validate(SubjectIdentifier patientId, Document document,
                          Map<String, String> repl)
            throws XdsValidationException {

        StringBuilder sb = new StringBuilder();

        // check patient identifier

        SubjectIdentifier docPatientId = document.getPatientId();

        // TODO the equals in subjectIdentifier is non-standard
        if (patientId.equals(docPatientId) == false) {

            sb.append(
                String.format(
                    "SubmitDocumentRequest patient id [%s] does not match document patient id [%s].",
                    patientId.getCXFormatted(), docPatientId.getCXFormatted()));
        }

        if (sb.length() > 0) {
            throw new XdsValidationException(sb.toString());
        }
    }
}
