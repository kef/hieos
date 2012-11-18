/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
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
public class SubjectName extends SubjectAbstractEntity implements Cloneable {

    private String givenName;
    private String familyName;
    private String middleName;
    private String prefix;
    private String suffix;
    // Not really happy about placing this here ... but since Subject(s) are used to support
    // searching, this is required to be placed here.
    private boolean fuzzySearchMode = false;

    /**
     *
     */
    public void nullEmptyFields() {
        if (givenName != null && givenName.length() == 0) {
            givenName = null;
        }
        if (familyName != null && familyName.length() == 0) {
            familyName = null;
        }
        if (middleName != null && middleName.length() == 0) {
            middleName = null;
        }
        if (prefix != null && prefix.length() == 0) {
            prefix = null;
        }
        if (suffix != null && suffix.length() == 0) {
            suffix = null;
        }
    }

    /**
     *
     * @return
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     *
     * @param familyName
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    /**
     *
     * @return
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     *
     * @param givenName
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     *
     * @return
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     *
     * @param prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     *
     * @return
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     *
     * @param suffix
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     *
     * @return
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     *
     * @param middleName
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * 
     * @return
     */
    public boolean isFuzzySearchMode() {
        return fuzzySearchMode;
    }

    /**
     *
     * @param fuzzySearchMode
     */
    public void setFuzzySearchMode(boolean fuzzySearchMode) {
        this.fuzzySearchMode = fuzzySearchMode;
    }

    /**
     * 
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     *
     * @param listToClone
     * @return
     * @throws CloneNotSupportedException
     */
    public static List<SubjectName> clone(List<SubjectName> listToClone) throws CloneNotSupportedException {
        List<SubjectName> copy = null;
        if (listToClone != null) {
            copy = new ArrayList<SubjectName>();
            for (SubjectName elementToClone : listToClone) {
                SubjectName clonedElement = (SubjectName) elementToClone.clone();
                copy.add(clonedElement);
            }
        }
        return copy;
    }
}
