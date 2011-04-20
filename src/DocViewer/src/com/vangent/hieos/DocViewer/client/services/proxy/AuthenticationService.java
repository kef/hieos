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
package com.vangent.hieos.DocViewer.client.services.proxy;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.vangent.hieos.DocViewer.client.helper.Observer;
import com.vangent.hieos.DocViewer.client.helper.TimeOutHelper;
import com.vangent.hieos.DocViewer.client.services.rpc.AuthenticationRemoteService;
import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContext;
import com.vangent.hieos.DocViewer.client.model.authentication.Credentials;

public class AuthenticationService extends ProxyService {

	private Credentials creds;
	
	
	/**
	 * 
	 * @param creds
	 * @param observer
	 * @param timeOutHelper
	 */
	public AuthenticationService(Credentials creds, Observer observer, TimeOutHelper timeOutHelper) {
		super(observer, timeOutHelper);
		this.creds = creds;
	}
	

	/**
	 * 
	 */
	public void doWork() {

		this.getTimeOutHelper().startTimer();
		// RPC:
		AuthenticationRemoteService.Util.getInstance().authenticateUser(creds,
				new AsyncCallback<AuthenticationContext>() {

					public void onSuccess(AuthenticationContext authCtx) {
						cancelTimer();
						if (getAbortFlag()) {
							// Timeout already occurred. discard result
							return;
						}
						update(authCtx);
					}

					public void onFailure(Throwable caught) {
						cancelTimer();
						caught.printStackTrace();
						SC.warn("EXCEPTION: " + caught.getMessage());
						SC.logWarn(caught.getMessage());
					}

				});

	}

}
