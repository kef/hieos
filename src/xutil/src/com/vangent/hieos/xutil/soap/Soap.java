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
package com.vangent.hieos.xutil.soap;

import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsFormatException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xml.Util;

import java.util.HashMap;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;

import com.vangent.hieos.xutil.xconfig.XConfig;
import java.util.List;
import org.apache.axis2.engine.Phase;
import com.vangent.hieos.xutil.xua.handlers.XUAOutPhaseHandler;
import com.vangent.hieos.xutil.xua.utils.XUAObject;
import java.util.Iterator;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.log4j.Logger;

/**
 * Main point of making SOAP requests via AXIS2.
 *
 * @author Bernie Thuman (adapted from original NIST code).
 */
public class Soap {

    private final static Logger logger = Logger.getLogger(Soap.class);
    // Default values (if XConfig is not available).
    private static final long DEFAULT_ASYNC_TIMEOUT_MSEC = 120000;  // 120 secs.
    private static final long DEFAULT_SYNC_TIMEOUT_MSEC = 45000;    // 45 secs.
    private static final String DEFAULT_ASYNC_RESPONSE_PORT = "8080";
    // XConfig propertie names.
    private static final String XCONFIG_PARAM_ASYNC_TIMEOUT_MSEC = "SOAPAsyncTimeOutInMilliseconds";
    private static final String XCONFIG_PARAM_SYNC_TIMEOUT_MSEC = "SOAPtimeOutInMilliseconds";
    private static final String XCONFIG_PARAM_ASYNC_RESPONSE_PORT = "SOAPAsyncResponseHTTPPort";
    // Axis2 property names.
    private static final String AXIS2_PARAM_RUNNING_PORT = "RUNNING_PORT";
    private static final String XUA_OUT_PHASE_NAME = "XUAOutPhase";
    // Private variables:
    private XUAObject xuaObject = null;             // Only used if XUA is enabled (null if not used).
    private ServiceClient serviceClient = null;     // Cached Axis2 ServiceClient.
    private OMElement result = null;                // Holds the SOAP result.
    private boolean async = false;                  // Boolean value (determines "async" mode).

    /**
     * Set boolean value to determine if this request should be an asynchronous
     * SOAP request.
     *
     * @param async Set to true if asynchronous.  Otherwise, set to false.
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * Sets the XUA object used to properly generate SAML during Axis2 outbound
     * message handling.
     *
     * @param xuaObj The XUAObject.
     */
    public void setXUAObject(XUAObject xuaObj) {
        this.xuaObject = xuaObj;
    }

    /**
     * Issues a SOAP call to the target endpoint.
     *
     * @param body The SOAP body to send.
     * @param endpoint The target endpoint of the request.
     * @param mtom Set to true if MTOM should be enabled.  Otherwise, false.
     * @param addressing Set to true if SOAP addressing should be enabled.  Otherwise, false.
     * @param soap12 Set to true if SOAP 1.2 should be enabled.  Otherwise, false.
     * @param action The SOAP action associated with the request.
     * @param expectedReturnAction The expected SOAP return action.
     * @return The SOAP body of the result.
     * @throws XdsException
     */
    public OMElement soapCall(OMElement body, String endpoint, boolean mtom,
            boolean addressing, boolean soap12, String action, String expectedReturnAction)
            throws XdsException {
        try {
            // Get the AXIS2 ServiceClient.
            if (this.serviceClient == null) {
                this.serviceClient = new ServiceClient();
            }
            // Setup ServiceClient options.
            Options options = this.serviceClient.getOptions();
            this.setTargetEndpoint(options, endpoint);
            this.setMTOMOption(options, mtom);
            this.setTimeOutFromConfig(options);
            this.setSOAPAction(options, action);
            this.setAddressing(this.serviceClient, addressing);
            this.setSOAPVersion(options, soap12);

            // Configure for "async" mode (if required).
            if (this.async) {
                if (!options.isUseSeparateListener()) {
                    options.setUseSeparateListener(this.async);
                }
                // Now, check to see if the request is simply "http".
                if (endpoint.startsWith("http://")) {
                    this.setAsyncResponsePort();
                    // NOTE: HTTPS is handled via "axis2.xml" configuration.
                }
            }

            // Setup for XUA (if required).
            this.setupXUAOutPhaseHandler();

            // Make the SOAP request (and save the result).
            this.result = serviceClient.sendReceive(body);

            // Cleanup after "async" (if required).
            if (this.async) {
                serviceClient.cleanupTransport();
            }

            // Make sure response is what is expected.
            this.validateSOAPResponse(this.serviceClient, mtom);

            // Validate proper return action is received.
            if (this.async) {
                verifySOAPReturnAction(expectedReturnAction, "urn:mediateResponse");
            } else {
                verifySOAPReturnAction(expectedReturnAction, null);
            }

            // Return the SOAP result.
            return this.result;
        } catch (AxisFault e) {
            // If an AxisFault ... turn into an XdsException and get out.
            throw new XdsException(e.getMessage());
        }
    }

