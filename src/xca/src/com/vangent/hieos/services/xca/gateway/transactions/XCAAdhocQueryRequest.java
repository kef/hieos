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

import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.registry.RegistryUtility;
import com.vangent.hieos.xutil.response.XCAAdhocQueryResponse;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.metadata.structure.ParamParser;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.metadata.structure.SqParams;

// Exceptions.
import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.exception.XdsInternalException;

import com.vangent.hieos.hl7v3util.client.PIXManagerClient;
import com.vangent.hieos.hl7v3util.model.subject.Custodian;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.services.xca.gateway.controller.XCARequestController;
import com.vangent.hieos.services.xca.gateway.controller.XCAAbstractRequestCollection;
import com.vangent.hieos.services.xca.gateway.controller.XCAQueryRequestCollection;
import com.vangent.hieos.services.xca.gateway.controller.XCAQueryRequest;

// XConfig
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;


// XATNA.
import com.vangent.hieos.xutil.atna.XATNALogger;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMNamespace;
import org.apache.log4j.Logger;

/**
 * Plugged into current NIST framework.
 *
 * @author Bernie Thuman
 */
public class XCAAdhocQueryRequest extends XCAAbstractTransaction {

    private final static Logger logger = Logger.getLogger(XCAAdhocQueryRequest.class);

    public enum InitiatingGatewayMode {

        PassThrough, XCPD
    };
    private static XConfigActor _xcpdIGConfig = null;

