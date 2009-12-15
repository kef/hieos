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

import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.exception.XdsWSException;
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
import java.util.Vector;

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
import com.vangent.hieos.xutil.exception.XdsFormatException;
import com.vangent.hieos.xutil.xua.client.XServiceProvider;

/**
 *
 * @author NIST
 * @author Bernie Thuman (BHT) - Comments, rewrites, lifecycle logging, streamlining.
 */
public class XAbstractService implements ServiceLifeCycle, Lifecycle {

    private final static Logger logger = Logger.getLogger(XAbstractService.class);
    protected XLogMessage log_message = null;

    public enum ActorType {

        REGISTRY, REPOSITORY
    }
    private String service_name;
    private ActorType mActor = ActorType.REGISTRY; // Default.

    /**
     *
     * @return
     */
    protected MessageContext getMessageContext() {
        return MessageContext.getCurrentMessageContext();
    }

    /**
     *
     * @return
     */
    protected String getServiceName() {
        return this.service_name;
    }

    /**
     *
     * @return
     */
    protected MessageContext getResponseMessageContext() throws AxisFault {
        MessageContext messageContext = this.getMessageContext();
        MessageContext responseMessageContext = messageContext.getOperationContext().getMessageContext("Out");
        return responseMessageContext;
    }

    /**
     *
     * @return
     */
    public String getSOAPAction() {
        MessageContext mc = this.getMessageContext();
        return mc.getSoapAction();
    }

    /**
     * 
     * @throws XdsWSException
     */
    protected void validateWS() throws XdsWSException {
        checkSOAP12();
        if (isAsync()) {
            throw new XdsWSException("Asynchronous web service request not acceptable on this endpoint" +
                    " - replyTo is " + getMessageContext().getReplyTo().getAddress());
        }
    }

    /**
     * This method ensures that an asynchronous request has been sent. It evaluates the message
     * context to dtermine if "ReplyTo" is non-null and is not anonymous. It also ensures that
     * "MessageID" is non-null. It throws an exception if that is not the case.
     * @throws XdsWSException
     */
    protected void validateAsyncWS() throws XdsWSException {
        checkSOAP12();
        if (!isAsync()) {
            throw new XdsWSException("Asynchronous web service required on this endpoint" +
                    " - replyTo is " + getMessageContext().getReplyTo().getAddress());
        }
    }

    /**
     *
     * @throws com.vangent.hieos.xutil.exception.XdsFormatException
     */
    protected void validateNoMTOM() throws XdsFormatException {
        if (getMessageContext().isDoingMTOM()) {
            throw new XdsFormatException("This transaction must use SIMPLE SOAP, MTOM found");
        }
    }

    /**
     *
     * @throws com.vangent.hieos.xutil.exception.XdsFormatException
     */
    protected void validateMTOM() throws XdsFormatException {
        if (!getMessageContext().isDoingMTOM()) {
            throw new XdsFormatException("This transaction must use MTOM, SIMPLE SOAP found");
        }
    }

