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

import com.vangent.hieos.xutil.response.AdhocQueryResponse;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.ParamParser;
import com.vangent.hieos.xutil.query.StoredQuery;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.XDSRegistryOutOfResourcesException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.SqParams;

import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class StoredQueryFactory {
    OMElement ahqr;
    boolean return_objects = false;
    SqParams params;
    String query_id;
    XLogMessage log_message = null;
    StoredQuery sq;
    String service_name;
    Response response = null;

    /**
     *
     * @return
     */
    public boolean isLeafClassReturnType() {
        OMElement response_option = MetadataSupport.firstChildWithLocalName(ahqr, "ResponseOption");
        if (response_option == null) {
            return true;
        }
        String return_type = response_option.getAttributeValue(MetadataSupport.return_type_qname);
        if (return_type == null || return_type.equals("") || !return_type.equals("LeafClass")) {
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
     * @param ahqr
     * @param resp
     * @param lmsg
     * @param sname
     * @param secure
     * @throws XdsInternalException
     * @throws MetadataException
     * @throws XdsException
     */
    public StoredQueryFactory(OMElement ahqr, Response resp, XLogMessage lmsg, String sname) throws XdsInternalException, MetadataException, XdsException {
        this.ahqr = ahqr;
        this.response = resp;
        this.log_message = lmsg;
        this.service_name = sname;

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
        }

        OMElement adhoc_query = MetadataSupport.firstChildWithLocalName(ahqr, "AdhocQuery");
        if (adhoc_query == null) {
            throw new XdsInternalException("Cannot find /AdhocQueryRequest/AdhocQuery element");
        }

        ParamParser parser = new ParamParser();
        params = parser.parse(ahqr);

        if (log_message != null) {
            log_message.addOtherParam("Parameters", params);
        }

        if (this.response == null) {
            // BHT: Fixed bug (was not setting response instance).
            this.response = new AdhocQueryResponse();
        }

        query_id = adhoc_query.getAttributeValue(MetadataSupport.id_qname);

        if (query_id.equals(MetadataSupport.SQ_FindDocuments)) {
            // FindDocuments
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "FindDocuments");
            }
            sq = new FindDocuments(params, return_objects, this.response, log_message);
        } else if (query_id.equals(MetadataSupport.SQ_FindSubmissionSets)) {
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "FindSubmissionSets");
            }
            // FindSubmissionSets
            sq = new FindSubmissionSets(params, return_objects, this.response, log_message);
        } else if (query_id.equals(MetadataSupport.SQ_FindFolders)) {
            // FindFolders
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "FindFolders");
            }
            sq = new FindFolders(params, return_objects, this.response, log_message);
        }
        else if (query_id.equals(MetadataSupport.SQ_FindDocumentsForMultiplePatients)) {
            // FindDocumentsForMultiplePatients
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "FindDocumentsForMultiplePatients");
            }
            sq = new FindDocumentsForMultiplePatients(params, return_objects, this.response, log_message);
        }
        else if (query_id.equals(MetadataSupport.SQ_FindFoldersForMultiplePatients)) {
            // FindFoldersForMultiplePatients
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "FindFoldersForMultiplePatients");
            }
            sq = new FindFoldersForMultiplePatients(params, return_objects, this.response, log_message);
        }
        else if (query_id.equals(MetadataSupport.SQ_GetAll)) {
            // GetAll
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "GetAll");
            }
            response.add_error(MetadataSupport.XDSRegistryError, "UnImplemented Stored Query query id = " + query_id, this.getClass().getName(), log_message);
        } else if (query_id.equals(MetadataSupport.SQ_GetDocuments)) {
            // GetDocuments
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "GetDocuments");
            }
            sq = new GetDocuments(params, return_objects, this.response, log_message);
        } else if (query_id.equals(MetadataSupport.SQ_GetFolders)) {
            // GetFolders
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "GetFolders");
            }
            sq = new GetFolders(params, return_objects, this.response, log_message);
        } else if (query_id.equals(MetadataSupport.SQ_GetAssociations)) {
            // GetAssociations
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "GetAssociations");
            }
            sq = new GetAssociations(params, return_objects, this.response, log_message);
        } else if (query_id.equals(MetadataSupport.SQ_GetDocumentsAndAssociations)) {
            // GetDocumentsAndAssociations
            if (log_message != null) {
                log_message.setTestMessage("GetDocumentsAndAssociations");
            }
            sq = new GetDocumentsAndAssociations(params, return_objects, this.response, log_message);
        } else if (query_id.equals(MetadataSupport.SQ_GetSubmissionSets)) {
            // GetSubmissionSets
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "GetSubmissionSets");
            }
            sq = new GetSubmissionSets(params, return_objects, this.response, log_message);
        } else if (query_id.equals(MetadataSupport.SQ_GetSubmissionSetAndContents)) {
            // GetSubmissionSetAndContents
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "GetSubmissionSetAndContents");
            }
            sq = new GetSubmissionSetAndContents(params, return_objects, this.response, log_message);
        } else if (query_id.equals(MetadataSupport.SQ_GetFolderAndContents)) {
            // GetFolderAndContents
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "GetFolderAndContents");
            }
            sq = new GetFolderAndContents(params, return_objects, this.response, log_message);
        } else if (query_id.equals(MetadataSupport.SQ_GetFoldersForDocument)) {
            // GetFoldersForDocument
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "GetFoldersForDocument");
            }
            sq = new GetFoldersForDocument(params, return_objects, this.response, log_message);
        } else if (query_id.equals(MetadataSupport.SQ_GetRelatedDocuments)) {
            // GetRelatedDocuments
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + "GetRelatedDocuments");
            }
            sq = new GetRelatedDocuments(params, return_objects, this.response, log_message);
        } else {
            if (log_message != null) {
                log_message.setTestMessage(service_name + " " + query_id);
            }
            this.response.add_error(MetadataSupport.XDSRegistryError, "Unknown Stored Query query id = " + query_id, this.getClass().getName(), log_message);
        }
    }

    /**
     *
     * @return
     * @throws XDSRegistryOutOfResourcesException
     * @throws XdsException
     */
    public List<OMElement> run() throws XDSRegistryOutOfResourcesException, XdsException {
        return sq.run();
    }
}
