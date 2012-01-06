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

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class CodesConfig {

    private final static Logger logger = Logger.getLogger(CodesConfig.class);
    private static String CODE_SETS = "code-sets.code-set";
    private static String CODE_SET_MAPPINGS = "code-set-mappings.code-set-mapping";
    private static String CODED_TYPE_ATTRIBUTE = "[@codedType]";
    private static String CODE_SYSTEM_ATTRIBUTE = "[@codeSystem]";
    private static String CODE_SYSTEM_NAME_ATTRIBUTE = "[@codeSystemName]";
    private static String CODE_SYSTEM_VERSION_ATTRIBUTE = "[@codeSystemVersion]";
    private static String CODE = "code";
    private static String VALUE_ATTRIBUTE = "[@value]";
    private static String DISPLAY_NAME_ATTRIBUTE = "[@displayName]";
    // Key = code system (versioning is not used).
    private Map<String, CodeSystem> codeSystemsByName = new HashMap<String, CodeSystem>();
    // Key = type
    private Map<String, CodeSystem> codeSystemsByType = new HashMap<String, CodeSystem>();

    /**
     *
     */
    public enum CodedType {

        /**
         *
         */
        GENDER,
        /**
         *
         */
        MARITAL_STATUS,
        /**
         *
         */
        RELIGIOUS_AFFILIATION,
        /**
         *
         */
        RACE,
        /**
         *
         */
        ETHNIC_GROUP,
        /**
         *
         */
        PERSONAL_RELATIONSHIP,
        /**
         *
         */
        LANGUAGE,
        /**
         *
         */
        NATION
    };

    /**
     *
     */
    public CodesConfig() {
    }

    /**
     *
     * @param codesConfigLocation
     * @throws EMPIException
     */
    public void loadConfiguration(String codesConfigLocation) throws EMPIException {
        try {
            XMLConfiguration xmlConfig = new XMLConfiguration(codesConfigLocation);

            // First load code sets (and index by code system name).
            List hcCodeSets = xmlConfig.configurationsAt(CODE_SETS);
            for (Iterator it = hcCodeSets.iterator(); it.hasNext();) {
                HierarchicalConfiguration hcCodeSet = (HierarchicalConfiguration) it.next();
                this.loadCodeSet(hcCodeSet);
            }

            // Now load mappings (and index by code system type).
            List hcCodeSetMappings = xmlConfig.configurationsAt(CODE_SET_MAPPINGS);
            for (Iterator it = hcCodeSetMappings.iterator(); it.hasNext();) {
                HierarchicalConfiguration hcCodeSetMapping = (HierarchicalConfiguration) it.next();
                this.loadCodeSetMapping(hcCodeSetMapping);
            }
        } catch (ConfigurationException ex) {
            throw new EMPIException(
                    "EMPIConfig: Could not load codes configuration from " + codesConfigLocation + " " + ex.getMessage());
        }
    }

    /**
     *
     * @param hc
     */
    private void loadCodeSet(HierarchicalConfiguration hc) {
        String codeSystem = hc.getString(CODE_SYSTEM_ATTRIBUTE);
        String codeSystemName = hc.getString(CODE_SYSTEM_NAME_ATTRIBUTE);
        String codeSystemVersion = hc.getString(CODE_SYSTEM_VERSION_ATTRIBUTE);
        CodeSystem codeSystemObj = new CodeSystem();
        codeSystemObj.setCodeSystem(codeSystem);
        codeSystemObj.setCodeSystemName(codeSystemName);
        codeSystemObj.setCodeSystemVersion(codeSystemVersion);
        this.codeSystemsByName.put(codeSystem, codeSystemObj);
        // Now, get list of codes.
        List hcCodes = hc.configurationsAt(CODE);
        for (Iterator it = hcCodes.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcCode = (HierarchicalConfiguration) it.next();
            String code = hcCode.getString(VALUE_ATTRIBUTE);
            String displayName = hcCode.getString(DISPLAY_NAME_ATTRIBUTE);
            codeSystemObj.addCode(code, displayName);
        }
    }

    /**
     * 
     * @param hc
     */
    private void loadCodeSetMapping(HierarchicalConfiguration hc) {
        String codedType = hc.getString(CODED_TYPE_ATTRIBUTE);
        String codeSystem = hc.getString(CODE_SYSTEM_ATTRIBUTE);
        CodeSystem codeSystemObj = this.getCodeSystemByName(codeSystem);
        this.codeSystemsByType.put(codedType, codeSystemObj);
    }

    /**
     *
     * @param code
     * @param codeSystem
     * @return
     */
    public boolean isValidCode(String code, String codeSystem) {
        CodedValue codedValue = this.getCodedValue(code, codeSystem);
        return codedValue != null;
    }

    /**
     *
     * @param code
     * @param codedType
     * @return
     */
    public boolean isValidCode(String code, CodedType codedType) {
        CodedValue codedValue = this.getCodedValue(code, codedType);
        return codedValue != null;
    }

    /**
     *
     * @param code
     * @param codeSystem
     * @return
     */
    public CodedValue getCodedValue(String code, String codeSystem) {
        // First lookup code system.
        CodeSystem codeSystemObj = this.getCodeSystemByName(codeSystem);
        if (codeSystemObj != null) {
            // Lookup code within the code system.
            return codeSystemObj.getCodedValue(code);
        } else {
            return null;
        }
    }

    /**
     * 
     * @param code
     * @param codedType
     * @return
     */
    public CodedValue getCodedValue(String code, CodedType codedType) {
        // First lookup code system.
        CodeSystem codeSystemObj = this.getCodeSystemByType(codedType);
        if (codeSystemObj != null) {
            // Lookup code within the code system.
            return codeSystemObj.getCodedValue(code);
        } else {
            return null;
        }
    }

    /**
     *
     * @param codeSystem
     * @return
     */
    public CodeSystem getCodeSystemByName(String codeSystemName) {
        return this.codeSystemsByName.get(codeSystemName);
    }

    /**
     * 
     * @param codedType
     * @return
     */
    public CodeSystem getCodeSystemByType(CodedType codedType) {
        return this.codeSystemsByType.get(codedType.toString());
    }
}
