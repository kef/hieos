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
package com.vangent.hieos.services.xds.repository.transactions;

import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.pdp.model.PDPResponse;
import com.vangent.hieos.policyutil.pep.impl.PEP;
import com.vangent.hieos.services.xds.policy.DocumentMetadata;
import com.vangent.hieos.services.xds.policy.DocumentResponse;
import com.vangent.hieos.services.xds.policy.DocumentResponseBuilder;
import com.vangent.hieos.services.xds.policy.DocumentResponseElementList;
import com.vangent.hieos.services.xds.policy.XDSRegistryClient;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsFormatException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.registry.RegistryUtility;
import com.vangent.hieos.xutil.response.RetrieveMultipleResponse;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.services.xds.repository.support.Repository;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import com.vangent.hieos.services.xds.repository.storage.XDSDocument;
import com.vangent.hieos.services.xds.repository.storage.XDSRepositoryStorage;

import com.vangent.hieos.xutil.exception.XDSDocumentUniqueIdError;
import java.util.ArrayList;

import javax.activation.DataHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman (plugged in new repository storage mechanism).
 */
public class RetrieveDocumentSet extends XBaseTransaction {

    String registry_endpoint = null;
    MessageContext messageContext;
    boolean optimize = true;
    private Repository repoConfig = null;
    private final static Logger logger = Logger.getLogger(RetrieveDocumentSet.class);

    /**
     *
     * @param log_message
     * @param messageContext
     */
    public RetrieveDocumentSet(XLogMessage log_message, MessageContext messageContext) {
        this.log_message = log_message;
        this.messageContext = messageContext;
        try {
            init(new RetrieveMultipleResponse(), messageContext);
        } catch (XdsInternalException e) {
            logger.fatal(logger_exception_details(e));
            response.add_error(MetadataSupport.XDSRepositoryError, e.getMessage(), this.getClass().getName(), log_message);
        }
    }

