/*
 *
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
package com.vangent.hieos.DocViewer.server.services.rpc.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.vangent.hieos.DocViewer.client.exception.RemoteServiceException;
import com.vangent.hieos.DocViewer.client.model.authentication.CredentialsDTO;
import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContextDTO;
import com.vangent.hieos.DocViewer.client.services.rpc.AuthenticationRemoteService;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.DocViewer.server.services.rpc.authentication.AuthenticationContextTransform;
import com.vangent.hieos.authutil.framework.AuthenticationService;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * 
 * @author Anand Sastry
 * 
 */
public class AuthenticationRemoteServiceImpl extends RemoteServiceServlet
		implements AuthenticationRemoteService {
	private ServletUtilMixin servletUtil = new ServletUtilMixin();

	/**
	 *
	 */
	private static final long serialVersionUID = -8437219381457225576L;

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

	/**
	 * 
	 */
	@Override
	public AuthenticationContextDTO login(CredentialsDTO credentialsDTO) throws RemoteServiceException {
		// Get the mixin to allow access to xconfig.xml.
		//servletUtil.init(this.getServletContext());

		AuthenticationContext authCtxt = null;
		AuthenticationService authService = new AuthenticationService(servletUtil.getConfig());
		Credentials authCredentials = new com.vangent.hieos.authutil.model.Credentials(
				credentialsDTO.getUserId(), credentialsDTO.getPassword(),
				credentialsDTO.getAuthDomainTypeKey());
		authCtxt = authService.authenticate(authCredentials);

		// Create session w/ login status.
		HttpServletRequest request = this.getThreadLocalRequest();
		HttpSession session = request.getSession();
		if (authCtxt.hasSuccessStatus()) {
			session.setAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_STATUS, "true");
			session.setAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_CREDS, authCredentials);
			session.setAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_CONTEXT, authCtxt);
		} else {
			session.setAttribute(ServletUtilMixin.SESSION_PROPERTY_AUTH_STATUS, "false");
		}

		// Return authentication context to client.
		AuthenticationContextDTO authCtxtDTO = this.getAuthenticationContext(authCtxt);
		// Echo back credentials used.
		//guiAuthCtxt.setCredentials(guiCreds);

		return authCtxtDTO;
	}
	

	/**
	 * 
	 */
	@Override
	public void logout()
			throws RemoteServiceException {
		HttpServletRequest request = this.getThreadLocalRequest();
		ServletUtilMixin.invalidateSession(request);
	}
	
	

	/**
	 * 
	 * @param authCtxt
	 * @return
	 */
	private AuthenticationContextDTO getAuthenticationContext(AuthenticationContext authCtxt) {
		AuthenticationContextTransform authContextTransform = new AuthenticationContextTransform(
				authCtxt, this.servletUtil);
		return authContextTransform.doWork();
	}

}
