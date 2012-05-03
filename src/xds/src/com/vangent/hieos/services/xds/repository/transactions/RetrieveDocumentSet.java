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
import com.vangent.hieos.services.xds.policy.DocumentPolicyEvaluator;
import com.vangent.hieos.services.xds.policy.DocumentPolicyResult;
import com.vangent.hieos.services.xds.policy.DocumentResponse;
import com.vangent.hieos.services.xds.policy.DocumentResponseBuilder;
import com.vangent.hieos.services.xds.policy.DocumentResponseElementList;
import com.vangent.hieos.services.xds.policy.XDSRegistryClient;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
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
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.ActorType;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.AuditEventType;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.IHETransaction;
import com.vangent.hieos.xutil.atna.ATNAAuditEventHelper;
import com.vangent.hieos.xutil.atna.ATNAAuditEventRetrieveDocumentSet;

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
            response.add_error(MetadataSupport.XDSRepositoryError, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (Exception e)
        {

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
                // Add documents to the response.
                OMElement repoResponse = response.getResponse();
                for (OMElement documentResponseNode : documentResponseNodes) {
                    repoResponse.addChild(documentResponseNode);
                }
            } else {
                PDPResponse pdpResponse = pep.evaluate();
                if (pdpResponse.isDenyDecision()) {
                    if (log_message.isLogEnabled()) {
                        log_message.addOtherParam("Policy:Note", "DENIED access to all content");
                    }
                    response.add_warning(MetadataSupport.XDSPolicyEvaluationWarning, "Request denied due to policy", this.getClass().getName(), log_message);
                } else if (!pdpResponse.hasObligations()) {
                    if (log_message.isLogEnabled()) {
                        log_message.addOtherParam("Policy:Note", "PERMITTED access to all content [no obligations]");
                    }
                    // No obligations.
                    ArrayList<OMElement> documentResponseNodes = retrieveDocuments(rds);
                    OMElement repoResponse = response.getResponse();
                    for (OMElement documentResponseNode : documentResponseNodes) {
                        repoResponse.addChild(documentResponseNode);
                    }
                } else {
                    // Has obligations.
                    this.handleObligations(pdpResponse, rds);
                }
            }

            //AUDIT:POINT
            //call to audit message for document repository
            //for Transaction id = ITI-43. (Retrieve Document Set)
            //Here document consumer is treated as document repository
            this.auditRetrieveDocumentSet(rds);
            //performAudit(
            //        XATNALogger.TXN_ITI43,
            //        rds,
            //        null,
            //        XATNALogger.ActorType.REPOSITORY,
            //        XATNALogger.OutcomeIndicator.SUCCESS);

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
     * @param pdpResponse
     * @param rds
     * @throws MetadataException
     * @throws XdsException
     */
    private void handleObligations(PDPResponse pdpResponse, OMElement rds) throws MetadataException, XdsException, PolicyException, SOAPFaultException {
        // Retrieve the documents from the data store.
        ArrayList<OMElement> documentResponseNodes = retrieveDocuments(rds);

        // See if we have any documents.
        if (documentResponseNodes != null && !documentResponseNodes.isEmpty()) {
            // Build List<DocumentResponse> from list of DocumentResponse OMElements.
            DocumentResponseBuilder documentResponseBuilder = new DocumentResponseBuilder();
            List<DocumentResponse> documentResponseList = documentResponseBuilder.buildDocumentResponseList(
                    new DocumentResponseElementList(documentResponseNodes));

            // Go to the registry to get meta-data for the documents.
            XDSRegistryClient registryClient = new XDSRegistryClient(repoConfig.getRegistryConfig());

            // See if document-level policy evaluation has been delegated to the registry.
            boolean delegateDocumentLevelPolicyEval = this.getConfigActor().getPropertyAsBoolean("DelegateDocumentLevelPolicyEval", true);
            List<DocumentMetadata> registryObjects = registryClient.getRegistryObjects(documentResponseList, delegateDocumentLevelPolicyEval);

            List<DocumentMetadata> permittedDocumentList = null;
            if (delegateDocumentLevelPolicyEval) {
                // Since we are delegating policy evaluation to the registry, the registry should
                // only return permitted objects.
                permittedDocumentList = registryObjects;
            } else {
                // Otherwise, we must evaluate policy here.

                // Get list of obligation ids to satisfy ... these will be used as the "action-id"
                // when evaluating policy at the document-level
                List<String> obligationIds = pdpResponse.getObligationIds();
                // FIXME(?): Only satisfy the first obligation in the list!

                // Run policy evaluation to get permitted objects list (using obligation id as "action-id").
                DocumentPolicyEvaluator policyEvaluator = new DocumentPolicyEvaluator(null); // Logged later.
                DocumentPolicyResult policyResult = policyEvaluator.evaluate(
                        obligationIds.get(0),
                        pdpResponse.getRequestType(),
                        registryObjects);
                permittedDocumentList = policyResult.getPermittedDocuments();
            }

            // Now, add list of permitted documents to the response.
            this.addPermittedDocumentsToResponse(documentResponseList, permittedDocumentList);
        }
    }

    /**
     *
     * @param documentResponseList
     * @param permittedDocumentList
     * @throws XdsInternalException
     * @return a list of denied document metadata (abbreviated - ids only)
     */
    private void addPermittedDocumentsToResponse(List<DocumentResponse> documentResponseList, List<DocumentMetadata> permittedDocumentList) throws XdsInternalException {
        StringBuilder logsb = new StringBuilder();
        OMElement repoResponse = response.getResponse();
        // Go through each DocumentResponse.
        for (DocumentResponse documentResponse : documentResponseList) {
            boolean permittedAccessToDocument = this.isPermittedAccessToDocument(documentResponse, permittedDocumentList);
            // If permitted.
            if (permittedAccessToDocument) {
                // Add to the response.
                OMElement documentResponseNode = documentResponse.getDocumentResponseObject();
                repoResponse.addChild(documentResponseNode);
            } else {
                DocumentPolicyResult.emitDocumentDenialWarning(new DocumentMetadata(documentResponse),
                        response, getClass(), this.log_message);
            }

            if (log_message.isLogEnabled()) {
                if (permittedAccessToDocument) {
                    logsb.append("...PERMIT" + "[doc_id=").append(documentResponse.getDocumentId()).append(", repo_id=").append(documentResponse.getRepositoryId()).append("]");
                } else {
                    logsb.append("...DENY" + "[doc_id=").append(documentResponse.getDocumentId()).append(", repo_id=").append(documentResponse.getRepositoryId()).append("]");
                }
            }
        }
        if (log_message.isLogEnabled()) {
            log_message.addOtherParam("DelegateDocumentLevelPolicyEval",
                    this.getConfigActor().getProperty("DelegateDocumentLevelPolicyEval"));
            log_message.addOtherParam("Policy:Note", logsb.toString());
        }
    }

    /**
     *
     * @param documentResponse
     * @param permittedDocumentList
     * @return
     */
    private boolean isPermittedAccessToDocument(DocumentResponse documentResponse, List<DocumentMetadata> permittedDocumentList) {
        // Get DocumentResponse vitals (docId, repoId).
        String docResponseDocId = documentResponse.getDocumentId();
        String docResponseRepoId = documentResponse.getRepositoryId();

        // See if the document was returned by the registry (Again, assuming that
        // if document meta-data was returned by the registry, then the
        // subject has been permitted access to the document).
        for (DocumentMetadata permittedDocument : permittedDocumentList) {
            String permittedObjectDocId = permittedDocument.getDocumentId();
            String permittedObjectRepoId = permittedDocument.getRepositoryId();

            // Compare against docId/repoId.
            if (docResponseDocId.equalsIgnoreCase(permittedObjectDocId)
                    && docResponseRepoId.equalsIgnoreCase(permittedObjectRepoId)) {
                return true;  // Found match -- permitted.
            }
        }
        return false;  // No match found -- not permitted.
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
     * @param rootNode
     */
    private void auditRetrieveDocumentSet(OMElement rootNode) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                // Create and log audit event.
                ATNAAuditEventRetrieveDocumentSet auditEvent = ATNAAuditEventHelper.getATNAAuditEventRetrieveDocumentSet(rootNode);
                auditEvent.setActorType(ActorType.REPOSITORY);
                auditEvent.setTransaction(IHETransaction.ITI43);
                auditEvent.setAuditEventType(AuditEventType.EXPORT);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            // FIXME?:
        }
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
        @Override
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
        @Override
        public String getContentType() {
            return contentType;
        }

        /**
         *
         * @return
         */
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        /**
         *
         * @return
         */
        @Override
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException();
        }
    }
}
