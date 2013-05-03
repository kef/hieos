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
package com.vangent.hieos.DocViewer.server.services.rpc.pip;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.vangent.hieos.DocViewer.client.exception.RemoteServiceException;
import com.vangent.hieos.DocViewer.client.model.patient.PatientConsentDirectivesDTO;
import com.vangent.hieos.DocViewer.client.model.patient.PatientConsentSearchCriteriaDTO;
import com.vangent.hieos.DocViewer.client.services.rpc.PIPRemoteService;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.pip.client.PIPClient;
import com.vangent.hieos.policyutil.pip.model.PIPRequest;
import com.vangent.hieos.policyutil.pip.model.PIPResponse;
import com.vangent.hieos.policyutil.pip.model.PatientConsentDirectives;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PIPRemoteServiceImpl extends RemoteServiceServlet implements
		PIPRemoteService {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3176690310560815852L;


	/**
	 * 
	 */
	
	private final ServletUtilMixin servletUtil = new ServletUtilMixin();

	@Override
	public void init() {
		servletUtil.init(this.getServletContext());
	}

	/**
	 * 
	 */
	@Override
	public PatientConsentDirectivesDTO getPatientConsentDirectives(PatientConsentSearchCriteriaDTO patientConsentSearchCriteria)
			throws RemoteServiceException {
		// See if we have a valid session ...
		HttpServletRequest request = this.getThreadLocalRequest();
		boolean validSession = ServletUtilMixin.isValidSession(request);
		if (!validSession) {
			throw new RemoteServiceException("Invalid Session!");
		}

		// TODO: Implement use of AuthenticationContext (XUA, etc.).
		// Get authentication context from session.
		HttpSession session = request.getSession(false);
		AuthenticationContext authCtxt = (AuthenticationContext) session
				.getAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_CONTEXT);
		Credentials authCreds = (Credentials) session
				.getAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_CREDS);
		
		// Get Patient ID from the search request.
		String patientID = patientConsentSearchCriteria.getPatientID();
		
		// Build Policy Information Point (PIP) request.
		PIPRequest pipRequest = new PIPRequest();
		SubjectIdentifier subjectIdentifier = new SubjectIdentifier(patientID);
		pipRequest.setPatientId(subjectIdentifier);
		try {
			// Create PIPClient and issue request to get patient consent directives.
			PIPClient pipClient = new PIPClient(this.getPIPConfig());
			PIPResponse pipResponse = pipClient.getPatientConsentDirectives(pipRequest, true /* buildDomainModel */);
			
			// Transform response.
			PatientConsentDirectives patientConsentDirectives = pipResponse.getPatientConsentDirectives();
			PatientConsentDirectivesDTO patientConsentDirectivesDTO = PatientConsentDirectivesTransform.transform(patientConsentDirectives);
			
			// Return response.
			return patientConsentDirectivesDTO;
		} catch (PolicyException e) {
			throw new RemoteServiceException("Exception: " + e.getMessage());
		}
	}


	/**
	 * 
	 * @return
	 */
	private XConfigActor getPIPConfig() {
		return servletUtil.getActorConfig("pip", "PolicyInformationPointType");
	}
	
}