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

import com.vangent.hieos.DocViewer.client.model.document.DocumentMetadata;
import com.vangent.hieos.DocViewer.client.model.document.DocumentSearchCriteria;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author Bernie Thuman
 *
 */
public interface DocumentRemoteServiceAsync {
	
	/**
	 * 
	 * @param criteria
	 * @param callback
	 */
	public void findDocuments(DocumentSearchCriteria criteria, AsyncCallback<List<DocumentMetadata>> callback);
}
