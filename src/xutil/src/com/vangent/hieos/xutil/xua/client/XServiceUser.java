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

import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.template.TemplateUtil;
import com.vangent.hieos.xutil.xml.XPathHelper;
import com.vangent.hieos.xutil.xua.utils.XUAConstants;
import com.vangent.hieos.xutil.xua.utils.XUAObject;
import com.vangent.hieos.xutil.xua.utils.XUAUtil;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.log4j.Logger;

/**
 * X-Service User - System making a service request of an X-Service Provider,
 * It has a responsibility to communicate with X-Assertion provider to get the SAML
 * token for user authentication. It initializes the X-ServiceUserClient which constructs
 * the SOAP message and send it to STS.
 * 
 * @author Fred Aabedi / Bernie Thuman
 */
public class XServiceUser {

    private final static Logger logger = Logger.getLogger(XServiceUser.class);

    /**
     * Constructor
     */
    public XServiceUser() {
    }

    /**
     * Build WS-Trust request body.
     *
     * @param serviceUri
     * @param claimsNode
     * @return
     * @throws SOAPFaultException
     */
    private OMElement buildTokenRequestBody(String serviceUri, OMElement claimsNode) throws SOAPFaultException {
        // Do template variable substitution.
        Map<String, String> templateVariableValues = new HashMap<String, String>();
        //if (userName != null) {
        //    templateVariableValues.put("USERNAME", userName);
        //}
        if (serviceUri != null) {
            templateVariableValues.put("SERVICE", serviceUri);
        }
        templateVariableValues.put("CLAIMS", claimsNode.toString());
        OMElement requestBodyElement = TemplateUtil.getOMElementFromTemplate(
                XUAConstants.WS_TRUST_TOKEN_REQUEST_BODY,
                templateVariableValues);

        return requestBodyElement;
    }

    /**
     * Construct WS-Trust request header element (using UserNameToken).
     *
     * @param userName user Name
     * @param password password
     * @return reqHeaderElement, converted DOM element
     * @throws SOAPFaultException, handling the exceptions
     */
    private OMElement buildUserNameTokenRequestHeader(String userName, String password) throws SOAPFaultException {
        // Do template variable substitution.
        Map<String, String> templateVariableValues = new HashMap<String, String>();
        if (userName != null) {
            templateVariableValues.put("USERNAME", userName);
        }
        if (password != null) {
            templateVariableValues.put("PASSWORD", password);
        }
        //if (serviceUri != null) {
        //    templateVariableValues.put("SERVICE", serviceUri);
        //}

        // Deal with CreatedTime and ExpiredTime.
        templateVariableValues.put("CREATEDTIME", XUAUtil.getCreatedTime());
        templateVariableValues.put("EXPIREDTIME", XUAUtil.getExpireTime());

        OMElement reqHeaderElement = TemplateUtil.getOMElementFromTemplate(
                XUAConstants.WS_TRUST_USERNAME_TOKEN_REQUEST_HEADER,
                templateVariableValues);

        return reqHeaderElement;
    }

    /**
     *
     * @param certBase64
     * @return
     * @throws SOAPFaultException
     */
    private OMElement buildBinaryTokenRequestHeader(String certBase64) throws SOAPFaultException {
        // Do template variable substitution.
        Map<String, String> templateVariableValues = new HashMap<String, String>();
        if (certBase64 != null) {
            templateVariableValues.put("CERT", certBase64);
        }

        // Deal with CreatedTime and ExpiredTime.
        templateVariableValues.put("CREATEDTIME", XUAUtil.getCreatedTime());
        templateVariableValues.put("EXPIREDTIME", XUAUtil.getExpireTime());

        OMElement reqHeaderElement = TemplateUtil.getOMElementFromTemplate(
                XUAConstants.WS_TRUST_BINARY_TOKEN_REQUEST_HEADER,
                templateVariableValues);

        return reqHeaderElement;
    }

