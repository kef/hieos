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

import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.services.sts.model.STSRequestData;
import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.services.sts.model.STSConstants;
import com.vangent.hieos.services.sts.util.STSUtil;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.signature.KeyInfo;

/**
 *
 * @author Bernie Thuman
 */
public class SAML2TokenValidateHandler extends SAML2TokenHandler {

    static private final String LOG_TOKEN_STATUS_PARAM = "Token Status";

    //
    //   <wst:RequestSecurityToken xmlns:wst="http://docs.oasis-open.org/ws-sx/ws-trust/200512">
    //       <wst:TokenType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/RSTR/Status</wst:TokenType>
    //       <wst:RequestType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate</wst:RequestType>
    //       <wsp:AppliesTo xmlns:wsp="http://schemas.xmlsoap.org/ws/2004/09/policy">
    //          <wsa:EndpointReference xmlns:wsa="http://www.w3.org/2005/08/addressing">
    //             <wsa:Address>http://www.vangent.com/X-ServiceProvider-HIEOS</wsa:Address>
    //          </wsa:EndpointReference>
    //       </wsp:AppliesTo>
    //       <wst:ValidateTarget>
    //          <saml:Assertion ID="urn:uuid:435F64C1CB77B451CF1307132901718" IssueInstant="2011-06-03T20:28:21.698Z" Version="2.0" MajorVersion="1" MinorVersion="0" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" xmlns:xenc="http://www.w3.org/2001/04/xmlenc#">
    //          ...
    //          </saml:Assertion>
    //       </wst:ValidateTarget>
    //    </wst:RequestSecurityToken>
    //
    /**
     *
     * @param logMessage
     */
    public SAML2TokenValidateHandler(XLogMessage logMessage) {
        super(logMessage);
    }

    /**
     *
     * @param requestData
     * @return
     * @throws STSException
     */
    @Override
    protected OMElement handle(STSRequestData requestData) throws STSException {
        XLogMessage logMessage = this.getLogMessage();

        // Get Assertion (as OMElement) from requestData.
        OMElement assertionOMElement = this.getAssertionOMElement(requestData.getRequest());

        STSConfig stsConfig = requestData.getSTSConfig();

        // Get the TrustStore.
        KeyStore trustStore = STSUtil.getTrustStore(stsConfig);

        // Get the Assertion (as an OpenSAML object).
        Assertion assertion = this.getAssertion(assertionOMElement);

        // Conduct some prelimary validation of the signature.
        Signature signature;
        try {
            assertion.validate(true);
            signature = assertion.getSignature();
            SAMLSignatureProfileValidator pv = new SAMLSignatureProfileValidator();
            pv.validate(signature);

            // Validate conditions (if they exist).
            Conditions conditions = assertion.getConditions();
            if (conditions != null) {
                DateTime startDate = conditions.getNotBefore();
                if (startDate != null && startDate.isAfterNow()) {
                    logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Assertion not valid yet");
                    return this.getWSTrustResponse(false);
                }
                DateTime endDate = conditions.getNotOnOrAfter();
                if (endDate != null && (endDate.isBeforeNow() || endDate.isEqualNow())) {
                    logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Assertion expired");
                    return this.getWSTrustResponse(false);
                }
            }
        } catch (ValidationException ex) {
            logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Unable to validate Assertion: " + ex.getMessage());
            return this.getWSTrustResponse(false);
        }

        // Get PublicKey that will be used to validate the signature on the assertion.
        PublicKey publicKey = null;
        KeyInfo keyInfo = signature.getKeyInfo();
        if (keyInfo != null) {
            // See if a certificate is present.
            X509Certificate certificate = this.getCertificateFromKeyInfo(keyInfo);
            if (certificate != null) {
                logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Using supplied X.509 certificate to validate assertion signature");
                logMessage.addOtherParam("X.509 Certificate (used to validate signature)", certificate);

                // Validate the certificate.
                boolean validCert = this.validateCertificate(certificate, trustStore);
                if (!validCert) {
                    return this.getWSTrustResponse(false);
                }
                publicKey = certificate.getPublicKey();
            } else {
                // Look for KeyInfo/KeyValue [PublicKey]
                publicKey = this.getPublicKeyFromKeyInfo(keyInfo);
                if (publicKey != null) {
                    logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Using supplied public key to validate assertion signature");
                }
            }
        }

        // If no public key is specified, use the issuer certificate to validate.
        if (publicKey == null) {
            logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Using issuer X.509 certificate to validate assertion signature");
            X509Certificate certificate = STSUtil.getIssuerCertificate(stsConfig, trustStore);
            publicKey = certificate.getPublicKey();
        }

        // Now, validate the signature with PublicKey
        boolean valid = this.validateSignature(signature, publicKey);
        if (logMessage.isLogEnabled()) {
            if (valid) {
                logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Token is valid!");
            } else {
                logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Token is NOT valid!");
            }
        }
        return this.getWSTrustResponse(valid);
    }

    /**
     *
     * @param keyInfo
     * @return
     */
    private X509Certificate getCertificateFromKeyInfo(KeyInfo keyInfo) {
        XLogMessage logMessage = this.getLogMessage();
        List<X509Certificate> certs = null;
        try {
            certs = KeyInfoHelper.getCertificates(keyInfo);
        } catch (CertificateException ex) {
            logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Unable to get Certificate used to sign Assertion from KeyInfo: " + ex.getMessage());
            certs = null;
        }
        if (certs == null || certs.isEmpty()) {
            return null;
        } else {
            return certs.get(0);  // Use the first one.
        }
    }

