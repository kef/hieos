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
package com.vangent.hieos.services.pip.serviceimpl;

import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import com.vangent.hieos.xutil.xml.XMLParser;
import java.util.logging.Level;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIP extends XAbstractService {

    private final static Logger logger = Logger.getLogger(PIP.class);

    /**
     *
     * @param request
     * @return
     * @throws AxisFault
     */
    public OMElement GetConsentDirectives(OMElement request) throws AxisFault {
        // FIXME (STUB):
        String content =
        "<pip:GetConsentDirectivesResponse xsi:schemaLocation=\"urn:hieos:policy:pip PIP.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:pip=\"urn:hieos:policy:pip\">" +
	"  <pip:ConsentDirectives alwaysAuthorize=\"false\">" +
        "     <pip:AllowedOrganizations>" +
        "        <pip:Organization>1.1</pip:Organization>" +
        "        <pip:Organization>1.2</pip:Organization>" +
        "     </pip:AllowedOrganizations>" +
        "     <pip:BlockedOrganizations>" +
        "        <pip:Organization>XXXX</pip:Organization>" +
        "        <pip:Organization>XXXX</pip:Organization>" +
        "     </pip:BlockedOrganizations>" +
        "     <pip:BlockedIndividuals>" +
	"        <pip:Individual>String</pip:Individual>" +
	"        <pip:Individual>String</pip:Individual>" +
        "     </pip:BlockedIndividuals>" +
	"     <pip:AllowedRoles>" +
	"        <pip:Role codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>" +
	"        <pip:Role codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>" +
	"     </pip:AllowedRoles>" +
	"     <pip:AllowedPurposeOfUse>" +
	"        <pip:PurposeOfUse codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>" +
	"        <pip:PurposeOfUse codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>" +
	"     </pip:AllowedPurposeOfUse>" +
        "     <pip:SensitiveDocumentTypes>" +
	"        <pip:DocumentType codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>" +
	"        <pip:DocumentType codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>" +
	"     </pip:SensitiveDocumentTypes>" +
	"     <pip:SensitiveDocumentAccessList>" +
	"        <pip:SensitiveDocumentAccess>" +
	"           <pip:Organization>String</pip:Organization>" +
	"           <pip:Individual>String</pip:Individual>" +
	"        </pip:SensitiveDocumentAccess>" +
	"        <pip:SensitiveDocumentAccess>" +
	"           <pip:Organization>String</pip:Organization>" +
	"           <pip:Individual>String</pip:Individual>" +
	"        </pip:SensitiveDocumentAccess>" +
        "     </pip:SensitiveDocumentAccessList>" +
	"  </pip:ConsentDirectives>" +
        "</pip:GetConsentDirectivesResponse>";
        try {
            OMElement pipResponse = XMLParser.stringToOM(content);
            return pipResponse;
        } catch (XMLParserException ex) {
            throw new AxisFault(ex.getMessage());
        }
    }

    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("PIP::startUp()");
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("PIP::shutDown()");
    }
}
