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

import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.policyutil.model.pdp.PDPRequest;
import com.vangent.hieos.policyutil.model.attribute.Attribute;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.jboss.security.xacml.core.model.context.ActionType;
import org.jboss.security.xacml.core.model.context.AttributeType;
import org.jboss.security.xacml.core.model.context.EnvironmentType;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResourceType;
import org.jboss.security.xacml.core.model.context.SubjectType;
import org.jboss.security.xacml.factories.RequestAttributeFactory;

/**
 *
 * @author Bernie Thuman
 */
public class XACMLRequestBuilder {

    private String issuer = null;
    
    /**
     *
     * @param request
     * @return
     */
    public RequestType buildXACMLRequestType(PDPRequest request) {
        RequestType requestType = new RequestType();
        SubjectType subjectType = this.buildSubject(request.getSubjectAttributes());
        ResourceType resourceType = this.buildResource(request.getResourceAttributes());
        ActionType actionType = this.buildAction(request.getAction());
        EnvironmentType envType = this.buildEnvironment(request);
        requestType.getSubject().add(subjectType);
        requestType.getResource().add(resourceType);
        requestType.setAction(actionType);
        requestType.setEnvironment(envType);
        return requestType;
    }

    /**
     *
     * @param subjectAttributes
     * @return
     */
    private SubjectType buildSubject(List<Attribute> subjectAttributes) {
        // Create a subject type
        SubjectType subjectType = new SubjectType();
        subjectType.setSubjectCategory(PolicyConstants.XACML_SUBJECT_CATEGORY);
        subjectType.getAttribute().addAll(getAttributes(subjectAttributes));
        return subjectType;
    }

    /**
     *
     * @param resourceAttributes
     * @return
     */
    public ResourceType buildResource(List<Attribute> resourceAttributes) {
        // FIXME?: Not dealing with multi-valued attributes
        ResourceType resourceType = new ResourceType();
        resourceType.getAttribute().addAll(getAttributes(resourceAttributes));
        return resourceType;
    }

    /**
     *
     * @param action
     * @return
     */
    private ActionType buildAction(String action) {
        try {
            ActionType actionType = new ActionType();
            URI uri = new URI(action);
            AttributeType attActionID = RequestAttributeFactory.createAnyURIAttributeType(
                    PolicyConstants.XACML_ACTION_ID,
                    issuer, uri);
            // TBD
            //AttributeType attActionID = RequestAttributeFactory.createStringAttributeType(
            //        "urn:oasis:names:tc:xacml:1.0:action:action-id", issuer, "read");
            actionType.getAttribute().add(attActionID);
            return actionType;
        } catch (URISyntaxException ex) {
            // FIXME:
            return null;
        }
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
    private List<AttributeType> getAttributes(List<Attribute> attributes) {
        // FIXME?: Not dealing with multi-valued attributes
        List<AttributeType> attrTypeList = new ArrayList<AttributeType>();

        // Create attributes
        for (Attribute attr : attributes) {
            AttributeType attributeType = RequestAttributeFactory.createStringAttributeType(
                    attr.getId(), issuer, attr.getValue());
            attrTypeList.add(attributeType);
        }
        return attrTypeList;
    }
}
