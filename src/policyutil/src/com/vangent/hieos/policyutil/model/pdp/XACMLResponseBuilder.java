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
package com.vangent.hieos.policyutil.model.pdp;

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
        String nsURI = PolicyConstants.XACML_CONTEXT_NS;
        String nsPrefix = PolicyConstants.XACML_CONTEXT_NS_PREFIX;
        OMFactory omfactory = OMAbstractFactory.getOMFactory();

        // Response
        OMElement responseNode = omfactory.createOMElement(new QName(nsURI, "Response", nsPrefix));

        // Result(s)
        for (ResultType resultType : responseType.getResult()) {
            String resourceId = resultType.getResourceId();
            String decision = resultType.getDecision().value();
            String statusCode = resultType.getStatus().getStatusCode().getValue();
            OMElement resultNode = omfactory.createOMElement(new QName(nsURI, "Result", nsPrefix));
            if (resourceId != null) {
                resultNode.addAttribute("ResourceId", resourceId, null);
            }
            resultNode.addAttribute("Decision", decision, null);
            OMElement statusNode = omfactory.createOMElement(new QName(nsURI, "Status", nsPrefix));
            OMElement statusCodeNode = omfactory.createOMElement(new QName(nsURI, "StatusCode", nsPrefix));
            statusCodeNode.setText(statusCode);
            statusNode.addChild(statusCodeNode);
            resultNode.addChild(statusNode);
            responseNode.addChild(resultNode);

            // Obligation(s).
            ObligationsType obligationsType = resultType.getObligations();
            if (obligationsType != null) {
                List<ObligationType> obligationTypes = resultType.getObligations().getObligation();
                if (!obligationTypes.isEmpty()) {
                    OMElement obligationsNode = omfactory.createOMElement(new QName(nsURI, "Obligations", nsPrefix));
                    resultNode.addChild(obligationsNode);
                    for (ObligationType obligationType : obligationTypes) {
                        EffectType fulfillOn = obligationType.getFulfillOn();
                        String obligationId = obligationType.getObligationId();
                        OMElement obligationNode = omfactory.createOMElement(new QName(nsURI, "Obligation", nsPrefix));
                        obligationNode.addAttribute("FulfillOn", fulfillOn.value(), null);
                        obligationNode.addAttribute("ObligationId", obligationId, null);
                        for (AttributeAssignmentType attributeAssignmentType : obligationType.getAttributeAssignment()) {
                            String attributeId = attributeAssignmentType.getAttributeId();
                            String dataType = attributeAssignmentType.getDataType();
                            OMElement attributeAssignmentNode = omfactory.createOMElement(new QName(nsURI, "AttributeAssignment", nsPrefix));
                            attributeAssignmentNode.addAttribute("AttributeId", attributeId, null);
                            attributeAssignmentNode.addAttribute("DataType", dataType, null);
                            // FIXME: Need to return AttributeAssignment Content!!!
                            obligationNode.addChild(attributeAssignmentNode);
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
    // FIXME: See if we can remove this conversion and go strait to OMElement
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
            // Find the Response
            OMElement responseNode = XPathHelper.selectSingleNode(samlResponseNode,
                    "./saml:Assertion/xacml-saml:XACMLAuthzDecisionStatement/xacml-context:Response[1]", nsPrefixes, nsURIs);

            ResponseType responseType = this.buildResponseType(new ResponseTypeElement(responseNode));
            pdpResponse.setResponseType(responseType);

            // Find the Request
            OMElement requestNode = XPathHelper.selectSingleNode(samlResponseNode,
                    "./saml:Assertion/xacml-saml:XACMLAuthzDecisionStatement/xacml-context:Request[1]", nsPrefixes, nsURIs);

            XACMLRequestBuilder requestBuilder = new XACMLRequestBuilder();
            RequestType requestType = requestBuilder.buildRequestType(new RequestTypeElement(requestNode));
            pdpResponse.setRequestType(requestType);
        } catch (XPathHelperException ex) {
            // FIXME: ? Do something ?
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

        // Convert RequestType to OMElement
        XACMLRequestBuilder requestBuilder = new XACMLRequestBuilder();
        OMElement requestNode = requestBuilder.buildRequestTypeElement(requestType).getElement();
        authzDecisionStatementNode.addChild(requestNode);

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
    private ResponseType buildResponseType(ResponseTypeElement responseTypeElement) {
        ResponseType responseType = new ResponseType();
        try {
            OMElement responseNode = responseTypeElement.getElement();
            String nsURI = PolicyConstants.XACML_CONTEXT_NS;

            // Result(s)
            Iterator<OMElement> resultNodes = responseNode.getChildrenWithName(
                    new QName(nsURI, "Result"));
            while (resultNodes.hasNext()) {
                // ResultType
                OMElement resultNode = resultNodes.next();
                String resourceId = resultNode.getAttributeValue(new QName("ResourceId"));
                String decision = resultNode.getAttributeValue(new QName("Decision"));
                ResultType resultType = new ResultType();
                resultType.setResourceId(resourceId);

                // DecisionType
                DecisionType decisionType = DecisionType.fromValue(decision);
                resultType.setDecision(decisionType);

                // StatusType
                OMElement statusCodeNode = XPathHelper.selectSingleNode(resultNode, "./ns:Status/ns:StatusCode[1]", nsURI);
                if (statusCodeNode != null) {
                    // FIXME: Handle status code messages, etc.
                    String statusText = statusCodeNode.getText();
                    StatusType statusType = new StatusType();
                    StatusCodeType statusCodeType = new StatusCodeType();
                    statusCodeType.setValue(statusText);
                    statusType.setStatusCode(statusCodeType);
                    resultType.setStatus(statusType);
                }

                // Obligation(s)
                List<OMElement> obligationNodes = XPathHelper.selectNodes(resultNode, "./ns:Obligations/ns:Obligation", nsURI);
                if (obligationNodes != null && !obligationNodes.isEmpty()) {
                    ObligationsType obligationsType = new ObligationsType();
                    resultType.setObligations(obligationsType);
                    for (OMElement obligationNode : obligationNodes) {
                        String obligationId = obligationNode.getAttributeValue(new QName("ObligationId"));
                        String fulfillOn = obligationNode.getAttributeValue(new QName("FulfillOn"));
                        ObligationType obligationType = new ObligationType();
                        obligationType.setObligationId(obligationId);
                        obligationType.setFulfillOn(EffectType.fromValue(fulfillOn));
                        //  FIXME: Content
                        obligationsType.getObligation().add(obligationType);
                    }
                }
                responseType.getResult().add(resultType);
            }

        } catch (XPathHelperException ex) {
            // FIXME: Do something?
        }
        return responseType;
    }
}
