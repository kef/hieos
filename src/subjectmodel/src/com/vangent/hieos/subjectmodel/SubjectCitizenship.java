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
package com.vangent.hieos.subjectmodel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectCitizenship extends SubjectAbstractEntity {

    private CodedValue nationCode = null;
    private String nationName = null;

    /**
     *
     * @return
     */
    public CodedValue getNationCode() {
        return nationCode;
    }

    /**
     *
     * @param nationCode
     */
    public void setNationCode(CodedValue nationCode) {
        this.nationCode = nationCode;
    }

    /**
     *
     * @return
     */
    public String getNationName() {
        return nationName;
    }

    /**
     *
     * @param nationName
     */
    public void setNationName(String nationName) {
        this.nationName = nationName;
    }

    /**
     * 
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SubjectCitizenship copy = (SubjectCitizenship) super.clone();
        if (nationCode != null) {
            copy.nationCode = (CodedValue) nationCode.clone();
        }
        return copy;
    }

    /**
     *
     * @param listToClone
     * @return
     * @throws CloneNotSupportedException
     */
    public static List<SubjectCitizenship> clone(List<SubjectCitizenship> listToClone) throws CloneNotSupportedException {
        List<SubjectCitizenship> copy = null;
        if (listToClone != null) {
            copy = new ArrayList<SubjectCitizenship>();
            for (SubjectCitizenship elementToClone : listToClone) {
                SubjectCitizenship clonedElement = (SubjectCitizenship) elementToClone.clone();
                copy.add(clonedElement);
            }
        }
        return copy;
    }
}
