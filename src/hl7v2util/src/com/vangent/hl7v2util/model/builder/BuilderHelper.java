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

package com.vangent.hl7v2util.model.builder;

import ca.uhn.hl7v2.model.v231.datatype.CE;
import ca.uhn.hl7v2.model.v231.datatype.ID;
import ca.uhn.hl7v2.model.v231.datatype.IS;
import ca.uhn.hl7v2.model.v231.datatype.NM;
import com.vangent.hieos.subjectmodel.CodedValue;

// FIXME: How to handle > HL7v2.3.1 without breaking code.

/**
 *
 * @author Bernie Thuman
 */
public class BuilderHelper {
/**
     *
     * @param value
     * @return
     */
    public static CodedValue buildCodedValue(CE value) {
        CodedValue codedValue = null;
        // Relies on CAND
        if (value != null && value.getIdentifier() != null && value.getIdentifier().getValue() != null) {
            codedValue = new CodedValue();
            codedValue.setCode(value.getIdentifier().getValue());
        }
        return codedValue;
    }

    /**
     *
     * @param value
     * @return
     */
    public static CodedValue buildCodedValue(IS value) {
        CodedValue codedValue = null;
        // Relies on CAND
        if (value != null && value.getValue() != null) {
            codedValue = new CodedValue();
            codedValue.setCode(value.getValue());
        }
        return codedValue;
    }

    /**
     *
     * @param value
     * @return
     */
    public static Integer buildInteger(NM value) {
        Integer integerValue = null;
        // Relies on CAND
        if (value != null && value.getValue() != null) {
            integerValue = new Integer(value.getValue());
        }
        return integerValue;
    }

    /**
     *
     * @param value
     * @return
     */
    public static Boolean buildBoolean(ID value) {
        Boolean booleanValue = null;
        // Relies on CAND
        if (value != null && value.getValue() != null) {
            booleanValue = value.getValue().equalsIgnoreCase("Y") ? true : false;
        }
        return booleanValue;
    }

}
