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
package com.vangent.hieos.xutil.services.framework;

import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEventStart;
import com.vangent.hieos.xutil.atna.ATNAAuditEventStop;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.response.AdhocQueryResponse;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.exception.ExceptionUtil;
import com.vangent.hieos.xutil.response.RegistryErrorList;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.response.RetrieveMultipleResponse;
import com.vangent.hieos.xutil.xlog.client.XLogger;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.TransportHeaders;
import org.apache.log4j.Logger;

// Axis2 LifeCycle support:
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.axis2.service.Lifecycle;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

// XATNALogger
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xua.client.XServiceProvider;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author NIST
 * @author Bernie Thuman (BHT) - Comments, rewrites, lifecycle logging, streamlining.
 */
abstract public class XAbstractService implements ServiceLifeCycle, Lifecycle {

    private final static Logger logger = Logger.getLogger(XAbstractService.class);
    /**
     *
     */
    protected XLogMessage log_message = null;
    private String serviceName;
    private boolean active = true;

    /**
     *
     */
    public enum ActorType {

        /**
         * 
         */
        REGISTRY,
        /**
         *
         */
        REPOSITORY,
        /**
         * 
         */
        PIXMGR,
        /**
         *
         */
        PDS,
        /**
         * 
         */
        XCPD_GW,
        /**
         *
         */
        DOCRECIPIENT,
        /**
         * 
         */
        STS,
        /**
         *
         */
        PDP,
        /**
         * 
         */
        PIP,
        /**
         *
         */
        XDSBRIDGE
    }

    /**
     *
     * @return
     */
    abstract protected XConfigActor getConfigActor();

    /**
     *
     * @return
     */
    protected MessageContext getCurrentMessageContext() {
        return MessageContext.getCurrentMessageContext();
    }

    /**
     *
     * @return
     * @throws SOAPFaultException
     */
    public static OMElement getSAMLAssertionFromRequest() throws SOAPFaultException {
        return XServiceProvider.getSAMLAssertionFromRequest(MessageContext.getCurrentMessageContext());
    }

    /**
     *
     * @return
     */
    protected String getServiceName() {
        return this.serviceName;
    }

    /**
     *
     * @return
     * @throws SOAPFaultException
     */
    protected MessageContext getResponseMessageContext() throws SOAPFaultException {
        try {
            MessageContext messageContext = this.getCurrentMessageContext();
            MessageContext responseMessageContext = messageContext.getOperationContext().getMessageContext("Out");
            return responseMessageContext;
        } catch (AxisFault ex) {
            throw new SOAPFaultException("Unable to get response message context", ex);
        }
    }

    /**
     *
     * @return
     */
    public String getSOAPAction() {
        MessageContext mc = this.getCurrentMessageContext();
        return mc.getSoapAction();
    }

    /**
     * 
     * @throws SOAPFaultException
     */
    protected void validateWS() throws SOAPFaultException {
        checkSOAP12();
        if (isAsync()) {
            this.throwSOAPFaultException("Asynchronous web service request not acceptable on this endpoint"
                    + " - replyTo is " + getCurrentMessageContext().getReplyTo().getAddress());
        }
    }

    /**
     * This method ensures that an asynchronous request has been sent. It evaluates the message
     * context to dtermine if "ReplyTo" is non-null and is not anonymous. It also ensures that
     * "MessageID" is non-null. It throws an exception if that is not the case.
     * @throws SOAPFaultException
     */
    protected void validateAsyncWS() throws SOAPFaultException {
        checkSOAP12();
        if (!isAsync()) {
            this.throwSOAPFaultException("Asynchronous web service required on this endpoint"
                    + " - replyTo is " + getCurrentMessageContext().getReplyTo().getAddress());
        }
    }

    /**
     *
     * @throws SOAPFaultException
     */
    protected void validateNoMTOM() throws SOAPFaultException {
        if (getCurrentMessageContext().isDoingMTOM()) {
            this.throwSOAPFaultException("This transaction must use SIMPLE SOAP, MTOM found");
        }
    }

    /**
     *
     * @throws SOAPFaultException
     */
    protected void validateMTOM() throws SOAPFaultException {
        if (!getCurrentMessageContext().isDoingMTOM()) {
            this.throwSOAPFaultException("This transaction must use MTOM, SIMPLE SOAP found");
        }
    }

