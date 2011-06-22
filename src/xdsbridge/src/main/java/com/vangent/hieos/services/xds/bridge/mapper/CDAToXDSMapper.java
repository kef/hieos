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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.Identifier;
import com.vangent.hieos.services.xds.bridge.model.XDSPnR;
import com.vangent.hieos.services.xds.bridge.utils.StringUtils;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import com.vangent.hieos.xutil.iosupport.Io;
import com.vangent.hieos.xutil.template.TemplateUtil;
import org.apache.axiom.om.OMElement;
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
    private final static String PNRFILE =
        "META-INF/templates/ProvideAndRegisterMetadata.xml";

    /** Field description */
    private final static Logger logger = Logger.getLogger(CDAToXDSMapper.class);

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
     * @throws Exception
     */
    protected Map<String, String> createReplaceVariables(Document document)
            throws Exception {

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
        // TODO map needs to be passed in

        // legal Authenticator
        remapForXON(result, ContentVariableName.LegalAuthenticatorRoot,
                    ContentVariableName.LegalAuthenticatorExtension);

        // patientId
        remapForCX(result, ContentVariableName.PatientRoot,
                   ContentVariableName.PatientExtension);

        // sourcePatientId
        remapForCX(result, ContentVariableName.SourcePatientRoot,
                   ContentVariableName.SourcePatientExtension);

        // title
        String title = result.get(ContentVariableName.DocumentTitle.toString());

        if (StringUtils.isBlank(title)) {

            // default to displayName
            result.put(
                ContentVariableName.DocumentTitle.toString(),
                result.get(ContentVariableName.DocumentDisplayName.toString()));
        }

        // uniqueId
        if (StringUtils.isNotBlank(document.getId())) {

            // document id from SDR is preferred
            result.put(ContentVariableName.DocumentUniqueId.toString(),
                       document.getId());

        } else {

            // if document content had internal id use that
            // else generate one
            String uuidField = ContentVariableName.DocumentUniqueId.toString();
            String uid = result.get(uuidField);

            if (StringUtils.isBlank(uid)) {

                uid = UUID.randomUUID().toString();
                result.put(uuidField, uid);
            }

            // sync up with the document object
            document.setId(uid);
        }

        // ///
        // Submission Set

        String sourceIdRoot =
            result.get(ContentVariableName.SourcePatientRoot.toString());
        String sourceIdExt =
            result.get(ContentVariableName.SourcePatientExtension.toString());

        if (StringUtils.isBlank(sourceIdExt)) {
            result.put(ContentVariableName.SourceId.toString(), sourceIdRoot);
        } else {

            result.put(ContentVariableName.SourceId.toString(),
                       String.format("%s^%s", sourceIdRoot, sourceIdExt));
        }

        result.put(ContentVariableName.SubmissionTime.toString(),
                   Hl7Date.now());

        result.put(ContentVariableName.SubmissionSetUniqueId.toString(), 
                UUID.randomUUID().toString());
        
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
     * @throws Exception
     */
    @Override
    public XDSPnR map(Identifier patientId, Document document)
            throws Exception {

        // TODO revisit where the template could/should be stored
        String template = readPNRTemplate();

        Map<String, String> repl = createReplaceVariables(document);

        validate(patientId, document, repl);

        for (Map.Entry<String, String> entry : repl.entrySet()) {

            // sanitize values (& becomes &amp;)
            entry.setValue(XML.escape(entry.getValue()));
        }

        OMElement elem = TemplateUtil.getOMElementFromTemplate(template, repl);

        XDSPnR result = new XDSPnR(elem);

        result.attachDocument(document);

        return result;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    protected String readPNRTemplate() throws IOException {

        String result = null;

        ClassLoader cl = getClass().getClassLoader();
        InputStream is = null;

        try {

            is = cl.getResourceAsStream(PNRFILE);
            result = Io.getStringFromInputStream(is);

        } catch (IOException e) {

            logger.error(String.format("Unable to read %s.", PNRFILE), e);

            throw e;

        } finally {

            if (is != null) {

                try {
                    is.close();
                } catch (IOException e) {

                    logger.warn(String.format("Unable to close %s.", PNRFILE),
                                e);
                }
            }
        }

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
     */
    private void remapForCX(Map<String, String> contentVariables,
                            ContentVariableName rootField,
                            ContentVariableName extensionField) {

        String root = contentVariables.get(rootField.toString());

        root = StringUtils.trimToEmpty(root);

        String ext = contentVariables.get(extensionField.toString());

        ext = StringUtils.trimToEmpty(ext);

        if (StringUtils.isBlank(ext)) {

            // "Case 1 - root only"
            // extension is everything before last period
            ext = StringUtils.substringAfterLast(root, ".");
            root = StringUtils.substringBeforeLast(root, ".");
        }

        String isoroot = String.format("&%s&ISO", root);

        contentVariables.put(rootField.toString(), isoroot);
        contentVariables.put(extensionField.toString(), ext);
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
     */
    private void validate(Identifier patientId, Document document,
                          Map<String, String> repl) {

        // TODO add validation for patientId
        // TODO add validation for required fields
    }
}
