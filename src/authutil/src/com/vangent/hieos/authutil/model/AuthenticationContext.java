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

import java.util.Date;

/**
 *
 * @author Anand Sastry
 */
public class AuthenticationContext {

    public enum Status {

        SUCCESS, FAILURE
    };
    private Status status = Status.FAILURE;
    private Date creationDate;
    private UserProfile userProfile;

    public AuthenticationContext() {
        this.creationDate = new Date();
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public UserProfile getUserProfile() {
        return this.userProfile;
    }

    public boolean hasSuccessStatus() {
        return this.status == Status.SUCCESS;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("CreationDate [")
           .append(creationDate)
           .append("], Status [")
           .append(this.status)
           .append("], User Profile [")
           .append(this.userProfile)
           .append("]");
        return buf.toString();
    }
}
