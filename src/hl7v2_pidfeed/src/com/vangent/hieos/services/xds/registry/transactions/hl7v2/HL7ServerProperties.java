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

import java.io.FileInputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7ServerProperties {

    private static final Logger log = Logger.getLogger(HL7ServerProperties.class);
    private Properties properties; // Holds properties.

    /**
     * 
     * @param fileName
     */
    public HL7ServerProperties(String fileName) throws Exception {
        this.loadProperties(fileName);
    }

    /**
     *
     * @param fileName
     */
    private void loadProperties(String fileName) throws Exception {
        properties = new Properties();
        try {
            log.info("Loading property file ...");
            FileInputStream in = new FileInputStream(fileName);
            properties.load(in);
            log.info("Loaded property file");
            in.close();
        } catch (Exception e) {
            log.error("EXCEPTION: " + e.getMessage());
            throw e;
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
