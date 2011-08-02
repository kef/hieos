/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.sts.transactions;

import com.vangent.hieos.authutil.framework.AuthenticationService;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.vangent.hieos.services.sts.model.STSRequestData;
import com.vangent.hieos.services.sts.model.SOAPHeaderData;
import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.services.sts.model.STSConstants;
import com.vangent.hieos.services.sts.util.STSUtil;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

//<wst:RequestSecurityToken xmlns:wst="http://docs.oasis-open.org/ws-sx/ws-trust/200512">
//         <wst:RequestType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue</wst:RequestType>
//         <wsp:AppliesTo xmlns:wsp="http://schemas.xmlsoap.org/ws/2004/09/policy">
//            <wsa:EndpointReference xmlns:wsa="http://www.w3.org/2005/08/addressing">
//               <wsa:Address>http://www.vangent.com/X-ServiceProvider-HIEOS</wsa:Address>
//            </wsa:EndpointReference>
//         </wsp:AppliesTo>
//         <wst:Claims Dialect="urn:oasis:names:tc:xspa:1.0:claims" xmlns:wst="http://docs.oasis-open.org/ws-sx/ws-trust/200512">
//            <xspa:ClaimType xmlns:xspa="urn:oasis:names:tc:xspa:1.0:claims" Uri="urn:oasis:names:tc:xacml:1.0:subject:subject-id">
//		<xspa:ClaimValue>Joe Smith</xspa:ClaimValue>
//	    </xspa:ClaimType>
//	    <xspa:ClaimType xmlns:xspa="urn:oasis:names:tc:xspa:1.0:claims" Uri="urn:oasis:names:tc:xspa:1.0:subject:purposeofuse">
//		<xspa:ClaimValue>TREATMENT</xspa:ClaimValue>
//	    </xspa:ClaimType>
//	    <xspa:ClaimType xmlns:xspa="urn:oasis:names:tc:xspa:1.0:claims" Uri="urn:oasis:names:tc:xspa:1.0:subject:organization-id">
//		<xspa:ClaimValue>HIP-O</xspa:ClaimValue>
//	    </xspa:ClaimType>
//         </wst:Claims>
//	 <!--<wst:KeyType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/SymmetricKey</wst:KeyType>-->
//      </wst:RequestSecurityToken>
/**
 *
 * @author Bernie Thuman
 */
public class STSRequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(STSRequestHandler.class);

    /**
     *
     */
    private STSRequestHandler() {
        // Do nothing.
    }

    /**
     * 
     * @param log_message
     * @param mCtx
     */
    public STSRequestHandler(XLogMessage log_message, MessageContext mCtx) {
        this.log_message = log_message;
        this.init(null, mCtx);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean getStatus() {
        return log_message.isPass();
    }

    /**
     *
     * @param request
     * @return
     * @throws SOAPFaultException
     */
    public OMElement run(OMElement request) throws SOAPFaultException {
        OMElement result = null;
        log_message.setPass(true);  // Hope for the best.

        // First parse and validate the SOAP header.
        MessageContext mCtx = this.getMessageContext();
        STSRequestData requestData = new STSRequestData(this.getConfigActor(), mCtx, request);
        try {
            requestData.parseHeader();
        } catch (STSException ex) {
            throw new SOAPFaultException(ex.getMessage());
        }

        // Authenticate user (for Issue requests).
        String soapAction = requestData.getSoapAction();
        if (soapAction.equalsIgnoreCase(STSConstants.WSTRUST_ISSUE_ACTION)) {
            boolean authenticated = false;
            try {
                authenticated = this.authenticate(requestData);
            } catch (STSException ex) {
                throw new SOAPFaultException(ex.getMessage());
            }
            if (!authenticated) {
                throw new SOAPFaultException("User not authenticated");
            }
        }

        // Now, either process an "Issue" or "Validate" token request.
        try {
            requestData.parseBody();
            String requestType = requestData.getRequestType();
            if (requestType.equalsIgnoreCase(STSConstants.ISSUE_REQUEST_TYPE)) {
                result = this.processIssueTokenRequest(requestData);
            } else if (requestType.equalsIgnoreCase(STSConstants.VALIDATE_REQUEST_TYPE)) {
                result = this.processValidateTokenRequest(requestData);
            } else {
                throw new STSException("RequestType not understood by this service!");
            }
        } catch (STSException ex) {
            throw new SOAPFaultException(ex.getMessage());
        }
        if (log_message.isLogEnabled()) {
            log_message.addOtherParam("Response", result);
        }
        return result;
        //return (result != null) ? result.getMessageNode() : null;
    }

    /**
     *
     * @param requestData
     * @return
     * @throws STSException
     */
    private OMElement processIssueTokenRequest(STSRequestData requestData) throws STSException {
        SAML2TokenIssueHandler handler = new SAML2TokenIssueHandler(this.log_message);
        return handler.handle(requestData);
    }

    /**
     * 
     * @param requestData
     * @return
     * @throws STSException
     */
    private OMElement processValidateTokenRequest(STSRequestData requestData) throws STSException {
        SAML2TokenValidateHandler handler = new SAML2TokenValidateHandler(this.log_message);
        return handler.handle(requestData);
    }

    /**
     *
     * @param requestData
     * @return
     * @throws STSException
     */
    private boolean authenticate(STSRequestData requestData) throws STSException {
        STSConfig stsConfig = requestData.getSTSConfig();
        boolean authenticated = false;
        SOAPHeaderData headerData = requestData.getHeaderData();
        if (headerData.getAuthenticationType() == STSConstants.AuthenticationType.USER_NAME_TOKEN) {
            String userName = headerData.getUserName();
            String userPassword = headerData.getUserPassword();
            AuthenticationService authService = new AuthenticationService(this.getConfigActor());
            Credentials authCredentials = new Credentials(userName, userPassword);
            AuthenticationContext authCtxt = authService.authenticate(authCredentials);
            authenticated = authCtxt.hasSuccessStatus();
            if (authenticated == true) {
                requestData.setSubjectDN(authCtxt.getUserProfile().getDistinguishedName());
            }
        } else {
            // Assume BinarySecurityToken.
            X509Certificate certificate = headerData.getClientCertificate();
            KeyStore trustStore = STSUtil.getTrustStore(stsConfig);
            // try {
            log_message.addOtherParam("Client X509 Certificate", certificate);
            STSUtil.validateCertificate(certificate, trustStore);
            requestData.setSubjectDN(certificate.getSubjectX500Principal().getName());
            authenticated = true;
            //} catch (STSException ex) {
            //    System.out.println("Client Certificate not valid: " + ex.getMessage());
            // }
        }
        return authenticated;
    }
}
