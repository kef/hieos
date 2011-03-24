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
import com.vangent.hieos.DocViewer.client.model.patient.Patient;
import com.vangent.hieos.DocViewer.client.model.patient.PatientSearchCriteria;
import com.vangent.hieos.DocViewer.client.services.rpc.PDSRemoteService;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientQueryService extends ProxyService {
	private PatientSearchCriteria criteria;

	/**
	 * 
	 * @param criteria
	 * @param observer
	 */
	public PatientQueryService(PatientSearchCriteria criteria,
			Observer observer, TimeOutHelper progressHelper) {
		super(observer, progressHelper);
		this.criteria = criteria;
	}

	/**
	 * 
	 */
	public void doWork() {
		this.startTimer();
		/*
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){

			@Override
			public void onUncaughtException(Throwable e) {
				// TODO Auto-generated method stub
			      //final ByteArrayOutputStream bas = new ByteArrayOutputStream();
			      //final PrintWriter pw = new PrintWriter(bas);
			      //e.printStackTrace(pw);
			      //SC.say("UNCAUGHT EXCEPTION TRACE: " + pw.toString());
				
			}
			
		}); */

		// RPC:
		PDSRemoteService.Util.getInstance().getPatients(criteria,
				new AsyncCallback<List<Patient>>() {

					/**
					 * 
					 * @param patients
					 */
					public void onSuccess(List<Patient> patients) {
						cancelTimer();
						if (getAbortFlag()) {
							// Timeout already occurred. discard result
							return;
						}
						if (patients.size() == 0) {
							SC.say("No patients found");
						}
						update(patients);
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