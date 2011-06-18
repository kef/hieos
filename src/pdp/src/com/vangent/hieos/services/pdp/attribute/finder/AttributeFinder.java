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
package com.vangent.hieos.services.pdp.attribute.finder;

import com.vangent.hieos.policyutil.model.patientconsent.PatientConsentDirectives;
import com.vangent.hieos.policyutil.model.patientconsent.Organization;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.policyutil.client.PIPClient;
import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.model.patientconsent.DocumentType;
import com.vangent.hieos.policyutil.util.PolicyConstants;

import java.util.List;

import org.apache.axis2.AxisFault;

import org.jboss.security.xacml.core.model.context.AttributeType;
import org.jboss.security.xacml.core.model.context.AttributeValueType;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResourceType;

/**
 *
 * @author Bernie Thuman
 */
public class AttributeFinder {

    private ResourceType resourceType;
    private PatientConsentDirectives consentDirectives;

    /**
     * 
     * @param requestType
     */
    public AttributeFinder(RequestType requestType) {

        // FIXME: Guard here against no resources.
        // Only picks the first Resource
        this.resourceType = requestType.getResource().get(0);
    }

    /**
     * 
     */
    public void addMissingAttributes() throws PolicyException {
        SubjectIdentifier patientId = new SubjectIdentifier();
        // TODO: fill in patientId properly from "requestType".

        // Get info from PIP.
        this.consentDirectives = this.getPatientConsentDirectives(patientId);
        this.addOrganizations(PolicyConstants.XACML_RESOURCE_ALLOWED_ORGANIZATIONS, consentDirectives.getAllowedOrganizations());
        this.addOrganizations(PolicyConstants.XACML_RESOURCE_BLOCKED_ORGANIZATIONS, consentDirectives.getBlockedOrganizations());
        this.addDocumentTypes(PolicyConstants.XACML_RESOURCE_SENSITIVE_DOCUMENT_TYPES, consentDirectives.getSensitiveDocumentTypes());
    }

    /**
     *
     * @param attributeId
     * @param orgs
     */
    private void addOrganizations(String attributeId, List<Organization> orgs) {
        if (orgs == null || orgs.isEmpty()) {
            return;
        }
        // Create a multi-valued attribute
        AttributeType multiValuedAttribute = new AttributeType();
        multiValuedAttribute.setAttributeId(attributeId);
        multiValuedAttribute.setDataType("http://www.w3.org/2001/XMLSchema#string");
        for (Organization org : orgs) {
            String orgId = org.getId().getIdentifier();
            multiValuedAttribute.getAttributeValue().add(createAttributeValueType(orgId));
        }
        resourceType.getAttribute().add(multiValuedAttribute);
    }

     /**
      *
      * @param attributeId
      * @param docTypes
      */
    private void addDocumentTypes(String attributeId, List<DocumentType> docTypes) {
        if (docTypes == null || docTypes.isEmpty()) {
            return;
        }
        // Create a multi-valued attribute
        AttributeType multiValuedAttribute = new AttributeType();
        multiValuedAttribute.setAttributeId(attributeId);
        multiValuedAttribute.setDataType("http://www.w3.org/2001/XMLSchema#string");
        for (DocumentType docType : docTypes) {
            String value = docType.asAttribute().getValue();
            multiValuedAttribute.getAttributeValue().add(createAttributeValueType(value));
        }
        resourceType.getAttribute().add(multiValuedAttribute);
    }


    /**
     * 
     * @param value
     * @return
     */
    private AttributeValueType createAttributeValueType(String value) {
        AttributeValueType avt = new AttributeValueType();
        avt.getContent().add(value);
        return avt;
    }

    /**
     *
     * @param patientId
     * @return
     */
    private PatientConsentDirectives getPatientConsentDirectives(SubjectIdentifier patientId) throws PolicyException {
        try {
            // FIXME: get configuration.
            PIPClient pipClient = new PIPClient(null);
            return pipClient.getPatientConsentDirectives(patientId);
        } catch (AxisFault ex) {
            throw new PolicyException("Unable to communicate with PIP: " + ex.getMessage());
        }
    }
}
