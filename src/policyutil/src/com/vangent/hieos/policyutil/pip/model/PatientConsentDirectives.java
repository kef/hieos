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
package com.vangent.hieos.policyutil.pip.model;

import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class PatientConsentDirectives {

    public enum StatusType {

        ACTIVE, INACTIVE, NOT_ESTABLISHED
    };
    private SubjectIdentifier patientId;
    private OMElement content;
    private List<PatientConsentRule> patientConsentRules = new ArrayList<PatientConsentRule>();
    private StatusType status;

    /**
     *
     * @return
     */
    public OMElement getContent() {
        return content;
    }

    /**
     *
     * @param content
     */
    public void setContent(OMElement content) {
        this.content = content;
    }

    /**
     *
     * @return
     */
    public SubjectIdentifier getPatientId() {
        return patientId;
    }

    /**
     *
     * @param patientId
     */
    public void setPatientId(SubjectIdentifier patientId) {
        this.patientId = patientId;
    }

    /**
     *
     * @return
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     */
    public void setStatus(StatusType status) {
        this.status = status;
    }

    /**
     *
     * @return
     */
    public List<PatientConsentRule> getPatientConsentRules() {
        return patientConsentRules;
    }

    /**
     *
     * @param patientConsentRule
     */
    public void add(PatientConsentRule patientConsentRule) {
        patientConsentRules.add(patientConsentRule);
    }
}
