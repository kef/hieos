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
package com.vangent.hieos.DocViewer.client.services.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.vangent.hieos.DocViewer.client.exception.RemoteServiceException;
import com.vangent.hieos.DocViewer.client.model.patient.PatientConsentDirectivesDTO;
import com.vangent.hieos.DocViewer.client.model.patient.PatientConsentSearchCriteriaDTO;

/**
 * 
 * @author Bernie Thuman
 *
 */
@RemoteServiceRelativePath("PIPRemoteService")
public interface PIPRemoteService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static PIPRemoteServiceAsync instance;
		public static PIPRemoteServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(PIPRemoteService.class);
			}
			return instance;
		}
	}
	
	/**
	 * 
	 * @param criteria
	 * @return
	 * @throws RemoteServiceException
	 */
	public PatientConsentDirectivesDTO getPatientConsentDirectives(PatientConsentSearchCriteriaDTO criteria) throws RemoteServiceException;
}
