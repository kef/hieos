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

import com.vangent.hieos.policyutil.util.PolicyConstants;
import java.util.List;
import oasis.names.tc.xacml._2_0.context.schema.os.ActionType;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeType;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.context.schema.os.EnvironmentType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceType;
import oasis.names.tc.xacml._2_0.context.schema.os.SubjectType;

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
        avt.getContent().add(action);
        attributeType.getAttributeValue().add(avt);
        // TBD
        //AttributeType attActionID = RequestAttributeFactory.createStringAttributeType(
        //        "urn:oasis:names:tc:xacml:1.0:action:action-id", issuer, "read");
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
     * @param id
     * @param value
     */
    public void addSubjectAttribute(String id, String value) {
        if (subjectType == null) {
            subjectType = new SubjectType();
            subjectType.setSubjectCategory(PolicyConstants.XACML_SUBJECT_CATEGORY);
        }
        this.addAttribute(subjectType.getAttribute(), id, value);
    }

    /**
     *
     * @param id
     * @param value
     */
    public void addResourceAttribute(String id, String value) {
        if (resourceType == null) {
            resourceType = new ResourceType();
        }
        this.addAttribute(resourceType.getAttribute(), id, value);
    }

    /**
     *
     * @param id
     * @param value
     */
    public void addEnvironmentAttribute(String id, String value) {
        if (environmentType == null) {
            environmentType = new EnvironmentType();
        }
        this.addAttribute(environmentType.getAttribute(), id, value);
    }

    /**
     *
     * @param attributeTypes
     * @param id
     * @param value
     */
    private void addAttribute(List<AttributeType> attributeTypes, String id, String value) {

        // See if the attribute "id" already exists (so we can add to it).
        AttributeType attributeType = null;
        for (AttributeType currentAttributeType : attributeTypes) {
            if (currentAttributeType.getAttributeId().equalsIgnoreCase(id)) {
                attributeType = currentAttributeType;
                break;
            }
        }
        // FIXME: Only dealing with strings ... need to add codedvalues.
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
}
