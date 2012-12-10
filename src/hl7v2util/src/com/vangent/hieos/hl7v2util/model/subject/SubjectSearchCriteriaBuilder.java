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
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectSearchCriteriaBuilder {

    private Terser terser;

    /**
     *
     * @param terser
     */
    public SubjectSearchCriteriaBuilder(Terser terser) {
        this.terser = terser;
    }

    /**
     *
     * @return
     * @throws HL7Exception
     */
    public SubjectSearchCriteria buildSubjectSearchCriteria() throws HL7Exception {
        // Build SubjectSearchCriteria.
        SubjectSearchCriteria subjectSearchCriteria = new SubjectSearchCriteria();
        Subject subject = new Subject();
        subjectSearchCriteria.setSubject(subject);

        // Pull fields from QPD segment.
        Segment segment = terser.getSegment("/QPD");

        // Patient id.
        Type[] patientIdTypes = segment.getField(3);
        if (patientIdTypes.length == 0) {
            throw new HL7Exception("No patient identifier supplied");
        }
        String patientIdCXFormatted = patientIdTypes[0].encode(); // Only require first one for PIX query.
        SubjectIdentifier subjectIdentifier = new SubjectIdentifier(patientIdCXFormatted);
        subject.addSubjectIdentifier(subjectIdentifier);

        // Get list (repeating field 4) of assigning authorities of interest - "What Domains Returned?"
        Type[] scopedAssigningAuthorityTypes = segment.getField(4);
        int numScopedAssigningAuthorityTypes = scopedAssigningAuthorityTypes.length;
        for (int i = 0; i < numScopedAssigningAuthorityTypes; i++) {
            String assigningAuthorityCXformatted = scopedAssigningAuthorityTypes[i].encode();
            SubjectIdentifierDomain scopingAssigningAuthority = new SubjectIdentifierDomain(assigningAuthorityCXformatted);
            subjectSearchCriteria.addScopingSubjectIdentifierDomain(scopingAssigningAuthority);
        }
        return subjectSearchCriteria;
    }
}
