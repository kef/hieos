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
package com.vangent.hieos.xutil.atna;

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.ParamParser;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import java.util.List;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class ATNAAuditEventHelper {

    private final static Logger logger = Logger.getLogger(ATNAAuditEventHelper.class);

    /**
     * 
     * @param rootNode
     * @return
     */
    public static ATNAAuditEventQuery getATNAAuditEventRegistryStoredQuery(OMElement rootNode) {
        // Get vitals ...
        OMElement adhocQuery = MetadataSupport.firstChildWithLocalName(rootNode, "AdhocQuery");
        String storedQueryId = adhocQuery.getAttributeValue(MetadataSupport.id_qname);
        String homeCommunityId = ATNAAuditEventHelper.getHomeCommunityId(adhocQuery);
        String patientId = ATNAAuditEventHelper.getQueryPatientID(rootNode, storedQueryId);
        String queryText = adhocQuery.toString();

        // Create and log audit event.
        ATNAAuditEventQuery auditEvent = new ATNAAuditEventQuery();
        auditEvent.setTransaction(ATNAAuditEvent.IHETransaction.ITI18);
        auditEvent.setActorType(ATNAAuditEvent.ActorType.REGISTRY);
        auditEvent.addPatientId(patientId);
        auditEvent.setQueryId(storedQueryId);
        auditEvent.setQueryText(queryText);
        auditEvent.setHomeCommunityId(homeCommunityId);
        return auditEvent;
    }

    /**
     *
     * @param rootNode
     * @return
     */
    public static ATNAAuditEventRetrieveDocumentSet getATNAAuditEventRetrieveDocumentSet(OMElement rootNode) {

        // Create audit event.
        ATNAAuditEventRetrieveDocumentSet auditEvent = new ATNAAuditEventRetrieveDocumentSet();
        auditEvent.setTransaction(ATNAAuditEvent.IHETransaction.ITI43);
        auditEvent.setActorType(ATNAAuditEvent.ActorType.REPOSITORY);

        // Document URIs:
        List<ATNAAuditDocument> documents = auditEvent.getDocuments();
        for (OMElement doc_request : MetadataSupport.childrenWithLocalName(rootNode, "DocumentRequest")) {
            String homeCommunityId = null;
            String repositoryUniqueId = MetadataSupport.firstChildWithLocalName(doc_request, "RepositoryUniqueId").getText();
            String documentId = MetadataSupport.firstChildWithLocalName(doc_request, "DocumentUniqueId").getText();
            OMElement homeNode = MetadataSupport.firstChildWithLocalName(doc_request, "HomeCommunityId");
            if (homeNode != null) {
                homeCommunityId = homeNode.getText();
            }
            // Document:
            ATNAAuditDocument document = new ATNAAuditDocument();
            document.setDocumentUniqueId(documentId);
            document.setHomeCommunityId(homeCommunityId);
            document.setRepositoryUniqueId(repositoryUniqueId);
            documents.add(document);
        }
        return auditEvent;
    }

    /**
     *
     * @param rootNode
     * @return 
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public static ATNAAuditEventRegisterDocumentSet getATNAAuditEventRegisterDocumentSet(OMElement rootNode) throws MetadataException, MetadataValidationException {
        // Get vitals ...
        Metadata m = new Metadata(rootNode);
        String patientId = m.getSubmissionSetPatientId();
        String submissionSetId = m.getSubmissionSetUniqueId();

        // Create audit event.
        ATNAAuditEventRegisterDocumentSet auditEvent = new ATNAAuditEventRegisterDocumentSet();
        auditEvent.setTransaction(ATNAAuditEvent.IHETransaction.ITI42);
        auditEvent.setActorType(ATNAAuditEvent.ActorType.REGISTRY);
        auditEvent.setPatientId(patientId);
        auditEvent.setSubmissionSetId(submissionSetId);
        return auditEvent;
    }

    /**
     *
     * @param rootNode
     * @return
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public static ATNAAuditEventRegisterDocumentSet getATNAAuditEventProvideAndRegisterDocumentSet(OMElement rootNode) throws MetadataException, MetadataValidationException {
        return ATNAAuditEventHelper.getATNAAuditEventRegisterDocumentSet(rootNode);
    }

    /**
     *
     * @param request
     * @param queryId
     * @return
     */
    public static String getQueryPatientID(OMElement request, String queryId) {
        if (queryId == null) {
            return "QueryId not known";  // Early exit (FIXME).
        }
        // Parse the query parameters.
        ParamParser parser = new ParamParser();
        SqParams params = null;
        try {
            params = parser.parse(request);
        } catch (MetadataValidationException ex) {
            logger.error("Could not parse stored query in ATNA", ex);
        } catch (XdsInternalException ex) {
            logger.error("Could not parse stored query in ATNA", ex);
        }

        if (params == null) {
            return "Query Parameters could not be parsed";  // Early exit.
        }
        String patientId = null;
        if (queryId.equals(MetadataSupport.SQ_FindDocuments)) {
            // $XDSDocumentEntryPatientId
            patientId = params.getStringParm("$XDSDocumentEntryPatientId");
        } else if (queryId.equals(MetadataSupport.SQ_FindFolders)) {
            // $XDSFolderPatientId
            patientId = params.getStringParm("$XDSFolderPatientId");
        } else if (queryId.equals(MetadataSupport.SQ_FindSubmissionSets)) {
            // $XDSSubmissionSetPatientId
            patientId = params.getStringParm("$XDSSubmissionSetPatientId");
        } else if (queryId.equals(MetadataSupport.SQ_GetAll)) {
            // FIXME: NOT IMPLEMENTED [NEED TO FIGURE OUT WHAT TO PULL OUT HERE.
            return "SQ_GetAll not implemented";
        }
        if (patientId == null) {
            return "PatientId not present on request";
        }
        return patientId;
    }

    /**
     *
     * @param queryRequest
     * @return
     */
    public static String getHomeCommunityId(OMElement queryRequest) {
        String homeCommunityId = queryRequest.getAttributeValue(MetadataSupport.home_qname);
        if (homeCommunityId == null || homeCommunityId.equals("")) {
            homeCommunityId = null;
        }
        /*if (homeCommunityId == null) {
        homeCommunityId = "HomeCommunityId not present in request";
        }*/
        return homeCommunityId;
    }
}
