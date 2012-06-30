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
package com.vangent.hieos.empi.match;

import com.vangent.hieos.hl7v3util.model.subject.InternalId;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author Bernie Thuman
 */
public class Record {

    private InternalId internalId;
    private List<Field> fields = new ArrayList<Field>();

    /**
     *
     * @return
     */
    public InternalId getInternalId() {
        return internalId;
    }

    /**
     * 
     * @param internalId
     */
    public void setId(InternalId internalId) {
        this.internalId = internalId;
    }

    /**
     *
     * @param field
     */
    public void addField(Field field) {
        fields.add(field);
    }

    /**
     *
     * @param name
     * @return
     */
    public Field getField(String name) {
        // FIXME(?): Should be OK with small lists versus using overhead of a Map.
        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase(name)) {
                return field;
            }
        }
        //System.out.println("Record: " + this);
        //System.out.println("+++ Field not found for field name = " + name);
        return null;
    }

    /**
     *
     * @param field
     */
    public void removeField(Field field) {
        if (field != null) {
            fields.remove(field);
        }
    }

    /**
     *
     * @return
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", internalId).append("fields", fields).toString();
    }
}
