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

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.vangent.hieos.DocViewer.client.model.patient.Patient;
import com.vangent.hieos.DocViewer.client.model.patient.PatientSearchCriteria;

/**
 * 
 * @author Bernie Thuman
 *
 */
@RemoteServiceRelativePath("PDSRemoteService")
public interface PDSRemoteService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static PDSRemoteServiceAsync instance;
		public static PDSRemoteServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(PDSRemoteService.class);
			}
			return instance;
		}
	}
	public List<Patient> getPatients(PatientSearchCriteria criteria);
}
