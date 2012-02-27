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
package com.vangent.hieos.services.xcpd.gateway.framework;

import com.vangent.hieos.hl7v3util.atna.ATNAAuditEventHelper;
import com.vangent.hieos.hl7v3util.client.PDSClient;
import com.vangent.hieos.hl7v3util.model.message.HL7V3ErrorDetail;

import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;

import com.vangent.hieos.hl7v3util.xml.HL7V3SchemaValidator;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.IHETransaction;
import com.vangent.hieos.xutil.atna.ATNAAuditEventQuery;

import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.exception.XMLSchemaValidatorException;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public abstract class XCPDGatewayRequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(XCPDGatewayRequestHandler.class);
    private final static int DEFAULT_MINIMUM_DEGREE_MATCH_PERCENTAGE = 90;

    /**
     *
     */
    public enum GatewayType {

        /**
         *
         */
        InitiatingGateway,
        /**
         *
         */
        RespondingGateway,
        /**
         *
         */
        Unknown
    };
    private GatewayType gatewayType = GatewayType.Unknown;

    /**
     *
     */
    protected XCPDGatewayRequestHandler() {
        // Do nothing.
    }

    /**
     *
     * @param log_message
     * @param gatewayType
     */
    protected XCPDGatewayRequestHandler(XLogMessage log_message, XCPDGatewayRequestHandler.GatewayType gatewayType) {
        this.log_message = log_message;
        this.gatewayType = gatewayType;
    }

    /**
     *
     * @return
     * @throws SOAPFaultException
     */
    public XConfigActor getGatewayConfig() throws SOAPFaultException {
        return this.getConfigActor();
    }

    /**
     *
     * @param propertyKey
     * @return
     */
    public String getGatewayConfigProperty(String propertyKey) {
        String propertyValue = "";
        try {
            XConfigActor gatewayConfig = this.getGatewayConfig();
            propertyValue = gatewayConfig.getProperty(propertyKey);
        } catch (SOAPFaultException ex) {
            // TBD: Do something.
            logger.fatal("XCPD EXCEPTION: Unable to load XConfig for XCPD Gateway", ex);
        }
        return propertyValue;
    }

    /**
     *
     * @param subjectSearchCriteria
     */
    public void setMinimumDegreeMatchPercentage(SubjectSearchCriteria subjectSearchCriteria) {
        // TEMPORARY DISABLE (CONNECTATHON) - some gateways do not interpret properly
        // FIXME?
        /*
        if (subjectSearchCriteria.hasSpecifiedMinimumDegreeMatchPercentage() == false) {
            logger.info("Setting MinimumDegreeMatchPercentage from xconfig");
            String minimumDegreeMatchPercentageText = this.getGatewayConfigProperty("MinimumDegreeMatchPercentage");
            int minimumDegreeMatchPercentage = XCPDGatewayRequestHandler.DEFAULT_MINIMUM_DEGREE_MATCH_PERCENTAGE;
            if (minimumDegreeMatchPercentageText != null) {
                minimumDegreeMatchPercentage = new Integer(minimumDegreeMatchPercentageText);
            }
            subjectSearchCriteria.setSpecifiedMinimumDegreeMatchPercentage(true);
            subjectSearchCriteria.setMinimumDegreeMatchPercentage(new Integer(minimumDegreeMatchPercentage));
        }*/
    }

    /**
     *
     * @return
     */
    public SubjectIdentifierDomain getCommunityAssigningAuthority() {
        String communityAssigningAuthority = this.getGatewayConfigProperty("CommunityAssigningAuthority");
        SubjectIdentifierDomain identifierDomain = new SubjectIdentifierDomain();
        identifierDomain.setUniversalId(communityAssigningAuthority);
        identifierDomain.setUniversalIdType("ISO");
        return identifierDomain;
    }

    /**
     *
     * @param senderDeviceInfo
     * @param subjectSearchCriteria
     * @return
     * @throws SOAPFaultException
     */
    public SubjectSearchResponse findCandidatesQuery(DeviceInfo senderDeviceInfo, SubjectSearchCriteria subjectSearchCriteria) throws SOAPFaultException {
        DeviceInfo receiverDeviceInfo = this.getDeviceInfo(this.getPDSConfig());
        XConfigActor pdsConfig = this.getPDSConfig();
        PDSClient pdsClient = new PDSClient(pdsConfig);
        SubjectSearchResponse subjectSearchResponse = pdsClient.findCandidatesQuery(senderDeviceInfo, receiverDeviceInfo, subjectSearchCriteria);
        return subjectSearchResponse;
    }

    /**
     * 
     * @param message
     * @throws SOAPFaultException
     */
    public void validateHL7V3Message(HL7V3Message message) throws SOAPFaultException {
        try {
            HL7V3SchemaValidator.validate(message.getMessageNode(), message.getType());
        } catch (XMLSchemaValidatorException ex) {
            //log_message.setPass(false);
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            throw new SOAPFaultException(ex.getMessage());
        }
    }

    /**
     * 
     * @param node
     * @return
     */
    protected OMElement clone(OMElement node) {
        OMElement clone = node.cloneOMElement();
        clone.build();
        return clone;
    }

    /**
     * 
     * @return
     * @throws SOAPFaultException
     */
    protected synchronized XConfigActor getPDSConfig() throws SOAPFaultException {
        // Must be overwridden.
        return null;
    }

    /**
     *
     * @return
     */
    public DeviceInfo getSenderDeviceInfo() {
        try {
            XConfigActor gatewayConfig = this.getGatewayConfig();
            return this.getDeviceInfo(gatewayConfig);
        } catch (SOAPFaultException ex) {
            logger.error("XCPD EXCEPTION: Can not get sender device info", ex);
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setId("UNKNOWN");
            deviceInfo.setName("UNKNOWN");
            return deviceInfo;
        }
    }

    /**
     *
     * @param actorConfig
     * @return
     */
    public DeviceInfo getDeviceInfo(XConfigActor actorConfig) {
        DeviceInfo deviceInfo = new DeviceInfo(actorConfig);
        return deviceInfo;
    }

    /**
     *
     * @param errorDetail
     */
    protected void log(HL7V3ErrorDetail errorDetail) {
        if (errorDetail != null) {
            log_message.setPass(false);
            if (log_message.isLogEnabled()) {
                log_message.addErrorParam("EXCEPTION", errorDetail.getText());
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean getStatus() {
        return log_message.isPass();
    }

    /**
     *
     * @param actorType
     * @param request
     * @param targetEndpoint
     */
    public void performAuditPDQQueryInitiator(ATNAAuditEvent.ActorType actorType,
            PRPA_IN201305UV02_Message request, String targetEndpoint) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                String homeCommunityId = this.getGatewayConfig().getUniqueId();
                ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getATNAAuditEventPDQQueryInitiator(
                        actorType, request, homeCommunityId, targetEndpoint);
                auditEvent.setTransaction(IHETransaction.ITI55);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            logger.error("XCPD EXCEPTION: Could not perform ATNA audit", ex);
        }
    }

    /**
     * 
     * @param actorType
     * @param request
     * @param subjectSearchResponse
     */
    public void performAuditPDQQueryProvider(ATNAAuditEvent.ActorType actorType,
            PRPA_IN201305UV02_Message request,
            SubjectSearchResponse subjectSearchResponse) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                String homeCommunityId = this.getGatewayConfig().getUniqueId();
                ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getATNAAuditEventPDQQueryProvider(
                        actorType, request, subjectSearchResponse, homeCommunityId);
                auditEvent.setTransaction(IHETransaction.ITI55);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            logger.error("XCPD EXCEPTION: Could not perform ATNA audit", ex);
        }
    }
}
