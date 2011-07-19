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
package com.vangent.hieos.services.pdp.resource;

import com.vangent.hieos.policyutil.pip.model.PatientConsentDirectives;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.policyutil.pip.client.PIPClient;
import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.pip.model.PIPRequest;
import com.vangent.hieos.policyutil.pip.model.PIPResponse;
import com.vangent.hieos.policyutil.util.PolicyUtil;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xml.XMLParser;

import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceContentType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceType;
import org.apache.axiom.om.OMElement;
import org.w3c.dom.Element;

/**
 *
 * @author Bernie Thuman
 */
public class ResourceContentFinder {

    private XConfigActor pipConfig;

    /**
     * 
     * @param pipConfig
     */
    public ResourceContentFinder(XConfigActor pipConfig) {
        this.pipConfig = pipConfig;
    }

    /**
     * 
     * @param requestType
     * @throws PolicyException
     */
    public void addResourceContentToRequest(RequestType requestType) throws PolicyException {
        // FIXME: What if a resourceId is not supplied (NHIN??)

        // FIXME: Guard here against no resources.
        // Only picks the first Resource
        // FIXME: Only dealing with the first resource here ...
        ResourceType resourceType = requestType.getResource().get(0);

        // Get the patient id.
        String resourceId = PolicyUtil.getResourceId(resourceType);  // The patientId (CX formatted).
        SubjectIdentifier patientId = new SubjectIdentifier(resourceId);

        // Go to the PIP and get the ConsentDirectives
        PIPResponse pipResponse = this.getPatientConsentDirectives(patientId);

        // Get the String (ConsentDirectives) to add as ResourceContent.
        PatientConsentDirectives consentDirectives = pipResponse.getPatientConsentDirectives();
        OMElement resourceContent = consentDirectives.getContent();
        if (resourceContent != null) {
            this.addResourceContent(resourceType, resourceContent);
        }
    }

    /**
     * 
     * @param resourceType
     * @param resourceContent
     * @throws PolicyException
     */
    private void addResourceContent(ResourceType resourceType, OMElement resourceContent) throws PolicyException {
        try {
            // First convert Axiom OMElement to DOM Element
            Element resourceContentElement;
            try {
                resourceContentElement = XMLParser.convertOMToDOM(resourceContent);
            } catch (XMLParserException ex) {
                throw new PolicyException("Unable to convert OM to DOM: " + ex.getMessage());
            }

            // Add node as ResourceContent to the Resource.
            ResourceContentType resourceContentType = new ResourceContentType();
            resourceContentType.getContent().add(resourceContentElement);
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
    private PIPResponse getPatientConsentDirectives(SubjectIdentifier patientId) throws PolicyException {
        PIPClient pipClient = new PIPClient(this.pipConfig);
        PIPRequest pipRequest = new PIPRequest();
        pipRequest.setPatientId(patientId);
        return pipClient.getPatientConsentDirectives(pipRequest);
    }
}
