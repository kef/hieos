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
package com.vangent.hieos.policyutil.model.attribute;

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;

/**
 *
 * @author Bernie Thuman
 */
public class CodedValueAttribute extends Attribute {

    private CodedValue codedValue;

    /**
     * 
     * @return
     */
    public CodedValue getCodedValue() {
        return codedValue;
    }

    /**
     *
     * @param codedValue
     */
    public void setCodedValue(CodedValue codedValue) {
        this.codedValue = codedValue;
    }

    @Override
    public String getValue() {
        return codedValue.getCode() + "@" + codedValue.getCodeSystem();
    }
}