    /**
     * Returns the result of the SOAP request.
     *
     * @return The SOAP body response.
     */
    public OMElement getResult() {
        return result;
    }

    /**
     * Returns a deep copy of the SOAP "in" header.
     *
     * @return A deep copy of the SOAP "in" header.
     * @throws XdsInternalException
     */
    public OMElement getInHeader() throws XdsInternalException {
        OperationContext oc = serviceClient.getLastOperationContext();
        HashMap<String, MessageContext> ocs = oc.getMessageContexts();
        MessageContext in = ocs.get("In");
        if (in == null) {
            return null;
        }
        if (in.getEnvelope() == null) {
            return null;
        }
        if (in.getEnvelope().getHeader() == null) {
            return null;
        }
        return Util.deep_copy(in.getEnvelope().getHeader());
    }

    /**
     * Returns a deep copy of the SOAP "out" header.
     *
     * @return A deep copy of the SOAP "out" header.
     * @throws XdsInternalException
     */
    public OMElement getOutHeader() throws XdsInternalException {
        OperationContext oc = serviceClient.getLastOperationContext();
        HashMap<String, MessageContext> ocs = oc.getMessageContexts();
        MessageContext out = ocs.get("Out");
        if (out == null) {
            return null;
        }
        return Util.deep_copy(out.getEnvelope().getHeader());
    }

    /**
     * Set the target endpoint.
     *
     * @param options Axis2 ServiceClient options.
     * @param endpoint The target http/https endpoint.
     */
    private void setTargetEndpoint(Options options, String endpoint) {
        options.setTo(new EndpointReference(endpoint));
    }

    /**
     * Set the MTOM option (true or false).
     *
     * @param options Axis2 ServiceClient options.
     * @param mtom true if MTOM should be enabled.  Otherwise, false.
     */
    private void setMTOMOption(Options options, boolean mtom) {
        options.setProperty(Constants.Configuration.ENABLE_MTOM,
                ((mtom) ? Constants.VALUE_TRUE : Constants.VALUE_FALSE));
    }

    /**
     * Set the "time out" for the request.  Pulled from XConfig if available; otherwise,
     * sets to default values.
     *
     * @param options Axis2 ServiceClient options.
     */
    private void setTimeOutFromConfig(Options options) {
        // Set defaults first in case configuration is not available.
        long timeOut;
        if (this.async) {
            timeOut = Soap.DEFAULT_ASYNC_TIMEOUT_MSEC;
        } else {
            timeOut = Soap.DEFAULT_SYNC_TIMEOUT_MSEC;
        }
        try {
            XConfig cfg = XConfig.getInstance();
            if (this.async) {
                timeOut = cfg.getHomeCommunityPropertyAsLong(Soap.XCONFIG_PARAM_ASYNC_TIMEOUT_MSEC);
            } else {
                timeOut = cfg.getHomeCommunityPropertyAsLong(Soap.XCONFIG_PARAM_SYNC_TIMEOUT_MSEC);
            }
        } catch (Exception e) {
            logger.warn("Unable to get SOAP timeout from XConfig -- using default");
        }
        options.setTimeOutInMilliSeconds(timeOut);
    }

    /**
     * Set the SOAP action to use in the request.
     *
     * @param options Axis2 ServiceClient options.
     * @param action The SOAP action.
     */
    private void setSOAPAction(Options options, String action) {
        options.setAction(action);
    }

    /**
     * Engage (or desengage) Axis2 SOAP "addressing".
     *
     * @param sc The Axis2 ServiceClient.
     * @param addressing true if SOAP addressing should be used.  Otherwise, false.
     * @throws AxisFault
     */
    private void setAddressing(ServiceClient sc, boolean addressing) throws AxisFault {
        if (addressing) {
            sc.engageModule("addressing");
        } else {
            sc.disengageModule("addressing");    // this does not work in Axis2 yet
        }
    }

    /**
     * Set the SOAP version to use.
     *
     * @param options Axis2 ServiceClient options.
     * @param soap12 true if SOAP1.2 should be used.  false if SOAP1.1.
     */
    private void setSOAPVersion(Options options, boolean soap12) {
        options.setSoapVersionURI(
                ((soap12) ? SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI : SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));

    }

