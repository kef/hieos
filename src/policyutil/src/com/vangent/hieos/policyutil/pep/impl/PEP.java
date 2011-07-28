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
package com.vangent.hieos.policyutil.pep.impl;

import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.pdp.client.PDPClient;
import com.vangent.hieos.policyutil.pdp.model.PDPRequest;
import com.vangent.hieos.policyutil.pdp.model.PDPResponse;
import com.vangent.hieos.policyutil.pdp.model.XACMLRequestBuilder;
import com.vangent.hieos.policyutil.saml.model.SAML2Assertion;
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PEP {

    private final static Logger logger = Logger.getLogger(PEP.class);
    // Singletons.
    private static XConfigActor _pdpConfig = null;
    private XConfigActor configActor;
    private PDPResponse pdpResponse;

    /**
     *
     */
    private PEP() {
        // Do not allow.
    }

    /**
     *
     * @param configActor
     */
    public PEP(XConfigActor configActor) {
        this.configActor = configActor;
    }

    /**
     *
     * @return
     */
    public XConfigActor getConfigActor() {
        return configActor;
    }

    /**
     *
     * @return
     */
    public PDPResponse getPDPResponse() {
        return pdpResponse;
    }

    /**
     *
     * @param pdpResponse
     */
    public void setPDPResponse(PDPResponse pdpResponse) {
        this.pdpResponse = pdpResponse;
    }

    /**
     *
     * @return
     * @throws PolicyException
     */
    public PDPResponse evaluate() throws PolicyException {

        // Otherwise, go through the evaluation for the current SOAP action.
        String currentSOAPAction = PEP.getCurrentSOAPAction();
        if (logger.isInfoEnabled()) {
            logger.info("++ Evaluating policy for " + configActor.getName()
                    + " actor (SOAP action: " + currentSOAPAction + ") ++");
        }
        PDPResponse response = this.evaluate(currentSOAPAction);
        if (logger.isInfoEnabled()) {
            logger.info("... DECISION: " + response.getDecision().toString());
        }
        // Save the response.
        this.setPDPResponse(pdpResponse);
        return pdpResponse;
    }

    /**
     *
     * @return
     */
    public boolean isPolicyEnabled() {
        // First, see if we should conduct policy evaluation for the request.
        if (!configActor.isPolicyEnabled()) {
            if (logger.isInfoEnabled()) {
                logger.info("++ Not evaluating policy for " + configActor.getName() + " actor ++");
            }
            return false;  // Not evaluating policy.
        } else {
            // See if the current SOAP action is enabled for Policy evaluation.
            String currentSOAPAction = PEP.getCurrentSOAPAction();
            if (!configActor.isSOAPActionPolicyEnabled(currentSOAPAction)) {
                if (logger.isInfoEnabled()) {
                    logger.info("++ Not evaluating policy for " + configActor.getName()
                            + " actor (SOAP action: " + currentSOAPAction + ") ++");
                }
                return false;  // Not evaluating policy.
            }
        }
        return true;  // Policy is enabled.
    }

    /**
     *
     * @param action
     * @return
     * @throws PolicyException
     */
    private PDPResponse evaluate(String action) throws PolicyException {
        // Get the request to send.
        PDPRequest pdpRequest = this.getPDPRequest(action);

        // Conduct the evaluation.
        return this.evaluate(pdpRequest);
    }

    /**
     * 
     * @param pdpRequest
     * @return
     * @throws PolicyException
     */
    public PDPResponse evaluate(PDPRequest pdpRequest) throws PolicyException {
        XConfigActor pdpConfig = this.getPDPConfig();
        PDPClient pdpClient = new PDPClient(pdpConfig);

        // Issue the authorization request.
        return pdpClient.authorize(pdpRequest);
    }

    /**
     * 
     * @return
     */
    private synchronized XConfigActor getPDPConfig() {
        if (_pdpConfig != null) {
            return _pdpConfig;
        }
        // Initialize _pdpConfig singleton.

        // Get the PDP configuration.
        XConfig xconf = null;
        try {
            xconf = XConfig.getInstance();
        } catch (XConfigException ex) {
            throw new RuntimeException("Can not get xconfig to support policy evaluation: " + ex.getMessage());
        }
        _pdpConfig = (XConfigActor) xconf.getHomeCommunityConfig().getXConfigObjectWithName("pdp", "PolicyDecisionPointType");
        return _pdpConfig;
    }

    /**
     *
     * @param action
     * @return
     * @throws PolicyException
     */
    private PDPRequest getPDPRequest(String action) throws PolicyException {
        XACMLRequestBuilder builder = new XACMLRequestBuilder();
        OMElement assertionNode;
        try {
            //
            // May seem odd to place here, but we do not want a xutil->policyutil
            // dependency.  We want to maintain a policyutil->xutil dependency.
            //
            assertionNode = XAbstractService.getSAMLAssertionFromRequest();
        } catch (Exception ex) {
            throw new PolicyException("Unable to get SAML Assertion: " + ex.getMessage());
        }
        if (assertionNode == null) {
            throw new PolicyException("Unable to locate SAML Assertion");
        }
        SAML2Assertion assertion = new SAML2Assertion(assertionNode);
        PDPRequest pdpRequest = builder.buildPDPRequest(action, assertion);
        return pdpRequest;
    }

    /**
     *
     * @return
     */
    private static String getCurrentSOAPAction() {
        return MessageContext.getCurrentMessageContext().getSoapAction();
    }
}