    /**
     * 
     * @param certificate
     * @param trustStore
     * @return
     */
    private boolean validateCertificate(X509Certificate certificate, KeyStore trustStore) {
        XLogMessage logMessage = this.getLogMessage();
        try {
            STSUtil.validateCertificate(certificate, trustStore);
        } catch (STSException ex) {
            logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "STS does not trust the Certificate used to sign Assertion: " + ex.getMessage());
            return false;  // Invalid.
        }
        return true; // Valid.
    }

    /**
     * 
     * @param keyInfo
     * @return
     * @throws STSException
     */
    private PublicKey getPublicKeyFromKeyInfo(KeyInfo keyInfo) {
        XLogMessage logMessage = this.getLogMessage();
        List<PublicKey> publicKeys = null;
        try {
            publicKeys = STSUtil.getPublicKeys(keyInfo);
        } catch (STSException ex) {
            logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Unable to get PublicKey used to sign Assertion from KeyInfo: " + ex.getMessage());
            publicKeys = null;
        }
        if (publicKeys == null || publicKeys.isEmpty()) {
            return null;
        } else {
            return publicKeys.get(0);  // Just pick first one.
        }
    }

    /**
     * 
     * @param signature
     * @param publicKey
     * @return
     */
    private boolean validateSignature(Signature signature, PublicKey publicKey) {
        XLogMessage logMessage = this.getLogMessage();
        if (logMessage.isLogEnabled()) {
            logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Validating signature using public key: " + publicKey);
        }
        // Now, validate the signature on the Assertion using given public key.
        BasicX509Credential credential = new BasicX509Credential();
        credential.setPublicKey(publicKey);
        SignatureValidator sigValidator = new SignatureValidator(credential);
        try {
            sigValidator.validate(signature);
            return true;  // Valid.
        } catch (ValidationException ex) {
            logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Unable to validate Assertion: " + ex.getMessage());
            return false;  // Not valid.
        }
    }

    /**
     *
     * @param signature
     * @param certificate
     * @return
     */
    //private boolean validateSignatureUsingCertificate(Signature signature, X509Certificate certificate) {
    //    XLogMessage logMessage = this.getLogMessage();
    //    if (logMessage.isLogEnabled()) {
    //        logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Validating signature using certificate: " + certificate);
    //    }
    // Now, validate the signature on the Assertion using chosen Certificate.
    //    BasicX509Credential credential = new BasicX509Credential();
    //    credential.setEntityCertificate(certificate);
    //    SignatureValidator sigValidator = new SignatureValidator(credential);
    //    try {
    //        sigValidator.validate(signature);
    //    } catch (ValidationException ex) {
    //        logMessage.addOtherParam(LOG_TOKEN_STATUS_PARAM, "Unable to validate Assertion: " + ex.getMessage());
    //        return false;  // Not valid.
    //    }
    //    return true;  // Valid.
    //}
    /**
     * 
     * @param assertionOMElement
     * @return
     * @throws STSException
     */
    private Assertion getAssertion(OMElement assertionOMElement) throws STSException {
        return (Assertion) STSUtil.convertOMElementToXMLObject(assertionOMElement);
    }

    /**
     *
     * @param success
     * @return
     */
    private OMElement getWSTrustResponse(boolean success) {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMNamespace wstNs = omfactory.createOMNamespace(STSConstants.WSTRUST_NS, "wst");
        OMElement rstResponseCollection = omfactory.createOMElement("RequestSecurityTokenResponseCollection", wstNs);
        OMElement rstResponse = omfactory.createOMElement("RequestSecurityTokenResponse", wstNs);
        OMElement requestedSecurityToken = omfactory.createOMElement("RequestedSecurityToken", wstNs);
        OMElement tokenType = omfactory.createOMElement("TokenType", wstNs);
        tokenType.setText(STSConstants.SAML2_TOKEN_TYPE);

        OMElement status = omfactory.createOMElement("Status", wstNs);
        OMElement code = omfactory.createOMElement("Code", wstNs);
        status.addChild(code);
        String statusText;
        if (success) {
            statusText = STSConstants.WSTRUST_TOKEN_VALID;
        } else {
            statusText = STSConstants.WSTRUST_TOKEN_INVALID;
        }
        code.setText(statusText);

        // Wire things up in proper order.
        rstResponseCollection.addChild(rstResponse);
        rstResponse.addChild(tokenType);
        rstResponse.addChild(requestedSecurityToken);
        rstResponse.addChild(status);
        return rstResponseCollection;
    }

    /**
     *
     * @param request
     * @return
     * @throws STSException
     */
    private OMElement getAssertionOMElement(OMElement request) throws STSException {
        OMElement assertionNode = null;
        try {
            String nameSpaceNames[] = {"wst", "saml2"};
            String nameSpaceURIs[] = {STSConstants.WSTRUST_NS, PolicyConstants.SAML2_NS};
            assertionNode = XPathHelper.selectSingleNode(
                    request,
                    "./wst:ValidateTarget/saml2:Assertion[1]",
                    nameSpaceNames, nameSpaceURIs);
            if (assertionNode == null) {
                throw new STSException("Unable to find Assertion to validate");
            }
        } catch (XPathHelperException ex) {
            throw new STSException("Unable to find Assertion to validate: " + ex.getMessage());
        }
        return assertionNode;
    }
}
