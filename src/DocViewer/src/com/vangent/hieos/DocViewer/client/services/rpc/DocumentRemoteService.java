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
import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadata;
import com.vangent.hieos.DocViewer.client.model.document.DocumentSearchCriteria;

/**
 * 
 * @author Bernie Thuman
 *
 */
@RemoteServiceRelativePath("DocumentRemoteService")
public interface DocumentRemoteService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static DocumentRemoteServiceAsync instance;
		public static DocumentRemoteServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(DocumentRemoteService.class);
			}
			return instance;
		}
	}
	
	public List<DocumentMetadata> findDocuments(DocumentSearchCriteria criteria);
}
