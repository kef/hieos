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
import com.vangent.hieos.policyutil.pdp.impl.PDPImpl;
import com.vangent.hieos.policyutil.pdp.model.PDPRequest;
import com.vangent.hieos.policyutil.pdp.model.RequestTypeElement;
import com.vangent.hieos.policyutil.pdp.model.SAMLResponseElement;
import com.vangent.hieos.policyutil.pdp.model.XACMLRequestBuilder;
import com.vangent.hieos.policyutil.pdp.model.XACMLResponseBuilder;
import com.vangent.hieos.policyutil.pdp.resource.PIPResourceContentFinder;
import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.xml.XPathHelper;

import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResponseType;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import com.sun.xacml.ctx.ResponseCtx;

import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Unable to test this the right way
 *
 * @author Vangent
 */
public class JUnitPDPRequestHandler {
    private final static Logger logger = Logger.getLogger(PDPRequestHandler.class);

    public JUnitPDPRequestHandler() {
        super();
    }

    /**
     *
     * @param request
     * @return
     * @throws SOAPFaultException
     */
    public OMElement run(OMElement request) throws SOAPFaultException {
        try {
            RequestType requestType = this.getRequestType(request);

            this.addResourceContent(requestType);

            boolean returnContext = getReturnContext(request);
            SAMLResponseElement samlResponse  = evaluate(requestType, 
                    returnContext);

            return samlResponse.getElement();
            
        } catch (Exception ex) {
            throw new SOAPFaultException(ex.getMessage());
        }
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
            OMElement requestTypeNode = XPathHelper.selectSingleNode(request, "./ns:Request[1]",
                                            PolicyConstants.XACML_CONTEXT_NS);

            // Build OASIS RequestType.
            XACMLRequestBuilder builder = new XACMLRequestBuilder();

            return builder.buildRequestType(new RequestTypeElement(requestTypeNode));
        } catch (Exception ex) {
            throw new PolicyException("Unable to marshall request: " + ex.getMessage());
        }
    }

    /**
     *
     * @param request
     * @return
     * @throws PolicyException
     */
    private boolean getReturnContext(OMElement request) throws PolicyException {
        try {

            // Locate Request node.
            String returnContext = request.getAttributeValue(new QName("ReturnContext"));

            if ((returnContext == null) || returnContext.equalsIgnoreCase("false")) {
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            throw new PolicyException("Unable to marshall request: " + ex.getMessage());
        }
    }

    /**
     *
     * @param requestType
     * @param returnContext
     * @return
     * @throws PolicyException
     */
    private SAMLResponseElement evaluate(RequestType requestType, boolean returnContext) throws PolicyException {
        try {
            PDPImpl pdp = getPDP();
            ResponseCtx responseCtx = pdp.evaluate(requestType);

            if (logger.isDebugEnabled()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                responseCtx.encode(baos);
                logger.debug("XACML Engine Response: " + baos.toString());
            }

            return this.createSAML2Response(returnContext
                                            ? requestType
                                            : null, responseCtx);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);

            throw new PolicyException("Exception creating PDP response: " + ex.getMessage());
        }
    }

    /**
     *
     * @return
     * @throws PolicyException
     */
    private PDPImpl getPDP() throws Exception {

        List<String> policyFiles = new ArrayList<String>();
        policyFiles.add("test/resources/policies/test-hie-policy.xml");
        policyFiles.add("test/resources/policies/test-hie-document-policy.xml");
        
        return new PDPImpl(policyFiles);
    }

    /**
     *
     * @param requestType
     * @param responseCtx
     * @return
     * @throws PolicyException
     */
    private SAMLResponseElement createSAML2Response(RequestType requestType, ResponseCtx responseCtx)
            throws PolicyException {
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
    private void addResourceContent(RequestType requestType) throws PolicyException {
        PIPResourceContentFinder resourceContentFinder = new PIPResourceContentFinder(null);

        // Get ResourceContent from the PIP.
        PDPRequest pdpRequest = new PDPRequest();

        pdpRequest.setRequestType(requestType);
        resourceContentFinder.addResourceContentToRequest(pdpRequest);
    }
}
