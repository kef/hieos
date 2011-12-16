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

import com.vangent.hieos.hl7v3util.client.PIXManagerClient;
import com.vangent.hieos.hl7v3util.model.subject.Custodian;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEventHelper;
import com.vangent.hieos.xutil.atna.ATNAAuditEventQuery;

import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCAIGAdhocQueryRequest extends XCAAdhocQueryRequest {

    private final static Logger logger = Logger.getLogger(XCAIGAdhocQueryRequest.class);

    public enum InitiatingGatewayMode {

        PassThrough, XCPD
    };
    private static XConfigActor _xcpdIGConfig = null;

    /**
     *
     * @param log_message
     * @param messageContext
     */
    public XCAIGAdhocQueryRequest(XLogMessage log_message, MessageContext messageContext) {
        super(log_message, messageContext);
    }

    /**
     *
     * @param request
     */
    @Override
    protected void validateRequest(OMElement request) {
        super.validateRequest(request);

        // Perform ATNA audit (FIXME - may not be best place).
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getATNAAuditEventRegistryStoredQuery(request);
                auditEvent.setActorType(ATNAAuditEvent.ActorType.INITIATING_GATEWAY);
                auditEvent.setTransaction(ATNAAuditEvent.IHETransaction.ITI18);
                auditEvent.setAuditEventType(ATNAAuditEvent.AuditEventType.QUERY_PROVIDER);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            // FIXME?:
        }
    }

    /**
     *
     * @param queryRequest
     * @param responseOption
     */
    @Override
    protected void processRequestWithPatientId(OMElement request, OMElement queryRequest, OMElement responseOption) throws XdsInternalException {
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
        } else {
            /* --- Go silent here according to XCA spec --- */
            this.logError("[* Not notifying client *]: Can not find Patient ID on request");
        }
    }

    /**
     *
     * @param queryRequest
     * @param responseOption
     * @param homeCommunityId
     * @param gatewayConfig
     * @throws XdsInternalException
     */
    @Override
    protected void processRemoteCommunityRequest(OMElement queryRequest, OMElement responseOption, String homeCommunityId, XConfigActor gatewayConfig) throws XdsInternalException {
        this.addRequest(queryRequest, responseOption, homeCommunityId, gatewayConfig, false);
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
                // Just use the registry name as the key (to avoid conflict with
                // local homeCommunityId testing).
                this.addRequest(queryRequest, responseOption, registry.getName(), registry, true);
            }
        }
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

        HashSet<String> remoteHomeCommunityIds = new HashSet<String>();
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
                            "localHomeCommunityId=" + localHomeCommunityId
                            + ", localPatientId=" + localPatientId
                            + ", remoteHomeCommunityId=" + remoteHomeCommunityId
                            + ", remotePatientId=" + remotePatientId);

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
     * @return
     * @throws XdsInternalException
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
}