    /**
     *
     * @param service_name
     * @param request
     * @param actor
     * @return
     */
    protected OMElement beginTransaction(String service_name, OMElement request, ActorType actor) throws AxisFault {

        // This gets around a bug in Leopard (MacOS X 10.5) on Macs
        System.setProperty("http.nonProxyHosts", "");
        this.service_name = service_name;
        this.mActor = actor;

        String remoteIP = (String) this.getMessageContext().getProperty(MessageContext.REMOTE_ADDR);
        logger.info("Start " + service_name + " : " + remoteIP + " : " + getMessageContext().getTo().toString());
        startTestLog();
        XLogger xlogger = XLogger.getInstance();
        log_message = xlogger.getNewMessage(remoteIP);
        log_message.setTestMessage(this.service_name);

        // Log basic parameters:
        log_message.addOtherParam(Fields.service, service_name);
        boolean is_secure = getMessageContext().getTo().toString().indexOf("https://") != -1;
        log_message.addHTTPParam(Fields.isSecure, (is_secure) ? "true" : "false");
        log_message.addHTTPParam(Fields.date, getDateTime());
        log_message.setSecureConnection(is_secure);
        if (request != null) {
            log_message.addOtherParam("Request", request);
        } else {
            log_message.addErrorParam("Error", "Cannot access request body in XdsService.begin_service()");
            return start_up_error(request, null, this.mActor, "Request body is null");
        }

        // Log HTTP header:
        TransportHeaders transportHeaders = (TransportHeaders) getMessageContext().getProperty("TRANSPORT_HEADERS");
        for (Object o_key : transportHeaders.keySet()) {
            String key = (String) o_key;
            String value = (String) transportHeaders.get(key);
            Vector<String> thdrs = new Vector<String>();
            thdrs.add(key + " : " + value);
            addHttp("HTTP Header", thdrs);
        }

        // Log SOAP header:
        if (getMessageContext().getEnvelope().getHeader() != null) {
            try {
                log_message.addSOAPParam("Soap Header", getMessageContext().getEnvelope().getHeader());
            } catch (OMException e) {
                //} catch (XMLStreamException e)
            }
        }

        // Log SOAP envelope:
        if (getMessageContext().getEnvelope().getBody() != null) {
            try {
                log_message.addSOAPParam("Soap Envelope", getMessageContext().getEnvelope());
            } catch (OMException e) {
                //} catch (XMLStreamException e) {
            }
        }
        log_message.addHTTPParam(Fields.fromIpAddress, remoteIP);
        log_message.addHTTPParam(Fields.endpoint, getMessageContext().getTo().toString());

        return this.validateXUA(request);  // Make sure we are good with XUA.
    }

    /**
     *
     * @param request
     * @return
     */
    private OMElement validateXUA(OMElement request) {
        // Validate XUA constraints here:
        XServiceProvider xServiceProvider = new XServiceProvider(log_message);
        XServiceProvider.Status response;
        try {
            MessageContext messageCtx = this.getMessageContext();
            response = xServiceProvider.run(messageCtx);
        } catch (Exception ex) {  // Catch everything here!!!
            // We have to treat this as a failure to validate assertion
            String exText = "XUA SAML Validation Exception (ignoring request) " + ex.getMessage();
            log_message.addErrorParam("XUA:ERROR", exText);
            return this.endTransaction(
                    request, new XdsException(exText),
                    this.mActor, this.service_name);
        }
        if (response != XServiceProvider.Status.CONTINUE) {
            // The assertion has not been validated, discontinue with processing SOAP request.
            log_message.addErrorParam("XUA:ERROR", "XUA did not pass validation!");
            return this.endTransaction(request,
                    new XdsException("XUA did not pass validation!"),
                    this.mActor,
                    this.service_name);
        }
        return null;  // All is good.
    }

