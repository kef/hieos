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

import java.util.List;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author Anand Sastry
 */
public class UserProfileDTO implements IsSerializable {
    private List<PermissionDTO> permissions;
    private String givenName;
    private String familyName;
    private String fullName;
    private String distinguishedName;
    private String userName;
    public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	private String organizationName;

    public void setPermissions(List<PermissionDTO> permissions) {
        this.permissions = permissions;
    }

    public List<PermissionDTO> getPermissions() {
        return this.permissions;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getGivenName() {
        return this.givenName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getLastName() {
        return this.familyName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public String getDistinguishedName() {
        return this.distinguishedName;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Permissions [")
           .append(this.permissions)
           .append("], Given Name [")
           .append(this.givenName)
           .append("], Family Name [")
           .append(this.familyName)
           .append("], Full Name [")
           .append(this.fullName)
           .append("], Distinguished Name [")
           .append(this.distinguishedName)
           .append("] organization [")
           .append(this.organizationName)
            .append("] UserName [")
           .append(this.userName)
           .append("]");
        return buf.toString();
    }
}

