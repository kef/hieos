/*
 *
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
 */package com.vangent.hieos.DocViewer.server.services.rpc.config;

import com.vangent.hieos.DocViewer.client.model.config.Config;
import com.vangent.hieos.DocViewer.client.services.rpc.ConfigRemoteService;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConfigRemoteServiceImpl extends RemoteServiceServlet implements ConfigRemoteService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7923244304825432784L;

	@Override
	public Config getConfig() {
		System.out.println("********* ConfigRemoteServiceImpl ********");
		
		// Get the mixin to allow access to xconfig.xml.
		ServletUtilMixin servletUtil = new ServletUtilMixin();
		servletUtil.init(this.getServletContext());

		// Create the Config instance that will be sent back to the client.
		Config config = new Config();
		
		// Now get the relevant properties
		
		// DefaultSearchMode:
		String defaultSearchMode = servletUtil.getProperty(Config.KEY_SEARCH_MODE);
		System.out.println("DefaultSearchMode = " + defaultSearchMode);
		if (defaultSearchMode == null)
		{
			defaultSearchMode = Config.VAL_SEARCH_MODE_HIE;
		}

		// Title:
		String title = servletUtil.getProperty(Config.KEY_TITLE);		
		if (title == null)
		{
			title = "HIEOS Doc Viewer";
		}
		
		// LogoFileName:
		String logoFileName = servletUtil.getProperty(Config.KEY_LOGO_FILE_NAME);
		System.out.println("LogoFileName = " + logoFileName);
		if (logoFileName == null)
		{
			logoFileName = "search_computer.png";
		}
		
		// LogoWidth/LogoHeight:
		String logoWidth = servletUtil.getProperty(Config.KEY_LOGO_WIDTH);
		String logoHeigth = servletUtil.getProperty(Config.KEY_LOGO_HEIGHT);
		
		// Fill up the config:
		config.put(Config.KEY_SEARCH_MODE, defaultSearchMode);
		config.put(Config.KEY_TITLE, title);
		config.put(Config.KEY_LOGO_FILE_NAME, logoFileName);
		config.put(Config.KEY_LOGO_WIDTH, logoWidth);
		config.put(Config.KEY_LOGO_HEIGHT, logoHeigth);
		
		return config;
	}
}
