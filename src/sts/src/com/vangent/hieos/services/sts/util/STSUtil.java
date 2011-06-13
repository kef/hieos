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

import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Bernie Thuman
 */
public class STSUtil {

    /**
     * 
     * @param cert
     * @param trustStore
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws CertPathValidatorException
     */
    public static void validateCertificate(X509Certificate cert, KeyStore trustStore) throws STSException {

        System.out.println("X509Certificate = " + cert);
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
            System.out.println(pkixCertPathValidatorResult);
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
