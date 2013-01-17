/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v2util.config;

import com.vangent.hieos.hl7v2util.exception.HL7v2UtilException;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 *
 * @author Bernie Thuman
 */
public class ListenerConfig implements ConfigItem {
    //<listener>
    //    <enabled>true</enabled>
    //    <tls-enabled>true</tls-enabled>
    //    <port>5051</port>
    //    <thread-pool-size>20</thread-pool-size>
    //    <cipher-suites>SSL_RSA_WITH_3DES_EDE_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA</cipher-suites>
    //</listener>

    private static String ENABLED = "enabled";
    private static String TLS_ENABLED = "tls-enabled";
    private static String PORT = "port";
    private static String THREAD_POOL_SIZE = "thread-pool-size";
    private static String CIPHER_SUITES = "cipher-suites";
    private boolean enabled = false;
    private int port = -1;
    private int threadPoolSize = -1;
    private boolean tlsEnabled = false;
    private String[] cipherSuites = null;

    /**
     * 
     * @return
     */
    public String[] getCipherSuites() {
        return cipherSuites;
    }

    /**
     *
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     *
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     *
     * @return
     */
    public boolean isTLSEnabled() {
        return tlsEnabled;
    }

    /**
     *
     * @return
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * 
     * @param hc
     * @param acceptorConfig
     * @throws HL7v2UtilException
     */
    public void load(HierarchicalConfiguration hc, AcceptorConfig acceptorConfig) throws HL7v2UtilException {
        enabled = hc.getBoolean(ENABLED, false);
        tlsEnabled = hc.getBoolean(TLS_ENABLED, false);
        port = hc.getInt(PORT, -1);
        threadPoolSize = hc.getInt(THREAD_POOL_SIZE, -1);
        if (tlsEnabled) {
            this.loadCipherSuites(hc);
        }
        if (port == -1) {
            throw new HL7v2UtilException("Must specify " + PORT + " in configuration.");
        }
        if (threadPoolSize == -1) {
            throw new HL7v2UtilException("Must specify " + THREAD_POOL_SIZE + " in configuration.");
        }
    }

    /**
     *
     * @param hc
     * @throws HL7v2UtilException
     */
    private void loadCipherSuites(HierarchicalConfiguration hc) throws HL7v2UtilException {
        cipherSuites = hc.getStringArray(CIPHER_SUITES);
        if (cipherSuites == null || cipherSuites.length == 0) {
            throw new HL7v2UtilException("Must specify " + CIPHER_SUITES + " in configuration.");
        }
    }
}
