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
package com.vangent.hieos.policyutil.client;

import com.vangent.hieos.hl7v3util.client.Client;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.policyutil.model.patientconsent.DocumentType;
import com.vangent.hieos.policyutil.model.patientconsent.Organization;
import com.vangent.hieos.policyutil.model.patientconsent.PatientConsentDirectives;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import org.apache.axis2.AxisFault;

/**
 *
 * @author Bernie Thuman
 */
public class PIPClient extends Client {

    /**
     *
     * @param config
     */
    public PIPClient(XConfigActor config) {
        super(config);
    }

    /**
     *
     * @param patientId
     * @return
     * @throws AxisFault
     */
    public PatientConsentDirectives getPatientConsentDirectives(
            SubjectIdentifier patientId) throws AxisFault {

        // TBD: MAY JUST SEND BACK RAW XML ... NOT SURE YET ... and use XPATH in Policy????
        // TBD: OR SHOULD THIS JUST RETURN ATTRIBUTES?  WOULD NEED SUPPORT FOR BAGS ...
        // FIXME: STUB!!!!
        PatientConsentDirectives pcd = new PatientConsentDirectives();

        Organization org = new Organization();
        SubjectIdentifier orgId = new SubjectIdentifier();
        orgId.setIdentifier("1.1");
        org.setId(orgId);
        pcd.getAllowedOrganizations().add(org);

        org = new Organization();
        orgId = new SubjectIdentifier();
        orgId.setIdentifier("1.2");
        org.setId(orgId);
        pcd.getAllowedOrganizations().add(org);

        DocumentType docType = new DocumentType();
        docType.setCode("code1");
        docType.setCodeSystem("codesystem1");
        pcd.getSensitiveDocumentTypes().add(docType);

        docType = new DocumentType();
        docType.setCode("code2");
        docType.setCodeSystem("codesystem2");
        pcd.getSensitiveDocumentTypes().add(docType);

        return pcd;
    }
}
