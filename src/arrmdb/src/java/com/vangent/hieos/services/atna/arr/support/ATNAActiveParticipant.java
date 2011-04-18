/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.atna.arr.support;

import java.util.List;


/**
 *
 *  @author Adeola Odunlami
 */
public class ATNAActiveParticipant {
    private String uniqueID;
    private String userID;
    private String alternativeUserID;
    private String userName;
    private Boolean userIsRequestor;
    private List<ATNACodedValue> roleIDCodes;
    private Integer networkAccessPointTypeCode;
    private String networkAccessPointID;

    /**
     * @return the userID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * @param userID the userID to set
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * @return the alternativeUserID
     */
    public String getAlternativeUserID() {
        return alternativeUserID;
    }

    /**
     * @param alternativeUserID the alternativeUserID to set
     */
    public void setAlternativeUserID(String alternativeUserID) {
        this.alternativeUserID = alternativeUserID;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userIsRequestor
     */
    public Boolean getUserIsRequestor() {
        return userIsRequestor;
    }

    /**
     * @param userIsRequestor the userIsRequestor to set
     */
    public void setUserIsRequestor(Boolean userIsRequestor) {
        this.userIsRequestor = userIsRequestor;
    }

    /**
     * @return the networkAccessPointTypeCode
     */
    public Integer getNetworkAccessPointTypeCode() {
        return networkAccessPointTypeCode;
    }

    /**
     * @param networkAccessPointTypeCode the networkAccessPointTypeCode to set
     */
    public void setNetworkAccessPointTypeCode(Integer networkAccessPointTypeCode) {
        this.networkAccessPointTypeCode = networkAccessPointTypeCode;
    }

    /**
     * @return the networkAccessPointID
     */
    public String getNetworkAccessPointID() {
        return networkAccessPointID;
    }

    /**
     * @param networkAccessPointID the networkAccessPointID to set
     */
    public void setNetworkAccessPointID(String networkAccessPointID) {
        this.networkAccessPointID = networkAccessPointID;
    }

    /**
     * @return the roleIDCodes
     */
    public List<ATNACodedValue> getRoleIDCodes() {
        return roleIDCodes;
    }

    /**
     * @param roleIDCodes the roleIDCodes to set
     */
    public void setRoleIDCodes(List<ATNACodedValue> roleIDCodes) {
        this.roleIDCodes = roleIDCodes;
    }

    /**
     * @return the uniqueID
     */
    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * @param uniqueID the uniqueID to set
     */
    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }
}