    /**
     * 
     * @param gatewayType
     * @param log_message
     * @param messageContext
     */
    public XCAAdhocQueryRequest(GatewayType gatewayType, XLogMessage log_message, MessageContext messageContext) {
        try {
            super.init(gatewayType, log_message, new XCAAdhocQueryResponse(), messageContext);
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

        // Perform ATNA audit (FIXME - may not be best place).
        String ATNAtxn;
        if (this.getGatewayType() == GatewayType.InitiatingGateway) {
            ATNAtxn = XATNALogger.TXN_ITI18;
        } else {
            ATNAtxn = XATNALogger.TXN_ITI38;
        }
        this.performAudit(
                ATNAtxn,
                request,
                null,
                XATNALogger.OutcomeIndicator.SUCCESS,
                XATNALogger.ActorType.REGISTRY);
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
     */
    private void processRequestWithPatientId(OMElement request, OMElement queryRequest, OMElement responseOption) throws XdsInternalException {
        if (this.getGatewayType() == GatewayType.RespondingGateway) {
            // RespondingGateway steps ...
            // Just go local.
            XConfigActor registry = this.getLocalRegistry();
            this.addRequest(queryRequest, responseOption, registry.getName(), registry, true);
            return;  // Early exit!
        }

        // InitiatingGateway steps ...

        // At this point, we know the following:
        //   We need to extract the patientId.
        //   A homeCommunityId is not present.
        // Send requests based upon patient identifier.
        String pidCXFormatted = this.getPatientId(request, queryRequest);
        if (pidCXFormatted != null) {
            this.logInfo("Patient ID", pidCXFormatted);
            InitiatingGatewayMode mode = this.getInitiatingGatewayMode();
            switch (mode) {
                case XCPD:
                    this.logInfo("PatientMappingMode", "XCPD");
                    this.processRequestWithPatientIdUsingXCPDMode(pidCXFormatted, queryRequest, responseOption);
                    break;
                case PassThrough:
                default:
                    this.logInfo("PatientMappingMode", "PassThrough");
                    this.processRequestWithPatientIdUsingPathThroughMode(pidCXFormatted, queryRequest, responseOption);
                    break;
            }
            // this.processRequestWithPatientId(patientId, queryRequest, responseOption);
            // this.processRequestWithPatientIdUsingCorrelationCacheMode(patientId, queryRequest, responseOption);
        } else {
            /* --- Go silent here according to XCA spec --- */
            this.logError("[* Not notifying client *]: Can not find Patient ID on request");
        }
    }

    /**
     * 
     * @return
     * @throws XdsInternalException
     */
    private InitiatingGatewayMode getInitiatingGatewayMode() throws XdsInternalException {
        InitiatingGatewayMode mode = InitiatingGatewayMode.PassThrough; // Default.

        // Get gateway configuration
        XConfigActor gateway = this.getGatewayConfig();

        String XCAInitiatingGatewayPatientMappingMode = gateway.getProperty("XCAInitiatingGatewayPatientMappingMode");
        if (XCAInitiatingGatewayPatientMappingMode == null) {
            logger.warn("XCA Initiating Gateway - using default 'passthrough' mode");
        } else {
            if (XCAInitiatingGatewayPatientMappingMode.equalsIgnoreCase("passthrough")) {
                mode = InitiatingGatewayMode.PassThrough;
                logger.info("XCA Initiating Gateway - using 'passthrough' mode");
            } else if (XCAInitiatingGatewayPatientMappingMode.equalsIgnoreCase("xcpd")) {
                mode = InitiatingGatewayMode.XCPD;
                logger.info("XCA Initiating Gateway - using 'xcpd' mode");
            } else {
                logger.warn("XCA Initiating Gateway - using default 'passthrough' mode");
            }
        }
        return mode;
    }

    /**
     * 
     * @param pidCXFormatted
     * @param queryRequest
     * @param responseOption
     * @throws XdsInternalException
     */
    private void processRequestWithPatientIdUsingXCPDMode(String pidCXFormatted, OMElement queryRequest, OMElement responseOption) throws XdsInternalException {
        List<XCAGatewayConfig> gatewayConfigs = this.getRespondingGatewaysForPatientIdUsingXCPDMode(pidCXFormatted);
        for (XCAGatewayConfig gatewayConfig : gatewayConfigs) {
            OMElement gatewayQueryRequest = this.getTargetGatewayQueryRequest(queryRequest, gatewayConfig.getPatientId());
            this.addRequest(gatewayQueryRequest,
                    responseOption,
                    gatewayConfig.getHomeCommunityId(),
                    gatewayConfig.getConfig(),
                    false);
        }
        // FIXME: Should we always go local in this case?
        XConfigActor registry = this.getLocalRegistry();
        this.addRequest(queryRequest, responseOption, registry.getName(), registry, true);
    }

    /**
     *
     * @param pidCXFormatted
     * @param queryRequest
     * @param responseOption
     * @throws XdsInternalException
     */
    private void processRequestWithPatientIdUsingPathThroughMode(String pidCXFormatted, OMElement queryRequest, OMElement responseOption) throws XdsInternalException {
        // Now get the Assigning Authority within the patient ID.
        String assigningAuthority = this.getAssigningAuthority(pidCXFormatted);
        if (assigningAuthority == null) {
            /* --- Go silent here according to XCA spec --- */
            this.logError("[* Not notifying client *]: Could not parse assigning authority from patient id = " + pidCXFormatted);
            return;  // Early exit!
        }
        XConfig xconfig = XConfig.getInstance();
        // Get the configuration for the assigning authority.
        XConfigObject aa = xconfig.getAssigningAuthorityConfigById(assigningAuthority);
        if (aa == null) {
            /* --- Go silent here according to XCA spec --- */
            this.logError("[* Not notifying client *]: Could not find assigning authority configuration for patient id = " + pidCXFormatted);
        } else {
            // Ok.  Now we should hopefully be good to go.

            // Add all remote gateways that can resolve patients within the assigning authority.
            List<XConfigActor> gateways = xconfig.getRespondingGatewayConfigsForAssigningAuthorityId(assigningAuthority);
            for (XConfigActor gateway : gateways) {
                this.addRequest(queryRequest, responseOption, gateway.getUniqueId(), gateway, false);
            }

            // Now, we may also need to go to a local registry.

            // Does the assigning authority configuration include a local registry?
            XConfigActor registry = xconfig.getRegistryConfigForAssigningAuthorityId(assigningAuthority);
            if (registry != null) {
                //String homeCommunityId = xconfig.getHomeCommunity().getHomeCommunityId();
                // Just use the registry name as the key (to avoid conflict with
                // local homeCommunityId testing).
                this.addRequest(queryRequest, responseOption, registry.getName(), registry, true);
            }
        }
    }

    /**
     * 
     * @param pidCXFormatted
     * @return
     * @throws XdsInternalException
     */
    private List<XCAGatewayConfig> getRespondingGatewaysForPatientIdUsingXCPDMode(String pidCXFormatted) throws XdsInternalException {
        String localHomeCommunityId = this.getLocalHomeCommunityId();
        String localPatientId = pidCXFormatted;

        XConfig xconf = XConfig.getInstance();
        List<XCAGatewayConfig> gatewayConfigs = new ArrayList<XCAGatewayConfig>();

        // Build subject search criteria (for PIX query).
        SubjectSearchCriteria subjectSearchCriteria = new SubjectSearchCriteria();
        Subject subject = new Subject();
        SubjectIdentifier subjectIdentifier = new SubjectIdentifier(localPatientId);
        subject.addSubjectIdentifier(subjectIdentifier);
        subjectSearchCriteria.setSubject(subject);

        HashSet remoteHomeCommunityIds = new HashSet<String>();
        try {
            // Get sender / receiver device info.
            DeviceInfo senderDeviceInfo = this.getSenderDeviceInfo();
            DeviceInfo receiverDeviceInfo = this.getDeviceInfo(this.getXCPDInitiatingGatewayConfig());

            // Prepare for PIX Query.
            XConfigActor xcpdIGConfig = this.getXCPDInitiatingGatewayConfig();
            PIXManagerClient pixClient = new PIXManagerClient(xcpdIGConfig);

            // Issue PIX Query.
            SubjectSearchResponse subjectSearchResponse = pixClient.getIdentifiersQuery(senderDeviceInfo, receiverDeviceInfo, subjectSearchCriteria);

            // Loop through matching subjects.
            List<Subject> matchSubjects = subjectSearchResponse.getSubjects();
            for (Subject matchSubject : matchSubjects) {

                // Get remote home community id for subject.
                Custodian custodian = matchSubject.getCustodian();
                String remoteHomeCommunityId = "urn:oid:" + custodian.getCustodianId();

                // Check to see if we already processed a patient id for this community.
                if (remoteHomeCommunityIds.contains(remoteHomeCommunityId)) {
                    continue;  // FIXME: Go to next matched subject.
                } else {
                    remoteHomeCommunityIds.add(remoteHomeCommunityId);
                }
                // Now get remote home patient ids (support > 1) and put into result list.
                // See FIXME above and below.
                for (SubjectIdentifier matchSubjectIdentifier : matchSubject.getSubjectIdentifiers()) {
                    String remotePatientId = matchSubjectIdentifier.getCXFormatted();
                    this.logInfo("Patient Correlation",
                            "localHomeCommunityId=" + localHomeCommunityId +
                            ", localPatientId=" + localPatientId +
                            ", remoteHomeCommunityId=" + remoteHomeCommunityId +
                            ", remotePatientId=" + remotePatientId);

                    // Save the remote gateway configuration in result list.
                    XConfigActor config = xconf.getXConfigActorById(remoteHomeCommunityId, XConfig.XCA_RESPONDING_GATEWAY_TYPE);
                    XCAGatewayConfig gatewayConfig = new XCAGatewayConfig(config);
                    gatewayConfig.setPatientId(remotePatientId);
                    gatewayConfigs.add(gatewayConfig);

                    break;  // FIXME: Should we be able to deal with > 1 pid for remote community?
                }
            }
        } catch (Exception ex) {
            this.logError("EXCEPTION: Unable to perform PIX query: " + ex.getMessage());
        }
        return gatewayConfigs;
    }

    /**
     *
     * @return
     * @throws AxisFault
     */
    private synchronized XConfigActor getXCPDInitiatingGatewayConfig() throws XdsInternalException {
        if (_xcpdIGConfig != null) {
            return _xcpdIGConfig;
        }
        XConfigObject gatewayConfig = this.getGatewayConfig();

        // Now get the "XCPD IG" object (and cache it away).
        _xcpdIGConfig = (XConfigActor) gatewayConfig.getXConfigObjectWithName("xcpdig", XConfig.XCA_INITIATING_GATEWAY_TYPE);
        return _xcpdIGConfig;
    }

    /**
     *
     * @param patientId
     * @return
     * @throws XdsInternalException
     */
    /* MOVE ...
    private List<XCAGatewayConfig> getRespondingGatewaysForPatientId(String patientId) throws XdsInternalException {
    XConfig xconf = XConfig.getInstance();
    List<XCAGatewayConfig> gatewayConfigs = new ArrayList<XCAGatewayConfig>();
    // First extract correlations.
    PatientCorrelationService patientCorrelationService = new PatientCorrelationService();
    try {
    String localHomeCommunityId = this.getLocalHomeCommunityId();
    List<PatientCorrelation> patientCorrelations = patientCorrelationService.lookup(patientId, localHomeCommunityId);
    if (patientCorrelations.size() == 0) {
    this.logInfo("Patient Correlation",
    "No correlations found for: " +
    "localHomeCommunityId=" + localHomeCommunityId +
    ", localPatientId=" + patientId);
    }
    for (PatientCorrelation patientCorrelation : patientCorrelations) {
    String remoteHomeCommunityId = patientCorrelation.getRemoteHomeCommunityId();
    String remotePatientId = patientCorrelation.getRemotePatientId();
    this.logInfo("Patient Correlation",
    "localHomeCommunityId=" + localHomeCommunityId +
    ", localPatientId=" + patientId +
    ", remoteHomeCommunityId=" + remoteHomeCommunityId +
    ", remotePatientId=" + remotePatientId);
    XConfigActor config = xconf.getXConfigActorById(remoteHomeCommunityId, XConfig.XCA_RESPONDING_GATEWAY_TYPE);
    XCAGatewayConfig gatewayConfig = new XCAGatewayConfig(config);
    gatewayConfig.setPatientId(remotePatientId);
    gatewayConfigs.add(gatewayConfig);
    }
    } catch (PatientCorrelationException ex) {
    // FIXME: Do something
    this.logError("Could not get correlations: " + ex.getMessage());
    }
    return gatewayConfigs;
    } */
    /**
     *
     * @param queryRequest
     * @param patientId
     * @return
     */
    private OMElement getTargetGatewayQueryRequest(OMElement queryRequest, String patientId) {
        // Assumes that this is a request that requires a patient id.

        // Clone request first.
        OMElement newQueryRequest = queryRequest.cloneOMElement();

        // Locate "AdhocQuery" on request.
        /*OMElement adhocQuery;
        try {
        adhocQuery = XPathHelper.selectSingleNode(
        newQueryRequest, "./ns:AdhocQueryRequest/ns:AdhocQuery",
        "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
        if (adhocQuery == null) {
        // FIXME: DO something.
        return newQueryRequest;  // Early exit!
        }
        } catch (XPathHelperException ex) {
        // FIXME: Do something.
        return newQueryRequest;  // Early exit!
        }*/

        // Get queryId.
        String queryId = this.getStoredQueryId(newQueryRequest);
        if (queryId == null) {
            return null;  // Early exit (FIXME).
        }

        // Look for slot to replace.
        String slotName = null;
        if (queryId.equals(MetadataSupport.SQ_FindDocuments)) {
            slotName = "$XDSDocumentEntryPatientId";
        } else if (queryId.equals(MetadataSupport.SQ_FindFolders)) {
            slotName = "$XDSFolderPatientId";
        } else if (queryId.equals(MetadataSupport.SQ_FindSubmissionSets)) {
            slotName = "$XDSSubmissionSetPatientId";
        } else if (queryId.equals(MetadataSupport.SQ_GetAll)) {
            // FIXME: NOT IMPLEMENTED [NEED TO FIGURE OUT WHAT TO PULL OUT HERE.
        }
        if (slotName != null) {
            // Replace the patientId slot value.
            //Metadata m = MetadataParser.parseNonSubmission(newQueryRequest);
            this.setSlotValue(newQueryRequest, slotName, 0, "'" + patientId + "'");
        } else {
            // FIXME: Do something.
        }
        return newQueryRequest;
    }

    /**
     * FIXME (BHT): This does not belong here .... but was unable to work with Metadata class to
     * get this done ... Also, could use XPathHelper to streamline ...
     *
     * @param obj
     * @param slotName
     * @param valueIndex
     * @param value
     */
    private void setSlotValue(OMElement obj, String slotName, int valueIndex, String value) {
        for (OMElement slot : MetadataSupport.childrenWithLocalName(obj, "Slot")) {
            String name = slot.getAttributeValue(MetadataSupport.slot_name_qname);
            if (name.equals(slotName)) {
                OMElement valueListNode = MetadataSupport.firstChildWithLocalName(slot, "ValueList");
                if (valueListNode == null) {
                    // FIXME ... should not happen.
                    break;
                }
                int valueCount = 0;
                for (OMElement valueNode : MetadataSupport.childrenWithLocalName(valueListNode, "Value")) {
                    if (valueCount == valueIndex) {
                        valueNode.setText(value);
                        return;  // Early exit: Get out now!
                    } else {
                        ++valueCount;
                    }
                }
            }
        }
    }

    /**
     *
     * @param patientId
     * @return
     */
    private String getAssigningAuthority(String patientId) {
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
    private String getPatientId(OMElement request, OMElement queryRequest) throws XdsInternalException {
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
     * @param queryRequest
     * @param responseOption
     * @param homeCommunityId
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void processTargetedHomeRequest(OMElement queryRequest, OMElement responseOption, String homeCommunityId) throws XdsInternalException {
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
        } else if (this.getGatewayType() == GatewayType.InitiatingGateway) {  // Going remote.
            this.logInfo("Note", "Going remote for homeCommunityId: " + homeCommunityId);
            // See if we know about a remote gateway that can respond.
            XConfigActor gatewayConfig = XConfig.getInstance().getRespondingGatewayConfigForHomeCommunityId(homeCommunityId);
            if (gatewayConfig == null) {
                response.add_error(MetadataSupport.XDSUnknownCommunity,
                        "Do not understand homeCommunityId " + homeCommunityId,
                        this.getLocalHomeCommunityId(), log_message);
            } else {
                // This request is good (targeted for a remote community.
                this.addRequest(queryRequest, responseOption, homeCommunityId, gatewayConfig, false);
            }
        } else {
            response.add_error(MetadataSupport.XDSUnknownCommunity,
                    "Do not understand homeCommunityId " + homeCommunityId,
                    this.getLocalHomeCommunityId(), log_message);
        }
    }

    /**
     * 
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private XConfigActor getLocalRegistry() throws XdsInternalException {
        // Get the gateway configuration.
        XConfig xconfig = XConfig.getInstance();
        XConfigObject homeCommunity = xconfig.getHomeCommunityConfig();

        // Return the proper registry configuration based upon the gateway configuration.
        XConfigActor gateway;
        if (this.getGatewayType() == GatewayType.InitiatingGateway) {
            gateway = (XConfigActor) homeCommunity.getXConfigObjectWithName("ig", XConfig.XCA_INITIATING_GATEWAY_TYPE);
        } else {
            gateway = (XConfigActor) homeCommunity.getXConfigObjectWithName("rg", XConfig.XCA_RESPONDING_GATEWAY_TYPE);
        }

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
    private boolean requiresHomeCommunityId(OMElement queryRequest) {
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
    private String getStoredQueryId(OMElement queryRequest) {
        return queryRequest.getAttributeValue(MetadataSupport.id_qname);
    }

    /**
     * Return home community id on request.  Return null if not present.
     *
     * @param queryRequest - <AdhocQuery> XML node
     * @return homeCommunitId string if present, otherwise null.
     */
    private String getHomeCommunityId(OMElement queryRequest) {
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
    private XCAQueryRequest addRequest(OMElement queryRequest, OMElement responseOption, String uniqueId, XConfigActor configActor, boolean isLocalRequest) {
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
