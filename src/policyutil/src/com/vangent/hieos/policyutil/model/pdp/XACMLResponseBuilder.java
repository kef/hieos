/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
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

    //<Response xmlns="urn:oasis:names:tc:xacml:2.0:context:schema:os">
    //   <Result ResourceId="PID^^^1">
    //      <Decision>Permit</Decision>
    //      <Status>
    //        <StatusCode Value="urn:oasis:names:tc:xacml:1.0:status:ok"/>
    //      </Status>
    //      <ns2:Obligations>
    //        <ns2:Obligation FulfillOn="Permit" ObligationId="test-obligation">
    //            <ns2:AttributeAssignment AttributeId="urn:oasis:names:tc:xacml:2.0:example:attribute:text" DataType="http://www.w3.org/2001/XMLSchema#string">&lt;SubjectAttributeDesignator AttributeId="urn:oasis:names:tc:xspa:1.0:subject:organization-id" DataType="http://www.w3.org/2001/XMLSchema#string"/></ns2:AttributeAssignment>
    //        </ns2:Obligation>
    //        <ns2:Obligation FulfillOn="Permit" ObligationId="sensitive-doc-types-obligation">
    //            <ns2:AttributeAssignment AttributeId="urn:oasis:names:tc:xspa:1.0:resource:sensitive-document-types" DataType="http://www.w3.org/2001/XMLSchema#string">&lt;ResourceAttributeDesignator AttributeId="urn:oasis:names:tc:xspa:1.0:resource:sensitive-document-types" DataType="http://www.w3.org/2001/XMLSchema#string"/></ns2:AttributeAssignment>
    //            <ns2:AttributeAssignment AttributeId="urn:oasis:names:tc:xacml:2.0:example:attribute:text" DataType="http://www.w3.org/2001/XMLSchema#string">Your medical record has been accessed</ns2:AttributeAssignment>
    //        </ns2:Obligation>
    //      </ns2:Obligations>
    //   </Result>
    // </Response>
    /**
     * 
     * @param responseType
     * @return
     * @throws PolicyException
     */
    public ResponseTypeElement buildResponseTypeElement(ResponseType responseType) throws PolicyException {
// FIXME: namespace hardcoding
        String nsURI = "urn:oasis:names:tc:xacml:2.0:context:schema:os";
        String nsPrefix = "xacml-context";
        OMFactory omfactory = OMAbstractFactory.getOMFactory();

        // Response
        OMElement responseNode = omfactory.createOMElement(new QName(nsURI, "Response", nsPrefix));

        // Result(s)
        for (ResultType resultType : responseType.getResult()) {
            String resourceId = resultType.getResourceId();
            String decision = resultType.getDecision().value();
            String statusCode = resultType.getStatus().getStatusCode().getValue();
            OMElement resultNode = omfactory.createOMElement(new QName(nsURI, "Result", nsPrefix));
            resultNode.addAttribute("ResourceId", resourceId, null);
            resultNode.addAttribute("Decision", decision, null);
            OMElement statusNode = omfactory.createOMElement(new QName(nsURI, "Status", nsPrefix));
            OMElement statusCodeNode = omfactory.createOMElement(new QName(nsURI, "StatusCode", nsPrefix));
            statusCodeNode.setText(statusCode);
            statusNode.addChild(statusCodeNode);
            resultNode.addChild(statusNode);
            responseNode.addChild(resultNode);

            // Obligation(s).
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
                        // FIXME: Content????
                        obligationNode.addChild(attributeAssignmentNode);
                    }
                    obligationsNode.addChild(obligationNode);
                }
            }
        }

        return new ResponseTypeElement(responseNode);
    }

    /**
     *
     * @param responseCtx
     * @return
     */
    // FIXME: See if we can remove this conversion and go strait to OMElement
    public ResponseType buildResponseType(ResponseCtx responseCtx) {
        //
        // This method is needed since JAXB will not work directly on XACML response generated by
        // sunxacml implementation and their is no accomodation for namespaces in RequestCtx.encode(..)
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

            //Status
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

            //Obligations
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
     * 
     * @param result
     * @return
     */
    public PDPResponse buildPDPResponse(SAMLResponseElement samlResponse) {
        PDPResponse pdpResponse = new PDPResponse();

        // TBD: Implement ...
        // TBD: Obligations
        return pdpResponse;
    }

    /**
     * 
     * @return
     */
    public SAMLResponseElement buildSAMLResponse(RequestType requestType, ResponseType responseType) throws PolicyException {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();

        //     <saml2p:Response Version="2.0" xmlns:saml2p="urn:oasis:names:tc:SAML:2.0:protocol">
        OMElement saml2pResponseNode = omfactory.createOMElement(new QName("urn:oasis:names:tc:SAML:2.0:protocol", "Response", "saml2p"));
        saml2pResponseNode.addAttribute("Version", "2.0", null);

        // <saml2:Assertion ID="2607abfd-36d6-4260-9d7b-1c79a1bce458" Version="2.0" xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">
        OMElement assertionNode = omfactory.createOMElement(new QName("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion", "saml"));
        assertionNode.addAttribute("Version", "2.0", null);
        assertionNode.addAttribute("ID", UUID.randomUUID().toString(), null);
        saml2pResponseNode.addChild(assertionNode);

        //<xacml-saml:XACMLAuthzDecisionStatement xmlns:xacml-saml="urn:oasis:names:tc:xacml:2.0:profile:saml2.0:v2:schema:assertion">
        OMElement authzDecisionStatementNode = omfactory.createOMElement(new QName("urn:oasis:names:tc:xacml:2.0:profile:saml2.0:v2:schema:assertion", "XACMLAuthzDecisionStatement", "xacml-saml"));
        assertionNode.addChild(authzDecisionStatementNode);

        // Convert JAXB RequestType to OMElement
        XACMLRequestBuilder requestBuilder = new XACMLRequestBuilder();
        OMElement requestNode = requestBuilder.buildRequestTypeElement(requestType).getElement();
        authzDecisionStatementNode.addChild(requestNode);

        // Convert JAXB ResponseType to OMElement
        OMElement responseNode = this.buildResponseTypeElement(responseType).getElement();
        authzDecisionStatementNode.addChild(responseNode);

        return new SAMLResponseElement(saml2pResponseNode);
    }
}
