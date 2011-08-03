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
package com.vangent.hieos.services.sts.config;

import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 * Manages singleton instance of STS configuration.
 *
 * @author Bernie Thuman
 */
public class STSConfig {

    private static final String DEFAULT_ISSUER_ALIAS = "s1as";
    private static final long DEFAULT_TIME_TO_LIVE = 30000;  // milliseconds
    // Configuration.
    private String issuerName;
    private String issuerAlias;
    private long timeToLive;
    private String keyStore;
    private String trustStore;
    private String keyStorePassword;
    private String trustStorePassword;
    private String issuerPassword;
    private String useSubjectNameFromClaimURI;
    private boolean emitIssuerX509Data;
    private boolean emitIssuerPublicKeyValue;
    private boolean emitSubjectX509Data;
    private boolean emitSubjectPublicKeyValue;
    private XConfigActor stsConfigActor;

    /**
     *
     */
    private STSConfig() {
        // Do not allow.
    }

    /**
     *
     * @param stsConfigActor
     */
    public STSConfig(XConfigActor stsConfigActor) {
        this.stsConfigActor = stsConfigActor;
        this.loadConfig();
    }

    /**
     * Pull configuration from "xconfig" and cache away.
     *
     * @param configObject
     */
    private void loadConfig() {
        issuerName = stsConfigActor.getProperty("IssuerName");
        issuerAlias = stsConfigActor.getProperty("IssuerAlias");
        if (issuerAlias == null) {
            issuerAlias = STSConfig.DEFAULT_ISSUER_ALIAS;  // Default.
        }
        String timeToLiveAsString = stsConfigActor.getProperty("TimeToLiveInMilliseconds");
        if (timeToLiveAsString == null) {
            timeToLive = STSConfig.DEFAULT_TIME_TO_LIVE;  // DEFAULT.
        } else {
            timeToLive = new Long(timeToLiveAsString);
        }
        keyStore = stsConfigActor.getProperty("KeyStore");
        if (keyStore == null) {
            keyStore = System.getProperty("javax.net.ssl.keyStore");
        }
        trustStore = stsConfigActor.getProperty("TrustStore");
        if (trustStore == null) {
            trustStore = System.getProperty("javax.net.ssl.trustStore");
        }
        keyStorePassword = stsConfigActor.getProperty("KeyStorePassword");
        if (keyStorePassword == null) {
            keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
        }
        trustStorePassword = stsConfigActor.getProperty("TrustStorePassword");
        if (trustStorePassword == null) {
            trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
        }
        issuerPassword = stsConfigActor.getProperty("IssuerPassword");
        if (issuerPassword == null) {
            issuerPassword = keyStorePassword;
        }
        useSubjectNameFromClaimURI = stsConfigActor.getProperty("UseSubjectNameFromClaimURI");

        // Properties supporting public key and certificate output data.
        emitIssuerX509Data = stsConfigActor.getPropertyAsBoolean("EmitIssuerX509Data", true);
        emitIssuerPublicKeyValue = stsConfigActor.getPropertyAsBoolean("EmitIssuerPublicKeyValue", true);
        emitSubjectX509Data = stsConfigActor.getPropertyAsBoolean("EmitSubjectX509Data", true);
        emitSubjectPublicKeyValue = stsConfigActor.getPropertyAsBoolean("EmitSubjectPublicKeyValue", true);
    }

    /**
     *
     * @return
     */
    public String getIssuerName() {
        return issuerName;
    }

    /**
     *
     * @return
     */
    public String getIssuerAlias() {
        return issuerAlias;
    }

    /**
     *
     * @return
     */
    public String getIssuerPassword() {
        return issuerPassword;
    }

    /**
     *
     * @return
     */
    public long getTimeToLive() {
        return timeToLive;
    }

    /**
     *
     * @return
     */
    public String getKeyStoreFileName() {
        return keyStore;
    }

    /**
     *
     * @return
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     *
     * @return
     */
    public String getTrustStoreFileName() {
        return trustStore;
    }

    /**
     *
     * @return
     */
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     *
     * @return
     */
    public String getUseSubjectNameFromClaimURI() {
        return useSubjectNameFromClaimURI;
    }

    /**
     *
     * @return
     */
    public boolean isEmitIssuerPublicKeyValue() {
        return emitIssuerPublicKeyValue;
    }

    /**
     *
     * @return
     */
    public boolean isEmitIssuerX509Data() {
        return emitIssuerX509Data;
    }

    /**
     *
     * @return
     */
    public boolean isEmitSubjectPublicKeyValue() {
        return emitSubjectPublicKeyValue;
    }

    /**
     *
     * @return
     */
    public boolean isEmitSubjectX509Data() {
        return emitSubjectX509Data;
    }
}
