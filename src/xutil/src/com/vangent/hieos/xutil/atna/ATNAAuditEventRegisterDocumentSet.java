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
package com.vangent.hieos.xutil.atna;

/**
 * Covers ITI-42.
 *
 * @author Bernie Thuman
 */
public class ATNAAuditEventRegisterDocumentSet extends ATNAAuditEvent {

    private String submissionSetId;
    private String patientId;

    /**
     *
     * @return
     */
    public String getSubmissionSetId() {
        return submissionSetId;
    }

    /**
     * 
     * @param submissionSetId
     */
    public void setSubmissionSetId(String submissionSetId) {
        this.submissionSetId = submissionSetId;
    }

    /**
     *
     * @return
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     *
     * @param patientId
     */
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
