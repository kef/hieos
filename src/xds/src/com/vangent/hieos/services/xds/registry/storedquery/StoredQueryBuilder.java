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
package com.vangent.hieos.services.xds.registry.storedquery;

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.metadata.structure.SQCodeAnd;
import com.vangent.hieos.xutil.metadata.structure.SQCodeOr;
import com.vangent.hieos.xutil.metadata.structure.SQCodedTerm;

import java.util.List;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class StoredQueryBuilder {

    private boolean where = false;
    private StringBuffer query = new StringBuffer();
    private boolean returnLeafClass;

    /**
     * 
     * @param returnLeafClass
     */
    public StoredQueryBuilder(boolean returnLeafClass) {
        this.returnLeafClass = returnLeafClass;
    }

    /**
     *
     * @return
     */
    public boolean isReturnLeafClass() {
        return returnLeafClass;
    }

    /**
     *
     * @param returnLeafClass
     */
    public void setReturnLeafClass(boolean returnLeafClass) {
        this.returnLeafClass = returnLeafClass;
    }

    /**
     * 
     * @return
     */
    public String getQuery() {
        return query.toString();
    }

    /**
     * Reinitialize the query buffer.
     */
    public void initQuery() {
        where = false;
        query = new StringBuffer();
    }

    /**
     * 
     * @param term
     */
    public void appendClassificationDeclaration(SQCodedTerm term) {
        if (term != null) {
            append(getClassificationDeclaration(term));
        }
    }

    /**
     *
     * @param term
     * @return
     */
    private String getClassificationDeclaration(SQCodedTerm term) {
        if (term instanceof SQCodeOr) {
            return getClassificationDeclaration((SQCodeOr) term);
        }
        if (term instanceof SQCodeAnd) {
            return getClassificationDeclaration((SQCodeAnd) term);
        }
        return null;
    }

    /**
     *
     * @param or
     * @return
     */
    private String getClassificationDeclaration(SQCodeOr or) {
        StringBuilder buf = new StringBuilder();
        buf.append(", Classification ").append(or.getCodeVarName()).append("\n");
        buf.append(", Slot ").append(or.getSchemeVarName()).append("\n");
        return buf.toString();
    }

    /**
     *
     * @param and
     * @return
     */
    private String getClassificationDeclaration(SQCodeAnd and) {
        StringBuilder buf = new StringBuilder();
        for (String name : and.getCodeVarNames()) {
            buf.append(", Classification ").append(name).append("\n");
        }
        for (String name : and.getSchemeVarNames()) {
            buf.append(", Slot ").append(name).append("\n");
        }
        return buf.toString();
    }
   
    /**
     *
     * @param term
     * @throws MetadataException
     */
    public void addCode(SQCodedTerm term) throws MetadataException {
        if (term instanceof SQCodeOr) {
            addCode((SQCodeOr) term);
        }
        if (term instanceof SQCodeAnd) {
            addCode((SQCodeAnd) term);
        }
    }

    /**
     *
     * @param term
     * @throws MetadataException
     */
    private void addCode(SQCodeOr term) throws MetadataException {
        and();
        append(" (");
        append(term.getCodeVarName());
        append(".classifiedobject=obj.id AND ");
        newline();
        append("  ");
        append(term.getCodeVarName());
        append(".classificationScheme='");
        append(term.classification);
        append("' AND ");
        newline();
        append(" ");
        append(term.getCodeVarName());
        append(".nodeRepresentation IN ");
        append(term.getCodes());
        append(" )");
        newline();

        and();
        append(" (");
        append(term.getSchemeVarName());
        append(".parent = ");
        append(term.getCodeVarName());
        append(".id AND   ");
        newline();
        append("  ");
        append(term.getSchemeVarName());
        append(".name_ = 'codingScheme' AND   ");
        newline();
        append("  ");
        append(term.getSchemeVarName());
        append(".value IN ");
        append(term.getSchemes());
        append(" )");
        newline();
    }

    /**
     *
     * @param term
     * @throws MetadataException
     */
    private void addCode(SQCodeAnd term) throws MetadataException {
        for (SQCodeOr or : term.getCodeOrs()) {
            addCode(or);
        }
    }

    // times come in as numeric values but convert them to string values to avoid numeric overflow
    /**
     * 
     * @param attributeName
     * @param fromVariable
     * @param toVariable
     * @param fromLimit
     * @param toLimit
     * @param variableName
     */
    public void addTimes(String attributeName, String fromVariable, String toVariable,
            String fromLimit, String toLimit, String variableName) {
        if (fromLimit != null) {
            // Parent:
            and();
            append(" (");
            append(fromVariable);
            append(".parent = " + variableName + ".id AND ");
            newline();
            // Name:
            append("  ");
            append(fromVariable);
            append(".name_ = '");
            append(attributeName);
            append("' AND     ");
            newline();
            // Value:
            append("  ");
            append(fromVariable);
            append(".value >= ");
            appendQuoted(fromLimit);
            append(" ) ");
            newline();
        }

        if (toLimit != null) {
            // Parent:
            and();
            append(" (");
            append(toVariable);
            append(".parent = " + variableName + ".id AND ");
            newline();
            // Name:
            append("  ");
            append(toVariable);
            append(".name_ = '");
            append(attributeName);
            append("' AND     ");
            newline();
            // Value:
            append("  ");
            append(toVariable);
            append(".value < ");
            appendQuoted(toLimit);
            append(" ) ");
            newline();
        }
    }

    /**
     * 
     */
    public void and() {
        if (!where) {
            append("AND");
        }
        where = false;
    }

    /**/
    /**
     *
     * @param varName
     */
    public void select(String varName) {
        if (this.returnLeafClass) {
            append("SELECT * ");
        } else {
            append("SELECT ");
            append(varName);
            append(".id ");
        }
    }

    /**
     *
     */
    public void where() {
        append("WHERE");
        where = true;
    }

    /**
     *
     * @param s
     */
    public void append(String s) {
        where = false;
        query.append(s);
    }

    /**
     *
     * @param s
     */
    public void appendQuoted(String s) {
        where = false;
        query.append("'");
        query.append(s);
        query.append("'");
    }

    /**
     *
     */
    public void newline() {
        query.append("\n");
    }

    /**
     *
     * @param list
     * @throws MetadataException
     */
    public void append(List list) throws MetadataException {
        where = false;
        query.append("(");
        boolean first_time = true;
        for (Object o : list) {
            //for (int i = 0; i < list.size(); i++) {
            if (!first_time) {
                query.append(",");
            }
            //Object o = list.get(i);
            if (o instanceof String) {
                query.append("'").append((String) o).append("'");
            } else if (o instanceof Integer) {
                query.append(((Integer) o).toString());
            } else {
                throw new MetadataException("Parameter value " + o + " cannot be decoded");
            }
            first_time = false;
        }
        query.append(")");
    }
}
