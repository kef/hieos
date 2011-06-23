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

        // FIXME: STUB
        PatientConsentDirectives pcd = new PatientConsentDirectives();
        String content = 
                "<hieos-consent:ConsentDirectives xmlns:hieos-consent=\"urn:hieos:policy:1.0:consent\">"+
                "  <hieos-consent:AllowedOrganizations>" +
                "    <hieos-consent:Organization>1.1</hieos-consent:Organization>" +
                "    <hieos-consent:Organization>1.2</hieos-consent:Organization>" +
                "  </hieos-consent:AllowedOrganizations>" +
                "  <hieos-consent:SensitiveDocumentTypes>" +
                "    <hieos-consent:DocumentType code=\"1\" codeSystem=\"1\"/>" +
                "    <hieos-consent:DocumentType code=\"2\" codeSystem=\"1\"/>" +
                "  </hieos-consent:SensitiveDocumentTypes>" +
                "</hieos-consent:ConsentDirectives>";
        pcd.setContent(content);
        return pcd;
    }
}