    /**
     *
     * @param serviceName
     * @param request
     * @throws SOAPFaultException
     */
    protected void beginTransaction(String serviceName, OMElement request) throws SOAPFaultException {
        // This gets around a bug in Leopard (MacOS X 10.5) on Macs
        //System.setProperty("http.nonProxyHosts", "");
        this.serviceName = serviceName;
        //this.mActor = actor;
        MessageContext messageContext = this.getCurrentMessageContext();

        String remoteIP = (String) messageContext.getProperty(MessageContext.REMOTE_ADDR);
        XLogger xlogger = XLogger.getInstance();
        log_message = xlogger.getNewMessage(remoteIP);
        log_message.setTestMessage(this.serviceName);
        logger.info("Start " + serviceName + " " + log_message.getMessageID() + " : " + remoteIP + " : " + messageContext.getTo().toString());

        if (log_message.isLogEnabled()) {
            // Log basic parameters:
            log_message.addOtherParam(Fields.service, serviceName);
            boolean is_secure = messageContext.getTo().toString().indexOf("https://") != -1;
            log_message.setSecureConnection(is_secure);
            log_message.addHTTPParam(Fields.isSecure, (is_secure) ? "true" : "false");
            log_message.addHTTPParam(Fields.date, getDateTime());
            if (request != null) {
                log_message.addOtherParam("Request", request);
            } else {
                log_message.addErrorParam("Error", "Cannot access request body in XAbstractService");
            }
        }

        // Need to get out if request body is null.
        if (request == null) {
            this.throwSOAPFaultException("Request body is null");
            //return start_up_error(request, null, this.mActor, "Request body is null");
        }

        if (log_message.isLogEnabled()) {
            // Log HTTP header:
            TransportHeaders transportHeaders = (TransportHeaders) messageContext.getProperty("TRANSPORT_HEADERS");
            for (Object o_key : transportHeaders.keySet()) {
                String key = (String) o_key;
                String value = (String) transportHeaders.get(key);
                List<String> thdrs = new ArrayList<String>();
                thdrs.add(key + " : " + value);
                this.addHttp("HTTP Header", thdrs);
            }
            // Log SOAP header:
            if (messageContext.getEnvelope().getHeader() != null) {
                try {
                    log_message.addSOAPParam("Soap Header", messageContext.getEnvelope().getHeader());
                } catch (OMException e) {
                    // Ignore.
                }
            }
            // Log SOAP envelope:
            if (messageContext.getEnvelope().getBody() != null) {
                try {
                    log_message.addSOAPParam("Soap Envelope", messageContext.getEnvelope());
                } catch (OMException e) {
                    // Ignore.
                }
            }
            log_message.addHTTPParam(Fields.fromIpAddress, remoteIP);
            log_message.addHTTPParam(Fields.endpoint, messageContext.getTo().toString());
        }
        this.validateXUA();  // Make sure we are good with XUA.
    }

    /**
     *
     * @throws SOAPFaultException
     */
    private void validateXUA() throws SOAPFaultException {
        XConfigActor configActor = this.getConfigActor();
        if (configActor == null) {
            this.throwSOAPFaultException("Configuration not established for Actor");
        }
        if (!configActor.isXUAEnabled()) {
            // Get out early - XUA is not enabled for this actor.
            return;
        }

        // Validate XUA constraints here:
        XServiceProvider xServiceProvider = new XServiceProvider(log_message);
        XServiceProvider.Status response = XServiceProvider.Status.ABORT;
        MessageContext messageCtx = this.getCurrentMessageContext();
        try {
            response = xServiceProvider.run(this.getConfigActor(), messageCtx);
        } catch (Exception ex) {
            this.throwSOAPFaultException("XUA:ERROR - SAML Validation Exception (ignoring request) " + ex.getMessage());
        }
        if (response != XServiceProvider.Status.CONTINUE) {
            // The assertion has not been validated, discontinue with processing SOAP request.
            this.throwSOAPFaultException("XUA:ERROR - SAML Assertion did not pass validation!");
        }
        // All is good.
    }

    /**
     *
     * @param status
     */
    protected void endTransaction(boolean status) {
        if (active && logger.isInfoEnabled()) {
            active = false;  // Only want to emit once.
            logger.info("End " + serviceName + " "
                    + ((log_message == null) ? "null" : log_message.getMessageID()) + " : "
                    + ((status) ? "Pass" : "Fail"));
        }
        stopXLogger();
    }

