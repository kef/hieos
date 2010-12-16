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

import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.xml.XMLParser;
import com.vangent.hieos.xutil.xua.utils.XUAConstants;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * It has a resposiblity to communicate with X-Assertion provider to get the SAML
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
     * Construct Ws-Trust request body
     * 
     * @param userName username
     * @param serviceUri STS service URI
     * @return requestBodyElement, converted DOM element
     * @throws Exception, handling the exceptions
     */
    private OMElement constructWsTrustRequestBody(String userName, String serviceUri) throws XdsException {
        String requestBodyContent = XUAConstants.WS_TRUST_TOKEN_REQUEST_BODY;
        String bodyContent = requestBodyContent;
        if (userName != null) {
            bodyContent = this.substituteVariables("__USERNAME__", userName, bodyContent);
        }
        if (serviceUri != null) {
            bodyContent = this.substituteVariables("__SERVICE__", serviceUri, bodyContent);
        }
        OMElement requestBodyElement = null;
        try {
            requestBodyElement = XMLParser.stringToOM(bodyContent);
        } catch (XMLParserException ex) {
            throw new XdsException("XUA:Exception: Error creating Ws-Trust request body - " + ex.getMessage());
        }
        return requestBodyElement;
    }

    /**
     * Construct Ws-Trust request header element
     *
     * @param userName user Name
     * @param password password
     * @param serviceUri STS service uri
     * @return reqHeaderElement, converted DOM element
     * @throws Exception, handling the exceptions
     */
    private OMElement constructWsTrustRequestHeader(String userName, String password, String serviceUri) throws XdsException {
        String headerContent = XUAConstants.WS_TRUST_TOKEN_REQUEST_HEADER;
        String reqHeaderContent = headerContent;
        if (userName != null) {
            reqHeaderContent = this.substituteVariables("__USERNAME__", userName, reqHeaderContent);
        }
        if (password != null) {
            reqHeaderContent = this.substituteVariables("__PASSWORD__", password, reqHeaderContent);
        }

        if (serviceUri != null) {
            reqHeaderContent = this.substituteVariables("__SERVICE__", serviceUri, reqHeaderContent);
        }

        OMElement reqHeaderElement;
        try {
            reqHeaderElement = XMLParser.stringToOM(reqHeaderContent);
        } catch (XMLParserException ex) {
            throw new XdsException("XUA:Exception: Error creating Ws-Trust request header - " + ex.getMessage());
        }
        return reqHeaderElement;
    }

    /**
     * get the SOAP response from STS
     * @param stsUrl STS endpoint URl
     * @param serviceUri STS serivce Uri
     * @param userName
     * @param password
     * @return response, received SOAP envelope from STS
     * @throws Exception, handling the exceptions
     */
    public SOAPEnvelope getSOAPResponseFromSts(String stsUrl, String serviceUri, String userName, String password) throws XdsException {
        if (userName == null || password == null || serviceUri == null) {
            throw new XdsException(
                    "XUA:Exception: You must specify a username, password, and service URI to use XUA.");
        }
        logger.info("---Getting the response Token from the STS---");

        SOAPEnvelope response = null;
        OMElement elementBody, elementHeader;
        elementBody = this.constructWsTrustRequestBody(userName, serviceUri);
        elementHeader = this.constructWsTrustRequestHeader(userName, password, serviceUri);
        response = this.send(stsUrl, elementBody, elementHeader, XUAConstants.SOAP_ACTION_ISSUE_TOKEN);
        return response;
    }

    /**
     * Get SAML token from the response SOAP Envelope
     * @param envelope soap Envelope
     * @return tokenElement, SAML token element
     * @throws XdsException, handling the exceptions
     */
    public OMElement getTokenFromResSOAPEnvelope(SOAPEnvelope envelope) throws XdsException {
        if (envelope == null) {
            throw new XdsException("XUA:Exception: Failed to get the response");
        }
        // Verify the response body is not null
        SOAPBody responseBody = envelope.getBody();
        if (responseBody == null) {
            throw new XdsException("XUA:Exception: Response body should not be null");
        }
        // Get Response Element
        OMElement responseOMElement = envelope.getBody().getFirstElement();
        if (responseOMElement == null) {
            throw new XdsException("XUA:Exception: Response element should not be null");
        }
        OMElement assertionEle = null;
        do {
            OMElement resElement = responseOMElement.getFirstChildWithName(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512",
                    "RequestedSecurityToken"));
            if (resElement == null) {
                break;
            }
            assertionEle = resElement.getFirstChildWithName(new QName("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion"));
        } while (false);
        if (assertionEle == null) {
            throw new XdsException("XUA:Exception: Could not get assertion.");
        }
        logger.info("Received SAML token from STS");
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
     * @throws Exception, handling the exceptions
     */
    private SOAPEnvelope send(String stsUrl, OMElement requestBody, OMElement requestHeader, String action) throws XdsException {
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
            throw new XdsException("XUA:Exception: Error creating header block" + ex.getMessage());
        }
        envelope.getHeader().addChild(b);

        // Send the soap message to the targeted endpoint uri
        SOAPEnvelope responseEnvelope;
        try {
            responseEnvelope = soapSender.send(new java.net.URI(stsUrl), envelope, action);
        } catch (URISyntaxException ex) {
            throw new XdsException("XUA:Exception: Could not interpret STS URL - " + ex.getMessage());
        }
        return responseEnvelope;
    }

    /**
     *
     * @param varName
     * @param val
     * @param template
     * @return
     */
    private String substituteVariables(String varName, String val, String template) {
        Pattern pattern = Pattern.compile(varName, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(template);
        String newContent = matcher.replaceAll(val);
        return newContent;
    }
}
