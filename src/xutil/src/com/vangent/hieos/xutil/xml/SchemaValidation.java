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

/*
 * SchemaValidation.java
 */
package com.vangent.hieos.xutil.xml;

import com.vangent.hieos.xutil.exception.XMLSchemaValidatorException;
import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.xutil.exception.XdsInternalException;

import com.vangent.hieos.xutil.xconfig.XConfig;
import org.apache.axiom.om.OMElement;

public class SchemaValidation implements MetadataTypes {

    /*
    public static String validate(OMElement ele, int metadataType) throws XdsInternalException {
    return validate_local(ele, metadataType);
    }*/
    // The only known use case for localhost validation failing is when this is called from
    // xdstest2 in which case it is trying to call home to reference the schema files.
    // What is really needed is a configuration parm that points the reference to the local filesystem
    // and include the schema files in the xdstest2tool environment.
    // port 80 does not exist for requests on-machine (on the server). only requests coming in from
    // off-machine go through the firewall where the port translation happens.
    // even though this says validate_local, it is used by all requests
    public static void validate_local(OMElement ele, int metadataType) throws XdsInternalException {
        SchemaValidation.run(ele.toString(), metadataType);
    }

    // empty string as result means no errors
    static private void run(String metadata, int metadataType) throws XdsInternalException {
        String localSchema = XConfig.getConfigLocation(XConfig.ConfigItem.SCHEMA_DIR);

        // Decode schema location
        String schemaLocation;
        switch (metadataType) {
            case METADATA_TYPE_Rb:
                schemaLocation = "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0 " +
                        (localSchema + "/v3/lcm.xsd");
                break;
            case METADATA_TYPE_SQ:
                schemaLocation = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0 " +
                        (localSchema + "/v3/query.xsd ") +
                        "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0 " +
                        (localSchema + "/v3/rs.xsd");
                break;
            case METADATA_TYPE_RET:
                schemaLocation = "urn:ihe:iti:xds-b:2007 " +
                        (localSchema + "/v3/XDS.b_DocumentRepository.xsd ") +
                        "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0 " +
                        (localSchema + "/v3/rs.xsd");
                break;
            default:
                throw new XdsInternalException("SchemaValidation: invalid metadata type = " + metadataType);
        }

        try {
            XMLSchemaValidator validator = new XMLSchemaValidator(schemaLocation);
            // Not sure if this next line really needs to stay (old NIST code).
            String metadata2 = metadata.replaceAll("urn:uuid:", "urn_uuid_");
            validator.validate(metadata2);
        } catch (XMLSchemaValidatorException ex) {
            throw new XdsInternalException(ex.getMessage());
        }
    }
}
