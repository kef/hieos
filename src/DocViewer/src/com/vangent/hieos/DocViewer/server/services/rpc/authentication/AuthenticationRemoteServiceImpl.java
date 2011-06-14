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

import com.vangent.hieos.DocViewer.client.model.authentication.Credentials;
import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContext;
import com.vangent.hieos.DocViewer.client.services.rpc.AuthenticationRemoteService;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.DocViewer.server.services.rpc.authentication.AuthenticationContextTransform;
import com.vangent.hieos.authutil.framework.AuthenticationService;
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
	/**
	 * @param guiCreds
	 * @return AuthenticationContext
	 */
	public AuthenticationContext authenticateUser(Credentials guiCreds) {
		// Get the mixin to allow access to xconfig.xml.
		servletUtil.init(this.getServletContext());

		com.vangent.hieos.authutil.model.AuthenticationContext authCtxt = null;
		AuthenticationService authService = new AuthenticationService(servletUtil.getConfig());
		com.vangent.hieos.authutil.model.Credentials authCredentials = new com.vangent.hieos.authutil.model.Credentials(
				guiCreds.getUserId(), guiCreds.getPassword());
		authCtxt = authService.authenticate(authCredentials);
		return getAuthenticationContext(authCtxt);
	}

	/**
	 * 
	 * @param authCtxt
	 * @return
	 */
	private AuthenticationContext getAuthenticationContext(
			com.vangent.hieos.authutil.model.AuthenticationContext authCtxt) {
		AuthenticationContextTransform authContextTransform = new AuthenticationContextTransform(
				authCtxt, this.servletUtil);
		return authContextTransform.doWork();
	}
}