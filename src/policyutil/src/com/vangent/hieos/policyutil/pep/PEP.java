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
package com.vangent.hieos.policyutil.pep;

import com.vangent.hieos.policyutil.client.PDPClient;
import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.model.pdp.PDPRequest;
import com.vangent.hieos.policyutil.model.pdp.PDPResponse;
import com.vangent.hieos.policyutil.model.pdp.XACMLRequestBuilder;
import com.vangent.hieos.policyutil.model.saml.SAML2Assertion;
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import oasis.names.tc.xacml._2_0.context.schema.os.DecisionType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResponseType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResultType;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PEP {

    private final static Logger logger = Logger.getLogger(PEP.class);
    // Initialize singleton here.
    private static final PEPResponseContext _defaultPermitPEPResponseContext = new PEPResponseContext();

    static {
        // If policy evaluation is not active, force a PERMIT w/ no obligations.
        //_defaultPermitPEPResponseContext = new PEPResponseContext();
        PDPResponse pdpResponse = new PDPResponse();
        ResponseType responseType = new ResponseType();
        ResultType resultType = new ResultType();
        resultType.setDecision(DecisionType.PERMIT);
        responseType.getResult().add(resultType);
        pdpResponse.setResponseType(responseType);
        _defaultPermitPEPResponseContext.setPDPResponse(pdpResponse);
    }
    private PEPRequestContext evalContext;

    /**
     * 
     * @param evalContext
     */
    public PEP(PEPRequestContext evalContext) {
        this.evalContext = evalContext;
    }

    /**
     *
     * @return
     */
    public PEPRequestContext getEvalContext() {
        return evalContext;
    }

    /**
     *
     * @param evalContext
     */
    public void setEvalContext(PEPRequestContext evalContext) {
        this.evalContext = evalContext;
    }

    /**
     *
     * @return
     * @throws PolicyException
     */
    protected PDPRequest getPDPRequest() throws PolicyException {
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
        PDPRequest pdpRequest = builder.buildPDPRequest(evalContext.getAction(), assertion);
        return pdpRequest;
    }

    /**
     *
     * @return
     * @throws PolicyException
     */
    protected PEPResponseContext evaluate() throws PolicyException {
        // Get the PDP configuration.
        XConfig xconf = null;
        try {
            xconf = XConfig.getInstance();
        } catch (XConfigException ex) {
            throw new PolicyException("Can not get xconfig to support policy evaluation: " + ex.getMessage());
        }
        // FIXME: Cache PDPConfig.
        XConfigObject pdpConfig = xconf.getHomeCommunityConfig().getXConfigObjectWithName("pdp", "PolicyDecisionPointType");
        PDPClient pdpClient = new PDPClient((XConfigActor) pdpConfig);

        // Get the request to send.
        PDPRequest pdpRequest = this.getPDPRequest();

        // Issue the authorization request.
        PDPResponse pdpResponse = pdpClient.authorize(pdpRequest);

        // Return a PEPResponseContext (holds a PDPResponse).
        PEPResponseContext pepResponseCtx = new PEPResponseContext();
        pepResponseCtx.setPDPResponse(pdpResponse);
        return pepResponseCtx;
    }

    /**
     * 
     * @param configActor
     * @return
     * @throws PolicyException
     */
    public static PEPResponseContext evaluateCurrentRequest(XConfigActor configActor) throws PolicyException {
        // First, see if we should conduct policy evaluation for the request.
        if (!configActor.isPolicyEnabled()) {
            if (logger.isInfoEnabled()) {
                logger.info("++ Not evaluating policy for " + configActor.getName() + " actor ++");
            }
            // Permit if no evaluation.
            return _defaultPermitPEPResponseContext;
        } else {
            // See if the current SOAP action is enabled for Policy evaluation.
            String currentSOAPAction = PEP.getCurrentSOAPAction();
            if (!configActor.isSOAPActionPolicyEnabled(currentSOAPAction)) {
                if (logger.isInfoEnabled()) {
                    logger.info("++ Not evaluating policy for " + configActor.getName()
                            + " actor (SOAP action: " + currentSOAPAction + ") ++");
                }
                // Permit if no evaluation.
                return _defaultPermitPEPResponseContext;
            }
        }
        // Otherwise, go through the evaluation.
        PEPRequestContext requestCtx = new PEPRequestContext();
        String currentSOAPAction = PEP.getCurrentSOAPAction();
        if (logger.isInfoEnabled()) {
            logger.info("++ Evaluating policy for " + configActor.getName()
                    + " actor (SOAP action: " + currentSOAPAction + ") ++");
        }
        requestCtx.setAction(currentSOAPAction);
        PEP pep = new PEP(requestCtx);
        PEPResponseContext pepResponseCtx = pep.evaluate();
        if (logger.isInfoEnabled()) {
            logger.info("... DECISION: " + pepResponseCtx.getDecision().toString());
        }
        return pepResponseCtx;
    }

    /**
     *
     * @return
     */
    private static String getCurrentSOAPAction() {
        return MessageContext.getCurrentMessageContext().getSoapAction();
    }
}
