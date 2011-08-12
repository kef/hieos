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
package com.vangent.hieos.policyutil.pdp.resource;

import com.vangent.hieos.policyutil.pip.model.PatientConsentDirectives;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.policyutil.pip.client.PIPClient;
import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.pdp.model.PDPRequest;
import com.vangent.hieos.policyutil.pip.model.PIPRequest;
import com.vangent.hieos.policyutil.pip.model.PIPResponse;
import com.vangent.hieos.xutil.hl7.formatutil.HL7FormatUtil;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIPResourceContentFinder {

    private final static Logger logger = Logger.getLogger(PIPResourceContentFinder.class);
    private XConfigActor pipConfig;

    /**
     * 
     * @param pipConfig
     */
    public PIPResourceContentFinder(XConfigActor pipConfig) {
        this.pipConfig = pipConfig;
    }

    /**
     *
     * @param pdpRequest
     * @throws PolicyException
     */
    public void addResourceContentToRequest(PDPRequest pdpRequest) throws PolicyException {
        if (pdpRequest.hasResourceContent()) {
            // Do nothing if resource content is already supplied.
            if (logger.isDebugEnabled()) {
                logger.debug("Resource Content already supplied -- not going to PIP!");
            }
            return;  // Early exit!
        }
        // FIXME: What if a resourceId is not supplied (NHIN??)

        // Get the patient id.
        String resourceId = pdpRequest.getResourceId();  // The patientId (CX formatted).
        if (!HL7FormatUtil.isCX_Formatted(resourceId)) {
            throw new PolicyException("'resource-id' (patient id) not in CX format");
        }
        SubjectIdentifier patientId = new SubjectIdentifier(resourceId);

        // Go to the PIP and get the ConsentDirectives
        PIPResponse pipResponse = this.getPatientConsentDirectives(patientId);

        // Get the String (ConsentDirectives) to add as ResourceContent.
        PatientConsentDirectives consentDirectives = pipResponse.getPatientConsentDirectives();
        OMElement resourceContent = consentDirectives.getContent();
        if (resourceContent != null) {
            pdpRequest.addResourceContent(resourceContent, false);
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
