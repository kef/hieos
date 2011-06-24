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

import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.policyutil.model.attribute.Attribute;
import com.vangent.hieos.policyutil.model.attribute.StringValueAttribute;
import com.vangent.hieos.policyutil.model.saml.SAML2Assertion;
import com.vangent.hieos.policyutil.util.PolicyConfig;
import com.vangent.hieos.policyutil.util.PolicyConfig.IdType;
import com.vangent.hieos.xutil.exception.XPathHelperException;
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
import org.apache.axis2.util.XMLUtils;

import org.joda.time.DateTime;
import org.w3c.dom.Element;

/**
 *
 * @author Bernie Thuman
 */
public class XACMLRequestBuilder {

    // <xacml-context:Request xsi:schemaLocation="urn:oasis:names:tc:xacml:2.0:context:schema:os http://docs.oasis-open.org/xacml/access_control-xacml-2.0-context-schema-os.xsd" xmlns="urn:oasis:names:tc:xacml:2.0:context:schema:os" xmlns:xacml-context="urn:oasis:names:tc:xacml:2.0:context:schema:os" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance/">
    //   <xacml-context:Subject SubjectCategory="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject">
    //      <xacml-context:Attribute Issuer="testIssuer" DataType="http://www.w3.org/2001/XMLSchema#string" AttributeId="urn:oasis:names:tc:xacml:1.0:subject:subject-id">
    //         <xacml-context:AttributeValue>SUBJECT-ID</xacml-context:AttributeValue>
    //      </xacml-context:Attribute>
    //      <xacml-context:Attribute Issuer="testIssuer" DataType="http://www.w3.org/2001/XMLSchema#string" AttributeId="urn:oasis:names:tc:xspa:1.0:subject:organization">
    //         <xacml-context:AttributeValue>ORGANIZATION</xacml-context:AttributeValue>
    //      </xacml-context:Attribute>
    //   </xacml-context:Subject>
    //   <xacml-context:Resource>
    //      <xacml-context:Attribute Issuer="testIssuer" DataType="http://www.w3.org/2001/XMLSchema#string" AttributeId="urn:oasis:names:tc:xacml:1.0:resource:resource-id">
    //         <xacml-context:AttributeValue>PID^^^1</xacml-context:AttributeValue>
    //      </xacml-context:Attribute>
    //   </xacml-context:Resource>
    //   <xacml-context:Action>
    //      <xacml-context:Attribute AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id" DataType="http://www.w3.org/2001/XMLSchema#anyURI">
    //         <xacml-context:AttributeValue>urn:ihe:iti:2007:CrossGatewayQuery</xacml-context:AttributeValue>
    //      </xacml-context:Attribute>
    //   </xacml-context:Action>
    //   <xacml-context:Environment>
    //      <xacml-context:Attribute AttributeId="urn:oasis:names:tc:xspa:1.0:environment:locality" DataType="http://www.w3.org/2001/XMLSchema#string">
    //         <xacml-context:AttributeValue>TEST</xacml-context:AttributeValue>
    //      </xacml-context:Attribute>
    //   </xacml-context:Environment>
    // </xacml-context:Request>
    /**
     * Builds an OASIS RequestType from an OMElement(XML).
     *
     * @param requestType
     * @return
     * @throws PolicyException
     */
    public RequestTypeElement buildRequestTypeElement(RequestType requestType) throws PolicyException {
        String nsURI = PolicyConstants.XACML_CONTEXT_NS;
        String nsPrefix = PolicyConstants.XACML_CONTEXT_NS_PREFIX;
        OMFactory omfactory = OMAbstractFactory.getOMFactory();

        // Request
        OMElement requestNode = omfactory.createOMElement(new QName(nsURI, "Request", nsPrefix));

        // Subject(s)
        for (SubjectType subjectType : requestType.getSubject()) {
            String subjectCategory = subjectType.getSubjectCategory();
            OMElement subjectNode = omfactory.createOMElement(new QName(nsURI, "Subject", nsPrefix));
            subjectNode.addAttribute("SubjectCategory", subjectCategory, null);
            this.addAttributes(subjectNode, omfactory, subjectType.getAttribute());
            requestNode.addChild(subjectNode);
        }

        // Resource(s)
        for (ResourceType resourceType : requestType.getResource()) {
            OMElement resourceNode = omfactory.createOMElement(new QName(nsURI, "Resource", nsPrefix));
            this.addAttributes(resourceNode, omfactory, resourceType.getAttribute());

            // ResourceContent
            ResourceContentType resourceContentType = resourceType.getResourceContent();
            if (resourceContentType != null && !resourceContentType.getContent().isEmpty()) {
                OMElement resourceContentNode = omfactory.createOMElement(new QName(nsURI, "ResourceContent", nsPrefix));
                Element contentElement = (Element) resourceContentType.getContent().get(0);
                try {
                    // Convert to OMElement.
                    OMElement contentNode = XMLUtils.toOM(contentElement);
                    resourceContentNode.addChild(contentNode);
                } catch (Exception ex) {
                    throw new PolicyException("Unable to get ResourceContent: " + ex.getMessage());
                }
                resourceNode.addChild(resourceContentNode);
            }
            requestNode.addChild(resourceNode);
        }

        // Action
        ActionType actionType = requestType.getAction();
        OMElement actionNode = omfactory.createOMElement(new QName(nsURI, "Action", nsPrefix));
        this.addAttributes(actionNode, omfactory, actionType.getAttribute());
        requestNode.addChild(actionNode);

        // Environment
        EnvironmentType envType = requestType.getEnvironment();
        OMElement envNode = omfactory.createOMElement(new QName(nsURI, "Environment", nsPrefix));
        this.addAttributes(envNode, omfactory, envType.getAttribute());
        requestNode.addChild(envNode);

        return new RequestTypeElement(requestNode);
    }

