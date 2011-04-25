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

/**
 *
 * @author Anand Sastry
 */
public class Permission {

    public enum Access {

        PERMIT, DENY
    };
    private String name;
    private Access access = Access.PERMIT;

    public Permission(String name, Access access) {
        this.name = name;
        this.access = access;
    }

    public String getPermissionName() {
        return this.name;
    }

    public Access getPermissionAccess() {
        return this.access;
    }

    public boolean isPermitted() {
        return this.access == Access.PERMIT;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Permission Name [")
           .append(this.name)
           .append("], Permission Access [")
           .append(this.access)
           .append("]");
        return buf.toString();
    }
}
