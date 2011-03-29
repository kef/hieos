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
//import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.vangent.hieos.DocViewer.client.model.document.DocumentAuthorMetadata;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadata;
import com.vangent.hieos.DocViewer.client.model.document.DocumentSearchCriteria;
import com.vangent.hieos.DocViewer.client.model.patient.Patient;
import com.vangent.hieos.DocViewer.client.model.patient.PatientUtil;
import com.vangent.hieos.DocViewer.client.services.rpc.DocumentRemoteService;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.DocViewer.server.gateway.InitiatingGateway;
import com.vangent.hieos.DocViewer.server.gateway.InitiatingGatewayFactory;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.template.TemplateUtil;
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
	private ServletUtilMixin servletUtil = new ServletUtilMixin();

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
	public List<DocumentMetadata> findDocuments(DocumentSearchCriteria criteria) {
		ServletContext servletContext = this.getServletContext();

		// First build the query message (from a template).
		OMElement query = this.getAdhocQuerySinglePID(servletContext,
				criteria.getPatient());
		List<DocumentMetadata> documentMetadataList = new ArrayList<DocumentMetadata>();
		try {
			if (query != null) {
				// Get the proper initiating gateway configuration.
				String searchMode = criteria.getSearchMode();
				InitiatingGateway ig = InitiatingGatewayFactory.getInitiatingGateway(searchMode, servletUtil);

				// Issue Document Retrieve ...
				System.out.println("Doc Query ...");

				OMElement response = ig.soapCall(
						InitiatingGateway.TransactionType.DOC_QUERY, query);
				if (response != null) // TBD: Need to check for errors!!!!
				{
					// Convert the response into value objects.
					this.loadDocumentMetadataList(documentMetadataList,
							response);
				}
			}
		} catch (XdsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Returning ...");
		return documentMetadataList;
	}

	/**
	 * 
	 * @param documentMetadataList
	 * @param response
	 * @throws MetadataException
	 * @throws MetadataValidationException
	 */
	private void loadDocumentMetadataList(
			List<DocumentMetadata> documentMetadataList, OMElement response)
			throws MetadataException, MetadataValidationException {

		// Parse the SOAP response to get Metadata instance.
		Metadata m = MetadataParser.parseNonSubmission(response);

		// Loop through all ExtrinsicObjects (Documents) and do conversion to
		// value objects.
		List<OMElement> extrinsicObjects = m.getExtrinsicObjects();
		System.out.println("# of documents: " + extrinsicObjects.size());
		for (OMElement extrinsicObject : extrinsicObjects) {
			System.out.println("Document found!!!!");
			DocumentMetadata documentMetadata = this.buildDocumentMetadata(m,
					extrinsicObject);
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
	private DocumentMetadata buildDocumentMetadata(Metadata m,
			OMElement extrinsicObject) {

		// Create the DocumentMetadata instance.
		DocumentMetadata documentMetadata = new DocumentMetadata();

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
		documentMetadata.setCreationTime(Hl7Date
				.getDateFromHL7Format(creationTime));

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
		List<DocumentAuthorMetadata> authors = this.getAuthors(m,
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
	private List<DocumentAuthorMetadata> getAuthors(Metadata m,
			OMElement extrinsicObject) {
		List<DocumentAuthorMetadata> documentAuthors = new ArrayList<DocumentAuthorMetadata>();
		try {
			ArrayList<OMElement> authorNodes = m.getClassifications(
					extrinsicObject,
					MetadataSupport.XDSDocumentEntry_author_uuid);
			for (OMElement authorNode : authorNodes) {
				String authorPerson = m.getSlotValue(authorNode,
						"authorPerson", 0);

				// FIXME: Just get first 1 (for now) .. can be multiple.
				String authorInstitution = m.getSlotValue(authorNode,
						"authorInstitution", 0);

				// Now create and load the DocumentAuthorMetadata instance.
				DocumentAuthorMetadata authorMetadata = new DocumentAuthorMetadata();
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
			ArrayList<OMElement> codeNodes = m.getClassifications(
					extrinsicObject, classificationScheme);
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
			Patient patient) {
		String template = servletUtil
				.getTemplateString(servletUtil
						.getProperty(DocumentRemoteServiceImpl.PROP_ADHOCQUERY_SINGLEPID_TEMPLATE));
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put("PID", patient.getPatientID());
		return TemplateUtil.getOMElementFromTemplate(template, replacements);
	}
}