    /**
     * Builds an OMElement(XML) from an OASIS RequestType.
     *
     * @param requestTypeElement
     * @return
     */
    public RequestType buildRequestType(RequestTypeElement requestTypeElement) {
        RequestType requestType = new RequestType();
        try {
            OMElement requestNode = requestTypeElement.getElement();
            String nsURI = PolicyConstants.XACML_CONTEXT_NS;

            // Action
            OMElement actionNode = XPathHelper.selectSingleNode(requestNode, "./ns:Action[1]", nsURI);
            if (actionNode != null) {
                List<AttributeType> attributeTypes = this.getAttributeTypes(actionNode);
                ActionType actionType = new ActionType();
                actionType.getAttribute().addAll(attributeTypes);
                requestType.setAction(actionType);
            }

            // Subjects
            List<OMElement> subjectNodes = XPathHelper.selectNodes(requestNode, "./ns:Subject[1]", nsURI);
            for (OMElement subjectNode : subjectNodes) {
                String subjectCategory = subjectNode.getAttributeValue(new QName("SubjectCategory"));
                List<AttributeType> attributeTypes = this.getAttributeTypes(subjectNode);
                SubjectType subjectType = new SubjectType();
                subjectType.getAttribute().addAll(attributeTypes);
                subjectType.setSubjectCategory(subjectCategory);
                requestType.getSubject().add(subjectType);
            }

            // Resources
            List<OMElement> resourceNodes = XPathHelper.selectNodes(requestNode, "./ns:Resource[1]", nsURI);
            for (OMElement resourceNode : resourceNodes) {
                List<AttributeType> attributeTypes = this.getAttributeTypes(resourceNode);
                ResourceType resourceType = new ResourceType();
                OMElement resourceContentNode = resourceNode.getFirstChildWithName(new QName(nsURI, "ResourceContent"));
                if (resourceContentNode != null) {
                    Iterator<OMElement> it = resourceContentNode.getChildElements();
                    if (it.hasNext()) {
                        OMElement childNode = it.next();  // Just get first.
                        try {
                            Element childElement = XMLUtils.toDOM(childNode);
                            ResourceContentType resourceContentType = new ResourceContentType();
                            resourceContentType.getContent().add(childElement);
                            resourceType.setResourceContent(resourceContentType);
                        } catch (Exception ex) {
                            // FIXME: ? Do nothing
                        }
                    }
                }

                resourceType.getAttribute().addAll(attributeTypes);
                requestType.getResource().add(resourceType);
            }

            // Environment
            OMElement environmentNode = XPathHelper.selectSingleNode(requestNode, "./ns:Environment[1]", nsURI);
            if (environmentNode != null) {
                List<AttributeType> attributeTypes = this.getAttributeTypes(environmentNode);
                EnvironmentType envType = new EnvironmentType();
                envType.getAttribute().addAll(attributeTypes);
                requestType.setEnvironment(envType);
            }
        } catch (XPathHelperException ex) {
            // FIXME: Do something?
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

        // Build Issuer [FIXME: May need more ...]
        OMElement issuerNode = omfactory.createOMElement(new QName("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer", "saml"));
        issuerNode.setText(pdpRequest.getIssuer());
        authzDecisionQueryNode.addChild(issuerNode);

        // Get XACML RequestType from PDPRequest
        RequestType requestType = this.buildRequestType(pdpRequest);
        OMElement requestNode = this.buildRequestTypeElement(requestType).getElement();
        authzDecisionQueryNode.addChild(requestNode);

        return new XACMLAuthzDecisionQueryElement(authzDecisionQueryNode);
    }

    /**
     *
     * @param pdpRequest
     * @return
     */
    public RequestType buildRequestType(PDPRequest pdpRequest) {
        RequestType requestType = new RequestType();
        SubjectType subjectType = this.buildSubject(pdpRequest);
        ResourceType resourceType = this.buildResource(pdpRequest);
        ActionType actionType = this.buildAction(pdpRequest);
        EnvironmentType envType = this.buildEnvironment(pdpRequest);
        requestType.getSubject().add(subjectType);
        requestType.getResource().add(resourceType);
        requestType.setAction(actionType);
        requestType.setEnvironment(envType);
        return requestType;
    }

    /**
     * Builds a PDPRequest from a SAML2 Assertion.
     *
     * @param action
     * @param saml2Assertion
     * @return
     */
    public PDPRequest buildPDPRequest(String action, SAML2Assertion saml2Assertion) {
        PDPRequest request = new PDPRequest();
        request.setAction(action);
        this.buildAttributes(request, saml2Assertion);
        return request;
    }

    /**
     *
     * @param rootNode
     * @param omfactory
     * @param attributeTypes
     */
    private void addAttributes(OMElement rootNode, OMFactory omfactory, List<AttributeType> attributeTypes) {
        String nsURI = PolicyConstants.XACML_CONTEXT_NS;
        String nsPrefix = PolicyConstants.XACML_CONTEXT_NS_PREFIX;
        for (AttributeType attributeType : attributeTypes) {
            String attributeId = attributeType.getAttributeId();
            String dataType = attributeType.getDataType();
            String issuer = attributeType.getIssuer();
            OMElement attributeNode = omfactory.createOMElement(new QName(nsURI, "Attribute", nsPrefix));
            attributeNode.addAttribute("AttributeId", attributeId, null);
            attributeNode.addAttribute("DataType", dataType, null);
            if (issuer != null) {
                attributeNode.addAttribute("Issuer", issuer, null);
            }
            rootNode.addChild(attributeNode);
            for (AttributeValueType attributeValueType : attributeType.getAttributeValue()) {
                // FIXME: AttributeValue content - how to properly handle?
                String attributeValue = attributeValueType.getContent().get(0).toString();
                OMElement attributeValueNode = omfactory.createOMElement(new QName(nsURI, "AttributeValue", nsPrefix));
                attributeValueNode.setText(attributeValue);
                attributeNode.addChild(attributeValueNode);
            }
        }
    }

    /**
     *
     * @param rootNode
     * @return
     */
    private List<AttributeType> getAttributeTypes(OMElement rootNode) {
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
                String attributeValueNodeContent = attributeValueNode.getText();
                AttributeValueType avt = new AttributeValueType();
                avt.getContent().add(attributeValueNodeContent);
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

    /**
     *
     * @param pdpRequest
     * @param saml2Assertion
     */
    private void buildAttributes(PDPRequest pdpRequest, SAML2Assertion saml2Assertion) {
        PolicyConfig pConfig = PolicyConfig.getInstance();
        OMElement assertionNode = saml2Assertion.getElement();
        try {
            List<OMElement> attributeNodes = XPathHelper.selectNodes(assertionNode, "./ns:AttributeStatement/ns:Attribute", PolicyConstants.SAML2_NS);
            for (OMElement attributeNode : attributeNodes) {
                String attributeId = attributeNode.getAttributeValue(new QName("Name"));
                // FIXME: Only handling single value String types ... need to also make
                // IdType much smarter.

                // Get attribute value (grab first one).
                OMElement attributeValueNode = attributeNode.getFirstChildWithName(new QName(PolicyConstants.SAML2_NS, "AttributeValue"));
                String attributeValueText = "";
                if (attributeValueNode != null) {
                    attributeValueText = attributeValueNode.getText();
                }
                IdType idType = pConfig.getIdType(attributeId);
                StringValueAttribute attr = new StringValueAttribute();
                attr.setId(attributeId);
                attr.setValue(attributeValueText);
                switch (idType) {
                    case SUBJECT_ID:
                        pdpRequest.getSubjectAttributes().add(attr);
                        break;
                    case RESOURCE_ID:
                        pdpRequest.getResourceAttributes().add(attr);
                        break;
                    default:
                        // Just treat all else as part of the Environment
                        pdpRequest.getEnvironmentAttributes().add(attr);
                        break;
                }
            }
        } catch (XPathHelperException ex) {
            // Fixme: Do something?
        }
    }

    /**
     * 
     * @param pdpRequest
     * @return
     */
    private SubjectType buildSubject(PDPRequest pdpRequest) {
        // Create a subject type
        SubjectType subjectType = new SubjectType();
        subjectType.setSubjectCategory(PolicyConstants.XACML_SUBJECT_CATEGORY);
        subjectType.getAttribute().addAll(
                this.getAttributes(pdpRequest.getIssuer(), pdpRequest.getSubjectAttributes()));
        return subjectType;
    }

    /**
     *
     * @param pdpRequest
     * @return
     */
    private ResourceType buildResource(PDPRequest pdpRequest) {
        // FIXME?: Not dealing with multi-valued attributes
        ResourceType resourceType = new ResourceType();
        resourceType.getAttribute().addAll(
                this.getAttributes(pdpRequest.getIssuer(), pdpRequest.getResourceAttributes()));
        return resourceType;
    }

    /**
     *
     * @param pdpRequest
     * @return
     */
    private ActionType buildAction(PDPRequest pdpRequest) {
        ActionType actionType = new ActionType();
        AttributeType attributeType = new AttributeType();
        attributeType.setAttributeId("urn:oasis:names:tc:xacml:1.0:action:action-id");
        attributeType.setDataType("http://www.w3.org/2001/XMLSchema#anyURI");
        attributeType.setIssuer(pdpRequest.getIssuer());
        AttributeValueType avt = new AttributeValueType();
        avt.getContent().add(pdpRequest.getAction());
        attributeType.getAttributeValue().add(avt);
        // TBD
        //AttributeType attActionID = RequestAttributeFactory.createStringAttributeType(
        //        "urn:oasis:names:tc:xacml:1.0:action:action-id", issuer, "read");
        actionType.getAttribute().add(attributeType);
        return actionType;
    }

    /**
     *
     * @param request
     * @return
     */
    private EnvironmentType buildEnvironment(PDPRequest request) {
        EnvironmentType env = new EnvironmentType();

        // TBD: DO SOMETHING HERE
        /*
        AttributeType attFacility = RequestAttributeFactory.createStringAttributeType(
        "urn:va:xacml:2.0:interop:rsa8:environment:locality", issuer, "Facility A");

        env.getAttribute().add(attFacility);
         *
         */
        return env;
    }

    /**
     *
     * @param attributes
     * @return
     */
    private List<AttributeType> getAttributes(String issuer, List<Attribute> attributes) {
        // FIXME?: Not dealing with multi-valued attributes
        List<AttributeType> attrTypeList = new ArrayList<AttributeType>();

        // Create attributes
        for (Attribute attr : attributes) {
            AttributeType attributeType = new AttributeType();
            attributeType.setAttributeId(attr.getId());
            attributeType.setDataType("http://www.w3.org/2001/XMLSchema#string");
            attributeType.setIssuer(issuer);
            AttributeValueType avt = new AttributeValueType();
            avt.getContent().add(attr.getValue());
            attributeType.getAttributeValue().add(avt);
            attrTypeList.add(attributeType);
        }
        return attrTypeList;
    }
}
