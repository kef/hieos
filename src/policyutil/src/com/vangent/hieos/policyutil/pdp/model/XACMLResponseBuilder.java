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
package com.vangent.hieos.policyutil.pdp.model;

import com.sun.xacml.Obligation;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.xml.namespace.QName;
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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

/**
 *
 * @author Bernie Thuman
 */
public class XACMLResponseBuilder {

    /**
     * Builds an OMElement(XML) from an OASIS ResponseType.
     *
     * @param responseType
     * @return
     * @throws PolicyException
     */
    public ResponseTypeElement buildResponseTypeElement(ResponseType responseType) throws PolicyException {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();

        // Response
        OMElement responseNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "Response", PolicyConstants.XACML_CONTEXT_NS_PREFIX));

        // Result(s)
        for (ResultType resultType : responseType.getResult()) {
            String resourceId = resultType.getResourceId();
            String decision = resultType.getDecision().value();
            StatusType statusType = resultType.getStatus();
            String statusCode = statusType.getStatusCode().getValue();
            String statusMessage = statusType.getStatusMessage();

            // Result.
            OMElement resultNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "Result", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
            responseNode.addChild(resultNode);
            if (resourceId != null) {
                resultNode.addAttribute("ResourceId", resourceId, null);
            }

            // Decision.
            OMElement decisionNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "Decision", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
            decisionNode.setText(decision);
            resultNode.addChild(decisionNode);

            // Status.
            OMElement statusNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "Status", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
            OMElement statusCodeNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "StatusCode", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
            statusCodeNode.addAttribute("Value", statusCode, null);
            statusNode.addChild(statusCodeNode);
            if (statusMessage != null) {
                OMElement statusMessageNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "StatusMessage", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
                statusMessageNode.setText(statusMessage);
                statusNode.addChild(statusMessageNode);
            }
            resultNode.addChild(statusNode);

            // Obligation(s).
            ObligationsType obligationsType = resultType.getObligations();
            if (obligationsType != null) {
                List<ObligationType> obligationTypes = resultType.getObligations().getObligation();
                if (!obligationTypes.isEmpty()) {
                    OMElement obligationsNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_NS, "Obligations", PolicyConstants.XACML_NS_PREFIX));
                    resultNode.addChild(obligationsNode);
                    for (ObligationType obligationType : obligationTypes) {
                        EffectType fulfillOn = obligationType.getFulfillOn();
                        String obligationId = obligationType.getObligationId();
                        OMElement obligationNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_NS, "Obligation", PolicyConstants.XACML_NS_PREFIX));
                        obligationNode.addAttribute("FulfillOn", fulfillOn.value(), null);
                        obligationNode.addAttribute("ObligationId", obligationId, null);
                        for (AttributeAssignmentType attributeAssignmentType : obligationType.getAttributeAssignment()) {
                            String attributeId = attributeAssignmentType.getAttributeId();
                            String dataType = attributeAssignmentType.getDataType();
                            OMElement attributeAssignmentNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_NS, "AttributeAssignment", PolicyConstants.XACML_NS_PREFIX));
                            attributeAssignmentNode.addAttribute("AttributeId", attributeId, null);
                            attributeAssignmentNode.addAttribute("DataType", dataType, null);
                            // FIXME - how to handle Obligation content?
                            //if (!attributeAssignmentType.getContent().isEmpty()) {
                            //    System.out.println("++++ type: " +
                            //            attributeAssignmentType.getContent().get(0).getClass().getCanonicalName());
                            //    /*
                            //    Element contentValueElement = (Element) attributeAssignmentType.getContent().get(0);
                            //    try {
                            //        OMElement contentValueNode = XMLParser.convertDOMtoOM(contentValueElement);
                            //        attributeAssignmentNode.addChild(contentValueNode);
                            //    } catch (XMLParserException ex) {
                            //        throw new PolicyException("Unable to convert DOM to OM: " + ex.getMessage());
                            //    }*/
                            //}
                            //obligationNode.addChild(attributeAssignmentNode);
                        }
                        obligationsNode.addChild(obligationNode);
                    }
                }
            }
        }
        return new ResponseTypeElement(responseNode);
    }

    /**
     * Builds an OASIS ResponseType from a sunxacml ResponseCtx.
     *
     * @param responseCtx
     * @return
     */
    // FIXME?: See if we can remove this conversion and go strait to OMElement
    public ResponseType buildResponseType(ResponseCtx responseCtx) {
        //
        // This method is needed since JAXB will not work directly on XACML response generated by
        // sunxacml implementation and there is no accomodation for namespaces in RequestCtx.encode(..)
        //
        ResponseType responseType = new ResponseType();
        Set<Result> results = responseCtx.getResults();
        Iterator<Result> it = results.iterator();

        // Loop through results.
        while (it.hasNext()) {
            Result result = it.next();
            ResultType resultType = new ResultType();
            resultType.setResourceId(result.getResource());
            int decision = result.getDecision();
            switch (decision) {
                case Result.DECISION_PERMIT:
                    resultType.setDecision(DecisionType.PERMIT);
                    break;
                case Result.DECISION_DENY:
                    resultType.setDecision(DecisionType.DENY);
                    break;
                case Result.DECISION_NOT_APPLICABLE:
                    resultType.setDecision(DecisionType.NOT_APPLICABLE);
                    break;
                case Result.DECISION_INDETERMINATE:
                default:
                    resultType.setDecision(DecisionType.INDETERMINATE);
                    break;
            }

            // Status
            Status status = result.getStatus();
            StatusType statusType = new StatusType();
            StatusCodeType statusCodeType = new StatusCodeType();
            List<String> statusList = status.getCode();
            if (statusList != null && statusList.size() > 0) {
                statusCodeType.setValue(statusList.get(0));
            }
            statusType.setStatusMessage(status.getMessage());
            statusType.setStatusCode(statusCodeType);
            resultType.setStatus(statusType);

            // Obligations
            Set<Obligation> obligationsSet = result.getObligations();
            if (obligationsSet != null) {
                ObligationsType obligationsType = new ObligationsType();
                for (Obligation obl : obligationsSet) {
                    ObligationType obType = new ObligationType();
                    obType.setObligationId(obl.getId().toASCIIString());
                    obType.setFulfillOn(EffectType.fromValue(Result.DECISIONS[obl.getFulfillOn()]));
                    List<Attribute> assignments = obl.getAssignments();
                    Iterator<Attribute> attrIt = assignments.iterator();
                    while (attrIt.hasNext()) {
                        Attribute attr = attrIt.next();
                        AttributeAssignmentType attrAssignmentType = new AttributeAssignmentType();
                        attrAssignmentType.setAttributeId(attr.getId().toString());
                        attrAssignmentType.setDataType(attr.getType().toString());
                        attrAssignmentType.getContent().add(attr.getValue().encode());
                        obType.getAttributeAssignment().add(attrAssignmentType);
                    }
                    obligationsType.getObligation().add(obType);
                }
                if (obligationsSet.size() > 0) {
                    resultType.setObligations(obligationsType);
                }
            }
            // Add ResultType to ResponseType
            responseType.getResult().add(resultType);
        }
        return responseType;
    }

    /**
     * Builds a PDPResponse from a SAMLResponseElement / OMElement(XML).
     *
     * @param samlResponse
     * @return
     */
    public PDPResponse buildPDPResponse(SAMLResponseElement samlResponse) throws PolicyException {
        PDPResponse pdpResponse = new PDPResponse();
        OMElement samlResponseNode = samlResponse.getElement();

        // Not using constants here (as the constants may change but these prefixes should stay
        // as they are only used to support xpath.
        String nsPrefixes[] = {
            "xacml-saml",
            "xacml-context",
            "saml"};
        String nsURIs[] = {
            PolicyConstants.XACML_SAML_NS,
            PolicyConstants.XACML_CONTEXT_NS,
            PolicyConstants.SAML2_NS};

        try {
            // Find Response
            OMElement responseNode = XPathHelper.selectSingleNode(samlResponseNode,
                    "./saml:Assertion/xacml-saml:XACMLAuthzDecisionStatement/xacml-context:Response[1]", nsPrefixes, nsURIs);
            // Build ResponseType.
            ResponseType responseType = this.buildResponseType(new ResponseTypeElement(responseNode));
            pdpResponse.setResponseType(responseType);

            // Find Request
            OMElement requestNode = XPathHelper.selectSingleNode(samlResponseNode,
                    "./saml:Assertion/xacml-saml:XACMLAuthzDecisionStatement/xacml-context:Request[1]", nsPrefixes, nsURIs);

            if (requestNode != null) {
                XACMLRequestBuilder requestBuilder = new XACMLRequestBuilder();
                // Build RequestType.
                RequestType requestType = requestBuilder.buildRequestType(new RequestTypeElement(requestNode));
                pdpResponse.setRequestType(requestType);
            }
        } catch (XPathHelperException ex) {
            throw new PolicyException("Failure to build PDP Response", ex);
        }
        return pdpResponse;
    }

    /**
     * Builds an OMElement(XML) from OASIS RequestType/ResponseType.
     *
     * @param requestType
     * @param responseType
     * @return
     * @throws PolicyException
     */
    public SAMLResponseElement buildSAMLResponse(RequestType requestType, ResponseType responseType) throws PolicyException {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();

        // <saml2p:Response Version="2.0" xmlns:saml2p="urn:oasis:names:tc:SAML:2.0:protocol">
        OMElement saml2pResponseNode = omfactory.createOMElement(
                new QName(PolicyConstants.SAML2_PROTOCOL_NS, "Response", PolicyConstants.SAML2_PROTOCOL_NS_PREFIX));
        saml2pResponseNode.addAttribute("Version", "2.0", null);

        // <saml2:Assertion ID="2607abfd-36d6-4260-9d7b-1c79a1bce458" Version="2.0" xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">
        OMElement assertionNode = omfactory.createOMElement(
                new QName(PolicyConstants.SAML2_NS, "Assertion", PolicyConstants.SAML2_NS_PREFIX));
        assertionNode.addAttribute("Version", "2.0", null);
        assertionNode.addAttribute("ID", UUID.randomUUID().toString(), null);
        saml2pResponseNode.addChild(assertionNode);

        //<xacml-saml:XACMLAuthzDecisionStatement xmlns:xacml-saml="urn:oasis:names:tc:xacml:2.0:profile:saml2.0:v2:schema:assertion">
        OMElement authzDecisionStatementNode = omfactory.createOMElement(
                new QName(PolicyConstants.XACML_SAML_NS, "XACMLAuthzDecisionStatement", PolicyConstants.XACML_SAML_NS_PREFIX));
        assertionNode.addChild(authzDecisionStatementNode);

        if (requestType != null) {
            // Convert RequestType to OMElement
            XACMLRequestBuilder requestBuilder = new XACMLRequestBuilder();
            OMElement requestNode = requestBuilder.buildRequestTypeElement(requestType).getElement();
            authzDecisionStatementNode.addChild(requestNode);
        }

        // Convert ResponseType to OMElement
        OMElement responseNode = this.buildResponseTypeElement(responseType).getElement();
        authzDecisionStatementNode.addChild(responseNode);

        return new SAMLResponseElement(saml2pResponseNode);
    }

    /**
     * Builds an OASIS ResponseType from an OMElement(XML).
     *
     * @param responseTypeElement
     * @return
     */
    private ResponseType buildResponseType(ResponseTypeElement responseTypeElement) throws PolicyException {
        ResponseType responseType = new ResponseType();
        try {
            OMElement responseNode = responseTypeElement.getElement();

            // Result(s)
            Iterator<OMElement> resultNodes = responseNode.getChildrenWithName(
                    new QName(PolicyConstants.XACML_CONTEXT_NS, "Result"));
            while (resultNodes.hasNext()) {
                // ResultType
                OMElement resultNode = resultNodes.next();
                String resourceId = resultNode.getAttributeValue(new QName("ResourceId"));

                // Decision
                OMElement decisionNode = resultNode.getFirstChildWithName(new QName(PolicyConstants.XACML_CONTEXT_NS, "Decision"));
                String decision = decisionNode.getText();
                ResultType resultType = new ResultType();
                resultType.setResourceId(resourceId);

                // DecisionType
                DecisionType decisionType = DecisionType.fromValue(decision);
                resultType.setDecision(decisionType);

                // StatusType
                OMElement statusCodeNode = XPathHelper.selectSingleNode(resultNode, "./ns:Status/ns:StatusCode[1]", PolicyConstants.XACML_CONTEXT_NS);
                if (statusCodeNode != null) {
                    String statusText = statusCodeNode.getAttributeValue(new QName("Value"));
                    StatusType statusType = new StatusType();
                    StatusCodeType statusCodeType = new StatusCodeType();
                    statusCodeType.setValue(statusText);
                    statusType.setStatusCode(statusCodeType);

                    // Optional: StatusMessage
                    OMElement statusMessageNode = XPathHelper.selectSingleNode(resultNode, "./ns:Status/ns:StatusMessage[1]", PolicyConstants.XACML_CONTEXT_NS);
                    if (statusMessageNode != null) {
                        String statusMessageText = statusMessageNode.getText();
                        statusType.setStatusMessage(statusMessageText);
                    }
                    resultType.setStatus(statusType);
                }

                // Obligation(s)
                List<OMElement> obligationNodes = XPathHelper.selectNodes(resultNode, "./ns:Obligations/ns:Obligation", PolicyConstants.XACML_NS);
                if (obligationNodes != null && !obligationNodes.isEmpty()) {
                    ObligationsType obligationsType = new ObligationsType();
                    resultType.setObligations(obligationsType);
                    for (OMElement obligationNode : obligationNodes) {
                        String obligationId = obligationNode.getAttributeValue(new QName("ObligationId"));
                        String fulfillOn = obligationNode.getAttributeValue(new QName("FulfillOn"));
                        ObligationType obligationType = new ObligationType();
                        obligationType.setObligationId(obligationId);
                        obligationType.setFulfillOn(EffectType.fromValue(fulfillOn));
                        //  FIXME: how to handle Obligation content?
                        obligationsType.getObligation().add(obligationType);
                    }
                }
                responseType.getResult().add(resultType);
            }
        } catch (XPathHelperException ex) {
            throw new PolicyException("Failure to build XACML ResponseType", ex);
        }
        return responseType;
    }
}
