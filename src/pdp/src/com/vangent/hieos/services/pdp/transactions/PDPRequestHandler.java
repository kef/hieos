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
import com.vangent.hieos.services.pdp.attribute.finder.AttributeFinder;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xml.XMLParser;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

import org.jboss.security.xacml.core.JBossPDP;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.factories.RequestResponseContextFactory;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.jboss.security.xacml.interfaces.RequestContext;
import org.jboss.security.xacml.interfaces.ResponseContext;

import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.factories.XACMLContextFactory;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.JAXBElementMappingUtil;
import org.picketlink.identity.federation.core.saml.v2.util.SOAPSAMLXACMLUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion.XACMLAuthzDecisionStatementType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.protocol.XACMLAuthzDecisionQueryType;

/**
 *
 * @author Bernie Thuman
 */
public class PDPRequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(PDPRequestHandler.class);
    private static PolicyDecisionPoint _pdp = null;

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
    public OMElement run(OMElement request) throws AxisFault {
        try {
            log_message.setPass(true); // Hope for the best.
            Unmarshaller unmarshaller = SOAPSAMLXACMLUtil.getUnmarshaller();
            JAXBElement<?> jaxbElement = (JAXBElement<?>) unmarshaller.unmarshal(request.getXMLStreamReader());
            XACMLAuthzDecisionQueryType decisionQuery = (XACMLAuthzDecisionQueryType) jaxbElement.getValue();
            OMElement authorizeResponse = this.evaluate(decisionQuery);
            if (log_message.isLogEnabled()) {
                log_message.addOtherParam("Response", authorizeResponse);
            }
            return authorizeResponse;
        } catch (Exception ex) {
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            log_message.setPass(false);
            throw new AxisFault(ex.getMessage());
        }
    }

    /**
     * 
     * @param decisionQuery
     * @return
     */
    private OMElement evaluate(XACMLAuthzDecisionQueryType decisionQuery) throws PolicyException {
        try {
            RequestType requestType = decisionQuery.getRequest();
            this.addMissingAttributes(requestType);
            RequestContext requestContext = RequestResponseContextFactory.createRequestCtx();
            requestContext.setRequest(requestType);
            PolicyDecisionPoint pdp = this.getPDP();
            ResponseContext responseContext = pdp.evaluate(requestContext);
            return this.createSAML2Response(requestType, responseContext);
        } catch (Exception ex) {
            throw new PolicyException("Exception creating PDP response: " + ex.getMessage());
        }
    }

    /**
     *
     * @return
     */
    private synchronized PolicyDecisionPoint getPDP() {
        if (_pdp == null) {
            // FIXME: Had to add location of policyConfig in CLASSPATH
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            InputStream configInputStream = tcl.getResourceAsStream("policyConfig.xml");
            //File file = new File("C:/dev/hieos/src/XACMLutil/test/policyConfig.xml");
            //configInputStream = new FileInputStream(file);
            // FIXME: Cache the PDP ... it is thread-safe according to JBOSS folks.
            // Invoke the PDP.
            _pdp = new JBossPDP(configInputStream);
        }
        return _pdp;
    }

    /**
     *
     * @param requestType
     * @param responseContext
     * @return
     * @throws PolicyException
     */
    private OMElement createSAML2Response(RequestType requestType, ResponseContext responseContext) throws PolicyException {
        try {
            //
            // Always include the request as well ... in this way any additional attributes added from the PIP
            // will be included (in order to satisfy Obligations).
            //
            // Normally, you would pass in the ResponseType here (set to null)
            //
            XACMLAuthzDecisionStatementType xacmlDecisionStatement = XACMLContextFactory.createXACMLAuthzDecisionStatementType(requestType, null);

            // Place the XACML statement in an assertion
            // Then the assertion goes inside a SAML Response
            String ID = IDGenerator.create("ID_");
            SAML2Response saml2Response = new SAML2Response();
            IssuerInfoHolder issuerInfo = new IssuerInfoHolder("HIEOS PDP");
            List<StatementAbstractType> statements = new ArrayList<StatementAbstractType>();
            statements.add(xacmlDecisionStatement);

            // Create the assertion:
            AssertionType assertion = SAMLAssertionFactory.createAssertion(ID, issuerInfo.getIssuer(), XMLTimeUtil.getIssueInstant(), null, null, statements);
            org.picketlink.identity.federation.saml.v2.protocol.ResponseType saml2ResponseType = saml2Response.createResponseType(ID, issuerInfo, assertion);
            JAXBElement<?> jaxbResponse = JAXBElementMappingUtil.get(saml2ResponseType);

            // Convert JAXBElement to OMElement now
            OMElement responseOMElement = this.convertJAXBElementToOMElement(jaxbResponse);

            // Now, add in the ResponseType [converting like this due to bug in ResponseContext.getResponse()]
            // Convert byte array to OMElement
            OMElement responseContextOMElement = this.convertResponseContextToOMElement(responseContext);
            String[] namespacePrefixes = {"xacml-samlp", "saml2"};
            String[] namespaceURIs = {"urn:oasis:names:tc:SAML:2.0:protocol", "urn:oasis:names:tc:SAML:2.0:assertion"};
            OMElement stmt = XPathHelper.selectSingleNode(responseOMElement, "/xacml-samlp:Response/saml2:Assertion/saml2:Statement[1]", namespacePrefixes, namespaceURIs);
            // FIXME: Need to put response first.
            stmt.addChild(responseContextOMElement);
            return responseOMElement;
        } catch (Exception ex) {
            throw new PolicyException("Exception creating PDP response: " + ex.getMessage());
        }
    }

    /**
     * 
     * @param responseContext
     * @return
     * @throws PolicyException
     */
    private OMElement convertResponseContextToOMElement(ResponseContext responseContext) throws PolicyException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            responseContext.marshall(baos);
            // Convert byte array to OMElement
            OMElement responseContextOMElement = XMLParser.bytesToOM(baos.toByteArray());
            return responseContextOMElement;
        } catch (Exception ex) {
            throw new PolicyException("Exception creating PDP response: " + ex.getMessage());
        }
    }

    /**
     *
     * @param jaxbElement
     * @return
     * @throws PolicyException
     */
    private OMElement convertJAXBElementToOMElement(JAXBElement<?> jaxbElement) throws PolicyException {
        try {
            OMElement responseOMElement = null;
            StringWriter sw = new StringWriter();
            Marshaller marshaller = SOAPSAMLXACMLUtil.getMarshaller();
            marshaller.marshal(jaxbElement, sw);
            String xml = sw.toString();
            responseOMElement = XMLParser.stringToOM(xml);
            return responseOMElement;
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
        AttributeFinder attrFinder = new AttributeFinder(requestType);
        attrFinder.addMissingAttributes();
    }
}
