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
package com.vangent.hieos.DocViewer.client.controller;

import com.vangent.hieos.DocViewer.client.entrypoint.DocViewer;
import com.vangent.hieos.DocViewer.client.helper.Observer;
import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContext;

/**
 * 
 * @author Anand Sastry
 * 
 */
public class AuthenticationObserver implements Observer {
	private DocViewer docViewer;

	public AuthenticationObserver(DocViewer docViewer) {
		this.docViewer = docViewer;
	}

	/**
	 * 
	 */
	public void update(Object object) {
		AuthenticationContext authCtx = (AuthenticationContext) object;
		//SC.say("CLIENTSIDE ===== " + authCtx.toString());
		docViewer.loadMainPageOnLoginSuccess(authCtx);
	}

}
