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
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.w3c.dom.Element;
import org.apache.axis2.util.XMLUtils;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;

/**
 *
 * @author Bernie Thuman
 */
public class SAML2TokenIssueHandler extends SAML2TokenHandler {

    /**
     * 
     * @param stsConfig
     */
    public SAML2TokenIssueHandler(STSConfig stsConfig) {
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
        STSConfig stsConfig = this.getSTSConfig();

        // Create the Assertion with a unique UUID.
        Assertion assertion = (Assertion) createSamlObject(Assertion.DEFAULT_ELEMENT_NAME);
        Namespace dsns = new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds");
        assertion.addNamespace(dsns);
        Namespace xsins = new Namespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        assertion.addNamespace(xsins);
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setID(UUID.randomUUID().toString());

        // Set the issuer name.
        Issuer issuer = (Issuer) createSamlObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(stsConfig.getIssuerName());
        assertion.setIssuer(issuer);

        // Set the instant the Assertion was created.
        DateTime createdDate = new DateTime();
        assertion.setIssueInstant(createdDate);

        // Add an AuthnStatement.
        assertion.getAuthnStatements().add(this.getAuthnStatement());

        // Set the validity period (as Conditions).
        long ttl = stsConfig.getTimeToLive();
        DateTime expiresDate = new DateTime(createdDate.getMillis() + ttl);
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(createdDate);
        conditions.setNotOnOrAfter(expiresDate);
        assertion.setConditions(conditions);

        // Create the Subject.
        Subject subj = (Subject) createSamlObject(Subject.DEFAULT_ELEMENT_NAME);
        assertion.setSubject(subj);

        // Set the "Name" of the Subject.
        // FIXME?
        NameID nameId = (NameID) createSamlObject(NameID.DEFAULT_ELEMENT_NAME);
        String userName = request.getHeaderData().getUserName();
        nameId.setValue(userName);
        subj.setNameID(nameId);

        // Set the SubjectConfirmation method to "holder of key".
        SubjectConfirmation subjConf = (SubjectConfirmation) createSamlObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        subjConf.setMethod("urn:oasis:names:tc:2.0:cm:holder-of-key");
        subj.getSubjectConfirmations().add(subjConf);
        SubjectConfirmationData subjData = (SubjectConfirmationData) createSamlObject(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        subjData.getUnknownAttributes().put(new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi"),
                "saml:KeyInfoConfirmationDataType");
        subjConf.setSubjectConfirmationData(subjData);

        // Set the validity period.
        subjData.setNotBefore(createdDate);
        subjData.setNotOnOrAfter(expiresDate);

        // Add the KeyInfo.
        KeyInfo ki = (KeyInfo) createSamlObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        subjData.getUnknownXMLObjects().add(ki);

        KeyName kn = (KeyName) createSamlObject(KeyName.DEFAULT_ELEMENT_NAME);
        kn.setValue(stsConfig.getIssuerAlias());  // FIXME!!
        ki.getKeyNames().add(kn);

        // Add Attribute statements.
        AttributeStatement as = (AttributeStatement) createSamlObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
        SAML2AttributeHandler attributeHandler = new SAML2AttributeHandler();
        List<Attribute> attributes = attributeHandler.handle(request);
        as.getAttributes().addAll(attributes);
        assertion.getAttributeStatements().add(as);

        // Get the issuer PrivateKeyEntry from KeyStore (used to "sign" the Assertion).
        KeyStore keyStore = STSUtil.getKeyStore(stsConfig);
        PrivateKeyEntry pkEntry = STSUtil.getIssuerPrivateKeyEntry(stsConfig, keyStore);
       
        // Now, get private key and certificate for the issuer.
        PrivateKey pk = pkEntry.getPrivateKey();
        X509Certificate certificate = (X509Certificate) pkEntry.getCertificate();

        // Get ready to sign the Assertion using the issuer's private key.
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(certificate);
        credential.setPrivateKey(pk);
        Signature signature = (Signature) createSamlObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(credential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        // Place the Certificate (public portion) for the issuer in the KeyInfo response.
        KeyInfo keyInfo = (KeyInfo) createSamlObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        KeyInfoHelper.addPublicKey(keyInfo, certificate.getPublicKey());
        try {
            KeyInfoHelper.addCertificate(keyInfo, certificate);
        } catch (CertificateEncodingException ex) {
            throw new STSException("Unable to encode Certificate: " + ex.getMessage());
        }
        signature.setKeyInfo(keyInfo);
        assertion.setSignature(signature);

        // Fully marshall the Assertion - required in order for the signature to be applied
        MarshallerFactory marshallerFactory =
                Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(assertion);
        Element assertionElement;
        try {
            assertionElement = marshaller.marshall(assertion);
        } catch (MarshallingException ex) {
            throw new STSException("Unable to marshall Assertion: " + ex.getMessage());
        }

        // Now, "sign" the Assertion using the issuer's private key.
        try {
            Signer.signObject(signature);
        } catch (SignatureException ex) {
            throw new STSException("Unable to sign Assertion: " + ex.getMessage());
        }

        // Convert the response to an OMElement (for subsequent processing).
        OMElement assertionOMElement;
        try {
            assertionOMElement = XMLUtils.toOM(assertionElement);
        } catch (Exception ex) {
            throw new STSException("Unable to convert Assertion from Element to OMElement: " + ex.getMessage());
        }

        // Return a properly formatted WS-Trust response.
        return this.getWSTrustResponse(assertionOMElement, createdDate, expiresDate);
    }

    //  <wst:RequestSecurityTokenResponseCollection xmlns:wst="http://docs.oasis-open.org/ws-sx/ws-trust/200512">
    //          <wst:RequestSecurityTokenResponse>
    //              <wst:TokenType>http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0</wst:TokenType>
    //              <wst:KeySize>256</wst:KeySize>
    //              <wst:RequestedAttachedReference>
    //                  <wsse:SecurityTokenReference xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
    //                      <wsse:Reference URI="#urn:uuid:B5B6F87D35F524847D1307123499963" ValueType="http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0"/>
    //                  </wsse:SecurityTokenReference>
    //              </wst:RequestedAttachedReference>
    //              <wst:RequestedUnattachedReference>
    //                  <wsse:SecurityTokenReference xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
    //                      <wsse:Reference URI="urn:uuid:B5B6F87D35F524847D1307123499963" ValueType="http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0"/>
    //                  </wsse:SecurityTokenReference>
    //              </wst:RequestedUnattachedReference>
    //              <wsp:AppliesTo xmlns:wsp="http://schemas.xmlsoap.org/ws/2004/09/policy">
    //                  <wsa:EndpointReference xmlns:wsa="http://www.w3.org/2005/08/addressing">
    //                      <wsa:Address>http://www.vangent.com/X-ServiceProvider-HIEOS</wsa:Address>
    //                  </wsa:EndpointReference>
    //              </wsp:AppliesTo>
    //              <wst:Lifetime>
    //                  <wsu:Created xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">2011-06-03T17:51:40.475Z</wsu:Created>
    //                  <wsu:Expires xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">2011-06-03T17:56:40.475Z</wsu:Expires>
    //              </wst:Lifetime>
    //              <wst:RequestedSecurityToken>
    //                  <saml:Assertion ID="urn:uuid:B5B6F87D35F524847D1307123499963" IssueInstant="2011-06-03T17:51:39.975Z" Version="2.0" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion">
    //                      ...
    //                  </saml:Assertion>
    //              </wst:RequestedSecurityToken>
    //              <wst:RequestedProofToken>
    //                  <wst:BinarySecret>aeZRUBMDMzsH1wqLxsXO7W2OMTRE/fNSdQM+XQAeXkc=</wst:BinarySecret>
    //              </wst:RequestedProofToken>
    //          </wst:RequestSecurityTokenResponse>
    //      </wst:RequestSecurityTokenResponseCollection>
    /**
     *
     * @param assertion
     * @return
     */
    private OMElement getWSTrustResponse(OMElement assertion, DateTime creationDate, DateTime expirationDate) {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMNamespace wstNs = omfactory.createOMNamespace(STSConstants.WSTRUST_NS, "wst");
        OMNamespace wsuNs = omfactory.createOMNamespace(STSConstants.WSSECURITY_UTILITY_NS, "wsu");
        OMElement rstResponseCollection = omfactory.createOMElement("RequestSecurityTokenResponseCollection", wstNs);
        OMElement rstResponse = omfactory.createOMElement("RequestSecurityTokenResponse", wstNs);
        OMElement requestedSecurityToken = omfactory.createOMElement("RequestedSecurityToken", wstNs);
        OMElement lifeTime = omfactory.createOMElement("Lifetime", wstNs);
        OMElement created = omfactory.createOMElement("Created", wsuNs);
        OMElement expires = omfactory.createOMElement("Expires", wsuNs);
        created.setText(creationDate.toString());
        expires.setText(expirationDate.toString());
        lifeTime.addChild(created);
        lifeTime.addChild(expires);

        OMElement tokenType = omfactory.createOMElement("TokenType", wstNs);
        tokenType.setText(STSConstants.SAML2_TOKEN_TYPE);

        // Wire things up in proper order.
        rstResponseCollection.addChild(rstResponse);
        rstResponse.addChild(tokenType);
        rstResponse.addChild(lifeTime);
        rstResponse.addChild(requestedSecurityToken);
        requestedSecurityToken.addChild(assertion);
        return rstResponseCollection;
    }

    /**
     * 
     * @return
     * @throws STSException
     */
    private AuthnStatement getAuthnStatement() throws STSException {
        // Construct AuthnStatement.
        AuthnStatement authStmt = (AuthnStatement) this.createSamlObject(AuthnStatement.DEFAULT_ELEMENT_NAME);

        // Set the Authentication instant.
        authStmt.setAuthnInstant(new DateTime());
        AuthnContext authContext = (AuthnContext) this.createSamlObject(AuthnContext.DEFAULT_ELEMENT_NAME);

        AuthnContextClassRef authCtxClassRef = (AuthnContextClassRef) this.createSamlObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);

        authCtxClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);

        // May need: urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport
        // FIXME: code to handler properly.
        // authCtxClassRef.setAuthnContextClassRef(AuthnContext.X509_AUTHN_CTX);

        authContext.setAuthnContextClassRef(authCtxClassRef);
        authStmt.setAuthnContext(authContext);
        return authStmt;
    }
}
