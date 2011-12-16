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
package com.vangent.hieos.services.xca.gateway.controller;

import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEventHelper;
import com.vangent.hieos.xutil.atna.ATNAAuditEventQuery;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.registry.RegistryUtility;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.soap.SoapActionFactory;
import com.vangent.hieos.xutil.metadata.structure.HomeAttribute;

// Exceptions.
import com.vangent.hieos.xutil.exception.SOAPFaultException;

// XConfig.
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

// Third-party.
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import java.util.ArrayList;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XCAQueryRequestCollection extends XCAAbstractRequestCollection {

    private final static Logger logger = Logger.getLogger(XCAQueryRequestCollection.class);

    /**
     * 
     * @param uniqueId
     * @param configActor
     * @param isLocalRequest
     * @param gatewayActorType
     */
    public XCAQueryRequestCollection(String uniqueId, XConfigActor configActor, boolean isLocalRequest, ATNAAuditEvent.ActorType gatewayActorType) {
        super(uniqueId, configActor, isLocalRequest, gatewayActorType);
    }

    /**
     * This returns the endpoint URL for the local registry or a responding gateway depending upon
     * whether the request is local or not.
     * @return a String value representing the URL.
     */
    public String getEndpointURL() {
        return this.getXConfigTransaction().getEndpointURL();
    }

    /**
     * 
     * @return OMElement
     * @throws SOAPFaultException
     */
    public OMElement sendRequests() throws SOAPFaultException {
        OMElement rootRequest = MetadataSupport.om_factory.createOMElement("AdhocQueryRequest", MetadataSupport.ebQns3);

        // Now consolidate all requests to send out.
        ArrayList<XCARequest> allXCARequests = this.getRequests();
        for (XCARequest request : allXCARequests) {
            rootRequest.addChild(((XCAQueryRequest) request).getResponseOption());
            rootRequest.addChild(request.getRequest());
        }

        // Get Transaction configuration
        XConfigTransaction xconfigTxn = getXConfigTransaction();
        // Now send the requests out.
        OMElement result = this.sendTransaction(rootRequest, xconfigTxn);
        this.setResult(result);
        if (result != null) { // to be safe.
            //logMessage.addOtherParam("Result (" + this.getEndpointURL() + ")", result);
            // Validate the response against the schema.
            try {
                RegistryUtility.schema_validate_local(result, MetadataTypes.METADATA_TYPE_SQ);
            } catch (Exception e) {
                result = null;  // Ignore the response.
                this.setResult(null);
                XCAErrorMessage errorMessage = new XCAErrorMessage(
                        MetadataSupport.XDSRepositoryMetadataError,
                        "Remote Gateway or Registry response did not validate against schema  [id = "
                        + this.getUniqueId() + ", endpoint = " + this.getEndpointURL() + "]",
                        this.getUniqueId());
                this.addErrorMessage(errorMessage);
                /*response.add_error(MetadataSupport.XDSRegistryMetadataError,
                "Remote Gateway or Registry response did not validate against schema  [id = " +
                this.getUniqueId() + ", endpoint = " + this.getEndpointURL() + "]",
                this.getUniqueId(), logMessage);*/
            }

            if ((result != null) && this.isLocalRequest()) {
                try {
                    XConfigObject homeCommunity = XConfig.getInstance().getHomeCommunityConfig();
                    this.setHomeAttributeOnResult(result, homeCommunity.getUniqueId());
                } catch (XConfigException ex) {
                    throw new SOAPFaultException("Unable to get home community configuration", ex);
                }
            }
        }
        return result;
    }

    /**
     *
     * @param request
     * @param xconfigTxn
     * @return
     * @throws SOAPFaultException
     */
    private OMElement sendTransaction(OMElement request, XConfigTransaction xconfigTxn)
            throws SOAPFaultException {

        String endpoint = xconfigTxn.getEndpointURL();
        boolean isAsyncTxn = xconfigTxn.isAsyncTransaction();
        String action = getAction();
        String expectedReturnAction = getExpectedReturnAction();

        logger.info("*** XCA action: " + action + ", expectedReturnAction: " + expectedReturnAction
                + ", Async: " + isAsyncTxn + ", endpoint: " + endpoint + " ***");

        // Do ATNA auditing (FIXME: Always showing success).
        this.auditQuery(request, endpoint, ATNAAuditEvent.OutcomeIndicator.SUCCESS);

        Soap soap = new Soap();
        soap.setAsync(isAsyncTxn);
        boolean soap12 = xconfigTxn.isSOAP12Endpoint();
        soap.soapCall(request, endpoint,
                false, /* mtom */
                soap12, /* addressing [only if SOAP1.2] */
                soap12,
                action,
                expectedReturnAction);

        OMElement result = soap.getResult();  // Get the result.


        return result;
    }

    /**
     * This method returns the SOAP action (either local or remote).
     *
     * @return a String value representing the action.
     */
    public String getAction() {
        String action = "";
        if (this.isLocalRequest()) {
            action = SoapActionFactory.XDSB_REGISTRY_SQ_ACTION;
        } else {
            action = SoapActionFactory.XCA_GATEWAY_CGQ_ACTION;
        }
        return action;
    }

    /**
     * This method returns the SOAP response action (either local or remote).
     * 
     * @return a String value representing the expected return action.
     */
    public String getExpectedReturnAction() {
        String action = "";
        if (this.isLocalRequest()) {
            action = SoapActionFactory.XDSB_REGISTRY_SQ_ACTION_RESPONSE;
        } else {
            action = SoapActionFactory.XCA_GATEWAY_CGQ_ACTION_RESPONSE;
        }
        return action;
    }

    /**
     * This method returns a transaction configuration definition for either RegistryStoredQuery or
     * CrossGatewayQuery, depending on whether the request is local or not.
     * @return XConfigTransaction.
     */
    private XConfigTransaction getXConfigTransaction() {
        String txnName = this.isLocalRequest() ? "RegistryStoredQuery" : "CrossGatewayQuery";
        XConfigTransaction txn = this.getXConfigActor().getTransaction(txnName);
        return txn;
    }

    /**
     *
     * @param result
     * @param home
     */
    private void setHomeAttributeOnResult(OMElement result, String home) {
        HomeAttribute homeAtt = new HomeAttribute(home);
        homeAtt.set(result);
    }

    /**
     * This method returns an appropriate ATNA transaction type depending on whether the request is local or not.
     * @return a String representing an ATNA transaction Type
     */
    public ATNAAuditEvent.IHETransaction getATNATransaction() {
        return this.isLocalRequest() ? ATNAAuditEvent.IHETransaction.ITI18 : ATNAAuditEvent.IHETransaction.ITI38;
    }

    /**
     * 
     * @param request
     * @param endpoint
     * @param outcome
     */
    private void auditQuery(OMElement request, String endpoint, ATNAAuditEvent.OutcomeIndicator outcome) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                ATNAAuditEvent.IHETransaction transaction = this.getATNATransaction();
                ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getATNAAuditEventRegistryStoredQuery(request);
                auditEvent.setTargetEndpoint(endpoint);
                auditEvent.setTransaction(transaction);
                auditEvent.setActorType(this.getGatewayActorType());
                auditEvent.setAuditEventType(ATNAAuditEvent.AuditEventType.QUERY_INITIATOR);
                // Determine home community id to log.
                String homeCommunityId;
                if (!this.isLocalRequest()) {
                    // Set to the target home community id for the query.
                    homeCommunityId = this.getUniqueId();
                } else {
                    // Set to the gateway's home community id.
                    XConfigObject homeCommunityConfig = XConfig.getInstance().getHomeCommunityConfig();
                    homeCommunityId = homeCommunityConfig.getUniqueId();
                }
                auditEvent.setHomeCommunityId(homeCommunityId);
                auditEvent.setOutcomeIndicator(outcome);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception e) {
            // Eat exception.
            logger.error("Could not perform ATNA audit", e);
        }
    }
}
