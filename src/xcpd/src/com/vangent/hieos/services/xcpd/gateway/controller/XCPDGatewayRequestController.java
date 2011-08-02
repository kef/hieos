/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xcpd.gateway.controller;

import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;
import com.vangent.hieos.hl7v3util.model.subject.Custodian;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectBuilder;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;

import com.vangent.hieos.services.xcpd.gateway.framework.XCPDGatewayRequestHandler;
import com.vangent.hieos.services.xcpd.gateway.exception.XCPDException;

import com.vangent.hieos.services.xcpd.patientcorrelationcache.exception.PatientCorrelationCacheException;
import com.vangent.hieos.services.xcpd.patientcorrelationcache.model.PatientCorrelationCacheEntry;
import com.vangent.hieos.services.xcpd.patientcorrelationcache.service.PatientCorrelationCacheService;
import com.vangent.hieos.xutil.exception.SOAPFaultException;

import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCPDGatewayRequestController {

    private final static Logger logger = Logger.getLogger(XCPDGatewayRequestController.class);

    // Result mode.
    public enum SearchResultMode {

        Identifiers,
        Demographics  // Used to get demographics from external gateways (since this are not cached).
    };
    // Statics.
    private static List<XConfigActor> _xcpdRespondingGateways = null;
    private static ExecutorService _executor = null;  // Only one of these shared across all web requests.
    private SearchResultMode searchResultMode = SearchResultMode.Identifiers;
    private XLogMessage logMessage;
    private XCPDGatewayRequestHandler requestHandler;
    private SubjectIdentifier communitySubjectIdentifier = null;
    private PatientCorrelationCacheService cacheService = null;
    private List<XConfigActor> targetRespondingGateways = new ArrayList<XConfigActor>();
    private List<PatientCorrelationCacheEntry> activeCacheEntries = new ArrayList<PatientCorrelationCacheEntry>();

    /**
     * 
     * @param logMessage
     */
    public XCPDGatewayRequestController(SearchResultMode searchResultMode, XCPDGatewayRequestHandler requestHandler, XLogMessage logMessage) {
        this.searchResultMode = searchResultMode;
        this.requestHandler = requestHandler;
        this.logMessage = logMessage;
        this.initCacheService();
    }

    /**
     *
     */
    private void initCacheService() {

        // Get configuration.
        logger.info("Setting MatchCacheExpirationDays from xconfig");
        String matchCacheExpirationDaysText = requestHandler.getGatewayConfigProperty("MatchCacheExpirationDays");
        int matchCacheExpirationDays = PatientCorrelationCacheService.DEFAULT_MATCH_EXPIRATION_DAYS;
        if (matchCacheExpirationDaysText != null) {
            matchCacheExpirationDays = new Integer(matchCacheExpirationDaysText);
        }
        logger.info("Setting MoMatchCacheExpirationDays from xconfig");
        String noMatchCacheExpirationDaysText = requestHandler.getGatewayConfigProperty("NoMatchCacheExpirationDays");
        int noMatchCacheExpirationDays = PatientCorrelationCacheService.DEFAULT_NO_MATCH_EXPIRATION_DAYS;
        if (noMatchCacheExpirationDaysText != null) {
            noMatchCacheExpirationDays = new Integer(noMatchCacheExpirationDaysText);
        }
        // Create cache service instance.
        this.cacheService = new PatientCorrelationCacheService(matchCacheExpirationDays, noMatchCacheExpirationDays);
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     */
    public void init(SubjectSearchCriteria subjectSearchCriteria) throws XCPDException {
        try {
            // Get subject identifier for the search subject (as it is known in this community).
            communitySubjectIdentifier = this.getCommunitySubjectIdentifier(subjectSearchCriteria);

            // Vitals.
            String localPatientId = communitySubjectIdentifier.getCXFormatted();
            String localHomeCommunityId = requestHandler.getGatewayConfig().getUniqueId();

            // First, get rid of any expired correlations for the localpid.
            cacheService.deleteExpired(localPatientId, localHomeCommunityId);

            // Get list of configured XCPD responding gateways.
            List<XConfigActor> respondingGateways = this.getXCPDRespondingGateways();

            if (searchResultMode == SearchResultMode.Demographics) {
                // Search all gateways in this mode.
                targetRespondingGateways = respondingGateways;

                // FIXME: Should we delete the entire cache for this localpid?
            } else {
                // Get list of cached correlations for the localpid.
                List<PatientCorrelationCacheEntry> cacheEntries = cacheService.lookup(localPatientId, localHomeCommunityId);

                // Gather list of XCPD responding gateways that are not in the cache ....
                int foundCount = 0;
                for (XConfigActor respondingGateway : respondingGateways) {
                    boolean foundGateway = false;

                    // This list will contain ACTIVE and NOT_FOUND entries.
                    for (PatientCorrelationCacheEntry cacheEntry : cacheEntries) {
                        if (cacheEntry.getRemoteHomeCommunityId().equals(respondingGateway.getUniqueId())) {
                            ++foundCount;
                            foundGateway = true;
                            break;
                        }
                    }
                    if (!foundGateway) {
                        // Keep track of which ones to search
                        targetRespondingGateways.add(respondingGateway);
                    }
                }
                // Now, create list of active cache entries (for later).
                for (PatientCorrelationCacheEntry cacheEntry : cacheEntries) {
                    if (cacheEntry.getStatus() == PatientCorrelationCacheEntry.STATUS_MATCH) {
                        this.activeCacheEntries.add(cacheEntry);
                    }
                }
            }
        } catch (Exception ex) {
            // Rethrow.
            throw new XCPDException(ex.getMessage());
        }
    }

    /**
     *
     * @return
     */
    public boolean isExternalSearchRequired() {
        return this.targetRespondingGateways.size() > 0;
    }

    /**
     *
     * @param patientDiscoverySearchCriteria
     * @return
     */
    public SubjectSearchResponse performCrossGatewayPatientDiscovery(
            SubjectSearchCriteria patientDiscoverySearchCriteria) {

        // Prepare gateway requests.
        List<GatewayRequest> gatewayRequests = this.getGatewayRequests(patientDiscoverySearchCriteria);

        // Issue XCPD CrossGatewayPatientDiscovery requests to targeted gateways (in parallel).
        List<GatewayResponse> gatewayResponses = this.sendRequests(gatewayRequests);

        // Process responses.
        return this.processResponses(gatewayResponses);
    }

    /**
     * 
     * @return
     */
    public SubjectSearchResponse getPatientDiscoverySearchResponse() {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        List<Subject> subjects = this.getCachedSubjects();
        subjectSearchResponse.setSubjects(subjects);
        return subjectSearchResponse;
    }

    /**
     * 
     * @return
     */
    private List<Subject> getCachedSubjects() {
        List<Subject> subjects = new ArrayList<Subject>();

        // Go through "active" cache entries.
        for (PatientCorrelationCacheEntry cacheEntry : this.activeCacheEntries) {

            // Create new subject for the cache entry.
            Subject subject = new Subject();
            subjects.add(subject);

            // Add the subject identifier.
            String remotePatientId = cacheEntry.getRemotePatientId();
            SubjectIdentifier subjectIdentifier = new SubjectIdentifier(remotePatientId);
            subject.addSubjectIdentifier(subjectIdentifier);

            // Create custodian.
            Custodian custodian = new Custodian();
            String remoteHomeCommunityId = cacheEntry.getRemoteHomeCommunityId();
            remoteHomeCommunityId = remoteHomeCommunityId.replace("urn:oid:", "");
            custodian.setCustodianId(remoteHomeCommunityId);
            subject.setCustodian(custodian);
        }
        return subjects;
    }

    /**
     *
     * @param patientDiscoverySearchCriteria
     * @return
     */
    private List<GatewayRequest> getGatewayRequests(SubjectSearchCriteria patientDiscoverySearchCriteria) {
        List<GatewayRequest> requests = new ArrayList<GatewayRequest>();

        // Prepare gateway requests (but only for targeted gateways).
        for (XConfigActor rgConfig : this.targetRespondingGateways) {
            // Prepare Gateway request.
            GatewayRequest gatewayRequest = new GatewayRequest();
            gatewayRequest.setRGConfig(rgConfig);
            gatewayRequest.setSubjectSearchCriteria(patientDiscoverySearchCriteria);

            DeviceInfo senderDeviceInfo = requestHandler.getSenderDeviceInfo();
            DeviceInfo receiverDeviceInfo = requestHandler.getDeviceInfo(rgConfig);
            // See if the target RespondingGateway can handle receipt of our community's
            // patientid as a LivingSubjectId parameter.
            // boolean sendCommunityPatientId = rgConfig.getPropertyAsBoolean("SendCommunityPatientId", true);

            PRPA_IN201305UV02_Message_Builder builder =
                    new PRPA_IN201305UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
            PRPA_IN201305UV02_Message cgpdRequest = builder.buildPRPA_IN201305UV02_Message(patientDiscoverySearchCriteria);

            gatewayRequest.setRequest(cgpdRequest);
            requests.add(gatewayRequest);
        }
        return requests;
    }

    /**
     *
     * @param requests
     * @return
     */
    private List<GatewayResponse> sendRequests(List<GatewayRequest> requests) {
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
            GatewayCallable callable = new GatewayCallable(requestHandler, request, logMessage);
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
     * @param responses
     * @return
     */
    private SubjectSearchResponse processResponses(List<GatewayResponse> responses) {
        // Get ready to aggregate all responses.
        SubjectSearchResponse aggregatedSubjectSearchResponse = new SubjectSearchResponse();

        // FIXME: May want to do all of this in parallel ...

        if (searchResultMode == SearchResultMode.Demographics) {
            List<Subject> aggregatedSubjects = aggregatedSubjectSearchResponse.getSubjects();
            for (GatewayResponse gatewayResponse : responses) {
                List<Subject> subjects = this.processResponse(gatewayResponse);
                if (subjects.size() > 0) {
                    aggregatedSubjects.addAll(subjects);
                }
                // Always update the cache ... we will keep track of non-responses also so we don't
                // search again until the cache is flushed for the remote community.
                this.updateCache(gatewayResponse, subjects);
            }
        } else {

            // Go through each response.
            List<PatientCorrelationCacheEntry> cacheEntries = new ArrayList<PatientCorrelationCacheEntry>();
            for (GatewayResponse gatewayResponse : responses) {
                List<Subject> subjects = this.processResponse(gatewayResponse);

                // Always update the cache ... we will keep track of non-responses also so we don't
                // search again until the cache is flushed for the remote community.
                List<PatientCorrelationCacheEntry> gatewayActiveCacheEntries = this.updateCache(gatewayResponse, subjects);
                cacheEntries.addAll(gatewayActiveCacheEntries);
            }

            // Update the list of active cache entries (based on search result).
            this.activeCacheEntries.addAll(cacheEntries);

            // Get the subjects (in the cache).
            List<Subject> cachedSubjects = this.getCachedSubjects();

            // Set these subjects in the response.
            aggregatedSubjectSearchResponse.setSubjects(cachedSubjects);
        }
        return aggregatedSubjectSearchResponse;
    }

    /**
     *
     * @param gatewayResponse
     * @param communitySubjectIdentifier
     * @return
     */
    private List<Subject> processResponse(GatewayResponse gatewayResponse) {
        List<Subject> resultSubjects = new ArrayList<Subject>();
        List<Subject> noMatchSubjects = new ArrayList<Subject>();
        SubjectSearchResponse subjectSearchResponse = this.getSubjectSearchResponse(gatewayResponse);

        // Now validate that each remote subject matches our local subject.
        for (Subject remoteSubject : subjectSearchResponse.getSubjects()) {
            boolean validRemoteSubject = this.isRemoteSubjectValid(remoteSubject, communitySubjectIdentifier);
            if (validRemoteSubject == true) {
                // Match!!! ... add the remote subject to aggregated result.
                resultSubjects.add(remoteSubject);
            } else {
                // No match ... keep track for logging purposes.
                noMatchSubjects.add(remoteSubject);
            }
        }
        this.log(gatewayResponse, resultSubjects, noMatchSubjects);
        return resultSubjects;
    }

    /**
     * 
     * @param remoteSubject
     * @param communitySubjectIdentifier
     * @return
     */
    private boolean isRemoteSubjectValid(Subject remoteSubject, SubjectIdentifier communitySubjectIdentifier) {
        boolean validRemoteSubject = false;

        // Save (then clear) remote SubjectIdentifiers (we don't want them in our local PDQ query).
        List<SubjectIdentifier> remoteSubjectIdentifiers = remoteSubject.getSubjectIdentifiers();
        remoteSubject.setSubjectIdentifiers(new ArrayList<SubjectIdentifier>());

        // Prepare PDQ search criteria.
        SubjectSearchCriteria pdqSubjectSearchCriteria = new SubjectSearchCriteria();
        pdqSubjectSearchCriteria.setSubject(remoteSubject);

        // FIXME: ? Not sure if necessary ?
        requestHandler.setMinimumDegreeMatchPercentage(pdqSubjectSearchCriteria);

        // Scope PDQ request to local community assigning authority only.
        SubjectIdentifierDomain communityAssigningAuthority = requestHandler.getCommunityAssigningAuthority();
        pdqSubjectSearchCriteria.setCommunityAssigningAuthority(communityAssigningAuthority);
        pdqSubjectSearchCriteria.addScopingAssigningAuthority(communityAssigningAuthority);

        try {
            // Issue PDQ using demographics supplied by remote community.
            DeviceInfo senderDeviceInfo = requestHandler.getSenderDeviceInfo();
            SubjectSearchResponse pdqSearchResponse = requestHandler.findCandidatesQuery(senderDeviceInfo, pdqSubjectSearchCriteria);

            // Restore identifiers in remote Subject.
            remoteSubject.setSubjectIdentifiers(remoteSubjectIdentifiers);

            // Now see if we can confirm a match.
            // See if we find our subject's identifier in the PDQ response.
            List<Subject> localSubjects = pdqSearchResponse.getSubjects();
            validRemoteSubject = false;
            for (Subject localSubject : localSubjects) {
                if (localSubject.hasSubjectIdentifier(communitySubjectIdentifier)) {
                    validRemoteSubject = true;
                    break;  // No need to look further.
                }
            }
        } catch (SOAPFaultException ex) {
            logger.error("XCPD EXCEPTION ... continuing", ex);
        }
        return validRemoteSubject;
    }

    /**
     *
     * @param gatewayResponse
     */
    private SubjectSearchResponse getSubjectSearchResponse(GatewayResponse gatewayResponse) {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
        PRPA_IN201306UV02_Message cgpdResponse = gatewayResponse.getResponse();
        try {
            requestHandler.validateHL7V3Message(cgpdResponse);
        } catch (SOAPFaultException ex) {
            logMessage.setPass(false);
            if (logMessage.isLogEnabled()) {
                logMessage.addErrorParam("EXCEPTION: " + gatewayResponse.getRequest().getVitals(), ex.getMessage());
            }
            logger.error("CGPD Response did not validate against XML schema: " + ex.getMessage());
            return subjectSearchResponse;
        }
        try {
            SubjectBuilder subjectBuilder = new SubjectBuilder();
            subjectSearchResponse = subjectBuilder.buildSubjectSearchResponse(cgpdResponse);

        } catch (ModelBuilderException ex) {
            // TBD ....
        }
        return subjectSearchResponse;
    }

    /**
     *
     * @param gatewayResponse
     * @param communitySubjectIdentifier
     * @param subjects
     */
    private List<PatientCorrelationCacheEntry> updateCache(GatewayResponse gatewayResponse, List<Subject> subjects) {
        List<PatientCorrelationCacheEntry> cacheEntries = new ArrayList<PatientCorrelationCacheEntry>();
        List<PatientCorrelationCacheEntry> gatewayActiveCacheEntries = new ArrayList<PatientCorrelationCacheEntry>();
        String localPatientId = communitySubjectIdentifier.getCXFormatted();
        String localHomeCommunityId;
        String remoteHomeCommunityId = gatewayResponse.getRequest().getRGConfig().getUniqueId();
        try {
            localHomeCommunityId = requestHandler.getGatewayConfig().getUniqueId();
        } catch (Exception ex) {
            logMessage.setPass(false);
            if (logMessage.isLogEnabled()) {
                logMessage.addErrorParam("XCPD EXCEPTION: " + gatewayResponse.getRequest().getVitals(), ex.getMessage());
            }
            return null;
        }
        if (subjects.size() == 0) {
            // Just note the fact that we did a search for this patient for the community with
            // no success (so we don't search again until the cache entry expires).

            // FIXME: SHOULD USE A DIFFERENT EXPIRATION TIME HERE ...
            PatientCorrelationCacheEntry cacheEntry =
                    this.getPatientCorrelationCacheEntry(localPatientId, localHomeCommunityId, null, remoteHomeCommunityId);
            cacheEntry.setStatus(PatientCorrelationCacheEntry.STATUS_NO_MATCH);
            cacheEntries.add(cacheEntry);  // Add to the list.
        } else {
            for (Subject subject : subjects) {
                // FIXME!!!!!
                // FIXME: for now store all patient identifiers from community.
                // FIXME: should only store those for configured assigning authority.
                for (SubjectIdentifier subjectIdentifier : subject.getSubjectIdentifiers()) {
                    String remotePatientId = subjectIdentifier.getCXFormatted();
                    PatientCorrelationCacheEntry cacheEntry =
                            this.getPatientCorrelationCacheEntry(localPatientId, localHomeCommunityId, remotePatientId, remoteHomeCommunityId);

                    cacheEntry.setStatus(PatientCorrelationCacheEntry.STATUS_MATCH);

                    // Add to the list to store.
                    cacheEntries.add(cacheEntry);

                    // Keep track of active cached entries (to return later).
                    gatewayActiveCacheEntries.add(cacheEntry);
                }
            }
        }

        // Store patient correlations in cache ...
        try {
            cacheService.store(cacheEntries);
        } catch (PatientCorrelationCacheException ex) {
            logMessage.setPass(false);
            if (logMessage.isLogEnabled()) {
                logMessage.addErrorParam("XCPD EXCEPTION: " + gatewayResponse.getRequest().getVitals(), ex.getMessage());
            }
        }
        return gatewayActiveCacheEntries;
    }

    /**
     *
     * @return
     */
    private synchronized List<XConfigActor> getXCPDRespondingGateways() {
        if (_xcpdRespondingGateways != null) {
            return _xcpdRespondingGateways;
        }
        // Get gateway configuration.
        XConfigObject gatewayConfig;
        try {
            gatewayConfig = requestHandler.getGatewayConfig();
        } catch (SOAPFaultException ex) {
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
            if (rgConfigs.isEmpty()) {
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
     * Return SubjectIdentifier in the CommunityAssigningAuthority using the Subject search
     * template in the given SubjectSearchCriteria.  Return null if not found (should not be
     * the case here based on prior validations).
     *
     * @param patientDiscoverySearchCriteria
     * @return
     */
    private SubjectIdentifier getCommunitySubjectIdentifier(SubjectSearchCriteria patientDiscoverySearchCriteria) {
        SubjectIdentifierDomain communityAssigningAuthority = requestHandler.getCommunityAssigningAuthority();
        return patientDiscoverySearchCriteria.getSubjectIdentifier(communityAssigningAuthority);
    }

    /**
     *
     * @param localPatientId
     * @param localHomeCommunityId
     * @param remotePatientId
     * @param remoteHomeCommunityId
     * @return
     */
    private PatientCorrelationCacheEntry getPatientCorrelationCacheEntry(
            String localPatientId, String localHomeCommunityId, String remotePatientId, String remoteHomeCommunityId) {
        PatientCorrelationCacheEntry cacheEntry = new PatientCorrelationCacheEntry();
        cacheEntry.setLocalPatientId(localPatientId);
        cacheEntry.setLocalHomeCommunityId(localHomeCommunityId);
        cacheEntry.setRemotePatientId(remotePatientId);
        cacheEntry.setRemoteHomeCommunityId(remoteHomeCommunityId);
        return cacheEntry;
    }

    /**
     *
     * @param gatewayResponse
     * @param matches
     * @param noMatches
     */
    private void log(GatewayResponse gatewayResponse, List<Subject> matches, List<Subject> noMatches) {
        if (matches.size() > 0) {
            if (logMessage.isLogEnabled()) {
                String logText = this.getLogText(matches);
                logMessage.addOtherParam("CGPD RESPONSE " + gatewayResponse.getRequest().getVitals(),
                        "CONFIRMED MATCH (count=" + matches.size() + "): " + logText);
            }
        }
        if (noMatches.size() > 0) {
            logMessage.setPass(false);
            if (logMessage.isLogEnabled()) {
                String logText = this.getLogText(noMatches);
                logMessage.addErrorParam("CGPD RESPONSE " + gatewayResponse.getRequest().getVitals(),
                        "NO CONFIRMED MATCH (count=" + noMatches.size() + "): " + logText);
            }
        }
    }

    /**
     *
     * @param subjects
     * @return
     */
    private String getLogText(List<Subject> subjects) {
        String logText = new String();
        int count = 0;
        for (Subject subject : subjects) {
            for (SubjectIdentifier subjectIdentifier : subject.getSubjectIdentifiers()) {
                if (count > 0) {
                    logText = logText + ", ";
                }
                ++count;
                logText = logText + subjectIdentifier.getCXFormatted();
            }
        }
        return logText;
    }
}
