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
package com.vangent.hieos.hl7v3util.model.subject;

import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201304UV02_Message;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectMergeRequestBuilder extends SubjectBuilder {

    private final static String XPATH_PRIOR_REGISTRATION =
            "./ns:controlActProcess/ns:subject/ns:registrationEvent/ns:replacementOf/ns:priorRegistration/ns:subject1/ns:priorRegisteredRole[1]";

    /**
     *
     * @param message
     * @return
     * @throws ModelBuilderException
     */
    public SubjectMergeRequest buildSubjectMergeRequest(PRPA_IN201304UV02_Message message) throws ModelBuilderException {
        // Build "surviving" subject.
        Subject survivingSubject = this.buildSubject(message);

        // Build "subsumed" subject.
        Subject subsumedSubject = this.buildSubsumedSubject(message);

        // Create SubjectMergeRequest.
        SubjectMergeRequest subjectMergeRequest = new SubjectMergeRequest();
        subjectMergeRequest.setSurvivingSubject(survivingSubject);
        subjectMergeRequest.setSubsumedSubject(subsumedSubject);

        return subjectMergeRequest;
    }

    /**
     *
     * @param message
     * @return
     * @throws ModelBuilderException
     */
    private Subject buildSubject(PRPA_IN201304UV02_Message message) throws ModelBuilderException {
        return this.buildSubjectFromMessage(message);
    }

    /**
     *
     * @param message
     * @return
     * @throws ModelBuilderException
     */
    private Subject buildSubsumedSubject(PRPA_IN201304UV02_Message message) throws ModelBuilderException {
        Subject subsumedSubject = new Subject();
        try {
            OMElement priorRegistrationNode = this.selectSingleNode(message.getMessageNode(), XPATH_PRIOR_REGISTRATION);
            this.setSubjectIdentifiers(subsumedSubject, priorRegistrationNode);
        } catch (XPathHelperException e) {
            throw new ModelBuilderException("No prior registration found on request: " + e.getMessage());
        }
        return subsumedSubject;
    }
}
