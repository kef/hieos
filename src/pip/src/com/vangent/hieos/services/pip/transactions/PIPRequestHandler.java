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

import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfig.ConfigItem;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xml.XMLParser;
import com.vangent.hieos.xutil.xml.XPathHelper;
import org.apache.axiom.om.OMElement;
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
     * @throws SOAPFaultException
     */
    public OMElement run(OMElement request) throws SOAPFaultException {
        try {
            log_message.setPass(true); // Hope for the best.
            // FIXME: Stub.
            try {
                // Read everytime - so we don't have to restart server during testing.

                // Load PIP stub data.
                String policyDir = XConfig.getConfigLocation(ConfigItem.POLICY_DIR);
                String pipStubDataFile = policyDir + "/pipstubdata/pip.xml";
                OMElement pipStubData = XMLParser.fileToOM(pipStubDataFile);
                OMElement requestedPidNode = XPathHelper.selectSingleNode(request, "./ns:PatientId", PolicyConstants.HIEOS_PIP_NS);
                String pid = requestedPidNode.getText();
                // Now lookup the patient id.
                OMElement pipResponse =
                        XPathHelper.selectSingleNode(pipStubData, "./ns:PIPEntry[@pid='" + pid + "']/ns:GetConsentDirectivesResponse[1]", PolicyConstants.HIEOS_PIP_NS);
                if (pipResponse == null) {
                    pid = "DEFAULT";
                    pipResponse =
                            XPathHelper.selectSingleNode(pipStubData, "./ns:PIPEntry[@pid='" + pid + "']/ns:GetConsentDirectivesResponse[1]", PolicyConstants.HIEOS_PIP_NS);

                }
                //OMElement pipResponse = XMLParser.stringToOM(content);
                if (log_message.isLogEnabled()) {
                    log_message.addOtherParam("Response", pipResponse);
                }
                return pipResponse;
            } catch (XMLParserException ex) {
                log_message.addErrorParam("EXCEPTION", ex.getMessage());
                log_message.setPass(false);
                throw new SOAPFaultException(ex.getMessage());
            }
        } catch (Exception ex) {
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            log_message.setPass(false);
            throw new SOAPFaultException(ex.getMessage());
        }
    }
}
