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

import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.policyutil.saml.model.SAML2Assertion;
import com.vangent.hieos.policyutil.util.AttributeConfig;
import com.vangent.hieos.policyutil.util.PolicyConfig;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.hl7.formatutil.HL7FormatUtil;
import com.vangent.hieos.xutil.xml.XMLParser;
import com.vangent.hieos.xutil.xml.XPathHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.namespace.QName;

import oasis.names.tc.xacml._2_0.context.schema.os.ActionType;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeType;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.context.schema.os.EnvironmentType;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceContentType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceType;
import oasis.names.tc.xacml._2_0.context.schema.os.SubjectType;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.log4j.Logger;

import org.joda.time.DateTime;
import org.w3c.dom.Element;

/**
 *
 * @author Bernie Thuman
 */
public class XACMLRequestBuilder {

    private final static Logger logger = Logger.getLogger(XACMLRequestBuilder.class);

    /**
     * Builds an OMElement(XML) from an OASIS RequestType.
     *
     * @param requestType
     * @return
     * @throws PolicyException
     */
    public RequestTypeElement buildRequestTypeElement(RequestType requestType) throws PolicyException {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();

        // Request
        OMElement requestNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "Request", PolicyConstants.XACML_CONTEXT_NS_PREFIX));

        // Subject(s)
        for (SubjectType subjectType : requestType.getSubject()) {
            String subjectCategory = subjectType.getSubjectCategory();
            OMElement subjectNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "Subject", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
            subjectNode.addAttribute("SubjectCategory", subjectCategory, null);
            this.buildAttributeTypes(subjectNode, omfactory, subjectType.getAttribute());
            requestNode.addChild(subjectNode);
        }

        // Resource(s)
        for (ResourceType resourceType : requestType.getResource()) {
            OMElement resourceNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "Resource", PolicyConstants.XACML_CONTEXT_NS_PREFIX));

            // ResourceContent
            ResourceContentType resourceContentType = resourceType.getResourceContent();
            if (resourceContentType != null && !resourceContentType.getContent().isEmpty()) {
                OMElement resourceContentNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "ResourceContent", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
                for (Object contentValue : resourceContentType.getContent()) {
                    Element contentValueElement = (Element) contentValue;
                    //Element contentValueElement = (Element) resourceContentType.getContent().get(0);
                    try {
                        OMElement contentValueNode = XMLParser.convertDOMtoOM(contentValueElement);
                        resourceContentNode.addChild(contentValueNode);
                    } catch (XMLParserException ex) {
                        throw new PolicyException("Unable to convert DOM to OM: " + ex.getMessage());
                    }
                }
                resourceNode.addChild(resourceContentNode);
            }

            // Resource attributes.
            this.buildAttributeTypes(resourceNode, omfactory, resourceType.getAttribute());
            requestNode.addChild(resourceNode);
        }

        // Action
        ActionType actionType = requestType.getAction();
        OMElement actionNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "Action", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
        this.buildAttributeTypes(actionNode, omfactory, actionType.getAttribute());
        requestNode.addChild(actionNode);

        // Environment (at least an empty node is required).
        OMElement envNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "Environment", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
        EnvironmentType envType = requestType.getEnvironment();
        if (envType != null) {
            this.buildAttributeTypes(envNode, omfactory, envType.getAttribute());
        }
        requestNode.addChild(envNode);
        return new RequestTypeElement(requestNode);
    }

    /**
     *
     * @param rootNode
     * @param omfactory
     * @param attributeTypes
     */
    private void buildAttributeTypes(OMElement rootNode, OMFactory omfactory, List<AttributeType> attributeTypes) throws PolicyException {
        for (AttributeType attributeType : attributeTypes) {
            String attributeId = attributeType.getAttributeId();
            String dataType = attributeType.getDataType();
            String issuer = attributeType.getIssuer();
            OMElement attributeNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "Attribute", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
            attributeNode.addAttribute("AttributeId", attributeId, null);
            attributeNode.addAttribute("DataType", dataType, null);
            if (issuer != null) {
                attributeNode.addAttribute("Issuer", issuer, null);
            }
            rootNode.addChild(attributeNode);
            for (AttributeValueType attributeValueType : attributeType.getAttributeValue()) {
                OMElement attributeValueNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_CONTEXT_NS, "AttributeValue", PolicyConstants.XACML_CONTEXT_NS_PREFIX));
                Object attributeValueContentObject = attributeValueType.getContent().get(0);
                if (attributeValueContentObject instanceof String) {
                    String attributeValue = attributeValueContentObject.toString();
                    attributeValueNode.setText(attributeValue);
                } else if (attributeValueContentObject instanceof Element) {
                    try {
                        OMElement attributeValueContentNode = XMLParser.convertDOMtoOM((Element) attributeValueContentObject);
                        attributeValueNode.addChild(attributeValueContentNode);
                    } catch (XMLParserException ex) {
                        throw new PolicyException("Unable to convert DOM to OM: " + ex.getMessage());
                    }
                } else {
                    logger.warn(
                            "XACML attribute content type unknown (id=" + attributeId + ")");
                }
                attributeNode.addChild(attributeValueNode);
            }
        }
    }

    /**
     * Builds an OASIS RequestType from an OMElement(XML).
     *
     * @param requestTypeElement
     * @return
     */
    public RequestType buildRequestType(RequestTypeElement requestTypeElement) throws PolicyException {
        RequestType requestType = new RequestType();
        try {
            OMElement requestNode = requestTypeElement.getElement();

            // Subjects
            List<OMElement> subjectNodes = XPathHelper.selectNodes(requestNode, "./ns:Subject", PolicyConstants.XACML_CONTEXT_NS);
            for (OMElement subjectNode : subjectNodes) {
                String subjectCategory = subjectNode.getAttributeValue(new QName("SubjectCategory"));
                List<AttributeType> attributeTypes = this.getAttributeTypes(subjectNode);
                SubjectType subjectType = new SubjectType();
                subjectType.getAttribute().addAll(attributeTypes);
                subjectType.setSubjectCategory(subjectCategory);
                requestType.getSubject().add(subjectType);
            }

            // Resources
            List<OMElement> resourceNodes = XPathHelper.selectNodes(requestNode, "./ns:Resource", PolicyConstants.XACML_CONTEXT_NS);
            for (OMElement resourceNode : resourceNodes) {
                List<AttributeType> attributeTypes = this.getAttributeTypes(resourceNode);
                ResourceType resourceType = new ResourceType();
                OMElement resourceContentNode = resourceNode.getFirstChildWithName(new QName(PolicyConstants.XACML_CONTEXT_NS, "ResourceContent"));
                if (resourceContentNode != null) {
                    ResourceContentType resourceContentType = new ResourceContentType();
                    resourceType.setResourceContent(resourceContentType);
                    Iterator<OMElement> it = resourceContentNode.getChildElements();
                    while (it.hasNext()) {
                        OMElement childNode = it.next();
                        try {
                            Element childElement = XMLParser.convertOMToDOM(childNode);
                            resourceContentType.getContent().add(childElement);
                        } catch (XMLParserException ex) {
                            throw new PolicyException("Unable to convert DOM to OM: " + ex.getMessage());
                        }
                    }
                }
                resourceType.getAttribute().addAll(attributeTypes);
                requestType.getResource().add(resourceType);
            }

            // Action
            OMElement actionNode = XPathHelper.selectSingleNode(requestNode, "./ns:Action[1]", PolicyConstants.XACML_CONTEXT_NS);
            if (actionNode != null) {
                List<AttributeType> attributeTypes = this.getAttributeTypes(actionNode);
                ActionType actionType = new ActionType();
                actionType.getAttribute().addAll(attributeTypes);
                requestType.setAction(actionType);
            } else {
                throw new PolicyException("An XACML Request 'Action' node is required");
            }

            // Environment
            OMElement environmentNode = XPathHelper.selectSingleNode(requestNode, "./ns:Environment[1]", PolicyConstants.XACML_CONTEXT_NS);
            if (environmentNode != null) {
                List<AttributeType> attributeTypes = this.getAttributeTypes(environmentNode);
                EnvironmentType envType = new EnvironmentType();
                envType.getAttribute().addAll(attributeTypes);
                requestType.setEnvironment(envType);
            } else {
                throw new PolicyException("An XACML Request 'Environment' node is required");
            }
        } catch (XPathHelperException ex) {
            throw new PolicyException("Failure to build XACML RequestType", ex);
        }
        return requestType;
    }

    /**
     * NOTE: Build by hand to avoid dependency on OpenSAML library.
     *
     * Builds an OMElement(XML) from a PDPRequest.
     *
     * @param pdpRequest
     * @return
     * @throws PolicyException
     */
    public XACMLAuthzDecisionQueryElement buildXACMLAuthzDecisionQuery(PDPRequest pdpRequest) throws PolicyException {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();

        // Build XACMLAuthzDecisionQueryElement
        OMElement authzDecisionQueryNode = omfactory.createOMElement(new QName(PolicyConstants.XACML_SAML_PROTOCOL_NS, "XACMLAuthzDecisionQuery", PolicyConstants.XACML_SAML_PROTOCOL_NS_PREFIX));
        authzDecisionQueryNode.addAttribute("InputContextOnly", "false", null);
        authzDecisionQueryNode.addAttribute("ReturnContext", "true", null);
        authzDecisionQueryNode.addAttribute("ID", UUID.randomUUID().toString(), null);
        authzDecisionQueryNode.addAttribute("Version", "2.0", null);
        authzDecisionQueryNode.addAttribute("IssueInstant", (new DateTime()).toString(), null);

        // Build Issuer [FIXME: May need to do more ...]
        OMElement issuerNode = omfactory.createOMElement(new QName(PolicyConstants.SAML2_NS, "Issuer", PolicyConstants.SAML2_NS_PREFIX));
        issuerNode.setText(pdpRequest.getIssuer());
        authzDecisionQueryNode.addChild(issuerNode);

        // Get XACML RequestType from PDPRequest
        RequestType requestType = pdpRequest.getRequestType();
        OMElement requestNode = this.buildRequestTypeElement(requestType).getElement();
        authzDecisionQueryNode.addChild(requestNode);

        return new XACMLAuthzDecisionQueryElement(authzDecisionQueryNode);
    }

    /**
     * Builds a PDPRequest from a SAML2 Assertion.
     *
     * @param action
     * @param saml2Assertion
     * @return
     * @throws PolicyException 
     */
    public PDPRequest buildPDPRequest(String action, SAML2Assertion saml2Assertion) throws PolicyException {
        PDPRequest request = new PDPRequest();
        request.setAction(action);
        this.buildAttributes(request, saml2Assertion);
        return request;
    }

    /**
     *
     * @param pdpRequest
     * @param saml2Assertion
     * @throws PolicyException
     */
    private void buildAttributes(PDPRequest pdpRequest, SAML2Assertion saml2Assertion) throws PolicyException {
        OMElement assertionNode = saml2Assertion.getElement();
        PolicyConfig pConfig = PolicyConfig.getInstance();
        try {
            List<OMElement> attributeNodes = XPathHelper.selectNodes(assertionNode, "./ns:AttributeStatement/ns:Attribute", PolicyConstants.SAML2_NS);
            for (OMElement attributeNode : attributeNodes) {
                String attributeId = attributeNode.getAttributeValue(new QName("Name"));

                // Get attribute value.
                OMElement attributeValueNode = attributeNode.getFirstChildWithName(new QName(PolicyConstants.SAML2_NS, "AttributeValue"));

                AttributeConfig attributeConfig = pConfig.getAttributeConfig(attributeId);
                AttributeConfig.AttributeClassType classType = attributeConfig.getClassType();

                // Handles any type including HL7V3 CodedValue types.
                OMElement attributeValueContentNode = attributeValueNode.getFirstElement();
                if (attributeValueContentNode != null) {
                    AttributeConfig.AttributeType type = attributeConfig.getType();
                    if (type == AttributeConfig.AttributeType.HL7V3_CODED_VALUE) {
                        // Transform to HL7v2 CNE type.
                        String formattedCode = HL7FormatUtil.getCNE_Code(attributeValueContentNode);
                        pdpRequest.addAttribute(classType, attributeId, formattedCode);
                    } else {
                        // Just add the node.
                        pdpRequest.addAttribute(classType, attributeId, attributeValueContentNode);
                    }
                } else {
                    // Assume STRING
                    String attributeValueContentText = "";
                    if (attributeValueNode != null) {
                        attributeValueContentText = attributeValueNode.getText();
                    }
                    pdpRequest.addAttribute(classType, attributeId, attributeValueContentText);
                }
            }
        } catch (XPathHelperException ex) {
            throw new PolicyException("Failure to build PDP Request", ex);
        }
    }

    /**
     *
     * @param rootNode
     * @return List<AttributeType>
     */
    private List<AttributeType> getAttributeTypes(OMElement rootNode) throws PolicyException {
        Iterator<OMElement> attributeNodesIt = this.getAttributeNodes(rootNode);
        List<AttributeType> attributeTypes = new ArrayList<AttributeType>();
        while (attributeNodesIt.hasNext()) {
            OMElement attributeNode = attributeNodesIt.next();
            String attributeId = attributeNode.getAttributeValue(new QName("AttributeId"));
            String dateType = attributeNode.getAttributeValue(new QName("DataType"));
            String issuer = attributeNode.getAttributeValue(new QName("Issuer"));
            AttributeType attributeType = new AttributeType();
            attributeType.setAttributeId(attributeId);
            attributeType.setDataType(dateType);
            attributeType.setIssuer(issuer);
            Iterator<OMElement> attributeValueNodesIt = this.getAttributeValueNodes(attributeNode);
            while (attributeValueNodesIt.hasNext()) {
                OMElement attributeValueNode = attributeValueNodesIt.next();
                OMElement attributeValueContentNode = attributeValueNode.getFirstElement();
                AttributeValueType avt = new AttributeValueType();
                if (attributeValueContentNode != null) {
                    // Handles coded value nodes amongst others.
                    try {
                        Element attributeValueContentElement = XMLParser.convertOMToDOM(attributeValueContentNode);
                        avt.getContent().add(attributeValueContentElement);
                    } catch (XMLParserException ex) {
                        throw new PolicyException("Unable to convert OM to DOM: " + ex.getMessage());
                    }
                } else {
                    String attributeValueContentText = attributeValueNode.getText();
                    avt.getContent().add(attributeValueContentText);
                }
                attributeType.getAttributeValue().add(avt);
            }
            attributeTypes.add(attributeType);
        }
        return attributeTypes;
    }

    /**
     *
     * @param rootNode
     * @return
     */
    private Iterator<OMElement> getAttributeNodes(OMElement rootNode) {
        return rootNode.getChildrenWithName(new QName(PolicyConstants.XACML_CONTEXT_NS, "Attribute"));
    }

    /**
     *
     * @param rootNode
     * @return
     */
    private Iterator<OMElement> getAttributeValueNodes(OMElement rootNode) {
        return rootNode.getChildrenWithName(new QName(PolicyConstants.XACML_CONTEXT_NS, "AttributeValue"));
    }
}
