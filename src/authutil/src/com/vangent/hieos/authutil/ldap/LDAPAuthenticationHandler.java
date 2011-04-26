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
package com.vangent.hieos.authutil.ldap;

import com.vangent.hieos.authutil.framework.AuthUtilException;
import com.vangent.hieos.authutil.framework.AuthenticationHandler;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.vangent.hieos.authutil.model.Role;
import com.vangent.hieos.authutil.model.UserProfile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import org.apache.log4j.Logger;

/**
 *
 * @author Anand Sastry
 */
public class LDAPAuthenticationHandler implements AuthenticationHandler {

    private static Logger log = Logger.getLogger(LDAPAuthenticationHandler.class);
    private LDAPAccessProperties ldapAccessProperties;
    private LDAPClient ldapClient = null;
    private static final String LDAP_URL = "LDAPURL";
    private static final String BASE_DN = "BaseDN";

    /**
     * CTOR
     * @throws AuthUtilException
     */
    public LDAPAuthenticationHandler() throws AuthUtilException  {
        try {
            // Property file location needs to be investigated.
            ldapAccessProperties = 
                    LDAPAccessProperties.getInstance("com/vangent/hieos/authutil/ldap/LDAPAccess.properties");
            ldapClient = new LDAPClient(ldapAccessProperties.getProperty(LDAP_URL));
        } catch (IOException e) {
            log.error("Error accessing LDAP Access Configuration.", e);
            throw new AuthUtilException("Error accessing LDAP Configuration." + e.getMessage());
        }
        catch (NamingException e) {
            log.error("Error accessing LDAP.", e);
            throw new AuthUtilException("Error accessing LDAP." + e.getMessage());
        }

    }

    /**
     *
     * @param creds
     * @return
     */
    public AuthenticationContext authenticate(Credentials creds)  {
        AuthenticationContext authnCtx = new AuthenticationContext();

        boolean status = false;
        if (creds != null) {
            // authenticate
            status = ldapClient.bind(creds.getUserId(), creds.getPassword());
        }
        if (status == true) {
            authnCtx.setStatus(AuthenticationContext.Status.SUCCESS);
            if (log.isInfoEnabled()) {
                log.info("LDAPAuthenticationHandler - User, " + creds.getUserId() + ", authenticated.");
            }
            // get attributes from LDAP
            authnCtx.setUserProfile(getUserProfile(creds));
        } else {
            authnCtx.setStatus(AuthenticationContext.Status.FAILURE);
            if (log.isInfoEnabled()) {
                log.info("LDAPAuthenticationHandler - User, " + (creds!=null ? creds.getUserId() : "") + ", could not be authenticated.");
            }
        }
        //disconnect
        ldapClient.unbind();
        return authnCtx;
    }

    // Private
    /**
     *
     * @param credentials
     * @return
     */
    private UserProfile getUserProfile(Credentials credentials) {
        String userName = extractUserName(credentials.getUserId());
        Map userAttrs = ldapClient.lookupUserAttributes(userName,
                ldapAccessProperties.getProperty(BASE_DN),
                userAttributes());

        if (userAttrs.isEmpty()) {
            log.warn("LDAPAuthenticationHandler - User, " + credentials.getUserId() + ", has no attributes.");
            return null;
        }

        UserProfile userProfile = new UserProfile();
        userProfile.setDistinguishedName(getDistinguishedName(userAttrs));
        userProfile.setGivenName(getGivenName(userAttrs));
        userProfile.setFamilyName(getSurname(userAttrs));
        userProfile.setFullName(getCommonName(userAttrs));
        userProfile.setRoles(getRoles(userAttrs));

        return userProfile;
    }

    /**
     * 
     * @param userAttrs
     * @return
     */
    private List<Role> getRoles(Map userAttrs) {
        ArrayList ldapRoles = (ArrayList) userAttrs.get("memberOf");
        List<Role> roles = new ArrayList<Role>();
        Iterator rolesIt = ldapRoles.iterator();
        while (rolesIt.hasNext()) {
            String roleName = (String) rolesIt.next();
            Role role = new Role(roleName);
            roles.add(role);
        }
        return roles;
    }

    /**
     *
     * @return
     */
    private String[] userAttributes() {
        String retAttrs[] = {"distinguishedName", "givenName", "sn", "cn", "memberOf"};
        return retAttrs;

    }

    /**
     *
     * @param userAttrs
     * @return
     */
    private String getDistinguishedName(Map userAttrs) {
        ArrayList dn = (ArrayList) userAttrs.get("distinguishedName");
        return (String) dn.get(0);
    }

    /**
     *
     * @param userAttrs
     * @return
     */
    private String getCommonName(Map userAttrs) {
        ArrayList cn = (ArrayList) userAttrs.get("cn");
        return (String) cn.get(0);
    }

    /**
     *
     * @param userAttrs
     * @return
     */
    private String getGivenName(Map userAttrs) {
        ArrayList givenName = (ArrayList) userAttrs.get("givenName");
        return (String) givenName.get(0);
    }

    /**
     *
     * @param userAttrs
     * @return
     */
    private String getSurname(Map userAttrs) {
        ArrayList sn = (ArrayList) userAttrs.get("sn");
        return (String) sn.get(0);
    }

    /**
     *
     * @param userWithDomainName
     * @return
     */
    private String extractUserName(String userWithDomainName) {
        return userWithDomainName.substring(userWithDomainName.lastIndexOf("\\") + 1);
    }

    public static void main(String[] args) throws Exception {
        LDAPAuthenticationHandler ldapAuthenticationHandler = new LDAPAuthenticationHandler();

        System.out.println(ldapAuthenticationHandler.authenticate( new Credentials("x\\testuser", "abc123")));
    }
}
