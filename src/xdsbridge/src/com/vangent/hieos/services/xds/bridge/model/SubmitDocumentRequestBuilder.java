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

package com.vangent.hieos.services.xds.bridge.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.services.xds.bridge.utils.StringUtils;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.exception.XdsIOException;
import com.vangent.hieos.xutil.soap.Mtom;
import com.vangent.hieos.xutil.xml.XPathHelper;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class SubmitDocumentRequestBuilder {

    /** Field description */
    private final static Logger logger =
        Logger.getLogger(SubmitDocumentRequestBuilder.class);

    /**
     * Constructs ...
     *
     */
    public SubmitDocumentRequestBuilder() {
        super();
    }

    /**
     * Method description
     *
     *
     * @param elem
     *
     * @return
     *
     * @throws ModelBuilderException
     */
    public SubmitDocumentRequest buildSubmitDocumentRequest(OMElement elem)
            throws ModelBuilderException {

        SubmitDocumentRequest result = unmarshal(elem);

        validate(result);

        return result;
    }

    /**
     * Method description
     *
     *
     *
     * @param ns
     * @param elem
     * @param expr
     *
     * @return
     *
     * @throws IOException
     * @throws XPathHelperException
     * @throws XdsIOException
     */
    private byte[] parseBinaryContent(OMElement elem, String expr, String ns)
            throws XdsIOException, IOException, XPathHelperException {

        byte[] result = null;

        OMElement binelem = XPathHelper.selectSingleNode(elem, expr, ns);

        if (binelem != null) {

            Mtom mtom = new Mtom();

            mtom.decode(binelem);

            result = mtom.getContents();

            if ((result != null) && (result.length < 1)) {
                result = null;
            }
        }

        return result;
    }

    /**
     * Method description
     *
     *
     *
     *
     * @param ns
     * @param elem
     * @param expr
     *
     * @return
     *
     * @throws XPathHelperException
     */
    private CodedValue parseCodedValue(OMElement elem, String expr, String ns)
            throws XPathHelperException {

        CodedValue result = null;
        OMElement cvelem = XPathHelper.selectSingleNode(elem, expr, ns);

        if (cvelem != null) {

            result = new CodedValue();
            result.setCode(parseText(cvelem, "@code", ns));
            result.setCodeSystem(parseText(cvelem, "@codeSystem", ns));
            result.setCodeSystemName(parseText(cvelem, "@codeSystemName", ns));
            result.setDisplayName(parseText(cvelem, "@displayName", ns));
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param docelem
     * @param ns
     *
     * @return
     *
     *
     * @throws IOException
     * @throws XPathHelperException
     * @throws XdsIOException
     */
    private Document parseDocument(OMElement docelem, String ns)
            throws XPathHelperException, XdsIOException, IOException {

        Document result = new Document();

        result.setId(parseText(docelem, "./ns:Id", ns));
        result.setReplaceId(parseText(docelem, "./ns:ReplaceId", ns));
        result.setType(parseCodedValue(docelem, "./ns:Type", ns));
        result.setContent(parseBinaryContent(docelem, "./ns:Content", ns));

        // TODO revisit how this gets populated
        result.setMimeType("text/xml");

        return result;
    }

    /**
     * Method description
     *
     *
     * @param elem
     * @param expr
     * @param ns
     *
     * @return
     *
     * @throws XPathHelperException
     */
    private Identifier parseIdentifier(OMElement elem, String expr, String ns)
            throws XPathHelperException {

        Identifier result = null;
        OMElement idelem = XPathHelper.selectSingleNode(elem, expr, ns);

        if (idelem != null) {

            result = new Identifier();
            result.setAssigningAuthorityName(parseText(idelem,
                    "@assigningAuthorityName", ns));
            result.setRoot(parseText(idelem, "@root", ns));
            result.setExtension(parseText(idelem, "@extension", ns));
        }

        return result;
    }

    /**
     * Method description
     *
     *
     *
     * @param ns
     * @param elem
     * @param expr
     *
     * @return
     *
     * @throws XPathHelperException
     */
    private String parseText(OMElement elem, String expr, String ns)
            throws XPathHelperException {

        String result = XPathHelper.stringValueOf(elem, expr, ns);

        return StringUtils.trimToNull(result);
    }

    /**
     * Method description
     *
     *
     * @param elem
     *
     * @return
     *
     * @throws ModelBuilderException
     */
    private SubmitDocumentRequest unmarshal(OMElement elem)
            throws ModelBuilderException {

        SubmitDocumentRequest result = new SubmitDocumentRequest();

        try {

            String ns = elem.getNamespace().getNamespaceURI();

            result.setPatientId(parseIdentifier(elem, "./ns:PatientId", ns));
            result.setOrganizationId(parseIdentifier(elem,
                    "./ns:OrganizationId", ns));

            List<OMElement> docelems = XPathHelper.selectNodes(elem,
                                           "./ns:Documents/ns:Document", ns);

            if (docelems != null) {

                List<Document> docs = new ArrayList<Document>();

                for (OMElement docelem : docelems) {

                    docs.add(parseDocument(docelem, ns));
                }

                result.setDocuments(docs);
            }

        } catch (Exception e) {

            throw new ModelBuilderException(e.getMessage(), e);
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param result
     *
     * @throws ModelBuilderException
     */
    private void validate(SubmitDocumentRequest result)
            throws ModelBuilderException {

        StringBuilder errmsg = new StringBuilder();

        // must have a pid, at least a root
        Identifier pid = result.getPatientId();

        if (pid == null || StringUtils.isBlank(pid.getRoot())) {
            errmsg.append("PatientId must have a root attribute.\n");
        }

        List<Document> docs = result.getDocuments();

        if (docs == null || docs.isEmpty()) {

            errmsg.append("Request must contain at least one document.\n");

        } else {

            for (int i = 0; i < docs.size(); ++i) {

                Document doc = docs.get(i);

                CodedValue type = doc.getType();

                if (type == null) {

                    errmsg.append(
                        String.format(
                            "Document %d must have a type.%n", i + 1));

                } else {

                    if (StringUtils.isBlank(type.getCode())) {

                        errmsg.append(
                            String.format(
                                "Document %d must have a type/@code.%n",
                                i + 1));
                    }

                    if (StringUtils.isBlank(type.getCodeSystem())) {

                        errmsg.append(
                            String.format(
                                "Document %d must have a type/@codeSystem.%n",
                                i + 1));
                    }
                }

                if (doc.getContent() == null) {

                    errmsg.append(
                        String.format(
                            "Document %d must contain content.%n", i + 1));
                }
            }
        }

        if (errmsg.length() > 0) {
            throw new ModelBuilderException(errmsg.toString());
        }
    }
}
