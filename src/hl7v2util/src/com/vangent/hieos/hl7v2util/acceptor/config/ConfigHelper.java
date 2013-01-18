/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v2util.acceptor.config;

import com.vangent.hieos.hl7v2util.exception.HL7v2UtilException;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class ConfigHelper {

    private final static Logger log = Logger.getLogger(ConfigHelper.class);

    /**
     * 
     * @param className
     * @return
     * @throws HL7v2UtilException 
     */
    public static Object loadClassInstance(String className) throws HL7v2UtilException {
        log.info("... className = " + className);

        // Dynamically load the class.
        Class classInstance;
        try {
            classInstance = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            log.error("Could not load class '" + className + "': " + ex.getMessage());
            throw new HL7v2UtilException("Could not load class '" + className + "': " + ex.getMessage());
        }

        // Create a new instance of the class.
        Object obj;
        try {
            log.info("... instantiating instance of = " + className);
            obj = classInstance.newInstance();
        } catch (InstantiationException ex) {
            log.error("Could not create instance '" + className + "': " + ex.getMessage());
            throw new HL7v2UtilException("Could not create instance '" + className + "': " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            log.error("Could not create instance '" + className + "': " + ex.getMessage());
            throw new HL7v2UtilException("Could not create instance '" + className + "': " + ex.getMessage());
        }
        return obj;
    }
}
