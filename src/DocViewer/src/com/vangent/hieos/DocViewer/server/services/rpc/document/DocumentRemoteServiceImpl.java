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
package com.vangent.hieos.DocViewer.server.services.rpc.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axiom.om.OMElement;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.vangent.hieos.DocViewer.client.exception.RemoteServiceException;
import com.vangent.hieos.DocViewer.client.model.config.ConfigDTO;
import com.vangent.hieos.DocViewer.client.model.document.DocumentAuthorMetadataDTO;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadataDTO;
import com.vangent.hieos.DocViewer.client.model.document.DocumentSearchCriteriaDTO;
import com.vangent.hieos.DocViewer.client.model.patient.PatientDTO;

import com.vangent.hieos.DocViewer.client.model.patient.PatientUtil;
import com.vangent.hieos.DocViewer.client.services.rpc.DocumentRemoteService;
import com.vangent.hieos.DocViewer.server.atna.ATNAAuditService;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.DocViewer.server.gateway.InitiatingGateway;
import com.vangent.hieos.DocViewer.server.gateway.InitiatingGatewayFactory;
import com.vangent.hieos.DocViewer.server.xua.XUAService;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;

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
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.template.TemplateUtil;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

import com.vangent.hieos.xutil.xua.utils.XUAObject;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class DocumentRemoteServiceImpl extends RemoteServiceServlet implements
		DocumentRemoteService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3195773598502538894L;
	private final ServletUtilMixin servletUtil = new ServletUtilMixin();

	static final String PROP_ADHOCQUERY_SINGLEPID_TEMPLATE = "AdhocQuerySinglePIDTemplate";
	static final String PROP_CONTENT_URL = "ContentURL";

	/**
	 * 
	 */
	@Override
	public void init() {
		// Initialize servlet.
		servletUtil.init(this.getServletContext());
	}

	/**
	 * 
	 */
	@Override
	public List<DocumentMetadataDTO> findDocuments(
			DocumentSearchCriteriaDTO criteria) throws RemoteServiceException {

		// See if we have a valid session ...
		HttpServletRequest request = this.getThreadLocalRequest();
		boolean validSession = ServletUtilMixin.isValidSession(request);
		if (!validSession) {
			throw new RemoteServiceException("Invalid Session!");
		}

		// Get authentication context from session.
		HttpSession session = request.getSession(false);
		AuthenticationContext authCtxt = (AuthenticationContext) session
				.getAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_CONTEXT);
		Credentials authCreds = (Credentials) session
				.getAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_CREDS);

		ServletContext servletContext = this.getServletContext();

		// First build the query message (from a template).
		System.out.println("DocViewer::findDocuments - target PID = "
				+ criteria.getPatient().getPatientID());
		OMElement query = this.getAdhocQuerySinglePID(servletContext,
				criteria.getPatient());
		List<DocumentMetadataDTO> documentMetadataList = new ArrayList<DocumentMetadataDTO>();
		try {
			if (query != null) {
				// Get the proper initiating gateway configuration.
				String searchMode = criteria.getSearchMode();
				InitiatingGateway ig = InitiatingGatewayFactory
						.getInitiatingGateway(searchMode, servletUtil);
				 
				//Prepare XCPD call only when the user has selected the "NHIN exchange" radio button 
				if(searchMode.equals(ConfigDTO.VAL_SEARCH_MODE_NHIN_EXCHANGE)){
					prepareXCPDCall(criteria, authCtxt, authCreds, ig);
				}

				// Issue Document Query ...
				System.out.println("Doc Query ...");

				// FIXME: Move this code.
				InitiatingGateway.TransactionType txnType = InitiatingGateway.TransactionType.DOC_QUERY;
				if (XUAService.isXUAEnabled(ig, txnType)) {
					XUAService xuaService = new XUAService(servletUtil,
							authCreds, authCtxt);
					XUAObject xuaObj = xuaService.getXUAObject(ig, txnType);
					OMElement samlClaimsNode = xuaService
							.getSAMLClaims(criteria.getPatient().getPatientID());
					// System.out.println("SAML Claims: " +
					// samlClaimsNode.toString());
					xuaObj.setClaims(samlClaimsNode);
					ig.setXuaObject(xuaObj);
				}

				// ATNA Audit.
				this.audit(authCreds, authCtxt, ig, query,
						ATNAAuditEvent.OutcomeIndicator.SUCCESS);

				OMElement response = ig.soapCall(
						InitiatingGateway.TransactionType.DOC_QUERY, query,
						authCtxt, criteria.getPatient().getPatientID());
				if (response != null) // TBD: Need to check for errors!!!!
				{

					// Convert the response into value objects.
					this.loadDocumentMetadataList(documentMetadataList,
							response);
				}
			}

		} catch (SOAPFaultException ex) {
			ex.printStackTrace();
			throw new RemoteServiceException(
					"Unable to contact document query service - "
							+ ex.getMessage());
		} catch (XdsException ex) {
			ex.printStackTrace();
			throw new RemoteServiceException(
					"Exception while contacting document query service - "
							+ ex.getMessage());
		}
		System.out.println("Returning ...");
		return documentMetadataList;
	}

	/**
	 * 
	 * @param m
	 * @param extrinsicObject
	 * @return
	 * @throws SOAPFaultException
	 */
	private void prepareXCPDCall(DocumentSearchCriteriaDTO criteria,
			AuthenticationContext authCtxt, Credentials authCreds,
			InitiatingGateway ig) throws SOAPFaultException {

		String patientId = criteria.getPatient().getPatientID();

		//InitiatingGateway.TransactionType txnType = InitiatingGateway.TransactionType.XCPD_QUERY;
		/**
		if (XUAService.isXUAEnabled(ig, txnType)) {
			XUAService xuaService = new XUAService(servletUtil, authCreds,
					authCtxt);
			XUAObject xuaObj = xuaService.getXUAObject(ig, txnType);
			OMElement samlClaimsNode = xuaService.getSAMLClaims(criteria
					.getPatient().getPatientID());
			// System.out.println("SAML Claims: " +
			// samlClaimsNode.toString());
			xuaObj.setClaims(samlClaimsNode);
			ig.setXuaObject(xuaObj);
		}
		 */
		// Get both sender & Receiver device info
		//XConfigActor pdsConfig = this.getPDSConfig();
		XConfigActor igConfig = ig.getIGConfig();
		//PDSClient pdsClient = new PDSClient(pdsConfig);
		DeviceInfo senderDeviceInfo = new DeviceInfo();
		senderDeviceInfo.setId(servletUtil.getProperty("DeviceId"));
		senderDeviceInfo.setName(servletUtil.getProperty("DeviceName"));
		DeviceInfo receiverDeviceInfo = new DeviceInfo();
		receiverDeviceInfo.setId(igConfig.getProperty("DeviceId"));
		receiverDeviceInfo.setName(igConfig.getProperty("DeviceName"));

		// Prepare Subject Search Criteria
		SubjectSearchCriteria subjectSearchCriteria = this
				.buildSubjectSearchCriteria(criteria.getPatient());

		// Create SubjectIdentifierDomain object and add it to SubjectSearchCriteria
		SubjectIdentifierDomain subjectIdentifierDomain = new SubjectIdentifierDomain(
				patientId);
		subjectSearchCriteria
				.setCommunitySubjectIdentifierDomain(subjectIdentifierDomain);

		PRPA_IN201305UV02_Message_Builder pdqQueryBuilder = new PRPA_IN201305UV02_Message_Builder(
				senderDeviceInfo, receiverDeviceInfo);

		PRPA_IN201305UV02_Message request = pdqQueryBuilder
				.buildPRPA_IN201305UV02_Message(subjectSearchCriteria);
		
		//System.out.println("*********************** before making a call XCPD request ********************");
		//System.out.println(request.getElement().toString());
		//System.out.println("*********************************************************");


		// Make SOAP call for XCPD
		try {
			OMElement response = ig.soapCall(
					InitiatingGateway.TransactionType.XCPD_QUERY, request
							.getMessageNode(), authCtxt, criteria.getPatient()
							.getPatientID());

			if (response != null) {
				SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
				SubjectBuilder subjectBuilder = new SubjectBuilder();
				subjectSearchResponse = subjectBuilder
						.buildSubjectSearchResponse(new PRPA_IN201306UV02_Message(
								response));
				//System.out.println("*********************** Response from XCPD call ********************");
				//System.out.println(response.toString());
				//System.out.println("*********************************************************");

				// ATNA Audit.
				this.auditXCPD(authCreds, authCtxt,request, ig,
						subjectSearchResponse,
						ATNAAuditEvent.OutcomeIndicator.SUCCESS);
			}

		} catch (ModelBuilderException ex) {
			throw new SOAPFaultException(ex.getMessage());
		}

	}

	/**
	 * 
	 * @param documentMetadataList
	 * @param response
	 * @throws MetadataException
	 * @throws MetadataValidationException
	 */
	private void loadDocumentMetadataList(
			List<DocumentMetadataDTO> documentMetadataList, OMElement response)
			throws MetadataException, MetadataValidationException {

		// Parse the SOAP response to get Metadata instance.
		Metadata m = MetadataParser.parseNonSubmission(response);

		// Loop through all ExtrinsicObjects (Documents) and do conversion to
		// value objects.
		List<OMElement> extrinsicObjects = m.getExtrinsicObjects();
		System.out.println("# of documents: " + extrinsicObjects.size());
		for (OMElement extrinsicObject : extrinsicObjects) {
			System.out.println("Document found!!!!");
			DocumentMetadataDTO documentMetadata = this.buildDocumentMetadata(
					m, extrinsicObject);
			documentMetadataList.add(documentMetadata);
		}
	}

	/**
	 * 
	 * @param m
	 * @param extrinsicObject
	 * @return
	 * @throws MetadataException
	 */
	private DocumentMetadataDTO buildDocumentMetadata(Metadata m,
			OMElement extrinsicObject) {

		// Create the DocumentMetadata instance.
		DocumentMetadataDTO documentMetadata = new DocumentMetadataDTO();

		// Document id.
		String documentID;
		try {
			documentID = m.getExternalIdentifierValue(m.getId(extrinsicObject),
					MetadataSupport.XDSDocumentEntry_uniqueid_uuid);
		} catch (MetadataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			documentID = "UNKNOWN";
		}
		documentMetadata.setDocumentID(documentID);

		// Patient id.
		String patientID;
		try {
			patientID = m.getExternalIdentifierValue(m.getId(extrinsicObject),
					MetadataSupport.XDSDocumentEntry_patientid_uuid);
		} catch (MetadataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			patientID = "UNKNOWN";
		}
		documentMetadata.setPatientID(patientID);
		documentMetadata.setEuid(PatientUtil.getIDFromPIDString(patientID));
		documentMetadata.setAssigningAuthority(PatientUtil
				.getAssigningAuthorityFromPIDString(patientID));

		// Repository id.
		String repositoryID = m.getSlotValue(extrinsicObject,
				"repositoryUniqueId", 0);
		documentMetadata.setRepositoryID(repositoryID);

		// Home Community id.
		String homeCommunityID = m.getHome(extrinsicObject);
		documentMetadata.setHomeCommunityID(homeCommunityID);

		// Creation time.
		String creationTime = m
				.getSlotValue(extrinsicObject, "creationTime", 0);
		documentMetadata.setCreationTime(Hl7Date.toDate(creationTime));

		// Name (Title?).
		String name = m.getNameValue(extrinsicObject);
		documentMetadata.setTitle(name);

		// Mime Type.
		String mimeType = extrinsicObject
				.getAttributeValue(MetadataSupport.mime_type_qname);
		documentMetadata.setMimeType(mimeType);

		// Size.
		String sizeString = m.getSlotValue(extrinsicObject, "size", 0);
		int size = -1;
		if (sizeString != null) {
			size = new Integer(sizeString);
		}
		documentMetadata.setSize(size);

		// Authors.
		List<DocumentAuthorMetadataDTO> authors = this.getAuthors(m,
				extrinsicObject);
		documentMetadata.setAuthors(authors);

		// Class Code, Format Code, Type Code.
		documentMetadata.setClassCode(this.getClassCode(m, extrinsicObject));
		documentMetadata.setFormatCode(this.getFormatCode(m, extrinsicObject));
		documentMetadata.setTypeCode(this.getTypeCode(m, extrinsicObject));

		// To allow retrieval by client.
		documentMetadata.setContentURL(servletUtil
				.getProperty(PROP_CONTENT_URL));

		return documentMetadata;
	}

	/**
	 * 
	 * @param m
	 * @param extrinsicObject
	 * @return
	 */
	private List<DocumentAuthorMetadataDTO> getAuthors(Metadata m,
			OMElement extrinsicObject) {
		List<DocumentAuthorMetadataDTO> documentAuthors = new ArrayList<DocumentAuthorMetadataDTO>();
		try {
			List<OMElement> authorNodes = m.getClassifications(extrinsicObject,
					MetadataSupport.XDSDocumentEntry_author_uuid);
			for (OMElement authorNode : authorNodes) {
				String authorPerson = m.getSlotValue(authorNode,
						"authorPerson", 0);

				// FIXME: Just get first 1 (for now) .. can be multiple.
				String authorInstitution = m.getSlotValue(authorNode,
						"authorInstitution", 0);

				// Now create and load the DocumentAuthorMetadata instance.
				DocumentAuthorMetadataDTO authorMetadata = new DocumentAuthorMetadataDTO();
				authorMetadata.setName(authorPerson);
				authorMetadata.setInstitution(authorInstitution);
				documentAuthors.add(authorMetadata);
			}
		} catch (MetadataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return documentAuthors;
	}

	/**
	 * 
	 * @param m
	 * @param extrinsicObject
	 * @return
	 */
	private String getClassCode(Metadata m, OMElement extrinsicObject) {
		return this.getCodeDisplayName(m, extrinsicObject,
				MetadataSupport.XDSDocumentEntry_classCode_uuid);
	}

	/**
	 * 
	 * @param m
	 * @param extrinsicObject
	 * @return
	 */
	private String getFormatCode(Metadata m, OMElement extrinsicObject) {
		return this.getCodeDisplayName(m, extrinsicObject,
				MetadataSupport.XDSDocumentEntry_formatCode_uuid);
	}

	/**
	 * 
	 * @param m
	 * @param extrinsicObject
	 * @return
	 */
	private String getTypeCode(Metadata m, OMElement extrinsicObject) {
		// FIXME: Add classification to MetadataSupport.
		return this.getCodeDisplayName(m, extrinsicObject,
				"urn:uuid:f0306f51-975f-434e-a61c-c59651d33983");
	}

	/**
	 * 
	 * @param m
	 * @param extrinsicObject
	 * @param classificationScheme
	 * @return
	 */
	private String getCodeDisplayName(Metadata m, OMElement extrinsicObject,
			String classificationScheme) {
		String codeDisplayName = "UNKNOWN";
		try {
			List<OMElement> codeNodes = m.getClassifications(extrinsicObject,
					classificationScheme);
			if (codeNodes != null && codeNodes.size() > 0) {
				// FIXME: ? Just take first one ? Likely ok.
				OMElement codeNode = codeNodes.get(0);
				codeDisplayName = this.getCodeDisplayName(m, codeNode);
			}
		} catch (MetadataException e) {
			// Just ignore ...
		}
		return codeDisplayName;
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	private String getCodeDisplayName(Metadata m, OMElement node) {
		String codeDisplayName = m.getNameValue(node);
		if (codeDisplayName == null) {
			codeDisplayName = "UNKNOWN";
		}
		return codeDisplayName;
		/*
		 * OMElement nameNode = node.getFirstChildWithName(new QName("Name"));
		 * if (nameNode != null) { OMElement localizedStringNode = nameNode
		 * .getFirstChildWithName(new QName("LocalizedString")); if
		 * (localizedStringNode != null) { codeDisplayName = localizedStringNode
		 * .getAttributeValue(new QName("value")); } } return codeDisplayName;
		 */
	}

	/**
	 * 
	 * @param servletContext
	 * @param patient
	 * @return
	 */
	public OMElement getAdhocQuerySinglePID(ServletContext servletContext,
			PatientDTO patient) {
		String template = servletUtil
				.getTemplateString(servletUtil
						.getProperty(DocumentRemoteServiceImpl.PROP_ADHOCQUERY_SINGLEPID_TEMPLATE));
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put("PID", patient.getPatientID());
		return TemplateUtil.getOMElementFromTemplate(template, replacements);
	}

	

	/**
	 * 
	 * @param authCreds
	 * @param authCtxt
	 * @param ig
	 * @param queryRequest
	 * @param outcome
	 */
	private void auditXCPD(Credentials authCreds,
			AuthenticationContext authCtxt, 
			PRPA_IN201305UV02_Message request,InitiatingGateway ig,
			SubjectSearchResponse subjectSearchResponse,
			OutcomeIndicator outcome) {

		if (ATNAAuditService.isPerformAudit()) {
			ATNAAuditService auditService = new ATNAAuditService(authCreds,
					authCtxt);
			//XConfigActor pdsConfig = pdsClient.getConfig();
			//XConfigTransaction txn = pdsConfig
				//	.getTransaction("CrossGatewayPatientDiscoveryQuery");
			String targetEndpoint =ig.getTransactionEndpointURL(InitiatingGateway.TransactionType.XCPD_QUERY); //txn.getEndpointURL();
			String homeCommunityId = ig.getIGConfig().getUniqueId();//  this.getXCPDConfig().getUniqueId(); 

			auditService.auditPatientDemographicsQuery(request,
					subjectSearchResponse, homeCommunityId, targetEndpoint,
					ATNAAuditEvent.IHETransaction.ITI55, outcome);
		}
	}

	/**
	 * 
	 * @param authCreds
	 * @param authCtxt
	 * @param ig
	 * @param queryRequest
	 * @param outcome
	 */
	private void audit(Credentials authCreds, AuthenticationContext authCtxt,
			InitiatingGateway ig, OMElement queryRequest,
			OutcomeIndicator outcome) {

		if (ATNAAuditService.isPerformAudit()) {
			ATNAAuditService auditService = new ATNAAuditService(authCreds,
					authCtxt);
			String targetEndpoint = ig
					.getTransactionEndpointURL(InitiatingGateway.TransactionType.DOC_QUERY);
			String homeCommunityId = ig.getIGConfig().getUniqueId();
			auditService.auditRegistryStoredQuery(queryRequest,
					homeCommunityId, targetEndpoint, outcome);
		}
	}

	/**
	 * 
	 * @return
	
	private XConfigActor getPDSConfig() {
		return servletUtil.getActorConfig("xcpd", "XCPDType");
	}
 */
	/**
	 * 
	 * @param patientSearchCriteria
	 * @return SubjectSearchCriteria
	 */
	private SubjectSearchCriteria buildSubjectSearchCriteria(
			PatientDTO patientSearchCriteria) {
		SubjectSearchCriteria subjectSearchCriteria = new SubjectSearchCriteria();
		Subject subject = new Subject();

		SubjectName subjectName = new SubjectName();
		// Name:
		subjectName.setGivenName(patientSearchCriteria.getGivenName());
		subjectName.setFamilyName(patientSearchCriteria.getFamilyName());
		subject.addSubjectName(subjectName);

		// DOB:
		subject.setBirthTime(patientSearchCriteria.getDateOfBirth());
		
		//Living Subject Id
		System.out.println("patientSearchCriteria.getEuidUniversalID()     :  "+patientSearchCriteria.getEuidUniversalID());
		System.out.println("patientSearchCriteria.getEuid()     :  "+patientSearchCriteria.getEuid());
		 List<SubjectIdentifier> subjectIdentifiers= new ArrayList<SubjectIdentifier>();
		 //FIXME Need to fix how subject identifier takes care when it received patient id. 
		 //currently it doesn't take care of "amp;" token in the patient id
		 //subjectIdentifiers.add(new SubjectIdentifier(patientSearchCriteria.getPatientID()));
		 subjectIdentifiers.add(new SubjectIdentifier(patientSearchCriteria.getEuid()+"^^^&"+patientSearchCriteria.getEuidUniversalID()+"&ISO"));
		// subjectIdentifiers.add(new SubjectIdentifier(patientSearchCriteria.getEuidUniversalID()));		 
		subject.setSubjectIdentifiers(subjectIdentifiers);

		/**
		 * Verify if we need this or not ?????? // SSN(last4) String ssnValue =
		 * patientSearchCriteria.getSSN(); if (StringUtils.isNotBlank(ssnValue))
		 * {
		 * 
		 * SubjectIdentifierDomain ssnDomain = new SubjectIdentifierDomain();
		 * ssnDomain.setUniversalId(SSN_IDENTIFIER_DOMAIN);
		 * ssnDomain.setUniversalIdType("ISO");
		 * 
		 * SubjectIdentifier ssnId = new SubjectIdentifier();
		 * ssnId.setIdentifierDomain(ssnDomain); ssnId.setIdentifier(ssnValue);
		 * 
		 * subject.addSubjectIdentifier(ssnId); }
		 */

		// Gender:
		String genderCode = patientSearchCriteria.getGender();
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
	private XConfigActor getXCPDConfig() {
		return this.servletUtil.getActorConfig("xcpd",
				"XCPDType");
	}
	**/
}