    /**
     * Get token from STS.
     *
     * @param xuaObject Holds relevant data.
     * @return SOAPEnvelope holding response from STS.
     * @throws SOAPFaultException
     */
    public SOAPEnvelope getToken(XUAObject xuaObject) throws SOAPFaultException {
        String userName = xuaObject.getUserName();
        String password = xuaObject.getPassword();
        String serviceUri = xuaObject.getSTSUri();
        String stsUrl = xuaObject.getSTSUrl();
        String clientCert = xuaObject.getClientCertBase64Encoded();
        OMElement claimsNode = xuaObject.getClaims();
        if (logger.isDebugEnabled()) {
            logger.debug("XUA: XUAOutPhaseHandler::invoke - stsUrl: " + stsUrl);
            logger.debug("XUA: XUAOutPhaseHandler::invoke - serviceUri: " + serviceUri);
            logger.debug("XUA: XUAOutPhaseHandler::invoke - userName: " + userName);
            logger.debug("XUA: XUAOutPhaseHandler::invoke - password: " + password);
            logger.debug("XUA: XUAOutPhaseHandler::invoke - clientCert: " + clientCert);
        }
        if (serviceUri == null) {
            // Do we really need this?
            throw new SOAPFaultException(
                    "XUA:Exception: You must specify a service URI to use XUA.");
        }
        if (clientCert == null) {
            if (userName == null || password == null) {
                throw new SOAPFaultException(
                        "XUA:Exception: You must specify an X509 cert or (username/password) to use XUA.");
            }
        }
        //logger.info("---Getting the response Token from the STS---");
        if (logger.isDebugEnabled()) {
            logger.debug("---Getting the response Token from the STS---");
        }

        SOAPEnvelope response = null;
        OMElement elementBody, elementHeader;
        elementBody = this.buildTokenRequestBody(serviceUri, claimsNode);
        if (clientCert != null) {
            elementHeader = this.buildBinaryTokenRequestHeader(clientCert);
        } else {
            elementHeader = this.buildUserNameTokenRequestHeader(userName, password);
        }
        response = this.send(stsUrl, elementBody, elementHeader, XUAConstants.SOAP_ACTION_ISSUE_TOKEN);
        return response;
    }

    /**
     * Get SAML token from the response SOAP Envelope
     * @param envelope soap Envelope
     * @return tokenElement, SAML token element
     * @throws SOAPFaultException, handling the exceptions
     */
    public OMElement getTokenFromSTSResponse(SOAPEnvelope envelope) throws SOAPFaultException {
        if (envelope == null) {
            throw new SOAPFaultException("XUA:Exception: Failed to get the response");
        }
        // Verify the response body is not null
        SOAPBody responseBody = envelope.getBody();
        if (responseBody == null) {
            throw new SOAPFaultException("XUA:Exception: Response body should not be null");
        }
        OMElement resElement = null;
        try {
            resElement = XPathHelper.selectSingleNode(responseBody, "./ns:RequestSecurityTokenResponseCollection/ns:RequestSecurityTokenResponse/ns:RequestedSecurityToken[1]", "http://docs.oasis-open.org/ws-sx/ws-trust/200512");
        } catch (XPathHelperException ex) {
            throw new SOAPFaultException("XUA:Exception: Could not get assertion - " + ex.getMessage());
        }
        OMElement assertionEle = null;
        if (resElement != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("XUA: RequestedSecurityToken = " + resElement.toString());
            }
            assertionEle = resElement.getFirstChildWithName(new QName("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion"));
            if (assertionEle != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("XUA: SAML Assertion = " + assertionEle.toString());
                }
            } else {
                logger.error("XUA: SAML Assertion = NULL!!!!");
            }
        } else {
            logger.error("XUA: RequestedSecurityToken = NULL!!!!");
        }
        if (assertionEle == null) {
            throw new SOAPFaultException("XUA:Exception: Could not get assertion.");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Received SAML token from STS");
        }
        return assertionEle;
    }

    /**
     * Used to send the request to STS endpoint URL
     *
     * @param stsUrl stsUrl STS URL
     * @param requestBody, constracted WS-Trust Request Body
     * @param requestHeader, constracted WS-Trust Request Header
     * @param action SOAP action to be sent
     * @return responseEnvelope, reseponseEnvelope contains SAML assertion
     * @throws SOAPFaultException, handling the exceptions
     */
    private SOAPEnvelope send(String stsUrl, OMElement requestBody, OMElement requestHeader, String action) throws SOAPFaultException {
        // create empty envelope
        SOAPSenderImpl soapSender = new SOAPSenderImpl();
        SOAPEnvelope envelope = soapSender.createEmptyEnvelope();

        // Finalize body for send
        SOAPBody reqBody = envelope.getBody();
        reqBody.addChild(requestBody);

        SOAPHeaderBlock b;
        try {
            b = ElementHelper.toSOAPHeaderBlock(requestHeader, OMAbstractFactory.getSOAP12Factory());
        } catch (Exception ex) {
            throw new SOAPFaultException("XUA:Exception: Error creating header block" + ex.getMessage());
        }
        envelope.getHeader().addChild(b);

        // Send the soap message to the targeted endpoint uri
        SOAPEnvelope responseEnvelope;
        try {
            responseEnvelope = soapSender.send(new java.net.URI(stsUrl), envelope, action);
        } catch (URISyntaxException ex) {
            throw new SOAPFaultException("XUA:Exception: Could not interpret STS URL - " + ex.getMessage());
        }
        return responseEnvelope;
    }
}
