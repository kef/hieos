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
public class SubjectLanguage extends SubjectAbstractEntity implements Cloneable {

    private CodedValue languageCode = null;
    private Boolean preferenceIndicator = null;

    /**
     *
     * @return
     */
    public CodedValue getLanguageCode() {
        return languageCode;
    }

    /**
     *
     * @param languageCode
     */
    public void setLanguageCode(CodedValue languageCode) {
        this.languageCode = languageCode;
    }

    /**
     *
     * @return
     */
    public Boolean getPreferenceIndicator() {
        return preferenceIndicator;
    }

    /**
     * 
     * @param preferenceIndicator
     */
    public void setPreferenceIndicator(Boolean preferenceIndicator) {
        this.preferenceIndicator = preferenceIndicator;
    }

    /**
     * 
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SubjectLanguage copy = (SubjectLanguage) super.clone();
        if (languageCode != null) {
            copy.languageCode = (CodedValue) languageCode.clone();
        }
        return copy;
    }

    /**
     *
     * @param listToClone
     * @return
     * @throws CloneNotSupportedException
     */
    public static List<SubjectLanguage> clone(List<SubjectLanguage> listToClone) throws CloneNotSupportedException {
        List<SubjectLanguage> copy = null;
        if (listToClone != null) {
            copy = new ArrayList<SubjectLanguage>();
            for (SubjectLanguage elementToClone : listToClone) {
                SubjectLanguage clonedElement = (SubjectLanguage) elementToClone.clone();
                copy.add(clonedElement);
            }
        }
        return copy;
    }
}
