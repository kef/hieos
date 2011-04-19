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
import com.vangent.hieos.DocViewer.server.services.rpc.authentication.AuthenticationContextTransform;
import com.vangent.hieos.authutil.framework.AuthenticationService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AuthenticationRemoteServiceImpl extends RemoteServiceServlet
		implements AuthenticationRemoteService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7923244304825432784L;

	@Override
	public AuthenticationContext authenticateUser(Credentials guiCreds) {
		System.out.println("SERVER SIDE - ABOUT TO CALL =======");
		com.vangent.hieos.authutil.model.AuthenticationContext authCtxt = null;
		AuthenticationService authService = new AuthenticationService();
		com.vangent.hieos.authutil.model.Credentials authCredentials = new com.vangent.hieos.authutil.model.Credentials(
				guiCreds.getUserId(), guiCreds.getPassword());
		authCtxt = authService.authenticateUser(authCredentials);
		System.out.println("SERVER SIDE =========" + authCtxt);

		return getAuthenticationContext(authCtxt);
	}

	private AuthenticationContext getAuthenticationContext(
			com.vangent.hieos.authutil.model.AuthenticationContext authCtxt) {
		return AuthenticationContextTransform.doWork(authCtxt);
	}
}
