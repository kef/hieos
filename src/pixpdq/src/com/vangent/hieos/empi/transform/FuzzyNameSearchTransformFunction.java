/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.transform;

import com.vangent.hieos.hl7v3util.model.subject.SubjectName;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author Bernie Thuman
 */
public class FuzzyNameSearchTransformFunction extends TransformFunction {

    private static String PARAM_FIELD_NAME = "field-name";

    /**
     *
     * @param obj
     * @return
     */
    public Object transform(Object obj) {
        SubjectName subjectName = (SubjectName) obj;
        // Get search mode.
        boolean isFuzzySearchMode = subjectName.isFuzzySearchMode();

        // DEBUG:
        System.out.println(this.getClass().toString()
                + " fuzzy search mode = " + isFuzzySearchMode);

        // Get field name to access from SubjectName.
        String fieldName = this.getFunctionConfig().getParameter(PARAM_FIELD_NAME);

        // DEBUG:
        System.out.println("... field name = " + fieldName);
        String fieldValue = null;
        try {
            // Get field value.
            Object fieldValueObj = PropertyUtils.getProperty(subjectName, fieldName);
            if (fieldValueObj != null && isFuzzySearchMode) {
                fieldValue = (String) fieldValueObj;
                // Convert to lower case and add a "%" for LIKE clause.
                fieldValue = fieldValue.toLowerCase();
                fieldValue = fieldValue + "%";
            }
        } catch (Exception ex) {
            System.out.println("Unable to access '" + fieldName + ": " + ex.getMessage());
        }
        return fieldValue;
    }
}
