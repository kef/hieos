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
package com.vangent.hieos.DocViewer.client.config;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class Config implements IsSerializable {
	private HashMap<String, String> props = new HashMap<String, String>();
	
	public static String KEY_SEARCH_MODE = "search_mode";
	public static String VAL_SEARCH_MODE_HIE = "hie";
	public static String VAL_SEARCH_MODE_NHIN_EXCHANGE = "nhin_exchange";

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
	 * @param value
	 */
	public void put(String key, String value) {
		props.put(key, value);
	}
}
