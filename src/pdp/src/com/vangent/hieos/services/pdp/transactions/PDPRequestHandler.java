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
package com.vangent.hieos.services.pdp.transactions;

import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xml.XPathHelper;
import com.vangent.hieos.services.pdp.impl.PDPImpl;
import com.vangent.hieos.services.pdp.attribute.finder.AttributeFinder;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

import com.sun.xacml.ctx.ResponseCtx;
import com.vangent.hieos.policyutil.model.pdp.RequestTypeElement;
import com.vangent.hieos.policyutil.model.pdp.SAMLResponseElement;
import com.vangent.hieos.policyutil.model.pdp.XACMLRequestBuilder;
import com.vangent.hieos.policyutil.model.pdp.XACMLResponseBuilder;
import com.vangent.hieos.policyutil.util.PolicyConfig;
import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import java.util.List;

import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResponseType;

/**
 *
 * @author Bernie Thuman
 */
public class PDPRequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(PDPRequestHandler.class);
    private static PDPImpl _pdp = null;  // FIXME: Single instance (safe?)
    private static XConfigActor _pdpConfig = null;
    private static XConfigActor _pipConfig = null;

    /**
     *
     */
    private PDPRequestHandler() {
        // Do nothing.
    }

    /**
     *
     * @param log_message
     * @param mCtx
     */
    public PDPRequestHandler(XLogMessage log_message, MessageContext mCtx) {
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
            this.validate(request);
            RequestType requestType = this.getRequestType(request);
            SAMLResponseElement samlResponse = this.evaluate(requestType);
            if (log_message.isLogEnabled()) {
                log_message.addOtherParam("Response", samlResponse.getElement());
            }
            this.validate(samlResponse.getElement());
            return samlResponse.getElement();
        } catch (Exception ex) {
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            log_message.setPass(false);
            throw new AxisFault(ex.getMessage());
        }
    }

    /**
     * 
     * @param node
     */
    private void validate(OMElement node) {
        // FIXME: TBD
        //try {
        //    String schemaLocation =
        //            PolicyConstants.XACML_SAML_PROTOCOL_NS
        //            + " "
        //            + "c:\\dev\\hieos\\config\\schema\\xacml\\access_control-xacml-2.0-saml-protocol-schema-os.xsd";
        //    XMLSchemaValidator validator = new XMLSchemaValidator(schemaLocation);
        //    validator.validate(request);
        //} catch (XMLSchemaValidatorException ex) {
        //    java.util.logging.Logger.getLogger(PDPRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        //}
    }

    /**
     *
     * @param request
     * @return
     * @throws PolicyException
     */
    private RequestType getRequestType(OMElement request) throws PolicyException {
        try {
            // Locate Request node.
            OMElement requestTypeNode = XPathHelper.selectSingleNode(request,
                    "./ns:Request[1]", PolicyConstants.XACML_CONTEXT_NS);

            // Build OASIS RequestType.
            XACMLRequestBuilder builder = new XACMLRequestBuilder();
            return builder.buildRequestType(new RequestTypeElement(requestTypeNode));
        } catch (Exception ex) {
            throw new PolicyException("Unable to marshall request: " + ex.getMessage());
        }
    }

    /**
     * 
     * @param requestType
     * @return
     * @throws PolicyException
     */
    private SAMLResponseElement evaluate(RequestType requestType) throws PolicyException {
        try {
            this.addMissingAttributes(requestType);
            PDPImpl pdp = this.getPDP();
            ResponseCtx responseCtx = pdp.evaluate(requestType);
            // DEBUG:
            responseCtx.encode(System.out);
            return this.createSAML2Response(requestType, responseCtx);
        } catch (Exception ex) {
            throw new PolicyException("Exception creating PDP response: " + ex.getMessage());
        }
    }

    /**
     * 
     * @return
     * @throws PolicyException
     */
    private synchronized PDPImpl getPDP() throws PolicyException {
        if (_pdp == null) {
            try {
                PolicyConfig pConfig = PolicyConfig.getInstance();
                List<String> policyFiles = pConfig.getPolicyFiles();
                // FIXME: Cache the PDP ... is this safe?
                // Invoke the PDP.
                _pdp = new PDPImpl(policyFiles);
            } catch (Exception ex) {
                throw new PolicyException("Unable to create PDPImpl: " + ex.getMessage());
            }
        }
        return _pdp;
    }

    /**
     *
     * @param requestType
     * @param responseCtx
     * @return
     * @throws PolicyException
     */
    private SAMLResponseElement createSAML2Response(RequestType requestType, ResponseCtx responseCtx) throws PolicyException {
        try {
            XACMLResponseBuilder builder = new XACMLResponseBuilder();
            ResponseType responseType = builder.buildResponseType(responseCtx);
            return builder.buildSAMLResponse(requestType, responseType);
        } catch (Exception ex) {
            throw new PolicyException("Exception creating PDP response: " + ex.getMessage());
        }
    }

    /**
     * 
     * @param requestType
     * @throws PolicyException
     */
    private void addMissingAttributes(RequestType requestType) throws PolicyException {
        AttributeFinder attrFinder = new AttributeFinder(this.getPIPConfig(), requestType);
        // Get missing attributes from the PIP.
        attrFinder.addMissingAttributes();
    }

    /**
     *
     * @return
     */
    private static synchronized XConfigActor getPDPConfig() {
        try {
            if (_pdpConfig == null) {
                XConfig xconf = XConfig.getInstance();
                // Get the home community config.
                XConfigObject homeCommunityConfig = xconf.getHomeCommunityConfig();
                _pdpConfig = (XConfigActor) homeCommunityConfig.getXConfigObjectWithName("pdp", "PolicyDecisionPointType");
            }
        } catch (XConfigException ex) {
            // FIXME: Do something.
        }
        return _pdpConfig;
    }

    /**
     * 
     * @return
     */
    private static synchronized XConfigActor getPIPConfig() {
        if (_pipConfig == null) {
            XConfigActor pdpConfig = PDPRequestHandler.getPDPConfig();
            _pipConfig = (XConfigActor) pdpConfig.getXConfigObjectWithName("pip", "PolicyInformationPointType");
        }
        return _pipConfig;
    }
}
