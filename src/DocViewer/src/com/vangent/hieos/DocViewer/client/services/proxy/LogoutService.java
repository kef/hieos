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
package com.vangent.hieos.DocViewer.client.services.proxy;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.vangent.hieos.DocViewer.client.helper.Observer;
import com.vangent.hieos.DocViewer.client.helper.TimeOutHelper;
import com.vangent.hieos.DocViewer.client.services.rpc.AuthenticationRemoteService;
import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContext;

/**
 * 
 * @author Bernie Thuman
 *
 */
public class LogoutService extends ProxyService {

	
	/**
	 * 
	 * @param authCtxt
	 * @param observer
	 * @param timeOutHelper
	 */
	public LogoutService(AuthenticationContext authCtxt, 
			Observer observer, TimeOutHelper timeOutHelper) {
		super(authCtxt, observer, timeOutHelper);
	}
	

	/**
	 * 
	 */
	public void doWork() {

		this.getTimeOutHelper().startTimer();
		// RPC:
		AuthenticationRemoteService.Util.getInstance().logout(this.getAuthenticationContext(),
				new AsyncCallback<Void>() {

			        /**
			         * 
			         * @param noop
			         */
					public void onSuccess(Void noop) {
						cancelTimer();
						if (getAbortFlag()) {
							// Timeout already occurred. discard result
							return;
						}
						update(null);
					}

					/**
					 * 
					 */
					public void onFailure(Throwable caught) {
						cancelTimer();
						caught.printStackTrace();
						SC.warn("EXCEPTION: " + caught.getMessage());
						SC.logWarn(caught.getMessage());
						update(null);  // Still go back to login page.
					}

				});

	}

}
