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
package com.vangent.hieos.services.pdp.test;


import com.vangent.hieos.hl7v3util.client.Client;
import com.vangent.hieos.policyutil.client.PDPClient;
import com.vangent.hieos.policyutil.client.PDPSOAPClient;
import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.model.attribute.Attribute;
import com.vangent.hieos.policyutil.model.pdp.PDPRequest;
import com.vangent.hieos.policyutil.model.pdp.PDPResponse;
import com.vangent.hieos.policyutil.model.pdp.PDPResponseBuilder;
import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.policyutil.model.attribute.StringValueAttribute;
import com.vangent.hieos.policyutil.model.pdp.XACMLRequestBuilder;
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.security.xacml.core.model.context.ActionType;
import org.jboss.security.xacml.core.model.context.AttributeType;
import org.jboss.security.xacml.core.model.context.AttributeValueType;
import org.jboss.security.xacml.core.model.context.EnvironmentType;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResultType;
import org.jboss.security.xacml.core.model.context.ResourceType;
import org.jboss.security.xacml.core.model.context.SubjectType;
import org.jboss.security.xacml.core.model.policy.AttributeAssignmentType;
import org.jboss.security.xacml.core.model.policy.ObligationType;
import org.jboss.security.xacml.core.model.policy.ObligationsType;
import org.jboss.security.xacml.factories.RequestAttributeFactory;

// For Test2
import org.jboss.security.xacml.core.JBossPDP;
import org.jboss.security.xacml.factories.RequestResponseContextFactory;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.jboss.security.xacml.interfaces.RequestContext;
import org.jboss.security.xacml.interfaces.ResponseContext;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;

import org.junit.Test;

/**
 *
 * @author Bernie Thuman
 */
public class PDPTest {
    private String endpoint = "http://localhost:8127/axis2/services/PDP";
    private String issuer = "testIssuer";

    @Test
    public void testPDPClient()
    {
        XConfig xconf = null;
        try {
            xconf = XConfig.getInstance();
        } catch (XConfigException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }
        XConfigObject config = xconf.getHomeCommunityConfig().getXConfigObjectWithName("pdp", "PolicyDecisionPointType");
        PDPClient pdpClient = new PDPClient((XConfigActor) config);
        try {

            PDPRequest request = this.getDummyPDPRequest();
            PDPResponse response = pdpClient.authorize(request);

            //pdpClient.testXACML();
            //pdpClient.testXACML2();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception: " + ex.getMessage());
        }
    }

     /**
     *
     * @throws Exception
     */
    @Test
    public void testPDPSOAPClient() throws Exception {
        //Create an XACML Request
        RequestType xacmlRequest = getXACMLRequest();
        PDPSOAPClient soapSAMLXACML = new PDPSOAPClient();

        ResultType result = soapSAMLXACML.send(endpoint, true, issuer, xacmlRequest);
        this.print(result);
        /*
        assertTrue("No fault", result.isFault() == false);
        assertTrue("DecisionX available", result.isResponseAvailable());
        assertTrue("Deny", result.isDeny());
         */
    }

