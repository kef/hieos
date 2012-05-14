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
package com.vangent.hieos.services.xdr.recipient.transactions;

import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.IHETransaction;
import com.vangent.hieos.xutil.atna.ATNAAuditEventHelper;
import com.vangent.hieos.xutil.atna.ATNAAuditEventRegisterDocumentSet;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.exception.XDSMissingDocumentException;
import com.vangent.hieos.xutil.exception.XDSMissingDocumentMetadataException;
import com.vangent.hieos.xutil.exception.XdsConfigurationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsFormatException;
import com.vangent.hieos.xutil.exception.XdsIOException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import com.vangent.hieos.xutil.exception.XDSRepositoryMetadataError;
import com.vangent.hieos.xutil.soap.SoapActionFactory;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import java.io.IOException;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami & Bernie Thuman
 */
public class ProcessXDRPackage extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(ProcessXDRPackage.class);

    /**
     *
     * @param log_message
     */
    public ProcessXDRPackage(XLogMessage log_message) {
        this.log_message = log_message;
        try {
            init(new RegistryResponse());
        } catch (XdsInternalException e) {
            logger.fatal("Internal Error creating XDR Response: " + e.getMessage());
        }
    }

    /**
     *
     * @param pnr
     * @param validater
     * @return
     */
    public OMElement processXDRPackage(OMElement pnr) {
        try {
            logger.info("In processXDRPackage");
            pnr.build();
            sendXDRPackage(pnr);
        } catch (XdsFormatException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "SOAP Format Error: " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (XDSMissingDocumentException e) {
            response.add_error(MetadataSupport.XDSMissingDocument, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XDSMissingDocumentMetadataException e) {
            response.add_error(MetadataSupport.XDSMissingDocumentMetadata, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XdsInternalException e) {
            response.add_error(MetadataSupport.XDRRecipientError, "XDR Internal Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (XdsIOException e) {
            response.add_error(MetadataSupport.XDRRecipientError, "XDR IO Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (XdsConfigurationException e) {
            response.add_error(MetadataSupport.XDRRecipientError, "XDR Configuration Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (MetadataValidationException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "Metadata Validation Errors:\n " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (MetadataException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "Metadata Validation Errors:\n " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (SchemaValidationException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "Schema Validation Errors:\n" + e.getMessage(), this.getClass().getName(), log_message);
        } catch (XDSRepositoryMetadataError e) {
            response.add_error(MetadataSupport.XDSRepositoryMetadataError, "Metadata Validation Errors:\n " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (XdsException e) {
            response.add_error(MetadataSupport.XDRRecipientError, "XDS Internal Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (Exception e) {
            response.add_error(MetadataSupport.XDRRecipientError, "Input Error - no SOAP Body:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        }

        this.log_response();
        OMElement res = null;
        try {
            res = response.getResponse();
        } catch (XdsInternalException e) {
            log_message.addErrorParam("Internal Error", "Error generating response from PnR");
        }
        return res;
    }

    /**
     *
     * @param pnr
     * @throws com.vangent.hieos.xutil.exception.MetadataValidationException
     * @throws com.vangent.hieos.xutil.exception.SchemaValidationException
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     * @throws com.vangent.hieos.xutil.exception.MetadataException
     * @throws com.vangent.hieos.xutil.exception.XdsConfigurationException
     * @throws com.vangent.hieos.xutil.exception.XdsIOException
     * @throws com.vangent.hieos.xutil.exception.XdsException
     * @throws java.io.IOException
     */
    private void sendXDRPackage(OMElement pnr)
            throws MetadataValidationException, SchemaValidationException,
            XdsInternalException, MetadataException, XdsConfigurationException,
            XdsIOException, XdsException, IOException {

        logger.info("In sendXDRPackage");
        OMElement sor = find_sor(pnr);
        if (logger.isDebugEnabled()) {
            logger.debug("SOR in XDR Request: " + sor.toString());
        }
        Metadata m = new Metadata(sor);
        //AUDIT:POINT
        //call to audit message for XDR Recipient
        //for Transaction id = ITI-41. (Provide & Register Document set-b)
        this.auditProvideAndRegisterDocumentSet(ATNAAuditEvent.ActorType.DOCRECIPIENT, ATNAAuditEvent.AuditEventType.IMPORT, sor, null);
        String intendedRecipient = m.getSlotValue(m.getRegistryPackages().get(0), "intendedRecipient", 0);
        if (intendedRecipient == null) {
            // default to spaces 
            intendedRecipient = "";
        }
        log_message.addOtherParam("SSuid", m.getSubmissionSetUniqueId());
        log_message.addOtherParam("Structure", m.structure());
        log_message.addOtherParam("IntendedRecipient", intendedRecipient);


        // Call PNR End Point
        Soap soap = new Soap();

        XConfigTransaction txn = this.getConfigActor().getTransaction("ProvideAndRegisterDocumentSet-b");
        String epr = txn.getEndpointURL();

        // Retrieve the PNR URL to use from the XConfig file
        // FIXME: Modify logic to retrieve the endpoint based on intendedRecipient value
        logger.info("XDR PNR transaction endpoint" + epr);
        log_message.addOtherParam("XDR PNR transaction endpoint", epr);
        String action = getAction();
        String expectedReturnAction = getExpectedReturnAction();
        soap.setAsync(txn.isAsyncTransaction());

        try {
            OMElement result;
            try {
                //AUDIT:POINT
                //call to audit message for XDR Recipient
                //for Transaction id = ITI-41. (Provide & Register Document set-b)
                this.auditProvideAndRegisterDocumentSet(ATNAAuditEvent.ActorType.DOCSOURCE, ATNAAuditEvent.AuditEventType.EXPORT, sor, epr);
                soap.soapCall(pnr, epr, true, txn.isSOAP12Endpoint(), txn.isSOAP12Endpoint(), action, expectedReturnAction);
            } catch (SOAPFaultException e) {
                logger.info("RETURNED FROM PNR TRANSACTION WITH ERROR" + e);
                response.add_error(MetadataSupport.XDSRepositoryError, e.getMessage(), this.getClass().getName(), log_message);
                return;  // Early exit!!
            }

            // Format Response
            result = soap.getResult();

            logger.info("Response from PNR: " + result.toString());

            log_headers(soap);
            if (result == null) {
                response.add_error(MetadataSupport.XDSRepositoryError, "Null response message from XDR PnR: ", this.getClass().getName(), log_message);
                log_message.addOtherParam("Register transaction response", "null");

            } else {
                log_message.addOtherParam("XDR PNR transaction response", result);
                String status = result.getAttributeValue(MetadataSupport.status_qname);
                if (status == null) {
                    response.add_error(MetadataSupport.XDSRepositoryError, "Null status from XDR PnR: ", this.getClass().getName(), log_message);
                } else {
                    status = m.stripNamespace(status);

                    if (!status.equals("Success")) {
                        OMElement registry_error_list = MetadataSupport.firstChildWithLocalName(result, "RegistryErrorList");
                        if (registry_error_list != null) {
                            response.addRegistryErrorList(registry_error_list, log_message);
                        } else {
                            response.add_error(MetadataSupport.XDSRepositoryError, "Registry returned Failure but no error list", this.getClass().getName(), log_message);
                        }
                    }
                }
            }
        } catch (Exception e) {
            response.add_error(MetadataSupport.XDRRecipientError, e.getMessage(), this.getClass().getName(), log_message);
        }
    }

    /**
     *
     * @param soap
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void log_headers(Soap soap) throws XdsInternalException {
        OMElement in_hdr = soap.getInHeader();
        OMElement out_hdr = soap.getOutHeader();
        log_message.addSOAPParam("Header sent to XDR Destination", (out_hdr == null) ? "Null" : out_hdr);
        log_message.addSOAPParam("Header received from XDR Destination", (in_hdr == null) ? "Null" : in_hdr);
    }

    /**
     *
     * @param pnr
     * @return
     * @throws com.vangent.hieos.xutil.exception.MetadataValidationException
     */
    private OMElement find_sor(OMElement pnr) throws MetadataValidationException {
        OMElement sor;
        sor = pnr.getFirstElement();
        if (sor == null || !sor.getLocalName().equals("SubmitObjectsRequest")) {
            throw new MetadataValidationException("Cannot find SubmitObjectsRequest element in submission - top level element is "
                    + pnr.getLocalName());
        }
        return sor;
    }

    /**
     * 
     * @return
     */
    private String getAction() {
        return SoapActionFactory.XDSB_REPOSITORY_PNR_ACTION;
    }

    /**
     *
     * @return
     */
    private String getExpectedReturnAction() {
        return SoapActionFactory.XDSB_REPOSITORY_PNR_ACTION_RESPONSE;
    }

    /**
     * 
     * @param actorType
     * @param rootNode
     * @param targetEndpoint
     */
    private void auditProvideAndRegisterDocumentSet(
            ATNAAuditEvent.ActorType actorType, ATNAAuditEvent.AuditEventType auditEventType, OMElement rootNode, String targetEndpoint) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                // Create and log audit event.
                ATNAAuditEventRegisterDocumentSet auditEvent = ATNAAuditEventHelper.getATNAAuditEventProvideAndRegisterDocumentSet(rootNode);
                auditEvent.setActorType(actorType);
                auditEvent.setTransaction(IHETransaction.ITI41);
                auditEvent.setTargetEndpoint(targetEndpoint);
                auditEvent.setAuditEventType(auditEventType);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            // FIXME?:
        }
    }
}
