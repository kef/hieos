/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xds.registry.transactions.hl7v2;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ServerSocketFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman (adapted from http://javasecurity.wikidot.com/example-item-1).
 */
public class TLSSocketSupport {
    private static final Logger log = Logger.getLogger(TLSSocketSupport.class);

    private static final String KEY_STORE_FILE_NAME = "key_store_file_name";
    private static final String TRUST_STORE_FILE_NAME = "trust_store_file_name";
    private static final String KEY_STORE_PASSWORD = "key_store_password";
    private static final String TRUST_STORE_PASSWORD = "trust_store_password";
    private HL7ServerProperties props;

    public TLSSocketSupport(HL7ServerProperties props) {
        this.props = props;
    }

    private TLSSocketSupport() {
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public java.net.Socket getSecureClientSocket() throws Exception {
        // Get props.
        String keyStoreFileName = props.getProperty(TLSSocketSupport.KEY_STORE_FILE_NAME);
        String keyStorePassword = props.getProperty(TLSSocketSupport.KEY_STORE_PASSWORD);
        String trustStoreFileName = props.getProperty(TLSSocketSupport.TRUST_STORE_FILE_NAME);
        String trustStorePassword = props.getProperty(TLSSocketSupport.TRUST_STORE_PASSWORD);

        KeyManager[] keyManagers = TLSSocketSupport.createKeyManagers(keyStoreFileName, keyStorePassword, null /* alias */);
        TrustManager[] trustManagers = TLSSocketSupport.createTrustManagers(trustStoreFileName, trustStorePassword);
        SSLContext context = SSLContext.getInstance("TLS");
        //TODO investigate: could also be "SSLContext context = SSLContext.getInstance("TLS");" Why?
        context.init(keyManagers, trustManagers, null);
        SSLSocketFactory socketFactory = context.getSocketFactory();
        SSLSocket socket = (SSLSocket) socketFactory.createSocket();
        // Authenticate the client?
        //boolean requireClientAuthentication = false;  // FOR NOW
        //socket.setNeedClientAuth(requireClientAuthentication);
        log.info("+++++ SSL CLIENT SOCKET CREATED!!!! +++++");
        return socket;
    }

    /**
     *
     * @param port
     * @param backlog
     * @return
     * @throws Exception
     */
    public java.net.ServerSocket getSecureServerSocket(int port, int backlog) throws Exception {
        // Get props.
        String keyStoreFileName = props.getProperty(KEY_STORE_FILE_NAME);
        String keyStorePassword = props.getProperty(KEY_STORE_PASSWORD);
        String trustStoreFileName = props.getProperty(TRUST_STORE_FILE_NAME);
        String trustStorePassword = props.getProperty(TRUST_STORE_PASSWORD);
        KeyManager[] keyManagers = TLSSocketSupport.createKeyManagers(keyStoreFileName, keyStorePassword, null /* alias */);
        TrustManager[] trustManagers = TLSSocketSupport.createTrustManagers(trustStoreFileName, trustStorePassword);
        SSLContext context = SSLContext.getInstance("TLS");
        //TODO investigate: could also be "SSLContext context = SSLContext.getInstance("TLS");" Why?
        context.init(keyManagers, trustManagers, null);
        ServerSocketFactory serverSocketFactory = context.getServerSocketFactory();
        SSLServerSocket socket = (SSLServerSocket) serverSocketFactory.createServerSocket(port, backlog);
        socket.setNeedClientAuth(true);
        /*if (debug) {
        printCipherSuites(socket);
        }*/
        TLSSocketSupport.enableAllSupportedCipherSuites(socket);
        /*if (debug) {
        printCipherSuites(socket);
        }*/
        log.info("+++++ TLS SOCKET CREATED (port = " + port + ") +++++");
        return socket;
    }

    /**
     *
     * @param keyStoreFileName
     * @param keyStorePassword
     * @param alias
     * @return
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    private static KeyManager[] createKeyManagers(String keyStoreFileName, String keyStorePassword, String alias)
            throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        //create Inputstream to keystore file
        java.io.InputStream inputStream = new java.io.FileInputStream(keyStoreFileName);
        //create keystore object, load it with keystorefile data
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(inputStream, keyStorePassword == null ? null : keyStorePassword.toCharArray());
        // printKeystoreInfo(keyStore);

        KeyManager[] managers;
        if (alias != null) {
            managers =
                    new KeyManager[]{
                        new TLSSocketSupport().new AliasKeyManager(keyStore, alias, keyStorePassword)};
        } else {
            //create keymanager factory and load the keystore object in it
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword == null ? null : keyStorePassword.toCharArray());
            managers = keyManagerFactory.getKeyManagers();
        }
        //return
        return managers;
    }

    /**
     *
     * @param trustStoreFileName
     * @param trustStorePassword
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    private static TrustManager[] createTrustManagers(String trustStoreFileName, String trustStorePassword)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        //create Inputstream to truststore file
        java.io.InputStream inputStream = new java.io.FileInputStream(trustStoreFileName);
        //create keystore object, load it with truststorefile data
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(inputStream, trustStorePassword == null ? null : trustStorePassword.toCharArray());
        //DEBUG information should be removed
        // printKeystoreInfo(trustStore);
        //create trustmanager factory and load the keystore object in it
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        //return
        return trustManagerFactory.getTrustManagers();
    }

    /**
     *
     * @param socket
     */
    private static void enableAllSupportedCipherSuites(SSLServerSocket socket) {
        log.info("Enabling all available cipher suites...");
        String[] suites = socket.getSupportedCipherSuites();
        socket.setEnabledCipherSuites(suites);
    }

    /**
     *
     * @param keystore
     * @throws KeyStoreException
     */
    private static void printKeystoreInfo(KeyStore keystore) throws KeyStoreException {
        System.out.println("Provider : " + keystore.getProvider().getName());
        System.out.println("Type : " + keystore.getType());
        System.out.println("Size : " + keystore.size());

        Enumeration en = keystore.aliases();
        while (en.hasMoreElements()) {
            System.out.println("Alias: " + en.nextElement());
        }
    }

    /**
     *
     * @param socket
     */
    private static void printCipherSuites(SSLServerSocket socket) {
        String[] enabledCipherSuites = socket.getEnabledCipherSuites();
        for (int i = 0; i < enabledCipherSuites.length; i++) {
            System.out.println("enabledCipherSuite[" + i + "]: " + enabledCipherSuites[i]);
        }
        String[] supportedCipherSuites = socket.getSupportedCipherSuites();
        for (int i = 0; i < supportedCipherSuites.length; i++) {
            System.out.println("supportedCipherSuite[" + i + "]: " + supportedCipherSuites[i]);
        }
    }

    /**
     *
     */
    private class AliasKeyManager implements X509KeyManager {

        private KeyStore _ks;
        private String _alias;
        private String _password;

        /**
         *
         * @param ks
         * @param alias
         * @param password
         */
        public AliasKeyManager(KeyStore ks, String alias, String password) {
            _ks = ks;
            _alias = alias;
            _password = password;
        }

        /**
         *
         * @param str
         * @param principal
         * @param socket
         * @return
         */
        public String chooseClientAlias(String[] str, Principal[] principal, Socket socket) {
            return _alias;
        }

        /**
         *
         * @param str
         * @param principal
         * @param socket
         * @return
         */
        public String chooseServerAlias(String str, Principal[] principal, Socket socket) {
            return _alias;
        }

        /**
         *
         * @param alias
         * @return
         */
        public X509Certificate[] getCertificateChain(String alias) {
            try {
                java.security.cert.Certificate[] certificates = this._ks.getCertificateChain(alias);
                if (certificates == null) {
                    throw new FileNotFoundException("no certificate found for alias:" + alias);
                }
                X509Certificate[] x509Certificates = new X509Certificate[certificates.length];
                System.arraycopy(certificates, 0, x509Certificates, 0, certificates.length);
                return x509Certificates;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         *
         * @param str
         * @param principal
         * @return
         */
        public String[] getClientAliases(String str, Principal[] principal) {
            return new String[]{_alias};
        }

        /**
         *
         * @param alias
         * @return
         */
        public PrivateKey getPrivateKey(String alias) {
            try {
                return (PrivateKey) _ks.getKey(alias, _password == null ? null : _password.toCharArray());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         *
         * @param str
         * @param principal
         * @return
         */
        public String[] getServerAliases(String str, Principal[] principal) {
            return new String[]{_alias};
        }
    }
}