    /**
     *
     */
    @Test
    public void testJBOSSPDP() {
        try {
            RequestType requestType = this.getXACMLRequest();
            //Marshaller marshaller = SOAPSAMLXACMLUtil.getMarshaller();

            RequestContext requestContext = RequestResponseContextFactory.createRequestCtx();
            requestContext.setRequest(requestType);
            //requestContext.readRequest(byteArrayInputStream);

            // Convert JAXBElement to DOM
            //DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //dbf.setNamespaceAware(true);
            //DocumentBuilder db = dbf.newDocumentBuilder();
            //Document doc = db.newDocument();
            //marshaller.marshal(request, doc);
            //requestContext.readRequest(doc);

            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            InputStream configInputStream = tcl.getResourceAsStream("policyConfig.xml");
            //File file = new File("C:/dev/hieos/src/XACMLutil/test/policyConfig.xml");
            //configInputStream = new FileInputStream(file);
            // Invoke the PDP.
            PolicyDecisionPoint pdp = new JBossPDP(configInputStream);
            ResponseContext response = pdp.evaluate(requestContext);
            // Print the result
            this.print(response.getResult());
        } catch (IOException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    /**
     *
     * @return
     */
    private PDPRequest getDummyPDPRequest() {
        PDPRequest request = new PDPRequest();

        // Issuer
        request.setIssuer("HIEOS TEST ISSUER");

        // Action requested
        request.setAction("urn:ihe:iti:2007:CrossGatewayQuery");

        // Load up Subject attributes
        List<Attribute> subjectAttributes = request.getSubjectAttributes();

        // subject-id
        StringValueAttribute attrSubjectId = new StringValueAttribute();
        attrSubjectId.setId(PolicyConstants.XACML_SUBJECT_ID);
        attrSubjectId.setValue("SUBJECT-ID");

        // organization
        StringValueAttribute attrOrganization = new StringValueAttribute();
        attrOrganization.setId(PolicyConstants.XACML_SUBJECT_ORGANIZATION);
        attrOrganization.setValue("ORG");

        // organization-id
        StringValueAttribute attrOrganizationId = new StringValueAttribute();
        attrOrganizationId.setId(PolicyConstants.XACML_SUBJECT_ORGANIZATION_ID);
        attrOrganizationId.setValue("1.1");

        // role
        StringValueAttribute attrRole = new StringValueAttribute();
        attrRole.setId(PolicyConstants.XACML_SUBJECT_ROLE);
        attrRole.setValue("DOCTOR");

        // purposeofuse
        StringValueAttribute attrPurposeOfUse = new StringValueAttribute();
        attrPurposeOfUse.setId(PolicyConstants.XACML_SUBJECT_PURPOSE_OF_USE);
        attrPurposeOfUse.setValue("TREATMENT");

        // npi
        StringValueAttribute attrNPI = new StringValueAttribute();
        attrNPI.setId(PolicyConstants.XACML_SUBJECT_NPI);
        attrNPI.setValue("NPI");

        // TODO: Test CodedValue Types.

        subjectAttributes.add(attrSubjectId);
        subjectAttributes.add(attrOrganization);
        subjectAttributes.add(attrOrganizationId);
        subjectAttributes.add(attrRole);
        subjectAttributes.add(attrPurposeOfUse);
        subjectAttributes.add(attrNPI);

        // Resource attributes:
        List<Attribute> resourceAttributes = request.getResourceAttributes();

        // resource-id
        StringValueAttribute attrResourceId = new StringValueAttribute();
        attrResourceId.setId(PolicyConstants.XACML_RESOURCE_ID);
        attrResourceId.setValue("RESOURCE-ID");
        resourceAttributes.add(attrResourceId);

        // FIXME: !!!! if resource-id is empty, always PERMITS????

        return request;
    }

    /**
     *
     * @param result
     */
    private void print(ResultType result) {
        System.out.println("Decision = " + result.getDecision());
        System.out.println("Obligations:");
        ObligationsType obligations = result.getObligations();
        if (obligations != null) {
            for (ObligationType obligationType : obligations.getObligation()) {
                System.out.println("... Id = " + obligationType.getObligationId());
                System.out.println("... fulfillOn.name = " + obligationType.getFulfillOn().name());
                System.out.println("... fulfillOn.value = " + obligationType.getFulfillOn().value());
                List<AttributeAssignmentType> attrAssignmentTypes = obligationType.getAttributeAssignment();
                for (AttributeAssignmentType attrAssignmentType : attrAssignmentTypes) {
                    System.out.println("..... attributeId = " + attrAssignmentType.getAttributeId());
                    System.out.println("..... dataType = " + attrAssignmentType.getDataType());
                }
            }
        }
        System.out.println("Status = " + result.getStatus().getStatusCode().getValue());
        System.out.println("Resource Id = " + result.getResourceId());
    }

    
    // REMOVE: TEST
    private RequestType getXACMLRequest() {
        RequestType requestType = new RequestType();

        requestType.getSubject().add(createSubject());
        requestType.getResource().add(createResource());
        requestType.setAction(createAction());
        requestType.setEnvironment(createEnvironment());
        return requestType;
    }

    // REMOVE: TEST
    private SubjectType createSubject() {
        //Create a subject type
        SubjectType subject = new SubjectType();
        subject.setSubjectCategory("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");

        subject.getAttribute().addAll(getSubjectAttributes());

        return subject;
    }

    // REMOVE: TEST
    public ResourceType createResource() {
        ResourceType resourceType = new ResourceType();

        AttributeType attResourceID = RequestAttributeFactory.createStringAttributeType(
                "urn:oasis:names:tc:xacml:1.0:resource:resource-id", issuer,
                "RESOURCE-ID");

        /*
        //Create a multi-valued attribute - hl7 permissions
        AttributeType multi = new AttributeType();
        multi.setAttributeId("urn:va:xacml:2.0:interop:rsa8:subject:hl7:permission");
        multi.setDataType("http://www.w3.org/2001/XMLSchema#string");

        if (issuer != null) {
        multi.setIssuer(issuer);
        }

        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-010"));
        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-012"));
        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-005"));
        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-003"));


        AttributeType attConfidentialityCode = RequestAttributeFactory.createStringAttributeType(
        "urn:va:xacml:2.0:interop:rsa8:resource:hl7:confidentiality-code", issuer,
        "MA");

        AttributeType attDissentedSubjectId = RequestAttributeFactory.createStringAttributeType(
        "urn:va:xacml:2.0:interop:rsa8:resource:hl7:radiology:dissented-subject-id", issuer,
        "Doctor, Bob I");
         *
         */

        //Add the attributes into the resource
        resourceType.getAttribute().add(attResourceID);
        /*resourceType.getAttribute().add(multi);
        resourceType.getAttribute().add(attConfidentialityCode);
        resourceType.getAttribute().add(attDissentedSubjectId);*/
        return resourceType;
    }

    // REMOVE: TEST
    private ActionType createAction() {
        try {
            ActionType actionType = new ActionType();
            URI uri = new URI("urn:ihe:iti:2007:CrossGatewayQuery");
            AttributeType attActionID = RequestAttributeFactory.createAnyURIAttributeType(
                    "urn:oasis:names:tc:xacml:1.0:action:action-id",
                    issuer, uri);
            // TBD
            //AttributeType attActionID = RequestAttributeFactory.createStringAttributeType(
            //        "urn:oasis:names:tc:xacml:1.0:action:action-id", issuer, "read");
            actionType.getAttribute().add(attActionID);
            return actionType;
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    /* FIXME: Put constants in common place and reference from STS also.
     *
    "urn:oasis:names:tc:xacml:1.0:subject:subject-id",
    "urn:oasis:names:tc:xspa:1.0:subject:organization",
    "urn:oasis:names:tc:xspa:1.0:subject:organization-id",
    "urn:oasis:names:tc:xspa:1.0:subject:hl7:permission",
    "urn:oasis:names:tc:xacml:2.0:subject:role",
    "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse",
    "urn:oasis:names:tc:xacml:1.0:resource:resource-id",
    "urn:oasis:names:tc:xspa:1.0:resource:hl7:type",
    "urn:oasis:names:tc:xspa:1.0:environment:locality",
    "urn:oasis:names:tc:xspa:2.0:subject:npi"
     *
     */
    // REMOVE: TEST
    private List<AttributeType> getSubjectAttributes() {
        List<AttributeType> attrList = new ArrayList<AttributeType>();

        // Create Subject attributes

        // subject-id
        AttributeType attSubjectId = RequestAttributeFactory.createStringAttributeType(
                "urn:oasis:names:tc:xacml:1.0:subject:subject-id", issuer, "SUBJECT-ID");

        // organization
        AttributeType attOrganization = RequestAttributeFactory.createStringAttributeType(
                "urn:oasis:names:tc:xspa:1.0:subject:organization", issuer, "ORGANIZATION");

        // organization-id
        AttributeType attOrganizationId = RequestAttributeFactory.createStringAttributeType(
                "urn:oasis:names:tc:xspa:1.0:subject:organization-id", issuer, "1.1");

        //Create a multi-valued attribute - hl7 permissions
        AttributeType multi = new AttributeType();
        multi.setAttributeId("urn:oasis:names:tc:xspa:1.0:subject:hl7:permission");
        multi.setDataType("http://www.w3.org/2001/XMLSchema#string");
        if (issuer != null) {
            multi.setIssuer(issuer);
        }
        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-010"));
        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-012"));
        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-017"));
        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-005"));
        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-003"));
        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-009"));
        multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-006"));

        // role
        AttributeType attRole = RequestAttributeFactory.createStringAttributeType(
                "urn:oasis:names:tc:xacml:2.0:subject:role", issuer, "DOCTOR");

        // purposeofuse
        AttributeType attPurposeOfUse = RequestAttributeFactory.createStringAttributeType(
                "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse", issuer, "PURPOSEOFUSE");

        // npi
        AttributeType attNPI = RequestAttributeFactory.createStringAttributeType(
                "urn:oasis:names:tc:xspa:2.0:subject:npi", issuer, "NPI");

        // locality
        AttributeType attLocality = RequestAttributeFactory.createStringAttributeType(
                "urn:oasis:names:tc:xacml:1.0:subject:locality", issuer, "LOCALITY");

        attrList.add(attSubjectId);
        attrList.add(attOrganization);
        attrList.add(attOrganizationId);
        attrList.add(attRole);
        attrList.add(attPurposeOfUse);
        attrList.add(attNPI);
        attrList.add(multi);
        attrList.add(attLocality);

        return attrList;
    }

    // REMOVE: TEST
    private EnvironmentType createEnvironment() {
        EnvironmentType env = new EnvironmentType();

        AttributeType attFacility = RequestAttributeFactory.createStringAttributeType(
                "urn:va:xacml:2.0:interop:rsa8:environment:locality", issuer, "Facility A");

        env.getAttribute().add(attFacility);
        return env;
    }

    // REMOVE: TEST
    private AttributeValueType createAttributeValueType(String value) {
        AttributeValueType avt = new AttributeValueType();
        avt.getContent().add(value);
        return avt;
    }
}
