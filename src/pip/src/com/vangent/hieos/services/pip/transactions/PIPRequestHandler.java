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
package com.vangent.hieos.services.pip.transactions;

import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xml.XMLParser;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIPRequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(PIPRequestHandler.class);

    /**
     *
     */
    private PIPRequestHandler() {
        // Do nothing.
    }

    /**
     *
     * @param log_message
     * @param mCtx
     */
    public PIPRequestHandler(XLogMessage log_message, MessageContext mCtx) {
        this.log_message = log_message;
        this.init(null, mCtx);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean getStatus() {
        return log_message.isPass();
    }

    /**
     *
     * @param request
     * @return
     * @throws AxisFault
     */
    public OMElement run(OMElement request) throws AxisFault {
        try {
            log_message.setPass(true); // Hope for the best.
            // FIXME (STUB):
            String content =
                    "<pip:GetConsentDirectivesResponse xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:pip=\"urn:hieos:policy:pip\">"
                    + "  <pip:ConsentDirectives alwaysAuthorize=\"false\">"
                    + "     <pip:AllowedOrganizations>"
                    + "        <pip:Organization>1.1</pip:Organization>"
                    + "        <pip:Organization>1.2</pip:Organization>"
                    + "     </pip:AllowedOrganizations>"
                    + "     <pip:BlockedOrganizations>"
                    + "        <pip:Organization>1.5</pip:Organization>"
                    + "        <pip:Organization>1.6</pip:Organization>"
                    + "     </pip:BlockedOrganizations>"
                    + "     <pip:BlockedIndividuals>"
                    + "        <pip:Individual>3.1</pip:Individual>"
                    + "        <pip:Individual>3.2</pip:Individual>"
                    + "     </pip:BlockedIndividuals>"
                    + "     <pip:AllowedRoles>"
                    + "        <pip:Role codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>"
                    + "        <pip:Role codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>"
                    + "     </pip:AllowedRoles>"
                    + "     <pip:AllowedPurposeOfUse>"
                    + "        <pip:PurposeOfUse codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>"
                    + "        <pip:PurposeOfUse codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>"
                    + "     </pip:AllowedPurposeOfUse>"
                    + "     <pip:SensitiveDocumentTypes>"
                    + "        <pip:DocumentType codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>"
                    + "        <pip:DocumentType codeSystem=\"String\" displayName=\"String\" codeSystemName=\"String\" code=\"String\"/>"
                    + "     </pip:SensitiveDocumentTypes>"
                    + "     <pip:SensitiveDocumentAccessList>"
                    + "        <pip:SensitiveDocumentAccess>"
                    + "           <pip:Organization>String</pip:Organization>"
                    + "           <pip:Individual>String</pip:Individual>"
                    + "        </pip:SensitiveDocumentAccess>"
                    + "        <pip:SensitiveDocumentAccess>"
                    + "           <pip:Organization>String</pip:Organization>"
                    + "           <pip:Individual>String</pip:Individual>"
                    + "        </pip:SensitiveDocumentAccess>"
                    + "     </pip:SensitiveDocumentAccessList>"
                    + "  </pip:ConsentDirectives>"
                    + "</pip:GetConsentDirectivesResponse>";
            try {
                OMElement pipResponse = XMLParser.stringToOM(content);
                if (log_message.isLogEnabled()) {
                    log_message.addOtherParam("Response", pipResponse);
                }
                return pipResponse;
            } catch (XMLParserException ex) {
                throw new AxisFault(ex.getMessage());
            }
        } catch (Exception ex) {
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            log_message.setPass(false);
            throw new AxisFault(ex.getMessage());
        }
    }
}
