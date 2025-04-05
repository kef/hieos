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
package com.vangent.hieos.DocViewer.server.servlets.docview;

import com.vangent.hieos.DocViewer.server.atna.ATNAAuditService;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.DocViewer.server.gateway.InitiatingGateway;
import com.vangent.hieos.DocViewer.server.gateway.InitiatingGatewayFactory;
import com.vangent.hieos.DocViewer.server.xua.XUAService;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.OutcomeIndicator;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.soap.Mtom;
import com.vangent.hieos.xutil.template.TemplateUtil;
import com.vangent.hieos.xutil.xml.XPathHelper;
import com.vangent.hieos.xutil.xua.utils.XUAObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.apache.axiom.om.OMElement;

/**
 * 
 * @author Bernie Thuman
 */
public class DocViewServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3787115682508341906L;

	// private static Properties _properties = null;
	private final ServletUtilMixin servletUtil = new ServletUtilMixin();

	static final String PROP_RETRIEVE_SINGLE_DOC_TEMPLATE = "RetrieveSingleDocTemplate";

	@Override
	public void init() {
		servletUtil.init(this.getServletContext());
	}

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// ServletContext servletContext = this.getServletContext();
		// System.out.println("Context Path" + servletContext.getContextPath());

		// See if we have a valid session ...
		boolean validSession = ServletUtilMixin.isValidSession(request);
		if (!validSession) {
			this.writeErrorToOutput(response, "Invalid Session!");
		} else {
			// Continue.
			this.getDocument(request, response);
		}
	}

	// <editor-fold defaultstate="collapsed"
	// desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 * 
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "docview";
	}// </editor-fold>

	/**
	 * 
	 * @param request
	 * @param response
	 */
	private void getDocument(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		try {
			// Get authentication context from session.
			HttpSession session = servletRequest.getSession(false);
			AuthenticationContext authCtxt = (AuthenticationContext) session
					.getAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_CONTEXT);
			Credentials authCreds = (Credentials) session
					.getAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_CREDS);

			ServletContext servletContext = this.getServletContext();
			OMElement retrieve = this.getRetrieveSingleDocument(servletRequest,
					servletContext);

			// Get the proper initiating gateway configuration.
			String searchMode = servletRequest.getParameter("search_mode");
			String patientID = servletRequest.getParameter("patient_id");
			patientID = this.getFormattedPatientID(patientID);

			InitiatingGateway ig = InitiatingGatewayFactory
					.getInitiatingGateway(searchMode, servletUtil);
			
			// FIXME: Move this code.
			InitiatingGateway.TransactionType txnType = InitiatingGateway.TransactionType.DOC_RETRIEVE;
			if (XUAService.isXUAEnabled(ig, txnType)) {
				XUAService xuaService = new XUAService(servletUtil, authCreds, authCtxt);
				XUAObject xuaObj = xuaService.getXUAObject(ig, txnType);
				OMElement samlClaimsNode = xuaService.getSAMLClaims(patientID);
				 System.out.println("SAML Claims: " +
				 samlClaimsNode.toString());
				xuaObj.setClaims(samlClaimsNode);
				ig.setXuaObject(xuaObj);
			}

			// ATNA Audit.
			this.audit(authCreds, authCtxt, ig, retrieve,
					ATNAAuditEvent.OutcomeIndicator.SUCCESS);

			// Issue Document Retrieve ...
			System.out.println("Doc Retrieve ...");
			OMElement response = ig.soapCall(
					InitiatingGateway.TransactionType.DOC_RETRIEVE, retrieve,authCtxt,patientID);
			if (response != null) // FIXME: Need to check for registry
									// errors!!!!
			{
				OMElement documentNode = this.getDocument(response);
				String mimeType = this.getDocumentMimeType(response);
				if (documentNode != null) {
					// FIXME: Should really use coded value type (formatCode?)
					// from registry
					// passed in as a parameter to the servlet.
					this.writeDocumentToOutput(servletRequest, servletResponse,
							documentNode, mimeType);
				} else {
					this.writeErrorToOutput(servletResponse,
							"No XDS.b Document Found!");
				}
			} else {
				this.writeErrorToOutput(servletResponse,
						"No XDS.b Document Found (SOAP response is null)!");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			this.writeExceptionToOutput(servletResponse, ex);
		}
	}
	
	/**
	 * 
	 * @param patientID
	 * @return
	 */
	private String getFormattedPatientID(String patientID)
	{
		//20130303112338^^^&amp;1.3.6.1.4.1.21367.13.20.3000&amp;ISO
		return patientID.replace("&", "&amp;");
	}

	/**
	 * 
	 * @param response
	 * @return
	 * @throws XPathHelperException
	 */
	private OMElement getDocument(OMElement response)
			throws XPathHelperException {
		OMElement documentNode = XPathHelper.selectSingleNode(response,
				"//*/ns:DocumentResponse/ns:Document[1]",
				"urn:ihe:iti:xds-b:2007");
		return documentNode;
	}

	/**
	 * 
	 * @param response
	 * @return
	 * @throws XPathHelperException
	 */
	private String getDocumentMimeType(OMElement response)
			throws XPathHelperException {
		OMElement mimeTypeNode = XPathHelper.selectSingleNode(response,
				"//*/ns:DocumentResponse/ns:mimeType[1]",
				"urn:ihe:iti:xds-b:2007");
		return mimeTypeNode.getText();
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param documentNode
	 * @param mimeType
	 */
	private void writeDocumentToOutput(HttpServletRequest request,
			HttpServletResponse response, OMElement documentNode,
			String mimeType) {
		Mtom mtom = new Mtom();
		try {
			mtom.decode(documentNode);
		} catch (Exception ex) {
			this.writeExceptionToOutput(response, ex);
			return;
		}
		// String contentType = mtom.getContent_type();
		System.out.println("mimeType = " + mimeType);
		if (mimeType.equals("text/xml")) {
			String document = this.getHMTL(request, this.getServletContext(),
					mtom.getContents());
			this.writeContentToOutput(response, document, "text/html");
		} else if (mimeType.equals("application/pdf")) {
			byte[] document = mtom.getContents();
			this.writeContentToOutput(response, document, mimeType);
		} else if (mimeType.equals("text/plain")) {
			String document = new String(mtom.getContents());
			// Wrapper in HTML.
			this.writeContentToOutput(response, "<html><body>" + document
					+ "</body></html>", "text/html");
			// response.setContentType("text/html;charset=UTF-8");
		} else {
			// FIXME: defaulting now to given mime type (and as string):
			String document = new String(mtom.getContents());
			this.writeContentToOutput(response, document, mimeType);
			// response.setContentType("text/html;charset=UTF-8");
		}
	}

	/**
	 * 
	 * @param response
	 * @param content
	 * @param contentType
	 */
	private void writeContentToOutput(HttpServletResponse response,
			byte[] content, String contentType) {
		ServletOutputStream out = null;
		try {
			response.setContentType(contentType);
			out = response.getOutputStream();
			out.write(content);
			// out.print(content);
		} catch (IOException ex) {
			// FIXME
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
					// FIXME
				}
			}
		}
	}

	/**
	 * 
	 * @param response
	 * @param content
	 * @param contentType
	 */
	private void writeContentToOutput(HttpServletResponse response,
			String content, String contentType) {
		PrintWriter out = null;
		try {
			response.setContentType(contentType);
			out = response.getWriter();
			out.print(content);
		} catch (IOException ex) {
			// FIXME
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * 
	 * @param response
	 * @param e
	 */
	private void writeExceptionToOutput(HttpServletResponse response,
			Exception e) {
		String error = "<html><body><h1>EXCEPTION: " + e.getMessage()
				+ "</h1></body></html>";
		this.writeContentToOutput(response, error, "text/html");
	}

	/**
	 * 
	 * @param response
	 * @param errorText
	 */
	private void writeErrorToOutput(HttpServletResponse response,
			String errorText) {
		String error = "<html><body><h1>ERROR: " + errorText
				+ "</h1></body></html>";
		this.writeContentToOutput(response, error, "text/html");
	}

	/**
	 * 
	 * @param request
	 * @param servletContext
	 * @return
	 */
	private OMElement getRetrieveSingleDocument(HttpServletRequest request,
			ServletContext servletContext) {
		String homeCommunityID = request.getParameter("hc_id");
		String repositoryID = request.getParameter("repo_id");
		String documentID = request.getParameter("doc_id");
		String template = servletUtil.getTemplateString(servletUtil
				.getProperty(DocViewServlet.PROP_RETRIEVE_SINGLE_DOC_TEMPLATE));
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put("HOME_COMMUNITY_ID", homeCommunityID);
		replacements.put("REPOSITORY_UNIQUE_ID", repositoryID);
		replacements.put("DOCUMENT_UNIQUE_ID", documentID);
		return TemplateUtil.getOMElementFromTemplate(template, replacements);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	private String getXMLTemplateFileName(HttpServletRequest request) {
		String xmlTemplateFileName = request.getParameter("template_filename");
		return xmlTemplateFileName;
	}

	/**
	 * 
	 * @param servletContext
	 * @param bytes
	 * @return
	 */
	private String getHMTL(HttpServletRequest request,
			ServletContext servletContext, byte[] bytes) {
		String xmlTemplateFileName = this.getXMLTemplateFileName(request);
		System.out.println("XML Template File Name = " + xmlTemplateFileName);
		ByteArrayInputStream xmlis = new ByteArrayInputStream(bytes);
		InputStream xslis = servletContext
				.getResourceAsStream("/resources/xsl/" + xmlTemplateFileName);
		return this.applyStyleSheetToXML(xmlis, xslis);
	}

	/**
	 * 
	 * @param xml
	 * @param xsl
	 * @return
	 */
	private String applyStyleSheetToXML(InputStream xml, InputStream xsl) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory
					.newTransformer(new javax.xml.transform.stream.StreamSource(
							xsl));
			transformer.transform(new javax.xml.transform.stream.StreamSource(
					xml), new javax.xml.transform.stream.StreamResult(output));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output.toString();
	}

	/**
	 * 
	 * @param authCreds
	 * @param authCtxt
	 * @param ig
	 * @param request
	 * @param outcome
	 */
	private void audit(Credentials authCreds,
			AuthenticationContext authCtxt,
			InitiatingGateway ig, OMElement request, OutcomeIndicator outcome) {
		
		if (ATNAAuditService.isPerformAudit()) {
			ATNAAuditService auditService = new ATNAAuditService(authCreds, authCtxt);
			String targetEndpoint = ig
					.getTransactionEndpointURL(InitiatingGateway.TransactionType.DOC_RETRIEVE);
			String homeCommunityId = ig.getIGConfig().getUniqueId();
			auditService.auditRetrieveDocumentSet(request, homeCommunityId,
					targetEndpoint, outcome);
		}
	}
}
