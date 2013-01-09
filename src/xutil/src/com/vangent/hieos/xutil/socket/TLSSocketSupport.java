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
package com.vangent.hieos.xutil.socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ServerSocketFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
    private static final String KEY_STORE_FILE_NAME = "javax.net.ssl.keyStore";
    private static final String TRUST_STORE_FILE_NAME = "javax.net.ssl.trustStore";
    private static final String KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";
    private static final String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
    private static final String CIPHER_SUITE = "https.cipherSuites";

    /**
     * 
     */
    public TLSSocketSupport() {
    }

    /**
     *
     * @param hostname
     * @param port
     */
    /*
    public TLSSocketSupport(String hostname, int port) {
    }*/
    /**
     *
     * @param hostname
     * @param port
     * @param message
     * @throws IOException
     */
    public void sendNonSecureMessage(String hostname, int port, String message)
            throws IOException {
        log.debug("Non TLS CLIENT Establishing Connection");
        Socket clientSocket = new Socket(hostname, port);
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
        pw.print(message);
        pw.close();
        log.debug("Non TLS Message Sent to Server");
    }

    /**
     * 
     * @param hostname
     * @param port
     * @param message
     * @throws IOException
     */
    public void sendSecureMessage(String hostname, int port, String message)
            throws IOException {
        PrintWriter pw = null;
        try {
            log.debug("TLS CLIENT Establishing Connection");
            Socket clientSocket = getSecureClientSocket(hostname, port);
            pw = new PrintWriter(clientSocket.getOutputStream(), true);
            pw.print(message);
            //pw.close();
            log.debug("TLS Message Sent to Server");
        } catch (Exception ex) {
            log.error(ex);
        } finally {
            if (pw != null) {
                // Should close the socket ...
                pw.close();
            }
        }
    }

    /**
     * 
     * @param hostname
     * @param port
     * @return
     * @throws Exception
     */
    public java.net.Socket getSecureClientSocket(String hostname, int port) throws Exception {
        // Get props.
        String keyStoreFileName = System.getProperty(TLSSocketSupport.KEY_STORE_FILE_NAME);
        String keyStorePassword = System.getProperty(TLSSocketSupport.KEY_STORE_PASSWORD);
        String trustStoreFileName = System.getProperty(TLSSocketSupport.TRUST_STORE_FILE_NAME);
        String trustStorePassword = System.getProperty(TLSSocketSupport.TRUST_STORE_PASSWORD);
        if (log.isTraceEnabled()) {
            log.trace("+++++ Key/Trust Stores :- " + keyStoreFileName + " and " + trustStoreFileName);
        }
        KeyManager[] keyManagers = TLSSocketSupport.createKeyManagers(keyStoreFileName, keyStorePassword, null /* alias */);
        TrustManager[] trustManagers = TLSSocketSupport.createTrustManagers(trustStoreFileName, trustStorePassword);
        SSLContext context = SSLContext.getInstance("TLS");
        //TODO investigate: could also be "SSLContext context = SSLContext.getInstance("TLS");" Why?
        context.init(keyManagers, trustManagers, null);
        SSLSocketFactory socketFactory = context.getSocketFactory();
        SSLSocket socket = (SSLSocket) socketFactory.createSocket(hostname, port);
        // Authenticate the client?
        //boolean requireClientAuthentication = false;  // FOR NOW
        //socket.setNeedClientAuth(requireClientAuthentication);
        if (log.isDebugEnabled()) {
            log.debug("+++++ SSL CLIENT SOCKET CREATED ON :- " + hostname + ":" + port);
        }
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
        return this.getSecureServerSocket(port, backlog, this.getCipherSuites());
    }

    /**
     *
     * @param port
     * @param backlog
     * @param cipherSuites
     * @return
     * @throws Exception
     */
    public java.net.ServerSocket getSecureServerSocket(int port, int backlog, String[] cipherSuites) throws Exception {
        // Get props.
        String keyStoreFileName = System.getProperty(KEY_STORE_FILE_NAME);
        String keyStorePassword = System.getProperty(KEY_STORE_PASSWORD);
        String trustStoreFileName = System.getProperty(TRUST_STORE_FILE_NAME);
        String trustStorePassword = System.getProperty(TRUST_STORE_PASSWORD);
        if (log.isTraceEnabled()) {
            log.trace("+++++ Key/Trust Stores :- " + keyStoreFileName + " and " + trustStoreFileName);
        }
        KeyManager[] keyManagers = TLSSocketSupport.createKeyManagers(keyStoreFileName, keyStorePassword, null /* alias */);
        TrustManager[] trustManagers = TLSSocketSupport.createTrustManagers(trustStoreFileName, trustStorePassword);
        SSLContext context = SSLContext.getInstance("TLS");
        //TODO investigate: could also be "SSLContext context = SSLContext.getInstance("TLS");" Why?
        context.init(keyManagers, trustManagers, null);
        ServerSocketFactory serverSocketFactory = context.getServerSocketFactory();
        SSLServerSocket socket = (SSLServerSocket) serverSocketFactory.createServerSocket(port, backlog);
        socket.setNeedClientAuth(true);
        socket.setEnabledCipherSuites(cipherSuites);
        if (log.isTraceEnabled()) {
            printCipherSuites(socket);
        }
        log.debug("+++++ TLS SOCKET CREATED (port = " + port + ") +++++");
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
        if (log.isTraceEnabled()) {
            printKeystoreInfo(keyStore);
        }

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
        if (log.isTraceEnabled()) {
            printKeystoreInfo(trustStore);
        }
        //create trustmanager factory and load the keystore object in it
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        //return
        return trustManagerFactory.getTrustManagers();
    }

    /**
     * 
     * @return
     */
    private String[] getCipherSuites() {
        String ciphers = System.getProperty(CIPHER_SUITE);
        if (log.isTraceEnabled()) {
            log.trace("Enabling required cipher suites...");
            log.trace("Specified cipher suites:  " + ciphers);
        }
        //String[] suites = socket.getSupportedCipherSuites();
        return ciphers.split(",");
    }

    /**
     *
     * @param keystore
     * @throws KeyStoreException
     */
    private static void printKeystoreInfo(KeyStore keystore) throws KeyStoreException {
        log.trace("Provider : " + keystore.getProvider().getName());
        log.trace("Type : " + keystore.getType());
        log.trace("Size : " + keystore.size());
        Enumeration en = keystore.aliases();
        while (en.hasMoreElements()) {
            log.trace("Alias: " + en.nextElement());
        }
    }

    /**
     *
     * @param socket
     */
    private static void printCipherSuites(SSLServerSocket socket) {
        String[] enabledCipherSuites = socket.getEnabledCipherSuites();
        for (int i = 0; i < enabledCipherSuites.length; i++) {
            log.trace("enabledCipherSuite[" + i + "]: " + enabledCipherSuites[i]);
        }
        String[] supportedCipherSuites = socket.getSupportedCipherSuites();
        for (int i = 0; i < supportedCipherSuites.length; i++) {
            log.trace("supportedCipherSuite[" + i + "]: " + supportedCipherSuites[i]);
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
