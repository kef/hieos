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

import java.util.Date;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author Anand Sastry
 */
public class AuthenticationContext implements IsSerializable {

    private boolean successStatus;
    private Date creationDate;
    private UserProfile userProfile;

    public AuthenticationContext() {
    }
    
    public void setCreationDate(Date creationDate) {
    	this.creationDate = creationDate;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setSuccessFlag(boolean successFlag) {
        this.successStatus = successFlag;
    }

    public boolean getSuccessStatus() {
        return this.successStatus;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public UserProfile getUserProfile() {
        return this.userProfile;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("CreationDate [")
           .append(creationDate)
           .append("], Success Status [")
           .append(this.successStatus)
           .append("], User Profile [")
           .append(this.userProfile)
           .append("]");
        return buf.toString();
    }
}

