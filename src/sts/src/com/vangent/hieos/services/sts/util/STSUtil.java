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
package com.vangent.hieos.services.sts.util;

import com.vangent.hieos.policyutil.util.PolicyConfig;
import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.services.sts.model.STSConstants;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.xml.XMLParser;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.signature.KeyInfo;
import org.w3c.dom.Element;

/**
 *
 * @author Bernie Thuman
 */
public class STSUtil {

    private final static Logger logger = Logger.getLogger(STSUtil.class);
    private static XMLObjectBuilderFactory _xmlObjectBuilderFactory = null;

    // Initialize OpenSAML library and other singletons.
    static {
        try {
            // OpenSAML 2.3
            logger.info("Initializing OpenSAML library");
            DefaultBootstrap.bootstrap();
            _xmlObjectBuilderFactory = Configuration.getBuilderFactory();
            logger.info("Initializing OpenSAML library - Success!");
        } catch (ConfigurationException ex) {
            logger.fatal("Failure initializing OpenSAML: " + ex.getMessage());
        }
    }

    /**
     *
     * @param request
     * @return
     * @throws STSException
     */
    public static String getRequestType(OMElement request) throws STSException {
        OMElement reqTypeElem = request.getFirstChildWithName(new QName(STSConstants.WSTRUST_NS,
                "RequestType"));
        if (reqTypeElem == null
                || reqTypeElem.getText() == null
                || reqTypeElem.getText().trim().length() == 0) {
            throw new STSException("Unable to locate RequestType on request");
        } else {
            return reqTypeElem.getText().trim();
        }
    }

    /**
     * 
     * @return
     */
    public static XMLObjectBuilderFactory getXMLObjectBuilderFactory() {
        return _xmlObjectBuilderFactory;
    }

    /**
     *
     * @return
     * @throws STSException
     */
    public static PolicyConfig getPolicyConfig() throws STSException {
        try {
            return PolicyConfig.getInstance();
        } catch (Exception ex) {
            throw new STSException(ex.getMessage()); // Rethrow.
        }
    }

    /**
     *
     * @param xmlObject
     * @return
     * @throws STSException
     */
    // FIXME: Any way to avoid double conversion?
    static public OMElement convertXMLObjectToOMElement(XMLObject xmlObject) throws STSException {
        // 2 step process.
        try {
            // Convert XMLObject to Element.
            Element element = STSUtil.convertXMLObjectToElement(xmlObject);
            // Convert Element to OMElement.
            return XMLParser.convertDOMtoOM(element);
        } catch (XMLParserException ex) {
            throw new STSException(ex.getMessage());
        }
    }

    /**
     * 
     * @param xmlObject
     * @return
     * @throws STSException
     */
    static public Element convertXMLObjectToElement(XMLObject xmlObject) throws STSException {

        // Fully marshall the XMLObject - required in order for signature validation to be applied
        MarshallerFactory marshallerFactory =
                Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
        try {
            return marshaller.marshall(xmlObject);
        } catch (MarshallingException ex) {
            throw new STSException("Unable to marshall XMLObject: " + ex.getMessage());
        }
    }

    /**
     * 
     * @param element
     * @return
     * @throws STSException
     */
    static public XMLObject convertElementToXMLObject(Element element) throws STSException {
        // Convert Element to an XMLObject.
        UnmarshallerFactory unmarshallerFactory =
                Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller =
                unmarshallerFactory.getUnmarshaller(element);
        try {
            return unmarshaller.unmarshall(element);
        } catch (UnmarshallingException ex) {
            throw new STSException("Unable to unmarshall XMLObject: " + ex.getMessage());
        }
    }

    /**
     *
     * @param omElement
     * @return
     * @throws STSException
     */
    // FIXME: Any way to avoid double conversion?
    static public XMLObject convertOMElementToXMLObject(OMElement omElement) throws STSException {
        // 2 step process.
        try {
            // Convert OMElement to Element.
            Element element = XMLParser.convertOMToDOM(omElement);
            // Convert Element to XMLObject.
            return STSUtil.convertElementToXMLObject(element);
        } catch (XMLParserException ex) {
            throw new STSException(ex.getMessage());
        }

    }

    /**
     * 
     * @param qname
     * @return
     * @throws STSException
     */
    static public XMLObject createXMLObject(QName qname) throws STSException {
        return STSUtil.getXMLObjectBuilderFactory().getBuilder(qname).buildObject(qname);
    }

    /**
     *
     * @param certificate
     * @param addCertificate
     * @param addPublicKey
     * @return
     * @throws STSException
     */
    static public KeyInfo getKeyInfo(X509Certificate certificate, boolean addCertificate, boolean addPublicKey) throws STSException {
        // Place the Certificate (public portion) for the issuer in the KeyInfo response.
        KeyInfo keyInfo = (KeyInfo) STSUtil.createXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        if (addPublicKey) {
            KeyInfoHelper.addPublicKey(keyInfo, certificate.getPublicKey());
        }
        try {
            if (addCertificate) {
                KeyInfoHelper.addCertificate(keyInfo, certificate);
            }
        } catch (CertificateEncodingException ex) {
            throw new STSException("Unable to encode certificate: " + ex.getMessage());
        }
        return keyInfo;
    }

