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

	public static String KEY_SEARCH_MODE = "DefaultSearchMode";
	public static String VAL_SEARCH_MODE_HIE = "hie";
	public static String VAL_SEARCH_MODE_NHIN_EXCHANGE = "nhin_exchange";
	public static String KEY_TITLE = "Title";
	public static String KEY_LOGO_FILE_NAME = "LogoFileName";
	public static String KEY_LOGO_WIDTH = "LogoWidth";
	public static String KEY_LOGO_HEIGHT = "LogoHeight";
	public static String KEY_TRIM_DOCUMENT_TAB_TITLES = "TrimDocumentTabTitles";
	public static String KEY_TRIM_DOCUMENT_TAB_TITLES_LENGTH = "TrimDocumentTabTitlesLength";
	
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
	public void setDocumentTemplateConfigs(List<DocumentTemplateConfig> documentTemplateConfigs) {
		this.documentTemplateConfigs = documentTemplateConfigs;
	}
	
	/**
	 * 
	 * @param documentTemplateConfig
	 */
	public void addDocumentTemplateConfig(DocumentTemplateConfig documentTemplateConfig)
	{
		this.documentTemplateConfigs.add(documentTemplateConfig);
	}

	/**
	 * 
	 * @return
	 */
	public List<DocumentTemplateConfig> getDocumentTemplateConfigs() {
		return documentTemplateConfigs;
	}
}
