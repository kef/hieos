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
public class PermissionDTO implements IsSerializable {

    private String name;
    private boolean access = false;

    public PermissionDTO() {
    	
    }
    public PermissionDTO(String name, boolean access) {
        this.name = name;
        this.access = access;
    }

	public void setName(String name) {
		this.name = name;
	}
	
    public String getName() {
        return this.name;
    }
    
	public void setAccess(boolean access) {
		this.access = access;
	}
	
    public boolean getAccess() {
        return this.access;
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

