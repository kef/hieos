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
package com.vangent.hieos.xutil.xua.handlers;

import com.vangent.hieos.xutil.xua.utils.XUAObject;
import com.vangent.hieos.xutil.xua.client.XServiceUser;
import com.vangent.hieos.xutil.xua.utils.XUAConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.handlers.AbstractHandler;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.log4j.Logger;

/**
 * Axis2 Outphase handler for attaching assertions to outbound 
 * SOAP messages as per the XUA profile.It will be inoked by axis2 engine before
 * making client soap call.
 * The Out Phase handler checks to verify XUA is enabled or not.  If it is enabled,
 * then it checks to see if the transaction is in the list of SOAP Actions for which
 * XUA is enabled. If so, then it attempts to get SAML assertion from STS and attach
 * it, via a WS-Security header wrapper into the outbound request.  If any exception
 * occurs when getting the assertion from STS and attempting to attach the assertion,
 * then the outbound request is aborted. 
 *
 * @author Fred Aabedi
 */
public class XUAOutPhaseHandler extends AbstractHandler {

    private final static Logger logger = Logger.getLogger(XUAOutPhaseHandler.class);
    private XUAObject xuaObject = null;

    /**
     * Constructor
     */
    public XUAOutPhaseHandler() {
    }

    public void setXUAObject(XUAObject xuaObj) {
        this.xuaObject = xuaObj;
    }

    /**
     * Overriden method invoked by axis2 engine before soap call to target endpoint.
     *
     * @param messageContext messageContext contains the soap envelope
     * @return InvocationResponse, invocation my be continue or abort
     * @throws AxisFault throws AxisFault
     */
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        logger.debug("XUA: XUAOutPhaseHandler::invoke");
        // check tSystem.outo see the received soap action is the XUA supported action or not
        // Condition check is required since the OutPhaseHandler will be invoked even for XUAServiceUser soap actions.
        // i.e IssueToken
        // check to see the received soap action is the XUA supported action or not
        if (this.xuaObject == null) {
            // Just to be safe, get out!
            return InvocationResponse.CONTINUE;
        }
        // Note (BHT): the xuaObject has likely been initalized from the test client.  If you want
        // to use the XUAOutPhaseHandler on the server, there is a little more work to do.
        if (!this.xuaObject.containsSOAPAction(messageContext.getSoapAction())) {
            return InvocationResponse.CONTINUE;
        }
        // also skip the SOAP call to STS if token already exists in the request.
        if (this.assertionExistsInRequest(messageContext)) {
            logger.debug("Assertion already exists on the request!!!");
            return InvocationResponse.CONTINUE;
        }

        // Continue to get the assertion from the STS
        XServiceUser xServiceUser = new XServiceUser();
        try {
            String stsUrl = this.xuaObject.getSTSUrl();
            String serviceUri = this.xuaObject.getSTSUri();
            String userName = this.xuaObject.getUserName();
            String password = this.xuaObject.getPassword();
            logger.debug("XUA: XUAOutPhaseHandler::invoke - stsUrl: " + stsUrl);
            logger.debug("XUA: XUAOutPhaseHandler::invoke - serviceUri: " + serviceUri);
            logger.debug("XUA: XUAOutPhaseHandler::invoke - userName: " + userName);
            logger.debug("XUA: XUAOutPhaseHandler::invoke - password: " + password);


            // Get the SAML assertion from the STS provider (for the given user):
            SOAPEnvelope responseEnvelope = xServiceUser.getSOAPResponseFromSts(
                    stsUrl, serviceUri, userName, password, this.xuaObject.getClaims());
            if (logger.isDebugEnabled()) {
                logger.debug("XUA: XUAOutPhaseHandler::invoke - STS Response: " + responseEnvelope.toString());
            }
            OMElement samlTokenEle = xServiceUser.getTokenFromResSOAPEnvelope(responseEnvelope);
            if (logger.isDebugEnabled()) {
                logger.debug("XUA: XUAOutPhaseHandler::invoke - SAML Token: " + samlTokenEle.toString());
            }

            // Get the SOAP envelope from the message context
            SOAPEnvelope requestEnvelope = messageContext.getEnvelope();

            // Create WS-Security wrapper element
            OMNamespace wsseNS = requestEnvelope.getOMFactory().createOMNamespace(XUAConstants.WS_SECURITY_NS_URL, XUAConstants.WS_SECURITY_NS_PREFIX);
            OMElement wsseSecurityHeader = requestEnvelope.getOMFactory().createOMElement(XUAConstants.WS_SECURITY_ELEMENT_NAME, wsseNS);

            // Attach assertion to wrapper and 
            wsseSecurityHeader.addChild(samlTokenEle);
            // Attach wrapper to SOAP message
            requestEnvelope.getHeader().addChild(wsseSecurityHeader);

        } catch (Exception ex) {
            logger.info("Unable to invoke STS to get SAML token" + ex.getLocalizedMessage());
            return InvocationResponse.ABORT;
        }
        return InvocationResponse.CONTINUE;
    }

    /**
     * Check SMAL token already exists in the Request.
     * @param mc, messageContext mc
     * @return found , bolean value true or false
     */
    private boolean assertionExistsInRequest(MessageContext mc) {
        boolean found = false;
        OMElement assertionEle = null;
        try {
            SOAPEnvelope envelope = mc.getEnvelope();
            // Verify the request header is not null
            SOAPHeader requestHeader = envelope.getHeader();
            if (requestHeader == null) {
                logger.error("Response header should not be null");
            }
            // Get Response Element
            Iterator ite = envelope.getHeader().getChildrenWithName(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                    "Security"));

            while (ite.hasNext()) {
                OMElement securityEle = (OMElement) ite.next();
                assertionEle = securityEle.getFirstChildWithName(new QName("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (assertionEle != null) {
            found = true;
        }
        return found;
    }
}