    /**
     * 
     * @param rds
     * @param optimize
     * @param service
     * @return
     * @throws SchemaValidationException
     * @throws XdsInternalException
     */
    public OMElement run(final OMElement rds, boolean optimize, XAbstractService service) throws SchemaValidationException, XdsInternalException {
        repoConfig = new Repository(this.getConfigActor());
        this.optimize = optimize;
        OMNamespace ns = rds.getNamespace();
        String nsURI = ns.getNamespaceURI();
        if (nsURI == null || !nsURI.equals(MetadataSupport.xdsB.getNamespaceURI())) {
            return service.start_up_error(rds, "RetrieveDocumentSet.java", XAbstractService.ActorType.REPOSITORY, "Invalid namespace on RetrieveDocumentSetRequest (" + nsURI + ")", true);
        }
        try {
            RegistryUtility.schema_validate_local(rds, MetadataTypes.METADATA_TYPE_RET);
        } catch (Exception e) {
            return service.start_up_error(rds, "RetrieveDocumentSet.java", XAbstractService.ActorType.REPOSITORY, "Schema validation errors:\n" + e.getMessage(), true);
        }
        try {
            // Policy Enforcement:
            PEP pep = new PEP(this.getConfigActor());
            boolean policyEnabled = pep.isPolicyEnabled();
            if (!policyEnabled) {
                // Policy is not enabled .. so retrieve the documents.
                ArrayList<OMElement> documentResponseNodes = retrieveDocuments(rds);
                OMElement repoResponse = response.getResponse();
                if (documentResponseNodes != null) {
                    for (OMElement documentResponseNode : documentResponseNodes) {
                        repoResponse.addChild(documentResponseNode);
                    }
                }
            } else {
                PDPResponse pdpResponse = pep.evaluate();
                if (pdpResponse.isDenyDecision()) {
                    response.add_error(MetadataSupport.XDSRepositoryError, "Request denied due to policy", this.getClass().getName(), log_message);
                } else if (!pdpResponse.hasObligations()) {
                    // No obligations.
                    ArrayList<OMElement> documentResponseNodes = retrieveDocuments(rds);
                    // FIXME: Should we check to see if the PID (for each doc) is = resource-id?
                    // FIXME: We will need to hook into the registry in this case?
                    OMElement repoResponse = response.getResponse();
                    if (documentResponseNodes != null) {
                        for (OMElement documentResponseNode : documentResponseNodes) {
                            repoResponse.addChild(documentResponseNode);
                        }
                    }
                } else {
                    // Has obligations.
                    this.handleObligations(rds);
                }
            }

            //AUDIT:POINT
            //call to audit message for document repository
            //for Transaction id = ITI-43. (Retrieve Document Set)
            //Here document consumer is treated as document repository
            performAudit(
                    XATNALogger.TXN_ITI43,
                    rds,
                    null,
                    XATNALogger.ActorType.REPOSITORY,
                    XATNALogger.OutcomeIndicator.SUCCESS);

        } catch (XdsFormatException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "SOAP Format Error: " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (MetadataException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "Request Validation Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (XdsException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (PolicyException e) {
            // We are unable to satisfy the Policy Evaluation request, so we must deny.
            response.add_error(MetadataSupport.XDSRepositoryError, "Policy Exception: " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (Exception e) {
            // Should never get here.
            response.add_error(MetadataSupport.XDSRepositoryError, e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        }
        this.log_response();
        return response.getRoot();
    }

    /**
     * 
     * @param rds
     * @throws MetadataException
     * @throws XdsException
     * @throws AxisFault
     */
    private void handleObligations(OMElement rds) throws MetadataException, XdsException, AxisFault {
        
        // Retrieve the documents from the data store.
        ArrayList<OMElement> documentResponseNodes = retrieveDocuments(rds);

        // See if we have any documents.
        if (documentResponseNodes != null && !documentResponseNodes.isEmpty()) {
            DocumentResponseBuilder documentResponseBuilder = new DocumentResponseBuilder();
            List<DocumentResponse> documentResponseList = documentResponseBuilder.buildDocumentResponseList(
                    new DocumentResponseElementList(documentResponseNodes));
            XDSRegistryClient registryClient = new XDSRegistryClient(repoConfig.getRegistryConfig());
            List<DocumentMetadata> documentMetadataList =
                    registryClient.getRegistryObjects(documentResponseList);
            // FIXME: The registry could be checking policy, so we should not check twice!!!
            // FIXME: How do we deal with this case?
            //DocumentPolicyEvaluator policyEvaluator = new DocumentPolicyEvaluator();
            //List<OMElement> permittedExtrinsicObjects = policyEvaluator.evaluate(this.getPDPResponse().getRequestType(), extrinsicObjects);
            List<DocumentMetadata> permittedObjectList = documentMetadataList;
            // Above line implies that registry policy checking already happened.
            OMElement repoResponse = response.getResponse();
            // Need to do filter ... may not be in identical order as registry response ...
            for (DocumentResponse documentResponse : documentResponseList) {
                String docResponseDocId = documentResponse.getDocumentId();
                String docResponseRepoId = documentResponse.getRepositoryId();
                // Brute force now (sequential search) ok for small lists ...
                for (DocumentMetadata permittedObject : permittedObjectList) {
                    String permittedObjectDocId = permittedObject.getDocumentId();
                    String permittedObjectRepoId = permittedObject.getRepositoryId();
                    // Compare against repoid/docid.
                    if (docResponseDocId.equalsIgnoreCase(permittedObjectDocId)
                            && docResponseRepoId.equalsIgnoreCase(permittedObjectRepoId)) {
                        // Found match!
                        OMElement documentResponseNode = documentResponse.getDocumentResponseObject();
                        repoResponse.addChild(documentResponseNode);
                        break;
                    }
                }
            }
        }
    }

    /**
     *
     * @param rds
     * @return
     * @throws com.vangent.hieos.xutil.exception.MetadataException
     * @throws com.vangent.hieos.xutil.exception.XdsException
     */
    private ArrayList<OMElement> retrieveDocuments(OMElement rds) throws MetadataException, XdsException {
        ArrayList<OMElement> documentResponseNodes = new ArrayList<OMElement>();
        for (OMElement documentRequestNode : MetadataSupport.childrenWithLocalName(rds, "DocumentRequest")) {
            String repositoryId = null;
            String documentId = null;
            try {
                repositoryId = MetadataSupport.firstChildWithLocalName(documentRequestNode, "RepositoryUniqueId").getText();
                if (repositoryId == null || repositoryId.equals("")) {
                    throw new Exception("");
                }
            } catch (Exception e) {
                throw new MetadataException("Cannot extract RepositoryUniqueId from DocumentRequest");
            }
            try {
                documentId = MetadataSupport.firstChildWithLocalName(documentRequestNode, "DocumentUniqueId").getText();
                if (documentId == null || documentId.equals("")) {
                    throw new Exception("");
                }
            } catch (Exception e) {
                throw new MetadataException("Cannot extract DocumentUniqueId from DocumentRequest");
            }
            OMElement documentResponseNode = retrieveDocument(repositoryId, documentId);
            if (documentResponseNode != null) {
                documentResponseNodes.add(documentResponseNode);
            }
        }
        return documentResponseNodes;
    }

    /**
     *
     * @param repositoryId
     * @param documentId
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsException
     */
    private OMElement retrieveDocument(String repositoryId, String documentId) throws XdsException {
        if (!repositoryId.equals(repoConfig.getRepositoryUniqueId())) {
            response.add_error(MetadataSupport.XDSUnknownRepositoryId,
                    "Repository Unique ID in request "
                    + repositoryId
                    + " does not match this repository's id "
                    + repoConfig.getRepositoryUniqueId(),
                    this.getClass().getName(), log_message);
            return null;
        }

        // Retrieve the document from disk.
        XDSDocument doc = new XDSDocument(repositoryId);
        doc.setUniqueId(documentId);
        XDSRepositoryStorage repoStorage = XDSRepositoryStorage.getInstance();
        try {
            doc = repoStorage.retrieve(doc);
        } catch (XDSDocumentUniqueIdError e) {
            response.add_error(MetadataSupport.XDSDocumentUniqueIdError,
                    "Document Unique ID in request "
                    + documentId
                    + " not found in this repository "
                    + repoConfig.getRepositoryUniqueId(),
                    this.getClass().getName(), log_message);
            return null;

        }
        // Set up the DataHandler.
        ByteArrayDataSource ds = new ByteArrayDataSource();
        ds.setBytes(doc.getBytes());
        ds.setName(doc.getUniqueId());
        ds.setContentType(doc.getMimeType());
        javax.activation.DataHandler dataHandler = new DataHandler(ds);

        OMText t = MetadataSupport.om_factory.createOMText(dataHandler, optimize);
        t.setOptimize(optimize);
        OMElement documentResponseNode = MetadataSupport.om_factory.createOMElement("DocumentResponse", MetadataSupport.xdsB);

        OMElement repositoryIdNode = MetadataSupport.om_factory.createOMElement("RepositoryUniqueId", MetadataSupport.xdsB);
        repositoryIdNode.addChild(MetadataSupport.om_factory.createOMText(repositoryId));
        documentResponseNode.addChild(repositoryIdNode);

        OMElement documentIdNode = MetadataSupport.om_factory.createOMElement("DocumentUniqueId", MetadataSupport.xdsB);
        documentIdNode.addChild(MetadataSupport.om_factory.createOMText(documentId));
        documentResponseNode.addChild(documentIdNode);

        OMElement mimeTypeNode = MetadataSupport.om_factory.createOMElement("mimeType", MetadataSupport.xdsB);
        mimeTypeNode.addChild(MetadataSupport.om_factory.createOMText(doc.getMimeType()));
        documentResponseNode.addChild(mimeTypeNode);

        OMElement documentNode = MetadataSupport.om_factory.createOMElement("Document", MetadataSupport.xdsB);
        documentNode.addChild(t);
        documentResponseNode.addChild(documentNode);
        return documentResponseNode;
    }

    /**
     *
     */
    public class ByteArrayDataSource implements javax.activation.DataSource {

        private byte[] bytes;
        private String contentType;
        private String name;

        /**
         *
         * @param name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         *
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         *
         * @param bytes
         */
        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        /**
         * 
         * @return
         */
        public byte[] getBytes() {
            return bytes;
        }

        /**
         *
         * @param contentType
         */
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        /**
         *
         * @return
         */
        public String getContentType() {
            return contentType;
        }

        /**
         *
         * @return
         */
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        /**
         *
         * @return
         */
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException();
        }
    }
}
