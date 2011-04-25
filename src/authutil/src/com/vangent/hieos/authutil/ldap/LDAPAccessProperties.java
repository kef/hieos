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
package com.vangent.hieos.authutil.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Anand Sastry
 */
public class LDAPAccessProperties {

    private static LDAPAccessProperties singletonInstance = null;
    private static final Logger log = Logger.getLogger(LDAPAccessProperties.class);
    private Properties properties; // Holds properties.

    /**
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static synchronized LDAPAccessProperties getInstance(String fileName) throws IOException {
        if (singletonInstance == null) {
            if (log.isInfoEnabled()) {
                log.info("Loading properties from " + fileName);
            }
            singletonInstance = new LDAPAccessProperties(fileName);
        }
        return singletonInstance;
    }

    /**
     *
     * @param fileName
     * @throws IOException
     */
    private LDAPAccessProperties(String fileName) throws IOException {

        loadProperties(fileName);

    }

    /**
     *
     * @param fileLocation
     * @throws IOException
     */
    private void loadProperties(String fileLocation) throws IOException {
        InputStream fin = null;
        //Properties properties = null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            fin = classLoader.getResourceAsStream(fileLocation);
            this.properties = new Properties();
            this.properties.load(fin);

        } finally {
            if (fin != null) {
                fin.close();
            }
        }
    }

    /**
     * Return the value for the given property key name.
     *
     * @param propKey The name of the property.
     * @return A string holding the property value.
     */
    public String getProperty(String propKey) {
        return properties.getProperty(propKey);
    }
}
