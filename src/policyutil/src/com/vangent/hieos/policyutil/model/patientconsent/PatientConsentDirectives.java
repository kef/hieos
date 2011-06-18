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
package com.vangent.hieos.policyutil.model.patientconsent;

import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class PatientConsentDirectives {

    private SubjectIdentifier patientId;
    private boolean alwaysAuthorize = true;
    private List<Organization> allowedOrganizations = new ArrayList<Organization>();
    private List<Organization> blockedOrganizations = new ArrayList<Organization>();
    private List<Individual> blockedIndividuals = new ArrayList<Individual>();
    private List<Individual> sensitiveAccessIndividuals = new ArrayList<Individual>();
    private List<DocumentType> sensitiveDocumentTypes = new ArrayList<DocumentType>();

    public SubjectIdentifier getPatientId() {
        return patientId;
    }

    public void setPatientId(SubjectIdentifier patientId) {
        this.patientId = patientId;
    }

    public List<Organization> getAllowedOrganizations() {
        return allowedOrganizations;
    }

    public boolean isAlwaysAuthorize() {
        return alwaysAuthorize;
    }

    public void setAlwaysAuthorize(boolean alwaysAuthorize) {
        this.alwaysAuthorize = alwaysAuthorize;
    }

    public List<Individual> getBlockedIndividuals() {
        return blockedIndividuals;
    }

    public List<Organization> getBlockedOrganizations() {
        return blockedOrganizations;
    }

    public List<Individual> getSensitiveAccessIndividuals() {
        return sensitiveAccessIndividuals;
    }

    public List<DocumentType> getSensitiveDocumentTypes() {
        return sensitiveDocumentTypes;
    }
}
