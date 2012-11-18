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
package com.vangent.hieos.empi.codes;

import com.vangent.hieos.subjectmodel.CodedValue;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Bernie Thuman
 */
public class CodeSystem {

    private String codeSystem;
    private String codeSystemName;
    private String codeSystemVersion;
    // Key = code
    private Map<String, CodedValue> codes = new HashMap<String, CodedValue>();

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
     * @param code
     * @param displayName
     */
    public void addCode(String code, String displayName) {
        CodedValue codedValue = new CodedValue();
        codedValue.setCode(code);
        codedValue.setDisplayName(displayName);
        codedValue.setCodeSystem(codeSystem);
        codedValue.setCodeSystemName(codeSystemName);
        this.codes.put(code, codedValue);
    }

    /**
     * 
     * @param code
     * @return
     */
    public CodedValue getCodedValue(String code) {
        return this.codes.get(code);
    }
}
