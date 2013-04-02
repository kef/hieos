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
 */
package com.vangent.hieos.DocViewer.server.services.rpc.config;

import java.util.List;

import com.vangent.hieos.DocViewer.client.exception.RemoteServiceException;
import com.vangent.hieos.DocViewer.client.model.config.Config;
import com.vangent.hieos.DocViewer.client.model.config.DocumentTemplateConfig;
import com.vangent.hieos.DocViewer.client.model.config.AuthenticationDomainConfig;
import com.vangent.hieos.DocViewer.client.services.rpc.ConfigRemoteService;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConfigRemoteServiceImpl extends RemoteServiceServlet implements
		ConfigRemoteService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7923244304825432784L;

	private final ServletUtilMixin servletUtil = new ServletUtilMixin();

	@Override
	public Config getConfig() throws RemoteServiceException {
		System.out.println("********* ConfigRemoteServiceImpl ********");

		// Get the mixin to allow access to xconfig.xml.
		servletUtil.init(this.getServletContext());

		// Create the Config instance that will be sent back to the client.
		Config config = new Config();

		// Now get the relevant properties

		// DefaultSearchMode:
		String defaultSearchMode = servletUtil
				.getProperty(Config.KEY_SEARCH_MODE);
		System.out.println("DefaultSearchMode = " + defaultSearchMode);
		if (defaultSearchMode == null) {
			defaultSearchMode = Config.VAL_SEARCH_MODE_HIE;
		}

		// TrimDocumentTabTitles:
		String trimDocumentTabTitles = servletUtil
				.getProperty(Config.KEY_TRIM_DOCUMENT_TAB_TITLES);
		if (trimDocumentTabTitles == null) {
			trimDocumentTabTitles = "false";
		}

		// TrimDocumentTabTitlesLength:
		String trimDocumentTabTitlesLength = servletUtil
				.getProperty(Config.KEY_TRIM_DOCUMENT_TAB_TITLES_LENGTH);
		if (trimDocumentTabTitlesLength == null) {
			trimDocumentTabTitlesLength = "50";
		}

		// Title:
		String title = servletUtil.getProperty(Config.KEY_TITLE);
		if (title == null) {
			title = "HIEOS Doc Viewer";
		}

		// LogoFileName:
		String logoFileName = servletUtil
				.getProperty(Config.KEY_LOGO_FILE_NAME);
		System.out.println("LogoFileName = " + logoFileName);
		if (logoFileName == null) {
			logoFileName = "search_computer.png";
		}

		// LogoWidth/LogoHeight:
		String logoWidth = servletUtil.getProperty(Config.KEY_LOGO_WIDTH);
		String logoHeigth = servletUtil.getProperty(Config.KEY_LOGO_HEIGHT);

		// Authentication domain list configuration:
		String showAuthDomainList = servletUtil
				.getProperty(Config.KEY_SHOW_AUTHDOMAIN_LIST);
		if (showAuthDomainList == null) {
			showAuthDomainList = "false";
		}
		String authDomainName = servletUtil
				.getProperty(Config.KEY_LABEL_AUTHDOMAIN_NAME);
		String authDomainSelect = servletUtil
				.getProperty(Config.KEY_LABEL_AUTHDOMAIN_SELECT);

		// Fill up the config:
		config.put(Config.KEY_SEARCH_MODE, defaultSearchMode);
		config.put(Config.KEY_TRIM_DOCUMENT_TAB_TITLES, trimDocumentTabTitles);
		config.put(Config.KEY_TRIM_DOCUMENT_TAB_TITLES_LENGTH,
				trimDocumentTabTitlesLength);
		config.put(Config.KEY_TITLE, title);
		config.put(Config.KEY_LOGO_FILE_NAME, logoFileName);
		config.put(Config.KEY_LOGO_WIDTH, logoWidth);
		config.put(Config.KEY_LOGO_HEIGHT, logoHeigth);
		config.put(Config.KEY_SHOW_AUTHDOMAIN_LIST, showAuthDomainList);
		config.put(Config.KEY_LABEL_AUTHDOMAIN_NAME, authDomainName);
		config.put(Config.KEY_LABEL_AUTHDOMAIN_SELECT, authDomainSelect);

		// copy properties from xconfig to config
		copyToConfig(config, Config.KEY_SHOW_FIND_DOCUMENTS_BUTTON);
		copyToConfig(config, Config.KEY_SHOW_ORGANIZATION_COLUMN);
		copyToConfig(config, Config.KEY_SHOW_TITLE_BRANDING);

		copyToConfig(config, Config.KEY_LABEL_EUID, Config.DEFAULT_LABEL_EUID);
		copyToConfig(config, Config.KEY_LABEL_FAMILY_NAME,
				Config.DEFAULT_LABEL_FAMILY_NAME);
		copyToConfig(config, Config.KEY_LABEL_GIVEN_NAME,
				Config.DEFAULT_LABEL_GIVEN_NAME);
		/* [03/14/13]  IHS Release 1.3 (Requirement # 7333 - Middle Name updates in DocViewer) */
		copyToConfig(config, Config.KEY_LABEL_MIDDLE_NAME,
				Config.DEFAULT_LABEL_MIDDLE_NAME);
		copyToConfig(config, Config.KEY_LABEL_HIE_MODE,
				Config.DEFAULT_LABEL_HIE_MODE);
		copyToConfig(config, Config.KEY_LABEL_NHIN_MODE,
				Config.DEFAULT_LABEL_NHIN_MODE);
		// Set the default label to blank, since one already exist.
		copyToConfig(config, Config.KEY_SHOW_FUZZY_NAME_SEARCH, "");

		copyToConfig(config, Config.KEY_TOOLTIP_CONFIDENCE);
		copyToConfig(config, Config.KEY_TOOLTIP_DATE_OF_BIRTH);
		copyToConfig(config, Config.KEY_TOOLTIP_EUID);
		copyToConfig(config, Config.KEY_TOOLTIP_FAMILY_NAME);
		copyToConfig(config, Config.KEY_TOOLTIP_GIVEN_NAME);
		/* [03/14/13]  IHS Release 1.3 (Requirement # 7333 - Middle Name updates in DocViewer) */
		copyToConfig(config, Config.KEY_TOOLTIP_MIDDLE_NAME);
		copyToConfig(config, Config.KEY_TOOLTIP_GENDER);
		copyToConfig(config, Config.KEY_TOOLTIP_SSN);

		this.loadDocumentTemplateConfigs(config);

		if (showAuthDomainList.equalsIgnoreCase("true")) {
			// Load the list of authentication domains.
			this.loadAuthDomainListConfig(config);
		}

		return config;
	}

	private void copyToConfig(Config config, String propertyName) {
		copyToConfig(config, propertyName, null);
	}

	private void copyToConfig(Config config, String propertyName,
			String defaultName) {

		if (defaultName != null) {

			config.put(propertyName,
					servletUtil.getProperty(propertyName, defaultName));

		} else {

			config.put(propertyName, servletUtil.getProperty(propertyName));
		}
	}

	/**
	 * 
	 * @param config
	 */
	private void loadDocumentTemplateConfigs(Config config) {
		try {
			XConfig xconf = XConfig.getInstance();
			XConfigObject propertiesObject = xconf.getXConfigObjectByName(
					"DocViewerProperties", "DocViewerPropertiesType");
			XConfigObject documentTemplateListConfig = propertiesObject
					.getXConfigObjectWithName("DocumentTemplates",
							"DocumentTemplateListType");
			List<XConfigObject> configObjects = documentTemplateListConfig
					.getXConfigObjectsWithType("DocumentTemplateType");
			for (XConfigObject configObject : configObjects) {
				DocumentTemplateConfig documentTemplateConfig = new DocumentTemplateConfig();
				documentTemplateConfig.setDisplayName(configObject
						.getProperty("DisplayName"));
				documentTemplateConfig.setFileName(configObject
						.getProperty("FileName"));
				config.addDocumentTemplateConfig(documentTemplateConfig);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Load the list of authentication domains.
	 * 
	 * @param config
	 */
	private void loadAuthDomainListConfig(Config config) {
		try {
			XConfig xconf = XConfig.getInstance();
			XConfigObject propertiesObject = xconf.getXConfigObjectByName(
					"DocViewerProperties", "DocViewerPropertiesType");
			XConfigObject authDomainListConfig = propertiesObject
					.getXConfigObjectWithName("AuthDomains",
							"AuthDomainListType");
			List<XConfigObject> configObjects = authDomainListConfig
					.getXConfigObjectsWithType("AuthDomainType");
			// Get the authentication domains from xconfig.xml.
			for (XConfigObject configObject : configObjects) {
				AuthenticationDomainConfig authDomainConfig = new AuthenticationDomainConfig();
				/**
				 * Changed for the IHS requirement- work order: 7334- Provide means for Tribal Sites (non-D1 users) to authenticate and log onto the HIE DocViewer” .
				authDomainConfig.setAuthDomainName(configObject
						.getProperty("AuthDomainName"));
				authDomainConfig.setAuthDomainValue(configObject
						.getProperty("DisplayName"));
						*/
				authDomainConfig.setAuthDomainName(configObject
						.getProperty("DisplayName"));
		authDomainConfig.setAuthDomainValue(configObject
				.getProperty("AuthDomainTypeKey"));
				authDomainConfig.setAuthHandlerLdapBaseDn(configObject
						.getProperty("AuthHandlerLDAP_BASE_DN"));
				authDomainConfig.setAuthHandlerLdapUrl("AuthHandlerLDAP_URL");
				authDomainConfig
						.setAuthHandlerLdapUsernameFormat("AuthHandlerLDAP_USERNAME_FORMAT");
				config.addAuthDomainListConfig(authDomainConfig);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
