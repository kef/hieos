/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.xua.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * It holds xua releated properties, username password, xua enabled or not,
 * STSurl,STSUrI. The outPhaseHandler will use this object to get the xua
 * properties.
 * @author Fred Aabedi
 */
public class XUAObject {

    private String userName = null;
    private String password = null;
    private boolean xuaEnabled = false;
    private String stsURL;
    private String stsURI;
    private List soapActionsList = new ArrayList();

    /**
     * Constructor
     */
    public XUAObject() {
    }

    /**
     * set STS username
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * get STS UserName
     * @return userName
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * set STS Password
     * @param psw
     */
    public void setPassword(String psw) {
        this.password = psw;
    }

    /**
     * get STS Password
     * @return password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * set XUA enabled ot not
     * @param enabled
     */
    public void setXUAEnabled(boolean enabled) {
        this.xuaEnabled = enabled;
    }

    /**
     * Is XUAEnabled or not
     * @return xuaEnabled
     */
    public boolean isXUAEnabled() {
        return this.xuaEnabled;
    }

    /**
     * set STS URL
     * @param url
     */
    public void setSTSUrl(String url) {
        this.stsURL = url;
    }

    /**
     * get STS URL
     * @return stsURL
     */
    public String getSTSUrl() {
        return this.stsURL;
    }

    /**
     * set STS URI
     * @param uri
     */
    public void setSTSUri(String uri) {
        this.stsURI = uri;
    }

    /**
     * get STS URI
     * @return
     */
    public String getSTSUri() {
        return this.stsURI;
    }

    /**
     * set XUA Supported saop actions and maintain in a list
     * @param saopactions
     */
    public void setXUASupportedSOAPActions(String saopactions) {
        if (!soapActionsList.isEmpty()) {
            soapActionsList.clear();
        }
        String[] soapActions = saopactions.split(";");
        for (int x = 0; x < soapActions.length; x++) {
            soapActionsList.add(soapActions[x].toLowerCase());
        }
    }

    /**
     *
     * @param soapAction
     * @return
     */
    public boolean containsSOAPAction(String soapAction) {
        return this.soapActionsList.contains(soapAction.toLowerCase());
    }
}
