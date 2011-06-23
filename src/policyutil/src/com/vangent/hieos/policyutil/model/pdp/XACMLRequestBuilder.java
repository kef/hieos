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
import com.vangent.hieos.policyutil.model.saml.SAML2Assertion;
import com.vangent.hieos.policyutil.util.PolicyConfig;
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
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceType;
import oasis.names.tc.xacml._2_0.context.schema.os.SubjectType;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

import org.joda.time.DateTime;

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
     *
     * @param requestType
     * @return
     * @throws PolicyException
     */
    public RequestTypeElement buildRequestTypeElement(RequestType requestType) throws PolicyException {
        // FIXME: namespace hardcoding
        String nsURI = "urn:oasis:names:tc:xacml:2.0:context:schema:os";
        String nsPrefix = "xacml-context";
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
            // FIXME: ResourceContent
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
     *
     * @param rootNode
     * @param omfactory
     * @param attributeTypes
     */
    private void addAttributes(OMElement rootNode, OMFactory omfactory, List<AttributeType> attributeTypes) {
        // FIXME: namespace hardcoding
        String nsURI = "urn:oasis:names:tc:xacml:2.0:context:schema:os";
        String nsPrefix = "xacml-context";
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
     * @param requestTypeElement
     * @return
     */
    public RequestType buildRequestType(RequestTypeElement requestTypeElement) {
        RequestType requestType = new RequestType();
        try {
            OMElement request = requestTypeElement.getElement();

            // FIXME: namespace hardcoding
            String nsURI = "urn:oasis:names:tc:xacml:2.0:context:schema:os";

            // Action
            OMElement actionNode = XPathHelper.selectSingleNode(request, "./ns:Action[1]", nsURI);
            if (actionNode != null) {
                List<AttributeType> attributeTypes = this.getAttributeTypes(actionNode);
                ActionType actionType = new ActionType();
                actionType.getAttribute().addAll(attributeTypes);
                requestType.setAction(actionType);
            }

            // Subjects
            List<OMElement> subjectNodes = XPathHelper.selectNodes(request, "./ns:Subject[1]", nsURI);
            for (OMElement subjectNode : subjectNodes) {
                String subjectCategory = subjectNode.getAttributeValue(new QName("SubjectCategory"));
                List<AttributeType> attributeTypes = this.getAttributeTypes(subjectNode);
                SubjectType subjectType = new SubjectType();
                subjectType.getAttribute().addAll(attributeTypes);
                subjectType.setSubjectCategory(subjectCategory);
                requestType.getSubject().add(subjectType);
            }

            // Resources
            List<OMElement> resourceNodes = XPathHelper.selectNodes(request, "./ns:Resource[1]", nsURI);
            for (OMElement resourceNode : resourceNodes) {
                List<AttributeType> attributeTypes = this.getAttributeTypes(resourceNode);
                ResourceType resourceType = new ResourceType();
                // FIXME: ResourceContent
                resourceType.getAttribute().addAll(attributeTypes);
                requestType.getResource().add(resourceType);
            }

            // Environment
            OMElement environmentNode = XPathHelper.selectSingleNode(request, "./ns:Environment[1]", nsURI);
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
        return rootNode.getChildrenWithLocalName("Attribute");
    }

    /**
     *
     * @param rootNode
     * @return
     */
    private Iterator<OMElement> getAttributeValueNodes(OMElement rootNode) {
        return rootNode.getChildrenWithLocalName("AttributeValue");
    }

    /**
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
     * @param pdpRequest
     * @param saml2Assertion
     */
    private void buildAttributes(PDPRequest pdpRequest, SAML2Assertion saml2Assertion) {
        PolicyConfig pConfig = PolicyConfig.getInstance();
        // TBD:
/*
        // Get OpenSAML assertion.
        Assertion assertion = saml2Assertion.getAssertion();
        // Get attribute statement.
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();

        if (attributeStatements == null || attributeStatements.isEmpty()) {
        return;  // Early exit!
        }

        // Just use the first one ... multiples not used in this implementation
        AttributeStatement attributeStatement = attributeStatements.get(0);
        List<Attribute> attributes = attributeStatement.getAttributes();
        if (attributes == null || attributes.isEmpty()) {
        return;  // Early exit!
        }

        // Loop through all attributes.
        System.out.println("Attribute count = " + attributes.size());
        for (Attribute attribute : attributes) {
        String name = attribute.getName();
        System.out.println("... Name = " + name);

        // Determine the type of attribute through configuration.
        IdType idType = pConfig.getIdType(name);

        List<XMLObject> attributeValues = attribute.getAttributeValues();

        // FIXME: Only dealing with single value attributes as strings
        if (attributeValues != null && !attributeValues.isEmpty()) {
        XSString value = (XSString) attributeValues.get(0);
        StringValueAttribute attr = new StringValueAttribute();
        attr.setId(name);
        attr.setValue(value.getValue());
        // TBD: Need to figure out what kind of attribute (Subject/Resource/etc.)
        switch (idType) {
        case SUBJECT_ID:
        request.getSubjectAttributes().add(attr);
        break;
        case RESOURCE_ID:
        request.getResourceAttributes().add(attr);
        break;
        default:
        // Just treat all else as part of the Environment
        request.getEnvironmentAttributes().add(attr);
        break;
        }
        }
        }*/
    }

    /**
     * NOTE: Build by hand to avoid dependency on OpenSAML library.
     *
     * @param pdpRequest
     * @return
     */
    public XACMLAuthzDecisionQueryElement buildXACMLAuthzDecisionQuery(PDPRequest pdpRequest) throws PolicyException {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();

        // FIXME: Remove hardwired prefix/URIs

        // Build XACMLAuthzDecisionQueryElement
        OMElement authzDecisionQueryNode = omfactory.createOMElement(new QName("urn:oasis:xacml:2.0:saml:protocol:schema:os", "XACMLAuthzDecisionQuery", "xacml-samlp"));
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
        RequestType requestType = this.buildXACMLRequestType(pdpRequest);
        OMElement requestNode = this.buildRequestTypeElement(requestType).getElement();
        authzDecisionQueryNode.addChild(requestNode);

        return new XACMLAuthzDecisionQueryElement(authzDecisionQueryNode);
    }

    /**
     *
     * @param pdpRequest
     * @return
     */
    public RequestType buildXACMLRequestType(PDPRequest pdpRequest) {
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
