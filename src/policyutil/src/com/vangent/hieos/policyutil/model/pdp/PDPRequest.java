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
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceType;
import oasis.names.tc.xacml._2_0.context.schema.os.SubjectType;
import org.apache.axiom.om.OMElement;
import org.w3c.dom.Element;

/**
 *
 * @author Bernie Thuman
 */
public class PDPRequest {

    private String issuer;
    ActionType actionType = null;
    SubjectType subjectType = null;
    ResourceType resourceType = null;
    EnvironmentType environmentType = null;

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
        AttributeType attributeType = actionType.getAttribute().get(0);
        AttributeValueType attributeValueType = attributeType.getAttributeValue().get(0);
        // Only allowing strings here for action-id attribute values.
        return attributeValueType.getContent().get(0).toString();
    }

    /**
     *
     * @param action
     */
    public void setAction(String action) {
        if (actionType == null) {
            actionType = new ActionType();
        }
        AttributeType attributeType = new AttributeType();
        attributeType.setAttributeId(PolicyConstants.XACML_ACTION_ID);
        attributeType.setDataType("http://www.w3.org/2001/XMLSchema#anyURI");
        attributeType.setIssuer(issuer);
        AttributeValueType avt = new AttributeValueType();
        // Only allowing strings here for action-id attribute values.
        avt.getContent().add(action);
        attributeType.getAttributeValue().add(avt);
        actionType.getAttribute().add(attributeType);
    }

    /**
     *
     * @return
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     *
     * @return
     */
    public EnvironmentType getEnvironmentType() {
        return environmentType;
    }

    /**
     *
     * @return
     */
    public ResourceType getResourceType() {
        return resourceType;
    }

    /**
     *
     * @return
     */
    public SubjectType getSubjectType() {
        return subjectType;
    }

    /**
     *
     * @return
     */
    public RequestType getRequestType() {
        RequestType requestType = new RequestType();
        requestType.getSubject().add(subjectType);
        requestType.getResource().add(resourceType);
        requestType.setAction(actionType);
        requestType.setEnvironment(environmentType);
        return requestType;
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
                if (subjectType == null) {
                    subjectType = new SubjectType();
                    subjectType.setSubjectCategory(PolicyConstants.XACML_SUBJECT_CATEGORY);
                }
                this.addAttributeValue(subjectType.getAttribute(), id, value);
                break;
            case RESOURCE_ID:
                if (resourceType == null) {
                    resourceType = new ResourceType();
                }
                this.addAttributeValue(resourceType.getAttribute(), id, value);
                break;
            case ENVIRONMENT_ID:
            default: // Fall-through
                if (environmentType == null) {
                    environmentType = new EnvironmentType();
                }
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
                if (subjectType == null) {
                    subjectType = new SubjectType();
                    subjectType.setSubjectCategory(PolicyConstants.XACML_SUBJECT_CATEGORY);
                }
                this.addAttributeValue(subjectType.getAttribute(), id, value);
                break;
            case RESOURCE_ID:
                if (resourceType == null) {
                    resourceType = new ResourceType();
                }
                this.addAttributeValue(resourceType.getAttribute(), id, value);
                break;
            case ENVIRONMENT_ID:
            default: // Fall-through
                if (environmentType == null) {
                    environmentType = new EnvironmentType();
                }
                this.addAttributeValue(environmentType.getAttribute(), id, value);
                break;
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
            // FIXME: Deal with data type!!!
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
