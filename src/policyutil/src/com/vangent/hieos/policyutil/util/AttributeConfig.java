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
package com.vangent.hieos.policyutil.util;

import com.vangent.hieos.xutil.hl7.formatutil.HL7FormatUtil;

/**
 *
 * @author Bernie Thuman
 */
public class AttributeConfig {

    private String id;
    private AttributeClassType classType;
    private AttributeType type;
    private AttributeFormat format;
    private String name;

    /**
     *
     */
    public enum AttributeClassType {

        /**
         *
         */
        SUBJECT_ID,
        /**
         *
         */
        RESOURCE_ID,
        /**
         *
         */
        ENVIRONMENT_ID,
        /**
         *
         */
        CLAIM_ID
    };

    /**
     *
     */
    public enum AttributeType {

        /**
         * 
         */
        STRING,
        /**
         *
         */
        HL7V3_CODED_VALUE,
        /**
         *
         */
        ANY
    };

    /**
     *
     */
    public enum AttributeFormat {

        /**
         *
         */
        STRING,
        /**
         *
         */
        XON_ID_ONLY,
        /**
         *
         */
        XCN_ID_ONLY,
        /**
         *
         */
        CNE_CODE_ONLY,
        /**
         *
         */
        CX,
        /**
         *
         */
        ANY
    };

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public AttributeClassType getClassType() {
        return classType;
    }

    /**
     *
     * @param classType
     */
    public void setClassType(AttributeClassType classType) {
        this.classType = classType;
    }

    /**
     * 
     * @return
     */
    public AttributeType getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(AttributeType type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public AttributeFormat getFormat() {
        return format;
    }

    /**
     *
     * @param format
     */
    public void setFormat(AttributeFormat format) {
        this.format = format;
    }

    /**
     * 
     * @param value
     * @return
     */
    public boolean isValidFormat(String value) {
        switch (format) {
            case XON_ID_ONLY:
                if (!HL7FormatUtil.isXON_Identifier(value)) {
                    return false;
                }
                break;
            case XCN_ID_ONLY:
                if (!HL7FormatUtil.isXCN_Identifier(value)) {
                    return false;
                }
                break;
            case CNE_CODE_ONLY:
                if (!HL7FormatUtil.isCNE_Code(value)) {
                    return false;
                }
                break;
            case CX:
                if (!HL7FormatUtil.isCX_Formatted(value)) {
                    return false;
                }
                break;
            default:
                return true;
        }
        return true;
    }
}
