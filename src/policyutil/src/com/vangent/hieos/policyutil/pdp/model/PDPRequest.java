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
import com.vangent.hieos.policyutil.util.AttributeConfig.AttributeClassType;
import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.xml.XMLParser;

import java.util.List;

import oasis.names.tc.xacml._2_0.context.schema.os.ActionType;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeType;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.context.schema.os.EnvironmentType;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceContentType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceType;
import oasis.names.tc.xacml._2_0.context.schema.os.SubjectType;
import org.apache.axiom.om.OMElement;
import org.w3c.dom.Element;

/**
 *
 * @author Bernie Thuman
 */
public class PDPRequest {

    RequestType requestType;
    private String issuer = "";

    /**
     * 
     */
    public PDPRequest() {
        // Initialize requestType.
        requestType = new RequestType();
        requestType.setAction(new ActionType());
        requestType.getSubject().add(new SubjectType());
        requestType.getResource().add(new ResourceType());
        requestType.setEnvironment(new EnvironmentType());
    }

    /**
     *
     * @return
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     *
     * @param requestType
     */
    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    /**
     * 
     * @return
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     *
     * @param issuer
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * 
     * @return
     */
    public String getAction() {
        ActionType actionType = this.getActionType();
        if (!actionType.getAttribute().isEmpty()) {
            AttributeType attributeType = actionType.getAttribute().get(0);
            AttributeValueType attributeValueType = attributeType.getAttributeValue().get(0);
            // Only allowing strings here for action-id attribute values.
            return attributeValueType.getContent().get(0).toString();
        } else {
            return "UNKNOWN";
        }
    }

    /**
     *
     * @param action
     */
    public void setAction(String action) {
        AttributeType attributeType = new AttributeType();
        attributeType.setAttributeId(PolicyConstants.XACML_ACTION_ID);
        attributeType.setDataType("http://www.w3.org/2001/XMLSchema#anyURI");
        attributeType.setIssuer(issuer);
        AttributeValueType avt = new AttributeValueType();
        // Only allowing strings here for action-id attribute values.
        avt.getContent().add(action);
        attributeType.getAttributeValue().add(avt);
        ActionType actionType = this.getActionType();
        actionType.getAttribute().clear();  // Clear (we may have already set an action).
        actionType.getAttribute().add(attributeType);
    }

    /**
     *
     * @return
     */
    public ActionType getActionType() {
        return requestType.getAction();
    }

    /**
     *
     * @return
     */
    public EnvironmentType getEnvironmentType() {
        return requestType.getEnvironment();
    }

    /**
     *
     * @return
     */
    public ResourceType getResourceType() {
        // Only handling one resource now.
        List<ResourceType> resourceTypes = requestType.getResource();
        return resourceTypes.isEmpty() ? null : resourceTypes.get(0);
    }

    /**
     *
     * @return
     */
    public SubjectType getSubjectType() {
        // Only handling one subject now.
        List<SubjectType> subjectTypes = requestType.getSubject();
        return subjectTypes.isEmpty() ? null : subjectTypes.get(0);
    }

    /**
     * 
     * @param classType
     * @param id
     * @param value
     */
    public void addAttribute(AttributeClassType classType, String id, String value) {
        switch (classType) {
            case SUBJECT_ID:
                SubjectType subjectType = this.getSubjectType();
                subjectType.setSubjectCategory(PolicyConstants.XACML_SUBJECT_CATEGORY);
                this.addAttributeValue(subjectType.getAttribute(), id, value);
                break;
            case RESOURCE_ID:
                ResourceType resourceType = this.getResourceType();
                this.addAttributeValue(resourceType.getAttribute(), id, value);
                break;
            case ENVIRONMENT_ID:
            default: // Fall-through
                EnvironmentType environmentType = this.getEnvironmentType();
                this.addAttributeValue(environmentType.getAttribute(), id, value);
                break;
        }
    }

    /**
     *
     * @param classType
     * @param id
     * @param value
     */
    public void addAttribute(AttributeClassType classType, String id, OMElement value) throws PolicyException {
        switch (classType) {
            case SUBJECT_ID:
                SubjectType subjectType = this.getSubjectType();
                subjectType.setSubjectCategory(PolicyConstants.XACML_SUBJECT_CATEGORY);
                this.addAttributeValue(subjectType.getAttribute(), id, value);
                break;
            case RESOURCE_ID:
                ResourceType resourceType = this.getResourceType();
                this.addAttributeValue(resourceType.getAttribute(), id, value);
                break;
            case ENVIRONMENT_ID:
            default: // Fall-through
                EnvironmentType environmentType = this.getEnvironmentType();
                this.addAttributeValue(environmentType.getAttribute(), id, value);
                break;
        }
    }

