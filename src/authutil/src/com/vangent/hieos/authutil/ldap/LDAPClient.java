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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Anand Sastry
 */
public class LDAPClient {

    private LdapContext ldapContext = null;
    private Control[] ldapConnControls = null;
    private Logger logger = Logger.getLogger(LDAPClient.class.getName());

    /**
     * Constructor - Creates an LDAP Client object to interface with an LDAP Repository.
     *
     * @param ldapURL - Endpoint of the LDAP Repository
     * @throws javax.naming.NamingException
     */
    public LDAPClient(String ldapURL) throws NamingException {
        if (ldapURL == null) {
            ldapURL = ""; // to prevent NPE
        }
        // Set up the fast bind connection control
        ldapConnControls = new Control[]{new LDAPConnectionControl()};
        // Initializes a hashtable for creating the LDAP Context.
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.PROVIDER_URL, ldapURL);

        //@todo - Investigate the effect of uncommenting the following two lines ...AMS
        // env.put(Context.REFERRAL, "follow");
        // env.put("com.sun.jndi.ldap.connect.pool", "true");

        //create the LDAP Context
        ldapContext = new InitialLdapContext(env, ldapConnControls);
    }

    /**
     * Enables binding to LDAP which simulates authentication. The following quotes
     * from http://forums.oracle.com/forums/thread.jspa?threadID=1155584&tstart=0, detail this approach
     * of authentication:
     *
     * 1) Many developers attempt to use LDAP Directories as an authentication service.
     * While LDAP is a directory protocol primarily designed to search, add, delete and modify entries
     * stored in the directory, implicit in the protocol is the ability to authenticate LDAP connections
     * using a variety of authentication mechanisms.
     *
     * 2) The only way to verify a user's credentials, is to actually perform a LDAP bind.
     *
     * @param user - Could be either the DN or a <domain>\sAMAccountName for the user.
     * @param password  - User's credential.
     * @return boolean - Indicating successful or failed authentication/bind.
     */
    public boolean bind(String user, String password) {
        try {
            // The following null/empty check is needed as the ldap API does not throw 
            // an AuthenticationException for empty values. If the values are null, a NPE gets thrown.
            if (user == null || user.trim().length() ==0 ||
                    password == null || password.trim().length() == 0) {
                logger.error("Null/Empty user/password passed in to the bind method for LDAP CLient");
                return false;
            }
            ldapContext.addToEnvironment(Context.SECURITY_PRINCIPAL, user);
            ldapContext.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            ldapContext.reconnect(ldapConnControls);
            return true; // all is well 
        } catch (AuthenticationException e) {
            logger.error(user + " could not be authenticated using LDAPCLient", e);
        } catch (NamingException e) {
            logger.error(user + " could not be authenticated using LDAPCLient", e);
        }
        return false;
    }

    /**
     * Looks up user attributes stored in LDAP.
     * @param sAMAccountName - User's sAMAccountName - Cannot be null/empty.
     * @param baseDN  - The node from which searches are performed in AD, cannot be null/empty.
     * @param retAttrs - Contains a list of attributes that are being sought by this method. If null, all
     * attributes get returned.
     * @return Map - A Map containing Attribute IDs, each associated with a list of attributes.
     * @throws Exception
     */
    public Map lookupUserAttributes(String sAMAccountName, String baseDN, String[] retAttrs) {
        Map userAttributes = new HashMap();
        NamingEnumeration searchResults;
        try {
            searchResults = ldapContext.search(baseDN, getSearchFilter(sAMAccountName), getSearchControls(retAttrs));
            if (searchResults.hasMore()) {
                SearchResult sres = (SearchResult) searchResults.next(); // picking only the first element if multiple are returned ??
                userAttributes = extractUserAttributes(sres);
            } else {
                logger.error("User, " + sAMAccountName + ", not found in LDAP");
            }
            // if more results write out error ??
            if (searchResults.hasMore()) {
                logger.error("Multiple instances of User, " + sAMAccountName + ", found in LDAP. Using first...");
            }
        } catch (NamingException ex) {
            logger.error("Error retrieving user attributes using LDAPClient - ", ex);
        }
        return userAttributes;
    }

    /*
     * unbinds from LDAP
     */
    public void unbind() {
        if (ldapContext != null) {
            try {
                ldapContext.close();
            } catch (NamingException ex) {
                logger.warn("Error unbinding from LDAP Context - ", ex);
            }
        }
    }


    /* Private Utility Methods */
    //  Extracts attributes and stores in a HashMap
    private Map extractUserAttributes(SearchResult sres) {
        Map userAttributes = new HashMap();
        Attributes attrs = sres.getAttributes();
        try {
            if (attrs != null) {
                for (NamingEnumeration ne = attrs.getAll(); ne.hasMoreElements();) {
                    Attribute attr = (Attribute) ne.next();
                    String attrId = attr.getID();
                    List attrVals = new ArrayList();
                    userAttributes.put(attrId, attrVals);
                    NamingEnumeration vals = attr.getAll();
                    //System.out.println("AttrId[" + attrId + "] ");
                    // int count = 0;
                    while (vals.hasMore()) {
                        String value = vals.next().toString();
                        attrVals.add(value);
                        // System.out.println("AttrVal-" + ++count + "-[" + value + "] ");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Error extracting User Attributes using LDAP Client - ", ex);
        }

        return userAttributes;
    }

    // Builds a search filter based on sAMAccountName
    private String getSearchFilter(String sAMAccountName) {
        return new StringBuilder().append("(&(sAMAccountName=").append(sAMAccountName).append("*)").append("(&(cn=*)))").toString();
    }

    // Build Search controls
    private SearchControls getSearchControls(String[] retAttrs) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningObjFlag(true);
        searchCtls.setTimeLimit(0);
        searchCtls.setCountLimit(0L);
        searchCtls.setDerefLinkFlag(false);
        searchCtls.setReturningAttributes(retAttrs);

        return searchCtls;
    }

    /* Non-public supporting class representing a fast bind LDAP Control */
    /* Based on material found on c */
    class LDAPConnectionControl implements Control {

        @Override
        public byte[] getEncodedValue() {
            return null;
        }

        @Override
        public String getID() {
            return "1.2.840.113556.1.4.1781";  //  identifies LDAP fast bind
        }

        @Override
        public boolean isCritical() {
            return Control.NONCRITICAL;
        }
    }

    public static void main(String[] args) throws Exception {
       // LDAPClient adc = new LDAPClient("ldap://yourldap.com:389");
         LDAPClient adc = new LDAPClient("ldap://10.0.10.151:389");
       // if (adc.bind("testuser", "abc123")) {
          if (adc.bind("vtil\\user1s", "2ava6ezujEtr5Y")) {
            System.out.println(adc.lookupUserAttributes("thumbe", "OU=Users,OU=SETHS,DC=vtil,DC=vangent,DC=local", new String[]{"initials"}));
            adc.unbind();
        }
        else System.out.println("Could not bind");

    }
}