    /**
     *
     * @param keyInfo
     * @return
     * @throws STSException
     */
    static public List<PublicKey> getPublicKeys(KeyInfo keyInfo) throws STSException {
        try {
            return KeyInfoHelper.getPublicKeys(keyInfo);
        } catch (KeyException ex) {
            throw new STSException("Unable to get public keys from KeyInfo: " + ex.getMessage());
        }
    }

    /**
     *
     * @param cert
     * @param trustStore
     * @throws STSException
     */
    public static void validateCertificate(X509Certificate cert, KeyStore trustStore) throws STSException {
        try {
            // To check the validity of the dates
            cert.checkValidity();
        } catch (CertificateExpiredException ex) {
            throw new STSException("Certificate expired: " + ex.getMessage());
        } catch (CertificateNotYetValidException ex) {
            throw new STSException("Certificate not yet valid: " + ex.getMessage());
        }

        // Check the chain.
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            List<X509Certificate> mylist = new ArrayList<X509Certificate>();
            mylist.add(cert);
            CertPath cp = cf.generateCertPath(mylist);
            PKIXParameters params = new PKIXParameters(trustStore);
            // FIXME: Add revocation checking.
            params.setRevocationEnabled(false);
            CertPathValidator cpv = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
            PKIXCertPathValidatorResult pkixCertPathValidatorResult = (PKIXCertPathValidatorResult) cpv.validate(cp, params);
            if (logger.isDebugEnabled()) {
                logger.debug(pkixCertPathValidatorResult);
            }
        } catch (Exception ex) {
            throw new STSException("Exception while validating Certificate: " + ex.getMessage());
        }
    }

    /**
     *
     * @param base64Text
     * @return
     * @throws STSException
     */
    public static X509Certificate getCertificate(String base64Text) throws STSException {
        try {
            byte[] base64Bytes = base64Text.getBytes();
            byte[] decodedBytes = Base64.decodeBase64(base64Bytes);
            ByteArrayInputStream bs = new ByteArrayInputStream(decodedBytes);
            CertificateFactory cf;
            cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(bs);
            return cert;
        } catch (CertificateException ex) {
            throw new STSException("Unable to create X509Certificate: " + ex.getMessage());
        }
    }

    /**
     * 
     * @param stsConfig
     * @return
     * @throws STSException
     */
    public static KeyStore getTrustStore(STSConfig stsConfig) throws STSException {
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
        return ks;
    }

    /**
     *
     * @param stsConfig
     * @return
     * @throws STSException
     */
    public static KeyStore getKeyStore(STSConfig stsConfig) throws STSException {
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            String keyStorePassword = stsConfig.getKeyStorePassword();
            char[] password = keyStorePassword.toCharArray();
            FileInputStream fis = new FileInputStream(stsConfig.getKeyStoreFileName());
            ks.load(fis, password);
            fis.close();
        } catch (Exception ex) {
            throw new STSException("Problem loading keystore: " + ex.getMessage());
        }
        return ks;
    }

    /**
     *
     * @param stsConfig
     * @param trustStore
     * @return
     * @throws STSException
     */
    public static X509Certificate getIssuerCertificate(STSConfig stsConfig, KeyStore trustStore) throws STSException {
        X509Certificate certificate;
        try {
            String issuerAlias = stsConfig.getIssuerAlias();
            KeyStore.TrustedCertificateEntry tcEntry =
                    (KeyStore.TrustedCertificateEntry) trustStore.getEntry(issuerAlias, null);
            certificate = (X509Certificate) tcEntry.getTrustedCertificate();
        } catch (Exception ex) {
            throw new STSException("Problem getting public certificate: " + ex.getMessage());
        }
        return certificate;
    }

    // FIXME: Cache truststore/keystore usage!!!
    // TBD: Move to xutil?
    /**
     *
     * @param stsConfig
     * @param keyStore
     * @return
     * @throws STSException
     */
    public static PrivateKeyEntry getIssuerPrivateKeyEntry(STSConfig stsConfig, KeyStore keyStore) throws STSException {
        //PrivateKey pk;
        PrivateKeyEntry pkEntry;
        try {
            String issuerAlias = stsConfig.getIssuerAlias();
            String issuerPassword = stsConfig.getIssuerPassword();
            pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(issuerAlias, new KeyStore.PasswordProtection(issuerPassword.toCharArray()));
            //pk = pkEntry.getPrivateKey();
            //certificate = (X509Certificate) pkEntry.getCertificate();
        } catch (Exception ex) {
            throw new STSException("Problem getting private key: " + ex.getMessage());
        }
        return pkEntry;
    }
}
