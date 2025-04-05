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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Anand Sastry
 */
public class LDAPAuthenticationHandler implements AuthenticationHandler {

    private static final Logger log = Logger.getLogger(LDAPAuthenticationHandler.class);
    private static final String LDAP_URL = "AuthHandlerLDAP_URL";
    private static final String LDAP_BASE_DN = "AuthHandlerLDAP_BASE_DN";
    private static final String LDAP_USERNAME_FORMAT = "AuthHandlerLDAP_USERNAME_FORMAT";
    private static final String USERNAME_REPLACE_STRING = "${UserName}";
    private String ldapBaseDN = null;
    private String ldapURL = null;
    private String userNameFormat = null;
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
    @Override
    public AuthenticationContext authenticate(Credentials creds) throws AuthUtilException {
        AuthenticationContext authnCtx = new AuthenticationContext();
        this.configure(creds.getAuthDomainTypeKey());

        LDAPClient ldapClient = null;
        try {

            ldapClient = new LDAPClient(this.ldapURL);

        } catch (NamingException e) {
            log.error("Error accessing LDAP.", e);
            throw new AuthUtilException("Error accessing LDAP." + e.getMessage());
        }

        try {

            boolean status = false;
            String username = "";
            if (creds != null) {
                 username = creds.getUserId();
                if (StringUtils.isNotBlank(this.userNameFormat)) {
                    username = StringUtils.replace(this.userNameFormat,
                            USERNAME_REPLACE_STRING, username);
                }

                // authenticate
                status = ldapClient.bind(username, creds.getPassword());
            }
            if (status == true) {
                authnCtx.setStatus(AuthenticationContext.Status.SUCCESS);
                if (log.isInfoEnabled()) {
                    log.info("LDAPAuthenticationHandler - User, " + username + ", authenticated.");
                }
                // get attributes from LDAP
                authnCtx.setUserProfile(getUserProfile(ldapClient, creds));
            } else {
                authnCtx.setStatus(AuthenticationContext.Status.FAILURE);
                if (log.isInfoEnabled()) {
                    log.info("LDAPAuthenticationHandler - User, " + username + ", could not be authenticated.");
                }
            }
        } finally {

            // disconnect, release resources!!!!
            if (ldapClient != null) {
                ldapClient.unbind();
            }
        }

        return authnCtx;
    }

    /**
     *
     * @throws AuthUtilException
     */
    private void configure(String authDomainTypeKey) {
        // Get the list of authentication domains from the xconfig file.
        //Changed for the IHS requirement- work order: 7334- Provide means for Tribal Sites (non-D1 users) to authenticate and log onto the HIE DocViewer” .
   //     XConfigObject authDomainListConfig = this.config.getXConfigObjectWithName("AuthDomainList", "AuthDomainListType");
             XConfigObject authDomainListConfig = this.config.getXConfigObjectWithName("AuthDomains", "AuthDomainListType");
        List<XConfigObject> configObjects = authDomainListConfig.getXConfigObjectsWithType("AuthDomainType");

        // Find the LDAP configuration that matches the authentication domain name.
        for (XConfigObject configObject : configObjects) {
            // Check if this is the selected authentication domain.
            if (configObject.getProperty("AuthDomainTypeKey").equals(authDomainTypeKey)) {
                log.info("AuthDomainTypeKey: " + authDomainTypeKey);
                this.ldapURL = configObject.getProperty(LDAP_URL);
                this.ldapBaseDN = configObject.getProperty(LDAP_BASE_DN);
                this.userNameFormat = configObject.getProperty(LDAP_USERNAME_FORMAT);
                break;
            }
        }

        if (log.isInfoEnabled()) {
            log.info("AuthHandlerClassImpl: " + config.getProperty("AuthHandlerClassImpl"));
            log.info("AuthHandlerLDAP_URL: " + this.ldapURL);
            log.info("AuthHandlerLDAP_BASE_DN: " + this.ldapBaseDN);
            log.info("AuthHandlerLDAP_USERNAME_FORMAT: " + this.userNameFormat);
        }
    }

    // Private
    /**
     *
     * @param credentials
     * @return
     */
    private UserProfile getUserProfile(LDAPClient ldapClient, Credentials credentials) {
        String userName = extractUserName(credentials.getUserId());
        Map userAttrs = ldapClient.lookupUserAttributes(userName,
                this.ldapBaseDN,
                userAttributes());

        if (userAttrs.isEmpty()) {
            log.warn("LDAPAuthenticationHandler - User, " + credentials.getUserId() + ", has no attributes.");
            return null;
        }

        UserProfile userProfile = new UserProfile();
        userProfile.setUserName(userName); //Added to populate the user id
        userProfile.setDistinguishedName(getDistinguishedName(userAttrs));
        userProfile.setGivenName(getGivenName(userAttrs));
        userProfile.setFamilyName(getSurname(userAttrs));
        userProfile.setFullName(getCommonName(userAttrs));
        userProfile.setRoles(getRoles(userAttrs));
        userProfile.setOrganization(getOrganizationName(userAttrs)); // Added to get Organization Name
        userProfile.setInitials(getInitials(userAttrs)); // Added to get Initials Name

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
        String retAttrs[] = {"distinguishedName", "givenName", "sn", "cn", "company","initials","memberOf"};
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
    private String getOrganizationName(Map userAttrs) {
        ArrayList company = (ArrayList) userAttrs.get("company");        
        return (String) company.get(0);
        
    }
    /**
     *
     * @param userAttrs
     * @return
     */
    private String getInitials(Map userAttrs) {
        ArrayList initials = (ArrayList) userAttrs.get("initials");
        return (String) initials.get(0);

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
    @Override
    public void setConfig(XConfigObject config) {
        this.config = config;
    }
}
