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

public class Credentials {
    private String authDomainTypeKey;
    private String userId;
    private String password;

    public Credentials(String userId, String password) {
        this.userId =  userId == null ? "" : userId.trim();
        this.password = password == null ? "" : password.trim();
        // Use the default the authentication domain type
        this.authDomainTypeKey = "default";
    }

    public Credentials(String userId, String password, String authDomainTypeKey) {
        this.userId =  userId == null ? "" : userId.trim();
        this.password = password == null ? "" : password.trim();
        this.authDomainTypeKey = authDomainTypeKey == null ? "" : authDomainTypeKey.trim();
    }

    public String getUserId() {
        return this.userId;
    }

    public String getPassword() {
        return this.password;
    }

    public String getAuthDomainTypeKey() {
        return this.authDomainTypeKey;
    }
}

