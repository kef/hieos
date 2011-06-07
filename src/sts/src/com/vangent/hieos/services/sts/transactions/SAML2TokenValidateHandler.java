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
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Element;
import org.apache.axis2.util.XMLUtils;

/**
 *
 * @author Bernie Thuman
 */
public class SAML2TokenValidateHandler extends SAML2TokenHandler {
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

    /**
     *
     * @param stsConfig
     */
    public SAML2TokenValidateHandler(STSConfig stsConfig) {
        super(stsConfig);
    }

    /**
     *
     * @param request
     * @return
     * @throws STSException
     */
    @Override
    protected OMElement handle(STSRequestData request) throws STSException {
        // Get Assertion from request.
        OMElement assertionOMElement = this.getAssertion(request.getRequest());

        STSConfig stsConfig = this.getSTSConfig();
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            String trustStorePassword = stsConfig.getTrustStorePassword();
            char[] password = trustStorePassword.toCharArray();
            FileInputStream fis = new FileInputStream(stsConfig.getTrustStoreFileName());
            ks.load(fis, password);
            fis.close();
        } catch (Exception ex) {
            throw new STSException("Problem loading truststore: " + ex.getMessage());
        }

        X509Certificate certificate;
        try {
            KeyStore.TrustedCertificateEntry tcEntry =
                    (KeyStore.TrustedCertificateEntry) ks.getEntry("s1as", null);
            certificate = (X509Certificate) tcEntry.getTrustedCertificate();
        } catch (Exception ex) {
            throw new STSException("Problem getting public certificate: " + ex.getMessage());
        }


        // Convert OMElement to Element.
        Element assertionElement;
        try {
            assertionElement = XMLUtils.toDOM(assertionOMElement);
        } catch (Exception ex) {
            throw new STSException("Unable to convert Assertion from OMElement to Element: " + ex.getMessage());
        }

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
        boolean success = true;
        try {
            assertion.validate(true);
            Signature signature = assertion.getSignature();
            SAMLSignatureProfileValidator pv = new SAMLSignatureProfileValidator();
            pv.validate(signature);
            BasicX509Credential credential = new BasicX509Credential();
            credential.setEntityCertificate(certificate);
            SignatureValidator sigValidator = new SignatureValidator(credential);
            sigValidator.validate(signature);
        } catch (ValidationException ex) {
            System.out.println("ValidationException: " + ex.getMessage());
            success = false;
            //throw new STSException("Unable to validate Assertion: " + ex.getMessage());
        }

        return this.getWSTrustResponse(success);
    }

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
    private OMElement getAssertion(OMElement request) throws STSException {
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
