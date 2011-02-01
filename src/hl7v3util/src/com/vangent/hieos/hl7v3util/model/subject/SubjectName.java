/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v3util.model.subject;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectName {

    private String givenName;
    private String familyName;
    private String middleName;
    private String prefix;
    private String suffix;

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

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
}