    /**
     *
     * @param request
     * @param e
     * @param actor
     * @param message
     * @return
     */
    protected OMElement endTransaction(OMElement request, Exception e, ActorType actor, String message) {
        if (message == null || message.equals("")) {
            message = e.getMessage();
        }
        logger.error("Exception thrown while processing web service request", e);
        OMElement errorResult = this.start_up_error(request, e, actor, message);
        if (log_message.isLogEnabled() && (errorResult != null)) {
            log_message.addOtherParam("Response - ERROR", errorResult);
        }
        endTransaction(false /* status */);
        return errorResult;
    }

    /**
     *
     * @param request
     * @param e
     * @param actor
     * @param message
     * @return
     */
    protected OMElement start_up_error(OMElement request, Exception e, ActorType actor, String message) {
        return start_up_error(request, e, actor, message, true);
    }

    /**
     *
     * @param request
     * @param e
     * @param actor
     * @param message
     * @param log
     * @return
     */
    public OMElement start_up_error(OMElement request, Object e, ActorType actor, String message, boolean log) {
        String error_type = (actor == ActorType.REGISTRY) ? MetadataSupport.XDSRegistryError : MetadataSupport.XDSRepositoryError;
        try {
            OMNamespace ns = (request != null) ? request.getNamespace() : MetadataSupport.ebRSns2;

            if (ns.getNamespaceURI().equals(MetadataSupport.ebRSns3.getNamespaceURI()) || ns.getNamespaceURI().equals("urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0")) {
                // xds.b submitobjectsrequest (could be xds.b retrieve)
                RegistryErrorList rel = new RegistryErrorList(log);
                rel.add_error(error_type, message, exception_details(e), log_message);
                return new RegistryResponse(rel).getResponse();
            }
            if (ns.getNamespaceURI().equals(MetadataSupport.xdsB.getNamespaceURI())) {
                // RetrieveDocumentSet
                RegistryErrorList rel = new RegistryErrorList(log);
                rel.add_error(error_type, message, exception_details(e), log_message);
                if (request.getLocalName().equals("RetrieveDocumentSetRequest")) {
                    OMElement res = new RetrieveMultipleResponse(rel).getResponse();
                    return res;
                } else {
                    return new RegistryResponse(rel).getResponse();
                }

            }
            if (ns.getNamespaceURI().equals(MetadataSupport.ebQns3.getNamespaceURI())) {
                // stored query
                RegistryErrorList rel = new RegistryErrorList(log);
                rel.add_error(error_type, message, exception_details(e), log_message);
                return new AdhocQueryResponse(rel).getResponse();
            }
        } catch (XdsInternalException e1) {
            return null;
        }
        return null;
    }

    /**
     * Stop the test log facility.
     */
    protected void stopXLogger() {
        if (log_message != null) {
            log_message.store();
            log_message = null;
        }
    }

    /**
     *
     * @param e
     * @return
     */
    protected String exception_details(Object e) {
        if (e == null) {
            return "No Additional Details Available";
        }
        if (e instanceof Exception) {
            return exception_details((Exception) e);
        }
        if (e instanceof String) {
            return exception_details((String) e);
        }
        return exception_details(e.toString());
    }

    /**
     *
     * @param e
     * @return
     */
    protected String exception_details(Exception e) {
        return ExceptionUtil.exception_details(e);
    }

    /**
     *
     * @param e
     * @return
     */
    protected String exception_details(String e) {
        return e;
    }

    /**
     *
     * @param title
     * @param t
     */
    private void addHttp(String title, List<String> t) {
        StringBuilder buffer = new StringBuilder();
        for (String s : t) {
            buffer.append(s).append("  ");
        }
        log_message.addHTTPParam(title, buffer.toString());
    }

    /**
     *
     * @return
     */
    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     *
     * @throws SOAPFaultException
     */
    protected void checkSOAP12() throws SOAPFaultException {
        if (this.getCurrentMessageContext().isSOAP11()) {
            throwSOAPFaultException("SOAP 1.1 not supported");
        }
        SOAPEnvelope env = this.getCurrentMessageContext().getEnvelope();
        if (env == null) {
            throwSOAPFaultException("No SOAP envelope found");
        }
        SOAPHeader hdr = env.getHeader();
        if (hdr == null) {
            throwSOAPFaultException("No SOAP header found");
        }
        if (!hdr.getChildrenWithName(new QName("http://www.w3.org/2005/08/addressing", "Action")).hasNext()) {
            throwSOAPFaultException("WS-Action required in header");
        }
    }

