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

import org.apache.axis2.AxisFault;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.vangent.hieos.DocViewer.client.model.patient.Patient;
import com.vangent.hieos.DocViewer.client.model.patient.PatientSearchCriteria;
import com.vangent.hieos.DocViewer.client.services.rpc.PDSRemoteService;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.hl7v3util.client.PDSClient;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectGender;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectName;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

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
	public List<Patient> getPatients(PatientSearchCriteria patientSearchCriteria) {
		// Issue PDQ - and do necessary conversions.

		// Convert PatientSearchCriteria to SubjectSearchCriteria.
		System.out.println("Converting ...");
		SubjectSearchCriteria subjectSearchCriteria = this
				.buildSubjectSearchCriteria(patientSearchCriteria);

		// Issue PDQ.
		System.out.println("PDQ ...");
		SubjectSearchResponse subjectSearchResponse = this
				.findCandidatesQuery(subjectSearchCriteria);

		// Convert response.
		System.out.println("Converting ...");
		List<Patient> patients = this.buildPatients(subjectSearchResponse);

		System.out.println("Returning ...");
		return patients;
	}

	/**
	 * 
	 * @param patientSearchCriteria
	 * @return SubjectSearchCriteria
	 */
	private SubjectSearchCriteria buildSubjectSearchCriteria(
			PatientSearchCriteria patientSearchCriteria) {
		SubjectSearchCriteria subjectSearchCriteria = new SubjectSearchCriteria();
		Subject subject = new Subject();

		// TODO: Deal with HRN and SSN(last4).
		SubjectName subjectName = new SubjectName();

		// Name:
		subjectName.setGivenName(patientSearchCriteria.getGivenName());
		subjectName.setFamilyName(patientSearchCriteria.getFamilyName());
		subject.addSubjectName(subjectName);

		// DOB:
		subject.setBirthTime(patientSearchCriteria.getDateOfBirth());

		// Gender:
		String genderCode = patientSearchCriteria.getGenderCode();
		if (genderCode.equals("UN")) {
			// Do not send.
			genderCode = null;
		}
		if (genderCode != null) {
			SubjectGender gender = new SubjectGender();
			gender.setCode(genderCode);
			subject.setGender(gender);
		}

		subjectSearchCriteria.setSubject(subject);
		return subjectSearchCriteria;
	}

	/**
	 * 
	 * @param subjectSearchCriteria
	 * @return
	 */
	private SubjectSearchResponse findCandidatesQuery(
			SubjectSearchCriteria subjectSearchCriteria) {
		SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
		XConfigActor pdsConfig = this.getPDSConfig();
		PDSClient pdsClient = new PDSClient(pdsConfig);
		DeviceInfo senderDeviceInfo = new DeviceInfo();
		// FIXME: Pull from xconfig.
		senderDeviceInfo.setId("TBD");
		senderDeviceInfo.setName("TBD");
		DeviceInfo receiverDeviceInfo = new DeviceInfo();
		receiverDeviceInfo.setId("TBD");
		receiverDeviceInfo.setName("TBD");

		try {
			subjectSearchResponse = pdsClient
					.findCandidatesQuery(senderDeviceInfo, receiverDeviceInfo,
							subjectSearchCriteria);
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	private List<Patient> buildPatients(
			SubjectSearchResponse subjectSearchResponse) {
		List<Patient> patients = new ArrayList<Patient>();

		System.out.println("# of subjects: "
				+ subjectSearchResponse.getSubjects().size());
		for (Subject subject : subjectSearchResponse.getSubjects()) {
			System.out.println("Subject found!!!!");
			Patient patient = this.buildPatient(subject);
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
	private Patient buildPatient(Subject subject) {
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
		Patient patient = new Patient();

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
		}
		// Gender.
		SubjectGender gender = subject.getGender();
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
		if (ssnSubjectIdentifier != null)
		{
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
		return this.getSubjectForIdentifierDomain(subject, SSN_IDENTIFIER_DOMAIN);
	}
	
	/**
	 * 
	 * @param identifier
	 * @return
	 */
	private String formatSSN(SubjectIdentifier identifier)
	{
		String id = identifier.getIdentifier();
		String formattedSSN = "N/A";
		int len = id.length();
		if (len >= 4)
		{
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

}
