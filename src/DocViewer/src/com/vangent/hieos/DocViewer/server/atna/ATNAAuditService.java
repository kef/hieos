/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.DocViewer.server.atna;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.IHETransaction;
import com.vangent.hieos.xutil.atna.ATNAAuditEventHelper;
import com.vangent.hieos.xutil.atna.ATNAAuditEventQuery;
import com.vangent.hieos.xutil.atna.ATNAAuditEventRetrieveDocumentSet;
import com.vangent.hieos.xutil.atna.UserContext;
import com.vangent.hieos.xutil.atna.XATNALogger;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class ATNAAuditService {
	private final static Logger logger = Logger
			.getLogger(ATNAAuditService.class);
	private UserContext userContext;

	/**
	 * 
	 * @param authCreds
	 * @param authCtxt
	 */
	public ATNAAuditService(
			Credentials authCreds,
			AuthenticationContext authCtxt) {

		// Fill in UserContext instance.
		String userId = authCreds.getUserId();
		// TBD: Should we use getFullName() instead?
		String userName = authCtxt.getUserProfile().getDistinguishedName();
		userContext = new UserContext();
		userContext.setUserId(userId);
		userContext.setUserName(userName);
		// TODO: Role
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isPerformAudit() {
		boolean performAudit = true;
		try {
			XATNALogger xATNALogger = new XATNALogger(null /* userContext */);
			performAudit = xATNALogger.isPerformAudit();
		} catch (Exception ex) {
			logger.error(
					"Could not get XATNALogger - defaulting to ATNA logging TRUE",
					ex);
		}
		return performAudit;
	}

	/**
	 * 
	 * @param request
	 * @param subjectSearchResponse
	 * @param homeCommunityId
	 * @param targetEndpoint
	 * @param outcome
	 */
	public void auditPatientDemographicsQuery(
			PRPA_IN201305UV02_Message request,
			SubjectSearchResponse subjectSearchResponse,
			String homeCommunityId, String targetEndpoint,IHETransaction iheTransaction,
			ATNAAuditEvent.OutcomeIndicator outcome) {
		try {
			XATNALogger xATNALogger = new XATNALogger(this.userContext);
			ATNAAuditEventQuery auditEvent = com.vangent.hieos.hl7v3util.atna.ATNAAuditEventHelper
					.getATNAAuditEventPDQQueryInitiator(
							ATNAAuditEvent.ActorType.PATIENT_DEMOGRAPHICS_CONSUMER,
							request, homeCommunityId, targetEndpoint);
			auditEvent.setTransaction(iheTransaction);
			auditEvent.setOutcomeIndicator(outcome);

			// Load up patient ids.
			List<String> patientIds = auditEvent.getPatientIds();
			List<Subject> subjects = subjectSearchResponse.getSubjects();
			for (Subject subject : subjects) {
				List<SubjectIdentifier> subjectIdentifiers = subject
						.getSubjectIdentifiers();
				for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
					patientIds.add(subjectIdentifier.getCXFormatted());
				}
			}
			xATNALogger.audit(auditEvent);
		} catch (Exception ex) {
			logger.error("Could not perform ATNA audit", ex);
		}
	}

		
	/**
	 * 
	 * @param request
	 * @param homeCommunityId
	 * @param targetEndpoint
	 * @param outcome
	 */
	public void auditRegistryStoredQuery(OMElement request,
			String homeCommunityId, String targetEndpoint,
			ATNAAuditEvent.OutcomeIndicator outcome) {
		try {
			XATNALogger xATNALogger = new XATNALogger(this.userContext);

			// xATNALogger.setParentThreadMessageContext(parentThreadMessageContext);
			ATNAAuditEvent.IHETransaction transaction = ATNAAuditEvent.IHETransaction.ITI18;
			ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper
					.getATNAAuditEventRegistryStoredQuery(request);
			auditEvent.setTargetEndpoint(targetEndpoint);
			auditEvent.setTransaction(transaction);
			auditEvent.setActorType(ATNAAuditEvent.ActorType.DOCCONSUMER);
			auditEvent
					.setAuditEventType(ATNAAuditEvent.AuditEventType.QUERY_INITIATOR);
			auditEvent.setHomeCommunityId(homeCommunityId);
			auditEvent.setOutcomeIndicator(outcome);
			xATNALogger.audit(auditEvent);
		} catch (Exception ex) {
			// Eat exception.
			logger.error("Could not perform ATNA audit", ex);
		}
	}

	/**
	 * 
	 * @param request
	 * @param homeCommunityId
	 * @param targetEndpoint
	 * @param outcome
	 */
	public void auditRetrieveDocumentSet(OMElement request,
			String homeCommunityId, // Not used - assumed to be inside request.
			String targetEndpoint, ATNAAuditEvent.OutcomeIndicator outcome) {
		try {
			XATNALogger xATNALogger = new XATNALogger(this.userContext);
			ATNAAuditEventRetrieveDocumentSet auditEvent = ATNAAuditEventHelper
					.getATNAAuditEventRetrieveDocumentSet(request);
			ATNAAuditEvent.IHETransaction transaction = ATNAAuditEvent.IHETransaction.ITI43;
			auditEvent.setTargetEndpoint(targetEndpoint);
			auditEvent.setTransaction(transaction);
			auditEvent.setActorType(ATNAAuditEvent.ActorType.DOCCONSUMER);
			auditEvent.setAuditEventType(ATNAAuditEvent.AuditEventType.IMPORT);
			auditEvent.setOutcomeIndicator(outcome);
			xATNALogger.audit(auditEvent);
		} catch (Exception ex) {
			// Eat exception.
			logger.error("Could not perform ATNA audit", ex);
		}
	}
}
