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
package com.vangent.hieos.authutil.model;

import java.util.List;

/**
 *
 * @author Anand Sastry
 */
public class UserProfile {
    private List<Permission> permissions;
    private List<Role> roles;
    private String givenName;
    private String familyName;
    private String fullName;
    private String distinguishedName;

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public List<Permission> getPermissions() {
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

    public String getFamilyName() {
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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("Roles [")
           .append(this.roles)
           .append("], Given Name [")
           .append(this.givenName)
           .append("], Family Name [")
           .append(this.familyName)
           .append("], Full Name [")
           .append(this.fullName)
           .append("], Distinguished Name [")
           .append(this.distinguishedName)
           .append("]");
        return buf.toString();
    }
}
