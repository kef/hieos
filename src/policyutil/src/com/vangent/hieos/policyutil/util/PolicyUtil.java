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

import java.util.List;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeType;
import oasis.names.tc.xacml._2_0.context.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResourceType;

/**
 *
 * @author Bernie Thuman
 */
public class PolicyUtil {

    /**
     *
     * @param resourceType
     * @return
     */
    static public String getResourceId(ResourceType resourceType) {
        List<AttributeType> attributeTypes = resourceType.getAttribute();
        // Find the resource-id attribute.
        List<AttributeValueType> attributeValueTypes = PolicyUtil.findAttribute(attributeTypes, PolicyConstants.XACML_RESOURCE_ID);

        // Extract the string content (the value).
        if (attributeValueTypes != null && !attributeValueTypes.isEmpty()) {
            AttributeValueType attributeValueType = attributeValueTypes.get(0);
            List<Object> content = attributeValueType.getContent();
            if (content != null && !content.isEmpty()) {
                // Just handle string.
                return (String) content.get(0);  // Found.
            }
        }
        return null;  // Not found.
    }

    /**
     *
     * @param attributes
     * @param searchId
     * @return
     */
    static public List<AttributeValueType> findAttribute(List<AttributeType> attributeTypes, String searchId) {
        for (AttributeType attributeType : attributeTypes) {
            String id = attributeType.getAttributeId();
            if (id.equalsIgnoreCase(searchId)) {
                return attributeType.getAttributeValue();  // Found.
            }
        }
        return null;  // Not found.
    }
}
