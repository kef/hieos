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
package com.vangent.hieos.DocViewer.client.model.authentication;

import com.google.gwt.user.client.rpc.IsSerializable;
/**
*
* @author Anand Sastry
*/
public class CredentialsDTO implements IsSerializable {
	private String authDomainTypeKey;
	private String userId;
	private String password;

	public CredentialsDTO() {
	}

	public String getAuthDomainTypeKey() {
		return authDomainTypeKey;
	}

	public void setAuthDomainTypeKey(String authDomainTypeKey) {
		this.authDomainTypeKey = authDomainTypeKey;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getUserId() {
		return this.userId;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return this.password;
	}
}