    /**
     *
     * @throws SOAPFaultException
     */
    protected void checkSOAP11() throws SOAPFaultException {

        if (!this.getCurrentMessageContext().isSOAP11()) {
            throwSOAPFaultException("SOAP 1.2 not supported");
        }
        SOAPEnvelope env = this.getCurrentMessageContext().getEnvelope();
        if (env == null) {
            throwSOAPFaultException("No SOAP envelope found");
        }
    }

    /**
     *
     * @throws SOAPFaultException
     */
    protected void checkSOAPAny() throws SOAPFaultException {
        if (this.getCurrentMessageContext().isSOAP11()) {
            checkSOAP11();
        } else {
            checkSOAP12();
        }
    }

    /**
     *
     * @param msg
     * @throws SOAPFaultException
     */
    private void throwSOAPFaultException(String msg) throws SOAPFaultException {
        if (log_message != null) {
            log_message.addErrorParam("SOAPError", msg);
            log_message.addOtherParam("Response", "SOAPFault: " + msg);
        }
        endTransaction(false);
        throw new SOAPFaultException(msg);
    }

    /**
     *
     * @param ex
     * @throws AxisFault
     */
    public void throwAxisFault(SOAPFaultException ex) throws AxisFault {
        if (log_message != null) {
            log_message.addErrorParam("SOAPError", ex.getMessage());
            log_message.addOtherParam("Response", "SOAPFault: " + ex.getMessage());
        }
        endTransaction(false);
        throw new AxisFault(ex.getMessage());
    }

    /**
     *
     * @param ex
     * @return
     */
    /*
    public AxisFault getAxisFault(Exception ex) {
    if (log_message != null) {
    log_message.addErrorParam("EXCEPTION", ex.getMessage());
    //log_message.setPass(false);
    }
    endTransaction(false);
    return new AxisFault(ex.getMessage());
    }*/
    /**
     *
     * @return
     */
    protected boolean isAsync() {
        MessageContext mc = getCurrentMessageContext();
        return mc.getMessageID() != null
                && !mc.getMessageID().equals("")
                && mc.getReplyTo() != null
                && !mc.getReplyTo().hasAnonymousAddress();
    }

    /**
     *
     * @return
     */
    boolean isSync() {
        return !isAsync();
    }

    // BHT (Added AXIS2 LifeCycle methods - can be overridden by subclasses.
    /**
     * This is called when a new instance of the implementing class has been created.
     * This occurs in sync with session/ServiceContext creation. This method gives classes
     * a chance to do any setup work (grab resources, establish connections, etc) before
     * they are invoked by a service request.
     * @param serviceContext 
     * @throws AxisFault
     */
    public void init(ServiceContext serviceContext) throws AxisFault {
        //logger.info("XdsService:::init() - NOOP (not overridden)");
    }

    /**
     * This is called when Axis2 decides that it is finished with a particular instance
     * of the back-end service class. It allows classes to clean up resources.
     * @param serviceContext
     */
    public void destroy(ServiceContext serviceContext) {
        //logger.info("XdsService:::destroy() - NOOP (not overridden)");
    }

    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     * @param configctx 
     * @param service
     */
    public void startUp(ConfigurationContext configctx, AxisService service) {
        //logger.info("XdsService:::startUp() - NOOP (not overridden)");
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     * @param configctx
     * @param service
     */
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        //logger.info("XdsService:::shutDown() - NOOP (not overridden)");
    }

    /**
     *
     * @param actorType
     */
    public void ATNAlogStop(ATNAAuditEvent.ActorType actorType) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                ATNAAuditEventStop auditEvent = new ATNAAuditEventStop();
                auditEvent.setTransaction(ATNAAuditEvent.IHETransaction.STOP);
                auditEvent.setActorType(actorType);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception e) {
            logger.error("Could not perform ATNA audit (stop)", e);
        }
    }

    /**
     *
     * @param actorType
     */
    public void ATNAlogStart(ATNAAuditEvent.ActorType actorType) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                ATNAAuditEventStart auditEvent = new ATNAAuditEventStart();
                auditEvent.setTransaction(ATNAAuditEvent.IHETransaction.START);
                auditEvent.setActorType(actorType);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception e) {
            logger.error("Could not perform ATNA audit (start)", e);
        }
    }
}
