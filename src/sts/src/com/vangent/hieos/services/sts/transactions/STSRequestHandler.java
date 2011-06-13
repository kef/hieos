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
import com.vangent.hieos.services.sts.model.STSConstants;
import com.vangent.hieos.services.sts.model.STSRequestData;
import com.vangent.hieos.services.sts.model.SOAPHeaderData;
import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.services.sts.util.STSUtil;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
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
     * @param messageType
     * @return
     * @throws AxisFault
     */
    public OMElement run(OMElement request) throws AxisFault {
        OMElement result = null;
        log_message.setPass(true);  // Hope for the best.

        // First parse and validate that SOAP header.
        STSRequestData requestData;
        SOAPHeaderData headerData;
        MessageContext mCtx = this.getMessageContext();
        try {
            headerData = new SOAPHeaderData(STSConfig.getInstance());
            requestData = new STSRequestData();
            requestData.setHeaderData(headerData);  // attach for later use.
            headerData.parse(mCtx);
            System.out.println("SOAP Header - " + headerData.toString());
        } catch (STSException ex) {
            throw new AxisFault(ex.getMessage());
        }

        // Authenticate user (for Issue requests).
        String soapAction = headerData.getSoapAction();
        if (soapAction.equalsIgnoreCase(STSConstants.ISSUE_ACTION)) {
            boolean authenticated = false;
            try {
                authenticated = this.authenticate(requestData);
            } catch (STSException ex) {
                throw new AxisFault(ex.getMessage());
            }
            if (!authenticated) {
                throw new AxisFault("User not authenticated");
            }
        }

        // Now, either process an "Issue" or "Validate" token request.
        try {
            requestData.parse(request);
            System.out.println("STSRequestData - " + requestData.toString());
            String requestType = requestData.getRequestType();
            if (requestType.equalsIgnoreCase(STSConstants.ISSUE_REQUEST_TYPE)) {
                result = this.processIssueTokenRequest(requestData);
            } else if (requestType.equalsIgnoreCase(STSConstants.VALIDATE_REQUEST_TYPE)) {
                result = this.processValidateTokenRequest(requestData);
            } else {
                System.out.println("RequestType not understood by this service!");
            }
        } catch (STSException ex) {
            throw new AxisFault(ex.getMessage());
        }

        return result;
        //return (result != null) ? result.getMessageNode() : null;
    }

    /**
     *
     * @param requestData
     * @return
     */
    private OMElement processIssueTokenRequest(STSRequestData requestData) throws STSException {
        System.out.println("STS ISSUE action!");
        //this.runTest();
        STSConfig stsConfig = STSConfig.getInstance();
        SAML2TokenIssueHandler handler = new SAML2TokenIssueHandler(stsConfig);
        return handler.handle(requestData);
    }

    /**
     *
     * @param requestData
     * @return
     */
    private OMElement processValidateTokenRequest(STSRequestData requestData) throws STSException {
        System.out.println("STS VALIDATE action!");
        STSConfig stsConfig = STSConfig.getInstance();
        SAML2TokenValidateHandler handler = new SAML2TokenValidateHandler(stsConfig);
        return handler.handle(requestData);
    }

    /**
     *
     * @param securityHeaderData
     */
    private boolean authenticate(STSRequestData requestData) throws STSException {
        SOAPHeaderData headerData = requestData.getHeaderData();
        if (headerData.getAuthenticationType() == STSConstants.AuthenticationType.USER_NAME_TOKEN) {
            String userName = headerData.getUserName();
            String userPassword = headerData.getUserPassword();
            STSConfig stsConfig = STSConfig.getInstance();
            XConfigObject configObject = stsConfig.getConfigObject();
            AuthenticationService authService = new AuthenticationService(configObject);
            Credentials authCredentials = new Credentials(userName, userPassword);
            AuthenticationContext authCtxt = authService.authenticate(authCredentials);
            boolean authStatus = authCtxt.hasSuccessStatus();
            if (authStatus == true) {
                requestData.setPrincipal(authCtxt.getUserProfile().getDistinguishedName());
            }
            return authStatus;
        } else {
            // Assume BinarySecurityToken.
            X509Certificate certificate = headerData.getClientCertificate();
            KeyStore trustStore = STSUtil.getTrustStore(STSConfig.getInstance());
            try {
                STSUtil.validateCertificate(certificate, trustStore);
                requestData.setPrincipal(certificate.getSubjectX500Principal().getName());
            } catch (STSException ex) {
                System.out.println("Certificate not valid: " + ex.getMessage());
                return false;
            }
            return true;
        }
    }
}
