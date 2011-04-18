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
public class ATNAAuditSource {

    private String uniqueID;
    private String id;
    private String enterpriseSiteID;
    private List<ATNACodedValue> typeCodes;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the enterpriseSiteID
     */
    public String getEnterpriseSiteID() {
        return enterpriseSiteID;
    }

    /**
     * @param enterpriseSiteID the enterpriseSiteID to set
     */
    public void setEnterpriseSiteID(String enterpriseSiteID) {
        this.enterpriseSiteID = enterpriseSiteID;
    }

    /**
     * @return the typeCodes
     */
    public List<ATNACodedValue> getTypeCodes() {
        return typeCodes;
    }

    /**
     * @param typeCodes the typeCodes to set
     */
    public void setTypeCodes(List<ATNACodedValue> typeCodes) {
        this.typeCodes = typeCodes;
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