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
package com.vangent.hieos.DocViewer.client.services.rpc;

import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContext;
import com.vangent.hieos.DocViewer.client.model.authentication.Credentials;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthenticationRemoteServiceAsync {

	/**
	 * 
	 * @param creds
	 * @param callback
	 */
	public void login(Credentials creds, AsyncCallback<AuthenticationContext> callback);

	/**
	 * 
	 * @param authCtxt
	 * @param callback
	 */
	public void logout(AsyncCallback<Void> callback);

}

	