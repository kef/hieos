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

/**
 *
 * @author Bernie Thuman
 */
public class AttributeConfig {

    private String id;
    private String type;
    private String name;

    /**
     *
     */
    public enum AttributeIdType {

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
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(String type) {
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
    public AttributeType getAttributeType() {
        if (type.equalsIgnoreCase("any")) {
            return AttributeType.ANY;
        }
        // Default.
        return AttributeType.STRING;
    }
}
