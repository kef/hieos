/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.registry;

import com.vangent.hieos.xutil.response.RegistryErrorList;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
//import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.validation.Validator;
import com.vangent.hieos.xutil.xml.SchemaValidation;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xconfig.XConfig;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author NIST (Adapted by Bernie Thuman)
 */
public class RegistryUtility {

    private final static Logger logger = Logger.getLogger(RegistryUtility.class);

    /**
     *
     * @param ahqr
     * @param metadata_type
     * @throws XdsInternalException
     * @throws SchemaValidationException
     */
    static public void schema_validate_local(OMElement ahqr, int metadata_type)
            throws XdsInternalException {
        // Only do schema validation if required per configuration.
        XConfig xconfig = XConfig.getInstance();
        boolean XMLSchemaValidationEnabled = xconfig.getHomeCommunityConfigPropertyAsBoolean(
                "XMLSchemaValidationEnabled", false /* default (off) */);
        if (XMLSchemaValidationEnabled == false) {
            if (logger.isDebugEnabled()) {
                logger.debug("**** HIEOS - XML Schema Validation DISABLED ****");
            }
            return;  // Early return!
        }
        if (logger.isDebugEnabled()) {
            logger.debug("**** HIEOS - XML Schema Validation ENABLED ****");
        }
        try {
            SchemaValidation.validate_local(ahqr, metadata_type);
        } catch (Exception e) {
            throw new XdsInternalException("Schema Validation Failed: " + e.getMessage());
        }
    }

    /**
     * 
     * @param m
     * @param isSubmit
     * @return
     * @throws XdsException
     */
    static public RegistryErrorList metadata_validator(Metadata m, boolean isSubmit) throws XdsException {
        RegistryErrorList rel = new RegistryErrorList();
        Validator v = new Validator(m, rel, isSubmit, (XLogMessage) null);
        v.run();
        return rel;
    }

    /**
     *
     * @param e
     * @return
     */
    public static String exception_details(Exception e) {
        /*
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps); */
        e.printStackTrace();
        return "Exception thrown: " + e.getClass().getName() + " : " + e.getMessage();
        //return "Exception thrown: " + e.getClass().getName() + "\n" + e.getMessage() + "\n" + new String(baos.toByteArray());
    }
}
