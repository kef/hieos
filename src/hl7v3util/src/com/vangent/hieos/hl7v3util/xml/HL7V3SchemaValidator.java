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
package com.vangent.hieos.hl7v3util.xml;

import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.exception.XMLSchemaValidatorException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xml.XMLSchemaValidator;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7V3SchemaValidator {

    private final static Logger logger = Logger.getLogger(HL7V3SchemaValidator.class);

    /**
     * 
     */
    private HL7V3SchemaValidator() {
        // Can't be instantiated.
    }

    /**
     *
     * @param xmlNode
     * @param messageName
     * @throws XMLSchemaValidatorException
     */
    public static void validate(OMElement xmlNode, String messageName) throws XMLSchemaValidatorException {
        String namespace = "urn:hl7-org:v3";
        //<Property name="XMLSchemaHL7V3ValidationEnabled">true</Property>
        //<Property name="XMLSchemaHL7V3Directory">C:\\dev\\ihe-materials\\ITI\\schema\\HL7V3\\NE2008\\multicacheschemas\\</Property>
        XConfig config;
        try {
            config = XConfig.getInstance();
        } catch (XConfigException ex) {
            logger.warn("Unable to get XConfig to enable/disable XMLSchemaHL73Validation: " + ex.getMessage());
            return;
        }
        boolean validate = config.getHomeCommunityConfigPropertyAsBoolean("XMLSchemaHL7V3ValidationEnabled", false);
        if (validate == true) {
            String schemaDirectory = XConfig.getConfigLocation(XConfig.ConfigItem.SCHEMA_DIR);
            if (schemaDirectory == null) {
                logger.warn("Unable to get HIEOS Schema Dir to enable/disable XMLSchemaHL73Validation");
                return;
            } else {
                schemaDirectory = schemaDirectory + "/HL7V3/NE2008/multicacheschemas/";
            }
            String fileName = messageName + ".xsd";
            String schemaLocation = namespace + " " + schemaDirectory + fileName;
            try {
                XMLSchemaValidator validator = new XMLSchemaValidator(schemaLocation);
                validator.validate(xmlNode);
            } catch (XMLSchemaValidatorException ex) {
                String errorText = messageName + " did not validate against XML schema " + ex.getMessage();
                throw new XMLSchemaValidatorException(errorText);
            }
        }
    }
}