    /**
     * Configure Axis2 port to use for asynchronous SOAP responses.  Should set in
     * XConfig (a default port will be used if not found) to match the HTTP listener
     * port being used by the application server.
     */
    private void setAsyncResponsePort() {
        // Get default in case configuration is not set.
        String responsePort = Soap.DEFAULT_ASYNC_RESPONSE_PORT;

        // Now, set the proper listening port.
        ConfigurationContext ctx = this.serviceClient.getServiceContext().getConfigurationContext();
        if (ctx.getProperty(Soap.AXIS2_PARAM_RUNNING_PORT) == null) {
            try {
                XConfig cfg = XConfig.getInstance();
                responsePort = cfg.getHomeCommunityProperty(Soap.XCONFIG_PARAM_ASYNC_RESPONSE_PORT);
            } catch (Exception e) {
                logger.warn("Unable to get " + Soap.XCONFIG_PARAM_ASYNC_RESPONSE_PORT + " from XConfig -- using default");
            }
            ctx.setProperty(Soap.AXIS2_PARAM_RUNNING_PORT, responsePort);
        }
    }

    /**
     * Validate that if the inbound request was MTOM, the result is also.
     *
     * @param sc The ServiceClient used to support SOAP request.
     * @param mtomExpected Set to true if MTOM is expected.
     * @throws XdsFormatException
     */
    private void validateSOAPResponse(ServiceClient sc, boolean mtomExpected) throws XdsFormatException, XdsInternalException {
        Object in = sc.getServiceContext().getLastOperationContext().getMessageContexts().get("In");
        if (!(in instanceof MessageContext)) {
            throw new XdsInternalException("Soap: In MessageContext of type " + in.getClass().getName() + " instead of MessageContext");
        }
        MessageContext messageContext = (MessageContext) in;
        boolean responseMtom = messageContext.isDoingMTOM();
        if (mtomExpected != responseMtom) {
            if (mtomExpected) {
                throw new XdsFormatException("Request was MTOM format but response was SIMPLE SOAP");
            } else {
                throw new XdsFormatException("Request was SIMPLE SOAP but response was MTOM");
            }
        }
    }

    /**
     * Verify that the the SOAP response includes the extected return SOAP action.
     *
     * @param expectedReturnAction Expected SOAP return action.
     * @param alternateReturnAction Alternative expected SOAP return action.
     * @throws XdsException
     */
    private void verifySOAPReturnAction(String expectedReturnAction, String alternateReturnAction) throws XdsException {
        if (expectedReturnAction == null) {
            return;  // None expected.
        }
        // First see if a SOAP header exists.
        OMElement soapHeader = this.getInHeader();
        if (soapHeader == null) {
            throw new XdsInternalException(
                    "No SOAPHeader returned: expected header with action = " + expectedReturnAction);
        }

        // Now see if the SOAP action exists.
        OMElement action = MetadataSupport.firstChildWithLocalName(soapHeader, "Action");
        if (action == null) {
            throw new XdsInternalException(
                    "No action returned in SOAPHeader: expected action = " + expectedReturnAction);
        }

        // Now get the SOAP action value and compare against expected results.
        String soapActionValue = action.getText();
        if (alternateReturnAction == null) {
            if (soapActionValue == null || !soapActionValue.equals(expectedReturnAction)) {
                throw new XdsInternalException(
                        "Wrong action returned in SOAPHeader: expected action = " + expectedReturnAction +
                        " returned action = " + soapActionValue);
            }
        } else {
            if (soapActionValue == null ||
                    ((!soapActionValue.equals(expectedReturnAction)) && (!soapActionValue.equals(alternateReturnAction)))) {
                throw new XdsInternalException(
                        "Wrong action returned in SOAPHeader: expected action = " + expectedReturnAction +
                        " returned action = " + soapActionValue);
            }
        }
    }

    /**
     * Sets the XUA "Out Phase Handler" (if XUA is enabled).
     */
    private void setupXUAOutPhaseHandler() {
        if ((this.xuaObject != null) && (this.xuaObject.isXUAEnabled())) {
            List outFlowPhases = serviceClient.getAxisConfiguration().getOutFlowPhases();
            // Check to see if the out phase handler already exists
            for (Iterator it = outFlowPhases.iterator(); it.hasNext();) {
                Phase phase = (Phase) it.next();
                if (phase.getName().equals(XUA_OUT_PHASE_NAME)) {
                    // Already exists.
                    return;  // EARLY EXIT!
                }
            }
            logger.info("Adding XUA out phase handler!!!");
            Phase xuaOutPhase = this.getXUAOutPhaseHandler();
            outFlowPhases.add(xuaOutPhase);
        }
    }

    /**
     * Sets the XUA "Out Phase Handler".
     * 
     * @return Axis2 Phase (XUAOutPhaseHandler).
     */
    private Phase getXUAOutPhaseHandler() {
        Phase phase = null;
        try {
            phase = new Phase(XUA_OUT_PHASE_NAME);
            XUAOutPhaseHandler xuaOutPhaseHandler = new XUAOutPhaseHandler();
            xuaOutPhaseHandler.setXUAObject(this.xuaObject);
            phase.addHandler(xuaOutPhaseHandler);

        } catch (Throwable t) {
            logger.error("Exception while initializing the XUA out phase handler", t);
            t.printStackTrace();
        }
        return phase;
    }
}
