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
package com.vangent.hieos.services.xca.gateway.transactions;

import com.vangent.hieos.services.xca.gateway.controller.XCARequestController;
import com.vangent.hieos.services.xca.gateway.controller.XCAAbstractRequestCollection;
import com.vangent.hieos.services.xca.gateway.controller.XCAQueryRequestCollection;
import com.vangent.hieos.services.xca.gateway.controller.XCAQueryRequest;

import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.registry.RegistryUtility;
import com.vangent.hieos.xutil.response.XCAAdhocQueryResponse;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.metadata.structure.ParamParser;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.axis2.context.MessageContext;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMNamespace;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public abstract class XCAAdhocQueryRequest extends XCAAbstractTransaction {

    private final static Logger logger = Logger.getLogger(XCAAdhocQueryRequest.class);

    /**
     *
     * @param queryRequest
     * @param responseOption
     */
    abstract void processRequestWithPatientId(OMElement request, OMElement queryRequest, OMElement responseOption) throws XdsInternalException;

    /**
     * 
     * @param queryRequest
     * @param responseOption
     * @param homeCommunityId
     * @param gatewayConfig
     * @throws XdsInternalException
     */
    abstract void processRemoteCommunityRequest(OMElement queryRequest, OMElement responseOption, String homeCommunityId, XConfigActor gatewayConfig) throws XdsInternalException;

    /**
     *
     * @return
     * @throws XdsInternalException
     */
    abstract XConfigActor getLocalRegistry() throws XdsInternalException;

    /**
     * 
     * @param gatewayConfig
     * @param log_message
     * @param messageContext
     */
    public XCAAdhocQueryRequest(XConfigActor gatewayConfig, XLogMessage log_message, MessageContext messageContext) {
        try {
            super.init(gatewayConfig, log_message, new XCAAdhocQueryResponse(), messageContext);
        } catch (XdsInternalException e) {
            logger.fatal(logger_exception_details(e));
            response.add_error(MetadataSupport.XDSRegistryError,
                    this.getLocalHomeCommunityId(), this.getClass().getName(), log_message);
        }
    }

    /**
     *
     * @param request
     */
    protected void validateRequest(OMElement request) {

        // Validate namespace.
        OMNamespace ns = request.getNamespace();
        String ns_uri = ns.getNamespaceURI();
        if (ns_uri == null || !ns_uri.equals(MetadataSupport.ebQns3.getNamespaceURI())) {
            response.add_error(MetadataSupport.XDSRegistryError,
                    "Invalid XML namespace on AdhocQueryRequest: " + ns_uri,
                    this.getLocalHomeCommunityId(), log_message);
        }

        // Validate against schema.
        try {
            RegistryUtility.schema_validate_local(request, MetadataTypes.METADATA_TYPE_SQ);
        } catch (SchemaValidationException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError,
                    "SchemaValidationException: " + e.getMessage(),
                    this.getLocalHomeCommunityId(), log_message);
        } catch (XdsInternalException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError,
                    "SchemaValidationException: " + e.getMessage(),
                    this.getLocalHomeCommunityId(), log_message);
        }
    }

    /**
     *
     * @param request
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    protected void prepareValidRequests(OMElement request) throws XdsInternalException {
        // Get the AdhocQuery & ResponseOption nodes.
        OMElement queryRequest = MetadataSupport.firstChildWithLocalName(request, "AdhocQuery");
        OMElement responseOption = MetadataSupport.firstChildWithLocalName(request, "ResponseOption");
        if (responseOption == null) {
            throw new XdsInternalException("Cannot find /AdhocQueryRequest/ResponseOption element");
        }

        // First check to see if homeCommunityId is required on request.
        if (this.requiresHomeCommunityId(queryRequest)) {
            this.logInfo("Note", "*** Query requires homeCommunityId ***");

            // Now get the homeCommunityId on the request.
            String homeCommunityId = this.getHomeCommunityId(queryRequest);

            // Is it missing?
            if (homeCommunityId == null) {  // Missing homeCommunityId.
                response.add_error(MetadataSupport.XDSMissingHomeCommunityId,
                        "homeCommunityId missing or empty",
                        this.getLocalHomeCommunityId(), log_message);
            } else {  // homeCommunityId is present.
                this.processTargetedHomeRequest(queryRequest, responseOption, homeCommunityId);
            }
        } else { // homeCommunityId is not required (but still may be present).

            // See if the request has a homeCommunityId.
            String homeCommunityId = this.getHomeCommunityId(queryRequest);
            if (homeCommunityId != null) // homeCommunityId is present.
            {
                this.processTargetedHomeRequest(queryRequest, responseOption, homeCommunityId);
            } else {  // homeCommunityId is not present.
                // Now, find communities that can respond to the request (by patient id).
                this.processRequestWithPatientId(request, queryRequest, responseOption);
            }
        }
    }

     /**
     *
     * @param queryRequest
     * @param responseOption
     * @param homeCommunityId
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    protected void processTargetedHomeRequest(OMElement queryRequest, OMElement responseOption, String homeCommunityId) throws XdsInternalException {
        this.logInfo("HomeCommunityId", homeCommunityId);
        // See if this is for the local community.
        String localHomeCommunityId = this.getLocalHomeCommunityId();
        if (homeCommunityId.equals(localHomeCommunityId)) {  // Destined for the local home.
            this.logInfo("Note", "Going local for homeCommunityId: " + homeCommunityId);

            // XDSAffinityDomain option - get the local registry.
            XConfigActor localRegistry = this.getLocalRegistry();
            if (localRegistry != null) {
                // Add the local request.
                // Just use the registry name as the key (to avoid conflict with
                // local homeCommunityId testing).
                this.addRequest(queryRequest, responseOption, localRegistry.getName(), localRegistry, true);
            }
        } else { // Going remote.
            this.logInfo("Note", "Going remote for homeCommunityId: " + homeCommunityId);
            // See if we know about a remote gateway that can respond.
            XConfigActor gatewayConfig = XConfig.getInstance().getRespondingGatewayConfigForHomeCommunityId(homeCommunityId);
            if (gatewayConfig == null) {
                response.add_error(MetadataSupport.XDSUnknownCommunity,
                        "Do not understand homeCommunityId " + homeCommunityId,
                        this.getLocalHomeCommunityId(), log_message);
            } else {
                // This request is good (targeted for a remote community.
                this.processRemoteCommunityRequest(queryRequest, responseOption, homeCommunityId, gatewayConfig);
            }
        }
    }

    /**
     *
     * @param patientId
     * @return
     */
    protected String getAssigningAuthority(String patientId) {
        // patientId format = <ID>^^^<AA>
        String assigningAuthority = null;

        // The last token will be the assigning authority ... this is a bit of a hack.
        // Had problems with split() and trying to escape the "^".  Anyway, this should work.
        StringTokenizer st = new StringTokenizer(patientId, "^");
        while (st.hasMoreTokens()) {
            assigningAuthority = st.nextToken();
        }

        return assigningAuthority;
    }

    /**
     *
     * @param request
     * @param queryRequest
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    protected String getPatientId(OMElement request, OMElement queryRequest) throws XdsInternalException {
        SqParams params = null;
        String queryId = this.getStoredQueryId(queryRequest);
        if (queryId == null) {
            return null;  // Early exit (FIXME).
        }
        // Parse the query parameters.
        ParamParser parser = new ParamParser();
        try {
            params = parser.parse(request);
        } catch (MetadataValidationException e) {
            response.add_error(MetadataSupport.XDSRegistryMetadataError,
                    "Problem parsing query parameters",
                    this.getLocalHomeCommunityId(), log_message);
        }
        if (params == null) {
            // Must have caught an exception above.
            return null;  // Early exit.
        }
        String pidCXFormatted = null;
        if (queryId.equals(MetadataSupport.SQ_FindDocuments)) {
            // $XDSDocumentEntryPatientId
            pidCXFormatted = params.getStringParm("$XDSDocumentEntryPatientId");
        } else if (queryId.equals(MetadataSupport.SQ_FindFolders)) {
            // $XDSFolderPatientId
            pidCXFormatted = params.getStringParm("$XDSFolderPatientId");
        } else if (queryId.equals(MetadataSupport.SQ_FindSubmissionSets)) {
            // $XDSSubmissionSetPatientId
            pidCXFormatted = params.getStringParm("$XDSSubmissionSetPatientId");
        } else if (queryId.equals(MetadataSupport.SQ_GetAll)) {
            // FIXME: NOT IMPLEMENTED [NEED TO FIGURE OUT WHAT TO PULL OUT HERE.
        }
        return pidCXFormatted;
    }

    /**
     * 
     * @param gatewayName
     * @return
     * @throws XdsInternalException
     */
    protected XConfigActor getLocalRegistry(String gatewayName) throws XdsInternalException {
        // Get the gateway configuration.
        XConfig xconfig = XConfig.getInstance();
        XConfigObject homeCommunity = xconfig.getHomeCommunityConfig();

        // Return the proper registry configuration based upon the gateway configuration.
        XConfigActor gateway = (XConfigActor) homeCommunity.getXConfigObjectWithName(gatewayName, XConfig.XCA_INITIATING_GATEWAY_TYPE);

        // Get the gateway's local registry.
        XConfigActor registry = (XConfigActor) gateway.getXConfigObjectWithName("registry", XConfig.XDSB_DOCUMENT_REGISTRY_TYPE);
        if (registry == null) {
            response.add_error(MetadataSupport.XDSRegistryNotAvailable,
                    "Can not find local registry endpoint",
                    this.getLocalHomeCommunityId(), log_message);
        }
        return registry;
    }

    /**
     *
     * @param queryRequest
     * @return
     */
    protected boolean requiresHomeCommunityId(OMElement queryRequest) {
        boolean requires = true;
        String queryId = this.getStoredQueryId(queryRequest);
        if (queryId == null) {
            requires = false;
        }
        if (queryId.equals(MetadataSupport.SQ_FindDocuments)) {
            requires = false;
        }
        if (queryId.equals(MetadataSupport.SQ_FindFolders)) {
            requires = false;
        }
        if (queryId.equals(MetadataSupport.SQ_FindSubmissionSets)) {
            requires = false;
        }
        if (queryId.equals(MetadataSupport.SQ_GetAll)) {
            requires = false;
        }
        this.logInfo("Note", "query " + queryId + " requires homeCommunityId = " + requires);
        return requires;
    }

    /**
     *
     * @param queryRequest - <AdhocQuery> XML node
     * @return
     */
    protected String getStoredQueryId(OMElement queryRequest) {
        return queryRequest.getAttributeValue(MetadataSupport.id_qname);
    }

    /**
     * Return home community id on request.  Return null if not present.
     *
     * @param queryRequest - <AdhocQuery> XML node
     * @return homeCommunitId string if present, otherwise null.
     */
    protected String getHomeCommunityId(OMElement queryRequest) {
        String homeCommunityId = queryRequest.getAttributeValue(MetadataSupport.home_qname);
        if (homeCommunityId == null || homeCommunityId.equals("")) {
            homeCommunityId = null;
        }
        return homeCommunityId;
    }

    /**
     * 
     * @param queryRequest
     * @param responseOption
     * @param uniqueId
     * @param configActor
     * @param isLocalRequest
     * @return
     */
    protected XCAQueryRequest addRequest(OMElement queryRequest, OMElement responseOption, String uniqueId, XConfigActor configActor, boolean isLocalRequest) {
        XCARequestController requestController = this.getRequestController();
        // FIXME: Logic is a bit problematic -- need to find another way.
        XCAAbstractRequestCollection requestCollection = requestController.getRequestCollection(uniqueId);
        if (requestCollection == null) {
            requestCollection = new XCAQueryRequestCollection(uniqueId, configActor, isLocalRequest);
            requestController.setRequestCollection(requestCollection);
        }

        XCAQueryRequest xcaRequest = new XCAQueryRequest(queryRequest);
        requestCollection.addRequest(xcaRequest);
        xcaRequest.setResponseOption(responseOption);  // Need this also!!
        return xcaRequest;
    }

    /**
     *
     * @param allResponses
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    protected boolean consolidateResponses(ArrayList<OMElement> allResponses) throws XdsInternalException {
        boolean atLeastOneSuccess = false;

        // FIXME: Should we Util.deep_copy() here?
        //OMElement rootResponseNode = response.getRawResponse();  // e.g. <AdhocQueryResponse>
        for (OMElement responseNode : allResponses) {
            // See if the registry response has a success status.
            String status = responseNode.getAttributeValue(MetadataSupport.status_qname);
            this.logInfo("Note", "*** Response Status = " + status + " ***");
            if (status.endsWith("Success")) {
                atLeastOneSuccess = true;
            }

            // Should only be one <RegistryObjectList> at most, but loop anyway.
            ArrayList<OMElement> regObjListNodes = MetadataSupport.decendentsWithLocalName(responseNode, "RegistryObjectList");
            for (OMElement regObjList : regObjListNodes) {
                // Add each child of <RegistryObjectList> to the query result.
                for (Iterator it = regObjList.getChildren(); it.hasNext();) {
                    // DEBUG (START)
                    Object nextNode = it.next();
                    // DEBUG (END)
                    OMElement queryResultNode = null;
                    try {
                        queryResultNode = (OMElement) nextNode;
                    } catch (Exception e) {
                        OMText textNode = (OMText) nextNode;
                        // Only have seen this problem with Intersystems XCA
                        logger.error("***** BUG: " + nextNode.getClass().getName());
                        logger.error(" -- Node -- ");
                        logger.error("isBinary: " + textNode.isBinary());
                        logger.error("isCharacters: " + textNode.isCharacters());
                        logger.error("isOptimized: " + textNode.isOptimized());
                        logger.error(textNode.getText());
                    }
                    response.addQueryResults(queryResultNode);
                }
            }

            // Consolidate all registry errors into the consolidated error list.
            ArrayList<OMElement> registryErrorLists = MetadataSupport.decendentsWithLocalName(responseNode, "RegistryErrorList");

            // Should only be one <RegistryErrorList> at most, but loop anyway.
            for (OMElement registryErrorList : registryErrorLists) {
                response.addRegistryErrorList(registryErrorList, null);  // Place into the final list.
            }

        }
        return atLeastOneSuccess;
    }
}
