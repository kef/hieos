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

import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.services.sts.model.STSConstants;
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigObject;

/**
 *
 * @author Bernie Thuman
 */
public class STSConfig {

    private static final String DEFAULT_ISSUER_ALIAS = "s1as";
    private static final long DEFAULT_TIME_TO_LIVE = 30000;
    // Singleton.
    private static STSConfig _instance = null;
    // Configuration.
    private String issuerName;
    private String issuerAlias;
    private long timeToLive;
    private String keyStore;
    private String trustStore;
    private String keyStorePassword;
    private String trustStorePassword;
    private String issuerPassword;
    private boolean computeSubjectNameFromClaims;
    private STSConstants.AuthenticationType authenticationType;
    private XConfigObject configObject;

    /**
     *
     * @return
     */
    static public synchronized STSConfig getInstance() throws STSException {
        if (_instance == null) {
            _instance = new STSConfig();
            XConfigObject configObject = STSConfig.getXConfigObject();
            _instance.loadConfig(configObject);
        }
        return _instance;
    }

    /**
     *
     * @param configObject
     */
    private void loadConfig(XConfigObject configObject) {
        this.configObject = configObject;
        issuerName = configObject.getProperty("issuerName");
        issuerAlias = configObject.getProperty("issuerAlias");
        if (issuerAlias == null) {
            issuerAlias = STSConfig.DEFAULT_ISSUER_ALIAS;  // Default.
        }
        String timeToLiveAsString = configObject.getProperty("timeToLiveInMilliseconds");
        if (timeToLiveAsString == null) {
            timeToLive = STSConfig.DEFAULT_TIME_TO_LIVE;  // DEFAULT.
        } else {
            timeToLive = new Long(timeToLiveAsString);
        }
        keyStore = configObject.getProperty("keyStore");
        if (keyStore == null) {
            keyStore = System.getProperty("javax.net.ssl.keyStore");
        }
        trustStore = configObject.getProperty("trustStore");
        if (trustStore == null) {
            trustStore = System.getProperty("javax.net.ssl.trustStore");
        }
        keyStorePassword = configObject.getProperty("keyStorePassword");
        if (keyStorePassword == null) {
            keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
        }
        trustStorePassword = configObject.getProperty("trustStorePassword");
        if (trustStorePassword == null) {
            trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
        }
        issuerPassword = configObject.getProperty("issuerPassword");
        if (issuerPassword == null) {
            issuerPassword = keyStorePassword;
        }
        String authenticationTypeText = configObject.getProperty("authenticationType");
        if (authenticationTypeText.equalsIgnoreCase("UserNameToken")) {
            authenticationType = STSConstants.AuthenticationType.USER_NAME_TOKEN;
        } else {
            authenticationType = STSConstants.AuthenticationType.X509_CERTIFICATE;
        }
        computeSubjectNameFromClaims = configObject.getPropertyAsBoolean("computeSubjectNameFromClaims", false);
    }

    /**
     * 
     * @return
     * @throws STSException
     */
    static private XConfigObject getXConfigObject() throws STSException {
        try {
            XConfig xconf = XConfig.getInstance();
            XConfigObject configObject = null;
            XConfigObject homeCommunityConfig = xconf.getHomeCommunityConfig();
            configObject = homeCommunityConfig.getXConfigObjectWithName("sts", "SecureTokenServiceType");
            return configObject;
        } catch (XConfigException ex) {
            throw new STSException("Unable to get XConfig: " + ex.getMessage());
        }
    }

    private STSConfig() {
        // Do not allow.
    }

    public XConfigObject getConfigObject() {
        return configObject;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getIssuerAlias() {
        return issuerAlias;
    }

    public String getIssuerPassword() {
        return issuerPassword;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public String getKeyStoreFileName() {
        return keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getTrustStoreFileName() {
        return trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public STSConstants.AuthenticationType getAuthenticationType() {
        return authenticationType;
    }
}
