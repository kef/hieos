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
package com.vangent.hieos.DocViewer.client.model.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class Config implements IsSerializable {
	private HashMap<String, String> props = new HashMap<String, String>();
	private List<DocumentTemplateConfig> documentTemplateConfigs = new ArrayList<DocumentTemplateConfig>();
	private List<AuthenticationDomainConfig> authDomainList = new ArrayList<AuthenticationDomainConfig>();

	public static final String KEY_SEARCH_MODE = "DefaultSearchMode";
	public static final String VAL_SEARCH_MODE_HIE = "hie";
	public static final String VAL_SEARCH_MODE_NHIN_EXCHANGE = "nhin_exchange";
	public static final String KEY_TITLE = "Title";
	public static final String KEY_LOGO_FILE_NAME = "LogoFileName";
	public static final String KEY_LOGO_WIDTH = "LogoWidth";
	public static final String KEY_LOGO_HEIGHT = "LogoHeight";
	public static final String KEY_LABEL_AUTHDOMAIN_NAME = "AuthDomainLabelText";
	public static final String KEY_LABEL_AUTHDOMAIN_SELECT = "AuthDomainDropDownName";
	public static final String KEY_SHOW_AUTHDOMAIN_LIST = "ShowAuthDomainList";
	public static final String KEY_SHOW_FUZZY_NAME_SEARCH = "ShowFuzzyNameSearch";
	public static final String KEY_TRIM_DOCUMENT_TAB_TITLES = "TrimDocumentTabTitles";
	public static final String KEY_TRIM_DOCUMENT_TAB_TITLES_LENGTH = "TrimDocumentTabTitlesLength";
	public static final String KEY_SAML_CLAIMS_TEMPLATE = "SAMLClaimsTemplate";
	public static final String KEY_SHOW_TITLE_BRANDING = "ShowTitleBranding";
	public static final String KEY_SHOW_FIND_DOCUMENTS_BUTTON = "ShowFindDocumentsButton";
	public static final String KEY_SHOW_ORGANIZATION_COLUMN = "ShowOrganizationColumn";

	public static final String KEY_LABEL_EUID = "LabelEUIDText";
	public static final String KEY_LABEL_FAMILY_NAME = "LabelFamilyNameText";
	public static final String KEY_LABEL_GIVEN_NAME = "LabelGivenNameText";
	/* [03/14/13]  IHS Release 1.3 (Requirement # 7333 - Middle Name updates in DocViewer) */
	public static final String KEY_LABEL_MIDDLE_NAME = "LabelMiddleNameText";
	public static final String KEY_LABEL_HIE_MODE = "LabelHIEModeText";
	public static final String KEY_LABEL_NHIN_MODE = "LabelNHINModeText";

	public static final String DEFAULT_LABEL_EUID = "EUID";
	public static final String DEFAULT_LABEL_FAMILY_NAME = "Family name";
	public static final String DEFAULT_LABEL_GIVEN_NAME = "Given name";
	/* [03/14/13]  IHS Release 1.3 (Requirement # 7333 - Middle Name updates in DocViewer) */
	public static final String DEFAULT_LABEL_MIDDLE_NAME = "Middle name";
	public static final String DEFAULT_LABEL_HIE_MODE = "HIE";
	public static final String DEFAULT_LABEL_NHIN_MODE = "NHIN Exchange";

	public static final String KEY_TOOLTIP_GIVEN_NAME = "ToolTipGivenNameColumn";
	public static final String KEY_TOOLTIP_FAMILY_NAME = "ToolTipFamilyNameColumn";
	/* [03/14/13] IHS Release 1.3 (Requirement # 7333 - Middle Name updates in DocViewer) */
	public static final String KEY_TOOLTIP_MIDDLE_NAME = "ToolTipMiddleNameColumn";
	public static final String KEY_TOOLTIP_DATE_OF_BIRTH = "ToolTipDateOfBirthColumn";
	public static final String KEY_TOOLTIP_GENDER = "ToolTipGenderColumn";
	public static final String KEY_TOOLTIP_SSN = "ToolTipSSNColumn";
	public static final String KEY_TOOLTIP_EUID = "ToolTipEUIDColumn";
	public static final String KEY_TOOLTIP_CONFIDENCE = "ToolTipConfidenceColumn";
//Changed for the IHS requirement- work order: 7334- Provide means for Tribal Sites (non-D1 users) to authenticate and log onto the HIE DocViewer” .
	public static final String DEFAULT_ATHENTICATION_DOMAIN_TYPE = "default";
	/**
	 * 
	 */
	public Config() {
		// Do nothing.
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return props.get(key);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean getAsBoolean(String key) {
		String value = props.get(key);
		if (value == null) {
			return false;
		}
		if (value.equalsIgnoreCase("true")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Integer getAsInteger(String key) {
		String value = props.get(key);
		if (value == null) {
			return new Integer(0);
		}
		return new Integer(value);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void put(String key, String value) {
		props.put(key, value);
	}

	/**
	 * 
	 * @param documentTemplateConfigs
	 */
	public void setDocumentTemplateConfigs(
			List<DocumentTemplateConfig> documentTemplateConfigs) {
		this.documentTemplateConfigs = documentTemplateConfigs;
	}

	/**
	 * 
	 * @param documentTemplateConfig
	 */
	public void addDocumentTemplateConfig(
			DocumentTemplateConfig documentTemplateConfig) {
		this.documentTemplateConfigs.add(documentTemplateConfig);
	}

	/**
	 * 
	 * @return
	 */
	public List<DocumentTemplateConfig> getDocumentTemplateConfigs() {
		return documentTemplateConfigs;
	}

	/**
	 * Set a list of facilities.
	 * 
	 * @param authDomainConfig
	 *            List of facilities from xconfig.xml.
	 */
	public void setAuthDomainListConfigs(
			List<AuthenticationDomainConfig> authDomainConfig) {
		this.authDomainList = authDomainConfig;
	}

	/**
	 * Add a authDomain list
	 * 
	 * @param authDomainConfig
	 *            List of facilities from xconfig.xml.
	 */
	public void addAuthDomainListConfig(
			AuthenticationDomainConfig authDomainConfig) {
		this.authDomainList.add(authDomainConfig);
	}

	/**
	 * Returns a list of facilities.
	 * 
	 * @return List List of facilities.
	 */
	public List<AuthenticationDomainConfig> getAuthDomainListConfigs() {
		return authDomainList;
	}
}
