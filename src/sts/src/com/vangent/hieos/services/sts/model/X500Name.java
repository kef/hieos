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
package com.vangent.hieos.services.sts.model;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

/**
 *
 * @author Bernie Thuman
 */
public class X500Name {

    private LdapName dn;

    /**
     * 
     * @param distinguishedName
     */
    public X500Name(String distinguishedName) {
        try {
            dn = new LdapName(distinguishedName);
        } catch (InvalidNameException ex) {
            // TBD: ...
        }
    }

    /*
    public String getCN() {
    // TBD.
    }

    public String getOU() {
    // TBD.
    }

    public String getO() {
    // TBD.
    }

    public String getL() {
    // TBD.
    }

    public String getST() {
    // TBD.
    }

    public String getC() {
    // TBD.
    }*/
    /**
     *
     * @param rdnName
     * @param replaceVal
     */
    public void replace(String rdnName, String replaceVal) {
        String searchRdnName = rdnName + "=";
        int index = this.find(searchRdnName);
        if (index != -1) {
            try {
                dn.remove(index);
                dn.add(index, searchRdnName + replaceVal);
            } catch (InvalidNameException ex) {
                // TBD
            }
        }
    }

    /**
    private String getRDNvalue(String rdnName)
    {
    String searchRdnName = rdnName + "=";
    int index = this.find(searchRdnName);
    if (index != -1)
    {
    String val = dn.get(index);
    // Now split out the searchRdnName part.
    val.
    return val.substring(2);

    }
    }*/

    /**
     *
     * @param searchRdnName
     * @return
     */
    private int find(String searchRdnName) {
        searchRdnName = searchRdnName.toLowerCase();
        int index = -1; // Not found.
        for (int i = 0; i < dn.size(); i++) {
            //System.out.println(dn.get(i));
            Rdn rdn = dn.getRdn(i);
            String rdnAsString = rdn.toString().toLowerCase();
            if (rdnAsString.startsWith(searchRdnName)) {
                //System.out.println("+++ found " + searchRdnName);
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        return dn.toString();
    }
}