    /**
     *
     * @param status
     */
    protected void endTransaction(boolean status) {
        logger.info("End " + service_name + " " +
                ((log_message == null) ? "null" : log_message.getMessageID()) + " : " +
                ((status) ? "Pass" : "Fail"));
        stopTestLog();
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
        OMElement errorResult =  this.start_up_error(request, e, actor, message);
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
            String request_type = (request != null) ? request.getLocalName() : "None";
            OMNamespace ns = (request != null) ? request.getNamespace() : MetadataSupport.ebRSns2;

            if (ns.getNamespaceURI().equals(MetadataSupport.ebRSns3.getNamespaceURI())) {
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
     *
     */
    protected void startTestLog() {
        //logger.info("+++ start log [service = " + service_name + "] +++");
    }

    /**
     * Stop the test log facility.
     */
    protected void stopTestLog() {
        if (log_message != null) {
            log_message.store();
            log_message = null;
        }
        //logger.info("+++ stop log [service = " + service_name + "] +++");
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
    private void addHttp(String title, Vector<String> t) {
        StringBuffer buffer = new StringBuffer();
        for (String s : t) {
            buffer.append(s + "  ");
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
     * @throws com.vangent.hieos.xutil.exception.XdsWSException
     */
    protected void checkSOAP12() throws XdsWSException {
        if (MessageContext.getCurrentMessageContext().isSOAP11()) {
            throwFault("SOAP 1.1 not supported");
        }
        SOAPEnvelope env = MessageContext.getCurrentMessageContext().getEnvelope();
        if (env == null) {
            throwFault("No SOAP envelope found");
        }
        SOAPHeader hdr = env.getHeader();
        if (hdr == null) {
            throwFault("No SOAP header found");
        }
        if (!hdr.getChildrenWithName(new QName("http://www.w3.org/2005/08/addressing", "Action")).hasNext()) {
            throwFault("WS-Action required in header");
        }
    }

    /**
     *
     * @throws com.vangent.hieos.xutil.exception.XdsWSException
     */
    protected void checkSOAP11() throws XdsWSException {

        if (!MessageContext.getCurrentMessageContext().isSOAP11()) {
            throwFault("SOAP 1.2 not supported");
        }
        SOAPEnvelope env = MessageContext.getCurrentMessageContext().getEnvelope();
        if (env == null) {
            throwFault("No SOAP envelope found");
        }
    }

    /**
     *
     * @throws com.vangent.hieos.xutil.exception.XdsWSException
     */
    protected void checkSOAPAny() throws XdsWSException {
        if (MessageContext.getCurrentMessageContext().isSOAP11()) {
            checkSOAP11();
        } else {
            checkSOAP12();
        }
    }

    /**
     *
     * @param msg
     * @throws com.vangent.hieos.xutil.exception.XdsWSException
     */
    private void throwFault(String msg) throws XdsWSException {
        if (log_message != null) {
            log_message.addErrorParam("SOAPError", msg);
            log_message.addOtherParam("Response", "SOAPFault: " + msg);
            endTransaction(false);
        }
        throw new XdsWSException(msg);
    }

    /**
     *
     * @return
     */
    protected boolean isAsync() {
        MessageContext mc = getMessageContext();
        return mc.getMessageID() != null &&
                !mc.getMessageID().equals("") &&
                mc.getReplyTo() != null &&
                !mc.getReplyTo().hasAnonymousAddress();
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
     */
    public void init(ServiceContext serviceContext) throws AxisFault {
        //logger.info("XdsService:::init() - NOOP (not overridden)");
    }

    /**
     * This is called when Axis2 decides that it is finished with a particular instance
     * of the back-end service class. It allows classes to clean up resources.
     */
    public void destroy(ServiceContext serviceContext) {
        //logger.info("XdsService:::destroy() - NOOP (not overridden)");
    }

    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    public void startUp(ConfigurationContext configctx, AxisService service) {
        //logger.info("XdsService:::startUp() - NOOP (not overridden)");
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        //logger.info("XdsService:::shutDown() - NOOP (not overridden)");
    }

    /**
     *
     * @param actorType
     */
    public void ATNAlogStop(XATNALogger.ActorType actorType) {
        try {
            XATNALogger xATNALogger = new XATNALogger(XATNALogger.TXN_STOP, actorType);
            xATNALogger.performAudit(null, null, XATNALogger.OutcomeIndicator.SUCCESS);
        } catch (Exception e) {
            logger.error("Could not perform ATNA audit (stop)", e);
        }
    }

    /**
     *
     * @param actorType
     */
    public void ATNAlogStart(XATNALogger.ActorType actorType) {
        try {
            XATNALogger xATNALogger = new XATNALogger(XATNALogger.TXN_START, actorType);
            xATNALogger.performAudit(null, null, XATNALogger.OutcomeIndicator.SUCCESS);
        } catch (Exception e) {
            logger.error("Could not perform ATNA audit (start)", e);
        }
    }
}
