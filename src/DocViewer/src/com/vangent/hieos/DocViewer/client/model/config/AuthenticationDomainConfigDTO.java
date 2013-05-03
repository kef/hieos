/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.DocViewer.client.model.config;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Class holding the authDomain information obtain from xconfig.xml.
 * 
 * @author Daniel Ng
 */
public class AuthenticationDomainConfigDTO implements IsSerializable {
	private String authHandlerLdapBaseDn;
	private String authHandlerLdapUrl;
	private String authHandlerLdapUsernameFormat;
	private String authDomainName;
	private String authDomainValue;

	/**
	 * 
	 * @return
	 */
	public String getAuthHandlerLdapBaseDn() {
		return authHandlerLdapBaseDn;
	}

	/**
	 * 
	 * @param authHandlerLdapBaseDn
	 */
	public void setAuthHandlerLdapBaseDn(String authHandlerLdapBaseDn) {
		this.authHandlerLdapBaseDn = authHandlerLdapBaseDn;
	}

	/**
	 * 
	 * @return
	 */
	public String getAuthHandlerLdapUrl() {
		return authHandlerLdapUrl;
	}

	/**
	 * 
	 * @param authHandlerLdapUrl
	 */
	public void setAuthHandlerLdapUrl(String authHandlerLdapUrl) {
		this.authHandlerLdapUrl = authHandlerLdapUrl;
	}

	/**
	 * 
	 * @return
	 */
	public String getAuthHandlerLdapUsernameFormat() {
		return authHandlerLdapUsernameFormat;
	}

	/**
	 * @param authHandlerLdapUsernameFormat
	 */
	public void setAuthHandlerLdapUsernameFormat(
			String authHandlerLdapUsernameFormat) {
		this.authHandlerLdapUsernameFormat = authHandlerLdapUsernameFormat;
	}

	/**
	 * 
	 * @return
	 */
	public String getAuthDomainName() {
		return authDomainName;
	}

	/**
	 * 
	 * @param authDomainName
	 */
	public void setAuthDomainName(String authDomainName) {
		this.authDomainName = authDomainName;
	}

	/**
	 * 
	 * @return
	 */
	public String getAuthDomainValue() {
		return authDomainValue;
	}

	/**
	 * 
	 * @param authDomainValue
	 */
	public void setAuthDomainValue(String authDomainValue) {
		this.authDomainValue = authDomainValue;
	}
}
