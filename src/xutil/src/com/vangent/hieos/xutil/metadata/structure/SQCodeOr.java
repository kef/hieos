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
package com.vangent.hieos.xutil.metadata.structure;

import com.vangent.hieos.xutil.exception.XdsInternalException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author NIST (Adapated by Bernie Thuman).
 */
public class SQCodeOr extends SQCodedTerm {

    public class CodeLet {

        public String code;
        public String scheme;

        public CodeLet(String value) throws XdsInternalException {
            String[] a = value.split("\\^");
            if (a.length != 3 || a[0] == null || a[0].equals("") || a[2] == null || a[2].equals("")) {
                throw new XdsInternalException("CodeLet: code value " + value + "  is not in CE format (code^^scheme)");
            }
            code = a[0];
            scheme = a[2];
        }

        public String toString() {
            return code + "^^" + scheme;
        }
    }
    String varname;
    int index;   // used to make varname unique
    public List<CodeLet> values;
    public String classification;   // uuid

    /**
     *
     * @param varname
     * @param classification
     */
    public SQCodeOr(String varname, String classification) {
        this.varname = varname;
        this.classification = classification;
        index = 0;  // means no index
        values = new ArrayList<CodeLet>();
    }

    /**
     *
     * @return
     */
    public String toString() {
        return "SQCodeOr: [\n" +
                "varname=" + varname + "\n" +
                "index=" + index + "\n" +
                "values=" + values + "\n" +
                "classification=" + classification + "\n" +
                "]\n";
    }

    /**
     *
     * @param i
     */
    public void setIndex(int i) {  // so unique names can be generated
        index = i;
    }

    /**
     *
     * @param value
     * @throws XdsInternalException
     */
    public void addValue(String value) throws XdsInternalException {
        values.add(new CodeLet(value));
    }

    /**
     *
     * @param values
     * @throws XdsInternalException
     */
    public void addValues(List<String> values) throws XdsInternalException {
        for (String value : values) {
            addValue(value);
        }
    }

    /**
     *
     * @return
     */
    public List<String> getCodes() {
        List<String> a = new ArrayList<String>();

        for (CodeLet cl : values) {
            a.add(cl.code);
        }
        return a;
    }

    /**
     *
     * @return
     */
    public List<String> getSchemes() {
        List<String> a = new ArrayList<String>();

        for (CodeLet cl : values) {
            a.add(cl.scheme);
        }
        return a;
    }

    /**
     *
     * @return
     */
    public String getCodeVarName() {
        if (index == 0) {
            return codeVarName(varname) + "_code";
        }
        return codeVarName(varname) + "_code_" + index;
    }

    /**
     *
     * @return
     */
    public String getSchemeVarName() {
        if (index == 0) {
            return codeVarName(varname) + "_scheme";
        }
        return codeVarName(varname) + "_scheme_" + index;
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return values.size() == 0;
    }
}
