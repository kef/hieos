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
package com.vangent.hieos.services.xcpd.gateway.transactions;

import com.vangent.hieos.hl7v3util.client.PDSClient;
import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.hl7v3util.xml.HL7V3SchemaValidator;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.XMLSchemaValidatorException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xml.XPathHelper;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public abstract class XCPDGatewayRequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(XCPDGatewayRequestHandler.class);

    public enum GatewayType {

        InitiatingGateway,
        RespondingGateway,
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
     * @throws AxisFault
     */
    protected XConfigActor getGatewayConfig() throws AxisFault {
        try {
            XConfig xconf = XConfig.getInstance();
            // Get the home community config.
            XConfigObject homeCommunityConfig = xconf.getHomeCommunityConfig();
            String gatewayConfigName = "ig";  // default.
            String gatewayConfigType = XConfig.XCA_INITIATING_GATEWAY_TYPE;
            if (this.gatewayType == GatewayType.RespondingGateway) {
                gatewayConfigName = "rg";
                gatewayConfigType = XConfig.XCA_RESPONDING_GATEWAY_TYPE;
            }
            XConfigObject gatewayConfig = homeCommunityConfig.getXConfigObjectWithName(
                    gatewayConfigName, gatewayConfigType);
            return (XConfigActor) gatewayConfig;
        } catch (XdsInternalException ex) {
            logger.fatal("Unable to load XConfig for XCPD Gateway", ex);
            throw new AxisFault(ex.getMessage());
        }
    }

    /**
     *
     * @param propertyKey
     * @return
     */
    protected String getGatewayConfigProperty(String propertyKey) {
        String propertyValue = "";
        try {
            XConfigActor gatewayConfig = this.getGatewayConfig();
            propertyValue = gatewayConfig.getProperty(propertyKey);
        } catch (AxisFault ex) {
            // TBD: Do something.
            logger.fatal("XCPD EXCEPTION: Unable to load XConfig for XCPD Gateway", ex);
        }
        return propertyValue;
    }

    /**
     *
     * @return
     */
    protected SubjectIdentifierDomain getCommunityAssigningAuthority() {
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
     * @throws AxisFault
     */
    protected SubjectSearchResponse findCandidatesQuery(DeviceInfo senderDeviceInfo, SubjectSearchCriteria subjectSearchCriteria) throws AxisFault {
        DeviceInfo receiverDeviceInfo = this.getDeviceInfo(this.getPDSConfig());
        XConfigActor pdsConfig = this.getPDSConfig();
        PDSClient pdsClient = new PDSClient(pdsConfig);
        SubjectSearchResponse subjectSearchResponse = pdsClient.findCandidatesQuery(senderDeviceInfo, receiverDeviceInfo, subjectSearchCriteria);
        return subjectSearchResponse;
    }

    /**
     * 
     * @param message
     * @throws AxisFault
     */
    protected void validateHL7V3Message(HL7V3Message message) throws AxisFault {
        try {
            HL7V3SchemaValidator.validate(message.getMessageNode(), message.getType());
        } catch (XMLSchemaValidatorException ex) {
            //log_message.setPass(false);
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            throw new AxisFault(ex.getMessage());
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
     * @throws AxisFault
     */
    protected synchronized XConfigActor getPDSConfig() throws AxisFault {
        // Must be overwridden.
        return null;
    }

    /**
     *
     * @return
     */
    protected DeviceInfo getSenderDeviceInfo() {
        try {
            XConfigActor gatewayConfig = this.getGatewayConfig();
            return this.getDeviceInfo(gatewayConfig);
        } catch (AxisFault ex) {
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
    protected DeviceInfo getDeviceInfo(XConfigActor actorConfig) {
        DeviceInfo deviceInfo = new DeviceInfo(actorConfig);
        return deviceInfo;
    }

    /**
     *
     * @param errorText
     */
    protected void log(String errorText) {
        if (errorText != null) {
            log_message.setPass(false);
            if (log_message.isLogEnabled()) {
                log_message.addErrorParam("EXCEPTION", errorText);
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

    // FIXME: Rewrite
    /**
     * 
     * @param request
     * @param subjectSearchCriteria
     * @param endpoint
     */
    protected void performATNAAudit(PRPA_IN201305UV02_Message request,
            SubjectSearchCriteria subjectSearchCriteria, String endpoint) {
        try {
            //Instantiate the audit class
            XATNALogger.ActorType actorType = XATNALogger.ActorType.RESPONDING_GATEWAY;
            if (this.gatewayType == GatewayType.InitiatingGateway) {
                actorType = XATNALogger.ActorType.INITIATING_GATEWAY;
            }
            XATNALogger xATNALogger = new XATNALogger(XATNALogger.TXN_ITI55, actorType);

            // Get patient ids.
            String patientIdText = null;
            Subject subject = subjectSearchCriteria.getSubject();
            for (SubjectIdentifier subjectIdentifer : subject.getSubjectIdentifiers()) {
                SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifer.getIdentifierDomain();
                String assigningAuthority = subjectIdentifierDomain.getUniversalId();
                String pid = subjectIdentifer.getIdentifier() + "^^^&" + assigningAuthority + "&ISO";
                if (patientIdText == null) {
                    patientIdText = pid;
                } else {
                    patientIdText = patientIdText + "," + pid;
                }
            }
            String homeCommunityId = this.getGatewayConfig().getUniqueId();
            xATNALogger.performAuditCrossGatewayPatientDiscovery(
                    patientIdText,
                    homeCommunityId,
                    this.getQueryId(request),
                    this.getQueryByParameter(request),
                    endpoint,
                    XATNALogger.OutcomeIndicator.SUCCESS);
        } catch (Exception ex) {
            logger.error("XCPD EXCEPTION: Could not perform ATNA audit", ex);
        }
    }

    // FIXME: Rewrite (MOVE TO PROPER CODE LOCATION).
    /**
     *
     * @param request
     * @return
     */
    private String getQueryByParameter(PRPA_IN201305UV02_Message request) {
        String XPATH_QUERY_BY_PARAMETER =
                "./ns:controlActProcess/ns:queryByParameter[1]";
        String HL7V3_NAMESPACE = "urn:hl7-org:v3";

        String queryByParameter = "UNKNOWN";
        try {
            OMElement queryByParameterNode = XPathHelper.selectSingleNode(
                    request.getMessageNode(),
                    XPATH_QUERY_BY_PARAMETER, HL7V3_NAMESPACE);
            queryByParameter = queryByParameterNode.toString();
        } catch (XPathHelperException ex) {
            // FIXME: ???
        }
        return queryByParameter;
    }

    // FIXME: Rewrite (MOVE TO PROPER CODE LOCATION).
    /**
     * 
     * @param request
     * @return
     */
    private String getQueryId(PRPA_IN201305UV02_Message request) {
        String XPATH_QUERY_ID =
                "./ns:controlActProcess/ns:queryByParameter/ns:queryId[1]";
        String HL7V3_NAMESPACE = "urn:hl7-org:v3";

        String queryId = "UNKNOWN";
        try {
            OMElement queryIdNode = XPathHelper.selectSingleNode(
                    request.getMessageNode(), XPATH_QUERY_ID, HL7V3_NAMESPACE);
            queryId = queryIdNode.toString();
        } catch (XPathHelperException ex) {
            // FIXME: ???
        }
        return queryId;
    }
}
