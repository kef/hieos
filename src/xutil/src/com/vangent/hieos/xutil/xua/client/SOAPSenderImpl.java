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
package com.vangent.hieos.xutil.xua.client;

import com.vangent.hieos.xutil.exception.XdsException;
import java.net.URI;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.log4j.Logger;

/**
 * This class uses the Axis2 API to send the SOAP message request to the specified target
 * Endpoint. Additionally it includes to initialize the under line HTTP client and also
 * it creates the messageContext used to be sent by operation client and receives the response
 * message context from the operation client. The message context contains request/response
 * SOAP envelope messages. The implementation of the send method used to send and receive the
 * SOAP messages to/from the target endpoint.
 *
 * @author Fred Aabedi / Bernie Thuman
 */
public class SOAPSenderImpl {

    private final static Logger logger = Logger.getLogger(SOAPSenderImpl.class);
    private Options mOptions;
    private ServiceClient mServiceClient;
    private SOAPFactory mSoapFactory;

    /**
     * 
     * @throws AxisFault
     */
    public SOAPSenderImpl() throws XdsException {
        try {
            // Prepare axis2 to be able act as a SOAP client.
            mOptions = new Options();
            mServiceClient = new ServiceClient();
            mServiceClient.setOptions(mOptions);
            mSoapFactory = OMAbstractFactory.getSOAP12Factory();
        } catch (AxisFault ex) {
            throw new XdsException(
                    "XUA:Exception: Failure initializing SOAP/STS client - " + ex.getMessage());
        }
    }

    /**
     * Sends a SOAP Envelope to a Web service target endpoint and receives the response
     * SOAP envelope from that invoked service.
     * 
     * @param endpointURI a URL for the Service endpoint
     * @param message A well-formed SOAP Envelope to send
     * @param action SOAP Action to use for the transaction
     * @return responseEnvelope A well-formed SOAP Envelope representing the response
     * @throws XdsException, handling exception.
     */
    public SOAPEnvelope send(URI endpointURI, SOAPEnvelope message, String action) throws XdsException {
        if (logger.isDebugEnabled()) {
            logger.debug("SOAP Envelope: " + message.toString());
        }
        try {
            // To send the SOAPEnvelope message to target Service endpoint
            // 1. Create message context, 2. send the message context
            // to the target service endpoint
            MessageContext reqMessageContext = this.constructMessageContext(endpointURI, message, action);
            // send the MC to target endpoint.
            MessageContext resMessageContext = this.sendToTargetEndpoint(reqMessageContext);

            // get response SOAP Envelope from the response MC
            SOAPEnvelope responseEnvelope = resMessageContext.getEnvelope();
            if (responseEnvelope != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("SOAP Response: " + responseEnvelope.toString());
                }
            } else {
                logger.info("No SOAP Response!!!!!");
            }

            return responseEnvelope;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("XUA EXCEPTION: " + ex.getMessage());
            throw new XdsException("XUA:Exception: Failure contacting STS - " + ex.getMessage());
        }
    }

    /**
     * Contruct empty envelope
     * @return envelop;
     */
    public SOAPEnvelope createEmptyEnvelope() {
        SOAPEnvelope envelope = mSoapFactory.createSOAPEnvelope();
        mSoapFactory.createSOAPBody(envelope);
        mSoapFactory.createSOAPHeader(envelope);
        envelope.build();
        return envelope;
    }

    /**
     * Construct the message Context, by setting up the propeties like SOAP envelope
     * ,Options, SOAP action and Endpoint Reference.
     *
     * @param endpoint a URI for the Service endpoint
     * @param envelope A well-formed SOAP Envelope to send
     * @param action SOAP Action to use for the transaction
     * @return messageContext message Context
     * @throws Exception, handling exceptions
     */
    private MessageContext constructMessageContext(URI endpoint, SOAPEnvelope envelope, String action) throws AxisFault {
        // Create MC
        MessageContext messageContext = new MessageContext();
        // set envelope
        messageContext.setEnvelope(envelope);
        // set options
        messageContext.setOptions(mOptions);
        // set SOAP action
        messageContext.getOptions().setAction(action);
        // set MTOM property false
        //messageContext.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_FALSE);
        // set SWA property to false
        //messageContext.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_FALSE);
        // set HTTP CHUNKED to false, not required now
        //messageContext.setProperty(HTTPConstants.CHUNKED, "false");

        // Set the endpoint reference to the MC
        messageContext.getOptions().setTo(new EndpointReference(endpoint.toString()));

        // set the service Client Context to MC
        messageContext.setServiceContext(mServiceClient.getServiceContext());

        return messageContext;
    }

    /**
     *  Send the MessageContext to the specified target endpoint, by calling
     *  execute method on Operation Client.
     * 
     * @param messageContext used to send
     * @return resMessageContext, response msg context contains out message
     * @throws Exception, handling exceptions
     */
    private MessageContext sendToTargetEndpoint(MessageContext messageContext) throws AxisFault {
        MessageContext resMessageContext = null;

        // create In-Out operation service client
        OperationClient operationClient = mServiceClient.createClient(ServiceClient.ANON_OUT_IN_OP);

        // add MC to the Operation client
        operationClient.addMessageContext(messageContext);

        // call for execute
        operationClient.execute(true);

        resMessageContext = operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        return resMessageContext;
    }
}
