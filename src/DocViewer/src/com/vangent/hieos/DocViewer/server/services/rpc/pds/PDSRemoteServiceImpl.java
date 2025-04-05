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
package com.vangent.hieos.DocViewer.server.services.rpc.pds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.vangent.hieos.DocViewer.client.exception.RemoteServiceException;
import com.vangent.hieos.DocViewer.client.model.patient.PatientDTO;
import com.vangent.hieos.DocViewer.client.model.patient.PatientSearchCriteriaDTO;
import com.vangent.hieos.DocViewer.client.services.rpc.PDSRemoteService;
import com.vangent.hieos.DocViewer.server.atna.ATNAAuditService;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.vangent.hieos.hl7v3util.client.PDSClient;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;
import com.vangent.hieos.hl7v3util.model.subject.SubjectBuilder;
import com.vangent.hieos.subjectmodel.CodedValue;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import com.vangent.hieos.subjectmodel.SubjectName;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.OutcomeIndicator;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PDSRemoteServiceImpl extends RemoteServiceServlet implements
		PDSRemoteService {
	private final String SSN_IDENTIFIER_DOMAIN = "2.16.840.1.113883.4.1";

	/**
	 * 
	 */
	private static final long serialVersionUID = 8292224336985456850L;
	private final ServletUtilMixin servletUtil = new ServletUtilMixin();

	@Override
	public void init() {
		servletUtil.init(this.getServletContext());
	}

	/**
	 * 
	 */
	@Override
	public List<PatientDTO> getPatients(PatientSearchCriteriaDTO patientSearchCriteria)
			throws RemoteServiceException {
		// See if we have a valid session ...
		HttpServletRequest request = this.getThreadLocalRequest();
		boolean validSession = ServletUtilMixin.isValidSession(request);
		if (!validSession) {
			throw new RemoteServiceException("Invalid Session!");
		}

		// Issue PDQ - and do necessary conversions.

		// TODO: Implement use of AuthenticationContext (XUA, etc.).
		// Get authentication context from session.
		HttpSession session = request.getSession(false);
		AuthenticationContext authCtxt = (AuthenticationContext) session
				.getAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_CONTEXT);
		Credentials authCreds = (Credentials) session
				.getAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_CREDS);

		// Convert PatientSearchCriteria to SubjectSearchCriteria.
		System.out.println("Converting ...");
		SubjectSearchCriteria subjectSearchCriteria = this
				.buildSubjectSearchCriteria(patientSearchCriteria);

		// Issue PDQ.
		System.out.println("PDQ ...");
		SubjectSearchResponse subjectSearchResponse = this.findCandidatesQuery(
				authCreds, authCtxt, subjectSearchCriteria);

		// Convert response.
		System.out.println("Converting ...");
		List<PatientDTO> patients = this.buildPatients(subjectSearchResponse);

		System.out.println("Returning ...");
		return patients;
	}

	/**
	 * 
	 * @param patientSearchCriteria
	 * @return SubjectSearchCriteria
	 */
	private SubjectSearchCriteria buildSubjectSearchCriteria(
			PatientSearchCriteriaDTO patientSearchCriteria) {
		SubjectSearchCriteria subjectSearchCriteria = new SubjectSearchCriteria();
		Subject subject = new Subject();

		// TODO: Deal with HRN and SSN(last4).
		SubjectName subjectName = new SubjectName();

		// Name:
		subjectName.setGivenName(patientSearchCriteria.getGivenName());
		subjectName.setFamilyName(patientSearchCriteria.getFamilyName());
		subjectName.setFuzzySearchMode(patientSearchCriteria
				.isFuzzyNameSearch());
		subject.addSubjectName(subjectName);

		// DOB:
		subject.setBirthTime(patientSearchCriteria.getDateOfBirth());

		// SSN(last4)
		String ssnValue = patientSearchCriteria.getSsnLast4();
		if (StringUtils.isNotBlank(ssnValue)) {

			SubjectIdentifierDomain ssnDomain = new SubjectIdentifierDomain();
			ssnDomain.setUniversalId(SSN_IDENTIFIER_DOMAIN);
			ssnDomain.setUniversalIdType("ISO");

			SubjectIdentifier ssnId = new SubjectIdentifier();
			ssnId.setIdentifierDomain(ssnDomain);
			ssnId.setIdentifier(ssnValue);

			subject.addSubjectIdentifier(ssnId);
		}

		// Gender:
		String genderCode = patientSearchCriteria.getGenderCode();
		if (genderCode.equals("UN")) {
			// Do not send.
			genderCode = null;
		}
		if (genderCode != null) {
			CodedValue gender = new CodedValue();
			gender.setCode(genderCode);
			subject.setGender(gender);
		}

		subjectSearchCriteria.setSubject(subject);
		return subjectSearchCriteria;
	}

	/**
	 * 
	 * @param authCreds
	 * @param authCtxt
	 * @param subjectSearchCriteria
	 * @return
	 * @throws RemoteServiceException
	 */
	private SubjectSearchResponse findCandidatesQuery(
			Credentials authCreds,
			AuthenticationContext authCtxt,
			SubjectSearchCriteria subjectSearchCriteria)
			throws RemoteServiceException {
		SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
		XConfigActor pdsConfig = this.getPDSConfig();
		PDSClient pdsClient = new PDSClient(pdsConfig);
		DeviceInfo senderDeviceInfo = new DeviceInfo();
		senderDeviceInfo.setId(servletUtil.getProperty("DeviceId"));
		senderDeviceInfo.setName(servletUtil.getProperty("DeviceName"));
		DeviceInfo receiverDeviceInfo = new DeviceInfo();
		receiverDeviceInfo.setId(pdsConfig.getProperty("DeviceId"));
		receiverDeviceInfo.setName(pdsConfig.getProperty("DeviceName"));

		try {
			subjectSearchResponse = this.findCandidatesQuery(authCreds,
					authCtxt, pdsClient, senderDeviceInfo, receiverDeviceInfo,
					subjectSearchCriteria);
		} catch (SOAPFaultException ex) {
			ex.printStackTrace();
			throw new RemoteServiceException(
					"Unable to contact patient demographics service - "
							+ ex.getMessage());
		}
		return subjectSearchResponse;
	}

	/**
	 * 
	 * @param authCreds
	 * @param authCtxt
	 * @param pdsClient
	 * @param senderDeviceInfo
	 * @param receiverDeviceInfo
	 * @param subjectSearchCriteria
	 * @return
	 * @throws SOAPFaultException
	 */
	public SubjectSearchResponse findCandidatesQuery(
			Credentials authCreds,
			AuthenticationContext authCtxt,
			PDSClient pdsClient, DeviceInfo senderDeviceInfo,
			DeviceInfo receiverDeviceInfo,
			SubjectSearchCriteria subjectSearchCriteria)
			throws SOAPFaultException {
		SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();

		// Build the HL7v3 message.
		PRPA_IN201305UV02_Message_Builder pdqQueryBuilder = new PRPA_IN201305UV02_Message_Builder(
				senderDeviceInfo, receiverDeviceInfo);

		PRPA_IN201305UV02_Message request = pdqQueryBuilder
				.buildPRPA_IN201305UV02_Message(subjectSearchCriteria);
		try {
			PRPA_IN201306UV02_Message queryResponse = pdsClient
					.findCandidatesQuery(request);
			if (queryResponse != null) {
				SubjectBuilder subjectBuilder = new SubjectBuilder();
				subjectSearchResponse = subjectBuilder
						.buildSubjectSearchResponse(queryResponse);

				// ATNA Audit.
				this.audit(authCreds, authCtxt, pdsClient, request,
						subjectSearchResponse,
						ATNAAuditEvent.OutcomeIndicator.SUCCESS);

			}

		} catch (ModelBuilderException ex) {
			throw new SOAPFaultException(ex.getMessage());
		}
		return subjectSearchResponse;
	}

	/**
	 * 
	 * @return
	 */
	private XConfigActor getPDSConfig() {
		return servletUtil.getActorConfig("pds", "PDSType");
	}

	/**
	 * 
	 * @param subjectSearchResponse
	 * @return
	 */
	private List<PatientDTO> buildPatients(
			SubjectSearchResponse subjectSearchResponse) {
		List<PatientDTO> patients = new ArrayList<PatientDTO>();

		System.out.println("# of subjects: "
				+ subjectSearchResponse.getSubjects().size());
		for (Subject subject : subjectSearchResponse.getSubjects()) {
			System.out.println("Subject found!!!!");
			PatientDTO patient = this.buildPatient(subject);
			if (patient != null) {
				// Add to the patient list.
				patients.add(patient);
			}
		}
		return patients;
	}

	/**
	 * 
	 * @param subject
	 * @return
	 */
	private PatientDTO buildPatient(Subject subject) {
		/*
		 * ArrayList<Patient>(); Patient patient = new Patient();
		 * patient.setEuid("8c0ea523b89f4e4");
		 * patient.setEuidUniversalID("1.3.6.1.4.1.21367.2005.3.7");
		 * patient.setGivenName("Joe"); patient.setFamilyName("Smith");
		 * patient.setDateOfBirth(this.getDate("06/10/1965"));
		 * patient.setGender("M"); patient.setSSN("4321");
		 * patient.setMatchWeight(1.0); patients.add(patient);
		 */

		// Enterprise EUID
		SubjectIdentifier subjectIdentifier = this.getSubjectEuid(subject);
		if (subjectIdentifier == null) {
			// This patient is not associated with the enterprise assigning
			// authority.
			return null; // Early exit!
		}

		// A valid patient ... create one.
		PatientDTO patient = new PatientDTO();

		// Set patient id.
		SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier
				.getIdentifierDomain();
		patient.setEuid(subjectIdentifier.getIdentifier());
		patient.setEuidUniversalID(subjectIdentifierDomain.getUniversalId());

		// Name (use first entry).
		List<SubjectName> subjectNames = subject.getSubjectNames();
		if (subjectNames.size() > 0) {
			SubjectName subjectName = subjectNames.get(0);
			patient.setGivenName(subjectName.getGivenName());
			patient.setFamilyName(subjectName.getFamilyName());
			/* [03/14/13]  IHS Release 1.3 (Requirement # 7333 - Middle Name updates in DocViewer) */
			patient.setMiddleName(subjectName.getMiddleName());
		}
		// Gender.
		CodedValue gender = subject.getGender();
		if (gender != null) {
			patient.setGender(gender.getCode());
		} else {
			patient.setGender("UN"); // Unknown.
		}

		// Data of birth.
		Date dateOfBirth = subject.getBirthTime();
		if (dateOfBirth == null) {
			dateOfBirth = this.getUnknownDate();
		}
		patient.setDateOfBirth(dateOfBirth);

		// Populate SSN field.
		SubjectIdentifier ssnSubjectIdentifier = this.getSubjectSSN(subject);
		if (ssnSubjectIdentifier != null) {
			patient.setSSN(this.formatSSN(ssnSubjectIdentifier));
		} else {
			patient.setSSN("N/A");
		}

		// Match confidence percentage.
		patient.setMatchConfidencePercentage(subject
				.getMatchConfidencePercentage());

		return patient;
	}

	/**
	 * 
	 * @param subject
	 * @return
	 */
	private SubjectIdentifier getSubjectEuid(Subject subject) {
		String enterpriseAssigningAuthority = servletUtil
				.getProperty("EnterpriseAssigningAuthority");
		return this.getSubjectForIdentifierDomain(subject,
				enterpriseAssigningAuthority);
	}

	/**
	 * 
	 * @param subject
	 * @return
	 */
	private SubjectIdentifier getSubjectSSN(Subject subject) {
		return this.getSubjectForIdentifierDomain(subject,
				SSN_IDENTIFIER_DOMAIN);
	}

	/**
	 * 
	 * @param identifier
	 * @return
	 */
	private String formatSSN(SubjectIdentifier identifier) {
		String id = identifier.getIdentifier();
		String formattedSSN = "N/A";
		int len = id.length();
		if (len >= 4) {
			// Get last 4 characters.
			String last4 = id.substring(len - 4);
			formattedSSN = "xxx-xxxx-" + last4;
		}
		return formattedSSN;
	}

	/**
	 * 
	 * @param subject
	 * @param universalId
	 * @return
	 */
	private SubjectIdentifier getSubjectForIdentifierDomain(Subject subject,
			String universalId) {

		List<SubjectIdentifier> subjectIdentifiers = subject
				.getSubjectIdentifiers();

		for (SubjectIdentifier subjectIdentifier : subjectIdentifiers) {
			SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier
					.getIdentifierDomain();
			if (subjectIdentifierDomain.getUniversalId().equals(universalId)) {
				// Match ...
				return subjectIdentifier;
			}
		}
		return null;
	}

	/**
	 * 
	 * @return
	 */
	private Date getUnknownDate() {
		Date unknownDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			unknownDate = sdf.parse("00010101");
		} catch (ParseException ex) {
			// FIXME:
		}
		return unknownDate;
	}

	/**
	 * 
	 * @param authCreds
	 * @param authCtxt
	 * @param pdsClient
	 * @param request
	 * @param subjectSearchResponse
	 * @param outcome
	 */
	private void audit(Credentials authCreds,
			AuthenticationContext authCtxt,
			PDSClient pdsClient, PRPA_IN201305UV02_Message request,
			SubjectSearchResponse subjectSearchResponse,
			OutcomeIndicator outcome) {
		
		if (ATNAAuditService.isPerformAudit()) {
			ATNAAuditService auditService = new ATNAAuditService(authCreds, authCtxt);
			XConfigActor pdsConfig = pdsClient.getConfig();
			XConfigTransaction txn = pdsConfig
					.getTransaction("PatientRegistryFindCandidatesQuery");
			String targetEndpoint = txn.getEndpointURL();
			String homeCommunityId = null; // FIXME?

			auditService.auditPatientDemographicsQuery(request,
					subjectSearchResponse, homeCommunityId, targetEndpoint,ATNAAuditEvent.IHETransaction.ITI47,
					outcome);
		}
	}
}