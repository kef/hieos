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
package com.vangent.hieos.services.xcpd.gateway.transactions;

import com.vangent.hieos.hl7v3util.client.XCPDGatewayClient;
import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.message.HL7V3MessageBuilderHelper;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message_Builder;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201301UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;
import com.vangent.hieos.hl7v3util.model.subject.SubjectBuilder;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteriaBuilder;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.services.xcpd.gateway.exception.XCPDException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCPDInitiatingGatewayRequestHandler extends XCPDGatewayRequestHandler {

    private final static Logger logger = Logger.getLogger(XCPDInitiatingGatewayRequestHandler.class);
    private static XConfigActor _pdsConfig = null;
    private static XConfigActor _pixConfig = null;
    private static List<XConfigActor> _xcpdRespondingGateways = null;
    private static ExecutorService _executor = null;  // Only one of these shared across all web requests.

    /**
     *
     * @param log_message
     * @param gatewayType
     */
    public XCPDInitiatingGatewayRequestHandler(XLogMessage log_message, XCPDGatewayRequestHandler.GatewayType gatewayType) {
        super(log_message, gatewayType);
    }

    /**
     *
     * @param request
     * @param messageType
     * @return
     */
    public OMElement run(OMElement request, MessageType messageType) throws AxisFault {
        HL7V3Message result = null;
        switch (messageType) {
            case CrossGatewayPatientDiscovery:
                result = this.processCrossGatewayPatientDiscovery(new PRPA_IN201305UV02_Message(request));
                break;
            case PatientRegistryRecordAdded:
                result = this.processPatientRegistryRecordAdded(new PRPA_IN201301UV02_Message(request));
                break;
        }
        if (result != null) {
            log_message.addOtherParam("Response", result.getMessageNode());
        }
        log_message.setPass(true);  // PASS!
        return (result != null) ? result.getMessageNode() : null;
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return
     */
    private PRPA_IN201306UV02_Message processCrossGatewayPatientDiscovery(PRPA_IN201305UV02_Message request) throws AxisFault {
        this.validateHL7V3Message(request);
        PRPA_IN201306UV02_Message result = null;
        String errorText = null;
        try {
            SubjectSearchCriteria subjectSearchCriteria = this.getSubjectSearchCriteria(request);
            this.validateRequest(subjectSearchCriteria);

            // TBD: Should we see if we know about this patient first?
            //PRPA_IN201306UV02_Message queryResponse = this.findCandidatesQuery(request);

            // TBD: Supplement subjectSearchCriteria with this gateway's specifics.
            result = this.performCrossGatewayPatientDiscovery(request, subjectSearchCriteria);

        } catch (XCPDException ex) {
            errorText = ex.getMessage();
            log_message.setPass(false);
            log_message.addErrorParam("EXCEPTION", errorText);
        }
        if (errorText != null) {
            result = this.getCrossGatewayPatientDiscoveryResponse(request, null /* subjects */, errorText);
        }
        this.validateHL7V3Message(result);
        return result;
    }

    /**
     *
     * @param PRPA_IN201301UV02_Message
     * @return
     */
    private MCCI_IN000002UV01_Message processPatientRegistryRecordAdded(PRPA_IN201301UV02_Message request) throws AxisFault {
        // FIXME: This is really not implemented yet!
        this.validateHL7V3Message(request);
        String errorText = null;
        try {
            SubjectBuilder builder = new SubjectBuilder();
            Subject subject = builder.buildSubjectFromPRPA_IN201301UV02_Message(request);
            if (subject.getSubjectIdentifiers().size() == 0) {
                errorText = "1 subject identifier must be specified";
            } else if (subject.getSubjectIdentifiers().size() > 1) {
                errorText = "Only 1 subject identifier must be specified";
            }
            // Create
            /*if (errorText == null) {
            PIXManagerClient pixClient = new PIXManagerClient(this.getPIXConfig());
            OMElement PRPA_IN201310UV02_Message = pixClient.getIdentifiersQuery(PRPA_IN201309UV02_Message);
            }*/
            //EMPIAdapter adapter = EMPIFactory.getInstance();
            //Subject subjectAdded = adapter.addSubject(subject);
        } catch (Exception ex) {
            errorText = ex.getMessage();
            log_message.setPass(false);
            log_message.addErrorParam("EXCEPTION", errorText);
        }
        MCCI_IN000002UV01_Message ackResponse = this.getPatientRegistryRecordAddedResponse(request, errorText);
        this.validateHL7V3Message(ackResponse);
        if (errorText != null) {
            log_message.setPass(true);
        }
        return ackResponse;
    }

    /**
     * 
     * @param request
     * @return
     * @throws XCPDException
     */
    private SubjectSearchCriteria getSubjectSearchCriteria(PRPA_IN201305UV02_Message request) throws XCPDException {
        // First build search criteria from inbound request.
        SubjectSearchCriteriaBuilder subjectSearchCriteriaBuilder = new SubjectSearchCriteriaBuilder();
        SubjectSearchCriteria subjectSearchCriteria;
        try {
            subjectSearchCriteria = subjectSearchCriteriaBuilder.buildSubjectSearchCriteriaFromPRPA_IN201305UV02_Message(request);
            String communityPatientIdAssigningAuthority = this.getGatewayConfigProperty("CommunityPatientIdAssigningAuthority");
            subjectSearchCriteria.setCommunityPatientIdAssigningAuthority(communityPatientIdAssigningAuthority);

        } catch (ModelBuilderException ex) {
            throw new XCPDException(ex.getMessage());
        }
        return subjectSearchCriteria;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @throws XCPDException
     */
    private void validateRequest(SubjectSearchCriteria subjectSearchCriteria) throws XCPDException {
        String communityPatientIdAssigningAuthority = subjectSearchCriteria.getCommunityPatientIdAssigningAuthority();

        // Validate request:
        Subject subject = subjectSearchCriteria.getSubject();

        // Make sure that at least one subject identifier is for the
        // designated "CommunityPatientIdAssigningAuthority"
        boolean foundCommunityPatientIdAssigningAuthority = false;
        for (SubjectIdentifier subjectIdentifier : subject.getSubjectIdentifiers()) {
            SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
            if (subjectIdentifierDomain.getUniversalId().equals(communityPatientIdAssigningAuthority)) {
                foundCommunityPatientIdAssigningAuthority = true;
                break;
            }
        }

        if (foundCommunityPatientIdAssigningAuthority == false) {
            // Did not find an appropriate patient on the request.
            throw new XCPDException(
                    "You must specify at least one LivingSubjectId for the " +
                    communityPatientIdAssigningAuthority + " assigning authority");
        }

        // Check required fields (Subject + BirthTime).
        if (subject.getSubjectNames().size() == 0) {
            throw new XCPDException("LivingSubjectName required");
        }

        if (subject.getBirthTime() == null) {
            throw new XCPDException("LivingSubjectBirthTime required");
        }
    }

    /**
     * 
     * @param request
     * @param subjectSearchCriteria
     * @return
     */
    private PRPA_IN201306UV02_Message performCrossGatewayPatientDiscovery(PRPA_IN201305UV02_Message request, SubjectSearchCriteria subjectSearchCriteria) {
        // Prepare gateway requests.
        List<GatewayRequest> gatewayRequests = this.getGatewayRequests(subjectSearchCriteria);

        // Issue XCPD CrossGatewayPatientDiscovery requests to targeted gateways (in parallel).
        List<GatewayResponse> gatewayResponses = this.sendGatewayRequests(gatewayRequests);

        // Process responses.
        PRPA_IN201306UV02_Message result = this.processResponses(request, gatewayResponses);
        return result;
    }

    /**
     *
     * @param PRPA_IN201301UV02_Message
     * @param errorText
     * @return
     */
    private MCCI_IN000002UV01_Message getPatientRegistryRecordAddedResponse(PRPA_IN201301UV02_Message request, String errorText) {
        DeviceInfo senderDeviceInfo = this.getDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        MCCI_IN000002UV01_Message_Builder ackBuilder = new MCCI_IN000002UV01_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        MCCI_IN000002UV01_Message ackResponse = ackBuilder.buildMCCI_IN000002UV01(request, errorText);
        return ackResponse;
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @param subjects
     * @param errorText
     * @return
     */
    private PRPA_IN201306UV02_Message getCrossGatewayPatientDiscoveryResponse(PRPA_IN201305UV02_Message request, List<Subject> subjects, String errorText) {
        DeviceInfo senderDeviceInfo = this.getDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        PRPA_IN201306UV02_Message_Builder builder =
                new PRPA_IN201306UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        return builder.buildPRPA_IN201306UV02_MessageFromSubjects(request, subjects, errorText);
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws AxisFault
     */
    private List<GatewayRequest> getGatewayRequests(SubjectSearchCriteria subjectSearchCriteria) {
        List<GatewayRequest> requests = new ArrayList<GatewayRequest>();
        // First get list of target XCPD Responding Gateways.
        List<XConfigActor> rgConfigs = this.getXCPDRespondingGateways();

        // Now prepare gateway requests.
        for (XConfigActor rgConfig : rgConfigs) {
            // Prepare Gateway request.
            GatewayRequest gatewayRequest = new GatewayRequest();
            gatewayRequest.setRGConfig(rgConfig);
            gatewayRequest.setSubjectSearchCriteria(subjectSearchCriteria);

            DeviceInfo senderDeviceInfo = this.getDeviceInfo();
            DeviceInfo receiverDeviceInfo = this.getReceiverDeviceInfo(rgConfig);
            // See if the target RespondingGateway can handle receipt of our community's
            // patientid as a LivingSubjectId parameter.
            boolean sendCommunityPatientId = rgConfig.getPropertyAsBoolean("SendCommunityPatientId", true);

            PRPA_IN201305UV02_Message_Builder builder =
                    new PRPA_IN201305UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
            PRPA_IN201305UV02_Message cgpdRequest = builder.getPRPA_IN201305UV02_Message(
                    subjectSearchCriteria, sendCommunityPatientId);

            gatewayRequest.setRequest(cgpdRequest);
            requests.add(gatewayRequest);
        }
        return requests;
    }

    /**
     *
     * @param rgConfig
     * @return
     */
    private DeviceInfo getReceiverDeviceInfo(XConfigActor rgConfig) {
        String deviceId = rgConfig.getProperty("DeviceId");
        String deviceName = rgConfig.getProperty("DeviceName");
        String homeCommunityId = rgConfig.getUniqueId();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setId(deviceId);
        deviceInfo.setName(deviceName);
        deviceInfo.setHomeCommunityId(homeCommunityId);
        return deviceInfo;
    }

    /**
     *
     * @param requests
     * @return
     */
    private List<GatewayResponse> sendGatewayRequests(List<GatewayRequest> requests) {
        ArrayList<GatewayResponse> responses = new ArrayList<GatewayResponse>();

        boolean XCPDMultiThread = true;  // FIXME: Place into XConfig.
        boolean multiThreadMode = false;
        ArrayList<Future<GatewayResponse>> futures = null;
        int taskSize = requests.size();
        if (XCPDMultiThread == true && taskSize > 1) {  // FIXME: Task bound size should be configurable.
            // Do multi-threading.
            multiThreadMode = true;
            futures = new ArrayList<Future<GatewayResponse>>();
        }
        logger.debug("*** multiThreadMode = " + multiThreadMode + " ***");

        // Submit work to be conducted in parallel (if required):
        for (GatewayRequest request : requests) {
            // Each pass is for a single entity (Responding Gateway).
            GatewayCallable callable = new GatewayCallable(request);
            if (multiThreadMode == true) {
                Future<GatewayResponse> future = this.submit(callable);
                futures.add(future);
            } else {
                // Not in multi-thread mode.
                // Just call in the current thread.
                try {
                    GatewayResponse gatewayResponse = callable.call();
                    if (gatewayResponse != null) {
                        responses.add(gatewayResponse);
                    }
                } catch (Exception ex) {
                    // Ignore here, already logged.
                    // Do not remove this exception logic!!!!!!!!
                    // Should never happen, but don't want to stop progress ...
                    //logger.error("XCPD EXCEPTION ... continuing", ex);
                }
            }
        }

        // If in mult-thread mode, wait for futures ...
        if (multiThreadMode == true) {
            for (Future<GatewayResponse> future : futures) {
                try {
                    GatewayResponse gatewayResponse = future.get();  // Will block until ready.
                    responses.add(gatewayResponse);
                    //logger.debug("*** FINISHED THREAD - " + requestCollection.getUniqueId());
                    //this.processOutboundRequestResult(requestCollection, results);
                } catch (InterruptedException ex) {
                    logger.error("XCPD EXCEPTION ... continuing", ex);
                } catch (ExecutionException ex) {
                    logger.error("XCPD EXCEPTION ... continuing", ex);
                }
            }
        }
        return responses;
    }

    /**
     *
     * @param request
     * @param responses
     * @return
     */
    private PRPA_IN201306UV02_Message processResponses(PRPA_IN201305UV02_Message request, List<GatewayResponse> responses) {
        List<Subject> subjects = new ArrayList<Subject>();

        // Go through reach response.
        for (GatewayResponse gatewayResponse : responses) {
            List<Subject> gatewayResponseSubjects = this.processResponse(gatewayResponse);
            if (gatewayResponseSubjects.size() > 0) {
                subjects.addAll(gatewayResponseSubjects);
            }
        }

        PRPA_IN201306UV02_Message aggregatedResponse = this.getCrossGatewayPatientDiscoveryResponse(request, subjects, null /* erroText */);
        return aggregatedResponse;
    }

    /**
     * 
     * @param gatewayResponse
     */
    private List<Subject> processResponse(GatewayResponse gatewayResponse) {
        List<Subject> subjects = new ArrayList<Subject>();
        PRPA_IN201306UV02_Message cgpdResponse = gatewayResponse.getResponse();
        try {
            this.validateHL7V3Message(cgpdResponse);
        } catch (AxisFault ex) {
            // TBD ...
            log_message.addErrorParam("EXCEPTION: " + gatewayResponse.getRequest().getVitals(), ex.getMessage());
            logger.error("CGPD Response did not validate against XML schema: " + ex.getMessage());
            return subjects;
        }
        try {
            SubjectBuilder subjectBuilder = new SubjectBuilder();
            subjects = subjectBuilder.buildSubjectsFromPRPA_IN201306UV02_Message(cgpdResponse);

        } catch (ModelBuilderException ex) {
            // TBD ....
        }
        return subjects;
    }

    /**
     *
     * @param request
     * @return
     */
    // SYNCHRONIZED!
    private synchronized Future<GatewayResponse> submit(GatewayCallable request) {
        ExecutorService exec = this.getExecutor();
        Future<GatewayResponse> future = exec.submit(request);
        return future;
    }

    /**
     *
     * @return
     */
    private ExecutorService getExecutor() {
        if (_executor == null) {
            // Shared across all web service requests.
            _executor = Executors.newCachedThreadPool();
        }
        return _executor;
    }

    /**
     *
     * @return
     * @throws AxisFault
     */
    @Override
    protected synchronized XConfigActor getPDSConfig() throws AxisFault {
        if (_pdsConfig != null) {
            return _pdsConfig;
        }
        XConfigObject gatewayConfig = this.getGatewayConfig();

        // Now get the "PDS" object (and cache it away).
        _pdsConfig = (XConfigActor) gatewayConfig.getXConfigObjectWithName("pds", XConfig.PDS_TYPE);
        return _pdsConfig;

    }

    /**
     *
     * @return
     * @throws AxisFault
     */
    private synchronized XConfigActor getPIXConfig() throws AxisFault {
        if (_pixConfig != null) {
            return _pixConfig;
        }
        XConfigObject gatewayConfig = this.getGatewayConfig();

        // Now get the "PDS" object (and cache it away).
        _pixConfig = (XConfigActor) gatewayConfig.getXConfigObjectWithName("pix", XConfig.PIX_MANAGER_TYPE);
        return _pixConfig;

    }

    /**
     * 
     * @return
     * @throws AxisFault
     */
    private synchronized List<XConfigActor> getXCPDRespondingGateways() {
        if (_xcpdRespondingGateways != null) {
            return _xcpdRespondingGateways;
        }
        // Get gateway configuration.
        XConfigObject gatewayConfig;
        try {
            gatewayConfig = this.getGatewayConfig();
        } catch (AxisFault ex) {
            logger.error("XCPD EXCEPTION: " + ex.getMessage());
            return new ArrayList<XConfigActor>();
        }

        // Get the list of XCPD Responding Gateways configuration.
        XConfigObject xcpdConfig = gatewayConfig.getXConfigObjectWithName("xcpd_rgs", "XCPDRespondingGatewaysType");
        _xcpdRespondingGateways = new ArrayList<XConfigActor>();
        if (xcpdConfig == null) {
            logger.warn("No target XCPD Responding Gateways configured.");
        } else {
            // Now navigate through the list of configurations and cache them.
            List<XConfigObject> rgConfigs = xcpdConfig.getXConfigObjectsWithType(XConfig.XCA_RESPONDING_GATEWAY_TYPE);
            if (rgConfigs.size() == 0) {
                logger.warn("No target XCPD Responding Gateways configured.");
            } else {
                for (XConfigObject rgConfig : rgConfigs) {
                    _xcpdRespondingGateways.add((XConfigActor) rgConfig);
                }
            }
        }
        return _xcpdRespondingGateways;
    }

    /**
     * 
     */
    public class GatewayRequest {

        private XConfigActor rgConfig;
        private PRPA_IN201305UV02_Message request;
        SubjectSearchCriteria subjectSearchCriteria;

        public XConfigActor getRGConfig() {
            return rgConfig;
        }

        public String getEndpoint() {
            XConfigTransaction txn = rgConfig.getTransaction("CrossGatewayPatientDiscovery");
            return txn.getEndpointURL();
        }

        public void setRGConfig(XConfigActor rgConfig) {
            this.rgConfig = rgConfig;
        }

        private PRPA_IN201305UV02_Message getRequest() {
            return request;
        }

        private void setRequest(PRPA_IN201305UV02_Message request) {
            this.request = request;
        }

        public SubjectSearchCriteria getSubjectSearchCriteria() {
            return subjectSearchCriteria;
        }

        public void setSubjectSearchCriteria(SubjectSearchCriteria subjectSearchCriteria) {
            this.subjectSearchCriteria = subjectSearchCriteria;
        }

        /**
         *
         * @return
         */
        public String getVitals() {
            return "(community: " + this.getRGConfig().getUniqueId() + ", endpoint: " + this.getEndpoint() + ")";
        }
    }

    /**
     *
     */
    public class GatewayResponse {

        private PRPA_IN201306UV02_Message response;
        private GatewayRequest request;

        private PRPA_IN201306UV02_Message getResponse() {
            return response;
        }

        private void setResponse(PRPA_IN201306UV02_Message response) {
            this.response = response;
        }

        private void setRequest(GatewayRequest request) {
            this.request = request;
        }

        private GatewayRequest getRequest() {
            return this.request;
        }
    }

    /**
     *
     */
    public class GatewayCallable implements Callable<GatewayResponse> {

        private GatewayRequest request;

        /**
         *
         * @param request
         */
        public GatewayCallable(GatewayRequest request) {
            this.request = request;
        }

        /**
         *
         * @return
         * @throws Exception
         */
        public GatewayResponse call() throws Exception {
            PRPA_IN201305UV02_Message message = this.request.getRequest();
            if (log_message.isLogEnabled()) {
                log_message.addOtherParam("CGPD REQUEST " + this.request.getVitals(),
                        message.getMessageNode().toString());
            }

            // Audit
            performATNAAudit(this.request.getRequest(),
                    this.request.getSubjectSearchCriteria(),
                    this.request.getEndpoint());


            // Make the call.
            XCPDGatewayClient client = new XCPDGatewayClient(request.getRGConfig());
            GatewayResponse gatewayResponse = null;
            PRPA_IN201306UV02_Message queryResponse;
            try {
                queryResponse = client.findCandidatesQuery(message);
                if (log_message.isLogEnabled()) {
                    if (queryResponse.getMessageNode() != null) {
                        log_message.addOtherParam("CGPD RESPONSE " + request.getVitals(),
                                queryResponse.getMessageNode().toString());
                    } else {
                        log_message.addErrorParam("CGPD RESPONSE " + request.getVitals(),
                                "NO RESPONSE FROM COMMUNITY");
                    }
                }
                gatewayResponse = new GatewayResponse();
                gatewayResponse.setRequest(this.request);
                gatewayResponse.setResponse(queryResponse);
            } catch (Exception ex) {
                 logger.error("XCPD EXCEPTION ... continuing " + request.getVitals(), ex);
                 log_message.addErrorParam("EXCEPTION " + request.getVitals(), ex.getMessage());
                 // ***** Rethrow is needed otherwise Axis2 gets confused with Async.
                 throw ex;  // Rethrow.
                //java.util.logging.Logger.getLogger(XCPDInitiatingGatewayRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return gatewayResponse;
        }
        /**
         * 
         * @return
         */
        //public String getTargetGatewayVitals() {
        //    return "(community: " + request.getRGConfig().getUniqueId() + ", endpoint: " + request.getEndpoint() + ")";
        //}
    }
}
