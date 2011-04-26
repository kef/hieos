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
import com.vangent.hieos.xutil.xconfig.XConfigObject;
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
    private static final String LDAP_URL = "AuthHandlerLDAP_URL";
    private static final String LDAP_BASE_DN = "AuthHandlerLDAP_BASE_DN";
    private LDAPClient ldapClient = null;
    private String ldapBaseDN = null;
    private XConfigObject config;

    /**
     *
     */
    public LDAPAuthenticationHandler() {
        // Do nothing.
    }

    /**
     *
     * @param creds
     * @return
     * @throws AuthUtilException
     */
    public AuthenticationContext authenticate(Credentials creds) throws AuthUtilException {
        AuthenticationContext authnCtx = new AuthenticationContext();
        this.configure();
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
                log.info("LDAPAuthenticationHandler - User, " + (creds != null ? creds.getUserId() : "") + ", could not be authenticated.");
            }
        }
        //disconnect
        ldapClient.unbind();
        return authnCtx;
    }

    /**
     *
     * @throws AuthUtilException
     */
    private void configure() throws AuthUtilException {
        try {
            String ldapURL = config.getProperty(LDAP_URL);
            this.ldapBaseDN = config.getProperty(LDAP_BASE_DN);
            ldapClient = new LDAPClient(ldapURL);
        } catch (NamingException e) {
            log.error("Error accessing LDAP.", e);
            throw new AuthUtilException("Error accessing LDAP." + e.getMessage());
        }
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
                this.ldapBaseDN,
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

    /**
     * 
     * @param config
     */
    public void setConfig(XConfigObject config) {
        this.config = config;
        if (log.isInfoEnabled()) {
            log.info("AuthHandlerClassImpl: " + config.getProperty("AuthHandlerClassImpl"));
            log.info("AuthHandlerLDAP_URL: " + config.getProperty("AuthHandlerLDAP_URL"));
            log.info("AuthHandlerLDAP_BASE_DN: " + config.getProperty("AuthHandlerLDAP_BASE_DN"));
        }
    }
}
