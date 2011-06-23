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
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.policyutil.client.PIPClient;
import com.vangent.hieos.policyutil.exception.PolicyException;
import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceContentType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceType;

import org.apache.axis2.AxisFault;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
     * @throws PolicyException
     */
    public void addMissingAttributes() throws PolicyException {
        SubjectIdentifier patientId = new SubjectIdentifier();
        // FIXME: Should we just pull from resource-id???
        // TODO: fill in patientId properly from "requestType".

        // Get info from PIP... just pass XML to Policy engine?  May be more configurable????
        this.consentDirectives = this.getPatientConsentDirectives(patientId);
        this.addResourceContent();
    }

    /**
     *
     * @throws PolicyException
     */
    private void addResourceContent() throws PolicyException {
        try {
            // Get the content
            String content = this.consentDirectives.getContent();

            // Convert the content into a DOM node.
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
            Document document = documentBuilder.parse(bais);
            Element node = document.getDocumentElement();

            // Add node as ResourceContent to the Resource.
            ResourceContentType resourceContentType = new ResourceContentType();
            resourceContentType.getContent().add(node);
            resourceType.setResourceContent(resourceContentType);

        } catch (Exception ex) {
            throw new PolicyException("Exception trying to get Consent Directives: " + ex.getMessage());
        }
    }

    /**
     * 
     * @param patientId
     * @return
     * @throws PolicyException
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
