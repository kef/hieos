/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vangent.hieos.services.pdp.transactions;

import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xml.XMLParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
import org.jboss.security.xacml.core.model.context.ResponseType;
import org.jboss.security.xacml.core.model.context.ResultType;
import org.jboss.security.xacml.core.model.policy.AttributeAssignmentType;
import org.jboss.security.xacml.core.model.policy.ObligationType;
import org.jboss.security.xacml.core.model.policy.ObligationsType;
import org.jboss.security.xacml.factories.RequestResponseContextFactory;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.jboss.security.xacml.interfaces.RequestContext;
import org.jboss.security.xacml.interfaces.ResponseContext;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
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
 * @author thumbe
 */
public class PDPRequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(PDPRequestHandler.class);

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
            OMElement result = null;
            log_message.setPass(true); // Hope for the best.

            Unmarshaller unmarshaller = SOAPSAMLXACMLUtil.getUnmarshaller();
            JAXBElement<?> jaxbElement = (JAXBElement<?>) unmarshaller.unmarshal(request.getXMLStreamReader());
            XACMLAuthzDecisionQueryType decisionQuery = (XACMLAuthzDecisionQueryType) jaxbElement.getValue();
            return this.evaluate(decisionQuery);
            //return (result != null) ? result.getMessageNode() : null;
        } catch (JAXBException ex) {
            java.util.logging.Logger.getLogger(PDPRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * 
     * @param decisionQuery
     * @return
     */
    private OMElement evaluate(XACMLAuthzDecisionQueryType decisionQuery) {
        try {
            RequestType requestType = decisionQuery.getRequest();
            RequestContext requestContext = RequestResponseContextFactory.createRequestCtx();
            requestContext.setRequest(requestType);

            // FIXME: Had to add location of policyConfig in CLASSPATH
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            InputStream configInputStream = tcl.getResourceAsStream("policyConfig.xml");
            //File file = new File("C:/dev/hieos/src/XACMLutil/test/policyConfig.xml");
            //configInputStream = new FileInputStream(file);

            // FIXME: Cache the PDP ... it is thread-safe according to JBOSS folks.
            // Invoke the PDP.
            PolicyDecisionPoint pdp = new JBossPDP(configInputStream);
            ResponseContext responseContext = pdp.evaluate(requestContext);
            // Print the result
            this.print(responseContext.getResult());
            // Now we need to create the response ...
            ResponseType responseType = new ResponseType();
            ResultType resultType = responseContext.getResult();
            responseType.getResult().add(resultType);

            XACMLAuthzDecisionStatementType xacmlStatement =
                    XACMLContextFactory.createXACMLAuthzDecisionStatementType(requestType, responseType);

            //Place the xacml statement in an assertion
            //Then the assertion goes inside a SAML Response

            String ID = IDGenerator.create("ID_");
            SAML2Response saml2Response = new SAML2Response();
            IssuerInfoHolder issuerInfo = new IssuerInfoHolder("HIEOS PDP");

            List<StatementAbstractType> statements = new ArrayList<StatementAbstractType>();
            statements.add(xacmlStatement);

            AssertionType assertion = SAMLAssertionFactory.createAssertion(ID,
                    issuerInfo.getIssuer(),
                    XMLTimeUtil.getIssueInstant(),
                    null,
                    null,
                    statements);

            JAXBElement<?> jaxbResponse = JAXBElementMappingUtil.get(saml2Response.createResponseType(ID, issuerInfo, assertion));

            // Convert to OMElement now
            OMElement omResponse = this.convertJAXBElementToOMElement(jaxbResponse);
            return omResponse;

        } catch (ConfigurationException ex) {
            java.util.logging.Logger.getLogger(PDPRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PDPRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     *
     * @param jaxbElement
     * @return
     * @throws ProcessingException
     */
    private OMElement convertJAXBElementToOMElement(JAXBElement<?> jaxbElement) {
        try {
            OMElement response = null;
            StringWriter sw = new StringWriter();
            Marshaller marshaller = SOAPSAMLXACMLUtil.getMarshaller();
            marshaller.marshal(jaxbElement, sw);
            String xml = sw.toString();
            response = XMLParser.stringToOM(xml);
            return response;
        } catch (JAXBException ex) {
            java.util.logging.Logger.getLogger(PDPRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLParserException ex) {
            java.util.logging.Logger.getLogger(PDPRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     *
     * @param result
     */
    private void print(ResultType result) {
        System.out.println("Decision = " + result.getDecision());
        System.out.println("Obligations:");
        ObligationsType obligations = result.getObligations();
        if (obligations != null) {
            for (ObligationType obligationType : obligations.getObligation()) {
                System.out.println("... Id = " + obligationType.getObligationId());
                System.out.println("... fulfillOn.name = " + obligationType.getFulfillOn().name());
                System.out.println("... fulfillOn.value = " + obligationType.getFulfillOn().value());
                List<AttributeAssignmentType> attrAssignmentTypes = obligationType.getAttributeAssignment();
                for (AttributeAssignmentType attrAssignmentType : attrAssignmentTypes) {
                    System.out.println("..... attributeId = " + attrAssignmentType.getAttributeId());
                    System.out.println("..... dataType = " + attrAssignmentType.getDataType());
                }
            }
        }
        System.out.println("Status = " + result.getStatus().getStatusCode().getValue());
        System.out.println("Resource Id = " + result.getResourceId());
    }
}
