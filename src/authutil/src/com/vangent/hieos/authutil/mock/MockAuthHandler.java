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
package com.vangent.hieos.authutil.mock;

import com.vangent.hieos.authutil.framework.AuthenticationHandler;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.vangent.hieos.authutil.model.Permission;
import com.vangent.hieos.authutil.model.UserProfile;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author sastan
 */
public class MockAuthHandler implements AuthenticationHandler {

    private static Logger log = Logger.getLogger(MockAuthHandler.class);

    public MockAuthHandler() throws Exception {
    }

    public AuthenticationContext authenticate(Credentials creds) {
        AuthenticationContext authnCtx = new AuthenticationContext();
        if (validateInput(creds)) {
            authnCtx.setStatus(AuthenticationContext.Status.SUCCESS);
            authnCtx.setUserProfile(getUserProfile(creds));
        } else {
            authnCtx.setStatus(AuthenticationContext.Status.FAILURE);
            authnCtx.setUserProfile(null);
        }

        return authnCtx;
    }

    // Private
    private UserProfile getUserProfile(Credentials credentials) {
        UserProfile userProfile = new UserProfile();
        userProfile.setDistinguishedName("Distinguished Name for " + credentials.getUserId());
        userProfile.setGivenName("Given Name for " + credentials.getUserId());
        userProfile.setFamilyName("Family Name for " + credentials.getUserId());
        userProfile.setFullName("Full Name for " + credentials.getUserId());
        userProfile.setPermissions(getPermissions());

        return userProfile;
    }

    private List<Permission> getPermissions() {
        List<Permission> permissions = new ArrayList<Permission>();
        Permission permission = new Permission("Role1", Permission.Access.PERMIT);
        permissions.add(permission);
        return permissions;
    }

    private boolean validateInput(Credentials creds) {
        if (creds == null) {
            return false;
        }

        if (creds.getUserId() == null || creds.getUserId().trim().length() == 0) {
            return false;
        }

        if (creds.getPassword() == null || creds.getPassword().trim().length() <= 3) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        MockAuthHandler MockAuthHandler = new MockAuthHandler();

        System.out.println(MockAuthHandler.authenticate(new Credentials("testuser", "abc123")));
    }
}
