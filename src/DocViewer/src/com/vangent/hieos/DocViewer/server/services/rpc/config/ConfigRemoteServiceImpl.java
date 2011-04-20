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
		Config config = new Config();
		config.put(Config.KEY_SEARCH_MODE, Config.VAL_SEARCH_MODE_HIE);

		// FIXME: read from XConfig ...
		ServletUtilMixin servletUtil = new ServletUtilMixin();
		servletUtil.init(this.getServletContext());
		String title = servletUtil.getProperty("DocViewerTitle");
		String logoName = servletUtil.getProperty("DocViewerLogo");
		String logoWidth = servletUtil.getProperty("DocViewerLogoW");
		String logoHeigth = servletUtil.getProperty("DocViewerLogoH");
		config.put("DocViewerTitle", title);
		config.put("DocViewerLogo", logoName);
		config.put("DocViewerLogoW", logoWidth);
		config.put("DocViewerLogoH", logoHeigth);
		System.out.println("Title: " + title);
		System.out.println("LogoName: " + logoName);
		
		return config;
	}
}
