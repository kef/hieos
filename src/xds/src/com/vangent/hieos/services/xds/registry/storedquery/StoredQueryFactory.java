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
import com.vangent.hieos.xutil.response.AdhocQueryResponse;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.ParamParser;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.XDSRegistryOutOfResourcesException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.exception.XdsResultNotSinglePatientException;
import com.vangent.hieos.xutil.metadata.structure.SqParams;

import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class StoredQueryFactory {

    private OMElement adhocQueryRequest;
    private boolean returnLeafClass = false;
    private SqParams params;
    private String queryId;
    private XLogMessage logMessage = null;
    private StoredQuery sq;
    private String serviceName;
    private Response response = null;

    /**
     *
     * @return
     */
    public boolean isLeafClassReturnType() {
        OMElement responseOption = MetadataSupport.firstChildWithLocalName(adhocQueryRequest, "ResponseOption");
        if (responseOption == null) {
            return true;
        }
        String returnType = responseOption.getAttributeValue(MetadataSupport.return_type_qname);
        if (returnType == null || returnType.equals("") || !returnType.equals("LeafClass")) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param parmName
     * @return
     */
    public boolean hasParm(String parmName) {
        return params.hasParm(parmName);
    }

    /**
     *
     * @param parmName
     * @return
     */
    public Object getParm(String parmName) {
        return params.getParm(parmName);
    }

    /**
     *
     * @param adhocQueryRequest
     * @param isLeafClassRequest
     * @param response
     * @param logMessage
     * @param serviceName
     * @throws XdsInternalException
     * @throws MetadataException
     * @throws XdsException
     */
    public StoredQueryFactory(
            OMElement adhocQueryRequest, boolean isLeafClassRequest, Response response, XLogMessage logMessage, String serviceName, BackendRegistry backendRegistry) throws XdsInternalException, MetadataException, XdsException {
        this.adhocQueryRequest = adhocQueryRequest;
        this.response = response;
        this.logMessage = logMessage;
        this.serviceName = serviceName;

        /*
        OMElement response_option = MetadataSupport.firstChildWithLocalName(ahqr, "ResponseOption");
        if (response_option == null) {
            throw new XdsInternalException("Cannot find /AdhocQueryRequest/ResponseOption element");
        }

        String return_type = response_option.getAttributeValue(MetadataSupport.return_type_qname);

        if (return_type == null) {
            throw new XdsException("Attribute returnType not found on query request");
        }
        if (return_type.equals("LeafClass")) {
            return_objects = true;
        } else if (return_type.equals("ObjectRef")) {
            return_objects = false;
        } else {
            throw new MetadataException("/AdhocQueryRequest/ResponseOption/@returnType must be LeafClass or ObjectRef. Found value " + return_type);
        }*/
        returnLeafClass = isLeafClassRequest;

        OMElement adhocQuery = MetadataSupport.firstChildWithLocalName(adhocQueryRequest, "AdhocQuery");
        if (adhocQuery == null) {
            throw new XdsInternalException("Cannot find /AdhocQueryRequest/AdhocQuery element");
        }

        ParamParser parser = new ParamParser();
        params = parser.parse(adhocQueryRequest);

        if (logMessage != null) {
            logMessage.addOtherParam("Parameters", params);
        }

        if (this.response == null) {
            // BHT: Fixed bug (was not setting response instance).
            this.response = new AdhocQueryResponse();
        }

        queryId = adhocQuery.getAttributeValue(MetadataSupport.id_qname);

        if (queryId.equals(MetadataSupport.SQ_FindDocuments)) {
            // FindDocuments
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "FindDocuments");
            }
            sq = new FindDocuments(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_FindSubmissionSets)) {
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "FindSubmissionSets");
            }
            // FindSubmissionSets
            sq = new FindSubmissionSets(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_FindFolders)) {
            // FindFolders
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "FindFolders");
            }
            sq = new FindFolders(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_FindDocumentsForMultiplePatients)) {
            // FindDocumentsForMultiplePatients
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "FindDocumentsForMultiplePatients");
            }
            sq = new FindDocumentsForMultiplePatients(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_FindFoldersForMultiplePatients)) {
            // FindFoldersForMultiplePatients
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "FindFoldersForMultiplePatients");
            }
            sq = new FindFoldersForMultiplePatients(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_GetAll)) {
            // GetAll
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "GetAll");
            }
            response.add_error(MetadataSupport.XDSRegistryError, "UnImplemented Stored Query query id = " + queryId, this.getClass().getName(), logMessage);
        } else if (queryId.equals(MetadataSupport.SQ_GetDocuments)) {
            // GetDocuments
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "GetDocuments");
            }
            sq = new GetDocuments(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_GetFolders)) {
            // GetFolders
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "GetFolders");
            }
            sq = new GetFolders(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_GetAssociations)) {
            // GetAssociations
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "GetAssociations");
            }
            sq = new GetAssociations(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_GetDocumentsAndAssociations)) {
            // GetDocumentsAndAssociations
            if (logMessage != null) {
                logMessage.setTestMessage("GetDocumentsAndAssociations");
            }
            sq = new GetDocumentsAndAssociations(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_GetSubmissionSets)) {
            // GetSubmissionSets
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "GetSubmissionSets");
            }
            sq = new GetSubmissionSets(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_GetSubmissionSetAndContents)) {
            // GetSubmissionSetAndContents
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "GetSubmissionSetAndContents");
            }
            sq = new GetSubmissionSetAndContents(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_GetFolderAndContents)) {
            // GetFolderAndContents
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "GetFolderAndContents");
            }
            sq = new GetFolderAndContents(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_GetFoldersForDocument)) {
            // GetFoldersForDocument
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "GetFoldersForDocument");
            }
            sq = new GetFoldersForDocument(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else if (queryId.equals(MetadataSupport.SQ_GetRelatedDocuments)) {
            // GetRelatedDocuments
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + "GetRelatedDocuments");
            }
            sq = new GetRelatedDocuments(params, returnLeafClass, this.response, logMessage, backendRegistry);
        } else {
            if (logMessage != null) {
                logMessage.setTestMessage(serviceName + " " + queryId);
            }
            this.response.add_error(MetadataSupport.XDSRegistryError, "Unknown Stored Query query id = " + queryId, this.getClass().getName(), logMessage);
        }
    }

    /**
     *
     * @param validateConsistentPatientId
     * @param maxLeafObjectsAllowedFromQuery
     * @return
     * @throws XDSRegistryOutOfResourcesException
     * @throws XdsResultNotSinglePatientException
     * @throws XdsException
     */
    public List<OMElement> run(boolean validateConsistentPatientId, long maxLeafObjectsAllowedFromQuery)
            throws XDSRegistryOutOfResourcesException, XdsResultNotSinglePatientException, XdsException {
        sq.setMaxLeafObjectsAllowedFromQuery(maxLeafObjectsAllowedFromQuery);
        return sq.run(validateConsistentPatientId);
    }
}
