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

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.vangent.hieos.DocViewer.client.helper.Observer;
import com.vangent.hieos.DocViewer.client.helper.TimeOutHelper;
import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContext;
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadata;
import com.vangent.hieos.DocViewer.client.model.document.DocumentSearchCriteria;
import com.vangent.hieos.DocViewer.client.services.rpc.DocumentRemoteService;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class DocumentQueryService extends ProxyService {
	private DocumentSearchCriteria criteria;
	
	/**
	 * 
	 * @param authCtxt
	 * @param criteria
	 * @param observer
	 * @param timeOutHelper
	 */
	public DocumentQueryService(AuthenticationContext authCtxt, DocumentSearchCriteria criteria,
			Observer observer, TimeOutHelper timeOutHelper) {
		super(authCtxt, observer, timeOutHelper);
		this.criteria = criteria;
	}

	/**
	 * 
	 */
	public void doWork() {
		this.getTimeOutHelper().startTimer();
		// RPC:
		DocumentRemoteService.Util.getInstance().findDocuments(this.getAuthenticationContext(), criteria,
				new AsyncCallback<List<DocumentMetadata>>() {
					/**
					 * 
					 * @param documents
					 */
					public void onSuccess(List<DocumentMetadata> documents) {
						cancelTimer();
						if (getAbortFlag()) {
							// Timeout already occurred. discard result
							return;
						}

						if (documents.size() == 0) {
							SC.say("No documents found for patient");
						} else {
							update(documents);
						}
					}

					/**
					 * 
					 */
					public void onFailure(Throwable caught) {
						cancelTimer();
						SC.warn("EXCEPTION: " + caught.getMessage());
						SC.logWarn(caught.getMessage());
					}
				});
	}
}