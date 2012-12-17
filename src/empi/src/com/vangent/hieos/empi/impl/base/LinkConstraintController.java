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
package com.vangent.hieos.empi.impl.base;

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.match.ScoredRecord;
import com.vangent.hieos.empi.model.SubjectReviewItem;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.empi.persistence.SubjectController;
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class LinkConstraintController {

    private static final Logger logger = Logger.getLogger(LinkConstraintController.class);
    private PersistenceManager persistenceManager;

    /**
     *
     */
    private LinkConstraintController() {
        // Do not allow.
    }

    /**
     *
     * @param persistenceManager
     */
    public LinkConstraintController(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    /**
     *
     * @param newSubject
     * @param matchedRecords
     * @return
     * @throws EMPIException
     */
    public List<SubjectReviewItem> getPotentialDuplicates(Subject newSubject, List<ScoredRecord> matchedRecords) throws EMPIException {
        List<SubjectReviewItem> subjectReviewItems = new ArrayList<SubjectReviewItem>();
        for (ScoredRecord matchedRecord : matchedRecords) {
            if (this.isPotentialDuplicate(newSubject, matchedRecord)) {
                SubjectReviewItem subjectReviewItem = new SubjectReviewItem();
                subjectReviewItem.setInternalId(newSubject.getInternalId());
                subjectReviewItem.setOtherSubjectId(matchedRecord.getRecord().getInternalId());
                subjectReviewItem.setReviewType(SubjectReviewItem.ReviewType.POTENTIAL_DUPLICATE);
                subjectReviewItems.add(subjectReviewItem);
            }
        }
        return subjectReviewItems;
    }

    /**
     *
     * @param newSubject
     * @param matchedRecords
     * @return
     * @throws EMPIException
     */
    public boolean isPotentialDuplicate(Subject newSubject, List<ScoredRecord> matchedRecords) throws EMPIException {

        // First check scored record against record matches to see if it is safe to link
        // the records.
        for (ScoredRecord matchedRecord : matchedRecords) {
            if (this.isPotentialDuplicate(newSubject, matchedRecord)) {
                // Link is not allowed.
                return false;  // Early exit.
            }
        }

        /* RETHINK!!!
         // FIXME: NEED TO OPTIMIZE ... doing many DB reads where should only read once per
         // record.

         // Now, go through each pair in the merge set.
         PersistenceManager pm = this.getPersistenceManager();
         for (ScoredRecord matchedRecord : recordMatches) {
         String matchedSystemSubjectId = matchedRecord.getRecord().getId();
         // Load identifiers for matched system subject.
         List<SubjectIdentifier> matchedSubjectIdentifiers = pm.loadSubjectIdentifiers(matchedSystemSubjectId);

         for (ScoredRecord compareMatchedRecord : recordMatches) {
         String compareMatchedSystemSubjectId = compareMatchedRecord.getRecord().getId();
         if (!matchedSystemSubjectId.equals(compareMatchedSystemSubjectId)) {
         // Load identifiers for matched system subject (to compare).
         List<SubjectIdentifier> compareMatchedSubjectIdentifiers = pm.loadSubjectIdentifiers(compareMatchedSystemSubjectId);
         boolean foundMatch = this.isMatchedRecordInSameIdentifierDomain(matchedSubjectIdentifiers, compareMatchedSubjectIdentifiers);
         if (foundMatch) {
         // Found a match.
         System.out.println("+++++ Not linking subject with same identifier domain (multi-merge) +++++");
         return true;  // Early exit!
         }
         }
         }
         }
         */
        return true;  // Link is allowed.
    }

    /**
     *
     * @param newSubject
     * @param matchedRecord
     * @return
     * @throws EMPIException
     */
    public boolean isPotentialDuplicate(Subject newSubject, ScoredRecord matchedRecord) throws EMPIException {
        InternalId matchedSystemSubjectId = matchedRecord.getRecord().getInternalId();

        // Load identifiers for matched system subject.
        SubjectController subjectController = new SubjectController(persistenceManager);
        List<SubjectIdentifier> matchedSubjectIdentifiers = subjectController.loadSubjectIdentifiers(matchedSystemSubjectId);

        // Get list of the new subject's identifiers.
        List<SubjectIdentifier> newSubjectIdentifiers = newSubject.getSubjectIdentifiers();

        return this.isPotentialDuplicate(matchedSubjectIdentifiers, newSubjectIdentifiers);
    }

    /**
     *
     * @param matchedSubjectIdentifiers
     * @param newSubjectIdentifiers
     * @return
     * @throws EMPIException
     */
    private boolean isPotentialDuplicate(List<SubjectIdentifier> matchedSubjectIdentifiers, List<SubjectIdentifier> newSubjectIdentifiers) throws EMPIException {
        // NOTE: Small list, so brute force is OK.

        // See if there is a match between the "matched subject" and the subject.
        for (SubjectIdentifier subjectIdentifier : newSubjectIdentifiers) {
            SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
            for (SubjectIdentifier matchedSubjectIdentifier : matchedSubjectIdentifiers) {
                SubjectIdentifierDomain matchedSubjectIdentifierDomain = matchedSubjectIdentifier.getIdentifierDomain();
                if (subjectIdentifierDomain.equals(matchedSubjectIdentifierDomain)) {
                    // No need to look further.
                    if (logger.isTraceEnabled()) {
                        logger.trace("+++++ Not linking +++++");
                        logger.trace(" ... subject identifier = " + subjectIdentifier.getCXFormatted());
                        logger.trace(" ... matched subject identifier (same assigning authority) = " + matchedSubjectIdentifier.getCXFormatted());
                    }
                    System.out.println("+++++ Not linking +++++");
                    System.out.println(" ... subject identifier = " + subjectIdentifier.getCXFormatted());
                    System.out.println(" ... matched subject identifier (same assigning authority) = " + matchedSubjectIdentifier.getCXFormatted());

                    return true;  // Early exit!
                }
            }
        }
        return false;
    }
}
