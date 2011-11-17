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

import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class description
 *
 * @author         Jim Horner
 */
public class DocumentPolicyResult {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(DocumentPolicyResult.class);

    /** Field description */
    private final List<DocumentMetadata> deniedDocuments;

    /** Field description */
    private final List<DocumentMetadata> permittedDocuments;

    /**
     * Constructs ...
     *
     */
    public DocumentPolicyResult() {

        this.permittedDocuments = new ArrayList<DocumentMetadata>();
        this.deniedDocuments = new ArrayList<DocumentMetadata>();
    }

    /**
     * Method description
     *
     *
     * @param documentMetadata
     * @param response
     * @param callerClazz
     * @param logMessage
     */
    public static void emitDocumentDenialWarning(
            DocumentMetadata documentMetadata, Response response,
            Class callerClazz, XLogMessage logMessage) {

        String msg =
            String.format("Request for document id [%s] denied due to policy",
                          documentMetadata.getDocumentId());

        if (logger.isDebugEnabled()) {

            String debugmsg = String.format("Emitting [%s] from %s.", msg,
                                            callerClazz.getName());

            logger.debug(debugmsg);
        }

        response.add_warning(MetadataSupport.XDSPolicyEvaluationWarning, msg,
                             callerClazz.getName(), logMessage);
    }

    /**
     * Method description
     *
     *
     * @param metadata
     */
    public void addDeniedDocument(DocumentMetadata metadata) {
        this.deniedDocuments.add(metadata);
    }

    /**
     * Method description
     *
     *
     * @param deniedList
     */
    public void addDeniedDocuments(List<DocumentMetadata> deniedList) {

        if (deniedList != null) {

            for (DocumentMetadata deniedDocument : deniedList) {
                addDeniedDocument(deniedDocument);
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param metadata
     */
    public void addPermittedDocument(DocumentMetadata metadata) {
        this.permittedDocuments.add(metadata);
    }

    /**
     * Method description
     *
     *
     * @param permittedList
     */
    public void addPermittedDocuments(List<DocumentMetadata> permittedList) {

        if (permittedList != null) {

            for (DocumentMetadata permittedDocument : permittedList) {
                addPermittedDocument(permittedDocument);
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param response
     * @param callerClazz
     * @param logMessage
     */
    public void emitDocumentDenialWarnings(Response response,
            Class callerClazz, XLogMessage logMessage) {

        for (DocumentMetadata deniedDocument : getDeniedDocuments()) {
            emitDocumentDenialWarning(deniedDocument, response, callerClazz, logMessage);
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<DocumentMetadata> getDeniedDocuments() {
        return Collections.unmodifiableList(this.deniedDocuments);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<DocumentMetadata> getPermittedDocuments() {
        return Collections.unmodifiableList(this.permittedDocuments);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public RegistryObjectElementList getPermittedRegistryObjects() {

        List<OMElement> permittedRegistryObjects = new ArrayList<OMElement>();

        for (DocumentMetadata permittedDocument : getPermittedDocuments()) {
            permittedRegistryObjects.add(permittedDocument.getRegistryObject());
        }

        return new RegistryObjectElementList(permittedRegistryObjects);
    }
}
