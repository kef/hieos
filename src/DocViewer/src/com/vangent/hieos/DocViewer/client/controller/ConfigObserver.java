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
import com.vangent.hieos.DocViewer.client.model.config.ConfigDTO;

/**
 * 
 * @author Bernie Thuman
 *
 */
public class ConfigObserver implements Observer {
	private final DocViewerController controller;
	private final DocViewer docViewer;
	
	/**
	 * 
	 * @param docViewer
	 * @param controller
	 */
	public ConfigObserver(DocViewer docViewer, DocViewerController controller)
	{
		this.docViewer = docViewer;
		this.controller = controller;
	}

	/**
	 * 
	 */
	@Override
	public void update(Object object) {
		ConfigDTO config = (ConfigDTO) object;
		controller.setConfig(config);
		docViewer.loadLoginPage();
	}

}