    /**
     * 
     * @return
     */
    public String getResourceId() {
        ResourceType resourceType = this.getResourceType();
        List<AttributeType> attributeTypes = resourceType.getAttribute();
        // Find the resource-id attribute.
        AttributeType resourceIdAttributeType = this.findAttributeType(attributeTypes, PolicyConstants.XACML_RESOURCE_ID);
        if (resourceIdAttributeType != null) {
            List<AttributeValueType> attributeValueTypes = resourceIdAttributeType.getAttributeValue();
            if (attributeValueTypes != null && !attributeValueTypes.isEmpty()) {
                AttributeValueType attributeValueType = attributeValueTypes.get(0);
                List<Object> content = attributeValueType.getContent();
                if (content != null && !content.isEmpty()) {
                    // Just handle string.
                    return (String) content.get(0);  // Found.
                }
            }
        }
        return "UNKNOWN";
    }

    /**
     * 
     * @return
     */
    public boolean hasResourceContent() {
        ResourceType resourceType = this.getResourceType();
        if (resourceType != null) {
            ResourceContentType resourceContentType = resourceType.getResourceContent();
            if (resourceContentType != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param resourceContent
     * @throws PolicyException
     */
    public void addResourceContent(OMElement resourceContent, boolean clearAllButFirst) throws PolicyException {
        ResourceType resourceType = this.getResourceType();
        if (resourceType == null) {
            throw new PolicyException("Attempted to add ResourceContent with no Resource");
        }
        try {
            // First convert Axiom OMElement to DOM Element
            Element resourceContentElement;
            try {
                resourceContentElement = XMLParser.convertOMToDOM(resourceContent);
            } catch (XMLParserException ex) {
                throw new PolicyException("Unable to convert OM to DOM: " + ex.getMessage());
            }
            // See if we already have a ResourceContentType.
            ResourceContentType resourceContentType = resourceType.getResourceContent();
            if (resourceContentType == null) {
                // Does not exist, so create one.
                resourceContentType = new ResourceContentType();
                resourceType.setResourceContent(resourceContentType);
            }
            // Now add the content.
            List<Object> contentObjectList = resourceContentType.getContent();
            if (clearAllButFirst == true && contentObjectList.size() > 1) {
                // Wipe out all resource content except the first Element.
                Element firstElement = (Element) contentObjectList.get(0);
                contentObjectList.clear();
                contentObjectList.add(firstElement);
            }
            contentObjectList.add(resourceContentElement);
        } catch (Exception ex) {
            throw new PolicyException("Exception trying to get Consent Directives: " + ex.getMessage());
        }
    }

    /**
     *
     * @param attributeTypes
     * @param id
     * @param value
     */
    private void addAttributeValue(List<AttributeType> attributeTypes, String id, String value) {
        // See if the attribute "id" already exists (so we can add to it).
        AttributeType attributeType = this.findAttributeType(attributeTypes, id);
        if (attributeType == null) {
            // Not found.
            attributeType = new AttributeType();
            attributeType.setAttributeId(id);
            attributeType.setDataType("http://www.w3.org/2001/XMLSchema#string");
            attributeType.setIssuer(issuer);
            AttributeValueType avt = new AttributeValueType();
            avt.getContent().add(value);
            attributeType.getAttributeValue().add(avt);
            attributeTypes.add(attributeType);
        } else {
            AttributeValueType avt = new AttributeValueType();
            avt.getContent().add(value);
            attributeType.getAttributeValue().add(avt);
        }
    }

    /**
     *
     * @param attributeTypes
     * @param id
     * @param value
     */
    private void addAttributeValue(List<AttributeType> attributeTypes, String id, OMElement value) throws PolicyException {
        // First convert Axiom OMElement to DOM Element
        Element valueElement;
        try {
            valueElement = XMLParser.convertOMToDOM(value);
        } catch (XMLParserException ex) {
            throw new PolicyException("Unable to convert OM to DOM: " + ex.getMessage());
        }
        // See if the attribute "id" already exists (so we can add to it).
        AttributeType attributeType = this.findAttributeType(attributeTypes, id);
        if (attributeType == null) {
            // Not found.
            attributeType = new AttributeType();
            attributeType.setAttributeId(id);
            // FIXME: Deal with data type - not always simply a string (resolve coded values) !!!
            attributeType.setDataType("http://www.w3.org/2001/XMLSchema#string");
            attributeType.setIssuer(issuer);
            AttributeValueType avt = new AttributeValueType();

            avt.getContent().add(valueElement);
            attributeType.getAttributeValue().add(avt);
            attributeTypes.add(attributeType);
        } else {
            AttributeValueType avt = new AttributeValueType();
            avt.getContent().add(valueElement);
            attributeType.getAttributeValue().add(avt);
        }
    }

    /**
     *
     * @param attributeTypes
     * @param id
     * @return
     */
    private AttributeType findAttributeType(List<AttributeType> attributeTypes, String id) {
        // See if the attribute "id" exists; if so, return it.
        for (AttributeType attributeType : attributeTypes) {
            if (attributeType.getAttributeId().equalsIgnoreCase(id)) {
                return attributeType;
            }
        }
        return null; // Not found.
    }
}
