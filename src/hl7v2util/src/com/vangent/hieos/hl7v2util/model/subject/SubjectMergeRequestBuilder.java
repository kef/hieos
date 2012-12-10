/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v2util.model.subject;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v231.datatype.CX;
import ca.uhn.hl7v2.model.v231.segment.MRG;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectMergeRequest;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectMergeRequestBuilder {

    private Terser terser;

    /**
     *
     * @param terser
     */
    public SubjectMergeRequestBuilder(Terser terser) {
        this.terser = terser;
    }

    /**
     * 
     * @return
     * @throws HL7Exception
     */
    public SubjectMergeRequest buildSubjectMergeRequest() throws HL7Exception {
        // Build SubjectMergeRequest.
        SubjectMergeRequest subjectMergeRequest = new SubjectMergeRequest();

        // Surviving subject (in PID segment).
        SubjectBuilder subjectBuilder = new SubjectBuilder(terser);
        Subject survivingSubject = subjectBuilder.buildSubject();
        subjectMergeRequest.setSurvivingSubject(survivingSubject);

        // Subsumed subject (in MRG segment).
        // SEGMENT: MRG
        MRG mrg = (MRG) terser.getSegment("MRG");
        CX[] priorPatientIdentifierListCX = mrg.getPriorPatientIdentifierList();
        if (priorPatientIdentifierListCX.length == 0) {
            throw new HL7Exception("No subsumed patient identifier supplied");
        } else {
            // Just pick the first identifier from the list.
            // FIXME?: Do we need to deal with > 1 identifier on merge list?
            CX priorPatientIdentifierCX = priorPatientIdentifierListCX[0];
            String priorPatientIdentifierCXFormatted = priorPatientIdentifierCX.encode();
            SubjectIdentifier subjectIdentifier = new SubjectIdentifier(priorPatientIdentifierCXFormatted);
            Subject subsumedSubject = new Subject();
            subsumedSubject.addSubjectIdentifier(subjectIdentifier);
            subjectMergeRequest.setSubsumedSubject(subsumedSubject);
        }
        return subjectMergeRequest;
    }
}
