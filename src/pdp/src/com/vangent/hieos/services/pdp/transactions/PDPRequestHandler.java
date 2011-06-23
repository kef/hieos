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
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xml.XPathHelper;
import com.vangent.hieos.services.pdp.impl.PDPImpl;
import com.vangent.hieos.policyutil.util.PolicyUtil;
import com.vangent.hieos.services.pdp.attribute.finder.AttributeFinder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

import com.sun.xacml.Obligation;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
import com.vangent.hieos.policyutil.model.pdp.RequestTypeElement;
import com.vangent.hieos.policyutil.model.pdp.SAMLResponseElement;
import com.vangent.hieos.policyutil.model.pdp.XACMLRequestBuilder;
import com.vangent.hieos.policyutil.model.pdp.XACMLResponseBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;
import oasis.names.tc.xacml._2_0.context.schema.os.DecisionType;

import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResponseType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResultType;
import oasis.names.tc.xacml._2_0.context.schema.os.StatusCodeType;
import oasis.names.tc.xacml._2_0.context.schema.os.StatusType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeAssignmentType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EffectType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ObligationType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ObligationsType;


/**
 *
 * @author Bernie Thuman
 */
public class PDPRequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(PDPRequestHandler.class);
    private static PDPImpl _pdp = null;
    private ClassLoader classLoader;

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
     * @param messageType
     * @return
     * @throws AxisFault
     */
    public OMElement run(OMElement request, ClassLoader classLoader) throws AxisFault {
        try {
            this.classLoader = classLoader;
            log_message.setPass(true); // Hope for the best.
            RequestType requestType = this.getRequestType(request);
            SAMLResponseElement samlResponse = this.evaluate(requestType);
            if (log_message.isLogEnabled()) {
                log_message.addOtherParam("Response", samlResponse.getElement());
            }
            return samlResponse.getElement();
        } catch (Exception ex) {
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            log_message.setPass(false);
            throw new AxisFault(ex.getMessage());
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
            OMElement requestTypeNode = XPathHelper.selectSingleNode(request,
                    "./ns:Request[1]", "urn:oasis:names:tc:xacml:2.0:context:schema:os");

            XACMLRequestBuilder builder = new XACMLRequestBuilder();
            return builder.buildRequestType(new RequestTypeElement(requestTypeNode));

            /* JAXB free zone
            XMLStreamReader xsr = requestTypeOMElement.getXMLStreamReader();
            JAXBContext jc = PolicyUtil.getXACML_JAXBContext(this.classLoader);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            JAXBElement<RequestType> unmarshal = (JAXBElement<RequestType>) unmarshaller.unmarshal(xsr);
            return unmarshal.getValue();*/
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
     */
    private synchronized PDPImpl getPDP() throws PolicyException {
        if (_pdp == null) {
            try {
                // FIXME: Had to add location of policyConfig in CLASSPATH
                //ClassLoader tcl = Thread.currentThread().getContextClassLoader();
                //InputStream configInputStream = tcl.getResourceAsStream("policyConfig.xml");
                //File file = new File("C:/dev/hieos/src/XACMLutil/test/policyConfig.xml");
                //configInputStream = new FileInputStream(file);
                // FIXME: Cache the PDP ... is this safe?
                // FIXME:
                // Invoke the PDP.
                String[] policyFiles = {"c:/dev/hieos/config/policy/policies/australia-hie-policy.xml"};
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

    // FIXME: Move this code to a Builder ...
    
    /**
     * 
     * @param requestType
     * @throws PolicyException
     */
    private void addMissingAttributes(RequestType requestType) throws PolicyException {
        AttributeFinder attrFinder = new AttributeFinder(requestType);
        // Get missing attributes from the PIP.
        attrFinder.addMissingAttributes();
    }
}
