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

import com.vangent.hieos.services.sts.model.STSConstants;
import com.vangent.hieos.services.sts.model.STSRequestData;
import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.services.sts.util.STSUtil;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.security.KeyStore;
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
     * @param requestData
     * @return
     * @throws STSException
     */
    @Override
    protected OMElement handle(STSRequestData requestData) throws STSException {
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

            // Validate time stamps
            Conditions conditions = assertion.getConditions();
            DateTime startDate = conditions.getNotBefore();
            DateTime endDate = conditions.getNotOnOrAfter();
            if (startDate.isAfterNow()) {
                System.out.println("Assertion not valid yet");
                return this.getWSTrustResponse(false);
            }
            if (endDate.isBeforeNow() || endDate.isEqualNow()) {
                System.out.println("Assertion expired");
                return this.getWSTrustResponse(false);
            }
        } catch (ValidationException ex) {
            System.out.println("Unable to validate Assertion: " + ex.getMessage());
            return this.getWSTrustResponse(false);
        }

        // Get the Certificate (from Signature/KeyInfo) used to sign the assertion.
        X509Certificate certificate = null;
        KeyInfo keyInfo = signature.getKeyInfo();
        if (keyInfo != null) {
            List<X509Certificate> certs = null;
            try {
                certs = KeyInfoHelper.getCertificates(keyInfo);
            } catch (CertificateException ex) {
                System.out.println("Unable to get Certificate used to sign Assertion from KeyInfo: " + ex.getMessage());
                return this.getWSTrustResponse(false);
            }
            if ((certs != null) || !certs.isEmpty()) {
                certificate = certs.get(0);  // Use the first one.
                try {
                    STSUtil.validateCertificate(certificate, trustStore);
                } catch (STSException ex) {
                    System.out.println("STS does not trust the Certificate used to sign Assertion: " + ex.getMessage());
                    return this.getWSTrustResponse(false);
                }
            }
        }

        // Get the "issuer" Certificate if not present in Signature/KeyInfo
        if (certificate == null) {
            certificate = STSUtil.getIssuerCertificate(stsConfig, trustStore);
            // Will validate using this.
        }

        // Now, validate the signature on the Assertion using chosen Certificate.
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(certificate);
        SignatureValidator sigValidator = new SignatureValidator(credential);
        try {
            sigValidator.validate(signature);
        } catch (ValidationException ex) {
            System.out.println("Unable to validate Assertion: " + ex.getMessage());
            return this.getWSTrustResponse(false);
        }
        // Finally, a success!
        return this.getWSTrustResponse(true);
    }

    /**
     * 
     * @param assertionOMElement
     * @return
     * @throws STSException
     */
    private Assertion getAssertion(OMElement assertionOMElement) throws STSException {
        return (Assertion)STSUtil.convertOMElementToXMLObject(assertionOMElement);
        /*
        // Convert OMElement to Element.
       Element assertionElement;
        try {
            assertionElement = XMLUtils.toDOM(assertionOMElement);
        } catch (Exception ex) {
            throw new STSException("Unable to convert Assertion from OMElement to Element: " + ex.getMessage());
        }

        // Fully unmarshall the Assertion so that the signature validation will work.
        UnmarshallerFactory unmarshallerFactory =
                Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller =
                unmarshallerFactory.getUnmarshaller(assertionElement);
        Assertion assertion;
        try {
            assertion = (Assertion) unmarshaller.unmarshall(assertionElement);
        } catch (UnmarshallingException ex) {
            throw new STSException("Unable to unmarshall Assertion: " + ex.getMessage());
        }
        return assertion;*/
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
            statusText = STSConstants.TOKEN_VALID;
        } else {
            statusText = STSConstants.TOKEN_INVALID;
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
            String nameSpaceURIs[] = {STSConstants.WSTRUST_NS, STSConstants.SAML2_NS};
            assertionNode = XPathHelper.selectSingleNode(
                    request,
                    "./wst:ValidateTarget/saml2:Assertion[1]",
                    nameSpaceNames, nameSpaceURIs);
            if (assertionNode == null) {
                throw new STSException("Unable to find Assertion to validate -- rejecting request");
            }
        } catch (XPathHelperException ex) {
            throw new STSException("Unable to find Assertion to validate -- rejecting request");
        }
        return assertionNode;
    }
}
