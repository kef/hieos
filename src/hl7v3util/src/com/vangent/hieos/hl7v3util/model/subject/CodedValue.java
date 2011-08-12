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
package com.vangent.hieos.hl7v3util.model.subject;

/**
 *
 * @author Bernie Thuman
 */
public class CodedValue {

    private String code;
    private String codeSystem;
    private String codeSystemName;
    private String codeSystemVersion;
    private String displayName;

    /**
     *
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     *
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     *
     * @return
     */
    public String getCodeSystem() {
        return codeSystem;
    }

    /**
     *
     * @param codeSystem
     */
    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    /**
     *
     * @return
     */
    public String getCodeSystemName() {
        return codeSystemName;
    }

    /**
     *
     * @param codeSystemName
     */
    public void setCodeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
    }

    /**
     *
     * @return
     */
    public String getCodeSystemVersion() {
        return codeSystemVersion;
    }

    /**
     *
     * @param codeSystemVersion
     */
    public void setCodeSystemVersion(String codeSystemVersion) {
        this.codeSystemVersion = codeSystemVersion;
    }

    /**
     *
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     *
     * @return
     */
    public String getCNEFormatted() {
        if (code == null) {
            return "UNKNOWN_CODE";
        }
        if (codeSystem == null || codeSystem.isEmpty()) {
            return code;
        }
        return code + "^" + codeSystem;
    }
}
